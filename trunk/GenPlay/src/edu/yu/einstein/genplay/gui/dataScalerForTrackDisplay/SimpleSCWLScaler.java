/*******************************************************************************
 * GenPlay, Einstein Genome Analyzer
 * Copyright (C) 2009, 2014 Albert Einstein College of Medicine
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * Authors: Julien Lajugie <julien.lajugie@einstein.yu.edu>
 *          Nicolas Fourel <nicolas.fourel@einstein.yu.edu>
 *          Eric Bouhassira <eric.bouhassira@einstein.yu.edu>
 * 
 * Website: <http://genplay.einstein.yu.edu>
 ******************************************************************************/
package edu.yu.einstein.genplay.gui.dataScalerForTrackDisplay;

import java.util.ArrayList;
import java.util.List;

import edu.yu.einstein.genplay.core.manager.project.ProjectManager;
import edu.yu.einstein.genplay.dataStructure.chromosome.Chromosome;
import edu.yu.einstein.genplay.dataStructure.genomeWindow.GenomeWindow;
import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.SCWListView.generic.GenericSCWListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.SCWList;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.SimpleSCWList.SimpleSCWList;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListView;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.ScoredChromosomeWindow;
import edu.yu.einstein.genplay.exception.ExceptionManager;
import edu.yu.einstein.genplay.exception.exceptions.InvalidChromosomeException;
import edu.yu.einstein.genplay.util.FloatLists;
import edu.yu.einstein.genplay.util.ListView.ChromosomeWindowListViews;


/**
 * This class scales a {@link SimpleSCWList}to be displayed on a track.
 * @author Julien Lajugie
 */
class SimpleSCWLScaler implements DataScalerForTrackDisplay<SCWList, ListView<ScoredChromosomeWindow>> {

	/**
	 * Threads that computes the scaled data for the chromosome currently displayed
	 * at the current zoom level and screen resolution.
	 * @author Julien Lajugie
	 */
	private class ScalerThread extends Thread {

		@Override
		public void run() {
			Thread thisThread = Thread.currentThread();
			setName("Data Scaler Thread");
			ListView<ScoredChromosomeWindow> currentChromosomeList;
			try {
				currentChromosomeList = dataToScale.get(scaledChromosome);
			} catch (InvalidChromosomeException e) {
				ExceptionManager.getInstance().caughtException(e);
				return;
			}
			if ((currentChromosomeList == null) || currentChromosomeList.isEmpty() || (scaledXRatio >= 1)) {
				scaledSCWList = currentChromosomeList;
			} else {
				if (currentChromosomeList.size() > 0) {
					// compute the width on the genome that takes up 1 pixel on the screen
					double pixelGenomicWidth = 1 / scaledXRatio;
					GenericSCWListViewBuilder scaledSCWListBuilder = new GenericSCWListViewBuilder();
					List<Float> scoreList = new ArrayList<Float>();
					int i = 0;
					while (i < currentChromosomeList.size()) {
						if (thisThread != scalerThread) {
							scaledSCWList = null;
							return;
						}
						int currentStart = currentChromosomeList.get(i).getStart();
						int currentStop = currentChromosomeList.get(i).getStop();
						float currentScore = currentChromosomeList.get(i).getScore();
						scoreList.add(currentScore);
						// we merge two windows together if there is a next window
						// and if the gap between the current window and the next one is smaller than 1 pixel
						// and if the score of the next window is equal to the score of the current one
						while (((i + 1) < currentChromosomeList.size())
								&& ((((currentChromosomeList.get(i + 1).getStart() - currentStop) < pixelGenomicWidth) && (currentChromosomeList.get(i + 1).getScore() == currentScore))
										|| ((currentChromosomeList.get(i + 1).getStop() - currentStart) < pixelGenomicWidth))) {

							i++;
							// the new stop position is the max of the current stop and the stop of the new merged interval
							currentStop = Math.max(currentStop, currentChromosomeList.get(i).getStop());
							currentScore = currentChromosomeList.get(i).getScore();
							if (currentScore != 0) {
								scoreList.add(currentScore);
							}
						}
						currentScore = FloatLists.average(scoreList);
						scoreList.clear();
						scaledSCWListBuilder.addElementToBuild(currentStart, currentStop, currentScore);
						i++;
					}
					scaledSCWList = scaledSCWListBuilder.getListView();
				}
			}
			DataScalerManager.getInstance().redrawLayers(SimpleSCWLScaler.this);
		}
	}


	/** Thread that scales the data */
	private ScalerThread scalerThread;

	/** Scaled chromosome */
	private Chromosome scaledChromosome;

	/** Scaled xRatio (ratio between the track width and the displayed genome window width) */
	private double scaledXRatio;

	/** The scw list scaled for a specified chromosome and xRatio */
	private ListView<ScoredChromosomeWindow> scaledSCWList;

	/** Data to be scaled for track display */
	private final SCWList dataToScale;


	/**
	 * Creates an instance of {@link SimpleSCWLScaler}
	 * @param dataToScale the data that needs to be scaled
	 */
	SimpleSCWLScaler(SCWList dataToScale) {
		this.dataToScale = dataToScale;
	}


	@Override
	public ListView<ScoredChromosomeWindow> getDataScaledForTrackDisplay() {
		GenomeWindow projectWindow = ProjectManager.getInstance().getProjectWindow().getGenomeWindow();
		double projectXRatio = ProjectManager.getInstance().getProjectWindow().getXRatio();
		// if the chromosome or the xRatio of the project window changed we need to rescale the data
		if (!projectWindow.getChromosome().equals(scaledChromosome) || (projectXRatio != scaledXRatio)) {
			scaledChromosome = projectWindow.getChromosome();
			scaledXRatio = projectXRatio;
			scaleChromosome();
		}
		if (scaledSCWList == null) {
			return null;
		}
		return ChromosomeWindowListViews.subList(scaledSCWList, projectWindow.getStart(), projectWindow.getStop());
	}


	@Override
	public SCWList getDataToScale() {
		return dataToScale;
	}


	/**
	 * Starts the thread that scales the current chromosome
	 * for the current zoom level and screen resolution
	 */
	private void scaleChromosome() {
		scaledSCWList = null;
		scalerThread = new ScalerThread();
		scalerThread.start();
	}
}

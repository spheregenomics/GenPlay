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
package edu.yu.einstein.genplay.core.operation.geneList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import edu.yu.einstein.genplay.core.manager.project.ProjectChromosomes;
import edu.yu.einstein.genplay.core.manager.project.ProjectManager;
import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operationPool.OperationPool;
import edu.yu.einstein.genplay.dataStructure.chromosome.Chromosome;
import edu.yu.einstein.genplay.dataStructure.enums.Strand;
import edu.yu.einstein.genplay.dataStructure.gene.Gene;
import edu.yu.einstein.genplay.dataStructure.gene.SimpleGene;
import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.geneListView.GeneListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.ListOfListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.geneList.GeneList;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.geneList.SimpleGeneList;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListView;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListViewBuilder;
import edu.yu.einstein.genplay.util.ListView.SCWListViews;


/**
 * Extracts intervals relative to gene positions
 * @author Julien Lajugie
 * @author Chirag Gorasia
 */
public class GLOExtractIntervals implements Operation<GeneList> {

	private final GeneList 	geneList;			// input list
	private final int 		startDistance;		// distance from the start reference
	private final int 		startFrom;			// start reference (see constants below)
	private final int 		stopDistance;		// distant from the stop reference
	private final int 		stopFrom;			// stop reference
	private boolean			stopped = false;	// true if the operation must be stopped


	/**
	 * before the start position (used for interval extraction)
	 */
	public static final int BEFORE_START = 0;


	/**
	 * after the start position (used for interval extraction)
	 */
	public static final int AFTER_START = 1;


	/**
	 * before the middle position (used for interval extraction)
	 */
	public static final int BEFORE_MIDDLE = 2;


	/**
	 * after the middle position (used for interval extraction)
	 */
	public static final int AFTER_MIDDLE = 3;


	/**
	 * before the stop position (used for interval extraction)
	 */
	public static final int BEFORE_STOP = 4;


	/**
	 * after the stop position (used for interval extraction)
	 */
	public static final int AFTER_STOP = 5;


	/**
	 * Extracts intervals relative to gene positions
	 * @param geneList input list
	 * @param startDistance distance from the start reference
	 * @param startFrom start reference (see constants below)
	 * @param stopDistance distant from the stop reference
	 * @param stopFrom stop reference
	 */
	public GLOExtractIntervals(GeneList geneList, int startDistance, int startFrom, int stopDistance, int stopFrom) {
		this.geneList = geneList;
		this.startDistance = startDistance;
		this.startFrom = startFrom;
		this.stopDistance = stopDistance;
		this.stopFrom = stopFrom;
	}


	@Override
	public GeneList compute() throws Exception {
		ProjectChromosomes projectChromosomes = ProjectManager.getInstance().getProjectChromosomes();
		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<Void>> threadList = new ArrayList<Callable<Void>>();
		ListViewBuilder<Gene> lvbPrototype = new GeneListViewBuilder();
		final ListOfListViewBuilder<Gene> resultListBuilder = new ListOfListViewBuilder<Gene>(lvbPrototype);

		for (final Chromosome chromosome: projectChromosomes) {
			final ListView<Gene> currentList = geneList.get(chromosome);

			Callable<Void> currentThread = new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					if (currentList != null) {
						List<Gene> geneListTmp = new ArrayList<Gene>();
						for (int j = 0; (j < currentList.size()) && !stopped; j++) {
							Gene currentGene = currentList.get(j);
							// search the new start
							int newStart = 0;
							int chromoLength = chromosome.getLength();
							switch (startFrom) {
							case BEFORE_START:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStart = Math.max(0, currentGene.getStart() - startDistance);
								} else {
									newStart = Math.min(chromosome.getLength(), currentGene.getStop() + startDistance);
								}
								break;
							case AFTER_START:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStart = Math.min(chromoLength, currentGene.getStart() + startDistance);
								} else {
									newStart = Math.max(0, currentGene.getStop() - startDistance);
								}
								break;
							case BEFORE_MIDDLE:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStart = Math.max(0, ((currentGene.getStop() + currentGene.getStart())/2) - startDistance);
								} else {
									newStart = Math.min(chromoLength, ((currentGene.getStart() + currentGene.getStop())/2) + startDistance);
								}
								break;
							case AFTER_MIDDLE:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStart = Math.min(chromoLength, ((currentGene.getStop() + currentGene.getStart())/2) + startDistance);
								} else {
									newStart = Math.max(0, ((currentGene.getStart() + currentGene.getStop())/2) - startDistance);
								}
								break;
							case BEFORE_STOP:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStart = Math.max(0, currentGene.getStop() - startDistance);
								} else {
									newStart = Math.min(chromoLength, currentGene.getStart() + startDistance);
								}
								break;
							case AFTER_STOP:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStart = Math.min(chromoLength, currentGene.getStop() + startDistance);
								} else {
									newStart = Math.max(0, currentGene.getStart() - startDistance);
								}
								break;
							default:
								// invalid argument
								throw new IllegalArgumentException("Invalid Start Reference");
							}
							// search the new stop
							int newStop = 0;
							switch (stopFrom) {
							case BEFORE_START:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStop = Math.max(0, currentGene.getStart() - stopDistance);
								} else {
									newStop =  Math.min(chromoLength, currentGene.getStop() + stopDistance);
								}
								break;
							case AFTER_START:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStop = Math.min(chromoLength, currentGene.getStart() + stopDistance);
								} else {
									newStop = Math.max(0, currentGene.getStop() - stopDistance);
								}
								break;
							case BEFORE_MIDDLE:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStop = Math.max(0, ((currentGene.getStop() + currentGene.getStart())/2) - stopDistance);
								} else {
									newStop =  Math.min(chromoLength, ((currentGene.getStart() + currentGene.getStop())/2) + stopDistance);
								}
								break;
							case AFTER_MIDDLE:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStop = Math.min(chromoLength, ((currentGene.getStop() + currentGene.getStart())/2) + stopDistance);
								} else {
									newStop = Math.max(0, ((currentGene.getStart() + currentGene.getStop())/2) - stopDistance);
								}
								break;
							case BEFORE_STOP:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStop = Math.max(0, currentGene.getStop() - stopDistance);
								} else {
									newStop =  Math.min(chromoLength, currentGene.getStart() + stopDistance);
								}
								break;
							case AFTER_STOP:
								if (currentGene.getStrand() == Strand.FIVE) {
									newStop = Math.min(chromoLength, currentGene.getStop() + stopDistance);
								} else {
									newStop = Math.max(0, currentGene.getStart() - stopDistance);
								}
								break;
							default:
								// invalid argument
								throw new IllegalArgumentException("Invalid Stop Reference");
							}
							Gene geneToAdd;
							// add the new gene
							if ((newStart < newStop) && (currentGene.getStrand() == Strand.FIVE)) {
								geneToAdd = new SimpleGene(
										currentGene.getName(),
										currentGene.getStrand(),
										newStart,
										newStop,
										currentGene.getScore(),
										SCWListViews.createGenericSCWListView(newStart, newStop, Float.NaN));
								geneListTmp.add(geneToAdd);
							} else if ((newStart > newStop) && (currentGene.getStrand() == Strand.THREE)) {
								geneToAdd = new SimpleGene(
										currentGene.getName(),
										currentGene.getStrand(),
										newStop,
										newStart,
										currentGene.getScore(),
										SCWListViews.createGenericSCWListView(newStop, newStart, Float.NaN));
								geneListTmp.add(geneToAdd);
							}
						}
						Collections.sort(geneListTmp);
						for (Gene currentGene: geneListTmp) {
							resultListBuilder.addElementToBuild(chromosome, currentGene);
						}
					}
					// tell the operation pool that a chromosome is done
					op.notifyDone();
					return null;
				}
			};

			threadList.add(currentThread);
		}
		op.startPool(threadList);
		List<ListView<Gene>> data = resultListBuilder.getGenomicList();
		return new SimpleGeneList(data, geneList.getGeneScoreType(), geneList.getGeneDBURL());
	}


	/**
	 * @return a string representing the distance from argument.
	 * See constant on top of the class
	 */
	private String distanceFromToString(int distanceFrom) {
		switch (distanceFrom) {
		case AFTER_MIDDLE:
			return "after gene middle positions";
		case AFTER_START:
			return "after gene start positions";
		case AFTER_STOP:
			return "after gene stop positions";
		case BEFORE_MIDDLE:
			return "before gene middle positions";
		case BEFORE_START:
			return "before gene start positions";
		case BEFORE_STOP:
			return "before gene stop positions";
		default:
			return null;
		}

	}


	@Override
	public String getDescription() {
		return "Operation: Extract intervals starting "
				+ startDistance + " bp " + distanceFromToString(startFrom)
				+ " and ending "
				+ stopDistance + " bp " + distanceFromToString(stopFrom) ;
	}


	@Override
	public String getProcessingDescription() {
		return "Extracting Intervals";
	}


	@Override
	public int getStepCount() {
		return 1;
	}


	@Override
	public void stop() {
		stopped = true;
	}
}

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
package edu.yu.einstein.genplay.core.operation.binList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import edu.yu.einstein.genplay.core.manager.project.ProjectChromosomes;
import edu.yu.einstein.genplay.core.manager.project.ProjectManager;
import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operationPool.OperationPool;
import edu.yu.einstein.genplay.dataStructure.chromosome.Chromosome;
import edu.yu.einstein.genplay.dataStructure.enums.ScoreOperation;
import edu.yu.einstein.genplay.dataStructure.enums.Strand;
import edu.yu.einstein.genplay.dataStructure.gene.Gene;
import edu.yu.einstein.genplay.dataStructure.gene.SimpleGene;
import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.geneListView.GeneListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.ListOfListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.binList.BinList;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.geneList.GeneList;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.geneList.SimpleGeneList;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListView;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.ScoredChromosomeWindow;
import edu.yu.einstein.genplay.util.ListView.SCWListViews;


/**
 * Defines regions as "islands" of non zero value bins
 * separated by more than a specified number of zero value bins.
 * Computes the average on these regions.
 * Returns a new {@link GeneList} with the defined regions having their average/max/sum as a score
 * @author Chirag Gorasia
 */
public class BLOTransfragGeneList implements Operation<GeneList> {

	private final BinList 					binList;		// input binlist
	private final int 						zeroBinGap; 	// number of zero value bins defining a gap between two islands
	private final ScoreOperation 			operation;		//sum / average / max
	private boolean							stopped = false;// true if the operation must be stopped


	/**
	 * Defines regions as "islands" of non zero value bins
	 * separated by more than a specified number of zero value bins.
	 * Computes the average on these regions.
	 * Returns a new {@link GeneList} with the defined regions having their average/max/sum as a score
	 * @param binList input BinList
	 * @param zeroBinGap number of zero value windows defining a gap between two islands
	 * @param operation operation to use to compute the score of the intervals
	 */
	public BLOTransfragGeneList(BinList binList, int zeroBinGap, ScoreOperation operation) {
		this.binList = binList;
		this.zeroBinGap = zeroBinGap;
		this.operation = operation;
	}


	@Override
	public GeneList compute() throws Exception {
		ProjectChromosomes projectChromosomes = ProjectManager.getInstance().getProjectChromosomes();
		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<Void>> threadList = new ArrayList<Callable<Void>>();
		ListViewBuilder<Gene> lvbPrototype = new GeneListViewBuilder();
		final ListOfListViewBuilder<Gene> resultListBuilder = new ListOfListViewBuilder<Gene>(lvbPrototype);

		for (final Chromosome chromosome: projectChromosomes) {
			final ListView<ScoredChromosomeWindow> currentList = binList.get(chromosome);
			Callable<Void> currentThread = new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					if (currentList != null) {
						int j = 0;
						int geneCounter = 1;
						while ((j < currentList.size()) && !stopped) {
							// skip zero values
							while ((j < currentList.size()) && (currentList.get(j).getScore() == 0) && !stopped) {
								j++;
							}
							int regionStart = j;
							int regionStop = regionStart;
							int zeroWindowCount = 0;

							// a region stops when there is maxZeroWindowGap consecutive zero bins
							while ((j < currentList.size()) && (zeroWindowCount <= zeroBinGap) && !stopped) {
								if (currentList.get(j).getScore() == 0) {
									zeroWindowCount++;
								} else {
									zeroWindowCount = 0;
									regionStop = j;
								}
								j++;
							}
							if (regionStop == currentList.size()) {
								regionStop--;
							}
							if (regionStop >= regionStart) {
								float regionScore = 0;
								if (operation == ScoreOperation.AVERAGE) {
									// all the windows of the region are set with the average value on the region
									regionScore = (float) SCWListViews.average(currentList, regionStart, regionStop);
								} else if (operation == ScoreOperation.ADDITION) {
									// all the windows of the region are set with the sum value on the region
									regionScore = (float) SCWListViews.sum(currentList, regionStart, regionStop);
								} else {
									// all the windows of the region are set with the max value on the region
									regionScore = SCWListViews.maxNoZero(currentList, regionStart, regionStop);
								}
								regionStart *= binList.getBinSize();
								regionStop++;
								regionStop *= binList.getBinSize();
								ListView<ScoredChromosomeWindow> exon = SCWListViews.createGenericSCWListView(regionStart, regionStop, regionScore);
								Gene newGene = new SimpleGene(chromosome.getName() + "." + Integer.toString(geneCounter++), Strand.FIVE, regionStart, regionStop, regionScore, exon);
								resultListBuilder.addElementToBuild(chromosome, newGene);
							}
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
		return new SimpleGeneList(data, null, null);
	}


	@Override
	public String getDescription() {
		return "Operation: Transfrag, Gap Size = " + zeroBinGap + " Zero Value Successive Bins";
	}


	@Override
	public String getProcessingDescription() {
		return "Computing Transfrag";
	}


	@Override
	public int getStepCount() {
		return binList.getCreationStepCount() + 1;
	}


	@Override
	public void stop() {
		stopped = true;
	}
}

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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import edu.yu.einstein.genplay.core.manager.project.ProjectChromosomes;
import edu.yu.einstein.genplay.core.manager.project.ProjectManager;
import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operationPool.OperationPool;
import edu.yu.einstein.genplay.dataStructure.chromosome.Chromosome;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.binList.BinList;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.binList.BinListBuilder;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListView;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.ScoredChromosomeWindow;
import edu.yu.einstein.genplay.util.NumberFormats;


/**
 * Applies a gaussian filter on the BinList and returns the result in a new BinList.
 * @author Julien Lajugie
 */
public class BLOGauss implements Operation<BinList> {

	private final BinList 	binList;			// input list
	private final int 		movingWindowWidth;	// size of the moving window in bp
	private final boolean	fillNullValues; 	// true to fill the null values
	private boolean			stopped = false;	// true if the operation must be stopped


	/**
	 * Creates an instance of {@link BLOGauss}
	 * Applies a gaussian filter on the BinList and returns the result in a new BinList.
	 * @param binList {@link BinList} to gauss
	 * @param movingWindowWidth size of the moving window in bp
	 * @param fillNullValues set to true to fill the null values
	 */
	public BLOGauss(BinList binList, int movingWindowWidth, boolean fillNullValues) {
		this.binList = binList;
		this.movingWindowWidth = movingWindowWidth;
		this.fillNullValues = fillNullValues;
	}


	@Override
	public BinList compute() throws InvalidParameterException, InterruptedException, ExecutionException, CloneNotSupportedException  {
		final int binSize =  binList.getBinSize();
		final int sigma = movingWindowWidth / 4;
		final int halfWidth = movingWindowWidth / 2 / binSize;
		// we create an array of coefficients. The index correspond to a distance and for each distance we calculate a coefficient
		final double[] coefTab = new double[halfWidth + 1];

		// compute the gaussian coefficients
		double gaussianCoefConst1 = 1 / (Math.sqrt(2.0 * Math.PI * sigma));
		double gaussianCoefConst2 = 2.0 * Math.pow(sigma, 2);
		for(int i = 0; i <= halfWidth; i++) {
			coefTab[i] = gaussianCoefConst1 * Math.exp(-(Math.pow((i * binSize), 2) / gaussianCoefConst2));
		}

		ProjectChromosomes projectChromosomes = ProjectManager.getInstance().getProjectChromosomes();
		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<Void>> threadList = new ArrayList<Callable<Void>>();
		final BinListBuilder resultListBuilder = new BinListBuilder(binList.getBinSize());

		for (final Chromosome chromosome: projectChromosomes) {
			final ListView<ScoredChromosomeWindow> currentList = binList.get(chromosome);
			Callable<Void> currentThread = new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					if (currentList != null) {
						for(int j = 0; (j < currentList.size()) && !stopped; j++) {
							float score = 0f;
							if ((currentList.get(j).getScore() != 0) || (fillNullValues)) {
								// apply the array of coefficients centered on the current value to gauss
								double SumCoef = 0;
								double SumNormSignalCoef = 0;
								for (int k = -halfWidth; (k <= halfWidth) && !stopped; k++) {
									if(((j + k) >= 0) && ((j + k) < currentList.size()))  {
										int distance = Math.abs(k);
										if(currentList.get(j + k).getScore() != 0)  {
											SumCoef += coefTab[distance];
											SumNormSignalCoef += coefTab[distance] * currentList.get(j + k).getScore();
										}
									}
								}
								if(SumCoef != 0) {
									score = (float) (SumNormSignalCoef / SumCoef);
								}
							}
							resultListBuilder.addElementToBuild(chromosome, score);
						}
					}
					op.notifyDone();
					return null;
				}
			};
			threadList.add(currentThread);
		}
		op.startPool(threadList);
		return (BinList) resultListBuilder.getSCWList();
	}


	@Override
	public String getDescription() {
		final int sigma = movingWindowWidth / 4;
		return "Operation: Gauss, Sigma = "
		+ NumberFormats.getPositionFormat().format(sigma)
		+ "bp, Moving Window = "
		+ NumberFormats.getPositionFormat().format(movingWindowWidth)
		+"bp";
	}


	@Override
	public String getProcessingDescription() {
		return "Gaussing";
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

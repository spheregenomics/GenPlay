/*******************************************************************************
 *     GenPlay, Einstein Genome Analyzer
 *     Copyright (C) 2009, 2011 Albert Einstein College of Medicine
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *     
 *     Authors:	Julien Lajugie <julien.lajugie@einstein.yu.edu>
 *     			Nicolas Fourel <nicolas.fourel@einstein.yu.edu>
 *     Website: <http://genplay.einstein.yu.edu>
 *******************************************************************************/
package edu.yu.einstein.genplay.core.operation.binList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operationPool.OperationPool;
import edu.yu.einstein.genplay.dataStructure.enums.DataPrecision;
import edu.yu.einstein.genplay.dataStructure.list.arrayList.ListFactory;
import edu.yu.einstein.genplay.dataStructure.list.binList.BinList;



/**
 * Normalizes a {@link BinList} and multiplies the result by a factor
 * @author Julien Lajugie
 * @version 0.1
 */
public class BLONormalize implements Operation<BinList> {

	private final BinList 	binList;		// input BinList
	private final double	factor;			// the result of the normalization is multiplied by this factor
	private Double 			scoreSum;		// sum of the scores
	private boolean			stopped = false;// true if the operation must be stopped
		
	
	/**
	 * Normalizes a {@link BinList} and multiplies the result by a specified factor
	 * @param binList BinList to normalize
	 * @param factor factor
	 */
	public BLONormalize(BinList binList, double factor) {
		this.binList = binList;
		this.factor = factor;
	}
	
	
	@Override
	public BinList compute() throws InterruptedException, ExecutionException {
		scoreSum = new BLOSumScore(binList, null).compute();
		// we want to multiply each bin by the following coefficient
		final double coef = factor / scoreSum;
		final DataPrecision precision = binList.getPrecision();
		
		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<List<Double>>> threadList = new ArrayList<Callable<List<Double>>>();
		for (short i = 0; i < binList.size(); i++)  {
			final List<Double> currentList = binList.get(i);

			Callable<List<Double>> currentThread = new Callable<List<Double>>() {	
				@Override
				public List<Double> call() throws Exception {
					List<Double> resultList = null;
					if ((currentList != null) && (currentList.size() != 0)) {
						resultList = ListFactory.createList(precision, currentList.size());
						for (int j = 0; j < currentList.size() && !stopped; j++) {
							// we multiply each bin by the coefficient previously computed
							resultList.set(j, currentList.get(j) * coef);
						}
					}
					// tell the operation pool that a chromosome is done
					op.notifyDone();
					return resultList;
				}
			};

			threadList.add(currentThread);
		}
		List<List<Double>> result = op.startPool(threadList);
		if (result != null) {
			BinList resultList = new BinList(binList.getBinSize(), precision, result);
			return resultList;
		} else {
			return null;
		}
	}

	
	@Override
	public String getDescription() {
		return "Operation: Normalize, Factor = " + factor;
	}
	
	
	@Override
	public int getStepCount() {
		return BinList.getCreationStepCount(binList.getBinSize()) + 1;
	}
	
	
	@Override
	public String getProcessingDescription() {
		return "Normalizing";
	}

	
	@Override
	public void stop() {
		this.stopped = true;
	}
}
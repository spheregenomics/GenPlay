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

import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operationPool.OperationPool;
import edu.yu.einstein.genplay.dataStructure.enums.DataPrecision;
import edu.yu.einstein.genplay.dataStructure.genomeList.binList.BinList;
import edu.yu.einstein.genplay.dataStructure.list.arrayList.ListFactory;



/**
 * Inverses the specified {@link BinList}. Applies the function f(x) = a / x, where a is a specified double
 * @author Julien Lajugie
 * @version 0.1
 */
public class BLOInvertConstant implements Operation<BinList> {

	private final BinList 	binList;		// input BinList 
	private final double 	constant;		// coefficient a in f(x) = a / x
	private boolean			stopped = false;// true if the operation must be stopped
	
	
	/**
	 * Creates an instance of {@link BLOInvertConstant}
	 * @param binList input {@link BinList}
	 * @param constant constant a in f(x) = a / x
	 */
	public BLOInvertConstant(BinList binList, double constant) {
		this.binList = binList;
		this.constant = constant;
	}

	
	@Override
	public BinList compute() throws Exception {
		if (constant == 0) {
			return null;
		}
		
		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<List<Double>>> threadList = new ArrayList<Callable<List<Double>>>();
		final DataPrecision precision = binList.getPrecision();

		for (short i = 0; i < binList.size(); i++) {
			final List<Double> currentList = binList.get(i);
			
			Callable<List<Double>> currentThread = new Callable<List<Double>>() {	
				@Override
				public List<Double> call() throws Exception {
					List<Double> resultList = null;
					if ((currentList != null) && (currentList.size() != 0)) {
						resultList = ListFactory.createList(precision, currentList.size());
						// We add a constant to each element
						for (int j = 0; j < currentList.size() && !stopped; j++) {
							if(currentList.get(j) == 0) {
								resultList.set(j, 0d);
							} else {
								resultList.set(j, constant / currentList.get(j));
							}
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
		return "Operation: Invert, constant = " + constant;
	}

	
	@Override
	public String getProcessingDescription() {
		return "Inverting";
	}

	
	@Override
	public int getStepCount() {
		return 1 + BinList.getCreationStepCount(binList.getBinSize());
	}

	
	@Override
	public void stop() {
		this.stopped = true;
	}
}

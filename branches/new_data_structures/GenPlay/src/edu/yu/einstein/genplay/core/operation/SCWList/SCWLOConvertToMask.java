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
package edu.yu.einstein.genplay.core.operation.SCWList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operationPool.OperationPool;
import edu.yu.einstein.genplay.dataStructure.genomeList.SCWList.MaskSCWListFactory;
import edu.yu.einstein.genplay.dataStructure.genomeList.SCWList.ScoredChromosomeWindowList;
import edu.yu.einstein.genplay.dataStructure.genomeList.SCWList.SimpleSCWList;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.MaskChromosomeWindow;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.ScoredChromosomeWindow;



/**
 * Converts a {@link ScoredChromosomeWindowList} to a {@link ScoredChromosomeWindowList} of {@link MaskChromosomeWindow}
 * @author Nicolas Fourel
 * @version 0.1
 */
public class SCWLOConvertToMask implements Operation<ScoredChromosomeWindowList> {

	private final ScoredChromosomeWindowList 	scwList;	// input list
	private boolean				stopped = false;// true if the operation must be stopped


	/**
	 * Constructor of {@link SCWLOConvertToMask}
	 * @param scwList input mask list
	 */
	public SCWLOConvertToMask(ScoredChromosomeWindowList scwList) {
		this.scwList = scwList;
	}


	@Override
	public ScoredChromosomeWindowList compute() throws Exception {
		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<List<ScoredChromosomeWindow>>> threadList = new ArrayList<Callable<List<ScoredChromosomeWindow>>>();

		for (short i = 0; i < scwList.size(); i++) {
			final List<ScoredChromosomeWindow> currentList = scwList.getView(i);

			Callable<List<ScoredChromosomeWindow>> currentThread = new Callable<List<ScoredChromosomeWindow>>() {
				@Override
				public List<ScoredChromosomeWindow> call() throws Exception {
					List<ScoredChromosomeWindow> resultList = new ArrayList<ScoredChromosomeWindow>();
					if ((currentList != null) && (currentList.size() > 0)) {
						ScoredChromosomeWindow lastInsertedWindow = new MaskChromosomeWindow(currentList.get(0));
						resultList.add(lastInsertedWindow);
						for (int j = 1; (j < currentList.size()) && !stopped; j++) {
							ScoredChromosomeWindow currentWindow = currentList.get(j);
							if (currentWindow.getStart() <= lastInsertedWindow.getStop()) {
								lastInsertedWindow.setStop(currentWindow.getStop());
							} else {
								lastInsertedWindow = new MaskChromosomeWindow(currentList.get(j));
								resultList.add(lastInsertedWindow);
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
		List<List<ScoredChromosomeWindow>> result = op.startPool(threadList);
		if (result != null) {
			ScoredChromosomeWindowList resultList = MaskSCWListFactory.createMaskSCWArrayList(result);
			return resultList;
		} else {
			return null;
		}
	}


	@Override
	public String getDescription() {
		return "Operation: Convert to Mask";
	}


	@Override
	public String getProcessingDescription() {
		return "Converting track to Mask";
	}


	@Override
	public int getStepCount() {
		return 1 + SimpleSCWList.getCreationStepCount();
	}


	@Override
	public void stop() {
		stopped = true;
	}
}

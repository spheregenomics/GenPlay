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
package edu.yu.einstein.genplay.dataStructure.list.SCWList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import edu.yu.einstein.genplay.core.manager.project.ProjectChromosome;
import edu.yu.einstein.genplay.core.manager.project.ProjectManager;
import edu.yu.einstein.genplay.core.operation.SCWList.overLap.OverLappingManagement;
import edu.yu.einstein.genplay.core.operationPool.OperationPool;
import edu.yu.einstein.genplay.dataStructure.chromosome.Chromosome;
import edu.yu.einstein.genplay.dataStructure.enums.SCWListType;
import edu.yu.einstein.genplay.dataStructure.enums.ScoreCalculationMethod;
import edu.yu.einstein.genplay.dataStructure.list.DisplayableListOfLists;
import edu.yu.einstein.genplay.dataStructure.list.GenomicDataList;
import edu.yu.einstein.genplay.dataStructure.list.binList.BinList;
import edu.yu.einstein.genplay.dataStructure.list.geneList.GeneList;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.MaskChromosomeWindow;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.ScoredChromosomeWindow;
import edu.yu.einstein.genplay.exception.ExceptionManager;
import edu.yu.einstein.genplay.exception.exceptions.InvalidChromosomeException;
import edu.yu.einstein.genplay.util.ChromosomeWindowLists;
import edu.yu.einstein.genplay.util.DoubleLists;
import edu.yu.einstein.genplay.util.Utils;


/**
 * A list of {@link MaskChromosomeWindow}
 * @author Julien Lajugie
 * @author Nicolas Fourel
 * @version 0.1
 */
public final class MaskWindowList extends DisplayableListOfLists<ScoredChromosomeWindow, List<ScoredChromosomeWindow>> implements ScoredChromosomeWindowList, Serializable {

	private static final long serialVersionUID = 6268393967488568174L; // generated ID
	private static final int  SAVED_FORMAT_VERSION_NUMBER = 0;			// saved format version
	private ProjectChromosome projectChromosome = ProjectManager.getInstance().getProjectChromosome(); // Instance of the Chromosome Manager

	/*
	 * The following values are statistic values of the list
	 * They are transient because they depend on the chromosome manager that is also transient
	 * They are calculated at the creation of the list to avoid being recalculated
	 */
	transient private Double 	totalNumberOfWindows = null;

	private OverLappingManagement overLapManagement;


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);
		out.writeObject(projectChromosome);
		out.writeDouble(totalNumberOfWindows);
	}


	/**
	 * Method used for unserialization. Computes the statistics of the list after unserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();
		projectChromosome = (ProjectChromosome) in.readObject();
		totalNumberOfWindows = in.readDouble();
		try {
			generateStatistics();
		} catch (Exception e) {
			ExceptionManager.getInstance().caughtException(e);
		}
	}


	/**
	 * @return the number of steps needed to create a {@link MaskWindowList}
	 */
	public static int getCreationStepCount() {
		return 5;
	}


	/**
	 * overLappingExist method
	 * Scan the original list to find overlapping region.
	 * 
	 * @param	startList	list of position start
	 * @param	stopList	list of position stop
	 * @return	true is an overlapping region is found
	 */
	public static boolean overLappingExist (GenomicDataList<Integer> startList, GenomicDataList<Integer> stopList) {
		ProjectChromosome projectChromosome = ProjectManager.getInstance().getProjectChromosome();
		int index = 0;
		boolean isFound = false;
		for(final Chromosome currentChromosome : projectChromosome) {
			while (index < startList.get(projectChromosome.getIndex(currentChromosome)).size()) {
				isFound = searchOverLappingPositionsForIndex(projectChromosome, startList, stopList, currentChromosome, index);	//Search for overlapping position on the current index
				if (isFound) {
					return true;
				} else {
					index++;
				}
			}
		}
		return false;
	}


	/**
	 * searchOverLappingPositionsForIndex method
	 * This method search if the index is involved on an overlapping region
	 * 
	 * @param	projectChromosomeTmp a static ChromosomeManager instance
	 * @param	startList			 list of position start
	 * @param	stopList			 list of position stop
	 * @param 	currentChromosome	 Chromosome
	 * @param 	index				 current index
	 * @return						 true if the current index is involved on an overlapping region
	 */
	private static boolean searchOverLappingPositionsForIndex (ProjectChromosome projectChromosomeTmp,
			GenomicDataList<Integer> startList,
			GenomicDataList<Integer> stopList,
			Chromosome currentChromosome,
			int index) {
		int size = startList.get(projectChromosomeTmp.getIndex(currentChromosome)).size();
		int nextIndex = index + 1;
		if (nextIndex < size) {
			boolean valid = true;
			while (valid & (stopList.get(currentChromosome, index) > startList.get(currentChromosome, nextIndex))) {
				if (stopList.get(currentChromosome, index) > startList.get(currentChromosome, nextIndex)) {
					return true;
				}
				if ((nextIndex + 1) < size) {
					nextIndex++;
				} else {
					valid = false;
				}
			}
		}
		return false;
	}


	/**
	 * Default constructor
	 */
	public MaskWindowList() {}


	/**
	 * Creates an instance of {@link MaskChromosomeWindow} from a specified {@link BinList}
	 * @param binList BinList used for the creation of the {@link MaskChromosomeWindow}
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public MaskWindowList (final BinList binList) throws InterruptedException, ExecutionException {
		super();
		// retrieve the instance of the OperationPool
		final OperationPool op = OperationPool.getInstance();
		// list for the threads
		final Collection<Callable<List<ScoredChromosomeWindow>>> threadList = new ArrayList<Callable<List<ScoredChromosomeWindow>>>();
		final int windowData = binList.getBinSize();
		for(final Chromosome currentChromosome : projectChromosome) {
			Callable<List<ScoredChromosomeWindow>> currentThread = new Callable<List<ScoredChromosomeWindow>>() {
				@Override
				public List<ScoredChromosomeWindow> call() throws Exception {
					List<ScoredChromosomeWindow> resultList = new ArrayList<ScoredChromosomeWindow>();
					if (binList.get(currentChromosome) != null) {
						for (int i = 0; i < binList.get(currentChromosome).size(); i++) {
							double currentScore = binList.get(currentChromosome, i);
							if (currentScore != 0.0) {
								int start = i * windowData;
								int stop = start + windowData;

								boolean hasToUpdate = false;										// here, we want to check whether the current window is following the previous one or not.
								int prevIndex = resultList.size() - 1;								// get the last index
								if (prevIndex >= 0) {												// if it exists
									int prevStop = resultList.get(prevIndex).getStop();				// get the last inserted stop
									if (prevStop == start) {										// if the previous window stops where the current one starts, both window follow each other and are the same
										hasToUpdate = true;										// an update of the previous window is enough
									}
								}

								if (hasToUpdate) {
									resultList.get(prevIndex).setStop(stop);
								} else {
									resultList.add(new MaskChromosomeWindow(start, stop));
								}
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
		List<List<ScoredChromosomeWindow>> result = null;
		// starts the pool
		result = op.startPool(threadList);
		// add the chromosome results
		if (result != null) {
			for (List<ScoredChromosomeWindow> currentList: result) {
				add(currentList);
			}
		}
		generateStatistics();
	}


	/**
	 * Creates an instance of {@link MaskWindowList}
	 * @param startList list of start positions
	 * @param stopList list of stop position
	 * @throws InvalidChromosomeException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public MaskWindowList(	final GenomicDataList<Integer> startList,
			final GenomicDataList<Integer> stopList) throws InvalidChromosomeException, InterruptedException, ExecutionException {
		super();
		// retrieve the instance of the OperationPool
		final OperationPool op = OperationPool.getInstance();
		// list for the threads
		final Collection<Callable<List<ScoredChromosomeWindow>>> threadList = new ArrayList<Callable<List<ScoredChromosomeWindow>>>();

		overLapManagement = new OverLappingManagement(startList, stopList, null);
		overLapManagement.setScoreCalculationMethod(null);
		for(final Chromosome currentChromosome : projectChromosome) {
			Callable<List<ScoredChromosomeWindow>> currentThread = new Callable<List<ScoredChromosomeWindow>>() {
				@Override
				public List<ScoredChromosomeWindow> call() throws Exception {
					if (startList.get(currentChromosome) == null) {
						return null;
					}
					List<ScoredChromosomeWindow> resultList = new ArrayList<ScoredChromosomeWindow>();
					overLapManagement.run(currentChromosome);
					List<ScoredChromosomeWindow> list = overLapManagement.getList(currentChromosome);
					for(int j = 0; j < list.size(); j++) {
						resultList.add(new MaskChromosomeWindow(list.get(j).getStart(), list.get(j).getStop()));
					}
					// tell the operation pool that a chromosome is done
					op.notifyDone();
					return resultList;
				}
			};

			threadList.add(currentThread);
		}
		List<List<ScoredChromosomeWindow>> result = null;
		// starts the pool
		result = op.startPool(threadList);
		// add the chromosome results
		if (result != null) {
			for (List<ScoredChromosomeWindow> currentList: result) {
				add(currentList);
			}
		}
		// generate the statistics
		generateStatistics();
	}



	/**
	 * Creates an instance of {@link MaskChromosomeWindow} from a list of {@link MaskChromosomeWindow}
	 * @param data list of {@link MaskChromosomeWindow} with the data of the {@link MaskChromosomeWindow} to create
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public MaskWindowList(Collection<? extends List<ScoredChromosomeWindow>> data) throws InterruptedException, ExecutionException {
		super();
		addAll(data);
		// add the eventual missing chromosomes
		if (size() < projectChromosome.size()) {
			for (int i = size(); i < projectChromosome.size(); i++){
				add(null);
			}
		}
		// sort the list
		for (List<ScoredChromosomeWindow> currentChrWindowList : this) {
			if (currentChrWindowList != null) {
				Collections.sort(currentChrWindowList);
			}
		}
		generateStatistics();
	}


	/**
	 * Creates an instance of {@link MaskWindowList}
	 * @param geneList list of genes
	 * @param scm {@link ScoreCalculationMethod} used to create the {@link BinList}
	 * @throws InvalidChromosomeException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public MaskWindowList(final GeneList geneList, final ScoreCalculationMethod scm) throws InvalidChromosomeException, InterruptedException, ExecutionException {
		super();

		final GenomicDataList<Integer> startList = ChromosomeWindowLists.getStartList(geneList);
		final GenomicDataList<Integer> stopList = ChromosomeWindowLists.getStopList(geneList);

		// retrieve the instance of the OperationPool
		final OperationPool op = OperationPool.getInstance();
		// list for the threads
		final Collection<Callable<List<ScoredChromosomeWindow>>> threadList = new ArrayList<Callable<List<ScoredChromosomeWindow>>>();

		final boolean runOverLapEngine;
		overLapManagement = new OverLappingManagement(startList, stopList, null);
		if (scm != null) {
			overLapManagement.setScoreCalculationMethod(scm);
			runOverLapEngine = true;
		} else {
			runOverLapEngine = false;
		}
		for(final Chromosome currentChromosome : projectChromosome) {
			Callable<List<ScoredChromosomeWindow>> currentThread = new Callable<List<ScoredChromosomeWindow>>() {
				@Override
				public List<ScoredChromosomeWindow> call() throws Exception {
					if (startList.get(currentChromosome) == null) {
						return null;
					}
					List<ScoredChromosomeWindow> resultList = new ArrayList<ScoredChromosomeWindow>();
					if (runOverLapEngine) {
						overLapManagement.run(currentChromosome);
					}
					List<ScoredChromosomeWindow> list = overLapManagement.getList(currentChromosome);
					for(int j = 0; j < list.size(); j++) {
						double score = list.get(j).getScore();
						if (score != 0) {
							resultList.add(new MaskChromosomeWindow(list.get(j).getStart(), list.get(j).getStop()));
						}
					}
					// tell the operation pool that a chromosome is done
					op.notifyDone();
					return resultList;
				}
			};

			threadList.add(currentThread);
		}
		List<List<ScoredChromosomeWindow>> result = null;
		// starts the pool
		result = op.startPool(threadList);
		// add the chromosome results
		if (result != null) {
			for (List<ScoredChromosomeWindow> currentList: result) {
				add(currentList);
			}
		}
		// generate the statistics
		generateStatistics();
	}


	/**
	 * Performs a deep clone of the current {@link MaskWindowList}
	 * @return a new ScoredChromosomeWindowList
	 */
	@Override
	public MaskWindowList deepClone() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ((MaskWindowList)ois.readObject());
		} catch (Exception e) {
			ExceptionManager.getInstance().caughtException(e);
			return null;
		}
	}


	/**
	 * Merges two windows together if the gap between this two windows is not visible
	 */
	@Override
	protected void fitToScreen() {
		List<ScoredChromosomeWindow> currentChromosomeList;
		try {
			currentChromosomeList = get(fittedChromosome);
		} catch (InvalidChromosomeException e) {
			ExceptionManager.getInstance().caughtException(e);
			fittedChromosome = null;
			return;
		}
		if (currentChromosomeList == null) {
			fittedDataList = null;
			return;
		}
		if (fittedXRatio > 1) {
			fittedDataList = currentChromosomeList;
		} else {
			fittedDataList = new ArrayList<ScoredChromosomeWindow>();
			if (currentChromosomeList.size() > 0) {
				ArrayList<Double> scoreList = new ArrayList<Double>();
				fittedDataList.add(new MaskChromosomeWindow(currentChromosomeList.get(0)));
				scoreList.add(currentChromosomeList.get(0).getScore());
				int i = 1;
				int j = 0;
				while (i < currentChromosomeList.size()) {
					double gapDistance = (currentChromosomeList.get(i).getStart() - fittedDataList.get(j).getStop()) * fittedXRatio;
					double windowWidth = (currentChromosomeList.get(i).getStop() - fittedDataList.get(j).getStart()) * fittedXRatio;
					double currentScore = fittedDataList.get(j).getScore();
					double nextScore = currentChromosomeList.get(i).getScore();
					// we merge two intervals together if there is a gap smaller than 1 pixel and have the same score
					// or if the width of a window is smaller than 1
					while ( ((i + 1) < currentChromosomeList.size()) &&
							( ((gapDistance < 1) && (currentScore == nextScore)) ||
									((windowWidth < 1) && (nextScore != 0)))) {
						// the new stop position is the max of the current stop and the stop of the new merged interval
						int newStop = Math.max(fittedDataList.get(j).getStop(), currentChromosomeList.get(i).getStop());
						fittedDataList.get(j).setStop(newStop);
						scoreList.add(currentChromosomeList.get(i).getScore());
						i++;
						gapDistance = (currentChromosomeList.get(i).getStart() - fittedDataList.get(j).getStop()) * fittedXRatio;
						windowWidth = (currentChromosomeList.get(i).getStop() - fittedDataList.get(j).getStart()) * fittedXRatio;
						nextScore = currentChromosomeList.get(i).getScore();
					}
					fittedDataList.get(j).setScore(DoubleLists.average(scoreList));
					fittedDataList.add(new MaskChromosomeWindow(currentChromosomeList.get(i)));
					scoreList = new ArrayList<Double>();
					scoreList.add(currentChromosomeList.get(i).getScore());
					j++;
					i++;
				}
			}
		}

	}


	/**
	 * Computes some statistic values for this list
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private void generateStatistics() throws InterruptedException, ExecutionException {
		totalNumberOfWindows = 0.0;
		ProjectChromosome projectChromosome = ProjectManager.getInstance().getProjectChromosome();
		for(Chromosome currentChromosome : projectChromosome) {
			totalNumberOfWindows += get(currentChromosome).size();
		}
	}


	/**
	 * @return the average of the BinList
	 */
	@Override
	public Double getAverage() {
		return 1.0;
	}


	@Override
	protected List<ScoredChromosomeWindow> getFittedData(int start, int stop) {
		if ((fittedDataList == null) || (fittedDataList.size() == 0)) {
			return null;
		}
		return Utils.searchChromosomeWindowInterval(fittedDataList, start, stop);
	}


	/**
	 * @return the greatest value of the BinList
	 */
	@Override
	public Double getMaximum() {
		return 1.0;
	}


	/**
	 * @return the smallest value of the BinList
	 */
	@Override
	public Double getMinimum() {
		return 1.0;
	}


	/**
	 * @return the count of none-null bins in the BinList
	 */
	@Override
	public Long getNonNullLength() {
		return totalNumberOfWindows.longValue();
	}


	/**
	 * @param position a position on the fitted chromosome
	 * @return the score of the window on the fitted chromosome containing the specified position
	 */
	public double getScore(int position) {
		return 1;
	}


	/**
	 * @return the sum of the scores
	 */
	@Override
	public final Double getScoreSum() {
		return totalNumberOfWindows;
	}


	/**
	 * @return the standard deviation of the BinList
	 */
	@Override
	public Double getStandardDeviation() {
		return 0.0;
	}


	@Override
	public double getScore(Chromosome chromosome, int position) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public SCWListType getScoredChromosomeWindowListType() {
		// TODO Auto-generated method stub
		return null;
	}
}

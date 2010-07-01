/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.core.list.SCWList.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import yu.einstein.gdp2.core.ScoredChromosomeWindow;
import yu.einstein.gdp2.core.list.SCWList.ScoredChromosomeWindowList;
import yu.einstein.gdp2.core.operation.Operation;
import yu.einstein.gdp2.core.operationPool.OperationPool;


/**
 * Computes a Standard Score normalization on a {@link ScoredChromosomeWindowList}
 * @author Julien Lajugie
 * @version 0.1
 */
public class SCWLONormalizeStandardScore implements Operation<ScoredChromosomeWindowList> {

	private final ScoredChromosomeWindowList 	scwList;	// input list 
	private final SCWLOAverage 					avgOp;		// average
	private final SCWLOStandardDeviation 		stdevOp;	// standard deviation


	/**
	 * Creates an instance of {@link SCWLONormalizeStandardScore}
	 * @param scwList input list
	 */
	public SCWLONormalizeStandardScore(ScoredChromosomeWindowList scwList) {
		this.scwList = scwList;
		avgOp = new SCWLOAverage(scwList, null);
		stdevOp = new SCWLOStandardDeviation(scwList, null);
	}

	@Override
	public ScoredChromosomeWindowList compute() throws Exception {
		// compute average
		final double avg = avgOp.compute();
		// compute standard deviation
		final double stdev = stdevOp.compute();
		// retrieve singleton operation pool
		final OperationPool op = OperationPool.getInstance();
		// creates collection of thread for the operation pool
		final Collection<Callable<List<ScoredChromosomeWindow>>> threadList = new ArrayList<Callable<List<ScoredChromosomeWindow>>>();
		for (short i = 0; i < scwList.size(); i++) {
			final List<ScoredChromosomeWindow> currentList = scwList.get(i);

			Callable<List<ScoredChromosomeWindow>> currentThread = new Callable<List<ScoredChromosomeWindow>>() {	
				@Override
				public List<ScoredChromosomeWindow> call() throws Exception {
					List<ScoredChromosomeWindow> resultList = null;
					if ((currentList != null) && (currentList.size() != 0)) {
						resultList = new ArrayList<ScoredChromosomeWindow>();
						for (ScoredChromosomeWindow currentWindow: currentList) {
							ScoredChromosomeWindow resultWindow = new ScoredChromosomeWindow(currentWindow);
							if (currentWindow.getScore() != 0) {
								// apply the standard score formula: (x - avg) / stdev 
								double resultScore = (currentWindow.getScore() - avg) / stdev; 
								resultWindow.setScore(resultScore);
							}
							resultList.add(resultWindow);
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
			ScoredChromosomeWindowList resultList = new ScoredChromosomeWindowList(result);
			return resultList;
		} else {
			return null;
		}
	}


	@Override
	public String getDescription() {
		return "Operation: Normalize, Standard Score";
	}


	@Override
	public String getProcessingDescription() {
		return "Normalizing";
	}


	@Override
	public int getStepCount() {
		return 1 + avgOp.getStepCount() + stdevOp.getStepCount() + ScoredChromosomeWindowList.getCreationStepCount();
	}
}

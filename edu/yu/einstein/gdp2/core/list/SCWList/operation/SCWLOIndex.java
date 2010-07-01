/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.core.list.SCWList.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import yu.einstein.gdp2.core.ScoredChromosomeWindow;
import yu.einstein.gdp2.core.list.SCWList.ScoredChromosomeWindowList;
import yu.einstein.gdp2.core.operation.Operation;
import yu.einstein.gdp2.core.operationPool.OperationPool;


/**
 * Indexes the scores of a {@link ScoredChromosomeWindowList} based on 
 * the greatest and the smallest value of the whole genome
 * @author Julien Lajugie
 * @version 0.1
 */
public class SCWLOIndex implements Operation<ScoredChromosomeWindowList> {

	private final ScoredChromosomeWindowList 	scwList;	// list to index
	private final double 						newMin;		// new min after index
	private final double 						newMax;		// new max after index


	/**
	 * Creates an instance of {@link SCWLOIndex}
	 * Indexes the scores between the specified minimum and maximum 
	 * based on the greatest and the smallest value of the whole genome.
	 * @param scwList {@link ScoredChromosomeWindowList} to index
	 * @param newMin minimum value after index
	 * @param newMax maximum value after index
	 */
	public SCWLOIndex(ScoredChromosomeWindowList scwList, double newMin, double newMax) {
		this.scwList = scwList;
		this.newMin = newMin;
		this.newMax = newMax;
	}


	@Override
	public ScoredChromosomeWindowList compute() throws InterruptedException, ExecutionException {
		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<List<ScoredChromosomeWindow>>> threadList = new ArrayList<Callable<List<ScoredChromosomeWindow>>>();

		final double oldMin = scwList.getMin();
		final double oldMax = scwList.getMax();
		// We calculate the difference between the highest and the lowest value
		final double oldDistance = oldMax - oldMin;
		if (oldDistance != 0) {
			final double newDistance = newMax - newMin;
			for (short i = 0; i < scwList.size(); i++) {
				final List<ScoredChromosomeWindow> currentList = scwList.get(i);

				Callable<List<ScoredChromosomeWindow>> currentThread = new Callable<List<ScoredChromosomeWindow>>() {
					@Override
					public List<ScoredChromosomeWindow> call() throws Exception {
						List<ScoredChromosomeWindow> resultList = null;
						if ((currentList != null) && (currentList.size() != 0)) {
							resultList = new ArrayList<ScoredChromosomeWindow>();
							// We index the intensities
							for (ScoredChromosomeWindow currentWindow: currentList) {
								ScoredChromosomeWindow resultWindow = new ScoredChromosomeWindow(currentWindow);
								if (currentWindow.getScore() != 0) {
									resultWindow.setScore(newDistance * (currentWindow.getScore() - oldMin) / oldDistance + newMin);
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
			}
		}
		return null;
	}


	@Override
	public String getDescription() {
		return "Operation: Index Between " +  newMin + " and " + newMax;
	}


	@Override
	public int getStepCount() {
		return 1 + ScoredChromosomeWindowList.getCreationStepCount();
	}


	@Override
	public String getProcessingDescription() {
		return "Indexing";
	}
}

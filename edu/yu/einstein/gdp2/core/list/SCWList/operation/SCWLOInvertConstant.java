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
 * Inverses the specified {@link ScoredChromosomeWindowList}. Applies the function f(x) = a / x, where a is a specified double
 * @author Julien Lajugie
 * @version 0.1
 */
public class SCWLOInvertConstant implements Operation<ScoredChromosomeWindowList> {

	private final ScoredChromosomeWindowList 	scwList;	// input list
	private final double 						constant;	// coefficient a in f(x) = a / x
	
	
	/**
	 * Creates an instance of {@link SCWLOInvertConstant}
	 * @param binList input {@link ScoredChromosomeWindowList}
	 * @param constant constant a in f(x) = a / x
	 */
	public SCWLOInvertConstant(ScoredChromosomeWindowList scwList, double constant) {
		this.scwList = scwList;
		this.constant = constant;
	}
	
	
	@Override
	public ScoredChromosomeWindowList compute() throws Exception {
		if (constant == 0) {
			return null;
		}
		
		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<List<ScoredChromosomeWindow>>> threadList = new ArrayList<Callable<List<ScoredChromosomeWindow>>>();

		for (short i = 0; i < scwList.size(); i++) {
			final List<ScoredChromosomeWindow> currentList = scwList.get(i);
			
			Callable<List<ScoredChromosomeWindow>> currentThread = new Callable<List<ScoredChromosomeWindow>>() {	
				@Override
				public List<ScoredChromosomeWindow> call() throws Exception {
					List<ScoredChromosomeWindow> resultList = null;
					if ((currentList != null) && (currentList.size() != 0)) {
						resultList = new ArrayList<ScoredChromosomeWindow>();
						// we invert each element
						for (ScoredChromosomeWindow currentWindow: currentList) {
							ScoredChromosomeWindow resultWindow = new ScoredChromosomeWindow(currentWindow);
							if (currentWindow.getScore() != 0) {
								resultWindow.setScore(constant / currentWindow.getScore());
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
		return "Operation: Invert, constant = " + constant;
	}

	
	@Override
	public String getProcessingDescription() {
		return "Inverting";
	}

	
	@Override
	public int getStepCount() {
		return 3;
	}
}
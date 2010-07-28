/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.core.list.binList.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import yu.einstein.gdp2.core.enums.DataPrecision;
import yu.einstein.gdp2.core.enums.ScoreCalculationTwoTrackMethod;
import yu.einstein.gdp2.core.list.DisplayableListOfLists;
import yu.einstein.gdp2.core.list.arrayList.ListFactory;
import yu.einstein.gdp2.core.list.binList.BinList;
import yu.einstein.gdp2.core.operation.Operation;
import yu.einstein.gdp2.core.operationPool.OperationPool;
import yu.einstein.gdp2.exception.BinListDifferentWindowSizeException;


/**
 * Adds the scores of the bins of two specified BinLists
 * @author Julien Lajugie
 * @version 0.1
 */
public class BLOTwoTracks implements Operation<DisplayableListOfLists<?, ?>> {

	private final BinList 		binList1;	// first binlist to add 
	private final BinList 		binList2; 	// second binlist to add
	private final DataPrecision precision;	// precision of the result list
	private ScoreCalculationTwoTrackMethod 	scm;


	/**
	 * Adds the scores of the bins of the two specified BinLists
	 * @param binList1
	 * @param binList2
	 * @param precision precision of the result {@link BinList} 
	 */
	public BLOTwoTracks(BinList binList1, BinList binList2, DataPrecision precision, ScoreCalculationTwoTrackMethod scm) {
		this.binList1 = binList1;
		this.binList2 = binList2;
		this.precision = precision;
		this.scm = scm;
	}


	@Override
	public BinList compute() throws InterruptedException, ExecutionException, BinListDifferentWindowSizeException {
		// make sure that the two binlists have the same size of bins
		if (binList1.getBinSize() != binList2.getBinSize()) {
			throw new BinListDifferentWindowSizeException();
		}

		final OperationPool op = OperationPool.getInstance();
		final Collection<Callable<List<Double>>> threadList = new ArrayList<Callable<List<Double>>>();
		for(short i = 0; i < binList1.size(); i++)  {
			final List<Double> currentList1 = binList1.get(i);
			final List<Double> currentList2 = binList2.get(i);

			Callable<List<Double>> currentThread = new Callable<List<Double>>() {	
				@Override
				public List<Double> call() throws Exception {
					List<Double> resultList = null;
					if ((currentList1 != null) && (currentList1.size() != 0) && (currentList2 != null) && (currentList2.size() != 0)) {
						resultList = ListFactory.createList(precision, currentList1.size());
						for (int j = 0; j < currentList1.size(); j++) {
							if (j < currentList2.size()) {
								// we add the bins of the two binlists
								resultList.set(j, getScore(currentList1.get(j), currentList2.get(j)));
							} else {
								resultList.set(j, 0d);
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
			BinList resultList = new BinList(binList1.getBinSize(), precision, result);
			return resultList;
		} else {
			return null;
		}
	}
	

	@Override
	public String getDescription() {
		return "Operation: Add";
	}


	@Override
	public int getStepCount() {
		return BinList.getCreationStepCount(binList1.getBinSize()) + 1;
	}


	@Override
	public String getProcessingDescription() {
		return "Adding";
	}
	
	
	/**
	 * getScore method
	 * This method manages the calculation of the score according to the score calculation method.
	 * 
	 * @return	the score
	 */
	private double getScore (double a, double b) {
		switch (scm) {
		case ADDITION:
			return sum(a, b);
		case SUBTRACTION:
			return subtraction(a, b);
		case MULTIPLICATION:
			return multiplication(a, b);
		case DIVISION:
			return division(a, b);
		case AVERAGE:
			return average(a, b);
		case MAXIMUM:
			return maximum(a, b);
		case MINIMUM:
			return minimum(a, b);
		default:
			return -1.0;
		}
	}
	
	
	///////////////////////////	Calculation methods
	
	private double sum(double a, double b) {
		return (a + b);
	}
	
	private double subtraction(double a, double b) {
		return (a - b);
	}
	
	private double multiplication(double a, double b) {
		return (a * b);
	}
	
	private double division(double a, double b) {
		if (a != 0.0 && b != 0.0) {
			return a / b;
		} else {
			return 0.0;
		}
	}
	
	private double average(double a, double b) {
		return sum(a, b) / 2;
	}
	
	private double maximum(double a, double b) {
		return Math.max(a, b);
	}
	
	private double minimum(double a, double b) {
		return Math.min(a, b);
	}
}
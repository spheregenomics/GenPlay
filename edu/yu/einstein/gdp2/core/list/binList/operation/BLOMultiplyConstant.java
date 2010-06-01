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
import yu.einstein.gdp2.core.list.binList.BinList;
import yu.einstein.gdp2.core.list.binList.ListFactory;


/**
 * Multiplies the scores of each bin of a {@link BinList} by a specified constant 
 * @author Julien Lajugie
 * @version 0.1
 */
public class BLOMultiplyConstant implements BinListOperation<BinList> {

	private final BinList 	binList;	// input binlist
	private final double 	constant;	// constant of the multiplication
	
	
	/**
	 * Multiplies the scores of each bin of a {@link BinList} by a specified constant
	 * @param binList input {@link BinList}
	 * @param constant constant of the multiplication
	 */
	public BLOMultiplyConstant(BinList binList, double constant) {
		this.binList = binList;
		this.constant = constant;
	}
	
	
	@Override
	public BinList compute() throws InterruptedException, ExecutionException {
		if (constant == 0) {
			return binList.deepClone();
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
						// We multiply each element by a constant
						for (int j = 0; j < currentList.size(); j++) {
							resultList.set(j, currentList.get(j) * constant);
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
		return "Operation: Multiply Constant, Constant = " + constant;
	}
	
	
	@Override
	public int getStepCount() {
		return BinList.getCreationStepCount(binList.getBinSize()) + 1;
	}
}

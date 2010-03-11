/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.core.enums;

import yu.einstein.gdp2.core.list.binList.BinListOperations;


/**
 * A type of filter used in the class {@link BinListOperations}
 * @author Julien Lajugie
 * @version 0.1
 */
public enum FilterType {
	
	/**
	 * filter a fixed number of values
	 */
	COUNT ("Count"),
	/**
	 * 
	 */	
	DENSITY ("Density"),
	/**
	 * filter a percentage of extreme values
	 */
	PERCENTAGE ("Percentage"),
	/**
	 * filter values above or under a specified threshold
	 */
	THRESHOLD ("Threshold");

	
	private final String name; // String representing the filter 
	
	
	/**
	 * Private constructor. Creates an instance of {@link FilterType}
	 * @param name
	 */
	private FilterType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

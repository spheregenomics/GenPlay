/*******************************************************************************
 * GenPlay, Einstein Genome Analyzer
 * Copyright (C) 2009, 2014 Albert Einstein College of Medicine
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * Authors: Julien Lajugie <julien.lajugie@einstein.yu.edu>
 *          Nicolas Fourel <nicolas.fourel@einstein.yu.edu>
 *          Eric Bouhassira <eric.bouhassira@einstein.yu.edu>
 * 
 * Website: <http://genplay.einstein.yu.edu>
 ******************************************************************************/
package edu.yu.einstein.genplay.dataStructure.enums;

/**
 * Type of a graph
 * @author Julien Lajugie
 * @version 0.1
 */
public enum GraphType {
	/**
	 * Curve graph
	 */
	CURVE ("curve"),
	/**
	 * Points graph
	 */
	POINTS ("points"),
	/**
	 * Bar graph
	 */
	BAR		("bar"),
	/**
	 * Dense graph
	 */
	DENSE	("dense");


	private final String name; // String representing the type of graph


	/**
	 * Private constructor. Creates an instance of {@link GraphType}
	 * @param name
	 */
	private GraphType(String name) {
		this.name = name;
	}


	/**
	 * @return the name of the graph type
	 */
	public String getName() {
		return name;
	}


	@Override
	public String toString() {
		return name;
	}


	/**
	 * @param name
	 * @return the {@link GraphType} element having the specified name.  Null if none
	 */
	public static GraphType lookup(String name) {
		for (GraphType currentGraphType: GraphType.values()) {
			if (currentGraphType.getName().equals(name)) {
				return currentGraphType;
			}
		}
		return null;
	}
}

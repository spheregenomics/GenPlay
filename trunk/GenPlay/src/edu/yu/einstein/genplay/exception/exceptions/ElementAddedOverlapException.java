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
package edu.yu.einstein.genplay.exception.exceptions;

import edu.yu.einstein.genplay.dataStructure.list.listView.ListView;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListViewBuilder;

/**
 * Exception thrown by {@link ListViewBuilder} objects when the elements added to
 * the {@link ListView} to be constructed overlap.
 * @author Julien Lajugie
 */
public class ElementAddedOverlapException extends RuntimeException {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = -5147649770292040995L;


	/**
	 * Creates an instance of {@link ElementAddedOverlapException}
	 */
	public ElementAddedOverlapException() {
		super("Elements added to the ListViewBuilder are overlapping.");
	}


	/**
	 * Creates an instance of {@link ElementAddedOverlapException}
	 * @param message message of the exception
	 */
	public ElementAddedOverlapException(String message) {
		super(message);
	}
}

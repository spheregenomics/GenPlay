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
package edu.yu.einstein.genplay.core.DAS;

import edu.yu.einstein.genplay.dataStructure.enums.Strand;


/**
 * An Entry Point as described in the DAS 1.53 specifications:
 * <br/><a href="http://www.biodas.org/documents/spec.html">http://www.biodas.org/documents/spec.html</a>
 * @author Julien Lajugie
 */
public class EntryPoint {
	private String 	ID;			// ID of the entry point
	private int 	start;		// start position of the entry point
	private int 	stop;		// stop position of the entry point
	private Strand 	orientation;// orientation of the entry point


	/**
	 * @return the iD
	 */
	public final String getID() {
		return ID;
	}


	/**
	 * @return the orientation
	 */
	public final Strand getOrientation() {
		return orientation;
	}


	/**
	 * @return the start
	 */
	public final int getStart() {
		return start;
	}


	/**
	 * @return the stop
	 */
	public final int getStop() {
		return stop;
	}


	/**
	 * @param iD the iD to set
	 */
	public final void setID(String iD) {
		ID = iD;
	}


	/**
	 * @param orientation the orientation to set
	 */
	public final void setOrientation(Strand orientation) {
		this.orientation = orientation;
	}


	/**
	 * @param start the start to set
	 */
	public final void setStart(int start) {
		this.start = start;
	}


	/**
	 * @param stop the stop to set
	 */
	public final void setStop(int stop) {
		this.stop = stop;
	}
}

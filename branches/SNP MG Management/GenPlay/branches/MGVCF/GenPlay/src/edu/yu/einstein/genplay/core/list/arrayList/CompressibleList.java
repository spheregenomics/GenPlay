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
 *     Author: Julien Lajugie <julien.lajugie@einstein.yu.edu>
 *     Website: <http://genplay.einstein.yu.edu>
 *******************************************************************************/
package edu.yu.einstein.genplay.core.list.arrayList;

import java.util.List;

import edu.yu.einstein.genplay.exception.CompressionException;



/**
 * Interface defining the method of the compressible {@link List}.
 * @author Julien Lajugie
 * @version 0.1
 */
public interface CompressibleList {

	
	/**
	 * Compresses the data of the list
	 * @throws CompressionException 
	 */
	public void compress() throws  CompressionException ;
	
	
	/**
	 * Uncompresses the data of the list
	 * @throws CompressionException 
	 */
	public void uncompress() throws  CompressionException;
	
	
	/**
	 * @return true if the list is compressed. False otherwise.
	 */
	public boolean isCompressed();
}
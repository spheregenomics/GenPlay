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
 *     Authors:	Julien Lajugie <julien.lajugie@einstein.yu.edu>
 *     			Nicolas Fourel <nicolas.fourel@einstein.yu.edu>
 *     Website: <http://genplay.einstein.yu.edu>
 *******************************************************************************/
package edu.yu.einstein.genplay.util;

import java.util.List;

import edu.yu.einstein.genplay.core.chromosomeWindow.ChromosomeWindow;
import edu.yu.einstein.genplay.core.list.GenomicDataArrayList;
import edu.yu.einstein.genplay.core.list.GenomicDataList;
import edu.yu.einstein.genplay.core.list.arrayList.IntArrayAsIntegerList;

/**
 * Provides operation on {@link List} of {@link ChromosomeWindow}
 * @author Julien Lajugie
 */
public class ChromosomeWindowLists {

	/**
	 * @param chromosomeWindowList a {@link GenomicDataList} of Object that extends {@link ChromosomeWindow}
	 * @return a {@link GenomicDataList} containing the start positions of the specified list. This list is organized the same way as the input list
	 */
	public static GenomicDataList<Integer> getStartList(GenomicDataList<? extends ChromosomeWindow> chromosomeWindowList) {
		GenomicDataList<Integer> list = new GenomicDataArrayList<Integer>();
		for (int i = 0; i < chromosomeWindowList.size(); i++) {
			list.add(new IntArrayAsIntegerList());
		}

		for (int i = 0; i < chromosomeWindowList.size(); i++) {
			List<? extends ChromosomeWindow> currentList = chromosomeWindowList.get(i);
			for (ChromosomeWindow currentChromosomeWindow: currentList) {
				list.get(i).add(currentChromosomeWindow.getStop());
			}
		}
		return list;
	}


	/**
	 * @param chromosomeWindowList a {@link GenomicDataList} of Object that extends {@link ChromosomeWindow}
	 * @return a {@link GenomicDataList} containing the stop positions of the specified list. This list is organized the same way as the input list
	 */
	public static GenomicDataList<Integer> getStopList(GenomicDataList<? extends ChromosomeWindow> chromosomeWindowList) {
		GenomicDataList<Integer> list = new GenomicDataArrayList<Integer>();
		for (int i = 0; i < chromosomeWindowList.size(); i++) {
			list.add(new IntArrayAsIntegerList());
		}

		for (int i = 0; i < chromosomeWindowList.size(); i++) {
			List<? extends ChromosomeWindow> currentList = chromosomeWindowList.get(i);
			for (ChromosomeWindow currentChromosomeWindow: currentList) {
				list.get(i).add(currentChromosomeWindow.getStop());
			}
		}
		return list;
	}
}
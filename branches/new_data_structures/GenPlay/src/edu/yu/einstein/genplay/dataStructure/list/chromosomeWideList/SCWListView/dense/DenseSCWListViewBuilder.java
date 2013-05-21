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
package edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.SCWListView.dense;

import java.util.List;

import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.SCWListView.SCWListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListView;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.primitiveList.FloatListFactory;
import edu.yu.einstein.genplay.dataStructure.list.primitiveList.ListOfIntArraysAsIntegerList;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.ScoredChromosomeWindow;
import edu.yu.einstein.genplay.exception.exceptions.ElementAddedNotSortedException;
import edu.yu.einstein.genplay.exception.exceptions.ElementAddedOverlapException;
import edu.yu.einstein.genplay.exception.exceptions.ObjectAlreadyBuiltException;

/**
 * Implementation of the {@link ListViewBuilder} interface vending
 * {@link DenseSCWListView} objects.
 * @author Julien Lajugie
 */
public final class DenseSCWListViewBuilder implements ListViewBuilder<ScoredChromosomeWindow>, SCWListViewBuilder {

	/** List of the stop positions of the SCWs */
	private List<Integer> windowStops;

	/** List of the score values of the SCWs */
	private List<Float> windowScores;


	/**
	 * Creates an instance of {@link DenseSCWListViewBuilder}
	 */
	public DenseSCWListViewBuilder() {
		windowStops = new ListOfIntArraysAsIntegerList();
		windowScores = FloatListFactory.createFloatList();
	}


	/**
	 * Adds an element to the ListView that will be built.
	 * To assure that ListView objects are immutable, this method
	 * will throw an exception if called after the getListView() has been called.
	 * Checks that the elements are added in start position order.
	 * @param start start position of the SCW to add
	 * @param stop stop position of the SCW to add
	 * @param score score value of the SCW to add
	 * @throws ObjectAlreadyBuiltException
	 * @throws ElementAddedNotSortedException If elements are not added in sorted order
	 * @throws ElementAddedOverlapException If elements added overlaps
	 */
	@Override
	public void addElementToBuild(int start, int stop, float score)
			throws ObjectAlreadyBuiltException, ElementAddedNotSortedException, ElementAddedOverlapException {
		if (windowStops == null) {
			throw new ObjectAlreadyBuiltException();
		}
		if (windowStops.size() > 0) {
			int lastElementIndex = windowStops.size() -1;
			int lastStart = 0;
			if (windowStops.size() > 1) {
				lastStart = windowStops.get(lastElementIndex - 1);
			}
			int lastStop = windowStops.get(lastElementIndex);
			if (start < lastStart) {
				// case where the elements added are not sorted
				throw new ElementAddedNotSortedException();
			} else if (start < lastStop) {
				// case where the elements added overlap
				throw new ElementAddedOverlapException();
			}
			// if the current window and the previous one have the same same score we merge them
			if (windowScores.get(lastElementIndex) == score) {
				windowStops.set(lastElementIndex, stop);
				return;
			}
			if (lastStop != start) {
				windowStops.add(start);
				windowScores.add(0f);
			}
		}
		windowStops.add(stop);
		windowScores.add(score);
	}


	@Override
	public void addElementToBuild(ScoredChromosomeWindow element)
			throws ObjectAlreadyBuiltException, ElementAddedNotSortedException, ElementAddedOverlapException {
		addElementToBuild(element.getStart(), element.getStop(), element.getScore());
	}


	@Override
	public DenseSCWListViewBuilder clone() {
		DenseSCWListViewBuilder clone = new DenseSCWListViewBuilder();
		return clone;
	}


	@Override
	public ListView<ScoredChromosomeWindow> getListView() {
		ListView<ScoredChromosomeWindow> listView = new DenseSCWListView(windowStops, windowScores);
		windowStops = null;
		windowScores = null;
		return listView;
	}
}

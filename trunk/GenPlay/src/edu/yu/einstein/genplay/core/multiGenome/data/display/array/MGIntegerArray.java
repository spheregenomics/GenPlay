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
package edu.yu.einstein.genplay.core.multiGenome.data.display.array;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.yu.einstein.genplay.core.multiGenome.data.display.content.MGLineContent;

/**
 * @author Nicolas Fourel
 * @version 0.1
 */
public class MGIntegerArray implements Serializable {

	/** Default serial version ID */
	private static final long serialVersionUID = 3395817941269577574L;
	private static final int  SAVED_FORMAT_VERSION_NUMBER = 0;		// saved format version
	private static final int 	DEFAULT_SIZE = 700000;				// minimum length added every time the array is resized
	private static final int 	RESIZE_MIN = 1000;					// minimum length added every time the array is resized
	private static final int 	RESIZE_MAX = 10000000;				// maximum length added every time the array is resized
	private static final int 	RESIZE_FACTOR = 2;					// multiplication factor of the length of the array every time it's resized
	private int[] 				data;								// int data array
	private int 				size;								// size of the list


	/**
	 * Creates an instance of {@link MGIntegerArray}
	 */
	public MGIntegerArray() {
		initialize(DEFAULT_SIZE);
	}


	/**
	 * Recreates the arrays with the right size in order to optimize the memory usage.
	 */
	public void compact() {
		boolean found = false;
		int currentIndex = size - 1;
		while (!found && (currentIndex > -1)) {
			if (data[currentIndex] != MGLineContent.NO_ALTERNATIVE) {
				found = true;
			} else {
				currentIndex--;
			}
		}

		int[] newData = new int[currentIndex + 1];
		for (int i = 0; i < newData.length; i++) {
			newData[i] = data[i];
		}
		data = newData;
		size = data.length;
	}


	/**
	 * Fill the data array with an unique value
	 * @param value the value to set
	 */
	private void fill (int value) {
		for (int i = 0; i < size; i++) {
			data[i] = value;
		}
	}


	/**
	 * @param index the index of the element to get
	 * @return	the element at the given index
	 */
	public Integer get(int index) {
		return data[index];
	}


	/**
	 * Recursive function. Returns the index where the value is found or -1 if the exact value is not found.
	 * @param value	value
	 * @return the index where the start value of the window is found or -1 if the value is not found
	 */
	public int getIndex (int value) {
		int index = getIndex(value, 0, size - 1);
		if (data[index] == value) {
			return index;
		}
		return -1;
	}


	/**
	 * Recursive function. Returns the index where the value is found
	 * or the index right after if the exact value is not found.
	 * @param value			value
	 * @param indexStart	start index (in the data array)
	 * @param indexStop		stop index (in the data array)
	 * @return the index where the start value of the window is found or the index right after if the exact value is not found
	 */
	private int getIndex (int value, int indexStart, int indexStop) {
		int middle = (indexStop - indexStart) / 2;
		if (indexStart == indexStop) {
			return indexStart;
		} else if (value == data[indexStart + middle]) {
			return indexStart + middle;
		} else if (value > data[indexStart + middle]) {
			return getIndex(value, indexStart + middle + 1, indexStop);
		} else {
			return getIndex(value, indexStart, indexStart + middle);
		}
	}


	/**
	 * Initialize the data array
	 * @param size size of the array
	 */
	private void initialize (int size) {
		data = new int[size];
		this.size = size;
		fill(MGLineContent.NO_ALTERNATIVE);
	}


	/**
	 * Method used for unserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();
		data = (int[]) in.readObject();
		size = in.readInt();
	}


	/**
	 * Resize the array
	 */
	private void resize () {
		// we multiply the current size by the resize multiplication factor
		int newLength = data.length * RESIZE_FACTOR;
		// we make sure we don't add less than RESIZE_MIN elements
		newLength = Math.max(newLength, data.length + RESIZE_MIN);
		// we make sure we don't add more than RESIZE_MAX elements
		newLength = Math.min(newLength, data.length + RESIZE_MAX);
		int[] newData = new int[newLength];
		for (int i = 0; i < data.length; i++) {
			newData[i] = data[i];
		}
		for (int i = data.length; i < newLength; i++) {
			newData[i] = MGLineContent.NO_ALTERNATIVE;
		}
		data = newData;
		size = newLength;
	}


	/**
	 * Resize the array copying every value to a new array from the beginning until the new size
	 * @param newSize the new size
	 */
	public void resize (int newSize) {
		int[] newData = new int[newSize];
		for (int i = 0; i < newSize; i++) {
			if (i < data.length) {
				newData[i] = data[i];
			} else {
				newData[i] = MGLineContent.NO_ALTERNATIVE;
			}
		}
		data = newData;
		size = newSize;
	}


	/**
	 * @param index index to set the element
	 * @param element element to set
	 * @return the element at the index before the set
	 */
	public Integer set(int index, Integer element) {
		while(index >= size) {
			resize();
		}
		int old = data[index];
		data[index] = element;
		return old;
	}



	/**
	 * Shows the content of the list
	 */
	public void show () {
		String info = "";
		for (int i = 0; i < size; i++) {
			info += "(" + i + "; " + data[i] + ") ";
		}
		System.out.println(info);
	}


	/**
	 * @return the size of the array
	 */
	public int size() {
		return size;
	}


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);
		out.writeObject(data);
		out.writeInt(size);
	}
}

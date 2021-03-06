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
package edu.yu.einstein.genplay.core.manager.project;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.yu.einstein.genplay.dataStructure.genomeWindow.GenomeWindow;
import edu.yu.einstein.genplay.gui.event.genomeWindowEvent.GenomeWindowEvent;
import edu.yu.einstein.genplay.gui.event.genomeWindowEvent.GenomeWindowEventsGenerator;
import edu.yu.einstein.genplay.gui.event.genomeWindowEvent.GenomeWindowListener;

/**
 * This class manages the genome window displayed, the track current width and the ratio between them.
 * {@link GenomeWindowListener} can be registered into this class to receive notification about window changes.
 * @author Nicolas Fourel
 * @author Julien Lajugie
 * @version 0.1
 */
public class ProjectWindow implements Serializable, GenomeWindowEventsGenerator {

	private static final long 	serialVersionUID 				= -9014173267531950797L;	// Generated serial version ID
	private static final int  	SAVED_FORMAT_VERSION_NUMBER 	= 0;						// saved format version
	private List<GenomeWindowListener> 	gwListenerList;			// list of GenomeWindowListener
	private GenomeWindow				genomeWindow;			// the genome window displayed by the track
	private int							trackWidth;				// width of the tracks
	private transient double			xRatio;					// ratio of the track width to the genome window width



	/**
	 * Constructor of {@link ProjectWindow}
	 */
	protected ProjectWindow () {
		gwListenerList = new ArrayList<GenomeWindowListener>();
		genomeWindow = null;
		trackWidth = 0;
		xRatio = 0;
	}


	@Override
	public void addGenomeWindowListener(GenomeWindowListener genomeWindowListener) {
		if (!gwListenerList.contains(genomeWindowListener)) {
			gwListenerList.add(genomeWindowListener);
		}
	}


	/**
	 * @param doubleValue a double value
	 * @return the largest integer value that is less than or equal to the argument specified double value
	 * Returns Integer.MAX_VALUE if the specified double is greater than Integer.MAX_VALUE
	 * Returns Integer.MIN_VALUE if the specified double is smaller than Integer.MIN_VALUE
	 */
	private int doubleToIntFloor(double doubleValue) {
		if (doubleValue >= Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else if (doubleValue <= Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		} else {
			return (int)Math.floor(doubleValue);
		}
	}


	/**
	 * @param doubleValue a double value
	 * @return the closest integer value to the specified double value
	 * Returns Integer.MAX_VALUE if the specified double is greater than Integer.MAX_VALUE
	 * Returns Integer.MIN_VALUE if the specified double is smaller than Integer.MIN_VALUE
	 */
	private int doubleToIntRounded(double doubleValue) {
		if (doubleValue >= Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		} else if (doubleValue <= Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		} else {
			return (int) Math.round(doubleValue);
		}
	}


	/**
	 * @param position a position on a chromosome in base pair
	 * @return a position on the screen
	 */
	public long genomeToAbsoluteScreenPosition(int position) {
		return Math.round(position * xRatio);
	}


	/**
	 * @param genomePosition a position on the genome in bp
	 * @return a horizontal position in pixel corresponding to the specified genomic position (can be out of the track bounds)
	 */
	public int genomeToScreenPosition(int genomePosition) {
		double result = (genomePosition - genomeWindow.getStart()) * xRatio;
		return doubleToIntRounded(result);
	}


	/**
	 * @param genomeWidth a genomic width in bp
	 * @return a width in pixels corresponding to the specified genomic width
	 */
	public int genomeToScreenWidth(int genomeWidth) {
		double result = genomeWidth * xRatio;
		return doubleToIntRounded(result);
	}


	/**
	 * @return the genomeWindow
	 */
	public GenomeWindow getGenomeWindow() {
		return genomeWindow;
	}


	@Override
	public GenomeWindowListener[] getGenomeWindowListeners() {
		GenomeWindowListener[] genomeWindowListeners = new GenomeWindowListener[gwListenerList.size()];
		return gwListenerList.toArray(genomeWindowListeners);
	}


	/**
	 * @return the width of the tracks in pixel (all tracks have the same width)
	 */
	public int getTrackWidth() {
		return trackWidth;
	}


	/**
	 * @return the ratio of the track width to the genome window width
	 */
	public double getXRatio() {
		return xRatio;
	}


	/**
	 * Show {@link ProjectWindow} content
	 */
	public void print() {
		String info = "";
		info += "Factor: " + xRatio + "\n";
		info += "Window: " + genomeWindow.getStart() + " to " + genomeWindow.getStop() + ", size: " + genomeWindow.getSize() + "\n";
		info += "Factor / Window size: " + (xRatio / genomeWindow.getSize()) + "\n";
		info += "Window size / Factor: " + (genomeWindow.getSize() / xRatio);
		System.out.println(info);
	}


	/**
	 * Method used for unserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();
		setGenomeWindow((GenomeWindow) in.readObject());
		setTrackWidth(in.readInt());
		updateXRatio();
		gwListenerList = new ArrayList<GenomeWindowListener>();
	}


	/**
	 * Removes all registered {@link GenomeWindowListener}
	 */
	public void removeAllListeners () {
		gwListenerList.clear();
	}


	@Override
	public void removeGenomeWindowListener(GenomeWindowListener genomeWindowListener) {
		gwListenerList.remove(genomeWindowListener);
	}


	/**
	 * @param xScreen horizontal position on the screen (on the track) in pixel
	 * @return position on the genome in bp
	 */
	public int screenToGenomePosition(int xScreen) {
		double result = Math.floor(xScreen / xRatio);	// the value must be rounded the lowest int. Position 10,5 is still 10, not 11.
		result += genomeWindow.getStart();
		return doubleToIntFloor(result);
	}


	/**
	 * @param screenWidth a width on the screen in pixels
	 * @return the distance in bp corresponding to the specified screen distance
	 */
	public int screenToGenomeWidth(int screenWidth) {
		double result = screenWidth / xRatio;
		return doubleToIntFloor(result);
	}


	/**
	 * Sets the genome window value.  If the new value is different from the current one
	 * the {@link GenomeWindowListener} will be notified
	 * @param genomeWindow {@link GenomeWindow} to set
	 */
	public void setGenomeWindow(GenomeWindow genomeWindow) {
		if (!genomeWindow.equals(this.genomeWindow)) {
			GenomeWindow oldGenomeWindow = this.genomeWindow;
			this.genomeWindow = genomeWindow;
			// update the xRatio
			updateXRatio();
			// we notify the listeners
			GenomeWindowEvent evt = new GenomeWindowEvent(this, oldGenomeWindow, genomeWindow);
			if ((gwListenerList != null) && !gwListenerList.isEmpty()) {
				for (GenomeWindowListener currentListener: gwListenerList) {
					currentListener.genomeWindowChanged(evt);
				}
			}
		}
	}


	/**
	 * Sets this instance fields with the values of the specified {@link ProjectWindow}
	 * @param projectWindow
	 */
	public void setProjectWindow(ProjectWindow projectWindow) {
		setGenomeWindow(projectWindow.getGenomeWindow());
		setTrackWidth(projectWindow.getTrackWidth());
		updateXRatio();
	}


	/**
	 * Sets the width of the tracks in pixel
	 * @param trackWidth width of the track to set
	 */
	public void setTrackWidth(int trackWidth) {
		if (this.trackWidth != trackWidth) {
			this.trackWidth = trackWidth;
			// update the xRatio
			updateXRatio();
		}
	}


	/**
	 * Computes and sets the xRatio variable.
	 * The xRatio is defines as the ratio of the track width to the genome window width
	 */
	private void updateXRatio() {
		if (genomeWindow.getSize() == 0) {
			xRatio = 0;
		} else {
			xRatio = trackWidth / (double)(genomeWindow.getSize());
		}
	}


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);
		out.writeObject(getGenomeWindow());
		out.writeInt(getTrackWidth());
	}
}

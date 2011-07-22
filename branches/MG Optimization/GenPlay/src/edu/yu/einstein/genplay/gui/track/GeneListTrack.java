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
package edu.yu.einstein.genplay.gui.track;

import java.awt.Color;

import edu.yu.einstein.genplay.core.GenomeWindow;
import edu.yu.einstein.genplay.core.list.geneList.GeneList;
import edu.yu.einstein.genplay.util.History;

/**
 * A track containing a {@link GeneList}
 * @author Julien Lajugie
 * @version 0.1
 */
public class GeneListTrack extends Track<GeneList> implements VersionedTrack {

	private static final long serialVersionUID = 907497013953591152L; // generated ID

	
	/**
	 * Creates an instance of {@link GeneListTrack}
	 * @param displayedGenomeWindow displayed {@link GenomeWindow}
	 * @param trackNumber number of the track
	 * @param data {@link GeneList} showed in the track
	 */
	public GeneListTrack(GenomeWindow displayedGenomeWindow, int trackNumber,  GeneList data) {
		super(displayedGenomeWindow, trackNumber, data);
	}


	@Override
	protected TrackGraphics<GeneList> createsTrackGraphics(GenomeWindow displayedGenomeWindow, GeneList data) {
		return new GeneListTrackGraphics(displayedGenomeWindow, data);
	}
	
	
	/**
	 * @return the history of the current track.
	 */
	public History getHistory() {
		return ((GeneListTrackGraphics) trackGraphics).getHistory();
	}

	
	/**
	 * @return true if the action redo is possible.
	 */
	public boolean isRedoable() {
		return ((GeneListTrackGraphics) trackGraphics).isRedoable();
	}

	
	/**
	 * @return true if the track can be reseted
	 */
	public boolean isResetable() {
		return ((GeneListTrackGraphics) trackGraphics).isResetable();
	}

	
	/**
	 * @return true if the action undo is possible.
	 */
	public boolean isUndoable() {
		return ((GeneListTrackGraphics) trackGraphics).isUndoable();
	}

	
	/**
	 * Redoes last action
	 */
	public void redoData() {
		((GeneListTrackGraphics) trackGraphics).redoData();
	}
	
	/**
	 * Resets the Data. Restore the original data
	 */
	public void resetData() {
		((GeneListTrackGraphics) trackGraphics).resetData();
	}

	
	/**
	 * Sets the data showed in the track
	 * @param data
	 * @param description description of the data
	 */
	public void setData(GeneList data, String description) {
		((GeneListTrackGraphics) trackGraphics).setData(data, description);
	}

	
	/**
	 * Renames the track
	 * @param newName a new name for the track
	 */
	@Override
	public void setName(String newName) {
		// add the name of the track to the history
		getHistory().add("Track Name: \"" + newName + "\"",	new Color(0, 100, 0));
		super.setName(newName);
	}

	
	/**
	 * Changes the undo count of the track
	 * @param undoCount
	 */
	public void setUndoCount(int undoCount) {
		((GeneListTrackGraphics) trackGraphics).setUndoCount(undoCount);
	}
	

	/**
	 * Undoes last action
	 */
	public void undoData() {
		((GeneListTrackGraphics) trackGraphics).undoData();
	}
}
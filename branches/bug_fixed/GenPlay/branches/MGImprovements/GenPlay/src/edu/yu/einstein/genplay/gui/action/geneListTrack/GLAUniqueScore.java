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
package edu.yu.einstein.genplay.gui.action.geneListTrack;

import java.text.DecimalFormat;

import javax.swing.ActionMap;

import edu.yu.einstein.genplay.core.list.geneList.GeneList;
import edu.yu.einstein.genplay.core.list.geneList.operation.GLOUniqueScore;
import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.gui.action.TrackListActionOperationWorker;
import edu.yu.einstein.genplay.gui.dialog.NumberOptionPane;
import edu.yu.einstein.genplay.gui.track.GeneListTrack;



/**
 * Gives a score to the exons of the genes from a scored track
 * @author Julien Lajugie
 * @version 0.1
 */
public class GLAUniqueScore  extends TrackListActionOperationWorker<GeneList> {

	private static final long serialVersionUID = 2102571378866219218L; 	// generated ID
	private static final String 	ACTION_NAME = "Unique Score (Constant)";		// action name
	private static final String 	DESCRIPTION =
			"Set a unique score for all exons.";	// tooltip
	private GeneListTrack 			selectedTrack;						// selected track


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = "GLAUniqueScore";


	/**
	 * Creates an instance of {@link GLAUniqueScore}
	 */
	public GLAUniqueScore() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}


	@Override
	public Operation<GeneList> initializeOperation() throws Exception {
		if ((getTrackList().getSelectedTrack() != null) && (getTrackList().getSelectedTrack() instanceof GeneListTrack)) {
			selectedTrack = (GeneListTrack) getTrackList().getSelectedTrack();
			if (selectedTrack != null) {
				Number constant = NumberOptionPane.getValue(getRootPane(), "Unique Score", "Enter a score for all exons", new DecimalFormat("0.0"), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
				if ((constant != null) && (constant.doubleValue() != 0)) {
					GeneList geneList = selectedTrack.getData();
					operation = new GLOUniqueScore(geneList, constant.doubleValue());
					return operation;
				}
			}
		}
		return null;
	}


	@Override
	protected void doAtTheEnd(GeneList actionResult) {
		if (actionResult != null) {
			selectedTrack.setData(actionResult, operation.getDescription());
		}
	}
}
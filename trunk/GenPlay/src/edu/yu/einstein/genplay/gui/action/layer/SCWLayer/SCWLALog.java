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
package edu.yu.einstein.genplay.gui.action.layer.SCWLayer;

import javax.swing.ActionMap;

import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operation.SCWList.SCWLOLog;
import edu.yu.einstein.genplay.dataStructure.enums.LogBase;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.SCWList;
import edu.yu.einstein.genplay.gui.action.TrackListActionOperationWorker;
import edu.yu.einstein.genplay.gui.track.layer.AbstractSCWLayer;
import edu.yu.einstein.genplay.util.Utils;



/**
 * Applies a log function to the scores of the selected {@link AbstractSCWLayer}
 * @author Julien Lajugie
 */
public final class SCWLALog extends TrackListActionOperationWorker<SCWList> {

	private static final long serialVersionUID = -7633526345952471304L; // generated ID
	private static final String 		ACTION_NAME = "Log";			// action name
	private static final String 		DESCRIPTION =
			"Apply a log function to the scores of the selected layer" + HELP_TOOLTIP_SUFFIX;	// tooltip
	private static final String			HELP_URL = "http://genplay.einstein.yu.edu/wiki/index.php/Documentation#Log";
	private AbstractSCWLayer<SCWList>	selectedLayer;					// selected layer


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = SCWLALog.class.getName();


	/**
	 * Creates an instance of {@link SCWLALog}
	 */
	public SCWLALog() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
		putValue(HELP_URL_KEY, HELP_URL);
	}


	@Override
	protected void doAtTheEnd(SCWList actionResult) {
		if (actionResult != null)	{
			selectedLayer.setData(actionResult, operation.getDescription());
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public Operation<SCWList> initializeOperation() {
		selectedLayer = (AbstractSCWLayer<SCWList>) getValue("Layer");
		if (selectedLayer != null) {
			LogBase logBase = Utils.chooseLogBase(getRootPane());
			if (logBase != null) {
				SCWList scwList = selectedLayer.getData();
				Operation<SCWList> operation = new SCWLOLog(scwList, logBase);
				return operation;
			}
		}
		return null;
	}
}

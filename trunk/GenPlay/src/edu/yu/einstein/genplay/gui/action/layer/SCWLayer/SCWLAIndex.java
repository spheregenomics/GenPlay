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
import javax.swing.JOptionPane;

import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operation.SCWList.SCWLOIndex;
import edu.yu.einstein.genplay.core.operation.SCWList.SCWLOIndexByChromosome;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.SCWList;
import edu.yu.einstein.genplay.gui.action.TrackListActionOperationWorker;
import edu.yu.einstein.genplay.gui.dialog.NumberOptionPane;
import edu.yu.einstein.genplay.gui.track.layer.AbstractSCWLayer;


/**
 * Indexes the selected {@link AbstractSCWLayer}
 * @author Julien Lajugie
 */
public final class SCWLAIndex extends TrackListActionOperationWorker<SCWList> {

	private static final long serialVersionUID = -4566157311251154991L; // generated ID
	private static final String 		ACTION_NAME = "Index";			// action name
	private static final String 		DESCRIPTION = "Index the scores of the selected " +
			"layer between specified minimum and maximum values" + HELP_TOOLTIP_SUFFIX;		// tooltip
	private static final String			HELP_URL = "http://genplay.einstein.yu.edu/wiki/index.php/Documentation#Index";
	private AbstractSCWLayer<SCWList>	selectedLayer;					// selected layer

	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = SCWLAIndex.class.getName();


	/**
	 * Creates an instance of {@link SCWLAIndex}
	 */
	public SCWLAIndex() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
		putValue(HELP_URL_KEY, HELP_URL);
	}


	@Override
	protected void doAtTheEnd(SCWList actionResult) {
		if (actionResult != null) {
			selectedLayer.setData(actionResult, operation.getDescription());
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public Operation<SCWList> initializeOperation() {
		selectedLayer = (AbstractSCWLayer<SCWList>) getValue("Layer");
		if (selectedLayer != null) {
			Number indexMin = NumberOptionPane.getValue(getRootPane(), "Minimum", "New minimum score:", -1000000, 1000000, 0);
			if (indexMin != null) {
				Number indexMax = NumberOptionPane.getValue(getRootPane(), "Maximum", "New maximum score:", -1000000, 1000000, 100);
				if(indexMax != null) {
					SCWList scwList = selectedLayer.getData();
					Operation<SCWList> operation;
					if (JOptionPane.showConfirmDialog(
							getRootPane(),
							"Do you want to index each chromosome independently?",
							"Index",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE
							) == JOptionPane.YES_OPTION) {
						operation = new SCWLOIndexByChromosome(scwList, indexMin.floatValue(), indexMax.floatValue());
					} else {
						operation = new SCWLOIndex(scwList, indexMin.floatValue(), indexMax.floatValue());
					}
					return operation;
				}
			}
		}
		return null;
	}
}

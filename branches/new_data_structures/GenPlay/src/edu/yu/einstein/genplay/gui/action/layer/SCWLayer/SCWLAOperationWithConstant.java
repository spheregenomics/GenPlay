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
package edu.yu.einstein.genplay.gui.action.layer.SCWLayer;

import javax.swing.ActionMap;

import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operation.SCWList.SCWLOAddConstant;
import edu.yu.einstein.genplay.core.operation.SCWList.SCWLOInvertConstant;
import edu.yu.einstein.genplay.core.operation.SCWList.SCWLOMultiplyConstant;
import edu.yu.einstein.genplay.core.operation.SCWList.SCWLOSubtractConstant;
import edu.yu.einstein.genplay.core.operation.SCWList.SCWLOUniqueScore;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.SCWList;
import edu.yu.einstein.genplay.gui.action.TrackListActionOperationWorker;
import edu.yu.einstein.genplay.gui.dialog.OperationWithConstantDialog;
import edu.yu.einstein.genplay.gui.track.layer.AbstractSCWLayer;


/**
 * Performs an arithmetic operation on the score of the selected {@link AbstractSCWLayer}
 * @author Julien Lajugie
 */
public class SCWLAOperationWithConstant extends TrackListActionOperationWorker<SCWList> {

	/**
	 * Enumeration of the different type of operation with constant
	 * @author Julien Lajugie
	 */
	public enum OperationWithConstant {

		/** Add a constant */
		ADDITION ("Addition", "<html>Apply the following function f for each score x of the selected layer:<br>f(x) = x + constant</html>"),

		/** Subtract a constant */
		SUBTRACTION ("Subtraction", "<html>Apply the following function f for each score x of the selected layer:<br>f(x) = x - constant</html>"),

		/** Multiply by a constant */
		MULTIPLICATION ("Multiplication", "<html>Apply the following function f for each score x of the selected layer:<br>f(x) = x * constant</html>"),

		/** Invert */
		INVERTION ("Invertion", "<html>Apply the following function f for each score x of the selected layer:<br>f(x) = constant / x</html>"),

		/** Set a unique score */
		UNIQUE_SCORE ("Unique Score", "<html>Apply the following function f for each score x of the selected layer:<br>f(x) = constant</html>");

		private final 	String name;			// name of the operation
		private final	String description;		// description of the operation


		/**
		 * Creates an instance of {@link OperationWithConstant}
		 * @param name
		 * @param description
		 */
		private OperationWithConstant(String name, String description) {
			this.name = name;
			this.description = description;
		}


		/**
		 * @return the description of the operation
		 */
		public String getDescription() {
			return description;
		}


		/**
		 * @param scwList a {@link SCWList}
		 * @param constant a constant
		 * @return an {@link Operation} corresponding to the specified element of this enumeration
		 */
		public Operation<SCWList> getOperation(SCWList scwList, float constant) {
			switch (this) {
			case ADDITION:
				return new SCWLOAddConstant(scwList, constant);
			case SUBTRACTION:
				return new SCWLOSubtractConstant(scwList, constant);
			case MULTIPLICATION:
				return new SCWLOMultiplyConstant(scwList, constant);
			case INVERTION:
				return new SCWLOInvertConstant(scwList, constant);
			case UNIQUE_SCORE:
				return new SCWLOUniqueScore(scwList, constant);
			default:
				return null;
			}
		}


		@Override
		public String toString() {
			return name;
		}
	}


	private static final long serialVersionUID = 4027173438789911860L; 								// generated ID
	private static final String 		ACTION_NAME = "Constant Operation";							// action name
	private static final String 		DESCRIPTION =
			"Performs an arithmetic operation with a constant on the scores of the selected layer";	// tooltip
	private AbstractSCWLayer<SCWList>	selectedLayer;												// selected layer


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = SCWLAOperationWithConstant.class.getName();


	/**
	 * Creates an instance of {@link SCWLAOperationWithConstant}
	 */
	public SCWLAOperationWithConstant() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
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
		selectedLayer =  (AbstractSCWLayer<SCWList>) getValue("Layer");
		if (selectedLayer != null) {
			OperationWithConstantDialog owcDialog = new OperationWithConstantDialog();
			if (owcDialog.showDialog(getRootPane()) == OperationWithConstantDialog.APPROVE_OPTION) {
				float constant = owcDialog.getConstant();
				OperationWithConstant operationWithConstant = owcDialog.getOperation();
				SCWList scwList = selectedLayer.getData();
				operation = operationWithConstant.getOperation(scwList, constant);
				return operation;
			}
		}
		return null;
	}
}
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
package edu.yu.einstein.genplay.gui.action.layer.geneLayer;

import javax.swing.ActionMap;
import javax.swing.JOptionPane;

import edu.yu.einstein.genplay.core.operation.Operation;
import edu.yu.einstein.genplay.core.operation.geneList.GLOSumScore;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.geneList.GeneList;
import edu.yu.einstein.genplay.gui.action.TrackListActionOperationWorker;
import edu.yu.einstein.genplay.gui.track.layer.GeneLayer;
import edu.yu.einstein.genplay.util.NumberFormats;
import edu.yu.einstein.genplay.util.Utils;


/**
 * Returns the sum of the scores on the selected chromosomes of the selected {@link GeneLayer}
 * @author Julien Lajugie
 * @author Nicolas Fourel
 */
public final class GLASumScore extends TrackListActionOperationWorker<Double> {

	private static final long serialVersionUID = -7198642565173540167L;	// generated ID
	private static final String 	ACTION_NAME = "Score Count";		// action name
	private static final String 	DESCRIPTION =
			"Return the sum of the scores on the " +
					"selected chromosomes of the selected layer" + HELP_TOOLTIP_SUFFIX;	// tooltip
	private static final String		HELP_URL = "http://genplay.einstein.yu.edu/wiki/index.php/Documentation#Score_Count";


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = GLASumScore.class.getName();


	/**
	 * Creates an instance of {@link GLASumScore}
	 */
	public GLASumScore() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
		putValue(HELP_URL_KEY, HELP_URL);
	}


	@Override
	protected void doAtTheEnd(Double actionResult) {
		if (actionResult != null) {
			JOptionPane.showMessageDialog(getRootPane(), "Score count: \n" + NumberFormats.getScoreFormat().format(actionResult), "Score Count", JOptionPane.INFORMATION_MESSAGE);
		}
	}


	@Override
	public Operation<Double> initializeOperation() {
		GeneLayer selectedLayer = (GeneLayer) getValue("Layer");
		if (selectedLayer != null) {
			boolean[] selectedChromo = Utils.chooseChromosomes(getRootPane());
			if (selectedChromo != null) {
				GeneList binList = selectedLayer.getData();
				Operation<Double> operation = new GLOSumScore(binList, selectedChromo);
				return operation;
			}
		}
		return null;
	}
}

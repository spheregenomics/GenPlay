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
package edu.yu.einstein.genplay.gui.customComponent.scatterPlot.action;

import java.awt.event.ActionEvent;

import edu.yu.einstein.genplay.gui.customComponent.scatterPlot.AxisOptionDialog;
import edu.yu.einstein.genplay.gui.customComponent.scatterPlot.ScatterPlotAxis;
import edu.yu.einstein.genplay.gui.customComponent.scatterPlot.ScatterPlotPane;



/**
 * Set the x-axis of the scatter plot chart
 * @author Julien Lajugie
 * @version 0.1
 */
public class SPAXAxisOptions extends ScatterPlotAction {

	private static final long serialVersionUID = 217368043621971648L;	// generated ID
	private static final String 	ACTION_NAME = "X-Axis Options";		// action name
	private static final String 	DESCRIPTION =
			"Configure the X-Axis";								// tooltip


	/**
	 * Creates an instance of {@link SPAXAxisOptions}
	 * @param scatterPlotPane a {@link ScatterPlotPane}
	 */
	public SPAXAxisOptions(ScatterPlotPane scatterPlotPane) {
		super(scatterPlotPane);
		putValue(NAME, ACTION_NAME);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		ScatterPlotAxis xAxis = getScatterPlotPane().getxAxis();
		AxisOptionDialog aod = new AxisOptionDialog();
		aod.setMin(xAxis.getMin());
		aod.setMax(xAxis.getMax());
		aod.setMajorUnit(xAxis.getMajorUnit());
		aod.setMinorUnit(xAxis.getMinorUnit());
		aod.setShowGrid(xAxis.isShowGrid());
		aod.setLogScale(xAxis.isLogScale());
		aod.setLogBase(xAxis.getLogBase());
		if (aod.showDialog(getRootPane(), "X-Axis Options") == AxisOptionDialog.APPROVE_OPTION) {
			xAxis.setMin(aod.getMin());
			xAxis.setMax(aod.getMax());
			xAxis.setMajorUnit(aod.getMajorUnit());
			xAxis.setMinorUnit(aod.getMinorUnit());
			xAxis.setShowGrid(aod.isShowGrid());
			xAxis.setLogScale(aod.isLogScale());
			xAxis.setLogBase(aod.getLogBase());
			getScatterPlotPane().repaint();
		}
	}
}

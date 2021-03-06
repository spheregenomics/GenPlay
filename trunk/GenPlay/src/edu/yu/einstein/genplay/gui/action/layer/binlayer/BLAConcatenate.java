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
package edu.yu.einstein.genplay.gui.action.layer.binlayer;

import java.io.File;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.filechooser.FileFilter;

import edu.yu.einstein.genplay.core.IO.writer.binListWriter.ConcatenateBinListWriter;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.binList.BinList;
import edu.yu.einstein.genplay.gui.action.TrackListActionWorker;
import edu.yu.einstein.genplay.gui.dialog.layerChooser.LayerChooserDialog;
import edu.yu.einstein.genplay.gui.fileFilter.TextFileFilter;
import edu.yu.einstein.genplay.gui.track.layer.BinLayer;
import edu.yu.einstein.genplay.gui.track.layer.Layer;
import edu.yu.einstein.genplay.gui.track.layer.LayerType;
import edu.yu.einstein.genplay.util.FileChooser;



/**
 * Concatenates the selected layer with other layers in an output file
 * @author Julien Lajugie
 */
public class BLAConcatenate extends TrackListActionWorker<Void> {

	private static final long serialVersionUID = 6381691669271998493L;			// generated ID
	private static final String 	ACTION_NAME = "Concatenate";				// action name
	private static final String 	DESCRIPTION =
			"Concatenate the selected layer with other layers in an output file" + HELP_TOOLTIP_SUFFIX;	// tooltip
	private static final String		HELP_URL = "http://genplay.einstein.yu.edu/wiki/index.php/Documentation#Concatenate";
	private ConcatenateBinListWriter writer;										// writer that generate the output


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = BLAConcatenate.class.getName();


	/**
	 * Creates an instance of {@link BLAConcatenate}
	 */
	public BLAConcatenate() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
		putValue(HELP_URL_KEY, HELP_URL);
	}


	@Override
	protected void doAtTheEnd(Void actionResult) {} // nothing to do


	@Override
	protected Void processAction() throws Exception {
		LayerChooserDialog layerChooserDialog = new LayerChooserDialog();
		layerChooserDialog.setLayers(getTrackListPanel().getModel().getAllLayers());
		LayerType[] selectableLayers = {LayerType.BIN_LAYER};
		layerChooserDialog.setSelectableLayerTypes(selectableLayers);
		layerChooserDialog.setMultiselectable(true);
		if (layerChooserDialog.showDialog(getRootPane(), "Select Layers to Concatenate") == LayerChooserDialog.APPROVE_OPTION) {
			List<Layer<?>> selectedLayers = layerChooserDialog.getSelectedLayers();
			if ((selectedLayers != null) && !selectedLayers.isEmpty()) {
				// save dialog
				File selectedFile = FileChooser.chooseFile(getRootPane(), FileChooser.SAVE_FILE_MODE, "Save As", new FileFilter[] {new TextFileFilter()}, false);
				if(selectedFile != null) {
					// create arrays with the selected BinLists and names
					BinList[] binListArray = new BinList[selectedLayers.size()];
					String[] nameArray = new String[selectedLayers.size()];
					for (int i = 0; i < selectedLayers.size(); i++) {
						binListArray[i] = ((BinLayer)selectedLayers.get(i)).getData();
						nameArray[i] = selectedLayers.get(i).getName();
					}
					notifyActionStart("Generating File", 1, true);
					writer = new ConcatenateBinListWriter(binListArray, nameArray, selectedFile);
					writer.write();
				}
			}
		}
		return null;
	}


	@Override
	public void stop() {
		writer.stop();
		super.stop();
	}
}

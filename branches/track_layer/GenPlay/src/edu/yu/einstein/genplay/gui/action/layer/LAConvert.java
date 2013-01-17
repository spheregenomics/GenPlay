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
package edu.yu.einstein.genplay.gui.action.layer;

import javax.swing.ActionMap;

import edu.yu.einstein.genplay.core.converter.Converter;
import edu.yu.einstein.genplay.core.converter.ConverterFactory;
import edu.yu.einstein.genplay.core.enums.DataPrecision;
import edu.yu.einstein.genplay.core.enums.ScoreCalculationMethod;
import edu.yu.einstein.genplay.core.list.ChromosomeListOfLists;
import edu.yu.einstein.genplay.core.list.SCWList.MaskWindowList;
import edu.yu.einstein.genplay.core.list.SCWList.ScoredChromosomeWindowList;
import edu.yu.einstein.genplay.core.list.binList.BinList;
import edu.yu.einstein.genplay.core.list.geneList.GeneList;
import edu.yu.einstein.genplay.gui.action.TrackListActionWorker;
import edu.yu.einstein.genplay.gui.dialog.ConvertDialog;
import edu.yu.einstein.genplay.gui.track.Track;
import edu.yu.einstein.genplay.gui.track.layer.BinLayer;
import edu.yu.einstein.genplay.gui.track.layer.GeneLayer;
import edu.yu.einstein.genplay.gui.track.layer.Layer;
import edu.yu.einstein.genplay.gui.track.layer.LayerType;
import edu.yu.einstein.genplay.gui.track.layer.MaskLayer;
import edu.yu.einstein.genplay.gui.track.layer.SCWLayer;
import edu.yu.einstein.genplay.gui.track.layer.VersionedLayer;
import edu.yu.einstein.genplay.util.colors.Colors;


/**
 * Converts the selected {@link Layer} into another type of {@link Layer}
 * @author Nicolas Fourel
 * @version 0.1
 */
public class LAConvert extends TrackListActionWorker<ChromosomeListOfLists<?>> {

	private static final long serialVersionUID = 4027173438789911860L; 	// generated ID
	private static final String 	ACTION_NAME = "Convert Layer";// action name
	private static final String 	DESCRIPTION = "Convert the current layer into another type of layer";			// tooltip
	private Layer<?> 				selectedLayer;					// The selected layer.
	private Track					resultTrack;					// The result track.
	private Converter				converter;						// The track converter.

	private ChromosomeListOfLists<?> 	data;
	private LayerType 					layerType;
	private String 						layerName;
	private int 						binSize;
	private DataPrecision 				precision;
	private ScoreCalculationMethod 		method;


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = LAConvert.class.getName();


	/**
	 * Creates an instance of {@link LAConvert}
	 */
	public LAConvert() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);

		resultTrack = null;
		data = null;
		layerType = null;
		layerName = "";
		binSize = 0;
		precision = null;
		method = null;
	}


	@Override
	public ChromosomeListOfLists<?> processAction() {
		selectedLayer = (Layer<?>) getValue("Layer");
		ConvertDialog dialog = new ConvertDialog(selectedLayer);
		if (dialog.showDialog(getRootPane()) == ConvertDialog.APPROVE_OPTION) {
			layerType = dialog.getOutputLayerType();
			if (layerType == LayerType.BIN_LAYER) {
				binSize = dialog.getBinSize();
				precision = dialog.getDataPrecision();
				method = dialog.getScoreCalculationMethod();
			}
			resultTrack = dialog.getOutputTrack();
			layerName = dialog.getOutputLayerName();
			data = (ChromosomeListOfLists<?>) selectedLayer.getData();

			if (data != null) {
				converter = ConverterFactory.getConverter(data, layerType, binSize, precision, method);
			}

			if (converter != null) {
				try {
					converter.convert();
					return converter.getList();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("No converter found");
			}
		}
		return null;
	}


	@Override
	protected void doAtTheEnd(ChromosomeListOfLists<?> actionResult) {
		if (actionResult != null) {
			Layer<?> newLayer = null;
			if (layerType == LayerType.GENE_LAYER) {
				newLayer = new GeneLayer(resultTrack, (GeneList) actionResult, layerName);
			} else if (layerType == LayerType.BIN_LAYER) {
				newLayer = new BinLayer(resultTrack, (BinList) actionResult, layerName);
			} else if (layerType == LayerType.SCW_LAYER) {
				newLayer = new SCWLayer(resultTrack, (ScoredChromosomeWindowList)actionResult, layerName);
			} else if (layerType == LayerType.MASK_LAYER) {
				newLayer = new MaskLayer(resultTrack, (MaskWindowList) actionResult, layerName);
			}
			if (newLayer != null) {
				((VersionedLayer<?>) newLayer).getHistory().add(layerType + " generated from " + selectedLayer.getType() + " \"" + selectedLayer.getName()+ "\"", Colors.GREY);
				resultTrack.getLayers().add(newLayer);
			} else {
				System.err.println("The track could not be converted");
			}
		}
	}

}

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
package edu.yu.einstein.genplay.gui.action.track;

import java.awt.event.ActionEvent;

import javax.swing.ActionMap;

import edu.yu.einstein.genplay.gui.MGDisplaySettings.MGDisplaySettings;
import edu.yu.einstein.genplay.gui.MGDisplaySettings.VariantLayerDisplaySettings;
import edu.yu.einstein.genplay.gui.action.TrackListAction;
import edu.yu.einstein.genplay.gui.action.multiGenome.properties.MGARefresh;
import edu.yu.einstein.genplay.gui.dialog.multiGenomeDialog.addOrEditVariantLayer.AddOrEditVariantLayerDialog;
import edu.yu.einstein.genplay.gui.track.Track;
import edu.yu.einstein.genplay.gui.track.layer.Layer;
import edu.yu.einstein.genplay.gui.track.layer.variantLayer.VariantLayer;


/**
 * Edit the {@link VariantLayer} information
 * @author Julien Lajugie
 * @author Nicolas Fourel
 * @version 0.1
 */
public class TAEditVariantLayer extends TrackListAction {

	private static final long serialVersionUID = 5229478480046927796L;
	private static final String ACTION_NAME = "Edit Variant Layer"; // action name
	private static final String DESCRIPTION = "Edit the layer information" + HELP_TOOLTIP_SUFFIX; // tooltip
	private static final String	HELP_URL = "http://genplay.einstein.yu.edu/wiki/index.php/Documentation#Edit_Variant_Layer";


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = TAEditVariantLayer.class.getName();


	/**
	 * Creates an instance of {@link TAEditVariantLayer}
	 */
	public TAEditVariantLayer() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
		putValue(HELP_URL_KEY, HELP_URL);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void trackListActionPerformed(ActionEvent arg0) {
		final Track selectedTrack = getTrackListPanel().getSelectedTrack();
		if (selectedTrack != null) {
			Layer<VariantLayerDisplaySettings> layer = (Layer<VariantLayerDisplaySettings>) getValue("Layer");
			VariantLayerDisplaySettings data = AddOrEditVariantLayerDialog.showEditDialog(getRootPane(), layer.getData());
			if (data != null) {
				MGDisplaySettings settings = MGDisplaySettings.getInstance();
				data.setHasChanged(true);
				layer.setData(data);
				// Updates track (filters, display)
				MGARefresh action = new MGARefresh();
				action.setPreviousFilterList(settings.getFilterSettings().getAllMGFilters());
				action.actionPerformed(null);
			}
		}
	}
}

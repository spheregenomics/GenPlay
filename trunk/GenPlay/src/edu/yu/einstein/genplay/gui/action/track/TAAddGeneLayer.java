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

import javax.swing.ActionMap;

import edu.yu.einstein.genplay.core.IO.dataReader.GeneReader;
import edu.yu.einstein.genplay.core.IO.extractor.Extractor;
import edu.yu.einstein.genplay.core.manager.project.ProjectManager;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.geneList.GeneList;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.geneList.GeneListFactory;
import edu.yu.einstein.genplay.exception.exceptions.InvalidFileTypeException;
import edu.yu.einstein.genplay.gui.action.TrackListActionExtractorWorker;
import edu.yu.einstein.genplay.gui.dialog.multiGenomeDialog.trackGenomeSelection.GenomeSelectionDialog;
import edu.yu.einstein.genplay.gui.track.Track;
import edu.yu.einstein.genplay.gui.track.layer.GeneLayer;
import edu.yu.einstein.genplay.util.colors.Colors;



/**
 * Adds a {@link GeneLayer} to the selected track
 * @author Julien Lajugie
 */
public final class TAAddGeneLayer extends TrackListActionExtractorWorker<GeneList> {

	private static final long serialVersionUID = -6264760599336397028L;				// generated ID
	private static final String 	ACTION_NAME = "Add Gene Layer";					// action name
	private static final String 	DESCRIPTION = "Add a layer displaying genes";	// tooltip


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = TAAddGeneLayer.class.getName();


	/**
	 * Creates an instance of {@link TAAddGeneLayer}
	 * @param extractor the extractor that will extract the data
	 */
	public TAAddGeneLayer(Extractor extractor) {
		super(extractor);
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}


	@Override
	public void doAtTheEnd(GeneList actionResult) {
		boolean valid = true;
		if (ProjectManager.getInstance().isMultiGenomeProject() && (genomeName == null)) {
			valid = false;
		}
		if ((actionResult != null) && valid) {
			Track selectedTrack = getTrackListPanel().getSelectedTrack();
			GeneLayer newLayer = new GeneLayer(selectedTrack, actionResult, name);
			newLayer.getHistory().add("Load " + extractor.getDataFile().getAbsolutePath(), Colors.GREY);
			selectedTrack.getLayers().add(newLayer);
			selectedTrack.setActiveLayer(newLayer);
		}
	}


	@Override
	protected void doBeforeExtraction() throws InterruptedException {
		if (ProjectManager.getInstance().isMultiGenomeProject()) {
			GenomeSelectionDialog genomeDialog = new GenomeSelectionDialog();
			if (genomeDialog.showDialog(getRootPane()) == GenomeSelectionDialog.APPROVE_OPTION) {
				genomeName = genomeDialog.getGenomeName();
				alleleType = genomeDialog.getAlleleType();
			} else {
				throw new InterruptedException();
			}
		}
	}


	@Override
	protected GeneList generateList() throws Exception {
		notifyActionStart("Generating Gene Layer", 1, true);
		try {
			GeneReader geneReader = (GeneReader) extractor;
			GeneList geneList = GeneListFactory.createGeneList(geneReader);
			return geneList;
		} catch (ClassCastException e) {
			throw new InvalidFileTypeException();
		}
	}
}

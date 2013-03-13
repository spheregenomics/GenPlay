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
package edu.yu.einstein.genplay.gui.action.track;

import java.io.File;
import java.text.NumberFormat;

import javax.swing.ActionMap;
import javax.swing.JOptionPane;

import edu.yu.einstein.genplay.core.IO.extractor.ReadLengthAndShiftHandler;
import edu.yu.einstein.genplay.core.IO.extractor.StrandedExtractor;
import edu.yu.einstein.genplay.core.generator.BinListGenerator;
import edu.yu.einstein.genplay.core.manager.project.ProjectManager;
import edu.yu.einstein.genplay.dataStructure.enums.DataPrecision;
import edu.yu.einstein.genplay.dataStructure.enums.ScoreCalculationMethod;
import edu.yu.einstein.genplay.dataStructure.enums.Strand;
import edu.yu.einstein.genplay.dataStructure.genomeList.binList.BinList;
import edu.yu.einstein.genplay.gui.action.TrackListActionExtractorWorker;
import edu.yu.einstein.genplay.gui.dialog.newCurveLayerDialog.NewCurveLayerDialog;
import edu.yu.einstein.genplay.gui.track.Track;
import edu.yu.einstein.genplay.gui.track.layer.BinLayer;
import edu.yu.einstein.genplay.util.Utils;
import edu.yu.einstein.genplay.util.colors.Colors;


/**
 * Adds a {@link BinLayer} to the specified track
 * @author Julien Lajugie
 * @version 0.1
 */
public final class TAAddBinLayer extends TrackListActionExtractorWorker<BinList> {

	private static final long serialVersionUID = -3974211916629578143L;	// generated ID
	private static final String 	ACTION_NAME = "Add Fixed Window Layer"; 						// action name
	private static final String 	DESCRIPTION = "Add a layer displaying bins with a fixed size"; 	// tooltip
	private int 					binSize = 0;													// Size of the bins of the BinList
	private ScoreCalculationMethod 	scoreCalculation = null;										// Method of calculation of the score of the BinList
	private DataPrecision 			precision = DataPrecision.PRECISION_32BIT;						// Precision of the Data
	private BinListGenerator 		binListGenerator;												// BinList Generator
	private Strand					strand = null;													// strand to extract
	private int						strandShift = 0;												// position shift on a strand
	private int 					readLength = 0;													// user specified length of the reads (0 to keep the original length)


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = TAAddBinLayer.class.getName();


	/**
	 * Creates an instance of {@link TAAddBinLayer}
	 */
	public TAAddBinLayer() {
		super(BinListGenerator.class);
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}


	@Override
	public void doAtTheEnd(BinList actionResult) {
		if (actionResult != null) {
			Track selectedTrack = getTrackListPanel().getSelectedTrack();
			BinLayer newLayer = new BinLayer(selectedTrack, actionResult, fileToExtract.getName());
			// add the history to the layer
			String history = "Bin Size = " + actionResult.getBinSize() + "bp, Precision = " + actionResult.getPrecision() + ", Score Count = " + NumberFormat.getInstance().format(actionResult.getSumScore());
			if (binListGenerator.isCriterionNeeded()) {
				history += ", Method of Calculation = " + scoreCalculation;
			}
			if (strand != null) {
				history += ", Strand = ";
				if (strand == Strand.FIVE) {
					history += "5'";
				} else {
					history += "3'";
				}
			}
			if (strandShift != 0) {
				history += ", Strand Shift = " + strandShift +"bp";
			}
			if (readLength != 0) {
				history += ", Read Length = " + readLength +"bp";
			}
			newLayer.getHistory().add("Load " + fileToExtract.getAbsolutePath(), Colors.GREY);
			newLayer.getHistory().add(history, Colors.GREY);
			selectedTrack.getLayers().add(newLayer);
			selectedTrack.setActiveLayer(newLayer);
		}
	}


	@Override
	protected void doBeforeExtraction() throws InterruptedException {
		binListGenerator = (BinListGenerator)extractor;
		boolean isStrandNeeded = extractor instanceof StrandedExtractor;
		NewCurveLayerDialog ncld = new NewCurveLayerDialog(name, true, binListGenerator.isBinSizeNeeded(), binListGenerator.isPrecisionNeeded(), binListGenerator.isCriterionNeeded(), isStrandNeeded, true, true);
		if (ncld.showDialog(getRootPane()) == NewCurveLayerDialog.APPROVE_OPTION) {
			name = ncld.getLayerName();
			binSize = ncld.getBinSize();
			scoreCalculation = ncld.getScoreCalculationMethod();
			precision = ncld.getDataPrecision();
			selectedChromo = ncld.getSelectedChromosomes();
			// if not all the chromosomes are selected we need
			// to ask the user if the file is sorted or not
			if (!Utils.allChromosomeSelected(selectedChromo)) {
				int dialogResult = JOptionPane.showConfirmDialog(getRootPane(), "GenPlay can accelerate the loading if you know that your file is sorted by chromosome." +
						"Press yes only if you know that your file is sorted.\n" +
						"If you press yes and your file is not sorted, the file may load incompletely, leading to a loss of valuable information.\n" +
						"The chromosomes must be ordered the same way it is ordered in the chromosome selection combo-box.\n\n" +
						"Is your file sorted?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (dialogResult == JOptionPane.YES_OPTION) {
					extractor.setFileSorted(true);
				} else if (dialogResult == JOptionPane.NO_OPTION) {
					extractor.setFileSorted(false);
				} else if (dialogResult == JOptionPane.CLOSED_OPTION) {
					throw new InterruptedException();
				}
			}
			extractor.setSelectedChromosomes(selectedChromo);
			if (isStrandNeeded) {
				strand = ncld.getStrandToExtract();
				strandShift = ncld.getStrandShiftValue();
				readLength = ncld.getReadLengthValue();
				((StrandedExtractor) extractor).selectStrand(strand);
				((StrandedExtractor) extractor).setReadLengthAndShiftHandler(new ReadLengthAndShiftHandler(strandShift, readLength));
			}
			if (ProjectManager.getInstance().isMultiGenomeProject()) {
				genomeName = ncld.getGenomeName();
			}
		} else {
			throw new InterruptedException();
		}
	}


	@Override
	protected BinList generateList() throws Exception {
		notifyActionStop();
		// if the binSize is known we can find out how many steps will be used
		if (binListGenerator.isBinSizeNeeded()) {
			notifyActionStart("Generating Layer", 1 + BinList.getCreationStepCount(binSize), true);
		} else {
			notifyActionStart("Generating Layer", 1, true);
		}
		return ((BinListGenerator) extractor).toBinList(binSize, precision, scoreCalculation);
	}


	@Override
	protected File retrieveFileToExtract() {
		String defaultDirectory = ProjectManager.getInstance().getProjectConfiguration().getDefaultDirectory();
		File selectedFile = Utils.chooseFileToLoad(getRootPane(), "Load Fixed Window Layer", defaultDirectory, Utils.getReadableBinListFileFilters(), true);
		if (selectedFile != null) {
			return selectedFile;
		} else {
			return null;
		}
	}
}

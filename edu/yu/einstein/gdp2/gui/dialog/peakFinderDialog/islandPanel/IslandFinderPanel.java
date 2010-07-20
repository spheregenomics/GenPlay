/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.gui.dialog.peakFinderDialog.islandPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import yu.einstein.gdp2.core.enums.IslandResultType;
import yu.einstein.gdp2.core.list.binList.BinList;
import yu.einstein.gdp2.core.list.binList.operation.BLOFindIslands;
import yu.einstein.gdp2.core.operation.Operation;
import yu.einstein.gdp2.gui.dialog.peakFinderDialog.PeakFinderPanel;


/**
 * A frame allowing to configure the properties of the island finder.
 * @author Nicolas Fourel
 * @version 0.1
 */
public final class IslandFinderPanel extends JPanel implements PeakFinderPanel {
	
	private static final long serialVersionUID = 8143320501058939077L;

	private final IslandDialogInformation	idfInformation;					// panel to manage additional information
	private final IslandDialogInput			idfInput;						// panel to manage input parameters
	private final IslandDialogOutput		idfOutput;						// panel to manage output data
	private BLOFindIslands 					bloFindIslands;					// Island Finder object needs to set parameters (ReadCountLimit, p-value, gap, cut-off)

	
	/**
	 * Constructor, create an instance of IslandDialog.
	 * @param island 	IslandFinder object, needs to define p-value/readCountLimit parameters.
	 */
	public IslandFinderPanel(BLOFindIslands bloFindIslands) {
		super();
		setName("Island Finder");
		this.bloFindIslands = bloFindIslands;
		
		//Fieldset initialization
		this.idfInformation = new IslandDialogInformation("Information", this.bloFindIslands.getIsland());
		this.idfInput = new IslandDialogInput("Input", this.bloFindIslands.getIsland(), (IslandDialogInformation)this.idfInformation);
		this.idfOutput = new IslandDialogOutput("Output", this.bloFindIslands.getIsland());
		
		//Layout Manager
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		Insets gbcInsets = new Insets (10, 0, 10, 0);
		
		// idfInformation
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = gbcInsets;
		this.add(this.idfInformation, gbc);
		
		// idfInput
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = gbcInsets;
		this.add(this.idfInput, gbc);
		
		// idfOuptut
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = gbcInsets;
		this.add(this.idfOutput, gbc);
	}
	
	
	@Override
	public String toString() {
		return getName();
	}


	@Override
	public void saveInput() {
		IslandDialogInput.windowMinValueStore = this.idfInput.getWindowLimitValue();
		IslandDialogInput.gapStore = this.idfInput.getGap();
		IslandDialogInput.IslandMinScoreStore = this.idfInput.getIslandLimitScore();
		IslandDialogInput.IslandMinLengthStore = this.idfInput.getMinIslandLength();
		IslandDialogOutput.FilteredStore = this.idfOutput.filteredSelected();
		IslandDialogOutput.IFScoreStore = this.idfOutput.IFScoreSelected();
	}


	@Override
	public Operation<BinList[]> validateInput() {
		if (this.idfOutput.filteredSelected() | this.idfOutput.IFScoreSelected()) {	// requirements to approved
			//All islands finder parameters must be set
			this.bloFindIslands.getIsland().setWindowMinValue(this.idfInput.getWindowLimitValue());
			this.bloFindIslands.getIsland().setGap(this.idfInput.getGap());
			this.bloFindIslands.getIsland().setIslandMinScore(this.idfInput.getIslandLimitScore());
			this.bloFindIslands.getIsland().setIslandMinLength(this.idfInput.getMinIslandLength());
			//IslandResultType array to manage the right number of track for the BLAIslands object
			IslandResultType[] list = new IslandResultType[2];
			if (this.idfOutput.filteredSelected()) {
				list[0] = IslandResultType.FILTERED;
			}
			if (this.idfOutput.IFScoreSelected()) {
				list[1] = IslandResultType.IFSCORE;
			}
			this.bloFindIslands.setList(list);
			return bloFindIslands;
		} else {
			JOptionPane.showMessageDialog(getRootPane(), "Please select at least one result type", "Invalid Input", JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}
}
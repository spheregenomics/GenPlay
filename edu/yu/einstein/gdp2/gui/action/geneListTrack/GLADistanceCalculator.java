package yu.einstein.gdp2.gui.action.geneListTrack;

import javax.swing.ActionMap;

import yu.einstein.gdp2.core.list.geneList.GeneList;
import yu.einstein.gdp2.core.list.geneList.operation.GLODistanceCalculator;
import yu.einstein.gdp2.core.operation.Operation;
import yu.einstein.gdp2.gui.action.TrackListActionOperationWorker;
import yu.einstein.gdp2.gui.dialog.DistanceCalculatorDialog;
import yu.einstein.gdp2.gui.dialog.TrackChooser;
import yu.einstein.gdp2.gui.track.GeneListTrack;

public class GLADistanceCalculator extends TrackListActionOperationWorker<Double[][]>{

	private static final long serialVersionUID = 1401297625985870348L;
	private static final String 	ACTION_NAME = "Distance Calculation";		// action name
	private static final String 	DESCRIPTION = 
		"Compute the number of base pairs at a specific distance between " +
		"the selected track and another track";							// tooltip
	private GeneListTrack 			selectedTrack;						// 1st selected track  	
	private GeneListTrack 			otherTrack;							// 2nd selected track
	private DistanceCalculatorDialog dcd;	
	
	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = "BLADistanceCalculator";


	/**
	 * Creates an instance of {@link BLADistanceCalculator}
	 */
	public GLADistanceCalculator() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}

	@Override
	public Operation<Double[][]> initializeOperation() throws Exception {
		selectedTrack = (GeneListTrack) getTrackList().getSelectedTrack();
		if (selectedTrack != null) {
			otherTrack = (GeneListTrack) TrackChooser.getTracks(getRootPane(), "Choose A Track", "Calculate the correlation with:", getTrackList().getBinListTracks());
			if (otherTrack != null) {
				GeneList geneList1 = selectedTrack.getData();
				GeneList geneList2 = otherTrack.getData();
				dcd = new DistanceCalculatorDialog();
				Operation<Double[][]> operation = new GLODistanceCalculator(geneList1, geneList2, dcd.getSelectionFlag());
				return operation;
			}
		}
		return null;
	}

}

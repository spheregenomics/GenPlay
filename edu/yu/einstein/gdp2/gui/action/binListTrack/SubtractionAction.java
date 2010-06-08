/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.gui.action.binListTrack;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.ActionMap;

import yu.einstein.gdp2.core.enums.DataPrecision;
import yu.einstein.gdp2.core.list.binList.BinList;
import yu.einstein.gdp2.core.list.binList.operation.BLOSubtract;
import yu.einstein.gdp2.core.list.binList.operation.BinListOperation;
import yu.einstein.gdp2.core.manager.ConfigurationManager;
import yu.einstein.gdp2.core.manager.ExceptionManager;
import yu.einstein.gdp2.exception.BinListDifferentWindowSizeException;
import yu.einstein.gdp2.gui.action.TrackListAction;
import yu.einstein.gdp2.gui.dialog.TrackChooser;
import yu.einstein.gdp2.gui.track.BinListTrack;
import yu.einstein.gdp2.gui.track.Track;
import yu.einstein.gdp2.gui.worker.actionWorker.ActionWorker;
import yu.einstein.gdp2.util.Utils;


/**
 * Subtracts the selected {@link Track} with another one. Creates a new track from the result
 * @author Julien Lajugie
 * @version 0.1
 */
public final class SubtractionAction extends TrackListAction {

	private static final long serialVersionUID = -2313977686484948489L; // generated ID
	private static final String 	ACTION_NAME = "Subtraction";		// action name
	private static final String 	DESCRIPTION = 
		"Subtract another track from the selected one";					// tooltip


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = "SubtractionAction";


	/**
	 * Creates an instance of {@link SubtractionAction}
	 */
	public SubtractionAction() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}


	/**
	 *  Subtracts the selected {@link Track} with another one. Creates a new track from the result
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		final BinListTrack selectedTrack = (BinListTrack) getTrackList().getSelectedTrack();
		if (selectedTrack != null) {
			final Track otherTrack = TrackChooser.getTracks(getRootPane(), "Choose A Track", "Choose a track to subtract from the selected track:", getTrackList().getBinListTracks());
			if(otherTrack != null) {
				final Track resultTrack = TrackChooser.getTracks(getRootPane(), "Choose A Track", "Generate the result on track:", getTrackList().getEmptyTracks());
				if (resultTrack != null) {
					final DataPrecision precision = Utils.choosePrecision(getRootPane());
					if (precision != null) {						
						final BinList binList1 = ((BinListTrack)selectedTrack).getBinList();
						final BinList binList2 = ((BinListTrack)otherTrack).getBinList();
						final BinListOperation<BinList> operation = new BLOSubtract(binList1, binList2, precision);
						// thread for the action
						new ActionWorker<BinList>(getTrackList(), "Subtracting") {
							@Override
							protected BinList doAction() {
								try {
									return operation.compute();
								} catch (BinListDifferentWindowSizeException e) {
									ExceptionManager.handleException(getRootPane(), e, "Subtracting two tracks with different window sizes is not allowed");
									return null;
								} catch (Exception e) {
									ExceptionManager.handleException(getRootPane(), e, "Error while subtracting the tracks");
									return null;
								}
							}
							@Override
							protected void doAtTheEnd(BinList actionResult) {
								int index = resultTrack.getTrackNumber() - 1;
								BinListTrack newTrack = new BinListTrack(getTrackList().getGenomeWindow(), index + 1, actionResult);
								// add info to the history
								newTrack.getHistory().add("Result of the subtraction of " + selectedTrack.getName() + " by " + otherTrack.getName(), Color.GRAY);
								newTrack.getHistory().add("Window Size = " + actionResult.getBinSize() + "bp, Precision = " + actionResult.getPrecision(), Color.GRAY);
								getTrackList().setTrack(index, newTrack, ConfigurationManager.getInstance().getTrackHeight(), selectedTrack.getName() + " - " + otherTrack.getName(), null);
							}
						}.execute();
					}
				}
			}
		}		
	}
}
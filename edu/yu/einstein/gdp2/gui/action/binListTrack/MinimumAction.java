/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.gui.action.binListTrack;

import java.awt.event.ActionEvent;

import javax.swing.ActionMap;
import javax.swing.JOptionPane;

import yu.einstein.gdp2.core.list.binList.BinList;
import yu.einstein.gdp2.core.list.binList.operation.BLOMin;
import yu.einstein.gdp2.core.list.binList.operation.BinListOperation;
import yu.einstein.gdp2.core.manager.ChromosomeManager;
import yu.einstein.gdp2.gui.action.TrackListAction;
import yu.einstein.gdp2.gui.dialog.ChromosomeChooser;
import yu.einstein.gdp2.gui.track.BinListTrack;
import yu.einstein.gdp2.gui.worker.actionWorker.ActionWorker;


/**
 * Shows the minimum score of the selected {@link BinListTrack}
 * @author Julien Lajugie
 * @version 0.1
 */
public final class MinimumAction extends TrackListAction {

	private static final long serialVersionUID = 3523404731226850786L;	// generated ID
	private static final String 	ACTION_NAME = "Minimum";			// action name
	private static final String 	DESCRIPTION = 
		"Show the minimum score of the selected track";					// tooltip


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = "minimum";


	/**
	 * Creates an instance of {@link MinimumAction}
	 */
	public MinimumAction() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}


	/**
	 * Shows the minimum score of the selected {@link BinListTrack}
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		final BinListTrack selectedTrack = (BinListTrack) getTrackList().getSelectedTrack();
		if (selectedTrack != null) {
			final boolean[] selectedChromo = ChromosomeChooser.getSelectedChromo(getRootPane(), ChromosomeManager.getInstance());
			if (selectedChromo != null) {
				final BinList binList = selectedTrack.getBinList();
				final BinListOperation<Double> operation = new BLOMin(binList, selectedChromo);
				// thread for the action
				new ActionWorker<Double>(getTrackList(), "Searching Minimum") {
					@Override
					protected Double doAction() throws Exception {
						return operation.compute();
					}
					@Override
					protected void doAtTheEnd(Double actionResult) {
						JOptionPane.showMessageDialog(getRootPane(), actionResult, "Minimum of \"" + selectedTrack.getName() +"\":", JOptionPane.INFORMATION_MESSAGE);
					}
				}.execute();
			}
		}
	}
}
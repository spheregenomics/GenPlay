/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.gui.action.binListTrack;

import java.awt.event.ActionEvent;

import javax.swing.ActionMap;

import yu.einstein.gdp2.core.list.binList.BinList;
import yu.einstein.gdp2.core.list.binList.operation.BLOGauss;
import yu.einstein.gdp2.core.list.binList.operation.BinListOperation;
import yu.einstein.gdp2.gui.action.TrackListAction;
import yu.einstein.gdp2.gui.dialog.GenomeWidthChooser;
import yu.einstein.gdp2.gui.track.BinListTrack;
import yu.einstein.gdp2.gui.worker.actionWorker.ActionWorker;


/**
 * Gausses the selected {@link BinListTrack}
 * @author Julien Lajugie
 * @version 0.1
 */
public final class GaussAction extends TrackListAction {

	private static final long serialVersionUID = -4566157311251154991L; // generated ID
	private static final String 	ACTION_NAME = "Gauss";				// action name
	private static final String 	DESCRIPTION = 
		"Apply a gaussian filter on the selected track";		 		// tooltip

	
	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = "gauss";


	/**
	 * Creates an instance of {@link GaussAction}
	 */
	public GaussAction() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}


	/**
	 * Gausses the selected {@link BinListTrack}
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		final BinListTrack selectedTrack = (BinListTrack) getTrackList().getSelectedTrack();
		if (selectedTrack != null) {
			final BinList binList = selectedTrack.getBinList();
			final int windowSize = binList.getBinSize();
			if(windowSize > 0) {
				final Integer sigma = GenomeWidthChooser.getSigma(getRootPane(), windowSize);
				if(sigma != null) {
					final BinListOperation<BinList> operation = new BLOGauss(binList, sigma);
					// thread for the action
					new ActionWorker<BinList>(getTrackList(), "Gaussing") {
						@Override
						protected BinList doAction() throws Exception {
							return operation.compute();
						}
						@Override
						protected void doAtTheEnd(BinList actionResult) {
							selectedTrack.setBinList(actionResult, operation.getDescription());
						}
					}.execute();
				}
			}
		}		
	}
}

/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.gui.action.emptyTrack;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.ActionMap;

import yu.einstein.gdp2.core.manager.ConfigurationManager;
import yu.einstein.gdp2.gui.action.TrackListAction;
import yu.einstein.gdp2.gui.track.BinListTrack;
import yu.einstein.gdp2.gui.trackList.TrackList;
import yu.einstein.gdp2.gui.worker.extractorWorker.BinListExtractorWorker;
import yu.einstein.gdp2.util.Utils;


/**
 * Loads a {@link BinListTrack} in the {@link TrackList}
 * @author Julien Lajugie
 * @version 0.1
 */
public final class LoadBinListTrackAction extends TrackListAction {

	private static final long serialVersionUID = -3974211916629578143L;	// generated ID
	private static final String ACTION_NAME = "Load Fixed Window Track"; // action name
	private static final String DESCRIPTION = "Load a track with a fixed window size"; // tooltip

	
	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = "loadBinList";


	/**
	 * Creates an instance of {@link LoadBinListTrackAction}
	 */
	public LoadBinListTrackAction() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
	}


	/**
	 * Loads a {@link BinListTrack} in the {@link TrackList}
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String defaultDirectory = ConfigurationManager.getInstance().getDefaultDirectory();
		String logFile = ConfigurationManager.getInstance().getLogFile();
		File selectedFile = Utils.chooseFileToLoad(getRootPane(), "Load Fixed Window Track", defaultDirectory, Utils.getReadableBinListFileFilters());
		if (selectedFile != null) {
			new BinListExtractorWorker(getTrackList(), logFile, selectedFile).execute();
		}
	}
}

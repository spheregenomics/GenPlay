/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.gui.action.allTrack;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.ActionMap;
import javax.swing.JFileChooser;

import yu.einstein.gdp2.core.manager.ConfigurationManager;
import yu.einstein.gdp2.gui.action.TrackListAction;
import yu.einstein.gdp2.gui.track.Track;
import yu.einstein.gdp2.gui.worker.actionWorker.ActionWorker;
import yu.einstein.gdp2.util.Utils;


/**
 * Saves the selected track as a JPG image
 * @author Julien Lajugie
 * @version 0.1
 */
public final class SaveAsImageAction extends TrackListAction {

	private static final long serialVersionUID = -4363481310731795005L; 				// generated ID
	private static final String ACTION_NAME = "Save as Image"; 							// action name
	private static final String DESCRIPTION = "Save the selected track as a JPG image"; // tooltip
	private static final int 	MNEMONIC = KeyEvent.VK_A; 								// mnemonic key

	
	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = "saveAsImage";


	/**
	 * Creates an instance of {@link SaveAsImageAction}
	 */
	public SaveAsImageAction() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
		putValue(MNEMONIC_KEY, MNEMONIC);
	}


	/**
	 * Saves the selected track as a JPG image
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		final Track selectedTrack = getTrackList().getSelectedTrack();
		if (selectedTrack != null) {
			final JFileChooser saveFC = new JFileChooser(ConfigurationManager.getInstance().getDefaultDirectory());
			saveFC.setFileSelectionMode(JFileChooser.FILES_ONLY);
			saveFC.setDialogTitle("Save track " + selectedTrack.getName() + " as a JPG image");
			saveFC.setSelectedFile(new File(".jpg"));
			final int returnVal = saveFC.showSaveDialog(getRootPane());
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				if (!Utils.cancelBecauseFileExist(getRootPane(), saveFC.getSelectedFile())) {
					// thread for the action
					new ActionWorker<Void>(getTrackList(), "Saving Track #" + selectedTrack.getTrackNumber() + " As Image") {
						@Override
						protected Void doAction() {
							selectedTrack.saveAsImage(Utils.addExtension(saveFC.getSelectedFile(), "jpg"));
							return null;
						}
						@Override
						protected void doAtTheEnd(Void actionResult) {}
					}.execute();
				}
			}
		}
	}
}

/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.gui.action.geneListTrack;

import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import yu.einstein.gdp2.core.Gene;
import yu.einstein.gdp2.core.GenomeWindow;
import yu.einstein.gdp2.gui.action.TrackListActionWorker;
import yu.einstein.gdp2.gui.track.GeneListTrack;


/**
 * Searches a gene on a {@link GeneListTrack}
 * @author Julien Lajugie
 * @version 0.1
 */
public final class GLASearchGene  extends TrackListActionWorker<Gene> {

	private static final long serialVersionUID = 2102571378866219218L; 	// generated ID
	private static final String 	ACTION_NAME = "Seach Gene";			// action name
	private static final String 	DESCRIPTION = 
		"Search a gene on the selected track"; 							// tooltip


	/**
	 * action accelerator {@link KeyStroke}
	 */
	public static final KeyStroke ACCELERATOR = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);


	/**
	 * key of the action in the {@link ActionMap}
	 */
	public static final String ACTION_KEY = "GLASearchGene";


	/**
	 * Creates an instance of {@link GLASearchGene}
	 */
	public GLASearchGene() {
		super();
		putValue(NAME, ACTION_NAME);
		putValue(ACTION_COMMAND_KEY, ACTION_KEY);
		putValue(SHORT_DESCRIPTION, DESCRIPTION);
		putValue(ACCELERATOR_KEY, ACCELERATOR);
	}


	@Override
	protected Gene processAction() throws Exception {
		if ((getTrackList().getSelectedTrack() != null) && (getTrackList().getSelectedTrack() instanceof GeneListTrack)) {
			GeneListTrack selectedTrack = (GeneListTrack) getTrackList().getSelectedTrack();
			if (selectedTrack != null) {
				String lastSearchedName = null;
				if (selectedTrack.getData().getLastSearchedGene() != null) {
					lastSearchedName = selectedTrack.getData().getLastSearchedGene().getName(); 
				}
				String geneName = (String) JOptionPane.showInputDialog(getRootPane(), "Enter the name of a gene", "Gene Search", JOptionPane.QUESTION_MESSAGE, null, null, lastSearchedName);
				if (geneName != null) {
					notifyActionStart("Searching Gene", 1, false);
					return selectedTrack.getData().search(geneName);
				}
			}
		}
		return null;
	}
	
	
	@Override
	protected void doAtTheEnd(Gene actionResult) {
		if (actionResult != null) {
			GenomeWindow newWindow = new GenomeWindow(actionResult.getChromo(), actionResult.getTxStart(), actionResult.getTxStop());
			getTrackList().setGenomeWindow(newWindow);
		}
	}
}

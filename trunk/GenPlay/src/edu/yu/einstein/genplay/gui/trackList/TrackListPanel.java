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
package edu.yu.einstein.genplay.gui.trackList;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.yu.einstein.genplay.core.manager.application.ConfigurationManager;
import edu.yu.einstein.genplay.exception.ExceptionManager;
import edu.yu.einstein.genplay.gui.action.track.TAPasteOrDrop;
import edu.yu.einstein.genplay.gui.action.track.TATrackSettings;
import edu.yu.einstein.genplay.gui.event.trackEvent.TrackEvent;
import edu.yu.einstein.genplay.gui.event.trackEvent.TrackEventType;
import edu.yu.einstein.genplay.gui.event.trackEvent.TrackListener;
import edu.yu.einstein.genplay.gui.mainFrame.MainFrame;
import edu.yu.einstein.genplay.gui.menu.TrackMenu;
import edu.yu.einstein.genplay.gui.track.Track;
import edu.yu.einstein.genplay.gui.track.TrackConstants;
import edu.yu.einstein.genplay.gui.track.layer.Layer;
import edu.yu.einstein.genplay.gui.track.layer.VersionedLayer;
import edu.yu.einstein.genplay.gui.track.layer.variantLayer.MultiGenomeDrawer;
import edu.yu.einstein.genplay.gui.track.layer.variantLayer.VariantLayer;
import edu.yu.einstein.genplay.gui.track.trackTransfer.TransferableTrack;
import edu.yu.einstein.genplay.util.Utils;


/**
 * Scroll panel showing a list of tracks
 * @author Julien Lajugie
 */
public class TrackListPanel extends JScrollPane implements Serializable, TrackListener, ListDataListener, DropTargetListener {

	private final static int 	SCROLL_BAR_BLOCK_INCREMENT = 40; 			// block increment for the scroll bar
	private final static int 	SCROLL_BAR_UNIT_INCREMENT = 15; 			// unit increment for the scroll bar
	private static final long 	serialVersionUID = -5070245121955382857L; 	// generated serial ID

	private transient Track 		draggedOverTrack = null;	// track rolled over by the dragged track, null if none
	private transient Track 		draggedTrack = null; 		// dragged track, null if none
	private transient Track 		selectedTrack = null; 		// track selected
	private final JPanel 			jpTrackList; 				// panel with the tracks
	private final TrackMenu 		trackMenu;					// menu for the track actions
	private TrackListModel 			model; 						// model handling the tracks list showed in this panel


	/**
	 * Creates an instance of {@link TrackListPanel}
	 * @param model {@link TrackListModel} handling the tracks showed in this panel
	 */
	public TrackListPanel(TrackListModel model) {
		super();
		this.model = model;
		this.model.addListDataListener(this);
		jpTrackList = new JPanel();

		setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));

		jpTrackList.setLayout(new BoxLayout(jpTrackList, BoxLayout.PAGE_AXIS));
		new DropTarget(jpTrackList, this);
		setActionMap(TrackListActionMap.getActionMap());
		setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, TrackListActionMap.getInputMap(this));
		trackMenu = new TrackMenu();
		getVerticalScrollBar().setUnitIncrement(SCROLL_BAR_UNIT_INCREMENT);
		getVerticalScrollBar().setBlockIncrement(SCROLL_BAR_BLOCK_INCREMENT);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		rebuildPanel();
	}


	@Override
	public void contentsChanged(ListDataEvent e) {
		rebuildPanel();
	}


	/**
	 * Cuts the selected track
	 */
	public void cutTrack() {
		if (selectedTrack != null) {
			try {
				getModel().deleteTrack(selectedTrack);
				selectedTrack = null;
			} catch (Exception e) {
				ExceptionManager.getInstance().caughtException(Thread.currentThread(), e, "Error while cutting the track");
			}
		}
	}


	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		if (!MainFrame.getInstance().isLocked() &&
				(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || dtde.isDataFlavorSupported(TransferableTrack.uriListFlavor))) {
			dtde.acceptDrag(TransferHandler.COPY);
			trackDragged();
		} else {
			dtde.acceptDrag(TransferHandler.NONE);
		}
	}


	@Override
	public void dragExit(DropTargetEvent dte) {
		if (draggedOverTrack != null) {
			draggedOverTrack.setBorder(TrackConstants.REGULAR_BORDER);
			draggedOverTrack = null;
			unlockTrackHandles();
		}
	}


	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		if (!MainFrame.getInstance().isLocked() &&
				(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || dtde.isDataFlavorSupported(TransferableTrack.uriListFlavor))) {
			trackDragged();
		}
	}


	@Override
	public void drop(DropTargetDropEvent dtde) {
		if (draggedOverTrack != null) {
			draggedOverTrack.setBorder(TrackConstants.REGULAR_BORDER);
			unlockTrackHandles();
			try {
				dtde.acceptDrop(TransferHandler.COPY);
				setSelectedTrack(draggedOverTrack);
				new TAPasteOrDrop(dtde.getTransferable()).processAction();
				dtde.dropComplete(true);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(getRootPane(), "The selected file cannot be loaded in GenPlay", "Cannot Drop File", JOptionPane.WARNING_MESSAGE, null);
				e.printStackTrace();
				dtde.dropComplete(false);
			}
			draggedOverTrack = null;
		}
	}


	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// do nothing
	}


	/**
	 * @return the {@link JPanel} displaying the tracks
	 */
	public Component getJpTrackList() {
		return jpTrackList;
	}


	/**
	 * @return the model containing the data displayed in this panel
	 */
	public TrackListModel getModel() {
		return model;
	}


	/**
	 * @return the selected track
	 */
	public Track getSelectedTrack() {
		return selectedTrack;
	}


	/**
	 * @param multiGenomeDrawer
	 * @return the track according to a {@link MultiGenomeDrawer}
	 */
	public Track getTrackFromGenomeDrawer(MultiGenomeDrawer multiGenomeDrawer) {
		Track[] trackList = getModel().getTracks();
		for (Track currentTrack: trackList) {
			Layer<?>[] layers = currentTrack.getLayers().getLayers();
			for (Layer<?> currentLayer: layers) {
				if (currentLayer instanceof VariantLayer) {
					if (((VariantLayer) currentLayer).getGenomeDrawer().equals(multiGenomeDrawer)) {
						return currentTrack;
					}
				}
			}
		}
		return null;
	}


	/**
	 * @return the track menu
	 */
	public TrackMenu getTrackMenu() {
		return trackMenu;
	}


	@Override
	public void intervalAdded(ListDataEvent e) {
		rebuildPanel();
	}


	@Override
	public void intervalRemoved(ListDataEvent e) {
		rebuildPanel();
	}


	/**
	 * @return true if there is a track to paste
	 */
	public boolean isPasteEnable() {
		Clipboard clipboard = Utils.getClipboard();
		if (clipboard.isDataFlavorAvailable(TransferableTrack.TRACK_FLAVOR)) {
			return true;
		}
		if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
			return true;
		}
		if (clipboard.isDataFlavorAvailable(TransferableTrack.uriListFlavor)) {
			return true;
		}
		return false;
	}


	/**
	 * Changes the legend display of the tracks
	 */
	public void legendChanged() {
		for (Track currentTrack: getModel().getTracks()) {
			currentTrack.repaint();
		}
	}


	/**
	 * Locks the handles of all the tracks
	 */
	public void lockTrackHandles() {
		for (Track currentTrack : getModel().getTracks()) {
			if (currentTrack != null) {
				currentTrack.lockHandle();
			}
		}
	}


	private void rebuildPanel() {
		jpTrackList.removeAll();
		Track[] trackList = getModel().getTracks();
		int selectedTrackIndex = getModel().indexOf(selectedTrack);
		for (int i = 0; i < trackList.length; i++) {
			trackList[i].setNumber(i + 1);
			jpTrackList.add(trackList[i].getTrackPanel());
			trackList[i].addTrackListener(this);
		}
		if (selectedTrackIndex == -1) {
			selectedTrack = null;
		} else {
			selectedTrack = getModel().getTrack(selectedTrackIndex);
		}
		jpTrackList.revalidate();
		setViewportView(jpTrackList);
	}


	/**
	 * Called when a track is released after being dragged in the list
	 */
	public void releaseTrack() {
		if ((draggedTrack != null) && (draggedOverTrack != null)) {
			draggedOverTrack.setBorder(TrackConstants.REGULAR_BORDER);
			if (getMousePosition() != null) {
				int insertIndex = getModel().indexOf(draggedOverTrack);
				getModel().deleteTrack(draggedTrack);
				getModel().insertTrack(insertIndex, draggedTrack);
			}
		}
		setSelectedTrack(draggedTrack);
		draggedTrack = null;
		draggedOverTrack = null;
		unlockTrackHandles();
	}


	/**
	 * Changes the reset layer function of the {@link VersionedLayer}.
	 */
	public void resetLayerChanged() {
		boolean hasToBeDisabled = !ConfigurationManager.getInstance().isResetTrack();
		if (hasToBeDisabled) {
			for (Track currentTrack: getModel().getTracks()) {
				for (Layer<?> currentLayer: currentTrack.getLayers()) {
					if (currentLayer instanceof VersionedLayer<?>) {
						((VersionedLayer<?>) currentLayer).deactivateReset();
					}
				}
			}
		}
	}


	/**
	 * Sets the model handling the data
	 * @param model
	 */
	public void setModel(TrackListModel model) {
		this.model = model;
		this.model.addListDataListener(this);
		rebuildPanel();
	}


	/**
	 * Sets the selected track in the track panel
	 * @param trackToSelect
	 */
	public void setSelectedTrack(Track trackToSelect) {
		if (trackToSelect != null) {
			// unselect the previous track
			if (selectedTrack != null) {
				selectedTrack.setSelected(false);
			}
			// select the dragged over track
			selectedTrack = trackToSelect;
			selectedTrack.setSelected(true);
		}
	}


	@Override
	public void trackChanged(TrackEvent evt) {
		if (evt.getEventType() == TrackEventType.POPUP_TRIGGERED) {
			selectedTrack = (Track) evt.getSource();
			trackMenu.setTrack(selectedTrack);
			Point mousePoint = getMousePosition();
			if (mousePoint != null) {
				trackMenu.show(this, mousePoint.x, mousePoint.y);
			}
		} else if (evt.getEventType() == TrackEventType.DRAGGED) {
			// set the dragged track
			if (draggedTrack == null) {
				draggedTrack = ((Track) evt.getSource());
			}
			trackDragged();
		} else if (evt.getEventType() == TrackEventType.RELEASED) {
			releaseTrack();
		} else if (evt.getEventType() == TrackEventType.SELECTED) {
			// unselect the previously selected track (if any)
			if ((selectedTrack != null) && (selectedTrack  != evt.getSource())){
				selectedTrack.setSelected(false);
			}
			// set the new selected track
			selectedTrack = (Track) evt.getSource();
		} else if (evt.getEventType() == TrackEventType.UNSELECTED) {
			selectedTrack = null;
		} else if (evt.getEventType() == TrackEventType.DOUBLE_CLICKED) {
			TATrackSettings trackSettings = new TATrackSettings();
			trackSettings.actionPerformed(null);
		}
	}


	/**
	 * Changes the number of {@link Track} in the {@link TrackListPanel} according to
	 * the value specified in the {@link ConfigurationManager}
	 */
	public void trackCountChanged() {
		int trackCount = ConfigurationManager.getInstance().getTrackCount();
		int preferredHeight = ConfigurationManager.getInstance().getTrackHeight();
		Track[] trackTmp = getModel().getTracks();
		Track[] trackList = new Track[trackCount];
		for (int i = 0; i < trackCount; i++) {
			if (i < trackTmp.length) {
				trackList[i] = trackTmp[i];
			} else {
				trackList[i] = new Track(i + 1);
				trackList[i].setPreferredHeight(preferredHeight);
			}
		}
		getModel().setTracks(trackList);
		rebuildPanel();
	}


	/**
	 * Called when a track is being dragged
	 */
	public void trackDragged() {
		lockTrackHandles();
		if (getMousePosition() != null) {
			for (Track currentTrack : getModel().getTracks()) {
				JPanel currentTrackPanel = currentTrack.getTrackPanel();
				int currentTrackTop = currentTrackPanel.getY() - getVerticalScrollBar().getValue();
				int currentrackBottom = (currentTrackPanel.getY() + currentTrackPanel.getHeight()) - getVerticalScrollBar().getValue();
				int mouseY = -1;
				try {
					mouseY = getMousePosition().y;
					if ((mouseY != -1) && (mouseY > currentTrackTop) && (mouseY < currentrackBottom) && (currentTrack != draggedOverTrack)) {
						if (draggedOverTrack != null) {
							draggedOverTrack.setBorder(TrackConstants.REGULAR_BORDER);
						}
						draggedOverTrack = currentTrack;
						if ((draggedTrack == null) || (draggedOverTrack == draggedTrack)) {
							draggedOverTrack.setBorder(TrackConstants.DRAG_START_BORDER);
						} else if (draggedOverTrack.getNumber() < draggedTrack.getNumber()) {
							draggedOverTrack.setBorder(TrackConstants.DRAG_UP_BORDER);
						} else {
							draggedOverTrack.setBorder(TrackConstants.DRAG_DOWN_BORDER);
						}
					}
				} catch (NullPointerException e) {
					// do nothing, it's because the getMousePosition return null
				}
			}
		}
	}


	/**
	 * Sets the height of each {@link Track} according to the value specified in the {@link ConfigurationManager}
	 */
	public void trackHeightChanged() {
		int preferredHeight = ConfigurationManager.getInstance().getTrackHeight();
		Track[] trackList = getModel().getTracks();
		for(int i = 0; i < trackList.length; i++) {
			trackList[i].setDefaultHeight(preferredHeight);
			trackList[i].setPreferredHeight(preferredHeight);
		}
		revalidate();
	}


	/**
	 * Changes the undo count of the undoable layers
	 */
	public void undoCountChanged() {
		int undoCount = ConfigurationManager.getInstance().getUndoCount();
		for (Track currentTrack: getModel().getTracks()) {
			for (Layer<?> currentLayer: currentTrack.getLayers()) {
				if (currentLayer instanceof VersionedLayer<?>) {
					((VersionedLayer<?>) currentLayer).setUndoCount(undoCount);
				}
			}
		}
	}


	/**
	 * Unlocks the handles of all the tracks
	 */
	public void unlockTrackHandles() {
		for (Track currentTrack : getModel().getTracks()) {
			if (currentTrack != null) {
				currentTrack.unlockHandle();
			}
		}
	}
}

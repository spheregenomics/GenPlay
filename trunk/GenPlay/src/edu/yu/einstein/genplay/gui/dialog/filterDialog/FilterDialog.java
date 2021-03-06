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
package edu.yu.einstein.genplay.gui.dialog.filterDialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import edu.yu.einstein.genplay.dataStructure.enums.FilterType;
import edu.yu.einstein.genplay.util.Images;
import edu.yu.einstein.genplay.util.Utils;



/**
 * A dialog window asking the user to choose a filter type its parameters
 * @author Julien Lajugie
 * @version 0.1
 */
public final class FilterDialog extends JDialog implements TreeSelectionListener {

	private static final long serialVersionUID = -4556158108666281869L;	// generated ID
	private static final Dimension 	FILTER_DIALOG_DIMENSION = new Dimension(600, 350); // dimension of this window
	private final JTree 			jt; 						// Tree
	private final JScrollPane 		jspTreeView; 				// Scroll pane containing the tree
	private final JPanel			jpFilter;					// right panel
	private final JButton 			jbOk; 						// Button OK
	private final JButton 			jbCancel; 					// Button cancel
	private final JSplitPane 		jspDivider; 				// Divider between the tree and the panel
	private int 					approved = CANCEL_OPTION;	// indicate if the user canceled or validated
	private static int 				selectionRow = 0;			// save the selected filter type

	/**
	 * Return value when OK has been clicked.
	 */
	public static final int APPROVE_OPTION = 0;


	/**
	 * Return value when Cancel has been clicked.
	 */
	public static final int CANCEL_OPTION = 1;


	/**
	 * Main method for the tests
	 * @param args
	 */
	public static void main(String[] args) {
		FilterDialog fd = new FilterDialog();
		if (fd.showFilterDialog(null) == FilterDialog.APPROVE_OPTION) {
			System.out.println("Filter type: " + fd.getFilterType());
			System.out.println("Min: " + fd.getMinInput().toString());
			System.out.println("Max: " + fd.getMaxInput().toString());
			System.out.println("Is Saturation: " + fd.isSaturation());
		}
		fd.dispose();
		FilterDialog fd2 = new FilterDialog();
		fd2.showFilterDialog(null);
		fd2.dispose();
	}


	/**
	 * Creates an instance of {@link FilterDialog}
	 */
	public FilterDialog() {
		super();
		// create the tree displayed in the JTree component
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Filters");
		createNodes(top);
		jt = new JTree(top);
		// hide the root node
		jt.setRootVisible(false);
		// hide the lines
		jt.setShowsRootHandles(true);
		// Remove the icon from the tree
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setLeafIcon(null);
		renderer.setClosedIcon(null);
		renderer.setOpenIcon(null);
		jt.setCellRenderer(renderer);
		jt.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jt.addTreeSelectionListener(this);
		// create the scroll pane containing the JTree
		jspTreeView = new JScrollPane(jt);
		jspTreeView.getVerticalScrollBar().setUnitIncrement(Utils.SCROLL_INCREMENT_UNIT);
		// create the right panel
		jpFilter = new JPanel();
		// add the scroll panes to a split pane.
		jspDivider = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		jspDivider.setLeftComponent(jspTreeView);
		jspDivider.setRightComponent(jpFilter);
		jspDivider.setContinuousLayout(true);
		Dimension minimumSize = new Dimension(100, 1);
		jspTreeView.setMinimumSize(minimumSize);
		jpFilter.setMinimumSize(minimumSize);
		jspDivider.setDividerLocation(FILTER_DIALOG_DIMENSION.width / 4);
		// create the OK button
		jbOk = new JButton("OK");
		jbOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt.getLastSelectedPathComponent();
				FilterPanel filterPanel = (FilterPanel) node.getUserObject();
				// check first if the input is valid
				boolean isInputValid = filterPanel.isInputValid();
				// close the window if the input is valid
				if (isInputValid) {
					// save the input so it becomes the default values next time the window is opened
					filterPanel.saveMax();
					filterPanel.saveMin();
					filterPanel.saveIsSaturation();
					// save the filter type selected
					selectionRow = jt.getLeadSelectionRow();
					// close the window
					approved = APPROVE_OPTION;
					setVisible(false);
				}
			}
		});
		// create the cancel button
		jbCancel = new JButton("Cancel");
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		// we want the size of the two buttons to be equal
		jbOk.setPreferredSize(jbCancel.getPreferredSize());

		// add the components
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0.99;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		add(jspDivider, c);

		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 0.99;
		c.weighty = 0.01;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		add(jbOk, c);

		c.weightx = 0.01;
		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		add(jbCancel, c);

		jt.setSelectionRow(selectionRow);
		setTitle("Filter");
		setIconImages(Images.getApplicationImages());
		setSize(FILTER_DIALOG_DIMENSION);
		setResizable(false);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setLocationRelativeTo(getRootPane());
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		getRootPane().setDefaultButton(jbOk);
	}


	/**
	 * Creates the data of the tree.
	 * @param top Root DefaultMutableTreeNode of the tree.
	 */
	private void createNodes(DefaultMutableTreeNode top) {
		DefaultMutableTreeNode category = null;

		category = new DefaultMutableTreeNode(new PercentagePanel());
		top.add(category);

		category = new DefaultMutableTreeNode(new ThresholdPanel());
		top.add(category);

		category = new DefaultMutableTreeNode(new BandStopPanel());
		top.add(category);

		category = new DefaultMutableTreeNode(new CountPanel());
		top.add(category);
	}


	/**
	 * @return the type of filter selected
	 */
	public FilterType getFilterType() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt.getLastSelectedPathComponent();
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof PercentagePanel) {
			return FilterType.PERCENTAGE;
		} else if (nodeInfo instanceof ThresholdPanel) {
			return FilterType.THRESHOLD;
		} else if (nodeInfo instanceof BandStopPanel) {
			return FilterType.BANDSTOP;
		} else if (nodeInfo instanceof CountPanel) {
			return FilterType.COUNT;
		} else {
			return null;
		}
	}


	/**
	 * @return the maximum parameter for the filter. Can be a double or an integer depending on the filter type
	 */
	public Number getMaxInput() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt.getLastSelectedPathComponent();
		FilterPanel filterPanel = (FilterPanel) node.getUserObject();
		return filterPanel.getMaxInput();
	}


	/**
	 * @return the minimum parameter for the filter. Can be a double or an integer depending on the filter type
	 */
	public Number getMinInput() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt.getLastSelectedPathComponent();
		FilterPanel filterPanel = (FilterPanel) node.getUserObject();
		return filterPanel.getMinInput();
	}


	/**
	 * @return true if the saturation option is selected. False if the remove option is selected
	 */
	public boolean isSaturation() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt.getLastSelectedPathComponent();
		FilterPanel filterPanel = (FilterPanel) node.getUserObject();
		return filterPanel.isSaturation();
	}


	/**
	 * Shows the component.
	 * @param parent the parent component of the dialog, can be null; see showDialog for details
	 * @return APPROVE_OPTION is OK is clicked. CANCEL_OPTION otherwise.
	 */
	public int showFilterDialog(Component parent) {
		setLocationRelativeTo(parent);
		setVisible(true);
		return approved;
	}


	/**
	 * Changes the panel displayed when the node of the tree changes.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt.getLastSelectedPathComponent();
		jpFilter.removeAll();
		if ((node != null) && (node.isLeaf())) {
			Object nodeInfo = node.getUserObject();
			if (nodeInfo != null) {
				jpFilter.add((JPanel) nodeInfo);
			}
		}
		jpFilter.revalidate();
		jpFilter.repaint();
	}
}

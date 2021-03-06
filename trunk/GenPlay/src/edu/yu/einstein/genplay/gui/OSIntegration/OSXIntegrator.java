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
package edu.yu.einstein.genplay.gui.OSIntegration;

import java.awt.Image;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.FullScreenUtilities;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

import edu.yu.einstein.genplay.core.IO.extractor.ExtractorFactory;
import edu.yu.einstein.genplay.core.manager.MGFiltersManager;
import edu.yu.einstein.genplay.gui.action.project.PAAbout;
import edu.yu.einstein.genplay.gui.action.track.TAAddGenPlayTrack;
import edu.yu.einstein.genplay.gui.action.track.TAInsert;
import edu.yu.einstein.genplay.gui.fileFilter.GenPlayTrackFilter;
import edu.yu.einstein.genplay.gui.mainFrame.MainFrame;
import edu.yu.einstein.genplay.gui.trackList.TrackListPanel;
import edu.yu.einstein.genplay.util.Images;
import edu.yu.einstein.genplay.util.Utils;

/**
 * Handle OSX specific events such the opening of a GenPlay project file in Mac OSX when the user
 * launched GenPlay by double clicking on a project file, or menus in the main menu bar
 * @author Julien Lajugie
 */
public class OSXIntegrator implements AboutHandler, PreferencesHandler, QuitHandler, OpenFilesHandler {

	/**
	 * Integrate the GenPlay app into the OSX environment
	 */
	public static void integrateApplication() {
		// set the menu bar for OSX
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		// set the application name for OSX
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GenPlay");
		Application macApplication = Application.getApplication();
		macApplication.setOpenFileHandler(getInstance());
		// if it's not a mac install with a icns resource we need to set the
		// icon image manually
		if (!Utils.isMacInstall()) {
			List<Image> appImages = Images.getApplicationImages();
			macApplication.setDockIconImage(appImages.get(appImages.size() - 1));
		}
	}


	/**
	 * Integrate GenPlay {@link MainFrame} window into the OSX environment
	 * @param mainFrame GenPlay main frame
	 */
	public static void integrateMainFrame(MainFrame mainFrame) {
		// add a menu bar for OSX
		Application macApplication = Application.getApplication();
		try {
			macApplication.setDefaultMenuBar(mainFrame.getJMenuBar());
		} catch (IllegalStateException e) {}// case where the menu bar is not suported by the look and feel
		macApplication.setAboutHandler(getInstance());
		macApplication.setPreferencesHandler(getInstance());
		macApplication.setQuitHandler(getInstance());
		FullScreenUtilities.setWindowCanFullScreen(mainFrame, true);
	}


	/**
	 * Toggles the full screen mode of GenPlay {@link MainFrame}
	 */
	public static void toggleMainFrameFullScreen() {
		if (MainFrame.isInitialized()) {
			Application.getApplication().requestToggleFullScreen(MainFrame.getInstance());
		}
	}


	/**
	 * GenPlay project file to open in Mac OSX if the user
	 * launched GenPlay by double clicking on a project file
	 */
	private File fileToOpen = null;


	/**
	 * Instance of the singleton {@link OSXIntegrator}
	 */
	private static OSXIntegrator instance = null;


	/**
	 * @return an instance of a {@link OSXIntegrator}.
	 * Makes sure that there is only one unique instance as specified in the singleton pattern
	 */
	public static OSXIntegrator getInstance() {
		if (instance == null) {
			synchronized(MGFiltersManager.class) {
				if (instance == null) {
					instance = new OSXIntegrator();
				}
			}
		}
		return instance;
	}


	/**
	 * Private constructor, creates an instance of {@link OSXIntegrator}
	 */
	private OSXIntegrator() {
		super();
	}



	/**
	 * @return the GenPlay project file to open in Mac OSX if the user
	 * launched GenPlay by double clicking on a project file
	 */
	public File getFileToOpen() {
		return fileToOpen;
	}


	@Override
	public void handleAbout(AboutEvent arg0) {
		JFrame jf = MainFrame.getInstance();
		new PAAbout(jf).actionPerformed(null);
	}


	@Override
	public void handlePreferences(PreferencesEvent preferencesEvent) {
		MainFrame.getInstance().showOption();
	}


	@Override
	public void handleQuitRequestWith(QuitEvent quitEvent, QuitResponse quitResponse) {
		JFrame jf = MainFrame.getInstance();
		int res = JOptionPane.showConfirmDialog(jf.getRootPane(), "Exit GenPlay?", "Confirm Exit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
		if (res == JOptionPane.CANCEL_OPTION) {
			quitResponse.cancelQuit();
		} else {
			quitResponse.performQuit();
		}
	}


	@Override
	public void openFiles(OpenFilesEvent openFilesEvent) {
		// case where a file is clicked when the program is loaded
		if (MainFrame.isInitialized()) {
			for (File fileToOpen: openFilesEvent.getFiles()) {
				// we try to open the selected files
				if (Utils.getExtension(fileToOpen).equals(GenPlayTrackFilter.EXTENSIONS[0])) {
					try {
						TrackListPanel trackListPanel =	MainFrame.getInstance().getTrackListPanel();
						trackListPanel.setSelectedTrack(trackListPanel.getModel().getTrack(0));
						new TAInsert().actionPerformed(null);
						trackListPanel.setSelectedTrack(trackListPanel.getModel().getTrack(0));
						new TAAddGenPlayTrack(ExtractorFactory.getExtractor(fileToOpen)).actionPerformed(null);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(MainFrame.getInstance().getRootPane(), "The selected file cannot be loaded", "Cannot Load File", JOptionPane.WARNING_MESSAGE, null);
					}
				}
			}
		} else {
			// case where the program is started by clicking a file associated with genplay
			fileToOpen = openFilesEvent.getFiles().get(0);
		}
	}
}

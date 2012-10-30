/*******************************************************************************
 *     GenPlay, Einstein Genome Analyzer
 *     Copyright (C) 2009, 2011 Albert Einstein College of Medicine
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *     Authors:	Julien Lajugie <julien.lajugie@einstein.yu.edu>
 *     			Nicolas Fourel <nicolas.fourel@einstein.yu.edu>
 *     Website: <http://genplay.einstein.yu.edu>
 *******************************************************************************/
package edu.yu.einstein.genplay.gui.track.drawer.multiGenome;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.yu.einstein.genplay.core.GenomeWindow;
import edu.yu.einstein.genplay.core.chromosome.Chromosome;
import edu.yu.einstein.genplay.core.comparator.ListComparator;
import edu.yu.einstein.genplay.core.enums.AlleleType;
import edu.yu.einstein.genplay.core.enums.VariantType;
import edu.yu.einstein.genplay.core.manager.project.ProjectManager;
import edu.yu.einstein.genplay.core.manager.project.ProjectWindow;
import edu.yu.einstein.genplay.core.multiGenome.VCF.VCFStatistics.VCFFileStatistics;
import edu.yu.einstein.genplay.core.multiGenome.display.DisplayableVariantListMaker;
import edu.yu.einstein.genplay.core.multiGenome.display.MGVariantListForDisplay;
import edu.yu.einstein.genplay.core.multiGenome.display.variant.VariantDisplay;
import edu.yu.einstein.genplay.core.multiGenome.filter.MGFilter;
import edu.yu.einstein.genplay.gui.MGDisplaySettings.MGDisplaySettings;
import edu.yu.einstein.genplay.gui.dialog.multiGenomeDialog.properties.editing.variants.VariantData;
import edu.yu.einstein.genplay.gui.dialog.multiGenomeDialog.toolTipStripe.ToolTipStripeDialog;
import edu.yu.einstein.genplay.gui.track.TrackGraphics;

/**
 * The multi genome drawer is in charge of drawing variation stripes for multi genome project.
 * A track can display stripes of one or two alleles. If the display for both allele is required, the track is then horizontally split.
 * The list makers are in charge to create the list of variant.
 * Drawing a stripe consist on:
 * - drawing the stripe from the specific height and width, genome positions must be translated to the screen positions
 * - drawing the letters (nucleotide) for each stripe when they are known
 * - changing the stripe display when the mouse goes over a stripe
 * - displaying an information dialog about a variant when the user clicks on it.
 * 
 * @author Nicolas Fourel
 * @version 0.1
 */
public class MultiGenomeDrawer implements Serializable {

	/** Generated serial version ID */
	private static final long serialVersionUID = 2329957235585775255L;
	private static final int  SAVED_FORMAT_VERSION_NUMBER = 0;				// saved format version

	private ProjectWindow					projectWindow;					// instance of the genome window manager
	private VCFFileStatistics				statistics;

	private DisplayableVariantListMaker		allele01VariantListMaker;		// displayable variants list creator (for MG project)
	private DisplayableVariantListMaker		allele02VariantListMaker;		// displayable variants list creator (for MG project)

	private List<MGFilter>					mgFiltersList;					// list of filters that will apply rules of filtering
	private List<VariantData>				variantDataList;					// list of stripes to apply to this track (for MG project)

	private VariantDisplay 					variantUnderMouse = null;		// Special display when the mouse is over a variant stripe
	private boolean							locked;

	private List<ToolTipStripeDialog> stripesDialogs;

	private MultiGenomeDensityDrawer densityDrawer;
	private MultiGenomeVariantDrawer variantDrawer;
	private Chromosome chromosome;


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);
		out.writeObject(allele01VariantListMaker);
		out.writeObject(allele02VariantListMaker);
		out.writeObject(statistics);
		out.writeObject(densityDrawer);
		out.writeObject(variantDrawer);
		out.writeObject(chromosome);
	}


	/**
	 * Method used for unserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();
		allele01VariantListMaker = (DisplayableVariantListMaker) in.readObject();
		allele02VariantListMaker = (DisplayableVariantListMaker) in.readObject();
		statistics = (VCFFileStatistics) in.readObject();
		densityDrawer = (MultiGenomeDensityDrawer) in.readObject();
		variantDrawer = (MultiGenomeVariantDrawer) in.readObject();
		chromosome = (Chromosome) in.readObject();

		densityDrawer.setDrawer(this);
		variantDrawer.setDrawer(this);
		projectWindow = ProjectManager.getInstance().getProjectWindow();
		locked = false;
		stripesDialogs = new ArrayList<ToolTipStripeDialog>();
		variantDataList = null;
		mgFiltersList = null;
	}


	/**
	 * Constructor of {@link MultiGenomeDrawer}
	 */
	public MultiGenomeDrawer () {
		projectWindow = ProjectManager.getInstance().getProjectWindow();
		if (ProjectManager.getInstance().isMultiGenomeProject()) {
			allele01VariantListMaker = new DisplayableVariantListMaker(projectWindow.getGenomeWindow(), projectWindow.getXFactor());
			allele02VariantListMaker = new DisplayableVariantListMaker(projectWindow.getGenomeWindow(), projectWindow.getXFactor());
		} else {
			allele01VariantListMaker = null;
			allele02VariantListMaker = null;
		}
		densityDrawer = new MultiGenomeDensityDrawer(this);
		variantDrawer = new MultiGenomeVariantDrawer(this);
		variantDataList = null;
		mgFiltersList = null;
		statistics = null;
		locked = false;
		stripesDialogs = new ArrayList<ToolTipStripeDialog>();
		chromosome = getCurrentChromosome();
	}


	/**
	 * @return the current chromosome
	 */
	private Chromosome getCurrentChromosome () {
		return ProjectManager.getInstance().getProjectChromosome().getCurrentChromosome();
	}


	/**
	 * Reset the list of the variant list makers
	 */
	public void resetVariantListMaker () {
		if (ProjectManager.getInstance().isMultiGenomeProject()) {
			if (allele01VariantListMaker != null) {
				//allele01VariantListMaker.resetList();
			}
			if (allele02VariantListMaker != null) {
				//allele02VariantListMaker.resetList();
			}
		}
	}


	/**
	 * Compare given multi genome information with the current ones.
	 * @param stripesList	the new stripes list
	 * @param filtersList	the new filters list
	 * @return	true if new information are different than the current ones
	 */
	public boolean hasMultiGenomeInformationChanged (List<VariantData> stripesList, List<MGFilter> filtersList) {
		if (haveVariantsChanged(stripesList) || haveFiltersChanged(filtersList)) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * Compare given variant information with the current one.
	 * @param variantDataList	the new stripes list
	 * @return	true if new information are different than the current ones
	 */
	private boolean haveVariantsChanged (List<VariantData> variantDataList) {
		ListComparator<VariantData> stripesComparator = new ListComparator<VariantData>();
		return stripesComparator.areDifferent(this.variantDataList, variantDataList);
	}


	/**
	 * Compare given filters information with the current one.
	 * @param filtersList	the new filters list
	 * @return	true if new information are different than the current ones
	 */
	private boolean haveFiltersChanged (List<MGFilter> filtersList) {
		ListComparator<MGFilter> filtersComparator = new ListComparator<MGFilter>();
		return filtersComparator.areDifferent(this.mgFiltersList, filtersList);
	}


	/**
	 * Compare the current chromosome and the chromosome used for the stripes/filter
	 * @return	true if the chromosome has changed, false otherwise
	 */
	private boolean hasChromosomeChanged () {
		return !chromosome.equals(getCurrentChromosome());
	}


	/**
	 * Updates information for multi genome project.
	 * These information are about:
	 * - stripes
	 * - filters
	 * @param variantDataList list of stripes
	 * @param filtersList list of filters
	 */
	public void updateMultiGenomeInformation (List<VariantData> variantDataList, List<MGFilter> filtersList) {
		if (hasChromosomeChanged() || hasMultiGenomeInformationChanged(variantDataList, filtersList)) {
			chromosome = getCurrentChromosome();
			this.statistics = null;
			killStripesDialogs();
			this.variantDataList = variantDataList;
			this.mgFiltersList = filtersList;

			List<MGVariantListForDisplay> allele01VariantLists = new ArrayList<MGVariantListForDisplay>();		// initializes a temporary list of variant for the first allele
			List<MGVariantListForDisplay> allele02VariantLists = new ArrayList<MGVariantListForDisplay>();		// initializes a temporary list of variant for the second allele

			for (VariantData data: variantDataList) {							// scans all stripes data
				// Checks wich alleles must be processed
				AlleleType alleleType = data.getAlleleType();				// get the allele type defined for the current stripe data
				boolean allele01 = false;									// initializes a boolean in order to know if we need to process data for the first allele
				boolean allele02 = false;									// initializes a boolean in order to know if we need to process data for the second allele
				if (alleleType == AlleleType.BOTH) {						// if the defined allele type is BOTH, both allele must be processed
					allele01 = true;
					allele02 = true;
				} else if (alleleType == AlleleType.ALLELE01) {				// if the defined allele type is ALLELE01, only the first allele will be processed
					allele01 = true;
				} else if (alleleType == AlleleType.ALLELE02) {				// if the defined allele type is ALLELE02, only the second allele will be processed
					allele02 = true;
				}

				// Gathers variant that have been found
				if (allele01) {												// if the first allele must be processed
					List<MGVariantListForDisplay> listOfVariantListTmp = data.getListOfVariantList(AlleleType.ALLELE01); 	// we ask the stripe data object the list of variant for this allele
					for (MGVariantListForDisplay currentVariantList: listOfVariantListTmp) {								// we add all the variants to the global temporary list for the first allele
						allele01VariantLists.add(currentVariantList);
					}
				}

				if (allele02) {												// if the second allele must be processed
					List<MGVariantListForDisplay> listOfVariantListTmp = data.getListOfVariantList(AlleleType.ALLELE02); 	// we ask the stripe data object the list of variant for this allele
					for (MGVariantListForDisplay currentVariantList: listOfVariantListTmp) {								// we add all the variants to the global temporary list for the second allele
						allele02VariantLists.add(currentVariantList);
					}
				}
			}

			// Sets the list maker with the new list of variant
			boolean showReference = MGDisplaySettings.getInstance().includeReferences();
			boolean showFiltered = MGDisplaySettings.DRAW_FILTERED_VARIANT == MGDisplaySettings.YES_MG_OPTION;
			allele01VariantListMaker.setListOfVariantList(allele01VariantLists, filtersList, showReference, showFiltered);	// we set the list maker with the temporary list
			allele02VariantListMaker.setListOfVariantList(allele02VariantLists, filtersList, showReference, showFiltered);	// we set the list maker with the temporary list

			//densityDrawer.updateDensityList(getFullVariantList());
			//repaint();
		}
	}


	///////////////////////////////////////////////////////////////// Stripes drawing

	/**
	 * This method locks the painting of the multi genome information.
	 * 
	 * The lock system for painting multi genome information prevent multi access to a same file.
	 */
	public void lockPainting () {
		locked = true;
	}


	/**
	 * This method unlocks the painting of the multi genome information.
	 * 
	 * The lock system for painting multi genome information prevent multi access to a same file.
	 */
	public void unlockPainting () {
		locked = false;
	}


	/**
	 * Draws stripes showing information for multi genome.
	 * The method checks if the track must show both allele or only one, in order to split the track or not.
	 * @param g				graphics object
	 * @param genomeWindow 	the genome window
	 * @param xFactor 		the x factor
	 */
	public void drawMultiGenomeInformation(Graphics g, GenomeWindow genomeWindow, double xFactor) {
		if ((variantDataList != null) && (variantDataList.size() > 0)) {
			variantDrawer.initializeStripesOpacity();
			/*if (genomeWindow.getSize() > 1000000) {
				stripesDrawer.drawMultiGenomeMask(g, "Multi genome information cannot be displayed at this zoom level.");
			} else {*/
			if (!locked) {														// if there are stripes
				AlleleType trackAlleleType = getTrackAlleleType();																// get the allele type of the track
				if (trackAlleleType == AlleleType.BOTH) {																		// if both allele must be displayed
					int halfHeight = g.getClipBounds().height / 2;																// calculates the half of the height track
					Graphics allele01Graphic = g.create(0, 0, g.getClipBounds().width, halfHeight);								// create a graphics for the first allele that correspond to the upper half of the track
					Graphics2D allele02Graphic = (Graphics2D) g.create(0, halfHeight, g.getClipBounds().width, halfHeight);		// create a 2D graphics for the second allele that correspond to the lower half of the track
					allele02Graphic.scale(1, -1);																				// all Y axis (vertical) coordinates must be reversed for the second allele
					allele02Graphic.translate(0, -allele02Graphic.getClipBounds().height - 1);									// translates all coordinates of the graphic for the second allele
					variantDrawer.setCurrentAllele(AlleleType.ALLELE01);
					variantDrawer.drawGenome(allele01Graphic, genomeWindow, allele01VariantListMaker.getDisplayPolicy(), allele01VariantListMaker.getFittedData(genomeWindow, xFactor)); 	// draw the stripes for the first allele
					variantDrawer.setCurrentAllele(AlleleType.ALLELE02);
					variantDrawer.drawGenome(allele02Graphic, genomeWindow, allele02VariantListMaker.getDisplayPolicy(), allele02VariantListMaker.getFittedData(genomeWindow, xFactor));	// draw the stripes for the second allele
					variantDrawer.drawMultiGenomeLine(g);																						// draw a line in the middle of the track to distinguish upper and lower half.
				} else if (trackAlleleType == AlleleType.ALLELE01) {															// if the first allele only must be displayed
					variantDrawer.drawGenome(g, genomeWindow, allele01VariantListMaker.getDisplayPolicy(), allele01VariantListMaker.getFittedData(genomeWindow, xFactor));					// draw its stripes
				} else if(trackAlleleType == AlleleType.ALLELE02) {																// if the second allele only must be displayed
					variantDrawer.drawGenome(g, genomeWindow, allele02VariantListMaker.getDisplayPolicy(), allele02VariantListMaker.getFittedData(genomeWindow, xFactor));					// draw its stripes
				}
			} else {
				variantDrawer.drawMultiGenomeMask(g, "Multi genome display interupted while loading information.");
			}
			//}
		}
	}


	/////////////////////////////////////////////////////////////////

	/**
	 * Display the content of a variant in the tool tip stripe dialog
	 * @param trackHeight
	 * @param e mouse event
	 */
	public void toolTipStripe (int trackHeight, MouseEvent e) {
		if (ProjectManager.getInstance().isMultiGenomeProject()) {												// we must be in a multi genome project
			double pos = projectWindow.screenXPosToGenomePos(TrackGraphics.getTrackGraphicsWidth(), e.getX());	// we translate the position on the screen into a position on the genome
			VariantDisplay variant = getDisplayableVariant(trackHeight, pos, e.getY());							// we get the variant (Y is needed to know if the variant is on the upper or lower half of the track)
			if (variant != null) {																				// if a variant has been found
				AlleleType trackAlleleType = getTrackAlleleType();												// we get the allele type of the track
				List<VariantDisplay> variantList = null;														// we will try to get the full list of variant displayed on the whole track (we want to move from a variant to another one no matter the allele)
				if (trackAlleleType == AlleleType.BOTH) {														// if both allele are displayed,
					variantList = mergeVariantList(allele01VariantListMaker.getVariantList(), allele02VariantListMaker.getVariantList());
				} else if (trackAlleleType == AlleleType.ALLELE01) {											// if the first allele only is displayed
					variantList = getCopyOfVariantList(allele01VariantListMaker.getVariantList());				// we get the copy of its list
				} else if (trackAlleleType == AlleleType.ALLELE02) {											// if the second allele only is displayed
					variantList = getCopyOfVariantList(allele02VariantListMaker.getVariantList());				// we get the copy of its list
				}
				ToolTipStripeDialog toolTip = new ToolTipStripeDialog(this);									// we create the information dialog
				stripesDialogs.add(toolTip);
				toolTip.show(variantList, variant, e.getXOnScreen(), e.getYOnScreen());						// we show it
			}
		}
	}


	/**
	 * Checks if the mouse is over a variant and says whether the track graphics must repaint or not
	 * @param trackHeight
	 * @param e the mouse event
	 * @return	true if the track must be repainted, false otherwise
	 */
	public boolean isOverVariant (int trackHeight, MouseEvent e) {
		if (ProjectManager.getInstance().isMultiGenomeProject() && (allele01VariantListMaker != null) && (allele02VariantListMaker != null)) {	// if we are in multi genome project
			double pos = projectWindow.screenXPosToGenomePos(TrackGraphics.getTrackGraphicsWidth(), e.getX());	// we translate the position on the screen into a position on the genome
			VariantDisplay variant = getDisplayableVariant(trackHeight, pos, e.getY());							// we get the variant (Y is needed to know if the variant is on the upper or lower half of the track)
			if (variant != null) {																				// if a variant has been found
				variantUnderMouse = variant;																	// the mouse is on this variant (we save it)
				return true;																					// we return true
			} else if (variantUnderMouse != null) {																// no variant has been found but one was already defined (the mouse is just getting out of the stripe)
				variantUnderMouse = null;																		// there is no variant under the mouse anymore
				return true;
			}
		}
		return false;
	}


	/**
	 * Checks if the track has to be repaint when the mouse exit from it.
	 * Basically, it has to be repaint if a variant was under the mouse in order to not highlight it.
	 * @return true if the track has to be repaint, false otherwise
	 */
	public boolean hasToBeRepaintAfterExit () {
		if (variantUnderMouse != null) {
			variantUnderMouse = null;
			return true;
		}
		return false;
	}


	private List<VariantDisplay> mergeVariantList (List<VariantDisplay> variantList01, List<VariantDisplay> variantList02) {
		List<VariantDisplay> newList = new ArrayList<VariantDisplay>();
		int index01 = 0;
		int index02 = 0;
		int size01 = variantList01.size();
		int size02 = variantList02.size();

		while ((index01 < size01) && (index02 < size02)) {
			VariantDisplay variant01 = variantList01.get(index01);
			VariantDisplay variant02 = variantList01.get(index02);
			int start01 = variant01.getStart();
			int start02 = variant02.getStart();
			if (start01 < start02) {
				newList.add(variant01);
				index01++;
			} else if (start02 < start01) {
				newList.add(variant02);
				index02++;
			} else {
				boolean dominant01 = variant01.isDominant(variant02);
				boolean dominant02 = variant02.isDominant(variant01);
				if (dominant01 && !dominant02) {
					newList.add(variant01);
				} else if (!dominant01 && dominant02) {
					newList.add(variant02);
				} else if (dominant01 && dominant02) {
					if (variant01.equals(variant02)) {
						newList.add(variant01);
					} else {
						newList.add(variant01);
						newList.add(variant02);
					}
				} else {
					System.out.println("MultiGenomeDrawer.mergeVariantList() no dominant variant, needs debugging");
				}
				index01++;
				index02++;
			}
		}

		if (index01 < size01) {
			for (int i = index01; i < size01; i++) {
				newList.add(variantList01.get(index01));
			}
		}

		if (index02 < size02) {
			for (int i = index02; i < size02; i++) {
				newList.add(variantList02.get(index02));
			}
		}

		return newList;
	}


	/**
	 * Creates a copy of a variant list
	 * @param variantList variant list to copy
	 * @return copy of the variant list given in parameter
	 */
	private List<VariantDisplay> getCopyOfVariantList (List<VariantDisplay> variantList) {
		List<VariantDisplay> copy = new ArrayList<VariantDisplay>();
		for (VariantDisplay currentVariant: variantList) {
			copy.add(currentVariant);
		}
		return copy;
	}

	/////////////////////////////////////////////////////////////////


	/**
	 * @return the stripesList
	 */
	public List<VariantData> getStripesList() {
		return variantDataList;
	}


	/**
	 * @return the vcfFiltersList
	 */
	public List<MGFilter> getFiltersList() {
		return mgFiltersList;
	}


	/**
	 * @param mgFiltersList the mgFiltersList to set
	 */
	public void setFiltersList(List<MGFilter> mgFiltersList) {
		this.mgFiltersList = mgFiltersList;
	}


	/**
	 * @param variantDataList the variantDataList to set
	 */
	public void setVariantDataList(List<VariantData> variantDataList) {
		this.variantDataList = variantDataList;
	}


	/**
	 * @return the list of required genomes for multi genome process
	 */
	public List<String> getRequiredGenomesList () {
		List<String> list = new ArrayList<String>();
		if (variantDataList != null) {
			for (VariantData data: variantDataList) {
				if (!list.contains(data.getGenome())) {
					list.add(data.getGenome());
				}
			}
		}
		return list;
	}


	/**
	 * @param genome	the genome name
	 * @return the map variant type/color defined for a genome
	 */
	public Map<VariantType, Color> getVariantColorMap (String genome) {
		Map<VariantType, Color> colors = new HashMap<VariantType, Color>();
		if (variantDataList != null) {
			for (VariantData data: variantDataList) {
				if (data.getGenome().equals(genome)) {
					for (int i = 0; i < data.getVariationTypeList().size(); i++) {
						colors.put(data.getVariationTypeList().get(i), data.getColorList().get(i));
					}
				}
			}
		}
		return colors;
	}


	/**
	 * @param trackHeight the track graphics height
	 * @param x position on the meta genome
	 * @param y y position on the track
	 * @return	the variant associated to the position if exists, null otherwise.
	 */
	public VariantDisplay getDisplayableVariant(int trackHeight, double x, double y) {
		List<VariantDisplay> variantList = null;					// instantiate a list that will lead to the right variant list
		AlleleType trackAlleleType = getTrackAlleleType();			// we get the allele type of the track
		if (trackAlleleType != null) {
			if (trackAlleleType == AlleleType.BOTH) {					// if both allele are displayed, we must distinguish on which allele the variant is
				if (y <= (trackHeight / 2)) {				// if Y is less than the half of the track height
					variantList = allele01VariantListMaker.getFittedData(projectWindow.getGenomeWindow(), projectWindow.getXFactor());	// we have to look on the first allele list
				} else {												// if Y is more than the half of the track height
					variantList = allele02VariantListMaker.getFittedData(projectWindow.getGenomeWindow(), projectWindow.getXFactor());	// we have to look on the second allele list
				}
			} else if (trackAlleleType == AlleleType.ALLELE01) {		// if the first allele only is displayed
				variantList = allele01VariantListMaker.getFittedData(projectWindow.getGenomeWindow(), projectWindow.getXFactor());	// we look on the first allele list
			} else if (trackAlleleType == AlleleType.ALLELE02) {		// if the second allele only is displayed
				variantList = allele02VariantListMaker.getFittedData(projectWindow.getGenomeWindow(), projectWindow.getXFactor());	// we look on the second allele list
			}
		}

		VariantDisplay variant = null;
		if (variantList != null) {									// if a variant list has been found
			for (VariantDisplay current: variantList) {			// we scan all of its variant
				if (current.getType() == VariantType.SNPS) {		// special case for SNP
					if (x == current.getStart()) {					// a SNP is defined for a start but return the stop as start + 1. However, is present on the start position only, it does not include the stop!
						return current;
					}
				} else {
					if ((x >= current.getStart()) && (x <= current.getStop())) {	// if X is included in a variant,
						return current;											// we have found it!
					}
				}
			}
		}
		return variant;												// we return the variant (or null if it has not been found)
	}


	/**
	 * @return the allele type defined for the track
	 */
	public AlleleType getTrackAlleleType () {
		if (variantDataList != null) {
			boolean allele01 = false;
			boolean allele02 = false;
			for (VariantData stripe: variantDataList) {
				AlleleType type = stripe.getAlleleType();
				if (type == AlleleType.BOTH) {					// if both allele are required
					return AlleleType.BOTH;
				} else if (type == AlleleType.ALLELE01) {		// if the first allele only is required
					allele01 = true;
				} else if (type == AlleleType.ALLELE02) {		// if the second allele only is required
					allele02 = true;
				}
			}

			if (allele01 & allele02) {
				return AlleleType.BOTH;
			} else if (allele01) {
				return AlleleType.ALLELE01;
			} else if (allele02) {
				return AlleleType.ALLELE02;
			}
		}
		return null;
	}


	private void killStripesDialogs () {
		for (ToolTipStripeDialog dialog: stripesDialogs) {
			dialog.dispose();
		}
		stripesDialogs = new ArrayList<ToolTipStripeDialog>();
	}


	/**
	 * @return the statistics
	 */
	public VCFFileStatistics getStatistics() {
		return statistics;
	}


	/**
	 * @param statistics the statistics to set
	 */
	public void setStatistics(VCFFileStatistics statistics) {
		this.statistics = statistics;
	}


	/**
	 * @return the variantUnderMouse
	 */
	protected VariantDisplay getVariantUnderMouse() {
		return variantUnderMouse;
	}

}
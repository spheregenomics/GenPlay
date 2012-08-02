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
package edu.yu.einstein.genplay.core.multiGenome.export.BEDExport;

import edu.yu.einstein.genplay.core.chromosome.Chromosome;
import edu.yu.einstein.genplay.core.enums.AlleleType;
import edu.yu.einstein.genplay.core.enums.CoordinateSystemType;
import edu.yu.einstein.genplay.core.multiGenome.VCF.VCFLine;
import edu.yu.einstein.genplay.core.multiGenome.utils.ShiftCompute;


/**
 * @author Nicolas Fourel
 * @version 0.1
 */
public class AlleleSettingsBed {

	protected final AlleleType allele;
	protected final CoordinateSystemType coordinateSystem;

	protected int		charIndex;
	protected int		currentOffset;
	protected int		currentLength;
	protected int		currentStart;
	protected int		currentStop;
	protected int		currentAltIndex;


	/**
	 * Constructor of {@link AlleleSettingsBed}
	 * @param path
	 * @param allele
	 */
	protected AlleleSettingsBed (AlleleType allele, CoordinateSystemType coordinateSystem) {
		this.allele = allele;
		this.coordinateSystem = coordinateSystem;

		// initialize parameters
		currentOffset = 0;
		if (allele.equals(AlleleType.ALLELE01)) {
			charIndex = 0;
		} else if (allele.equals(AlleleType.ALLELE02)) {
			charIndex = 2;
		}
	}


	/**
	 * Initializes the current information about:
	 * - start and stop position
	 * - length
	 * - alternative index
	 * @param chromosome 	the current chromosome
	 * @param lengths		lengths of variations in the line
	 * @param currentLine	the current line
	 * @param altIndex		the index of the alternative
	 */
	public void initializeCurrentInformation (Chromosome chromosome, int[] lengths, VCFLine currentLine, int altIndex) {
		if (coordinateSystem == CoordinateSystemType.METAGENOME) {
			initializeCurrentInformationForMetaGenome(chromosome, lengths, currentLine, altIndex);
		} else if (coordinateSystem == CoordinateSystemType.REFERENCE) {
			initializeCurrentInformationForReferenceGenome(lengths, currentLine, altIndex);
		} else if (coordinateSystem == CoordinateSystemType.CURRENT_GENOME) {
			initializeCurrentInformationForGenome(lengths, currentLine, altIndex);
		}
	}


	/**
	 * Initializes the current information about:
	 * - start and stop position (on the current genome)
	 * - length
	 * - alternative index
	 * @param lengths		lengths of variations in the line
	 * @param currentLine	the current line
	 * @param altIndex		the index of the alternative
	 */
	protected void initializeCurrentInformationForGenome (int[] lengths, VCFLine currentLine, int altIndex) {
		currentAltIndex = altIndex;
		if (currentAltIndex == -1) {
			currentLength = 0;
			currentStart = currentLine.getReferencePosition() + currentOffset;
			currentStop = currentStart + 1;
		} else if (currentAltIndex > -1) {
			currentLength = lengths[currentAltIndex];
			if (currentLength > 0) {
				currentStart = currentLine.getReferencePosition() + currentOffset;
				currentStop = (currentStart + currentLength) + 1;
			} else if (currentLength < 0) {
				currentStart = currentLine.getReferencePosition() + currentOffset;
				currentStop = currentStart + 1;
			} else {
				currentStart = currentLine.getReferencePosition() + currentOffset;
				currentStop = currentStart + 1;
			}
			currentOffset += currentLength;
		} else {
			currentLength = 0;
			currentStart = -1;
			currentStop = -1;
		}
	}


	/**
	 * Initializes the current information about:
	 * - start and stop position (on the reference genome)
	 * - length
	 * - alternative index
	 * @param lengths		lengths of variations in the line
	 * @param currentLine	the current line
	 * @param altIndex		the index of the alternative
	 */
	protected void initializeCurrentInformationForReferenceGenome (int[] lengths, VCFLine currentLine, int altIndex) {
		currentAltIndex = altIndex;
		if (currentAltIndex == -1) {
			currentLength = 0;
			currentStart = currentLine.getReferencePosition();
			currentStop = currentStart + 1;
		} else if (currentAltIndex > -1) {
			currentLength = lengths[currentAltIndex];
			if (currentLength > 0) {
				currentStart = currentLine.getReferencePosition();
				currentStop = currentStart + 1;
			} else if (currentLength < 0) {
				currentStart = currentLine.getReferencePosition();
				currentStop = currentStart + (currentLength * -1) + 1;
			} else {
				currentStart = currentLine.getReferencePosition();
				currentStop = currentStart + 1;
			}
		} else {
			currentLength = 0;
			currentStart = -1;
			currentStop = -1;
		}
	}


	/**
	 * Initializes the current information about:
	 * - start and stop position (on the meta genome)
	 * - length
	 * - alternative index
	 * @param chromosome 	the current chromosome
	 * @param lengths		lengths of variations in the line
	 * @param currentLine	the current line
	 * @param altIndex		the index of the alternative
	 */
	protected void initializeCurrentInformationForMetaGenome (Chromosome chromosome, int[] lengths, VCFLine currentLine, int altIndex) {
		initializeCurrentInformationForReferenceGenome(lengths, currentLine, altIndex);
		currentStart = ShiftCompute.computeShiftForReferenceGenome(chromosome, currentStart);
		currentStop = ShiftCompute.computeShiftForReferenceGenome(chromosome, currentStop);
	}


	/**
	 * Updates the current information using information from the other allele.
	 * e.g.: with a 0/1 genotype, information in the 0 allele has to be updated with information from the 1 allele
	 * @param allele the other allele
	 */
	public void updateCurrentInformation (AlleleSettingsBed allele) {
		if (isReference() && allele.isAlternative()) {
			currentLength = allele.getCurrentLength();
			if (coordinateSystem != CoordinateSystemType.CURRENT_GENOME) {
				if (currentLength < 0) {
					currentStop += currentLength * -1;
				}
			}
		}
	}


	/**
	 * @return the current and usable start position
	 */
	public int getCurrentStart () {
		if (coordinateSystem == CoordinateSystemType.METAGENOME) {
			return getCurrentStartForMetaGenome();
		} else if (coordinateSystem == CoordinateSystemType.REFERENCE) {
			return getCurrentStartForReferenceGenome();
		} else if (coordinateSystem == CoordinateSystemType.CURRENT_GENOME) {
			return getCurrentStartForCurrentGenome();
		}
		return -1;
	}


	/**
	 * @return the current start for writing purpose (BED rules) and for the current genome
	 */
	private int getCurrentStartForCurrentGenome () {
		if (isAlternative() && (currentLength > 0)) {
			return currentStart + 1;
		}
		return currentStart;
	}


	/**
	 * @return the current start for writing purpose (BED rules) and for the reference genome
	 */
	private int getCurrentStartForReferenceGenome () {
		if (currentLength < 0) {
			return currentStart + 1;
		}
		return currentStart;
	}


	/**
	 * @return the current start for writing purpose (BED rules) and for the meta genome
	 */
	private int getCurrentStartForMetaGenome () {
		if (currentLength == 0) {
			return currentStart;
		}
		return currentStart + 1;
	}


	/**
	 * @return the current and usable stop position
	 */
	public int getCurrentStop () {
		return currentStop;
	}


	/**
	 * @return the displayable current stop
	 */
	public int getDisplayableCurrentStop () {
		if (coordinateSystem == CoordinateSystemType.METAGENOME) {
			return getDisplayableCurrentStopForMetaGenome();
		} else if (coordinateSystem == CoordinateSystemType.REFERENCE) {
			return getDisplayableCurrentStopForReferenceGenome();
		} else if (coordinateSystem == CoordinateSystemType.CURRENT_GENOME) {
			return getDisplayableCurrentStopForCurrentGenome();
		}
		return -1;
	}


	/**
	 * @return the current displayable stop for the current genome
	 */
	private int getDisplayableCurrentStopForCurrentGenome () {
		if (currentLength > 0) {
			return currentStop - 1;
		}
		return currentStop;
	}


	/**
	 * @return the current displayable stop for the reference genome
	 */
	private int getDisplayableCurrentStopForReferenceGenome () {
		if (currentLength < 0) {
			return currentStop - 1;
		}
		return currentStop;
	}


	/**
	 * @return the current displayable stop for the meta genome
	 */
	private int getDisplayableCurrentStopForMetaGenome () {
		return currentStop;
	}


	/**
	 * @return true if the current information refers to the reference, false otherwise
	 */
	public boolean isReference () {
		if (currentAltIndex == -1) {
			return true;
		}
		return false;
	}


	/**
	 * @return true if the current information refers to known variation (reference/alternatives), false otherwise (if '.')
	 */
	public boolean isKnown () {
		if (currentAltIndex > -2) {
			return true;
		}
		return false;
	}


	/**
	 * @return true if the current information refers to an alternative, false otherwise
	 */
	public boolean isAlternative () {
		if (currentAltIndex > -1) {
			return true;
		}
		return false;
	}


	/**
	 * @return the offset
	 */
	public int getOffset() {
		return currentOffset;
	}


	/**
	 * @param offset the offset to set
	 */
	public void addOffset(int offset) {
		this.currentOffset += offset;
	}


	/**
	 * @return the allele
	 */
	public AlleleType getAllele() {
		return allele;
	}


	/**
	 * @return the charIndex
	 */
	public int getCharIndex() {
		return charIndex;
	}


	/**
	 * @return the currentLength
	 */
	public int getCurrentLength() {
		return currentLength;
	}


	/**
	 * @return the currentAltIndex
	 */
	public int getCurrentAltIndex() {
		return currentAltIndex;
	}


	/**
	 * @return the raw currentStart
	 */
	public int getNativeCurrentStart() {
		return currentStart;
	}


	/**
	 * @return the raw currentStop
	 */
	public int getNativeCurrentStop() {
		return currentStop;
	}


	/**
	 * @return the coordinateSystem
	 */
	public CoordinateSystemType getCoordinateSystem() {
		return coordinateSystem;
	}

}

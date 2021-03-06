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
package edu.yu.einstein.genplay.core.multiGenome.VCF.VCFStatistics;

/**
 * @author Nicolas Fourel
 * @version 0.1
 */
public interface VCFSampleStatistics extends VCFStatistics {

	/**
	 * increment the numberOfSNPs
	 */
	public void incrementNumberOfSNPs();


	/**
	 * increment the numberOfShortInsertions
	 */
	public void incrementNumberOfShortInsertions();


	/**
	 * increment the numberOfLongInsertions
	 */
	public void incrementNumberOfLongInsertions();


	/**
	 * increment the numberOfShortDeletions
	 */
	public void incrementNumberOfShortDeletions();


	/**
	 * increment the numberOfLongDeletions
	 */
	public void incrementNumberOfLongDeletions();


	/**
	 * increment the numberOfHomozygoteSNPs
	 */
	public void incrementNumberOfHomozygoteSNPs();


	/**
	 * increment the numberOfHomozygoteInsertions
	 */
	public void incrementNumberOfHomozygoteInsertions();


	/**
	 * increment the numberOfHomozygoteDeletions
	 */
	public void incrementNumberOfHomozygoteDeletions();


	/**
	 * increment the numberOfHeterozygoteSNPs
	 */
	public void incrementNumberOfHeterozygoteSNPs();


	/**
	 * increment the numberOfHeterozygoteInsertions
	 */
	public void incrementNumberOfHeterozygoteInsertions();


	/**
	 * increment the numberOfHeterozygoteDeletions
	 */
	public void incrementNumberOfHeterozygoteDeletions();


	/**
	 * increment the numberOfHemizygoteSNPs
	 */
	public void incrementNumberOfHemizygoteSNPs();


	/**
	 * increment the numberOfHemizygoteInsertions
	 */
	public void incrementNumberOfHemizygoteInsertions();


	/**
	 * increment the numberOfHemizygoteDeletions
	 */
	public void incrementNumberOfHemizygoteDeletions();

}

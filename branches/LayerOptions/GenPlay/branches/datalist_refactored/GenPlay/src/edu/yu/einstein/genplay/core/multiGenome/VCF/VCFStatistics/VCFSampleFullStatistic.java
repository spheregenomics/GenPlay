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
package edu.yu.einstein.genplay.core.multiGenome.VCF.VCFStatistics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * @author Nicolas Fourel
 * @version 0.1
 */
public class VCFSampleFullStatistic implements Serializable, VCFSampleStatistics {

	/** Default generated serial version ID */
	private static final long serialVersionUID = -1037070449560631967L;
	private static final int  SAVED_FORMAT_VERSION_NUMBER = 0;			// saved format version

	// Number of lines and columns
	private static final int LINE_NUMBER				= 18;		// Number of lines in the data object
	private static final int COLUMN_NUMBER				= 4;		// Number of columns in the data object

	// Column indexes
	private static final int SECTION_INDEX				= 0;		// Index for the section column
	private static final int NUMBER_INDEX 				= 1;		// Index for the number column
	private static final int PERCENTAGE_SECTION_INDEX 	= 2;		// Index for the section percentage column
	private static final int PERCENTAGE_TOTAL_INDEX 	= 3;		// Index for the total percentage column

	// Line indexes
	private static final int VARIATION_INDEX 					= 0;
	private static final int SNP_VARIATION_INDEX 				= 1;
	private static final int INSERTION_VARIATION_INDEX 			= 2;
	private static final int INSERTION_INDEL_INDEX 				= 3;
	private static final int INSERTION_SV_INDEX 				= 4;
	private static final int DELETION_VARIATION_INDEX 			= 5;
	private static final int DELETION_INDEL_INDEX 				= 6;
	private static final int DELETION_SV_INDEX 					= 7;
	private static final int GENOTYPE_INDEX 					= 8;
	private static final int SNP_GENOTYPE_INDEX 				= 9;
	private static final int SNP_HOMOZYGOTE_INDEX 				= 10;
	private static final int SNP_HETEROZYGOTE_INDEX 			= 11;
	private static final int INSERTION_GENOTYPE_INDEX 			= 12;
	private static final int INSERTION_HOMOZYGOTE_INDEX 		= 13;
	private static final int INSERTION_HETEROZYGOTE_INDEX 		= 14;
	private static final int DELETION_GENOTYPE_INDEX 			= 15;
	private static final int DELETION_HOMOZYGOTE_INDEX 			= 16;
	private static final int DELETION_HETEROZYGOTE_INDEX 		= 17;

	// Column names
	private static final String SECTION_NAME				= "Sections";					// Name for the section column
	private static final String NUMBER_NAME 				= "Number";						// Name for the number column
	private static final String PERCENTAGE_SECTION_NAME 	= "% on the section type";		// Name for the section percentage column
	private static final String PERCENTAGE_TOTAL_NAME 		= "% on the whole genome";		// Name for the total percentage column

	// Line names
	private static final String VARIATION_TITLE						= "Number of variations";
	private static final String GENOTYPE_TITLE						= "Genotype variations";
	private static final String SNP_LABEL							= "   SNP";
	private static final String INSERTION_LABEL						= "   Insertion";
	private static final String DELETION_LABEL						= "   Deletion";
	private static final String HOMOZYGOTE_LABEL					= "      Homozygote";
	private static final String HETEROZYGOTE_LABEL					= "      Heterozygote";
	private static final String INDEL_LABEL							= "      Indel";
	private static final String SV_LABEL							= "      SV";

	private Object[][] data;

	private int numberOfSNPs;
	private int numberOfShortInsertions;
	private int numberOfLongInsertions;
	private int numberOfShortDeletions;
	private int numberOfLongDeletions;

	private int numberOfHomozygoteSNPs;
	private int numberOfHomozygoteInsertions;
	private int numberOfHomozygoteDeletions;
	private int numberOfHeterozygoteSNPs;
	private int numberOfHeterozygoteInsertions;
	private int numberOfHeterozygoteDeletions;


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);

		out.writeObject(data);
	}


	/**
	 * Method used for unserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();

		data = (Object[][]) in.readObject();
	}


	/**
	 * Constructor of {@link VCFSampleFullStatistic}
	 */
	protected VCFSampleFullStatistic () {
		numberOfSNPs = 0;
		numberOfShortInsertions = 0;
		numberOfLongInsertions = 0;
		numberOfShortDeletions = 0;
		numberOfLongDeletions = 0;

		numberOfHomozygoteSNPs = 0;
		numberOfHomozygoteInsertions = 0;
		numberOfHomozygoteDeletions = 0;
		numberOfHeterozygoteSNPs = 0;
		numberOfHeterozygoteInsertions = 0;
		numberOfHeterozygoteDeletions = 0;
	}


	@Override
	public String[] getColumnNamesForData () {
		String[] columnNames = {SECTION_NAME, NUMBER_NAME, PERCENTAGE_SECTION_NAME, PERCENTAGE_TOTAL_NAME};
		return columnNames;
	}


	@Override
	public void processStatistics () {
		if (data == null) {

			data = new Object[LINE_NUMBER][COLUMN_NUMBER];

			data[VARIATION_INDEX][SECTION_INDEX] = VARIATION_TITLE;
			data[SNP_VARIATION_INDEX][SECTION_INDEX] = SNP_LABEL;
			data[INSERTION_VARIATION_INDEX][SECTION_INDEX] = INSERTION_LABEL;
			data[INSERTION_INDEL_INDEX][SECTION_INDEX] = INDEL_LABEL;
			data[INSERTION_SV_INDEX][SECTION_INDEX] = SV_LABEL;
			data[DELETION_VARIATION_INDEX][SECTION_INDEX] = DELETION_LABEL;
			data[DELETION_INDEL_INDEX][SECTION_INDEX] = INDEL_LABEL;
			data[DELETION_SV_INDEX][SECTION_INDEX] = SV_LABEL;
			data[GENOTYPE_INDEX][SECTION_INDEX] = GENOTYPE_TITLE;
			data[SNP_GENOTYPE_INDEX][SECTION_INDEX] = SNP_LABEL;
			data[SNP_HETEROZYGOTE_INDEX][SECTION_INDEX] = HETEROZYGOTE_LABEL;
			data[SNP_HOMOZYGOTE_INDEX][SECTION_INDEX] = HOMOZYGOTE_LABEL;
			data[INSERTION_GENOTYPE_INDEX][SECTION_INDEX] = INSERTION_LABEL;
			data[INSERTION_HETEROZYGOTE_INDEX][SECTION_INDEX] = HETEROZYGOTE_LABEL;
			data[INSERTION_HOMOZYGOTE_INDEX][SECTION_INDEX] = HOMOZYGOTE_LABEL;
			data[DELETION_GENOTYPE_INDEX][SECTION_INDEX] = DELETION_LABEL;
			data[DELETION_HETEROZYGOTE_INDEX][SECTION_INDEX] = HETEROZYGOTE_LABEL;
			data[DELETION_HOMOZYGOTE_INDEX][SECTION_INDEX] = HOMOZYGOTE_LABEL;


			int totalSNP = numberOfSNPs;
			int totalInsertion = numberOfShortInsertions + numberOfLongInsertions;
			int totalDeletion = numberOfShortDeletions + numberOfLongDeletions;
			int totalVariation = totalSNP + totalInsertion + totalDeletion;

			int totalGTSNP = numberOfHeterozygoteSNPs + numberOfHomozygoteSNPs;
			int totalGTInsertion = numberOfHeterozygoteInsertions + numberOfHomozygoteInsertions;
			int totalGTDeletion = numberOfHeterozygoteDeletions + numberOfHomozygoteDeletions;
			int totalGT = totalGTSNP + totalGTInsertion + totalGTDeletion;


			data[VARIATION_INDEX][NUMBER_INDEX] = totalVariation;
			data[SNP_VARIATION_INDEX][NUMBER_INDEX] = totalSNP;
			data[INSERTION_VARIATION_INDEX][NUMBER_INDEX] = totalInsertion;
			data[INSERTION_INDEL_INDEX][NUMBER_INDEX] = numberOfShortInsertions;
			data[INSERTION_SV_INDEX][NUMBER_INDEX] = numberOfLongInsertions;
			data[DELETION_VARIATION_INDEX][NUMBER_INDEX] = totalDeletion;
			data[DELETION_INDEL_INDEX][NUMBER_INDEX] = numberOfShortDeletions;
			data[DELETION_SV_INDEX][NUMBER_INDEX] = numberOfLongDeletions;
			data[GENOTYPE_INDEX][NUMBER_INDEX] = totalGT;
			data[SNP_GENOTYPE_INDEX][NUMBER_INDEX] = totalGTSNP;
			data[SNP_HETEROZYGOTE_INDEX][NUMBER_INDEX] = numberOfHeterozygoteSNPs;
			data[SNP_HOMOZYGOTE_INDEX][NUMBER_INDEX] = numberOfHomozygoteSNPs;
			data[INSERTION_GENOTYPE_INDEX][NUMBER_INDEX] = totalGTInsertion;
			data[INSERTION_HETEROZYGOTE_INDEX][NUMBER_INDEX] = numberOfHeterozygoteInsertions;
			data[INSERTION_HOMOZYGOTE_INDEX][NUMBER_INDEX] = numberOfHomozygoteInsertions;
			data[DELETION_GENOTYPE_INDEX][NUMBER_INDEX] = totalGTDeletion;
			data[DELETION_HETEROZYGOTE_INDEX][NUMBER_INDEX] = numberOfHeterozygoteDeletions;
			data[DELETION_HOMOZYGOTE_INDEX][NUMBER_INDEX] = numberOfHomozygoteDeletions;


			data[VARIATION_INDEX][PERCENTAGE_SECTION_INDEX] = "100";
			data[SNP_VARIATION_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(SNP_VARIATION_INDEX), totalVariation);
			data[INSERTION_VARIATION_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(INSERTION_VARIATION_INDEX), totalVariation);
			data[INSERTION_INDEL_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(INSERTION_INDEL_INDEX), totalInsertion);
			data[INSERTION_SV_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(INSERTION_SV_INDEX), totalInsertion);
			data[DELETION_VARIATION_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(DELETION_VARIATION_INDEX), totalVariation);
			data[DELETION_INDEL_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(DELETION_INDEL_INDEX), totalDeletion);
			data[DELETION_SV_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(DELETION_SV_INDEX), totalDeletion);
			data[GENOTYPE_INDEX][PERCENTAGE_SECTION_INDEX] = "100";
			data[SNP_GENOTYPE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(SNP_GENOTYPE_INDEX), totalGT);
			data[SNP_HETEROZYGOTE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(SNP_HETEROZYGOTE_INDEX), totalGTSNP);
			data[SNP_HOMOZYGOTE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(SNP_HOMOZYGOTE_INDEX), totalGTSNP);
			data[INSERTION_GENOTYPE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(INSERTION_GENOTYPE_INDEX), totalGT);
			data[INSERTION_HETEROZYGOTE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(INSERTION_HETEROZYGOTE_INDEX), totalGTInsertion);
			data[INSERTION_HOMOZYGOTE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(INSERTION_HOMOZYGOTE_INDEX), totalGTInsertion);
			data[DELETION_GENOTYPE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(DELETION_GENOTYPE_INDEX), totalGT);
			data[DELETION_HETEROZYGOTE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(DELETION_HETEROZYGOTE_INDEX), totalGTDeletion);
			data[DELETION_HOMOZYGOTE_INDEX][PERCENTAGE_SECTION_INDEX] = getPercentage(getDataInt(DELETION_HOMOZYGOTE_INDEX), totalGTDeletion);


			data[VARIATION_INDEX][PERCENTAGE_TOTAL_INDEX] = "100";
			data[SNP_VARIATION_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(SNP_VARIATION_INDEX), totalVariation);
			data[INSERTION_VARIATION_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(INSERTION_VARIATION_INDEX), totalVariation);
			data[INSERTION_INDEL_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(INSERTION_INDEL_INDEX), totalVariation);
			data[INSERTION_SV_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(INSERTION_SV_INDEX), totalVariation);
			data[DELETION_VARIATION_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(DELETION_VARIATION_INDEX), totalVariation);
			data[DELETION_INDEL_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(DELETION_INDEL_INDEX), totalVariation);
			data[DELETION_SV_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(DELETION_SV_INDEX), totalVariation);
			data[GENOTYPE_INDEX][PERCENTAGE_TOTAL_INDEX] = "100";
			data[SNP_GENOTYPE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(SNP_GENOTYPE_INDEX), totalGT);
			data[SNP_HETEROZYGOTE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(SNP_HETEROZYGOTE_INDEX), totalGT);
			data[SNP_HOMOZYGOTE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(SNP_HOMOZYGOTE_INDEX), totalGT);
			data[INSERTION_GENOTYPE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(INSERTION_GENOTYPE_INDEX), totalGT);
			data[INSERTION_HETEROZYGOTE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(INSERTION_HETEROZYGOTE_INDEX), totalGT);
			data[INSERTION_HOMOZYGOTE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(INSERTION_HOMOZYGOTE_INDEX), totalGT);
			data[DELETION_GENOTYPE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(DELETION_GENOTYPE_INDEX), totalGT);
			data[DELETION_HETEROZYGOTE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(DELETION_HETEROZYGOTE_INDEX), totalGT);
			data[DELETION_HOMOZYGOTE_INDEX][PERCENTAGE_TOTAL_INDEX] = getPercentage(getDataInt(DELETION_HOMOZYGOTE_INDEX), totalGT);
		}
	}


	/**
	 * @param value	the value
	 * @param total	the total
	 * @return		the percentage between the value and its total, 0 otherwise
	 */
	private int getPercentage (int value, int total) {
		int result = 0;
		if ((total == 0) && (value == total)) {
			result = 100;
		} else {
			try {
				result = (value * 100) / total;
			} catch (Exception e) {}
		}
		return result;
	}


	/**
	 * @param indexLine index of a line
	 * @return			the integer located in the column containing the number, -1 otherwise
	 */
	@Override
	public int getDataInt (int indexLine) {
		return getDataInt(indexLine, NUMBER_INDEX);
	}


	/**
	 * @param indexLine		index of a line
	 * @param indexColumn	index of a column
	 * @return				the associated integer, -1 otherwise
	 */
	private int getDataInt (int indexLine, int indexColumn) {
		int result = -1;
		try {
			result = Integer.parseInt(data[indexLine][indexColumn].toString());
		} catch (Exception e) {}

		return result;
	}


	@Override
	public Object[][] getData() {
		return data;
	}


	@Override
	public void incrementNumberOfSNPs() {
		this.numberOfSNPs++;
	}


	@Override
	public void incrementNumberOfShortInsertions() {
		this.numberOfShortInsertions++;
	}


	@Override
	public void incrementNumberOfLongInsertions() {
		this.numberOfLongInsertions++;
	}


	@Override
	public void incrementNumberOfShortDeletions() {
		this.numberOfShortDeletions++;
	}


	@Override
	public void incrementNumberOfLongDeletions() {
		this.numberOfLongDeletions++;
	}


	@Override
	public void incrementNumberOfHomozygoteSNPs() {
		this.numberOfHomozygoteSNPs++;
	}


	@Override
	public void incrementNumberOfHomozygoteInsertions() {
		this.numberOfHomozygoteInsertions++;
	}


	@Override
	public void incrementNumberOfHomozygoteDeletions() {
		this.numberOfHomozygoteDeletions++;
	}


	@Override
	public void incrementNumberOfHeterozygoteSNPs() {
		this.numberOfHeterozygoteSNPs++;
	}


	@Override
	public void incrementNumberOfHeterozygoteInsertions() {
		this.numberOfHeterozygoteInsertions++;
	}


	@Override
	public void incrementNumberOfHeterozygoteDeletions() {
		this.numberOfHeterozygoteDeletions++;
	}


	@Override
	public void show () {
		String info = "";
		info += SECTION_NAME + "\t" + NUMBER_NAME + "\t" + PERCENTAGE_SECTION_NAME +  "\t" + PERCENTAGE_TOTAL_NAME + "\n";
		for (int i = 0; i < LINE_NUMBER; i++) {
			for (int j = 0; j < COLUMN_NUMBER; j++) {
				info += data[i][j];
				if (j < (COLUMN_NUMBER - 1)) {
					info += "\t";
				}
			}
			info += "\n";
		}
		System.out.println(info);
	}


	@Override
	public String getString () {
		String info = "";
		info += SECTION_NAME + "\t" + NUMBER_NAME + "\t" + PERCENTAGE_SECTION_NAME +  "\t" + PERCENTAGE_TOTAL_NAME + "\n";
		for (int i = 0; i < LINE_NUMBER; i++) {
			for (int j = 0; j < COLUMN_NUMBER; j++) {
				info += data[i][j];
				if (j < (COLUMN_NUMBER - 1)) {
					info += "\t";
				}
			}
			if (i < (LINE_NUMBER - 1)) {
				info += "\n";
			}
		}
		return info;
	}


	@Override
	public String getFullString() {
		return "";
	}

}
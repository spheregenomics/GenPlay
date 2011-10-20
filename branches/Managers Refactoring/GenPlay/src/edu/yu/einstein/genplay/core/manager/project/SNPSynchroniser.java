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
package edu.yu.einstein.genplay.core.manager.project;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.yu.einstein.genplay.core.chromosome.Chromosome;
import edu.yu.einstein.genplay.core.enums.VCFType;
import edu.yu.einstein.genplay.core.enums.VariantType;
import edu.yu.einstein.genplay.core.multiGenome.VCF.VCFReader;
import edu.yu.einstein.genplay.core.multiGenome.VCF.VCFFileType.VCFSNP;
import edu.yu.einstein.genplay.core.multiGenome.engine.MGChromosome;
import edu.yu.einstein.genplay.core.multiGenome.engine.MGGenome;
import edu.yu.einstein.genplay.core.multiGenome.engine.MGMultiGenome;
import edu.yu.einstein.genplay.core.multiGenome.engine.MGPosition;
import edu.yu.einstein.genplay.core.multiGenome.engine.Variant;
import edu.yu.einstein.genplay.core.multiGenome.stripeManagement.MultiGenomeStripes;
import edu.yu.einstein.genplay.core.multiGenome.utils.FormattedMultiGenomeName;

/**
 * SNPs can be enabled or disabled according to a genome.
 * When SNPs are enabled for a genome, SNP variants are added to {@link MGMultiGenome} lists.
 * When SNPs are disabled for a genome, SNP variants are deleted from {@link MGMultiGenome} lists.
 * Those modifications are made according to the chromosomes present in {@link MGMultiGenome}.
 * It means it is sensitive to the CHROMOSOME_LOADING_OPTION.
 * 
 * @author Nicolas Fourel
 * @version 0.1
 */
public class SNPSynchroniser implements Serializable {

	private static final long serialVersionUID = -4204806185089675978L;	// generated ID
	private static final int  SAVED_FORMAT_VERSION_NUMBER = 0;			// saved format version
	private 		GenomeSynchronizer 		multiGenome;		// Instance of the Multi Genome Manager
	private 	 	MGMultiGenome 			genomes;			// Instance of the MGMultiGenome
	private			Map<String, Boolean> 	activeGenome; 		// Mapping list of enable/disable genomes
	private			Map<String, Integer> 	genomeCounter; 		// Mapping list of enable/disable genomes


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);
		out.writeObject(multiGenome);
		out.writeObject(genomes);
		out.writeObject(activeGenome);
		out.writeObject(genomeCounter);
	}


	/**
	 * Method used for unserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();
		multiGenome = (GenomeSynchronizer) in.readObject();
		genomes = (MGMultiGenome) in.readObject();
		activeGenome = (Map<String, Boolean>) in.readObject();
		genomeCounter = (Map<String, Integer>) in.readObject();
	}


	/**
	 * Constructor of {@link SNPSynchroniser}
	 */
	protected SNPSynchroniser () {
		/*multiGenome = GenomeSynchronizer.getInstance();
		genomes = multiGenome.getGenomesInformation();
		initializesGenomeList();*/
	}


	/**
	 * Initializes the list of enabled/disabled genome.
	 * By default, all genome are set to false.
	 */
	protected void initializesGenomeList (List<String> genomeNameList) {
		activeGenome = new HashMap<String, Boolean>();
		genomeCounter = new HashMap<String, Integer>();
		for (String genomeName: genomeNameList) {
			activeGenome.put(genomeName, false);
			genomeCounter.put(genomeName, 0);
		}
	}


	/**
	 * Reinitializes SNP after each {@link PAMultiGenome} action.
	 * @param chromosomeList the chromosome list for multi genome project
	 */
	/*public void compute (Map<String, Chromosome> chromosomeList) {
		for (String name: activeGenome.keySet()) {
			
			if (activeGenome.get(name) && genomeCounter.get(name) == 0) {
				addSNP(chromosomeList, name);
			} else if (!activeGenome.get(name) && genomeCounter.get(name) == 0) {
				removeSNP(chromosomeList, name);
			}
		}

		/*for (String name: genomeCounter.keySet()) {
			if (genomeCounter.get(name) == 1) {
				addSNP(chromosomeList, name);
			} else if (genomeCounter.get(name) == 0) {
				removeSNP(chromosomeList, name);
			}
		}*/
	//}


	/**
	 * Adds SNPs information to a genome.
	 * @param genomeName a genome name
	 */
	private void addSNP (List<Chromosome> chromosomeList, String genomeName) {

		// Gets the reader
		VCFReader reader = multiGenome.getReader(genomeName, VCFType.SNPS);

		if (reader != null) {

			// Gets the multi genome chromosome list
			List<Chromosome> chromosomes = new ArrayList<Chromosome>(chromosomeList);

			// Gets other genome names present in the same VCF file (in order to get the MGPosition if already created)
			List<String> otherGenomeNames = getOtherGenomeNames(genomeName);

			// For each chromosome of the multi genome object
			for (Chromosome chromosome: chromosomes) {
				List<Map<String, Object>> result = null;
				try {
					result = reader.query(chromosome.getName(), 0, chromosome.getLength());
				} catch (IOException e) {
					e.printStackTrace();
				}

				// If results exist
				if (result != null) {
					for (Map<String, Object> info: result) {												// Scans every result lines
						int position = Integer.parseInt(info.get("POS").toString());						// Gets the reference genome position
						MGPosition positionInformation = null;												// Declares the MGPosition,
						// It is the VCF line information who can already exist for other genomes from the same VCF file,
						// if they have been required in the project and already processed
						for (String name: otherGenomeNames) {												// Scan for the other genomes
							positionInformation = genomes.getMGPosition(name, chromosome, position);		// Tries to get the MGPosition
							if (positionInformation != null) {												// If it is not null it exists
								break;																		// and the loop can be quit to do not scan the other genome (obviously same MGPosition object)
							}
						}
						if (positionInformation == null) {													// If no MGPosition has been got, it means it does not exist 
							positionInformation = new MGPosition(chromosome, info, reader);					// and it has to be instanced
						}
						Variant variant = new VCFSNP(genomeName, chromosome, positionInformation);			// Creates the SNP variant
						genomes.addVariant(genomeName, chromosome, variant);								// Adds the variant
					}

					// Updates all SNPs,
					// it consists to initializes the reference and meta genome offset.
					MGChromosome chromosomeInformation = genomes.getChromosomeInformation(genomeName, chromosome);
					chromosomeInformation.resetIndexList();													// Many position have just been added, the list has to be reinitialized
					int[] indexes = chromosomeInformation.getPositionIndex();
					for (int i = 1; i < indexes.length; i++) {
						chromosomeInformation.setCurrentPosition(indexes[i]);								// Sets the current position
						Variant current = chromosomeInformation.getCurrentVariant();						// Gets the current variant
						if (current instanceof VCFSNP) {													// If it is a SNP (it has just been added and has to be updated)
							chromosomeInformation.updatePreviousPosition(indexes[i-1]);						// Sets the previous position: value of indexes[i-1]
							Variant previous = chromosomeInformation.getPreviousPosition();					// Gets the previous variant
							current.setInitialReferenceOffset(previous.getNextReferencePositionOffset());	// Sets the initial reference genome offset
							current.setInitialMetaGenomeOffset(previous.getNextMetaGenomePositionOffset());	// Sets the initial meta genome offset
						}
					}

				}
			}

			//activeGenome.put(genomeName, true);																// The SNPs for that genome are now enabled

			performGC();
		}
	}


	/**
	 * Removes SNPs information from a genome.
	 * @param genomeName a genome name
	 */
	private void removeSNP (List<Chromosome> chromosomeList, String genomeName) {

		// Gets the multi genome chromosome list
		List<Chromosome> chromosomes = new ArrayList<Chromosome>(chromosomeList);

		// Gets the genome information
		MGGenome genome = genomes.getGenomeInformation(genomeName);

		// For each chromosome of the multi genome object
		for (Chromosome chromosome: chromosomes) {

			// Gets the variant list
			Map<Integer, Variant> variantList = genome.getGenomeInformation().get(chromosome).getPositionInformationList();

			// Gets the list of all reference genome position
			List<Integer> indexList = new ArrayList<Integer>(variantList.keySet());

			// Scans the variant list to find the ones instanced as VCFSNP in order to remove them
			for (int i: indexList) {
				if (variantList.get(i) instanceof VCFSNP) {
					variantList.remove(i);
				}
			}
		}

		//activeGenome.put(genomeName, false);		// The SNPs for that genome are now disabled

		performGC();
	}


	/**
	 * Enables SNPs information for a genome.
	 * @param genomeName a genome name
	 */
	private void enableGenome (List<Chromosome> chromosomeList, String genomeName) {
		int counter = genomeCounter.get(genomeName);
		if (counter == 0) {
			addSNP(chromosomeList, genomeName);
		}
		counter++;
		genomeCounter.put(genomeName, counter);
		activeGenome.put(genomeName, true);
	}


	/**
	 * Disables SNPs information for a genome.
	 * @param genomeName a genome name
	 */
	private void disableGenome (List<Chromosome> chromosomeList, String genomeName) {
		int counter = genomeCounter.get(genomeName);
		if (counter > 0) {
			counter--;
			if (counter == 0) {
				removeSNP(chromosomeList, genomeName);
			}
			genomeCounter.put(genomeName, counter);
		}
		activeGenome.put(genomeName, false);
	}


	/**
	 * Compares previous and new multi genome stripe in order to update lists of enabled genome SNPs.
	 * Enable a genome for SNP will load its SNP information.
	 * Disable a gnome for SNP will delete its SNP information.
	 * @param chromosomeList the chromosome list for multi genome project
	 * @param previousSettings 	the previous {@link MultiGenomeStripes} object
	 * @param newSettings		the new {@link MultiGenomeStripes} object
	 */
	public void updateEnabledSNPList (List<Chromosome> chromosomeList, MultiGenomeStripes previousSettings, MultiGenomeStripes newSettings) {
		List<String> nextGenomes = getGenomeNamesForSNP(newSettings.getRequiredGenomes());
		if (previousSettings != null) {
			List<String> previousGenomes = getGenomeNamesForSNP(previousSettings.getRequiredGenomes());

			// If genomes were present in the last multi genome stripe settings but not in the new one,
			// they have to be disabled.
			for (String name: previousGenomes) {
				if (!nextGenomes.contains(name)) {
					disableGenome(chromosomeList, name);
				}
			}

			// If genomes are present in the new mutli genome stripe settings but not in the previous one,
			// they have to be enable.
			for (String name: nextGenomes) {
				if (!previousGenomes.contains(name)) {
					enableGenome(chromosomeList, name);
				}
			}
		} else {
			// If there is no previous multi genome stripe settings,
			// every genome of the new one have to be enabled.
			for (String name: nextGenomes) {
				enableGenome(chromosomeList, name);
			}
		}
	}


	/**
	 * Gathers genome names require for a SNP display
	 * @param list association of genome name/variant type list
	 * @return the list of genome names
	 */
	private List<String> getGenomeNamesForSNP (Map<String, List<VariantType>> list) {
		List<String> names = new ArrayList<String>();
		for (String name: list.keySet()) {
			List<VariantType> variantList = list.get(name);
			if (variantList.contains(VariantType.SNPS)) {
				names.add(name);
			}
		}
		return names;
	}


	/**
	 * Gathers genome names required in the project and present in the VCF file.
	 * In order to do that, it uses a genome name but do not include it in the returned list.
	 * @param genomeName	a genome name
	 * @return				the list of genome names
	 */
	private List<String> getOtherGenomeNames (String genomeName) {
		final List<String> projectGenomeNames = genomes.getGenomeNameList();
		final VCFReader reader = multiGenome.getReader(genomeName, VCFType.SNPS);
		final List<String> vcfGenomeNames = reader.getRawGenomesNames();
		List<String> genomeNames = new ArrayList<String>();
		for (String fullName: projectGenomeNames) {
			if (activeGenome.get(fullName) &&
					vcfGenomeNames.contains(FormattedMultiGenomeName.getRawName(fullName))) {
				if (!fullName.equals(genomeName)) {
					genomeNames.add(fullName);
				}
			}
		}
		return genomeNames;
	}


	/**
	 * Runs the garbage collector
	 */
	private void performGC () {
		System.gc(); System.gc(); System.gc(); System.gc(); System.gc();
	}
}
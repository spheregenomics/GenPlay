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
 *     Author: Julien Lajugie <julien.lajugie@einstein.yu.edu>
 *     Website: <http://genplay.einstein.yu.edu>
 *******************************************************************************/
package edu.yu.einstein.genplay.core.multiGenome.VCFFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.yu.einstein.genplay.core.Chromosome;
import edu.yu.einstein.genplay.core.enums.VariantType;
import edu.yu.einstein.genplay.core.enums.VCFType;
import edu.yu.einstein.genplay.core.manager.ProjectManager;
import edu.yu.einstein.genplay.core.manager.multiGenomeManager.ReferenceGenomeManager;
import edu.yu.einstein.genplay.core.multiGenome.utils.FormattedMultiGenomeName;


/**
 * This class manages the genome information.
 * Those information are all genome names and their relative information.
 * A genome has a raw name (from the VCF file) and an explicit name given by users.
 * A genome belongs to a group which can have multiple genomes.
 * A group is related to a VCF file.
 * @author Nicolas Fourel
 */
public class VCFMultiGenomeInformation {

	private Map<String, VCFGenomeInformation> 	multiGenomeInformation;	// Genomes information: key are genome raw names
	private Map<String, List<String>> 			genomeGroupAssociation;	// Keys are group names, values are genome raw names
	private Map<String, String> 				genomeNamesAssociation;	// Keys are genome raw names, values are understandable names
	private Map<String, List<File>>				genomeFilesAssociation;	// Keys are group names, values are VCF files
	private Map<VCFType, List<File>>			filesTypeAssociation;	// Keys are information type (indel, SNPs, sv), values are associated files

	
	/**
	 * Constructor of {@link VCFMultiGenomeInformation}
	 */
	public VCFMultiGenomeInformation () {
		multiGenomeInformation = new HashMap<String, VCFGenomeInformation>();
	}


	/**
	 * Sets genome information.
	 * @param genomeGroupAssociation	association between groups and genome names
	 * @param genomeFilesAssociation	association between groups and VCF files
	 * @param genomeNamesAssociation	association between genome raw names and explicit names
	 */
	public void setGenomes (Map<String, List<String>> genomeGroupAssociation,
			Map<String, List<File>> genomeFilesAssociation,
			Map<String, String> genomeNamesAssociation,
			Map<VCFType, List<File>> filesTypeAssociation) {
		this.genomeGroupAssociation = genomeGroupAssociation;
		this.genomeFilesAssociation = genomeFilesAssociation;
		this.genomeNamesAssociation = genomeNamesAssociation;
		this.filesTypeAssociation = filesTypeAssociation;
		
		//showAllAssociation();
		
		//initMultiGenomeInformation();
	}
	
	
	public void initMultiGenomeInformation () {
		for (String genomeName: genomeNamesAssociation.keySet()) {
			multiGenomeInformation.put(genomeName, new VCFGenomeInformation());
		}
		multiGenomeInformation.put(ProjectManager.getInstance().getAssembly().getDisplayName(), new VCFGenomeInformation());
	}


	/**
	 * Adds a position information according to a genome and a chromosome.
	 * @param genome		the related genome
	 * @param chromosome	the related chromosome
	 * @param position		the position
	 * @param type			the information type
	 * @param offset		the offset position
	 */
	public void addInformation (String genome, Chromosome chromosome, Integer position, VariantType type, int length, Map<String, String> info) {
		multiGenomeInformation.get(genome).addInformation(chromosome, position, type, length, info);
	}
	

	/**
	 * @param chromosome 	the related chromosome
	 * @return 				list of valid chromosome containing position information
	 */
	public List<VCFChromosomeInformation> getCurrentChromosomeInformation (Chromosome chromosome) {
		List<VCFChromosomeInformation> info = new ArrayList<VCFChromosomeInformation>();
		for (String genomeName: multiGenomeInformation.keySet()) {
			if (!genomeName.equals(ReferenceGenomeManager.getInstance().getReferenceName())) {
				info.add(multiGenomeInformation.get(genomeName).getChromosomeInformation(chromosome));
			}
		}
		return info;
	}
	
	
	/**
	 * 
	 * @param genome
	 * @param chromosome
	 * @return
	 */
	public VCFChromosomeInformation getChromosomeInformation (String genome, Chromosome chromosome) {
		return multiGenomeInformation.get(genome).getChromosomeInformation(chromosome);
	}

	
	/**
	 * @param genome		the genome
	 * @param chromosome	the chromosome
	 * @param position		the position
	 * @return				the type of a specified position according to a genome and a chromosome
	 */
	public VariantType getType (String genome, Chromosome chromosome, Integer position) {
		return multiGenomeInformation.get(genome).getType(chromosome, position);
	}
	
	
	/**
	 * @return the multiGenomeInformation
	 */
	public Map<String, VCFGenomeInformation> getMultiGenomeInformation() {
		return multiGenomeInformation;
	}
	
	
	/**
	 * @return the multiGenomeInformation
	 */
	public VCFGenomeInformation getMultiGenomeInformation(String genome) {
		return multiGenomeInformation.get(genome);
	}
	
	
	/**
	 * @return the genomeGroupAssociation
	 */
	public Map<String, List<String>> getGenomeGroupAssociation() {
		return genomeGroupAssociation;
	}


	/**
	 * @return the genomeNamesAssociation
	 */
	public Map<String, String> getGenomeNamesAssociation() {
		return genomeNamesAssociation;
	}


	/**
	 * @return the genomeFilesAssociation
	 */
	public Map<String, List<File>> getGenomeFilesAssociation() {
		return genomeFilesAssociation;
	}
	
	
	/**
	 * @return vcf files
	 */
	public List<File> getVCFFiles () {
		List<File> list = new ArrayList<File>();
		for (List<File> fileList: filesTypeAssociation.values()) {
			for (File file: fileList) {
				list.add(file);
			}
		}
		return list;
	}
	
	
	/**
	 * @param file	a VCF file
	 * @return		the group name related to the VCF file
	 */
	public String getGenomeGroupFromFile(File file) {
		for (String groupName: genomeFilesAssociation.keySet()) {
			for (File vcf: genomeFilesAssociation.get(groupName)) {
				if (file.equals(vcf)) {
					return groupName;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @param file	a VCF file
	 * @return		the genome raw names list related to the VCF file
	 */
	public List<String> getGenomeNamesFromVCF (File file) {
		List<String> names = genomeGroupAssociation.get(getGenomeGroupFromFile(file));
		if (names != null) {
			return names;
		} else {
			return new ArrayList<String>();
		}
		
	}
	
	
	/**
	 * @param usualName the usual genome name
	 * @return	the raw genome name associated to the given usual genome name
	 */
	public String getRawGenomeName (String usualName) {
		for (String name: genomeNamesAssociation.keySet()) {
			if (usualName.equals(genomeNamesAssociation.get(name))) {
				return genomeNamesAssociation.get(name);
			}
		}
		return null;
	}
	
	
	/**
	 * @param vcf	the vcf file
	 * @return		the vcf type related to the vcf file
	 */
	public VCFType getTypeFromVCF (File vcf) {
		for (VCFType type: filesTypeAssociation.keySet()) {
			if (filesTypeAssociation.get(type).contains(vcf)) {
				return type;
			}
		}
		return null;
	}
	
	
	private int getGenomeNumber () {
		int cpt = 0;
		for (List<String> list: genomeGroupAssociation.values()) {
			cpt = cpt + list.size();
		}
		return cpt;
	}
	
	
	/**
	 * Creates an array with all genome names association.
	 * Used for display.
	 * @return	genome names association array
	 */
	public Object[] getFormattedGenomeArray () {
		String[] names = new String[getGenomeNumber() + 1];
		names[0] = ReferenceGenomeManager.getInstance().getReferenceName();
		int index = 1;
		List<String> namesList = new ArrayList<String>();
		for (String groupName: genomeGroupAssociation.keySet()) {
			namesList.add(groupName);
		}
		Collections.sort(namesList);
		for (String groupName: namesList) {
			for (String rawNames: genomeGroupAssociation.get(groupName)) {
				String name = FormattedMultiGenomeName.getFullFormattedGenomeName(groupName, genomeNamesAssociation.get(rawNames), rawNames);
				names[index] = name;
				index++;
			}
		}
		return names;
	}
	
	
	/**
	 * Shows content information
	 */
	public void showData () {
		System.out.println("===== Data");
		for (String genomeName: multiGenomeInformation.keySet()) {
			System.out.println("Genome name: " + genomeName);
			multiGenomeInformation.get(genomeName).showData();
		}
	}
	
	
	/**
	 * Shows all association information
	 */
	public void showAllAssociation () {
		showGenomeGroupAssociation();
		showGenomeNamesAssociation();
		showGenomeFilesAssociation();
		showFilesTypeAssociation();
	}
	
	
	/**
	 * Shows every raw genomes name for every genome group
	 */
	public void showGenomeGroupAssociation () {
		System.out.println("_____ GenomeGroupAssociation");
		for (String groupName: genomeGroupAssociation.keySet()) {
			String info = groupName + ":";
			for (String rawNames: genomeGroupAssociation.get(groupName)) {
				info = info + " " + rawNames;
			}
			System.out.println(info);
		}
	}
	
	
	/**
	 * Shows every association between raw and usual genome names
	 */
	public void showGenomeNamesAssociation () {
		System.out.println("_____ GenomeNamesAssociation");
		for (String rawNames: genomeNamesAssociation.keySet()) {
			System.out.println(rawNames + " : " + genomeNamesAssociation.get(rawNames));
		}
	}
	
	
	/**
	 * Shows all vcf file names for every genome group
	 */
	public void showGenomeFilesAssociation () {
		System.out.println("_____ GenomeFilesAssociation");
		for (String groupName: genomeFilesAssociation.keySet()) {
			String info = groupName + ":";
			for (File vcf: genomeFilesAssociation.get(groupName)) {
				info = info + " " + vcf.getAbsolutePath();
			}
			System.out.println(info);
		}
	}
	
	
	/**
	 * Shows all vcf file names for every vcf type
	 */
	public void showFilesTypeAssociation () {
		System.out.println("_____ FilesTypeAssociation");
		for (VCFType type: filesTypeAssociation.keySet()) {
			String info = type + ":";
			for (File vcf: filesTypeAssociation.get(type)) {
				info = info + " " + vcf.getName();
			}
			System.out.println(info);
		}
	}

	
}
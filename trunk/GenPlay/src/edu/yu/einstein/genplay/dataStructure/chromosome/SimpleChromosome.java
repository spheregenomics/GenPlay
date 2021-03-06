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
package edu.yu.einstein.genplay.dataStructure.chromosome;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.yu.einstein.genplay.core.comparator.ChromosomeComparator;


/**
 * Simple implementation of the {@link Chromosome} interface.
 * {@link Chromosome} objects are immutable.
 * @author Julien Lajugie
 */
public final class SimpleChromosome extends AbstractChromosome implements Chromosome {

	/**  Generated serial ID */
	private static final long serialVersionUID = -8339402742378578413L;

	/**  Version number of the class */
	private static final transient int CLASS_VERSION_NUMBER = 0;

	/** Length of the chromosome */
	private final int length;

	/** Name of the chromosome */
	private final String name;


	/**
	 * Constructor. Creates an instance of a {@link SimpleChromosome}.
	 * @param name Name of the chromosome.
	 * @param length Length of the chromosome.
	 */
	public SimpleChromosome(String name, int length) {
		this.name = name;
		this.length = length;
	}


	@Override
	public int compareTo(Chromosome otherChromosome) {
		ChromosomeComparator comp = new ChromosomeComparator();
		return comp.compare(this, otherChromosome);
	}


	@Override
	public int getLength() {
		return length;
	}


	@Override
	public String getName() {
		return name;
	}


	/**
	 * Returns true if the name of the current chromosome is equal to the specified string.
	 * Removes "chr" and "chromosome" before comparing if the string in parameter or if the
	 * chromosome name starts this way (ex: "chr1" becomes "1")
	 * @param otherChromoName
	 * @return true if equal, false otherwise
	 */
	public boolean hasSameNameAs(String otherChromoName) {
		// we remove "chr" or "chromosome" if the name of the current chromosome starts this way
		String chromoName = getName().trim();
		if ((chromoName.length() >= 10) && (chromoName.substring(0, 10).equalsIgnoreCase("chromosome"))) {
			chromoName = chromoName.substring(10);
		} else if ((chromoName.length() >= 3) && (chromoName.substring(0, 3).equalsIgnoreCase("chr"))) {
			chromoName = chromoName.substring(3);
		}
		// we remove "chr" or "chromosome" if the name of the other chromosome starts this way
		otherChromoName = otherChromoName.trim();
		if ((otherChromoName.length() >= 10) && (otherChromoName.substring(0, 10).equalsIgnoreCase("chromosome"))) {
			otherChromoName = otherChromoName.substring(10);
		} else if ((otherChromoName.length() >= 3) && (otherChromoName.substring(0, 3).equalsIgnoreCase("chr"))) {
			otherChromoName = otherChromoName.substring(3);
		}
		return chromoName.equalsIgnoreCase(otherChromoName);
	}


	/**
	 * Prints the chromosome information
	 */
	public void print() {
		String info = "";
		info += "Name: " + name + "; ";
		info += "Length: " + length;
		System.out.println(info);
	}


	/**
	 * Method used for deserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		// read the class version number
		in.readInt();
		// read the final fields
		in.defaultReadObject();
	}


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// write the class version number
		out.writeInt(CLASS_VERSION_NUMBER);
		// write the final fields
		out.defaultWriteObject();
	}
}

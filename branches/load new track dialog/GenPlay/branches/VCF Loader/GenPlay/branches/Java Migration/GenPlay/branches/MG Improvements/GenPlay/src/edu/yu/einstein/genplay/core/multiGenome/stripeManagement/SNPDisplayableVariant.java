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
package edu.yu.einstein.genplay.core.multiGenome.stripeManagement;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.yu.einstein.genplay.core.ChromosomeWindow;
import edu.yu.einstein.genplay.core.enums.VariantType;
import edu.yu.einstein.genplay.core.multiGenome.engine.Variant;


/**
 * This class symbolizes a variation information.
 * All variation are specific to a type and a start/stop couple of data.
 * @author Nicolas Fourel
 * @version 0.1
 */
public class SNPDisplayableVariant implements DisplayableVariant {

	private static final long serialVersionUID = -5300574920101738663L; // generated ID
	private static final int  SAVED_FORMAT_VERSION_NUMBER = 0;			// saved format version
	private Variant 			variant;		// The native variant
	private int 				position;		// Start position of the SNP displayable variant
	
	
	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);
		out.writeObject(variant);
		out.writeInt(position);
	}


	/**
	 * Method used for unserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();
		variant = (Variant) in.readObject();
		position = in.readInt();
	}
	

	/**
	 * Constructor of {@link SNPDisplayableVariant}
	 * @param variant 	a native variant
	 * @param position	start position of the SNP displayable variant
	 */
	public SNPDisplayableVariant (Variant variant, int position) {
		this.variant = variant;
		this.position = position;
	}


	@Override
	public Variant getNativeVariant() {
		return variant;
	}


	@Override
	public VariantType getType() {
		return VariantType.SNPS;
	}


	@Override
	public int getStart() {
		return position;
	}


	@Override
	public int getStop() {
		return position + 1;
	}


	@Override
	public ChromosomeWindow getDeadZone() {
		return null;
	}


	@Override
	public boolean deadZoneExists () {
		return false;
	}


	@Override
	public boolean isOnFirstAllele() {
		if (variant != null) {
			return variant.isOnFirstAllele();
		}
		return false;
	}


	@Override
	public boolean isOnSecondAllele() {
		if (variant != null) {
			return variant.isOnSecondAllele();
		}
		return false;
	}


	@Override
	public Double getQualityScore() {
		if (variant != null) {
			return variant.getQuality();
		}
		return 100.0;
	}

	
	@Override
	public int compareTo (DisplayableVariant displayableVariant) {
		int result;
		if (this.getStart() < displayableVariant.getStart()) {
			result = -1;
		} else if (this.getStart() > displayableVariant.getStart()) {
			result = 1;
		} else {
			result = -1;
			if (this.getStop() == displayableVariant.getStop() &&
					//this.getDeadZone().getStart() == variant.getDeadZone().getStart() &&
					//this.getDeadZone().getStop() == variant.getDeadZone().getStop() &&
					this.getQualityScore() == displayableVariant.getQualityScore()) {
				result = 0;
			}
		}
		return result;
	}


	@Override
	public void show () {
		String info = "";
		info += "start: " + getStart() + "\n";
		info += "stop: " + getStop() + "\n";
		info += "isOnFirstAllele: " + isOnFirstAllele() + "\n";
		info += "isOnSecondAllele: " + isOnSecondAllele() + "\n";
		info += "quality: " + getQualityScore() + "\n";
		System.out.println(info);
	}

}
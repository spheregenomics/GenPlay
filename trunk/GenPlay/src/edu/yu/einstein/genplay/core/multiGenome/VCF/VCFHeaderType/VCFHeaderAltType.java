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
package edu.yu.einstein.genplay.core.multiGenome.VCF.VCFHeaderType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.yu.einstein.genplay.dataStructure.enums.VCFColumnName;

/**
 * This class manages the ALT VCF field type information.
 * @author Nicolas Fourel
 * @version 0.1
 */
public class VCFHeaderAltType implements VCFHeaderType {

	private static final long serialVersionUID = 7171924074043506204L;	// generated ID
	private static final int  SAVED_FORMAT_VERSION_NUMBER = 0;			// saved format version
	private String id;				// information ID
	private String description; 	// field description


	@Override
	public String getAsOriginalLine() {
		String line = "";
		line += "##ALT=<ID=";
		line += id;
		line += ",Description=\"";
		line += description;
		line += "\">";
		return line;
	}


	@Override
	public VCFColumnName getColumnCategory() {
		return VCFColumnName.ALT;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public String getId() {
		return id;
	}


	/**
	 * Method used for unserialization
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readInt();
		id = (String) in.readObject();
		description = (String) in.readObject();
	}


	@Override
	public void setColumnCategory(VCFColumnName columnCategory) {}


	@Override
	public void setDescription(String description) {
		this.description = description;
	}


	@Override
	public void setId(String id) {
		this.id = id;
	}


	@Override
	public String toString () {
		return getColumnCategory() + " - " + id + ": " + description;
	}


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);
		out.writeObject(id);
		out.writeObject(description);
	}

}

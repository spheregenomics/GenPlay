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
package edu.yu.einstein.genplay.core.multiGenome.VCF.filtering;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import edu.yu.einstein.genplay.core.enums.VCFColumnName;
import edu.yu.einstein.genplay.core.multiGenome.VCF.VCFHeaderType.VCFHeaderType;
import edu.yu.einstein.genplay.core.multiGenome.VCF.filtering.utils.FilterUtility;
import edu.yu.einstein.genplay.core.multiGenome.VCF.filtering.utils.FlagUtility;
import edu.yu.einstein.genplay.core.multiGenome.VCF.filtering.utils.FormatFilterOperatorType;

/**
 * @author Nicolas Fourel
 * @version 0.1
 */
public class FlagIDFilter implements IDFilterInterface, Serializable {

	/** Generated default serial ID*/
	private static final long serialVersionUID = -2692600453534744380L;
	private static final int  SAVED_FORMAT_VERSION_NUMBER = 0;			// saved format version

	private FilterUtility				utility;
	private VCFHeaderType 				header;			// ID of the filter
	private boolean 					required;		// true if the value is required to pass the the filter
	private List<String>				genomeNames;	// the list of genomes to apply the filter (if required, null otherwise)
	private FormatFilterOperatorType 	operator;		// the operator to use to filter the genomes (if required, null otherwise)


	/**
	 * Method used for serialization
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(SAVED_FORMAT_VERSION_NUMBER);
		out.writeObject(header);
		out.writeBoolean(required);
		out.writeObject(genomeNames);
		out.writeObject(operator);
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
		header = (VCFHeaderType) in.readObject();
		required = in.readBoolean();
		genomeNames = (List<String>) in.readObject();
		operator = (FormatFilterOperatorType) in.readObject();
		utility = new FlagUtility();
	}

	
	/**
	 * Constructor of {@link FlagIDFilter}
	 */
	public FlagIDFilter () {
		utility = new FlagUtility();
	}
	

	@Override
	public VCFHeaderType getHeaderType() {
		return header;
	}


	@Override
	public void setHeaderType(VCFHeaderType id) {
		this.header = id;
	}


	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}


	/**
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}


	@Override
	public String toStringForDisplay() {
		return utility.toStringForDisplay(this);
	}


	@Override
	public String getErrors() {
		return utility.getErrors(this);
	}


	@Override
	public boolean isValid(Map<String, Object> object) {
		return utility.isValid(this, object);
	}


	@Override
	public boolean equals(Object obj) {
		return utility.equals(this, obj);
	}


	@Override
	public VCFColumnName getColumnName() {
		return header.getColumnCategory();
	}


	@Override
	public void setGenomeNames(List<String> genomeNames) {
		this.genomeNames = genomeNames;
	}


	@Override
	public List<String> getGenomeNames() {
		return genomeNames;
	}


	@Override
	public void setOperator(FormatFilterOperatorType operator) {
		this.operator = operator;
	}


	@Override
	public FormatFilterOperatorType getOperator() {
		return operator;
	}


	@Override
	public IDFilterInterface getDuplicate() {
		IDFilterInterface duplicate = new FlagIDFilter();
		duplicate.setHeaderType(getHeaderType());
		((FlagIDFilter)duplicate).setRequired(isRequired());
		duplicate.setGenomeNames(getGenomeNames());
		duplicate.setOperator(getOperator());
		return duplicate;
	}

}

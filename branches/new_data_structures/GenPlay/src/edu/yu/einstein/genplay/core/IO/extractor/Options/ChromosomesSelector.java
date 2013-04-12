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
package edu.yu.einstein.genplay.core.IO.extractor.Options;

import java.util.HashMap;
import java.util.Map;

import edu.yu.einstein.genplay.core.manager.project.ProjectChromosome;
import edu.yu.einstein.genplay.core.manager.project.ProjectManager;

/**
 * Used by extractor to define which chromosomes need to be extracted
 * @author Julien Lajugie
 */
public class ChromosomesSelector {

	/** Map of chromosomes name associated with a boolean set to null if all chromosomes need to be extracted */
	private final Map<String, Boolean> chromosomesSelection;


	/**
	 * Creates an instance of {@link ChromosomesSelector}
	 * @param chromosomesSelection chromosome to extract
	 */
	public ChromosomesSelector(boolean[] chromosomesSelection) {
		this.chromosomesSelection = new HashMap<String, Boolean>();
		ProjectChromosome projectChromosome = ProjectManager.getInstance().getProjectChromosome();
		for (int i = 0; i < projectChromosome.size(); i++) {
			this.chromosomesSelection.put(projectChromosome.get(i).getName(), chromosomesSelection[i]);
		}
	}





}

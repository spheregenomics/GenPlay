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
package edu.yu.einstein.genplay.core.converter.geneListConverter;

import java.util.ArrayList;
import java.util.List;

import edu.yu.einstein.genplay.core.converter.Converter;
import edu.yu.einstein.genplay.core.pileupFlattener.SimpleSCWPileupFlattener;
import edu.yu.einstein.genplay.dataStructure.enums.ScoreOperation;
import edu.yu.einstein.genplay.dataStructure.gene.Gene;
import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.SCWListView.bin.BinListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.GenomicListView;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.binList.BinList;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.geneList.GeneList;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListView;
import edu.yu.einstein.genplay.dataStructure.list.listView.ListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.scoredChromosomeWindow.ScoredChromosomeWindow;


/**
 * Creates a {@link BinList} from the data of the input {@link GeneList}
 * @author Julien Lajugie
 * @author Nicolas Fourel
 */
public class GeneListToBinList implements Converter {

	private final GeneList 				list; 			// The input list.
	private final int 					binSize;		// size of the bin of the result binlist
	private final ScoreOperation 		method; 		// method for the calculation of the scores of the result binlist
	private GenomicListView<?> 			result;			// The output list.


	/**
	 * Creates a {@link BinList} from the data of the input {@link GeneList}
	 * @param geneList the BinList
	 * @param binSize size of the bins
	 * @param method method to generate the BinList (eg: AVERAGE, SUM or MAXIMUM)
	 */
	public GeneListToBinList(GeneList geneList, int binSize, ScoreOperation method) {
		list = geneList;
		this.binSize = binSize;
		this.method = method;
	}


	@Override
	public void convert() throws Exception {
		List<ListView<ScoredChromosomeWindow>> resultList = new ArrayList<ListView<ScoredChromosomeWindow>>();
		for (ListView<Gene> currentLV: list) {
			ListViewBuilder<ScoredChromosomeWindow> lvBuilder = new BinListViewBuilder(binSize);
			SimpleSCWPileupFlattener flattener = new SimpleSCWPileupFlattener(method);
			for (ScoredChromosomeWindow scw: currentLV) {
				List<ScoredChromosomeWindow> flattenedWindows = flattener.addWindow(scw);
				if (!flattenedWindows.isEmpty()) {
					for (ScoredChromosomeWindow currentFlattenedWindow: flattenedWindows) {
						lvBuilder.addElementToBuild(currentFlattenedWindow);
					}
				}
			}
			List<ScoredChromosomeWindow> flattenedWindows = flattener.flush();
			if (!flattenedWindows.isEmpty()) {
				for (ScoredChromosomeWindow currentFlattenedWindow: flattenedWindows) {
					lvBuilder.addElementToBuild(currentFlattenedWindow);
				}
			}
			resultList.add(lvBuilder.getListView());
		}
		result = new BinList(resultList);
	}


	@Override
	public String getDescription() {
		return "Operation: Generate Fixed Window Track";
	}


	@Override
	public GenomicListView<?> getList() {
		return result;
	}


	@Override
	public String getProcessingDescription() {
		return "Generating Fixed Window Track";
	}
}

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
package edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.SimpleSCWList;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import edu.yu.einstein.genplay.core.IO.dataReader.SCWReader;
import edu.yu.einstein.genplay.core.pileupFlattener.GenomeWideFlattener;
import edu.yu.einstein.genplay.core.pileupFlattener.PileupFlattener;
import edu.yu.einstein.genplay.core.pileupFlattener.SimpleSCWPileupFlattener;
import edu.yu.einstein.genplay.dataStructure.chromosome.Chromosome;
import edu.yu.einstein.genplay.dataStructure.enums.ScoreOperation;
import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.SCWListView.SCWListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.SCWListView.dense.DenseSCWListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.SCWListView.generic.GenericSCWListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.chromosomeWideList.SCWListView.mask.MaskListViewBuilder;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.SCWList;
import edu.yu.einstein.genplay.dataStructure.list.genomeWideList.SCWList.SCWListBuilder;
import edu.yu.einstein.genplay.exception.exceptions.InvalidChromosomeException;
import edu.yu.einstein.genplay.exception.exceptions.ObjectAlreadyBuiltException;

/**
 * Factory class for vending {@link SimpleSCWList} objects.
 * @author Julien Lajugie
 */
public class SimpleSCWListFactory {


	/**
	 * Creates a dense {@link SCWList} from the data retrieved by the specified {@link SCWReader}.
	 * Dense {@link SCWList} are optimized to minimize the memory usage when most of the windows
	 * are consecutive (not separated by windows with a score of 0).
	 * @param scwReader a {@link SCWReader}
	 * @param scoreOperation {@link ScoreOperation} to compute the score of windows resulting from the "flattening" of a pileup of overlapping windows
	 * @return a new {@link SCWList}
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 * @throws ObjectAlreadyBuiltException
	 * @throws InvalidChromosomeException
	 */
	public static SCWList createDenseSCWList(SCWReader scwReader, ScoreOperation scoreOperation) throws InterruptedException, ExecutionException, CloneNotSupportedException, InvalidChromosomeException, ObjectAlreadyBuiltException, IOException {
		SCWListViewBuilder lvBuilderPrototype = new DenseSCWListViewBuilder();
		return createSimpleSCWList(scwReader, lvBuilderPrototype, scoreOperation);
	}


	/**
	 * Creates a generic {@link SCWList} from the data retrieved by the specified {@link SCWReader}.
	 * Generic {@link SCWList} are optimized to minimize the memory usage when most of the windows
	 * are not consecutive (separated by windows with a score of 0).
	 * @param scwReader a {@link SCWReader}
	 * @param scoreOperation {@link ScoreOperation} to compute the score of windows resulting from the "flattening" of a pileup of overlapping windows
	 * @return a new {@link SCWList}
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 * @throws ObjectAlreadyBuiltException
	 * @throws InvalidChromosomeException
	 */
	public static SCWList createGenericSCWList(SCWReader scwReader, ScoreOperation scoreOperation) throws InterruptedException, ExecutionException, CloneNotSupportedException, InvalidChromosomeException, ObjectAlreadyBuiltException, IOException {
		SCWListViewBuilder lvBuilderPrototype = new GenericSCWListViewBuilder();
		return createSimpleSCWList(scwReader, lvBuilderPrototype, scoreOperation);
	}


	/**
	 * Creates a mask {@link SCWList} from the data retrieved by the specified {@link SCWReader}.
	 * Mask {@link SCWList} only contains windows with a score of 0 or 1
	 * @param scwReader a {@link SCWReader}
	 * @return a new {@link SCWList}
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 * @throws ObjectAlreadyBuiltException
	 * @throws InvalidChromosomeException
	 */
	public static SCWList createMaskSCWList(SCWReader scwReader) throws InterruptedException, ExecutionException, CloneNotSupportedException, InvalidChromosomeException, ObjectAlreadyBuiltException, IOException {
		SCWListViewBuilder lvBuilderPrototype = new MaskListViewBuilder();
		SCWListBuilder builder = new SCWListBuilder(lvBuilderPrototype);
		while (scwReader.readItem()) {
			Chromosome currentChromosome = scwReader.getChromosome();
			builder.addElementToBuild(currentChromosome, scwReader.getStart(), scwReader.getStop(), scwReader.getScore());
		}
		return builder.getSCWList();
	}


	/**
	 * Creates a {@link SCWList} from the data retrieved by the specified {@link SCWReader}.
	 * The specified prototype determine the underlying data structure of the {@link SCWList}.
	 * @param scwReader a {@link SCWReader}
	 * @param scorePrecision precision of the scores of the scores of the genes and exons
	 * @param lvBuilderPrototype a prototype of builders
	 * @param scoreOperation {@link ScoreOperation} to compute the score of windows resulting from the "flattening" of a pileup of overlapping windows
	 * @return a new {@link SCWList}
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 * @throws ObjectAlreadyBuiltException
	 * @throws InvalidChromosomeException
	 */
	private static SCWList createSimpleSCWList(SCWReader scwReader, SCWListViewBuilder lvBuilderPrototype, ScoreOperation scoreOperation) throws InterruptedException, ExecutionException, CloneNotSupportedException, InvalidChromosomeException, ObjectAlreadyBuiltException, IOException {
		// create object that will "flattened" pileups of overlapping windows
		PileupFlattener flattenerPrototype = new SimpleSCWPileupFlattener(scoreOperation, lvBuilderPrototype);
		GenomeWideFlattener gwFlattener = new GenomeWideFlattener(flattenerPrototype);
		while (scwReader.readItem()) {
			gwFlattener.addElementToBuild(scwReader.getChromosome(), scwReader.getStart(), scwReader.getStop(), scwReader.getScore());
		}
		return new SimpleSCWList(gwFlattener.getGenomicList());
	}
}

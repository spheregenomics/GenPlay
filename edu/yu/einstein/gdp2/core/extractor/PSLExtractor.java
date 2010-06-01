/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.core.extractor;

import generator.BinListGenerator;
import generator.ChromosomeWindowListGenerator;
import generator.GeneListGenerator;
import generator.RepeatFamilyListGenerator;
import generator.ScoredChromosomeWindowListGenerator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import yu.einstein.gdp2.core.Chromosome;
import yu.einstein.gdp2.core.enums.DataPrecision;
import yu.einstein.gdp2.core.enums.ScoreCalculationMethod;
import yu.einstein.gdp2.core.enums.Strand;
import yu.einstein.gdp2.core.list.ChromosomeArrayListOfLists;
import yu.einstein.gdp2.core.list.ChromosomeListOfLists;
import yu.einstein.gdp2.core.list.SCWList.ScoredChromosomeWindowList;
import yu.einstein.gdp2.core.list.arrayList.DoubleArrayAsDoubleList;
import yu.einstein.gdp2.core.list.arrayList.IntArrayAsIntegerList;
import yu.einstein.gdp2.core.list.binList.BinList;
import yu.einstein.gdp2.core.list.chromosomeWindowList.ChromosomeWindowList;
import yu.einstein.gdp2.core.list.geneList.GeneList;
import yu.einstein.gdp2.core.list.repeatFamilyList.RepeatFamilyList;
import yu.einstein.gdp2.exception.InvalidChromosomeException;
import yu.einstein.gdp2.exception.InvalidDataLineException;


/**
 * A PSL file extractor
 * @author Julien Lajugie
 * @version 0.1
 */
public final class PSLExtractor extends TextFileExtractor implements Serializable, RepeatFamilyListGenerator, ChromosomeWindowListGenerator, 
ScoredChromosomeWindowListGenerator, BinListGenerator, GeneListGenerator {

	private static final long serialVersionUID = -7099425835087057587L;	//generated ID
	
	private ChromosomeListOfLists<Integer>	startList;		// list of position start
	private ChromosomeListOfLists<Integer>	stopList;		// list of position stop
	private ChromosomeListOfLists<String> 	nameList;		// list of name
	private ChromosomeListOfLists<Double>	scoreList;		// list of scores
	private ChromosomeListOfLists<Strand> 	strandList;		// list of strand
	private ChromosomeListOfLists<int[]> 	exonStartsList;	// list of list of exon starts
	private ChromosomeListOfLists<int[]> 	exonStopsList;	// list of list of exon stops
	
	private String							searchURL;		// url of the gene database for the search
	
	
	/**
	 * Creates an instance of {@link PSLExtractor}
	 * @param dataFile file containing the data
	 * @param logFile file for the log (no log if null)
	 */
	public PSLExtractor(File dataFile, File logFile) {
		super(dataFile, logFile);
		// initialize the lists
		startList = new ChromosomeArrayListOfLists<Integer>();
		stopList = new ChromosomeArrayListOfLists<Integer>();
		nameList = new ChromosomeArrayListOfLists<String>();
		scoreList = new ChromosomeArrayListOfLists<Double>();
		strandList = new ChromosomeArrayListOfLists<Strand>();
		exonStartsList = new ChromosomeArrayListOfLists<int[]>();
		exonStopsList = new ChromosomeArrayListOfLists<int[]>();
		// initialize the sublists
		for (int i = 0; i < chromosomeManager.size(); i++) {
			startList.add(new IntArrayAsIntegerList());
			stopList.add(new IntArrayAsIntegerList());
			nameList.add(new ArrayList<String>());
			scoreList.add(new DoubleArrayAsDoubleList());
			strandList.add(new ArrayList<Strand>());
			exonStartsList.add(new ArrayList<int[]>());
			exonStopsList.add(new ArrayList<int[]>());
		}
	}

	@Override
	protected void extractLine(String extractedLine)	throws InvalidDataLineException {
		if (extractedLine.trim().substring(0, 10).equalsIgnoreCase("searchURL=")) {
			searchURL = extractedLine.split("\"")[1].trim();
		}
		String[] splitedLine = extractedLine.split("\t");
		if (splitedLine.length == 1) {
			splitedLine = extractedLine.split(" ");
		}
		if (splitedLine.length < 21) {
			throw new InvalidDataLineException(extractedLine);
		}

		try {
			Chromosome chromosome = chromosomeManager.get(splitedLine[13]) ;
			nameList.add(chromosome, splitedLine[9]);
			startList.add(chromosome, Integer.parseInt(splitedLine[15]));
			stopList.add(chromosome, Integer.parseInt(splitedLine[16]));
			scoreList.add(chromosome, Double.parseDouble(splitedLine[0]));
			strandList.add(chromosome, Strand.get(splitedLine[8]));
			// add exons
			String[] exonStartsStr = splitedLine[20].split(",");
			String[] exonLengthsStr = splitedLine[18].split(",");
			int[] exonStarts = new int[exonStartsStr.length];
			int[] exonStops = new int[exonStartsStr.length];
			for (int i = 0; i < exonStartsStr.length; i++) {
				exonStarts[i] = Integer.parseInt(exonStartsStr[i].trim());
				exonStops[i] = exonStarts[i] + Integer.parseInt(exonLengthsStr[i].trim());
			}
			exonStartsList.add(chromosome, exonStarts);
			exonStopsList.add(chromosome, exonStops);
			lineCount++;
		} catch (InvalidChromosomeException e) {
			throw new InvalidDataLineException(extractedLine);
		}
	}


	@Override
	public RepeatFamilyList toRepeatFamilyList() throws InvalidChromosomeException {
		return new RepeatFamilyList(startList, stopList, nameList);
	}


	@Override
	public ChromosomeWindowList toChromosomeWindowList() throws InvalidChromosomeException {
		return new ChromosomeWindowList(startList, stopList);
	}


	@Override
	public ScoredChromosomeWindowList toScoredChromosomeWindowList() throws InvalidChromosomeException {
		return new ScoredChromosomeWindowList(startList, stopList, scoreList);
	}


	@Override
	public boolean isBinSizeNeeded() {
		return true;
	}


	@Override
	public boolean isCriterionNeeded() {
		return true;
	}
	
	
	@Override
	public boolean isPrecisionNeeded() {
		return true;
	}


	@Override
	public BinList toBinList(int binSize, DataPrecision precision, ScoreCalculationMethod method) throws IllegalArgumentException, InterruptedException, ExecutionException {
		return new BinList(binSize, precision, method, startList, stopList, scoreList);
	}

	@Override
	public GeneList toGeneList() throws InvalidChromosomeException {
		return new GeneList(nameList, strandList, startList, stopList, exonStartsList, exonStopsList, null, searchURL);
	}
}

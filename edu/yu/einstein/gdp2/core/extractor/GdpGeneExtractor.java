package yu.einstein.gdp2.core.extractor;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import yu.einstein.gdp2.core.Chromosome;
import yu.einstein.gdp2.core.enums.Strand;
import yu.einstein.gdp2.core.list.ChromosomeArrayListOfLists;
import yu.einstein.gdp2.core.list.ChromosomeListOfLists;
import yu.einstein.gdp2.core.list.geneList.GeneList;
import yu.einstein.gdp2.core.list.geneList.GeneListGenerator;
import yu.einstein.gdp2.exception.InvalidChromosomeException;
import yu.einstein.gdp2.exception.InvalidDataLineException;
import yu.einstein.gdp2.exception.ManagerDataNotLoadedException;
import yu.einstein.gdp2.util.ChromosomeManager;

public final class GdpGeneExtractor extends Extractor
implements Serializable, GeneListGenerator {

	private static final long serialVersionUID = 7967902877674655813L; // generated ID

	private ChromosomeListOfLists<Integer>	startList;		// list of position start
	private ChromosomeListOfLists<Integer>	stopList;		// list of position stop
	private ChromosomeListOfLists<String> 	nameList;		// list of name
	private ChromosomeListOfLists<Strand> 	strandList;		// list of strand
	private ChromosomeListOfLists<int[]> 	exonStartsList;	// list of list of exon starts
	private ChromosomeListOfLists<int[]> 	exonStopsList;	// list of list of exon stops
	private ChromosomeListOfLists<double[]>	exonScoresList;	// list of list of exon scores


	/**
	 * Creates an instance of a {@link GdpGeneExtractor}
	 * @param dataFile file containing the data
	 * @param logFile file for the log (no log if null)
	 * @param chromosomeManager a {@link ChromosomeManager}
	 */
	public GdpGeneExtractor(File dataFile, File logFile, ChromosomeManager chromosomeManager) {
		super(dataFile, logFile, chromosomeManager);
		// initialize the lists
		startList = new ChromosomeArrayListOfLists<Integer>(chromosomeManager);
		stopList = new ChromosomeArrayListOfLists<Integer>(chromosomeManager);
		nameList = new ChromosomeArrayListOfLists<String>(chromosomeManager);
		strandList = new ChromosomeArrayListOfLists<Strand>(chromosomeManager);
		exonStartsList = new ChromosomeArrayListOfLists<int[]>(chromosomeManager);
		exonStopsList = new ChromosomeArrayListOfLists<int[]>(chromosomeManager);
		exonScoresList = new ChromosomeArrayListOfLists<double[]>(chromosomeManager);
		// initialize the sublists
		for (int i = 0; i < chromosomeManager.chromosomeCount(); i++) {
			startList.add(new ArrayList<Integer>());
			stopList.add(new ArrayList<Integer>());
			nameList.add(new ArrayList<String>());
			strandList.add(new ArrayList<Strand>());
			exonStartsList.add(new ArrayList<int[]>());
			exonStopsList.add(new ArrayList<int[]>());
			exonScoresList.add(new ArrayList<double[]>());
		}
	}


	/**
	 * Receives one line from the input file and extracts and adds the data in the lists
	 * @param Extractedline line read from the data file  
	 * @throws ManagerDataNotLoadedException 
	 * @throws InvalidDataLineException 
	 */
	@Override
	protected void extractLine(String extractedLine) throws ManagerDataNotLoadedException, InvalidDataLineException {
		String[] splitedLine = extractedLine.split("\t");
		if (splitedLine.length == 1) {
			splitedLine = extractedLine.split(" ");
		}
		if (splitedLine.length < 3) {
			throw new InvalidDataLineException(extractedLine);
		}
		try {
			Chromosome chromosome = chromosomeManager.getChromosome(splitedLine[1]) ;
			String name = splitedLine[0].trim();
			nameList.add(chromosome, name);
			Strand strand = Strand.get(splitedLine[2].trim());
			strandList.add(chromosome, strand);
			int start = Integer.parseInt(splitedLine[3].trim());
			startList.add(chromosome, start);
			int stop = Integer.parseInt(splitedLine[4].trim());
			stopList.add(chromosome, stop);
			String[] exonStartsStr = splitedLine[5].split(",");
			String[] exonStopsStr = splitedLine[6].split(",");
			int[] exonStarts = new int[exonStartsStr.length];
			int[] exonStops = new int[exonStartsStr.length];
			for (int i = 0; i < exonStartsStr.length; i++) {
				exonStarts[i] = Integer.parseInt(exonStartsStr[i].trim());
				exonStops[i] = Integer.parseInt(exonStopsStr[i].trim());
			}
			exonStartsList.add(chromosome, exonStarts);
			exonStopsList.add(chromosome, exonStops);
			if (splitedLine.length > 7) {
				String[] exonScoresStr = splitedLine[7].split(",");
				double[] exonScores = new double[exonScoresStr.length];
				for (int i = 0; i < exonScoresStr.length; i++) {
					exonScores[i] = Double.parseDouble(exonScoresStr[i]);
				}
				exonScoresList.add(chromosome, exonScores);
			}
			lineCount++;
		} catch (InvalidChromosomeException e) {
			throw new InvalidDataLineException(extractedLine);
		}
	}


	@Override
	public GeneList toGeneList() throws ManagerDataNotLoadedException, InvalidChromosomeException {
		return new GeneList(chromosomeManager, nameList, strandList, startList, stopList, exonStartsList, exonStopsList, exonScoresList);
	}
}

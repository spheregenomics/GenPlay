/**
 * @author Julien Lajugie
 * @version 0.1
 */
package yu.einstein.gdp2.core.writer.binListWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import yu.einstein.gdp2.core.Chromosome;
import yu.einstein.gdp2.core.list.binList.BinList;
import yu.einstein.gdp2.gui.statusBar.Stoppable;


/**
 * Allows to write a BinList as a Bed file.
 * @author Julien Lajugie
 * @version 0.1
 */
public final class BinListAsBedWriter extends BinListWriter implements Stoppable {

	private boolean needsToBeStopped = false;	// true if the writer needs to be stopped 
	
	
	/**
	 * Creates an instance of {@link BinListAsBedWriter}.
	 * @param outputFile output {@link File}
	 * @param data {@link BinList} to write
	 * @param name a name for the {@link BinList}
	 */
	public BinListAsBedWriter(File outputFile, BinList data, String name) {
		super(outputFile, data, name);
	}


	@Override
	public void write() throws IOException, InterruptedException {
		BufferedWriter writer = null;
		try {
			// try to create a output file
			writer = new BufferedWriter(new FileWriter(outputFile));
			// print the title of the graph
			writer.write("track type=bed name=" + name);
			writer.newLine();
			int binSize = data.getBinSize();
			// print the data
			for(Chromosome currentChromosome: chromosomeManager) {
				if(data.get(currentChromosome) != null) {
					List<Double> currentList = data.get(currentChromosome);
					for (int j = 0; j < currentList.size(); j++) {
						// if the operation need to be stopped we close the writer and delete the file 
						if (needsToBeStopped) {
							writer.close();
							outputFile.delete();
							throw new InterruptedException();
						}
						// we don't print the line if the score is 0
						if (currentList.get(j) != 0) {
							writer.write(currentChromosome.getName() + "\t" + (j * binSize) + "\t" + ((j + 1) * binSize) + "\t-\t" + currentList.get(j));
							writer.newLine();
						}
					}
				}
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	
	/**
	 * Stops the writer while it's writing a file
	 */
	@Override
	public void stop() {
		needsToBeStopped = true;
	}
}

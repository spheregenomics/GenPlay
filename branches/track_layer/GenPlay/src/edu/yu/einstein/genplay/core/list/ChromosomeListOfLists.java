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
package edu.yu.einstein.genplay.core.list;

import java.io.Serializable;
import java.util.List;

import edu.yu.einstein.genplay.core.chromosome.Chromosome;
import edu.yu.einstein.genplay.core.manager.project.ProjectChromosome;
import edu.yu.einstein.genplay.exception.InvalidChromosomeException;




/**
 * This class represents a generic list organized by chromosome
 * @param <T> type of the objects stored in the list
 * @author Julien Lajugie
 * @version 0.1
 */
public interface ChromosomeListOfLists<T> extends Cloneable, Serializable, List<List<T>> {


	/**
	 * Adds an element to the list of the specified chromosome
	 * @param chromosome chromosome of the item
	 * @param element element to add
	 * @throws InvalidChromosomeException
	 */
	public void add(Chromosome chromosome, T element) throws InvalidChromosomeException;


	/**
	 * @param chromosome a {@link Chromosome}
	 * @return the list associated to the specified {@link Chromosome}
	 * @throws InvalidChromosomeException
	 */
	public List<T> get(Chromosome chromosome) throws InvalidChromosomeException;


	/**
	 * @param chromosome index of a chromosome
	 * @param index
	 * @return the data with the specified index on the specified chromosome
	 * @throws InvalidChromosomeException
	 */
	public T get(Chromosome chromosome, int index) throws InvalidChromosomeException;


	/**
	 * @param chromosomeIndex index of a chromosome
	 * @param elementIndex
	 * @return the data with the specified index on the specified chromosome
	 */
	public T get(int chromosomeIndex, int elementIndex);


	/**
	 * Sets the element on the specified index of the specified {@link Chromosome}
	 * @param chromosome a {@link Chromosome}
	 * @param index
	 * @param element element to set
	 * @throws InvalidChromosomeException if the specified {@link Chromosome} is not a chromosome of the {@link ProjectChromosome}
	 */
	public void set(Chromosome chromosome, int index, T element) throws InvalidChromosomeException;



	/**
	 * Sets the list of elements on the specified {@link Chromosome}
	 * @param chromosome a {@link Chromosome}
	 * @param list list to set
	 * @throws InvalidChromosomeException if the specified {@link Chromosome} is not a chromosome of the {@link ProjectChromosome}
	 */
	public void set(Chromosome chromosome, List<T> list) throws InvalidChromosomeException;


	/**
	 * Sets the element on the specified index of the specified {@link Chromosome}
	 * @param chromosomeIndex
	 * @param elementIndex
	 * @param element value to set
	 */
	public void set(int chromosomeIndex, int elementIndex, T element);


	/**
	 * @param index index of a chromosome
	 * @return the size of the list for the specified chromosome
	 */
	public int size(int index);


	/**
	 * @param chromosome
	 * @return the size of the list for a specified chromosome
	 * @throws InvalidChromosomeException
	 */
	public int size(Chromosome chromosome) throws InvalidChromosomeException;
}

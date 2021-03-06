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
package edu.yu.einstein.genplay.core.DAS;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Parse a DNS XML file and extract the list of data source
 * <br/>See <a href="http://www.biodas.org/documents/spec.html">http://www.biodas.org/documents/spec.html</a>
 * @author Julien Lajugie
 */
public class DataSourceHandler extends DefaultHandler {

	private final List<DataSource> 	dataSourceList;					// list of DataSource
	private String 					currentMarkup = null;			// current XML markup
	private DataSource 				currentDataSource = null;		// current data source


	/**
	 * Creates an instance of {@link DataSourceHandler}
	 */
	public DataSourceHandler() {
		super();
		dataSourceList = new ArrayList<DataSource>();
	}


	@Override
	public void characters(char[] ch, int start, int length) {
		if (currentMarkup != null) {
			String elementValue = new String(ch, start, length);
			if (currentMarkup.equals("SOURCE")) {
				currentDataSource.setName(elementValue);
			} else if (currentMarkup.equals("MAPMASTER")) {
				currentDataSource.setMapMaster(elementValue);
			} else if (currentMarkup.equals("DESCRIPTION")) {
				currentDataSource.setDescription(elementValue);
			}
		}
	}


	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("DSN")) {
			dataSourceList.add(currentDataSource);
		}
	}


	/**
	 * @return the List of {@link DataSource}
	 */
	public final List<DataSource> getDataSourceList() {
		return dataSourceList;
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("DSN")) {
			currentDataSource = new DataSource();
		} else if (qName.equalsIgnoreCase("SOURCE")) {
			currentMarkup = "SOURCE";
			if(attributes.getLength() > 0) {
				currentDataSource.setID(attributes.getValue("id"));
				currentDataSource.setVersion(attributes.getValue("version"));
			}
		} else if (qName.equalsIgnoreCase("MAPMASTER")) {
			currentMarkup = "MAPMASTER";
		} else if (qName.equalsIgnoreCase("DESCRIPTION")) {
			currentMarkup = "DESCRIPTION";
			if(attributes.getLength() > 0) {
				currentDataSource.setHref(attributes.getValue("href"));
			}
		} else {
			currentMarkup = null;
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.calculation;

import java.util.ArrayList;

public class Data {
	
	int id;
	double value;
	ArrayList<Coordinate> coordinateCollection;
	
	public Data() {}
	public Data(double value, Coordinate c) {
		setValue(value);
		addOrdinate(c);
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public ArrayList<Coordinate> getOrdinateCollection() {
		return coordinateCollection;
	}
	
	public void setOrdinateCollection(ArrayList<Coordinate> c) {
		coordinateCollection = c;
	}
	
	public void addOrdinate(Coordinate c){
		coordinateCollection.add(c);
	}

	public String toXML() {
		String xmlString ="<Data id=\"" + id + "\" value=\"" + getValue() + "\">\n";
		
		xmlString = xmlString + "<CoordinateCollection>";
		int coordCount = coordinateCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + coordinateCollection.get(i).toXML() + "\n";
		}
		xmlString = xmlString + "</CoordinateCollection>\n";
		
		xmlString = xmlString + "</Data>\n";
		return xmlString;
	}
}

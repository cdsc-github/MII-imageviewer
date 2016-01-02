/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.calculation;

public class Coordinate {
	
	int id;
	int dimensionIndex;
	int position;
	
	public Coordinate() {}	
	public Coordinate(int dimensionIndex, int position) {
		id = 0;
		setDimensionIndex(dimensionIndex);
		setPosition(position);
	}

	public int getDimensionIndex() {
		return dimensionIndex;
	}

	public void setDimensionIndex(int dimensionIndex) {
		this.dimensionIndex = dimensionIndex;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String toXML() {
		String xmlString = "<Coordinate dimensionIndex=\"" + getDimensionIndex() + "\" position=\"" + getPosition() + "\" id=\"" + id + "\" />\n";
		return xmlString;
	}
	
	

}

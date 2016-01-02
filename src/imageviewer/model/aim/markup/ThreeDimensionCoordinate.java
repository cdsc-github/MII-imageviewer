/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.markup;

public class ThreeDimensionCoordinate  extends SpatialCoordinate{
	
	String frameOfReferenceUID;
	double x;
	double y;
	double z;
	
	public ThreeDimensionCoordinate(double x, double y, double z) {
		setX(x);
		setY(y);
		setZ(z);
	}
	
	public String toXML() {
		String xmlString = "<SpatialCoordinate coordinateIndex = \"" + coordinateIndex + "\""
											+ "frameOfReferenceUID=\"" + frameOfReferenceUID + "\" "
											+ "xsi:type=\"TwoDimensionSpatialCoordinate\""
											+ "x=\"" + x + "\" y=\"" + y + "\" z=\"" + z + "\">";		
		
		return xmlString;
	}

	public String getFrameOfReferenceUID() {
		return frameOfReferenceUID;
	}

	public void setFrameOfReferenceUID(String frameOfReferenceUID) {
		this.frameOfReferenceUID = frameOfReferenceUID;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
	
	

}

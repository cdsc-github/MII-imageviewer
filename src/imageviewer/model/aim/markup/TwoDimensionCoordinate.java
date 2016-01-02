/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.markup;

public class TwoDimensionCoordinate extends SpatialCoordinate{
	
	String ImageReferenceUID;
	int referencedFrameNumber;
	double x;
	double y;
	
	public TwoDimensionCoordinate(double x, double y) {
		setX(x);
		setY(y);
		setReferencedFrameNumber(0);
	}
	
	public TwoDimensionCoordinate(double x, double y, int frame) {
		setX(x);
		setY(y);
		setReferencedFrameNumber(frame);
	}
	
	public String toXML() {
		String xmlString = "<SpatialCoordinate coordinateIndex = \"" + coordinateIndex + "\""
											+ " referencedFrameNumber=\"" + referencedFrameNumber + "\""
											+ " imageReferenceUID=\"" + ImageReferenceUID + "\""
											+ " xsi:type=\"TwoDimensionSpatialCoordinate\""
											+ " x=\"" + x + "\" y=\"" + y + "\" />";		
		
		return xmlString;
	}

	public String getImageReferenceUID() {
		return ImageReferenceUID;
	}

	public void setImageReferenceUID(String imageReferenceUID) {
		ImageReferenceUID = imageReferenceUID;
	}

	public int getReferencedFrameNumber() {
		return referencedFrameNumber;
	}

	public void setReferencedFrameNumber(int referencedFrameNumber) {
		this.referencedFrameNumber = referencedFrameNumber;
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
	
	

}

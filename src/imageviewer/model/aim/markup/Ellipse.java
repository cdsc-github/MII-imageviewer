/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.markup;

public class Ellipse extends GeometricShape{

	SpatialCoordinate center;
	SpatialCoordinate longRadiusPoint;
	SpatialCoordinate shortRadiusPoint;
	
	public Ellipse() {}
	
	public Ellipse(SpatialCoordinate c, SpatialCoordinate lr, SpatialCoordinate sr) {
		setCenter(c);
		setLongRadiusPoint(lr);
		setShortRadiusPoint(sr);	
	}
	
	public String toXML(){
		String xmlString = "<GeometricShape id=\"" + id + "\" xsi:type=\"Ellipse\"" + ">\n";
		xmlString = xmlString + "<SpatialCoordinateCollection>\n";
		int coordCount = SpatialCoordinateCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + SpatialCoordinateCollection.get(i).toXML() + "\n";
		}
		xmlString = xmlString + "</SpatialCoordinateCollection>\n";
		xmlString = xmlString + "</GeometricShape>\n";
		
		
		return xmlString;
	}

	public SpatialCoordinate getCenter() {
		return center;
	}

	public void setCenter(SpatialCoordinate center) {
		this.center = center;
	}

	public SpatialCoordinate getLongRadiusPoint() {
		return longRadiusPoint;
	}

	public void setLongRadiusPoint(SpatialCoordinate longRadiusPoint) {
		this.longRadiusPoint = longRadiusPoint;
	}

	public SpatialCoordinate getShortRadiusPoint() {
		return shortRadiusPoint;
	}

	public void setShortRadiusPoint(SpatialCoordinate shortRadiusPoint) {
		this.shortRadiusPoint = shortRadiusPoint;
	}
	
	
	
}

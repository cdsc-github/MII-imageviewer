/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.markup;

public class Circle extends GeometricShape{
	
	SpatialCoordinate center;
	SpatialCoordinate radiusPoint;
	
	public Circle() {super();}
	public Circle(SpatialCoordinate c, SpatialCoordinate r) {
		super();
		
		setCenter(c);
		setRadiusPoint(r);
		
	}
	
	public String toXML(){
		String xmlString = "<GeometricShape id=\"" + id + "\" xsi:type=\"Circle\"" + ">\n";
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
	
	public SpatialCoordinate getRadiusPoint() {
		return radiusPoint;
	}
	public void setRadiusPoint(SpatialCoordinate radiusPoint) {
		this.radiusPoint = radiusPoint;
	}

}

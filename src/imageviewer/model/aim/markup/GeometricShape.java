/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.markup;

import java.util.ArrayList;

public class GeometricShape {
	
	int id = 0;
	String lineColor;
	String lineOpacity;
	String lineStyle;
	String lineThickness;
	boolean includeFlag;
	int shapeIdentifier;
	
	ArrayList<SpatialCoordinate> SpatialCoordinateCollection = new ArrayList<SpatialCoordinate>();
	
	public GeometricShape() {}
	
	public String toXML(){
		return "";
	}
	
	public ArrayList<SpatialCoordinate> getSpatialCoordinateCollection() {
		return SpatialCoordinateCollection;
	}

	public void setSpatialCoordinateCollection(ArrayList<SpatialCoordinate> scCollection) {
		SpatialCoordinateCollection = scCollection;
	}
	
	public void addSpatialCoordinate(SpatialCoordinate sc){
		SpatialCoordinateCollection.add(sc);
	}

	public String getLineColor() {
		return lineColor;
	}

	public void setLineColor(String lineColor) {
		this.lineColor = lineColor;
	}

	public String getLineOpacity() {
		return lineOpacity;
	}

	public void setLineOpacity(String lineOpacity) {
		this.lineOpacity = lineOpacity;
	}

	public String getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(String lineStyle) {
		this.lineStyle = lineStyle;
	}

	public String getLineThickness() {
		return lineThickness;
	}

	public void setLineThickness(String lineThickness) {
		this.lineThickness = lineThickness;
	}

	public boolean isIncludeFlag() {
		return includeFlag;
	}

	public void setIncludeFlag(boolean includeFlag) {
		this.includeFlag = includeFlag;
	}

	public int getShapeIdentifier() {
		return shapeIdentifier;
	}

	public void setShapeIdentifier(int shapeIdentifier) {
		this.shapeIdentifier = shapeIdentifier;
	}	

}

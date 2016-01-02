/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.markup;

public class SpatialCoordinate {
	
	int id;
	int coordinateIndex;
	
	public SpatialCoordinate() {}

	public int getCoordinateIndex() {
		return coordinateIndex;
	}

	public void setCoordinateIndex(int coordinateIndex) {
		this.coordinateIndex = coordinateIndex;
	}

	public String toXML() {
		return "";
	}
}

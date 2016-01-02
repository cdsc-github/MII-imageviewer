/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.plugins.fp;

import java.awt.Point;
import java.awt.Polygon;

// =======================================================================

public class Contour extends Polygon {

	public Contour next=null;
	int area=0;

	public Contour() {}

	// =======================================================================

	public Point getFirstPoint() {return (npoints>0) ? new Point(xpoints[0],ypoints[0]) : null;}
	public Point getLastPoint() {return (npoints>0) ? new Point(xpoints[npoints-1],ypoints[npoints-1]) : null;}

	public int getArea() {return area;}
	public void setArea(int x) {area=x;}

	// =======================================================================
	
	public int computeArea() {area=0;	for (int j=0; j<(npoints-1); j++)	area+=(xpoints[j]*(ypoints[j+1]-ypoints[j]))-ypoints[j]*(xpoints[j+1]-xpoints[j]); return Math.abs(area/2);}
}


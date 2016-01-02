/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import imageviewer.rendering.RenderingProperties;
import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.annotation.ControlPoint.Location;

// =======================================================================

public interface Selectable {

	public boolean isSelected();                                                  // Is the object currently selected?
	public boolean isMovable();                                                   // Can the object be moved?
	public boolean hasControlPoints();                                            // Are there control points on the object for resizing?
	public boolean hasRotationAxis();                                             // Does the object have its own rotation axis that needs to be preserved?

	public void select();                                                         // Select the object
	public void deselect();                                                       // Deselect the object

	public void cut();
	public void copy();
	public void paste();

	public void delete();

	public void moveStart(int x, int y);                                          // Handle moving of object by (x,y) coordinates
	public void moveSDrag(int x, int y);                                          // Handle moving of object by (x,y) coordinates
	public void moveEnd(int x, int y);                                            // Handle moving of object by (x,y) coordinates

	public void controlPointMoveStart(ControlPoint cp, Point2D.Double p1);              
	public void controlPointMoveDrag(ControlPoint cp, RenderingProperties rp, Point2D.Double[] delta, Point2D.Double p);
	public void controlPointMoveEnd(ControlPoint cp, Point2D.Double p1, Point2D.Double p2); 
	public void setControlPoints(ArrayList<ControlPoint> x); 

	public double getRotationAxis();                                              // Return the computed rotation axis for the object. Used to preserve geometry.

	public ArrayList<ControlPoint> getControlPoints();                            // Get the control points associated with the object, if any
	public ControlPoint getControlPoint(Location x);                              // Get a specific control point by location
}

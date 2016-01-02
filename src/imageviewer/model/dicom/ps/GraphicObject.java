/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom.ps;

import java.io.Serializable;

// =======================================================================
/* The presentation state file determines certain type information by
 * the layer it resides in.  There are 2 layers we're storing things
 * in, the DEFAULT and MEASUREMENT layers.  DEFAULT layer is for
 * things like Text comments and POLYLINE that are just pointers to
 * things, not measurements.  The MEASUREMENT layer can have a
 * ELLIPSE, POINT, POLYLINE type.
 *
 */

public class GraphicObject implements Serializable {

	private static final long serialVersionUID = 3326843407376573432L;

	public static enum AnnotationUnitType {                 // Graphic and BoundingBox

		PIXEL, DISPLAY;

		public static AnnotationUnitType getAnnotationUnitType(String s) {
			AnnotationUnitType aut=AnnotationUnitType.PIXEL;
			if (s==null || s.equals("") || s.equalsIgnoreCase("PIXEL")) {
				aut=AnnotationUnitType.PIXEL;
			} else if (s.equalsIgnoreCase("DISPLAY")) {
				aut=AnnotationUnitType.DISPLAY;
			} 
			return aut;
		}
	}

	public static enum GraphicType {
		
		POINT, POLYLINE, INTERPOLATED, CIRCLE, ELLIPSE;
		
		public static GraphicType getGraphicType(String s) {
			GraphicType gt=GraphicType.POINT;
			if (s==null || s.equals("") || s.equalsIgnoreCase("POINT")) {
				gt=GraphicType.POINT;
			} else if (s.equalsIgnoreCase("POLYLINE")) {
				gt=GraphicType.POLYLINE;
			} else if (s.equalsIgnoreCase("INTERPOLATED")) {
				gt=GraphicType.INTERPOLATED;
			} else if (s.equalsIgnoreCase("CIRCLE")) {
				gt=GraphicType.CIRCLE;
			} else if (s.equalsIgnoreCase("ELLIPSE")) {
				gt=GraphicType.ELLIPSE;
			} 
			return gt;
		}
	}

	// =======================================================================

	String graphicLayer=null;
	AnnotationUnitType annotationUnits=null;
	GraphicType type=null;
	short dimensions=2, numberOfPoints;
	boolean isFilled=false;
	float[] data;
	
	// =======================================================================

	public AnnotationUnitType getAnnotationUnits() {return annotationUnits;}
	public GraphicType getType() {return type;}

	public String getGraphicLayer() {return graphicLayer;}

	public boolean isFilled() {return isFilled;}
	public boolean isMeasurement() {if (type==null) return false; return (type.equals("MEASUREMENT")) ? true : false;}

	public float[] getData() {return data;}

	public short getDimensions() {return dimensions;}
	public short getNumberOfPoints() {return numberOfPoints;}

	public void setAnnotationUnits(AnnotationUnitType annotationUnits) {this.annotationUnits=annotationUnits;}
	public void setData(float[] data) {this.data=data;}
	public void setDimensions(short dimensions) {this.dimensions=dimensions;}
	public void setFilled(boolean isFilled) {this.isFilled=isFilled;}
	public void setGraphicLayer(String graphicLayer) {this.graphicLayer=graphicLayer;}		
	public void setNumberOfPoints(short numberOfPoints) {this.numberOfPoints=numberOfPoints;}
	public void setType(GraphicType type) {this.type=type;}

	public String toString() {return "go["+type+","+annotationUnits+","+graphicLayer+"]";}
}

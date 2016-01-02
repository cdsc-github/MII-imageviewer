/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import imageviewer.model.aim.TextAnnotation;
import imageviewer.model.aim.markup.*;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class AnnotationAIMObjectFactory {
	
	  private static final float AANG=(float)Math.PI/8;
	  private static final int ALEN=10;
	  
		private AnnotationAIMObjectFactory() {}

		public static Object createAIMObject(Annotation a, String graphicLayerName) {
				Shape s=a.getBaseShape();
				switch (a.getAnnotationType()) {
	
					  case TEXT: {
						  Line2D.Double l=(Line2D.Double)s;
						  MultiPoint mp = new MultiPoint();
						  mp.addSpatialCoordinate(new TwoDimensionCoordinate(l.x1, l.y1));
						  TextAnnotation text = new TextAnnotation(a.getText(),mp);
						  return text;
					  }
					  case LINE: {
						  Polyline line = new Polyline();
						  Line2D.Double l=(Line2D.Double)s;
						  line.addSpatialCoordinate(new TwoDimensionCoordinate(l.x1, l.y1));
						  line.addSpatialCoordinate(new TwoDimensionCoordinate(l.x2, l.y2));
						  return line;
					  }
					  case ARROW: {
						  Polyline arrow = new Polyline();
						  GeneralPath gp=(GeneralPath)s;
						  Point2D.Double[] arrowCoords=Annotation.computeArrowCoordinates(gp);
						  float x0=(float)arrowCoords[0].x;
						  float y0=(float)arrowCoords[0].y;
						  float x1=(float)arrowCoords[1].x;
						  float y1=(float)arrowCoords[1].y;
						  float theta=(float)Math.atan((double)(y1-y0)/(double)(x1-x0));
						  float len=(float)Math.sqrt(Math.pow((x1-x0),2)+Math.pow((y1-y0),2));
						  float side=(x1<x0) ? -1 : 1;
						  
						  arrow.addSpatialCoordinate(new TwoDimensionCoordinate(x0, y0));
						  arrow.addSpatialCoordinate(new TwoDimensionCoordinate(x1, y1));
						  arrow.addSpatialCoordinate(new TwoDimensionCoordinate(x1, y1));
						  double xAdjust = (double)(x1-side*ALEN*(float)Math.cos(theta+AANG));
						  double yAdjust = (double)(y1-side*ALEN*(float)Math.sin(theta+AANG));
						  arrow.addSpatialCoordinate(new TwoDimensionCoordinate(xAdjust,yAdjust));
						  arrow.addSpatialCoordinate(new TwoDimensionCoordinate(x1, y1));
						  xAdjust = (double)(x1-side*ALEN*Math.cos(theta-AANG));
						  yAdjust = (double)(y1-side*ALEN*Math.sin(theta-AANG));
						  arrow.addSpatialCoordinate(new TwoDimensionCoordinate(xAdjust,yAdjust));

						  return arrow;
					  }
					  case BOX: {
						  Polyline box = new Polyline();
						  
						  Polygon2D p=(Polygon2D)s;
						  box.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[0], p.ypoints[0]));
						  box.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[1], p.ypoints[1]));
						  box.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[1], p.ypoints[1]));
						  box.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[2], p.ypoints[2]));
						  box.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[2], p.ypoints[2]));
						  box.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[3], p.ypoints[3]));
						  box.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[3], p.ypoints[3]));
						  box.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[0], p.ypoints[0]));
						  
						  return box;
					  }
					  case ELLIPSE: {
						  Ellipse ellip = new Ellipse();
						  Polygon2D p=(Polygon2D)s;
						  
						  ellip.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[0], p.ypoints[0]));
						  ellip.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[1], p.ypoints[1]));
						  ellip.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[2], p.ypoints[2]));
						  ellip.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[3], p.ypoints[3]));
						  
						  return ellip;
					  }
					  case CURVE: break;
					  case POLYLINE: {
						  Polyline polyLine = new Polyline();
						  Polygon2D p=(Polygon2D)s;
						  for (int loop=0; loop<(p.npoints); loop++) {
							  polyLine.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[loop], p.ypoints[loop]));
						  }
						  return polyLine;
					  }
					  case POLYGON: {
						  Polyline polygon = new Polyline();
						  Polygon2D p=(Polygon2D)s;
						  for (int loop=0; loop<(p.npoints); loop++) {
							  polygon.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[loop], p.ypoints[loop]));
						  }
						// Unlike polylines, polygons are closed off
						  polygon.addSpatialCoordinate(new TwoDimensionCoordinate(p.xpoints[0], p.ypoints[0]));
						  return polygon;
					  }
				}
				return null;
		}
		
		public static Annotation createAnnotation(GeometricShape gs) {

			// Try and reverse the logic accordingly based on the factory
			// generation method above.  A polyline can be a polygon, a
			// polyline, a line, an arrow, or a box.

			ArrayList<SpatialCoordinate> coordinates = gs.getSpatialCoordinateCollection();
			
			if(gs instanceof Circle || gs instanceof Ellipse) {
				TwoDimensionCoordinate tdc = (TwoDimensionCoordinate) coordinates.get(0);
				TwoDimensionCoordinate tdc2 = (TwoDimensionCoordinate) coordinates.get(2);
				return Annotation.createEllipse(tdc.getX(),tdc.getY(), tdc2.getX(),tdc2.getY());
			} else if(gs instanceof Polyline) {
				int numPoints=coordinates.size();
				float[] data = null;
				if (numPoints==2) {
					TwoDimensionCoordinate tdc = (TwoDimensionCoordinate) coordinates.get(0);
					TwoDimensionCoordinate tdc2 = (TwoDimensionCoordinate) coordinates.get(1);					
					return Annotation.createLine(tdc.getX(),tdc.getY(), tdc2.getX(),tdc2.getY());
				} else if (numPoints==6) {
					float x0=(float) ((TwoDimensionCoordinate)coordinates.get(0)).getX(); //0
					float y0=(float) ((TwoDimensionCoordinate)coordinates.get(0)).getY(); //1
					float x1=(float) ((TwoDimensionCoordinate)coordinates.get(1)).getX(); //2
					float y1=(float) ((TwoDimensionCoordinate)coordinates.get(1)).getY(); //3
					float x2=(float) ((TwoDimensionCoordinate)coordinates.get(2)).getX(); //4
					float y2=(float) ((TwoDimensionCoordinate)coordinates.get(2)).getY(); //5
					float x3=(float) ((TwoDimensionCoordinate)coordinates.get(3)).getX(); //6
					float y3=(float) ((TwoDimensionCoordinate)coordinates.get(3)).getY(); //7
					float x4=(float) ((TwoDimensionCoordinate)coordinates.get(4)).getX(); //8
					float y4=(float) ((TwoDimensionCoordinate)coordinates.get(4)).getY(); //9
					float x5=(float) ((TwoDimensionCoordinate)coordinates.get(5)).getX(); //10
					float y5=(float) ((TwoDimensionCoordinate)coordinates.get(5)).getY(); //11
					float theta=(float)Math.atan((double)(y1-y0)/(double)(x1-x0));
					float len=(float)Math.sqrt(Math.pow((x1-x0),2)+Math.pow((y1-y0),2));
					float side=(x1<x0) ? -1 : 1;
					if ((x2==x4)&&(y2==y4)&&(x3==(x1-side*ALEN*(float)Math.cos(theta+AANG)))&&(y3==(float)(y1-side*ALEN*(float)Math.sin(theta+AANG)))&&
							(x5==(float)(x1-side*ALEN*Math.cos(theta-AANG)))&&(y5==(float)(y1-side*ALEN*Math.sin(theta-AANG)))) {
						return Annotation.createArrow(x0,y0,x1,y1);
					} else {
						return null;
					}
				} else if (numPoints==8) {
					
					float x0=(float) ((TwoDimensionCoordinate)coordinates.get(0)).getX(); //0
					float y0=(float) ((TwoDimensionCoordinate)coordinates.get(0)).getY(); //1
					float x1=(float) ((TwoDimensionCoordinate)coordinates.get(1)).getX(); //2
					float y1=(float) ((TwoDimensionCoordinate)coordinates.get(1)).getY(); //3
					float x2=(float) ((TwoDimensionCoordinate)coordinates.get(2)).getX(); //4
					float y2=(float) ((TwoDimensionCoordinate)coordinates.get(2)).getY(); //5
					float x3=(float) ((TwoDimensionCoordinate)coordinates.get(3)).getX(); //6
					float y3=(float) ((TwoDimensionCoordinate)coordinates.get(3)).getY(); //7
					float x4=(float) ((TwoDimensionCoordinate)coordinates.get(4)).getX(); //8
					float y4=(float) ((TwoDimensionCoordinate)coordinates.get(4)).getY(); //9
					float x5=(float) ((TwoDimensionCoordinate)coordinates.get(5)).getX(); //10
					float y5=(float) ((TwoDimensionCoordinate)coordinates.get(5)).getY(); //11
					float x6=(float) ((TwoDimensionCoordinate)coordinates.get(6)).getX(); //12
					float y6=(float) ((TwoDimensionCoordinate)coordinates.get(6)).getY(); //13
					float x7=(float) ((TwoDimensionCoordinate)coordinates.get(7)).getX(); //14
					float y7=(float) ((TwoDimensionCoordinate)coordinates.get(7)).getY(); //15
					
					if ((x0==x7)&&(y0==y7)&&(x1==x2)&&(y1==y2)&&(x3==x4)&&(y3==y4)&&(x5==x6)&&(y5==y6)) {
						return Annotation.createBox(x0,y0,x3,y3);
					} else {
						return null;
					}
				}
			}
			return null;
		}

		public static Annotation createAnnotation(TextAnnotation ta) {
			
			ArrayList<SpatialCoordinate> coordinates = ta.getConnectorPoints().getSpatialCoordinateCollection();
			
			TwoDimensionCoordinate tdc = (TwoDimensionCoordinate) coordinates.get(0);
			String text = ta.getText();
			
			return Annotation.createText(tdc.getX(), tdc.getY(), text);
			
		}

}

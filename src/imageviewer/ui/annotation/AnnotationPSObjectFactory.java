/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import imageviewer.model.dicom.ps.GraphicObject;
import imageviewer.model.dicom.ps.GraphicObject.AnnotationUnitType;
import imageviewer.model.dicom.ps.GraphicObject.GraphicType;
import imageviewer.model.dicom.ps.TextObject;

import imageviewer.ui.annotation.Annotation.AnnotationType;

//=======================================================================

public class AnnotationPSObjectFactory {

  private static final float AANG=(float)Math.PI/8;
  private static final int ALEN=10;

	public static Object createObject(Annotation a, String graphicLayerName) {

		if (a.getAnnotationType()==AnnotationType.TEXT) {

		} else {
			GraphicObject go=new GraphicObject();
			go.setGraphicLayer(graphicLayerName);
			go.setAnnotationUnits(AnnotationUnitType.PIXEL);
			go.setFilled(a.isFilled());
			Shape s=a.getBaseShape();
			switch (a.getAnnotationType()) {

			      case LINE: {go.setType(GraphicType.POLYLINE);
										    go.setNumberOfPoints((short)2);
												Line2D.Double l=(Line2D.Double)s;
												go.setData(new float[] {(float)l.x1,(float)l.y1,(float)l.x2,(float)l.y2});
												return go;}
			     case ARROW: {go.setType(GraphicType.POLYLINE);
										    go.setNumberOfPoints((short)6);
												GeneralPath gp=(GeneralPath)s;
												Point2D.Double[] arrowCoords=Annotation.computeArrowCoordinates(gp);
												float x0=(float)arrowCoords[0].x;
												float y0=(float)arrowCoords[0].y;
												float x1=(float)arrowCoords[1].x;
												float y1=(float)arrowCoords[1].y;
												float theta=(float)Math.atan((double)(y1-y0)/(double)(x1-x0));
												float len=(float)Math.sqrt(Math.pow((x1-x0),2)+Math.pow((y1-y0),2));
												float side=(x1<x0) ? -1 : 1;
												go.setData(new float[] {x0,y0,x1,y1,
																								x1,y1,(float)(x1-side*ALEN*(float)Math.cos(theta+AANG)),(float)(y1-side*ALEN*(float)Math.sin(theta+AANG)),
																								x1,y1,(float)(x1-side*ALEN*Math.cos(theta-AANG)),(float)(y1-side*ALEN*Math.sin(theta-AANG))});
												return go;}
			       case BOX: {go.setType(GraphicType.POLYLINE);
					              go.setNumberOfPoints((short)8);
												Polygon2D p=(Polygon2D)s;
												go.setData(new float[] {(float)p.xpoints[0],(float)p.ypoints[0],(float)p.xpoints[1],(float)p.ypoints[1],
																								(float)p.xpoints[1],(float)p.ypoints[1],(float)p.xpoints[2],(float)p.ypoints[2],
																								(float)p.xpoints[2],(float)p.ypoints[2],(float)p.xpoints[3],(float)p.ypoints[3],
																								(float)p.xpoints[3],(float)p.ypoints[3],(float)p.xpoints[0],(float)p.ypoints[0]});
												return go;}
			   case ELLIPSE: {go.setType(GraphicType.ELLIPSE);
					              go.setNumberOfPoints((short)4);
												Polygon2D p=(Polygon2D)s;
												go.setData(new float[] {(float)p.xpoints[0],(float)p.ypoints[0],(float)p.xpoints[1],(float)p.ypoints[1],
																								(float)p.xpoints[2],(float)p.ypoints[2],(float)p.xpoints[3],(float)p.ypoints[3]});
												return go;}
			     case CURVE: break;
			  case POLYLINE: {go.setType(GraphicType.POLYLINE); 
												Polygon2D p=(Polygon2D)s;
												go.setNumberOfPoints((short)(2*p.npoints));
												float[] fPoints=new float[2*p.npoints];
												for (int loop=0; loop<(p.npoints); loop++) {
													fPoints[loop*2]=(float)p.xpoints[loop]; 
													fPoints[(loop*2)+1]=(float)p.ypoints[loop]; 
												}
												go.setData(fPoints);
												return go;}
		     case POLYGON: {go.setType(GraphicType.POLYLINE); 
												Polygon2D p=(Polygon2D)s;
												go.setNumberOfPoints((short)(2*(1+p.npoints)));
												float[] fPoints=new float[2*(1+p.npoints)];
												for (int loop=0; loop<(p.npoints); loop++) {
													fPoints[loop*2]=(float)p.xpoints[loop]; 
													fPoints[(loop*2)+1]=(float)p.ypoints[loop]; 
												}
												fPoints[p.npoints]=(float)p.xpoints[0];            // Unlike polylines, polygons are closed off
												fPoints[p.npoints+1]=(float)p.ypoints[0];
												go.setData(fPoints);
												return go;}
			}
		}
		return null;
	}

	//=======================================================================

	private static Annotation createPolyLineAnnotation(float[] data) {

		return null;
	}

	public static Annotation createAnnotation(GraphicObject go) {

		// Try and reverse the logic accordingly based on the factory
		// generation method above.  A polyline can be a polygon, a
		// polyline, a line, an arrow, or a box.

		GraphicType gt=go.getType();
		if (gt==GraphicType.POLYLINE) {
			int numPoints=go.getNumberOfPoints();
			float[] data=go.getData();
			if (numPoints==2) {
				return Annotation.createLine((double)data[0],(double)data[1],(double)data[2],(double)data[3]);
			} else if (numPoints==6) {
				float x0=data[0];
				float y0=data[1];
				float x1=data[2];
				float y1=data[3];
				float theta=(float)Math.atan((double)(y1-y0)/(double)(x1-x0));
				float len=(float)Math.sqrt(Math.pow((x1-x0),2)+Math.pow((y1-y0),2));
				float side=(x1<x0) ? -1 : 1;
				if ((data[4]==data[8])&&(data[5]==data[9])&&(data[6]==(x1-side*ALEN*(float)Math.cos(theta+AANG)))&&(data[7]==(float)(y1-side*ALEN*(float)Math.sin(theta+AANG)))&&
						(data[10]==(float)(x1-side*ALEN*Math.cos(theta-AANG)))&&(data[11]==(float)(y1-side*ALEN*Math.sin(theta-AANG)))) {
					return Annotation.createArrow(x0,y0,x1,y1);
				} else {
					return createPolyLineAnnotation(data);
				}
			} else if (numPoints==8) {
				if ((data[0]==data[14])&&(data[1]==data[15])&&(data[2]==data[4])&&(data[3]==data[5])&&
						(data[6]==data[8])&&(data[7]==data[9])&&(data[10]==data[12])&&(data[11]==data[13])) {
					return Annotation.createBox((double)data[0],(double)data[1],(double)data[6],(double)data[7]);
				} else {
					return createPolyLineAnnotation(data);
				}
			}
		} else if (gt==GraphicType.ELLIPSE) {
			float[] data=go.getData();
			return Annotation.createEllipse(data[0],data[1],data[2],data[5]);
		}
		return null;
	}

	public static Annotation createAnnotation(TextObject to) {return null;}

	private AnnotationPSObjectFactory() {}

}

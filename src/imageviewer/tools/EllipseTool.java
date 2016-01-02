/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.Polygon2D;

// =======================================================================

public class EllipseTool extends BasicAnnotationTool {

	public EllipseTool() {super();}

	// =======================================================================

	public String getToolName() {return new String("Ellipse");}

	// =======================================================================

	protected Annotation createAnnotation(Point2D.Double p) {return Annotation.createEllipse(p.x,p.y,p.x,p.y);}
	protected Annotation createAnnotation(Point p) {return Annotation.createEllipse(p.x,p.y,p.x,p.y);}
	
	protected void setShapeCoordinate() {

		Polygon2D p=(Polygon2D)a.getBaseShape();

		// Figure out which of the points is the upper-left corner vs. the
		// bottom-right corner. We need to make sure the bounding box is
		// in the needed direction. p0->p1->p2->p3 should start from the
		// top-left and go clockwise.
		
		Point2D.Double p0=null, p1=null, p2=null, p3=null;

		if (startPoint.y<endPoint.y) {
			if (startPoint.x<endPoint.x) {
				p0=startPointTranslated;
				p1=translateToImage(rp,new Point(endPoint.x,startPoint.y));
				p2=endPointTranslated;
				p3=translateToImage(rp,new Point(startPoint.x,endPoint.y));
			} else {
				p0=translateToImage(rp,new Point(endPoint.x,startPoint.y));
				p1=startPointTranslated;
				p2=translateToImage(rp,new Point(startPoint.x,endPoint.y));
				p3=endPointTranslated;
			}
		} else {
			if (startPoint.x<endPoint.x) {
				p0=translateToImage(rp,new Point(startPoint.x,endPoint.y));
				p1=endPointTranslated;
				p2=translateToImage(rp,new Point(endPoint.x,startPoint.y));
				p3=startPointTranslated;
			} else {
				p0=endPointTranslated;
				p1=translateToImage(rp,new Point(startPoint.x,endPoint.y));
				p2=startPointTranslated;
				p3=translateToImage(rp,new Point(endPoint.x,startPoint.y));
			}
		}

		p.xpoints[0]=p0.x; p.ypoints[0]=p0.y;
		p.xpoints[1]=p1.x; p.ypoints[1]=p1.y;
		p.xpoints[2]=p2.x; p.ypoints[2]=p2.y;
		p.xpoints[3]=p3.x; p.ypoints[3]=p3.y;
	}
}

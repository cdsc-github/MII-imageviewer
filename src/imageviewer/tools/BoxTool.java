/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Point;
import java.awt.geom.Point2D;

import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.Polygon2D;

// =======================================================================

public class BoxTool extends BasicAnnotationTool {

	public BoxTool() {super();}

	// =======================================================================

	public String getToolName() {return new String("Box");}

	// =======================================================================

	protected Annotation createAnnotation(Point2D.Double p) {return Annotation.createBox(p.x,p.y,p.x,p.y);}
	protected Annotation createAnnotation(Point p) {return Annotation.createBox(p.x,p.y,p.x,p.y);}
	
	protected void setShapeCoordinate() {

		Polygon2D p=(Polygon2D)a.getBaseShape();
		Point2D.Double p0=startPointTranslated;
		Point2D.Double p1=translateToImage(rp,new Point(endPoint.x,startPoint.y));
		Point2D.Double p2=endPointTranslated;
		Point2D.Double p3=translateToImage(rp,new Point(startPoint.x,endPoint.y));

		p.xpoints[0]=p0.x; p.ypoints[0]=p0.y;
		p.xpoints[1]=p1.x; p.ypoints[1]=p1.y;
		p.xpoints[2]=p2.x; p.ypoints[2]=p2.y;
		p.xpoints[3]=p3.x; p.ypoints[3]=p3.y;
	}
}

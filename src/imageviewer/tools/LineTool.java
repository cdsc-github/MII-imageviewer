/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Point;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import imageviewer.ui.annotation.Annotation;

// =======================================================================

public class LineTool extends BasicAnnotationTool {

	public LineTool() {super();}

	// =======================================================================

	public String getToolName() {return new String("Line");}

	// =======================================================================

	protected Annotation createAnnotation(Point2D.Double p) {return Annotation.createLine(p.x,p.y,p.x,p.y);}
	protected Annotation createAnnotation(Point p) {return Annotation.createLine(p.x,p.y,p.x,p.y);}

	protected void setShapeCoordinate() {Line2D.Double l=(Line2D.Double)a.getBaseShape(); l.setLine(l.getX1(),l.getY1(),endPointTranslated.x,endPointTranslated.y);}
}

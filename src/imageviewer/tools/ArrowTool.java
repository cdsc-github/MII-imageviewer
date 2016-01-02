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

// =======================================================================

public class ArrowTool extends BasicAnnotationTool {

	public ArrowTool() {super();}

	// =======================================================================
 
	public String getToolName() {return new String("Arrow");}

	// =======================================================================

	protected Annotation createAnnotation(Point2D.Double p) {return Annotation.createArrow(p.x,p.y,p.x,p.y);}
	protected Annotation createAnnotation(Point p) {return Annotation.createArrow(p.x,p.y,p.x,p.y);}

	protected void setShapeCoordinate() {a.setBaseShape(Annotation.createArrowLine(startPointTranslated.x,startPointTranslated.y,endPointTranslated.x,endPointTranslated.y));}
}

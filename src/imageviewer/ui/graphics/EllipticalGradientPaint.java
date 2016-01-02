/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.graphics;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.image.ColorModel;

// =======================================================================

public class EllipticalGradientPaint implements Paint {

	protected Point2D mPoint, mRadius;
	protected Color mPointColor, mBackgroundColor;
	
	public EllipticalGradientPaint(double x, double y, Color pointColor, Point2D radius, Color backgroundColor) {

		if (radius.distance(0,0)<=0) throw new IllegalArgumentException("Radius must be greater than 0.");
		mPoint=new Point2D.Double(x,y);
		mPointColor=pointColor;
		mRadius=radius;
		mBackgroundColor=backgroundColor;
	}

	// =======================================================================
 
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		Point2D transformedPoint=xform.transform(mPoint,null);
		Point2D transformedRadius=xform.deltaTransform(mRadius,null);
		return new EllipticalGradientContext(transformedPoint,mPointColor,transformedRadius,mBackgroundColor);
	}

	// =======================================================================
 
	public int getTransparency() {
		int a1=mPointColor.getAlpha();
		int a2=mBackgroundColor.getAlpha();
		return (((a1&a2)==0xff) ? OPAQUE : TRANSLUCENT);
	}
}
 

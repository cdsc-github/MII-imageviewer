/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

//=======================================================================
// Transformation methods for the location of the control point, in
// image world vs. the current viewport.

public class RenderingUtil {

	public static Point2D.Double translateToViewport(RenderingProperties rp, Point2D.Double p) {

		double scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
		double translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
		double translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
		int sourceWidth=((Integer)rp.getProperty(RenderingProperties.SOURCE_WIDTH)).intValue();
		int sourceHeight=((Integer)rp.getProperty(RenderingProperties.SOURCE_HEIGHT)).intValue();
		float rotationAngle=((Float)rp.getProperty(RenderingProperties.ROTATION)).floatValue();	
		boolean isVFlip=((Boolean)rp.getProperty(RenderingProperties.VERTICAL_FLIP)).booleanValue();	
		boolean isHFlip=((Boolean)rp.getProperty(RenderingProperties.HORIZONTAL_FLIP)).booleanValue();	
		double vFlip=(isVFlip) ? -1 : 1;
		double hFlip=(isHFlip) ? -1 : 1;
		double yTranslate=(isVFlip) ? -sourceHeight : 0;
		double xTranslate=(isHFlip) ? -sourceWidth : 0;
		
		AffineTransform at=new AffineTransform();
		at.translate(translateX,translateY);
		at.scale(scale,scale);
		at.rotate((double)rotationAngle,sourceWidth/2,sourceHeight/2);
		at.scale(hFlip,vFlip);
		at.translate(xTranslate,yTranslate);
		
		Point2D.Double viewportCoordinates=new Point2D.Double();
		at.transform(p,viewportCoordinates);
		return viewportCoordinates;
	}

	//=======================================================================
	
	public static Point2D.Double translateToImage(RenderingProperties rp, Point2D.Double p) {

		double scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
		double translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
		double translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
		float rotationAngle=((Float)rp.getProperty(RenderingProperties.ROTATION)).floatValue();		
		int sourceWidth=((Integer)rp.getProperty(RenderingProperties.SOURCE_WIDTH)).intValue();
		int sourceHeight=((Integer)rp.getProperty(RenderingProperties.SOURCE_HEIGHT)).intValue();
		boolean isVFlip=((Boolean)rp.getProperty(RenderingProperties.VERTICAL_FLIP)).booleanValue();	
		boolean isHFlip=((Boolean)rp.getProperty(RenderingProperties.HORIZONTAL_FLIP)).booleanValue();	
		double vFlip=(isVFlip) ? -1 : 1;
		double hFlip=(isHFlip) ? -1 : 1;
		double yTranslate=(isVFlip) ? -sourceHeight : 0;
		double xTranslate=(isHFlip) ? -sourceWidth : 0;

		double pX=(double)((p.x-translateX)/scale);
		double pY=(double)((p.y-translateY)/scale);
		AffineTransform matrix=AffineTransform.getRotateInstance((double)rotationAngle,sourceWidth/2,sourceHeight/2);
		matrix.scale(hFlip,vFlip);
		matrix.translate(xTranslate,yTranslate);
		Point2D.Double p1=new Point2D.Double(pX,pY);
		Point2D.Double p2=new Point2D.Double();
		try {matrix.inverseTransform(p1,p2);} catch (Exception exc) {}
		return new Point2D.Double(p2.x,p2.y);
	}
}

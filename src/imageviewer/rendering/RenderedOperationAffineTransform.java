/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering;

import java.awt.geom.AffineTransform;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

public class RenderedOperationAffineTransform implements RenderedOperation {

	public static final String OPERATION_NAME=new String("AFFINE_TRANSFORM");
	public static final InterpolationBilinear BILINEAR_INTERP=new InterpolationBilinear();
	public static final InterpolationBicubic BICUBIC_INTERP=new InterpolationBicubic(2);
	
	public RenderedOperationAffineTransform() {}

	// =======================================================================

	public String[] getListenerProperties() {return new String[] {RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y};}

	public String getRenderedOperationName() {return OPERATION_NAME;}

	public RenderedImage performOperation(RenderedImage source, RenderingProperties rp) {

		if (source==null) return null;
		double scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
		double shearX=0.0, shearY=0.0;		
		double translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
		double translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
		AffineTransform at=new AffineTransform(scale,shearY,shearX,scale,translateX,translateY);
		if (at.isIdentity()) return source;
		ParameterBlock pb=new ParameterBlock();
		pb.addSource(source);
		pb.add(at);
		//if (scale<=1) pb.add(BILINEAR_INTERP);
		//if (scale>1) 
		pb.add(BICUBIC_INTERP);
		RenderedOp image=JAI.create("affine",pb);
		pb=null;
		at=null;
		return image;
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.processing;

import java.awt.geom.AffineTransform;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.InterpolationBicubic;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import imageviewer.model.Image;

// =======================================================================

public class TransformedImageProcessor implements ImageProcessor {

	private static final InterpolationBicubic INTERPOLATION=new InterpolationBicubic(8);

	double scaleX=1.0, scaleY=1.0, shearX=0.0, shearY=0.0;
	double translateX=0.0, translateY=0.0, rotation=0.0;
	
	public TransformedImageProcessor() {}

	// =======================================================================

	public double getScaleX() {return scaleX;}
	public double getScaleY() {return scaleY;}
	public double getShearX() {return shearX;}
	public double getShearY() {return shearY;}
	public double getTranslateX() {return translateX;}
	public double getTranslateY() {return translateY;}
	public double getRotation() {return rotation;}

	public void setScale(double x) {scaleX=x; scaleY=x;}

	public void setScaleX(double x) {scaleX=x;}
	public void setScaleY(double x) {scaleY=x;}
	public void setShearX(double x) {shearX=x;}
	public void setShearY(double x) {shearY=x;}
	public void setTranslateX(double x) {translateX=x;}
	public void setTranslateY(double x) {translateY=x;}
	public void setRotation(double x) {rotation=x;}

	// =======================================================================

	public AffineTransform getAffineTransform() {

		AffineTransform at=new AffineTransform(scaleX,shearY,shearX,scaleY,translateX,translateY);
		at.rotate(rotation);
		return at;
	}

	public PlanarImage process(Image[] sources) {

		AffineTransform at=getAffineTransform();
		ParameterBlock pb=new ParameterBlock();
		pb.addSource(sources[0].getRenderedImage());
		pb.add(at);
		pb.add(INTERPOLATION);
		return JAI.create("affine",pb);
	}
}

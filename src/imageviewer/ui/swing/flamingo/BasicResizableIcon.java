/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;

import java.io.File;

import javax.imageio.ImageIO;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.jvnet.flamingo.common.icon.ResizableIcon;

import imageviewer.ui.swing.GrayFilter;

// =======================================================================

public class BasicResizableIcon implements ResizableIcon {

	private static final Interpolation INTERP=Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2);

	String filename=null, alternateFile=null;
	BufferedImage sourceImage=null, sourceAlternateImage=null, currentImage=null;
	Dimension originalDimension=null, currentDimension=null;
	boolean isDisabled=false;

	public BasicResizableIcon(String filename) {this(filename,null,false);}
	public BasicResizableIcon(String filename, boolean isDisabled) {this(filename,null,isDisabled);}
	public BasicResizableIcon(String filename, String alternateFile) {this(filename,alternateFile,false);}

	public BasicResizableIcon(String filename, String alternateFile, boolean isDisabled) {

		try {
			this.filename=filename;
			this.alternateFile=alternateFile;
			this.isDisabled=isDisabled;
			sourceImage=ImageIO.read(new File(filename));
			if (alternateFile!=null) sourceAlternateImage=ImageIO.read(new File(alternateFile));
			if (isDisabled) {
				BufferedImage bi=createGrayscale(sourceImage);
				sourceImage.flush();
				sourceImage=bi;
				if (sourceAlternateImage!=null) {
					bi=createGrayscale(sourceAlternateImage);
					sourceAlternateImage.flush();
					sourceAlternateImage=bi;
				}
			}
			originalDimension=new Dimension(sourceImage.getWidth(),sourceImage.getHeight());
			currentDimension=(Dimension)originalDimension.clone();
			currentImage=sourceImage;
		} catch (Exception exc) {
			System.err.println("Error reading file: "+filename);
			exc.printStackTrace();
		}
	}

	// =======================================================================

	private BufferedImage createGrayscale(BufferedImage source) {

		Image i=GrayFilter.createDisabledImage(source);
		BufferedImage bi=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics g=bi.getGraphics();
		g.drawImage(i,0,0,null);				
		g.dispose();
		return bi;
	}

	// =======================================================================

	private void rescaleImage() {

		BufferedImage bi=(sourceAlternateImage==null) ? sourceImage : ((Math.abs(currentDimension.width-sourceImage.getWidth())<Math.abs(currentDimension.width-sourceAlternateImage.getWidth())) ?
																																	 sourceImage : sourceAlternateImage);
		double scaleX=(double)currentDimension.width/(double)bi.getWidth();
		double scaleY=(double)currentDimension.height/(double)bi.getHeight();
		if ((scaleX==1)&&(scaleY==1)) currentImage=bi;
		double scale=Math.min(scaleX,scaleY);
		ParameterBlock pb=new ParameterBlock();
		pb.addSource(bi);
		pb.add(AffineTransform.getScaleInstance(scale,scale));
		pb.add(INTERP);
		PlanarImage pi=JAI.create("affine",pb);
		if (currentImage!=null) currentImage.flush();
		currentImage=pi.getAsBufferedImage();
		pi.dispose();
		pi=null;
	}

	// =======================================================================

	public void setDimension(Dimension newDimension) {currentDimension=newDimension; rescaleImage();}
	public void setHeight(int height) {currentDimension.height=height; currentDimension.width=(int)Math.round(originalDimension.width*height/originalDimension.height); rescaleImage();}
	public void setWidth(int width) {currentDimension.width=width; currentDimension.height=(int)Math.round(originalDimension.height*width/originalDimension.width); rescaleImage();}
	public void revertToOriginalDimension() {currentDimension=(Dimension)originalDimension.clone(); rescaleImage();}

	public int getIconHeight() {return currentDimension.height;}
	public int getIconWidth() {return currentDimension.width;}

	// =======================================================================

	public Object clone() {return new BasicResizableIcon(filename,alternateFile,isDisabled);}

	protected void finalize() {if (currentImage!=null) currentImage.flush(); if (sourceImage!=null) sourceImage.flush();}

	// =======================================================================

	public void paintIcon(Component c, Graphics g, int x, int y) {

		Graphics2D g2=(Graphics2D)g;
		Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(currentImage,null,x,y);
	}
}

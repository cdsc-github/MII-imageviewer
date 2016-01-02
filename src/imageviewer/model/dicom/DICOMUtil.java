/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import java.awt.geom.AffineTransform;
import java.io.File;

import java.util.ArrayList;

import javax.imageio.ImageIO;

import imageviewer.model.Image;
import imageviewer.model.ImageProperties;
import imageviewer.model.ImageSequence;

import imageviewer.rendering.ImagePipelineRenderer;
import imageviewer.rendering.RenderingOpPipeline;
import imageviewer.rendering.RenderingOpPipelineFactory;
import imageviewer.rendering.RenderingProperties;

import imageviewer.rendering.wl.DefaultWindowLevelManager;
import imageviewer.rendering.wl.WindowLevel;

// =======================================================================

public class DICOMUtil {

	// =======================================================================
	// Convenience method to generate an ordered set of bufferedImages
	// from a given directory.  The consequences of this image set are
	// up to the user (i.e., memory not managed as part of the image
	// cache).

	public static ArrayList<BufferedImage> generateBufferedImages(String dir) {

		DICOMReader dr=new DICOMReader();
		ArrayList<BufferedImage> imageArray=new ArrayList<BufferedImage>();
		ArrayList<DICOMImageStudy> al=dr.organizeByStudy(dr.readImages(dir,true));
		AffineTransform at=new AffineTransform();
		for (DICOMImageStudy study : al) {
			ArrayList<ImageSequence> series=study.getGroups();
			for (ImageSequence is : series) {
				ArrayList<Image> images=is.getSequence();
				Image i0=images.get(0);
				WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(i0);				
				RenderingProperties rp=new RenderingProperties();
				rp.setProperties(new String[] {RenderingProperties.HORIZONTAL_FLIP,RenderingProperties.ROTATION,RenderingProperties.SCALE,
																			 RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,RenderingProperties.WINDOW_LEVEL,
																			 RenderingProperties.MAX_PIXEL,RenderingProperties.SOURCE_WIDTH,RenderingProperties.SOURCE_HEIGHT},
					                             new Object[] {new Boolean(false),new Float(0),new Double(1),new Double(0),new Double(0),wl,
																										 new Integer(i0.getMaxPixelValue()),new Integer(i0.getWidth()),new Integer(i0.getHeight())});
				ImagePipelineRenderer ipr=new ImagePipelineRenderer(RenderingOpPipelineFactory.create(),rp);
				for (Image i : images) {
					BufferedImage bi=new BufferedImage(i.getWidth(),i.getHeight(),BufferedImage.TYPE_INT_RGB);
					RenderedImage source=i.getRenderedImage();
					ipr.setSource(source);
					RenderedImage ri=ipr.getRenderedImage();
					Graphics2D g2=bi.createGraphics(); 
					if (ri!=null) {
						g2.drawRenderedImage(ri,at);
						imageArray.add(bi);
					}
					ipr.flush();
				}
				ipr.doCleanup();
				ipr=null;
			}
		}
		return imageArray;
	}

	// =======================================================================
	// Simple routine to spit out the images to a new file format.

	public static void generateImageFiles(String inputDir, String outputDir, String imageFormat) {

		DICOMReader dr=new DICOMReader();
		ArrayList<DICOMImageStudy> al=dr.organizeByStudy(dr.readImages(inputDir,true));
		try {
			File outputDirFile=new File(outputDir);
			outputDirFile.mkdir();
			int count=0;
			for (DICOMImageStudy study : al) {
				ArrayList<ImageSequence> series=study.getGroups();
				for (ImageSequence is : series) {
					ArrayList<Image> images=is.getSequence();
					Image i0=images.get(0);
					WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(i0);				
					RenderingProperties rp=new RenderingProperties();
					rp.setProperties(new String[] {RenderingProperties.HORIZONTAL_FLIP,RenderingProperties.ROTATION,RenderingProperties.SCALE,
																				 RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,RenderingProperties.WINDOW_LEVEL,
																				 RenderingProperties.MAX_PIXEL,RenderingProperties.SOURCE_WIDTH,RenderingProperties.SOURCE_HEIGHT},
					                               new Object[] {new Boolean(false),new Float(0),new Double(1),new Double(0),new Double(0),wl,
																											 new Integer(i0.getMaxPixelValue()),new Integer(i0.getWidth()),new Integer(i0.getHeight())});
					ImagePipelineRenderer ipr=new ImagePipelineRenderer(RenderingOpPipelineFactory.create(),rp);
					for (Image i : images) {
						ipr.setSource(i.getRenderedImage());
						RenderedImage ri=ipr.getRenderedImage();
						File f=new File(outputDir+"/"+count+"."+imageFormat);
						ImageIO.write(ri,imageFormat,f);
						count++;
					}
					ipr.flush();
					ipr.doCleanup();
					ipr=null;
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	// =======================================================================

	private DICOMUtil() {}

	// =======================================================================

	public static void main(String args[]) {

		if (args.length==1) System.err.println(DICOMUtil.generateBufferedImages(args[0]));
		if (args.length==3) DICOMUtil.generateImageFiles(args[0],args[1],args[2]);
	}
}

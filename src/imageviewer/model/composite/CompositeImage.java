/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.composite;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.CompositeDescriptor;

import imageviewer.model.Image;
import imageviewer.model.DataLayer;
import imageviewer.model.PresentationState;
import imageviewer.ui.image.BasicImagePanel;
import imageviewer.util.ImageCache;

// =======================================================================

public class CompositeImage implements Image {

	protected PlanarImage compositedImage=null;
	protected CompositeImageDescriptor[] sources=null;
	protected int maxPixelValue=-1;

	public CompositeImage() {}
	public CompositeImage(CompositeImageDescriptor[] sources) {this.sources=sources;}

	// =======================================================================

	public Image[] getSources() {

		Image[] rawSources=new Image[sources.length];
		for (int loop=0; loop<sources.length; loop++) rawSources[loop]=sources[loop].getSource();
		return rawSources;
	}

	public void setSources(CompositeImageDescriptor[] x) {sources=x;}

	// =======================================================================

	public int getWidth() {if (compositedImage==null) process(); return compositedImage.getWidth();}
	public int getHeight() {if (compositedImage==null) process(); return compositedImage.getHeight();}
	public int getBitDepth() {if (compositedImage==null) process(); return compositedImage.getColorModel().getComponentSize(0);}

	public RenderedImage getRenderedImage() {if (compositedImage==null) process(); return compositedImage;}         
	public Raster getData() {if (compositedImage==null) process(); return compositedImage.getData();}                                   

	public boolean isUnloadable() {return true;}                     
	public boolean isLoaded() {return (compositedImage!=null) ? true : false;}

	public void load() {if (compositedImage!=null) return; process(); }                                
	public void unload() {if (compositedImage==null) return; compositedImage.dispose(); compositedImage=null;}

	public String getKey() {

		StringBuffer sb=new StringBuffer("_COMPOSITED");
		for (int loop=0; loop<sources.length; loop++) sb.append("|"+sources[loop].getSource().getKey());
		return sb.toString();
	}

	// =======================================================================
	// Compute a simple constant image with the same number of bands as the
	// source image, given the specified alpha value.

	private PlanarImage computeAlphaImage(RenderedImage source, float targetAlpha) {

		int numBands=source.getSampleModel().getNumBands();
		Byte[] bandValues=new Byte[numBands];
		for (int i=0 ; i<numBands; i++) bandValues[i]=new Byte((byte)(targetAlpha*255));
		ParameterBlock pb=new ParameterBlock();
		pb.add((float)source.getWidth());
		pb.add((float)source.getHeight());
		pb.add(bandValues);
		return JAI.create("constant",pb,null);
	}

	// =======================================================================
	// Composite the images together.  This mechanism is way faster and
	// more memory efficient than using the graphics component and
	// compositing buffered images.

	private void process() {

		if (compositedImage!=null) {
			compositedImage.dispose(); 
			compositedImage=null;
		} 

		// First, set up the images so that the first primary image in the
		// first source, and then the next image overlaid are going to be
		// composited together. Importantly, the overlay image must be of
		// the same number of bands and the same dimensions as the source
		// image, so crop the image accordingly.

		CompositeImageDescriptor cid1=sources[0];
		CompositeImageDescriptor cid2=sources[1];
		RenderedImage source=cid1.getSource().getRenderedImage();

		ParameterBlock pb=new ParameterBlock();
		pb.addSource(cid2.getSource().getRenderedImage());
		pb.add(0.0f);
		pb.add(0.0f);
		pb.add((float)source.getWidth());
		pb.add((float)source.getHeight());
		PlanarImage overlay=JAI.create("crop",pb,null);

		// Generate the alpha bands for the composition.  The alpha values
		// are given by the compositeImageDescriptor; constant alpha
		// images are then generated.

		PlanarImage sourceAlpha=computeAlphaImage(source,cid1.getAlpha());
		PlanarImage overlayAlpha=computeAlphaImage(overlay,cid2.getAlpha());

		// Use the JAI composite operation to join the two images
		// together. Dispose of the temporary planar images.

		pb=new ParameterBlock();
    pb.addSource(overlay);
    pb.addSource(source);
    pb.add(overlayAlpha);
    pb.add(sourceAlpha);
    pb.add(new Boolean(false));
    pb.add(CompositeDescriptor.NO_DESTINATION_ALPHA);
    compositedImage=JAI.create("composite",pb,null);

		overlay.dispose();
		sourceAlpha.dispose();
		overlayAlpha.dispose();

		// Handle any additional images that are composited into this
		// single image. Note that the first composited image now has an
		// alpha of 1, and is looped through accordingly -- the newly
		// generated image of each iteration becomes the next "base"
		// image. Reuse any temporary variables.

		for (int loop=2; loop<sources.length; loop++) {
			CompositeImageDescriptor cid=sources[loop];
			pb=new ParameterBlock();
			pb.addSource(cid.getSource().getRenderedImage());
			pb.add(0.0f);
			pb.add(0.0f);
			pb.add((float)compositedImage.getWidth());
			pb.add((float)compositedImage.getHeight());
			overlay=JAI.create("crop",pb,null);
			overlayAlpha=computeAlphaImage(overlay,cid.getAlpha());
			pb=new ParameterBlock();
			pb.addSource(overlay);
			pb.addSource(compositedImage);
			pb.add(overlayAlpha);
			pb.add(null);
			pb.add(new Boolean(false));
			pb.add(CompositeDescriptor.NO_DESTINATION_ALPHA);
			PlanarImage oldImage=compositedImage;
			compositedImage=JAI.create("composite",pb,null);
			overlay.dispose();
			overlayAlpha.dispose();
			oldImage.dispose();
			oldImage=null;
		}

		// Set all the temporaries to null to make sure the system garbage
		// collects, and add the image to the cache.

		overlay=null;
		sourceAlpha=null;
		overlayAlpha=null;

		ImageCache.getDefaultImageCache().add(this);
	}

	// =======================================================================
	
	public int getMaxPixelValue() {
		
		if (maxPixelValue!=-1) return maxPixelValue;
		if (compositedImage==null) process();
		try {
			PlanarImage pi=JAI.create("extrema",compositedImage);
			double[] maximums=(double[])pi.getProperty("maximum");
			if ((maximums!=null)&&(maximums.length>0)) {
				maxPixelValue=(int)maximums[0];
				return (int)(maximums[0]);
			}
			pi=null;
		} catch (ArrayIndexOutOfBoundsException exc) {
			exc.printStackTrace();
		}
		return maxPixelValue;
	}

	// =======================================================================

	protected void finalize() throws Throwable {

		super.finalize(); 
		ImageCache.getDefaultImageCache().remove(this); 
		unload();
		sources=null;
	}

	public Histogram getHistogram() {return null;}	                 // TODO Auto-generated method stub

	public ArrayList<DataLayer> getDataLayers() {

		if (sources==null) return null;
		CompositeImageDescriptor cid=sources[0];
		return cid.getSource().getDataLayers();
	}       

	public void setDataLayers(ArrayList<DataLayer> x) {}
	public void addDataLayer(DataLayer x) {}
	public void removeDataLayer(DataLayer x) {}
	public DataLayer findDataLayer(String name) {return null;}

	public double[] getPixelDimensions() {return null;}
	public double getImageSliceThickness() {return 0;}

	public Hashtable<String,Object> getProperties() {return null;}

	// =======================================================================

	public String getPropertyDescription(String property) {return null;}
	public String getAssocKey() {return getKey();}

	// =======================================================================

	public boolean canGeneratePresentationState() {return false;}
	public PresentationState generatePresentationState(BasicImagePanel bip, String username, String psDescription, String psLabel) {return null;}
}


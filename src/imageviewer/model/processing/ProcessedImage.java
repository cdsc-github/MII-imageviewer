/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.processing;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import imageviewer.model.DataLayer;
import imageviewer.model.Image;
import imageviewer.model.PresentationState;
import imageviewer.ui.image.BasicImagePanel;
import imageviewer.util.ImageCache;

// =======================================================================

public class ProcessedImage implements Image {

	protected PlanarImage processedImage=null;
	protected ImageProcessor ip=null;
	protected Image[] sources=null;
	protected int maxPixelValue=-1;

	public ProcessedImage() {}
	public ProcessedImage(ImageProcessor ip, Image i) {this.ip=ip; sources=new Image[1]; sources[0]=i; process();}
	public ProcessedImage(ImageProcessor ip, Image[] sources) {this.ip=ip; this.sources=sources;}

	// =======================================================================

	public Image[] getSources() {return sources;}
	public ImageProcessor getImageProcessor() {return ip;}

	public void setSources(Image[] x) {sources=x;}
	public void setImageProcessor(ImageProcessor x) {ip=x;}

	// =======================================================================

	public int getWidth() {if (processedImage==null) process(); return processedImage.getWidth();}
	public int getHeight() {if (processedImage==null) process(); return processedImage.getHeight();}
	public int getBitDepth() {if (processedImage==null) process(); return processedImage.getColorModel().getComponentSize(0);}

	public RenderedImage getRenderedImage() {if (processedImage==null) process(); return processedImage;}         
	public Raster getData() {if (processedImage==null) process(); return processedImage.getData();}                                   

	public boolean isUnloadable() {return true;}                     
	public boolean isLoaded() {return (processedImage!=null) ? true : false;}

	public void load() {if (processedImage!=null) return; process(); }                                
	public void unload() {if (processedImage==null) return; processedImage.dispose(); processedImage=null;}

	public String getKey() {

		StringBuffer sb=new StringBuffer("_PROCESSED");
		for (int loop=0; loop<sources.length; loop++) sb.append("|"+sources[loop].getKey());
		return sb.toString();
	}

	// =======================================================================

	private void process() {

		if (processedImage!=null) {
			processedImage.dispose(); 
			processedImage=null;
		} 
		processedImage=ip.process(sources); 
		ImageCache.getDefaultImageCache().add(this);
	}

	// =======================================================================
	
	public int getMaxPixelValue() {
		
		if (maxPixelValue!=-1) return maxPixelValue;
		if (processedImage==null) process();
		try {
			PlanarImage pi=JAI.create("extrema",processedImage);
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
		return sources[0].getDataLayers();
	}      

	public void setDataLayers(ArrayList<DataLayer> x) {}
	public void addDataLayer(DataLayer x) {}
	public void removeDataLayer(DataLayer x) {}
	public DataLayer findDataLayer(String name) {return null;}

	// =======================================================================

	public double[] getPixelDimensions() {return null;}
	public double getImageSliceThickness() {return 0;}

	// =======================================================================

	public Hashtable<String,Object> getProperties() {return null;}
	public String getPropertyDescription(String property) {return null;}

	public String getAssocKey() {return getKey();}

	// =======================================================================

	public boolean canGeneratePresentationState() {return false;}
	public PresentationState generatePresentationState(BasicImagePanel bip, String username, String psDescription, String psLabel) {return null;}
}

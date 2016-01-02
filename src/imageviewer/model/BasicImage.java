/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.awt.image.renderable.ParameterBlock;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;

import imageviewer.ui.image.BasicImagePanel;

// =======================================================================
// A simple implementation for the Image interface, leveraging JAI.
// Primarily used for the classes such as DICOMImage, AnalyzeImage,
// and other basic implementations.

public abstract class BasicImage implements Image {

	protected TiledImage ti=null;
	protected Histogram histogram=null;
	protected int maxPixelValue=-1;

	protected ArrayList<DataLayer> dataLayers=new ArrayList<DataLayer>();

	// =======================================================================
	/**
	 * Set the maximum pixel value.
	 * @param x
	 */
	public void setMaxPixelValue(int x) {maxPixelValue=x;}

	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getMaxPixelValue()
	 */
	public int getMaxPixelValue() {
		
		if (maxPixelValue!=-1) return maxPixelValue;
		if (ti==null) load();
		try {
			PlanarImage pi=JAI.create("extrema",ti);
			double[] maximums=(double[])pi.getProperty("maximum");
			if ((maximums!=null)&&(maximums.length>0)) {
				maxPixelValue=1+(int)maximums[0];
				return 1+(int)(maximums[0]);
			}
			pi=null;
		} catch (ArrayIndexOutOfBoundsException exc) {
			exc.printStackTrace();
		}
		return maxPixelValue;
	}

	// =======================================================================
	/**
	 * Generates a histogram using JAI operations. Starts by determining
	 * what the extrema is for this image so that the maximum value can
	 * be determined.
	 * 
	 * @return a Histrogram with bins counting pixel values, or null if the image cannot be loaded correctly.
	 */
	public Histogram getHistogram() {

		if (histogram!=null) return histogram;
		if (ti==null) load();
		if (ti==null) return null;
		PlanarImage pi=JAI.create("extrema",ti,null);
		double[] minimums=(double[]) pi.getProperty("minimum");
		double[] maximums=(double[]) pi.getProperty("maximum");
		int[] bins={(int) (maximums[0]-minimums[0])};

		ParameterBlock pb=new ParameterBlock();
		pb.addSource(ti);
		pb.add(null);
		pb.add(1);
		pb.add(1);
		pb.add(bins);
		pb.add(minimums);
		pb.add(maximums);

		PlanarImage histogramImage=(PlanarImage)JAI.create("histogram",pb);
		histogram=(Histogram)histogramImage.getProperty("histogram");
		pi=null;
		histogramImage=null;
		return histogram;
	}

	// =======================================================================

	public void setDataLayers(ArrayList<DataLayer> x) {dataLayers=x;}
	public void addDataLayer(DataLayer x) {dataLayers.add(x);}
	public void removeDataLayer(DataLayer x) {dataLayers.remove(x);}

	public ArrayList<DataLayer> getDataLayers() {return dataLayers;}

	public DataLayer findDataLayer(String name) {

		for (int loop=0, n=dataLayers.size(); loop<n; loop++) {
			DataLayer dl=dataLayers.get(loop);
			if (name.equals(dl.getName())) return dl;
		}
		return null;
	}

	// =======================================================================

	public boolean canGeneratePresentationState() {return false;}
	public PresentationState generatePresentationState(BasicImagePanel bip, String username, String psDescription, String psLabel) {return null;}

	// =======================================================================

	public Hashtable<String,Object> getProperties() {return null;}
	public String getPropertyDescription(String property) {return null;}

	public double getImageSliceThickness() {return 0;}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.media.jai.Histogram;

import imageviewer.ui.image.BasicImagePanel;

/**
 * The base Image interface in our model.  
 */
public interface Image extends ImageProperties {

	/**
	 * Get width of the image.
	 * 
	 * @return
	 */
	public int getWidth();
	
	/**
	 * Get image height.
	 * 
	 * @return
	 */
	public int getHeight();
	
	/**
	 * Reflects the actual maximal value associated with the image.
	 * 
	 * @return
	 */
	public int getMaxPixelValue();                     
	
	/**
	 * Number of bits used to represent maximum value in image.
	 * 
	 * @return
	 */
	public int getBitDepth();                          

	/**
	 * Get an awt RenderedImage version of this image for display.
	 * 
	 * @return
	 */
	public RenderedImage getRenderedImage();           
	
	/**
	 * Get the underlying raw data associated with the image.
	 * 
	 * @return
	 */
	public Raster getData();                           

	/**
	 * Can the image be loaded/unloaded on demand?.
	 * 
	 * @return
	 */
	public boolean isUnloadable();                      
		
	/**
	 * Is the image actually loaded at the moment?
	 * 
	 * @return
	 */
	public boolean isLoaded();                          

	/**
	 * Load the image if it's not loaded and supports the unloadable parameter 
	 */
	public void load();                                
	
	/**
	 * Unload the image if it's not loaded and supports the unloadable parameter
	 */
	public void unload();                              

	/**
	 * Return a list of images that contribute to this given image; null if it is the source
	 * 
	 * @return
	 */
	public Image[] getSources();                       

	/**
	 * Get key for image.
	 * 
	 * @return
	 */
	public String getKey();

	public Histogram getHistogram();

	// =======================================================================

	public ArrayList<DataLayer> getDataLayers();
	public DataLayer findDataLayer(String name);

	public void setDataLayers(ArrayList<DataLayer> x);
	public void addDataLayer(DataLayer x);
	public void removeDataLayer(DataLayer x);

	// =======================================================================

	public double[] getPixelDimensions();

	// =======================================================================

	public boolean canGeneratePresentationState();
	public PresentationState generatePresentationState(BasicImagePanel bip, String username, String psDescription, String psLabel);

	// =======================================================================

	public Hashtable<String,Object> getProperties();
	public String getPropertyDescription(String property);
	public String getAssocKey();
	public double getImageSliceThickness();
}

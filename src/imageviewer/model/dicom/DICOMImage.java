/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import java.util.Hashtable;

import javax.media.jai.TiledImage;

import imageviewer.model.Image;
import imageviewer.model.BasicImage;
import imageviewer.model.PresentationState;
import imageviewer.model.dicom.ps.DICOMPresentationState;
import imageviewer.model.dicom.ps.DICOMPSFactory;

import imageviewer.ui.image.BasicImagePanel;
import imageviewer.util.ImageCache;

// =======================================================================
/**
 * The base DICOMImage representation in ImageViewer, and core
 * implementation of Image.  This implementation depends heavily on
 * JAI, and automatically stores itself in the imageviewer ImageCache,
 * and handles automatic loading from cache whenever an operation is
 * called on it.
 */
public class DICOMImage extends BasicImage {

	String filename=null;
	DICOMHeader dh=null;
	int bitDepth=0;
	long filePosition=0;

	/**
	 * Construct an image, and add this to the ImageViewer cache.
	 * 
	 * @param ti - TiledImage
	 * @param filename - filesystem path to file
	 * @param filePosition - position of image in channel stream 
	 */
	public DICOMImage(TiledImage ti, String filename, long filePosition) {

		this.ti=ti;
		this.filename=filename;
		this.filePosition=filePosition;
		ImageCache.getDefaultImageCache().add(this);
	}

	// =======================================================================

	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getRenderedImage()
	 */
	public RenderedImage getRenderedImage() {if (ti==null) load(); return ti;}
	
	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getData()
	 */
	public Raster getData() {if (ti==null) load(); return ti.getData();}
	
	/**
	 * Returns a DICOMHeader for this Image.
	 * @return
	 */
	public DICOMHeader getDICOMHeader() {return dh;}	 

	/**
	 * Get the filename for this image.
	 * @return
	 */
	public String getFilename() {return filename;}
	
	/**
	 * Key consists of the filename|filePosition
	 * 
	 * @see imageviewer.model.Image#getKey()
	 */
	public String getKey() {return new String(filename+"|"+filePosition);}

	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getBitDepth()
	 */
	public int getBitDepth() {return bitDepth;}
	
	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getWidth()
	 */
	public int getWidth() {return dh.getImageColumns();}
	
	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getHeight()
	 */
	public int getHeight() {return dh.getImageRows();}
	
	/**
	 * File position in the channel stream 
	 * 
	 * @return
	 */
	public long getFilePosition() {return filePosition;}
	
	/**
	 * Set file position in the channel stream. 
	 * @param x
	 */
	public void setFilePosition(long x) {filePosition=x;}
	
	/**
	 * Set the bit depth. 
	 * @param x
	 */
	public void setBitDepth(int x) {bitDepth=x;}
	
	/**
	 * Set the dicom header.
	 * @param x
	 */
	public void setDICOMHeader(DICOMHeader x) {dh=x;}
	
	/**
	 * Set the maximum pixel value.
	 * @param x
	 */

	public boolean isUnloadable() {return true;}
	
	/* (non-Javadoc)
	 * @see imageviewer.model.Image#isLoaded()
	 */
	public boolean isLoaded() {return (ti!=null) ? true : false;}

	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getSources()
	 */
	public Image[] getSources() {return null;}

	public double[] getPixelDimensions() {return dh.getImagePixelSpacingArray();}
	public double getImageSliceThickness() {return dh.getImageSliceThickness();}

	// =======================================================================
	/**
	 * Load and unload methods handle the need to get rid of the data
	 * associated with the raw image data, as needed.  Loading will
	 * update any assocated imageCache, and conversely, unloading will
	 * remove the image from an imageCache.
	 * 
	 * @see imageviewer.model.Image#load()
	 */
	public void load() {if (ti!=null) return;	ti=DICOMReader.readFile(dh,filename,filePosition); ImageCache.getDefaultImageCache().add(this);}
	
	/**
	 * Load and unload methods handle the need to get rid of the data
	 * associated with the raw image data, as needed.  Loading will
	 * update any assocated imageCache, and conversely, unloading will
	 * remove the image from an imageCache.
	 * 
	 * @see imageviewer.model.Image#unload()
	 */
	public void unload() {if (ti==null) return;	ti.dispose();	ti=null;}

	/**
	 * This finalize removes the image from the cache.
	 * 
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {super.finalize(); ImageCache.getDefaultImageCache().remove(this); unload();}

	// =======================================================================

	public String getAssocKey() {return dh.getPatientID();}

	// =======================================================================

	public Hashtable<String,Object> getProperties() {return dh.getProperties();}

	public String getPropertyDescription(String property) {

		// Format the hex code string to separate out the first four
		// digits (make sure that it's 8 characters long) and then put
		// brackets around it.

		String s=DICOMTagMap.doHexCodeLookup(property);
		if (s!=null) {
			for (int loop=8-s.length(); loop>=0; loop--) s=("0"+s);
			return ("("+s.substring(0,4)+","+s.substring(4,8)+")");
		}
		return null;
	}

	// =======================================================================

	public boolean canGeneratePresentationState() {return true;}

	public PresentationState generatePresentationState(BasicImagePanel bip, String username, String psDescription, String psLabel) {

		DICOMPresentationState dps=DICOMPSFactory.generate(username,psDescription,psLabel);
		DICOMPSFactory.populate(dps,dh);
		DICOMPSFactory.populate(dps,bip);
		return dps;
	}
}


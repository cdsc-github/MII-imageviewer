/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.vhd;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import javax.media.jai.TiledImage;

import imageviewer.model.Image;
import imageviewer.model.BasicImage;
import imageviewer.util.ImageCache;

// =======================================================================

/**
 * Model for Visible Human Dataset images.  Manages opening of files, header reading
 * and image display.  Model design is based on the code of the ANALYZE Model with
 * appropriate changes.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 * @author Brian Burns, Jean Garcia, Kyle Singleton, Jamal Madni, Agatha Lee
 * @version $Revision: 1.1 $ $Date: 2008/12/05 10:23:00 $
 */

public class VhdImage extends BasicImage {

	String filename=null;
	VhdHeader ah=null;
	int bitDepth=0;
	long filePosition=0;

	/**
	 * Construct a Visible Human Dataset(Vhd) image, and add this to the ImageViewer cache.
	 *
	 * @param ti - TiledImage
	 * @param filename - filesystem path to file
	 * @param filePosition - position of image in channel stream
	 */
	public VhdImage(TiledImage ti, String filename, long filePosition) {

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
	 * Returns a VhdHeader for this Image.
	 * @return VhdHeader - the current image header
	 */
	public VhdHeader getVhdHeader() {return ah;}

	/**
	 * Get the filename for this image.
	 * @return String - the current image filename
	 */
	public String getFilename() {return filename;}

	/**
	 * Key consists of the filename|filePosition
	 * @return String - string value for a Key
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
	public int getWidth() {return ah.getWidth();}

	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getHeight()
	 */
	public int getHeight() {return ah.getHeight();}

	/**
	 * Set the bit depth.
	 * @param x - value to be used for bit depth
	 */
	public void setBitDepth(int x) {bitDepth=x;}

	/**
	 * Set the Vhd header.
	 * @param x - header to be used
	 */
	public void setVhdHeader(VhdHeader x) {ah=x;}

	/* (non-Javadoc)
	 * @see imageviewer.model.Image#isUnloadable()
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

	public long getFilePosition() {return filePosition;}

	public double[] getPixelDimensions() {return ah.getPixelDimensions();}

	// =======================================================================
	/**
	 * Load and unload methods handle the need to get rid of the data
	 * associated with the raw image data, as needed.  Loading will
	 * update any assocated imageCache, and conversely, unloading will
	 * remove the image from an imageCache.
	 *
	 * @see imageviewer.model.Image#load()
	 */
	public void load() {if (ti!=null) return;	ti=VhdReader.readFile(ah,filename,filePosition); ImageCache.getDefaultImageCache().add(this);}

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

	public String getAssocKey() {return ah.getPatientID();}
}


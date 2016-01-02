/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.raw;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import javax.media.jai.TiledImage;

import imageviewer.model.BasicImage;
import imageviewer.model.Image;
import imageviewer.util.ImageCache;

// =======================================================================

public class RawImage extends BasicImage {

	int width=0, height=0, bitsAllocated=0, bitsStored=0, bitDepth=0;
	long fileOffset=0, filePosition=0;
	double[] pixelDimensions=new double[2];
	String filename=null;

	public RawImage(TiledImage ti, String filename, long filePosition) {

		this.ti=ti;
		this.filename=filename;
		this.filePosition=filePosition;
		ImageCache.getDefaultImageCache().add(this);
	}

	// =======================================================================

	public RenderedImage getRenderedImage() {if (ti==null) load(); return ti;}
	public Raster getData() {if (ti==null) load(); return ti.getData();}

	public String getFilename() {return filename;}
	public String getKey() {return new String(filename+"|"+filePosition);}

	public int getBitDepth() {return bitDepth;}
	public int getBitsAllocated() {return bitsAllocated;}
	public int getBitsStored() {return bitsStored;}
	public int getWidth() {return width;}
	public int getHeight() {return height;}

	public long getFilePosition() {return filePosition;}
	public long getFileOffset() {return fileOffset;}

	public double[] getPixelDimensions() {return pixelDimensions;}
	
	public void setWidth(int x) {width=x;}
	public void setHeight(int x) {height=x;}
	public void setFilePosition(long x) {filePosition=x;}
	public void setBitDepth(int x) {bitDepth=x;}
	public void setBitsAllocated(int x) {bitsAllocated=x;}
	public void setBitsStored(int x) {bitsStored=x;}
	public void setFileOffset(long x) {fileOffset=x;}
	public void setPixelDimensions(double[] x) {pixelDimensions=x;}

	public boolean isUnloadable() {return true;}
	public boolean isLoaded() {return (ti!=null) ? true : false;}

	public Image[] getSources() {return null;}

	// =======================================================================
	// Load and unload methods handle the need to get rid of the data
	// associated with the raw image data, as needed.  Loading will
	// update any assocated imageCache, and conversely, unloading will
	// remove the image from an imageCache.

	public void load() {

		if (ti!=null) return;	
		ti=RawImageReader.readFile(width,height,bitsAllocated,bitsStored, filename,filePosition); 
		ImageCache.getDefaultImageCache().add(this);
	}

	public void unload() {if (ti==null) return;	ti.dispose();	ti=null;}
	protected void finalize() throws Throwable {super.finalize(); ImageCache.getDefaultImageCache().remove(this); unload();}

	public String getAssocKey() {return getKey();}

}


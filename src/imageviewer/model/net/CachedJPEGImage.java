/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.net;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.media.jai.TiledImage;

import imageviewer.model.Image;
import imageviewer.model.BasicImage;
import imageviewer.util.ImageCache;

// =======================================================================

public class CachedJPEGImage extends BasicImage {

	String url=null, selectedCase=null;
	CachedJPEGHeader cjh=null;
	int sliceNumber=0;

	public CachedJPEGImage(TiledImage ti, String url, String selectedCase, int sliceNumber) {

		this.ti=ti;
		this.url=url;
		this.selectedCase=selectedCase;
		this.sliceNumber=sliceNumber;
		// ImageCache.getDefaultImageCache().add(this);
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
	
	public String getKey() {return new String(url+"|"+sliceNumber);}

	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getBitDepth()
	 */
	public int getBitDepth() {return 8;}
	
	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getWidth()
	 */
	public int getWidth() {if (ti==null) load(); return ti.getWidth();}
	
	/* (non-Javadoc)
	 * @see imageviewer.model.Image#getHeight()
	 */
	public int getHeight() {if (ti==null) load(); return ti.getHeight();}
	
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

	public double[] getPixelDimensions() {return null;}

	public int getMaxPixelValue() {return 256;}

	// =======================================================================
	/**
	 * Load and unload methods handle the need to get rid of the data
	 * associated with the raw image data, as needed.  Loading will
	 * update any associated imageCache, and conversely, unloading will
	 * remove the image from an imageCache.
	 * 
	 * @see imageviewer.model.Image#load()
	 */
	public void load() {

		if (ti!=null) return;	
		try {
			URL getURL=new URL(url);
			URLConnection getImageConnection=getURL.openConnection();
			getImageConnection.setUseCaches(false);
			getImageConnection.setDoOutput(true);
			OutputStream outStream=getImageConnection.getOutputStream();
			ObjectOutputStream outObjectStream=new ObjectOutputStream(outStream);
			outObjectStream.writeObject(selectedCase+"|"+sliceNumber);
			outObjectStream.flush();
			ObjectInputStream ois=new ObjectInputStream(getImageConnection.getInputStream());
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			int iRead=0;
			byte[] baChunk=new byte[4096];
			while ((iRead=ois.read(baChunk))>0) baos.write(baChunk,0,iRead);
			byte[] image=baos.toByteArray();
			InputStream is=new ByteArrayInputStream(image);
			BufferedImage bi=ImageIO.read(is);
			ti=new TiledImage(bi,false);
			ois.close();
			ImageCache.getDefaultImageCache().add(this);
		} catch (Exception exc) {
			System.err.println("Error attempting to read image from network connection.");
			exc.printStackTrace();
		}
	}
	
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

	public String getAssocKey() {return cjh.getPatientID();}
}


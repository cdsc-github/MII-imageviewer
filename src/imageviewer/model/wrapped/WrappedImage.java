/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.wrapped;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import javax.media.jai.TiledImage;

import imageviewer.model.BasicImage;
import imageviewer.model.Image;
import imageviewer.model.PresentationState;
import imageviewer.ui.image.BasicImagePanel;

// =======================================================================
/* Basic class for taking a renderedImage and wrapping it so that it
 * can be viewed in imageViewer. */

public class WrappedImage extends BasicImage {

	String wrappedKey=null;
	RenderedImage ri=null;

	public WrappedImage(RenderedImage ri) {this.ri=ri; ti=new TiledImage(ri,true); wrappedKey=("_WRAPPED"+System.currentTimeMillis());}

	// =======================================================================

	public int getWidth() {return ti.getWidth();}
	public int getHeight() {return ti.getHeight();}
	public int getBitDepth() {return ti.getColorModel().getComponentSize(0);}

	public RenderedImage getRenderedImage() {return ti;}         
	public Raster getData() {return getData();}                                   

	public boolean isUnloadable() {return false;}                     
	public boolean isLoaded() {return true;}

	public void load() {}
	public void unload() {}

	public String getKey() {return wrappedKey;}
	public String getAssocKey() {return getKey();}

	protected void finalize() throws Throwable {

		super.finalize(); 
		ri=null;
		ti=null;
	}

	public double[] getPixelDimensions() {return null;}
	public Image[] getSources() {return null;}

	// =======================================================================

	public boolean canGeneratePresentationState() {return false;}
	public PresentationState generatePresentationState(BasicImagePanel bip, String username, String psDescription, String psLabel) {return null;}
}

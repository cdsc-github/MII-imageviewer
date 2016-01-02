/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.net;

import java.util.ArrayList;

import imageviewer.model.Image;
import imageviewer.model.ImageSequence;

// =======================================================================

public class CachedJPEGSeries extends ImageSequence {

	CachedJPEGHeader cjh=null;
	String servletURL=null;

	public CachedJPEGSeries(Image[] imageArray) {for (int loop=0; loop<imageArray.length; loop++) sequence.add(imageArray[loop]); initialize();} 
	public CachedJPEGSeries(ArrayList images, CachedJPEGHeader cjh) {sequence=images; this.cjh=cjh; initialize();}

	private void initialize() {

		if (!isEmpty()) {
			String seriesDescription=cjh.getDescription();
			if (seriesDescription!=null) setProperty(DESCRIPTION,seriesDescription);
			setProperty(FILENAME,servletURL);
		}
		setProperty(NUMBER_IMAGES,new Double(sequence.size()));
		registerModel();
	}

	// =======================================================================

	public String getShortDescription() {return new String("Network CachedJPEG series: "+servletURL);}

	public String[] getLongDescription() {

		if (!isEmpty()) {
			String numImages=new String(sequence.size()+" "+((sequence.size()>1) ? "images" : "image"));
			String[] description=new String[] {"CachedJPEG file ("+numImages+")"};
			return description;
		}
		return null;
	}

	public int getMaxPixelValue() {return 256;}

	// =======================================================================

	public CachedJPEGHeader getHeader() {return cjh;}

	public Object getDependentKey() {return cjh.getStudyUID();}
	public Object getClosingKey() {return null;}

	public void setHeader(CachedJPEGHeader x) {cjh=x;}
}

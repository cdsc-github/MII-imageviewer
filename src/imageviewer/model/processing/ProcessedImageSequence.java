/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.processing;

import imageviewer.model.ImageSequence;

// =======================================================================

public class ProcessedImageSequence extends ImageSequence {

	ImageProcessor ip=null;
	ImageSequence is=null;

	public ProcessedImageSequence() {super();}
	public ProcessedImageSequence(ImageProcessor ip, ImageSequence is) {

		this.ip=ip;
		this.is=is;
		for (int loop=0, n=is.size(); loop<n; loop++) {
			sequence.add(new ProcessedImage(ip,is.getImage(loop)));
		}
	}

	// =======================================================================

	public void setProperty(String x, Object o) {is.setProperty(x,o);}
	public Object getProperty(String x) {return is.getProperty(x);}

	public String getShortDescription() {return is.getShortDescription();}
	public String[] getLongDescription() {return is.getLongDescription();}

	public Object getClosingKey() {return null;}
	public Object getDependentKey() {return this;}
}

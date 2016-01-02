/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.processing;

import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;

import imageviewer.model.Image;
import imageviewer.rendering.wl.WindowLevel;

// =======================================================================

public class WindowLevelImageProcessor implements ImageProcessor {

	WindowLevel wl=null;

	public WindowLevelImageProcessor(WindowLevel wl) {this.wl=wl;}

	// =======================================================================

	public PlanarImage process(Image[] sources) {

		int maxPixelValue=(int)Math.pow(2,sources[0].getBitDepth());
		LookupTableJAI lut=WindowLevel.createLinearGrayscaleLookupTable(wl,maxPixelValue,255);
		PlanarImage pi=(PlanarImage)JAI.create("lookup",sources[0].getRenderedImage(),lut);
		lut=null;
		return pi;
	}
}

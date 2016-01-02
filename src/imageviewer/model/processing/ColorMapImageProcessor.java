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

public class ColorMapImageProcessor implements ImageProcessor {

	ColorMap cm=null;
	int lowerThreshold=0, upperThreshold=0;

	public ColorMapImageProcessor(ColorMap cm, int lowerThreshold, int upperThreshold) {

		this.cm=cm;
		this.lowerThreshold=lowerThreshold;
		this.upperThreshold=upperThreshold;
	}

	// =======================================================================

	protected LookupTableJAI createColorMapLookupTable(int range) {

		String lutKey=("_AlphaWL|"+range);
		LookupTableJAI ltj=(LookupTableJAI)WindowLevel.LUT_CACHE.doLookup(lutKey);
		if (ltj!=null) return ltj;

		byte[][] lut=new byte[3][range];
		for (int i=0; i<range; i++) {
			int[] color=cm.lookup(i);
			lut[0][i]=(byte)color[0];
			lut[1][i]=(byte)color[1];
			lut[2][i]=(byte)color[2];
		}

		ltj=new LookupTableJAI(lut);
		WindowLevel.LUT_CACHE.add(lutKey,ltj);
		return ltj;
	}	

	// =======================================================================

	public PlanarImage process(Image[] sources) {

		LookupTableJAI lut=createColorMapLookupTable((int)Math.pow(2,sources[0].getBitDepth()));
		PlanarImage pi=(PlanarImage)JAI.create("lookup",sources[0].getRenderedImage(),lut);
		lut=null;
		return pi;
	}
}

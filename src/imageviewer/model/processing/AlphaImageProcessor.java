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

public class AlphaImageProcessor implements ImageProcessor {

	public AlphaImageProcessor() {}

	// =======================================================================

	protected LookupTableJAI createAlphaGrayscaleLookupTable(int range) {

		String lutKey=("_AlphaWL|"+range);
		LookupTableJAI ltj=(LookupTableJAI)WindowLevel.LUT_CACHE.doLookup(lutKey);
		if (ltj!=null) return ltj;

		int[][] lut=new int[4][range];
		for (int i=0; i<range; i++) {
			lut[0][i]=lut[1][i]=lut[2][i]=i;
			lut[4][i]=1;
		}

		ltj=new LookupTableJAI(lut);
		WindowLevel.LUT_CACHE.add(lutKey,ltj);
		return ltj;
	}	

	// =======================================================================

	public PlanarImage process(Image[] sources) {

		LookupTableJAI lut=createAlphaGrayscaleLookupTable((int)Math.pow(2,sources[0].getBitDepth()));
		PlanarImage pi=(PlanarImage)JAI.create("lookup",sources[0].getRenderedImage(),lut);
		lut=null;
		return pi;
	}
}

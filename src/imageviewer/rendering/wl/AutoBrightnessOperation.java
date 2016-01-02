/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering.wl;

import javax.media.jai.Histogram;

// =======================================================================
// Simple algorithm that examines the histogram for the image and
// attempts to select an appropriate auto brightness/contrast based
// on the pixel data. To handle the potential problem of black
// washout and noise, a pThreshold is set to find that value at
// which 40% of the pixels are below (should be safe?). Loosely
// taken from the base algorithm in ImageJ.

public class AutoBrightnessOperation {

	public static int[] compute(Histogram h) {

		int autoThreshold=5000;
		double threshold=((h.getTotals())[0])/autoThreshold;
		int[] bins=h.getBins(0);
		int nBins=bins.length;
		int histogramMin=0, histogramMax=0;

		double[] pThreshold=h.getPTileThreshold(0.4);
		for (int loop=(int)pThreshold[0]; loop<nBins; loop++) if (bins[loop]>threshold) {histogramMin=loop; break;}
		for (int loop=nBins-1; loop>=0; loop--) if (bins[loop]>threshold) {histogramMax=loop; break;}
		if (histogramMax>=histogramMin) {
			int lowValue=(int)h.getLowValue(0);
			int min=lowValue+histogramMin;
			int max=lowValue+histogramMax;
			if (min==max) {
				min=(int)h.getLowValue(0); 
				max=(int)h.getHighValue(0);
			}
			return new int[] {max-min,min+(int)((max-min)/2)};
		}
		return null;
	}
}

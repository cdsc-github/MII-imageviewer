/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering.wl;

import javax.media.jai.LookupTableJAI;

public class RescaledLookupTable extends LookupTableJAI {

	double rescaleSlope=1, rescaleIntercept=0;

	public RescaledLookupTable(byte[][] lut, double rescaleSlope, double rescaleIntercept) {super(lut); System.err.println("HA!"); this.rescaleSlope=rescaleSlope; this.rescaleIntercept=rescaleIntercept;}

	public int lookup(int band, int value) {System.err.println("HA!"); return super.lookup(band,(int)((value*rescaleSlope)+rescaleIntercept));}
	public double lookupDouble(int band, int value) {System.err.println("HA!"); return super.lookupDouble(band,(int)((value*rescaleSlope)+rescaleIntercept));}
	public float lookupFloat(int band, int value) {System.err.println("HA!"); return super.lookupFloat(band,(int)((value*rescaleSlope)+rescaleIntercept));}
}

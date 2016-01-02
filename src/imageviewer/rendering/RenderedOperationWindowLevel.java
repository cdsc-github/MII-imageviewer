/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering;

import java.awt.image.RenderedImage;

import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.RenderedOp;

import imageviewer.rendering.wl.WindowLevel;

public class RenderedOperationWindowLevel implements RenderedOperation {

	public static final String OPERATION_NAME=new String("WINDOW/LEVEL");
	
	public RenderedOperationWindowLevel() {}

	// =======================================================================

	public String[] getListenerProperties() {return new String[] {RenderingProperties.WINDOW_LEVEL};}

	public String getRenderedOperationName() {return OPERATION_NAME;}

	public RenderedImage performOperation(RenderedImage source, RenderingProperties rp) {

		if (source==null) return null;
		if (source.getSampleModel().getNumBands()==1) {
			WindowLevel wl=(WindowLevel)rp.getProperty(RenderingProperties.WINDOW_LEVEL);
			if (wl==null) return source;
			int maxPixelValue=((Integer)rp.getProperty(RenderingProperties.MAX_PIXEL)).intValue();
			LookupTableJAI lut=WindowLevel.createLinearGrayscaleLookupTable(wl,maxPixelValue,255);
			RenderedOp image=JAI.create("lookup",source,lut);
			lut=null;
			return image;
		} else {
			return source;
		}
	}
}

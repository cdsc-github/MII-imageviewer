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
import javax.media.jai.operator.TransposeDescriptor;

public class RenderedOperationFlip implements RenderedOperation {

	public static final String OPERATION_NAME=new String("FLIP");
	
	public RenderedOperationFlip() {}

	// =======================================================================

	public String[] getListenerProperties() {return new String[] {RenderingProperties.VERTICAL_FLIP,RenderingProperties.HORIZONTAL_FLIP};}

	public String getRenderedOperationName() {return OPERATION_NAME;}

	public RenderedImage performOperation(RenderedImage source, RenderingProperties rp) {

		if (source==null) return null;
		boolean flipV=((Boolean)rp.getProperty(RenderingProperties.VERTICAL_FLIP)).booleanValue();
		boolean flipH=((Boolean)rp.getProperty(RenderingProperties.HORIZONTAL_FLIP)).booleanValue();
		if ((!flipV)&&(!flipH)) return source;
		if (flipV&&flipH) {
			return JAI.create("transpose",source,TransposeDescriptor.ROTATE_180);
		} else if (flipV) {
			return JAI.create("transpose",source,TransposeDescriptor.FLIP_VERTICAL);
		} else if (flipH) {
			return JAI.create("transpose",source,TransposeDescriptor.FLIP_HORIZONTAL);
		}
		return null;
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;

public class RenderedOperationRotation implements RenderedOperation {

	public static final String OPERATION_NAME=new String("ROTATE");
	
	public RenderedOperationRotation() {}

	// =======================================================================

	public String[] getListenerProperties() {return new String[] {RenderingProperties.ROTATION};}

	public String getRenderedOperationName() {return OPERATION_NAME;}

	public RenderedImage performOperation(RenderedImage source, RenderingProperties rp) {

		if (source==null) return null;
		float rotationAngle=((Float)rp.getProperty(RenderingProperties.ROTATION)).floatValue();
		if (rotationAngle==0) return source;
		ParameterBlock pb=new ParameterBlock();
		pb.addSource(source);
		pb.add(source.getWidth()/2.0f);
		pb.add(source.getHeight()/2.0f);
		pb.add(rotationAngle);
		pb.add(new InterpolationBilinear());
		return JAI.create("rotate",pb);
	}
}

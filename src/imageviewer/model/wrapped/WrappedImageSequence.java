/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.wrapped;

import java.awt.image.RenderedImage;
import java.util.ArrayList;

import imageviewer.model.ImageSequence;

// =======================================================================

public class WrappedImageSequence extends ImageSequence {

	public WrappedImageSequence(RenderedImage[] imageArray) {for (int loop=0; loop<imageArray.length; loop++) sequence.add(new WrappedImage(imageArray[loop]));}
	public WrappedImageSequence(ArrayList images) {sequence=images;}

	// =======================================================================

	public Object getClosingKey() {return null;}
	public Object getDependentKey() {return this;}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering;

import java.awt.image.RenderedImage;

import imageviewer.model.Image;

public interface RenderedOperation {

	public String getRenderedOperationName();
	public String[] getListenerProperties();
	public RenderedImage performOperation(RenderedImage source, RenderingProperties properties);

}

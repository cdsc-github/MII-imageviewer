/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.io.File;
import java.util.ArrayList;

import imageviewer.model.Image;

// =======================================================================

public abstract class AnnotationReader {

	// =======================================================================

	public abstract boolean canRead(File f);

	// =======================================================================

	public abstract void readFile(File f, ArrayList<? extends Image> images);
	
}

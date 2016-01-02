/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.composite;

public interface SequenceMap {

	public int size();                           // Total length/size of the sequence map
	public int[] computeSlices(int imageNumber); // For a given image number, return a mapping to the respective images in other sequences.

}

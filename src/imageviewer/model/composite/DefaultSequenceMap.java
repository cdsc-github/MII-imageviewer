/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.composite;

import imageviewer.model.ImageSequence;

public class DefaultSequenceMap implements SequenceMap {

	int compositeSequenceLength=0, numSequences=0;

 	public DefaultSequenceMap(ImageSequence[] sequences) {

		int compositeSequenceLength=Integer.MAX_VALUE;
		for (int loop=0; loop<sequences.length; loop++) {
			int sequenceLength=sequences[loop].size();
			compositeSequenceLength=(compositeSequenceLength>sequenceLength)? sequenceLength : compositeSequenceLength;
		}
		numSequences=sequences.length;
	}

	// =======================================================================

	public int size() {return compositeSequenceLength;}                      

	public int[] computeSlices(int imageNumber) {

		int[] targetSlices=new int[numSequences];
		for (int loop=0; loop<numSequences; loop++) targetSlices[loop]=imageNumber;
		return targetSlices;
	} 
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.wrapped;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;

import java.util.ArrayList;

// =======================================================================

public class WrappedImageStudy extends ImageSequenceGroup {

	/**
	 * Construct a Study from an ArrayList of series.
	 * 
	 * @param series
	 */
	public WrappedImageStudy(ArrayList<WrappedImageSequence> series) {

		addAll(series);
		if (!isEmpty()) {
			setProperty(NUMBER_SERIES,series.size());
			registerModel();
		} else {
			System.err.println("Empty study due to image reading problems, check log.");
		}
	}

	// =======================================================================

	public String getDescription() {return null;}
	public void addSeries(WrappedImageSequence wis) {add(wis);}
	public void removeSeries(WrappedImageSequence wis) {remove(wis);}

	// =======================================================================

	public Object getDependentKey() {return this;}
	public Object getClosingKey() {return null;}
}

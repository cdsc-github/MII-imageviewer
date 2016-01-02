/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.raw;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;

import java.util.ArrayList;

// =======================================================================

public class RawImageStudy extends ImageSequenceGroup {

	/**
	 * Construct a Study from an ArrayList of series.
	 * 
	 * @param series
	 */
	public RawImageStudy(ArrayList<RawImageSeries> series) {

		addAll(series);
		if (!isEmpty()) {
			setProperty(NUMBER_SERIES,series.size());
			registerModel();
		} else {
			System.err.println("Empty study due to image reading problems, check log.");
		}
	}

	// =======================================================================

	/**
	 * Return study description from header.
	 * 
	 * @return
	 */
	public String getDescription() {return null;}
	
	/**
	 * Add a series to this study.
	 * 
	 * @param ais - series to add
	 */
	public void addSeries(RawImageSeries ris) {add(ris);}
	
	/**
	 * Remove a series from this study.  
	 * 
	 * @param ais - series to remove
	 */
	public void removeSeries(RawImageSeries ris) {remove(ris);}

	// =======================================================================

	public Object getDependentKey() {return this;}
	public Object getClosingKey() {return null;}
}

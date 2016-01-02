/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.analyze;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;

// =======================================================================
/**
 * Basic management of a group of ANALYZE images series. Adds handling
 * of study description information based on the first image in the
 * given ANALYZE image series.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 */

public class AnalyzeImageStudy extends ImageSequenceGroup {

	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	// =======================================================================
	/**
	 * Construct a Study from an ArrayList of series.
	 * 
	 * @param series
	 */
	public AnalyzeImageStudy(ArrayList<AnalyzeImageSeries> series) {

		addAll(series);
		if (!isEmpty()) {
			AnalyzeImageSeries ais=(AnalyzeImageSeries)series.get(0);
			AnalyzeImage image=(AnalyzeImage)ais.get(0);
			AnalyzeHeader ah=image.getAnalyzeHeader();
			String description=ah.getDescription();
			if (description!=null) setProperty(DESCRIPTION,description);
			setProperty(NUMBER_SERIES,groups.size());
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
	public String getDescription() {return (String)getProperty(DESCRIPTION);}
	
	/**
	 * Add a series to this study.
	 * 
	 * @param ais - series to add
	 */
	public void addSeries(AnalyzeImageSeries ais) {add(ais);}
	
	/**
	 * Remove a series from this study.  
	 * 
	 * @param ais - series to remove
	 */
	public void removeSeries(AnalyzeImageSeries ais) {remove(ais);}

	// =======================================================================

	private AnalyzeHeader getHeader() {

		if (!isEmpty()) {
			AnalyzeImageSeries ais=(AnalyzeImageSeries)groups.get(0);
			AnalyzeImage image=(AnalyzeImage)ais.get(0);
			return image.getAnalyzeHeader();
		}
		return null;
	}

	public Object getDependentKey() {AnalyzeHeader ah=getHeader(); return (ah!=null) ? ah.getPatientID() : this;}
	public Object getClosingKey() {AnalyzeHeader ah=getHeader(); return (ah!=null) ? ah.getExpirationDate() : this;} // Hack!
}

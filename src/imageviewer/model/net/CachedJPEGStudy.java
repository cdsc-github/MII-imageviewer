/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.net;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;

// =======================================================================
/**
 * Basic management of a group of images series that are read through
 * a JPEG connection over a network. Adds handling of study
 * description information based on the first image in the given
 * ANALYZE image series.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2011/05/11 22:13:34 $
 */

public class CachedJPEGStudy extends ImageSequenceGroup {

	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	// =======================================================================
	/**
	 * Construct a Study from an ArrayList of series.
	 * 
	 * @param series
	 */
	public CachedJPEGStudy(ArrayList<CachedJPEGSeries> series) {

		addAll(series);
		if (!isEmpty()) {
			CachedJPEGSeries cjs=(CachedJPEGSeries)series.get(0);
			CachedJPEGHeader cjh=cjs.getHeader();
			String description=cjh.getDescription();
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
	 * @param cjs - series to add
	 */
	public void addSeries(CachedJPEGSeries cjs) {add(cjs);}
	
	/**
	 * Remove a series from this study.  
	 * 
	 * @param cjs - series to remove
	 */
	public void removeSeries(CachedJPEGSeries cjs) {remove(cjs);}

	// =======================================================================

	private CachedJPEGHeader getHeader() {

		if (!isEmpty()) {
			CachedJPEGSeries cjs=(CachedJPEGSeries)groups.get(0);
			return cjs.getHeader();
		}
		return null;
	}

	public Object getDependentKey() {CachedJPEGHeader cjh=getHeader(); return (cjh!=null) ? cjh.getPatientID() : this;}
	public Object getClosingKey() {CachedJPEGHeader cjh=getHeader(); return (cjh!=null) ? cjh.getStudyUID() : this;} 
}

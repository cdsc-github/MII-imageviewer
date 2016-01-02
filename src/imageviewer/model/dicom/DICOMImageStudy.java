/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// =======================================================================
/**
 * Basic management of a group of DICOM images series. Adds handling
 * of study description information based on the first image in the
 * given DICOM image series.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 */

public class DICOMImageStudy extends ImageSequenceGroup {

	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	// =======================================================================
	/**
	 * Construct a Study from an ArrayList of series.
	 * 
	 * @param series
	 */
	public DICOMImageStudy(ArrayList<DICOMImageSeries> series) {

		addAll(series);
		if (!isEmpty()) {
			DICOMImageSeries dis=(DICOMImageSeries)series.get(0);
			DICOMImage image=(DICOMImage)dis.get(0);
			DICOMHeader dh=image.getDICOMHeader();
			String studyDescription=dh.getStudyDescription();
			if (studyDescription!=null) setProperty(DESCRIPTION,studyDescription);
			String studyImageType=dh.getStudyImageType();
			if (studyImageType!=null) setProperty(TYPE,studyImageType);
			try {
				String str=dh.getStudyDate()+" "+dh.getStudyTime();
				setProperty(TIMESTAMP,(str.indexOf(":")>0) ? DATE_FORMAT2.parse(str) : DATE_FORMAT1.parse(str));
			} catch (Exception exc) {
				System.err.println("Could not parse timestamp on DICOM image study: "+dh.getStudyDate()+" "+dh.getStudyTime());
			}
			setProperty(NUMBER_SERIES,groups.size());
		} else {
			System.err.println("Empty study due to image reading problems, check log.");
		}
		registerModel();
	}

	// =======================================================================

	/**
	 * Return study description from header.
	 * 
	 * @return
	 */
	public String getStudyDescription() {return (String)getProperty(DESCRIPTION);}
	
	/**
	 * Return Study Image Type.
	 * 
	 * @return
	 */
	public String getStudyImageType() {return (String)getProperty(TYPE);}

	/**
	 * Get the series in this study.
	 * 
	 * @return
	 */
	public ArrayList<ImageSequence> getSeries() {return getGroups();}

	/**
	 * Get the timestamp for this study. 
	 * 
	 * @return
	 */
	public Date getTimestamp() {return (Date)getProperty(TIMESTAMP);}

	/**
	 * Add a series to this study.
	 * 
	 * @param dis - series to add
	 */
	public void addSeries(DICOMImageSeries dis) {add(dis);}
	
	/**
	 * Remove a series from this study.  
	 * 
	 * @param dis - series to remove
	 */
	public void removeSeries(DICOMImageSeries dis) {remove(dis);}

	// =======================================================================
	/**
	 *	Combine the arrayLists of two given studies, adding the studies
	 * 	from the specified study into this study.  Allows for collapsing of
	 * 	the study information.
	 * 
	 * @param dis - study to add 
	 */
	public void combine(DICOMImageStudy dis) {

		ArrayList<ImageSequence> al=dis.getGroups();
		al.addAll(groups);
		groups=al;
		setProperty(TIMESTAMP,dis.getTimestamp());
	}

	// =======================================================================

	private DICOMHeader getHeader() {

		if (!isEmpty()) {
			DICOMImageSeries dis=(DICOMImageSeries)groups.get(0);
			DICOMImage image=(DICOMImage)dis.get(0);
			return image.getDICOMHeader();
		}
		return null;
	}
	
	public Object getClosingKey() {DICOMHeader dh=getHeader(); return (dh!=null) ? dh.getStudyInstanceUID() : this;}
	public Object getDependentKey() {DICOMHeader dh=getHeader(); return ((dh!=null)&&(dh.getPatientID()!=null)) ? dh.getPatientID() : this;}
}

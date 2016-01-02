/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import imageviewer.model.ImageSequence;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

// =======================================================================
/**
 * Basic management of a group of DICOM images series. Adds handling
 * of series description information based on the first image in the
 * given set.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 */
public class DICOMImageSeries extends ImageSequence {

	public static final String INSTANCE_UID=new String("__INSTANCE_UID");

	// =======================================================================

	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	private static final SimpleDateFormat DATE_FORMAT3=new SimpleDateFormat("M/dd/yy");
	private static final SimpleDateFormat DATE_FORMAT4=new SimpleDateFormat("MMMM d, yyyy h:mm a");
	private static final SimpleDateFormat DATE_FORMAT5=new SimpleDateFormat("yyyyMMdd");

	private static Logger LOG=Logger.getLogger("imageviewer.dicom");

	// =======================================================================

	String filePath=null;

	/**
	 * Construct a Series from an array of DICOMImages
	 *  
	 * @param images
	 */
	public DICOMImageSeries(DICOMImage[] images) {

		for (int loop=0; loop<images.length; loop++) add(images[loop]);
		initialize();
	}

	/**
	 * Construct a Series from an ArrayList of DICOMImages 
	 *  
	 * @param images
	 */
	public DICOMImageSeries(ArrayList<DICOMImage> images) {addAll(images); initialize();}

	// =======================================================================

	private void initialize() {
		
		if (!isEmpty()) {
			DICOMImage image=(DICOMImage)get(0);
			DICOMHeader dh=image.getDICOMHeader();
			String seriesDescription=dh.getSeriesDescription();
			if (seriesDescription!=null) setProperty(DESCRIPTION,seriesDescription);
			String seriesModality=dh.getSeriesImageModality();
			if (seriesModality!=null) setProperty(MODALITY,seriesModality);
			String seriesUID=dh.getSeriesInstanceUID();
			if (seriesUID!=null) setProperty(INSTANCE_UID,seriesUID);
			File f=new File(image.getFilename());
			filePath=f.getParent();
			try {
				String str=((dh.getSeriesDate()==null) ? ((dh.getStudyDate()==null) ? "" : dh.getStudyDate()) : dh.getSeriesDate())+" "+
					((dh.getSeriesTime()==null) ? ((dh.getStudyTime()==null) ? "" : dh.getStudyTime()) : dh.getSeriesTime());
				if (str!=null) str=str.trim();
				setProperty(TIMESTAMP,((str!=null) ? (((str.indexOf(":")>0) ? DATE_FORMAT2.parse(str) : ((str.indexOf(" ")>0) ? DATE_FORMAT1.parse(str) : DATE_FORMAT5.parse(str)))) : new Date(0)));
			} catch (Exception exc) {
				LOG.error("Could not parse timestamp on DICOM image series: "+dh.getSeriesDate()+" "+dh.getSeriesTime());
				LOG.error(filePath);
			}
			setProperty(NUMBER_IMAGES,new Double(sequence.size()));
			setProperty(BODY_PART,dh.getBodyPartExamined());
			setProperty(MANUFACTURER,dh.getSeriesManufacturer());
			setProperty(MANUFACTURER_MODEL,dh.getSeriesManufacturerModel());
			setProperty(STATION,dh.getSeriesStationName());
			setProperty(CONTRAST,dh.getSeriesContrast());
			setProperty(SCAN_SEQUENCE,dh.getSeriesScanningSequence());
			setProperty(SEQUENCE_VARIANT,dh.getSeriesSequenceVariant());
			setProperty(SEQUENCE_NAME,dh.getSeriesSequenceName());
		}
		registerModel();
	}

	// =======================================================================

	/**
	 * Get the description for the Series out of the image header.
	 * 
	 * @return
	 */
	public String getSeriesDescription() {return (String)getProperty(DESCRIPTION);}
	
	/**
	 * Get the series image modality code from the header.
	 * @return
	 */
	public String getSeriesImageModality() {return (String)getProperty(MODALITY);}
	
	
	/**
	 * Get the SeriesInstanceUID from the header.
	 * @return
	 */
	public String getSeriesInstanceUID() {return (String)getProperty(INSTANCE_UID);}
	
	/**
	 * Get the file path for this image.
	 * 
	 * @return
	 */
	public String getFilePath() {return filePath;}

	/**
	 * Get the timestamp for the series from the header. 
	 * 
	 * @return
	 */
	public Date getTimestamp() {return (Date)getProperty(TIMESTAMP);}

	// =======================================================================
	/**
	 * Construct a short description for the series, including a timestamp, 
	 * modality, and description from the image header. 
	 * 
	 * @see imageviewer.model.ImageSequence#getShortDescription()
	 */

	public String getShortDescription() {

		String s=null;
		try {s=DATE_FORMAT3.format((Date)getProperty(TIMESTAMP))+" ";} catch (Exception exc) {s=new String();}
		StringBuffer sb=new StringBuffer(s);
		String modality=(String)getProperty(MODALITY);
		String seriesDesc=(String)getProperty(DESCRIPTION);
		if (seriesDesc!=null) {
			if (!seriesDesc.contains(modality+" ")) sb.append(modality+" ");
			sb.append(seriesDesc);
		} else {
			sb.append(modality+" ");
		}
		return (sb.toString().toUpperCase());
	}

	// =======================================================================
	/**
	 * Get a long description from the header, including timestamp, the
	 * image number in the series, institution, manufacturer, series
	 * manufacturer, study description, series description referring
	 * physician.
	 * 
	 * @see imageviewer.model.ImageSequence#getLongDescription()
	 */
	public String[] getLongDescription() {

		if (!isEmpty()) {
			DICOMImage image=(DICOMImage)get(0);
			DICOMHeader dh=image.getDICOMHeader();
			String s=null;
			try {s=DATE_FORMAT4.format((Date)getProperty(TIMESTAMP));} catch (Exception exc) {s=new String();}
			String date=s;
			String numImages=new String(sequence.size()+" "+dh.getSeriesImageModality()+" "+((sequence.size()>1) ? "images" : "image"));
			String location1=(dh.getStudyInstitution()==null) ? "" : dh.getStudyInstitution();
			String location2=dh.getSeriesManufacturer();
			String location3=(dh.getSeriesManufacturerModel()==null) ? "" : dh.getSeriesManufacturerModel();
			String studyDesc=(dh.getStudyDescription()==null) ? "" : (dh.getStudyDescription()+" study");
			String seriesDesc=dh.getSeriesDescription();
			String physician=dh.getStudyReferringPhysician();
			String[] description=new String[] {date+" ("+numImages+")",location1+" ("+location2+" "+location3+")",studyDesc,seriesDesc,physician};
			return description;
		}
		return null;
	}

	// =======================================================================

	public Object getDependentKey() {

		if (!isEmpty()) {
			DICOMImage image=(DICOMImage)get(0);
			DICOMHeader dh=image.getDICOMHeader();
			return dh.getStudyInstanceUID();
		} else {
			return this;
		}
	}

	public Object getClosingKey() {return null;}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.vhd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;

// =======================================================================

/**
 * Basic management of a group of Visible Human Dataset(Vhd) image series. Adds handling
 * of study description information based on the first image in the
 * given Vhd image series.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 * @author Brian Burns, Jean Garcia, Kyle Singleton, Jamal Madni, Agatha Lee
 * @version $Revision: 1.1 $ $Date: 2008/12/05 10:23:00 $
 */

public class VhdImageStudy extends ImageSequenceGroup {

	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	// =======================================================================
	/**
	 * Construct a Study from an ArrayList of series.
	 *
	 * @param series - list of series to be added to the study
	 */
	public VhdImageStudy(ArrayList<VhdImageSeries> series) {

		addAll(series);
		if (!isEmpty()) {
			VhdImageSeries ais=(VhdImageSeries)series.get(0);
			VhdImage image=(VhdImage)ais.get(0);
			VhdHeader ah=image.getVhdHeader();
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
	 * @return String - description of the study
	 */
	public String getDescription() {return (String)getProperty(DESCRIPTION);}

	/**
	 * Add a series to this study.
	 *
	 * @param ais - series to add
	 */
	public void addSeries(VhdImageSeries ais) {add(ais);}

	/**
	 * Remove a series from this study.
	 *
	 * @param ais - series to remove
	 */
	public void removeSeries(VhdImageSeries ais) {remove(ais);}

	// =======================================================================

	/**
	 * Gets the image and header information for the series being examined
	 *
	 * @return VhdHeader - header information of the image
	 */
	private VhdHeader getHeader() {

		if (!isEmpty()) {
			VhdImageSeries ais=(VhdImageSeries)groups.get(0);
			VhdImage image=(VhdImage)ais.get(0);
			return image.getVhdHeader();
		}
		return null;
	}

	public Object getDependentKey() {VhdHeader ah=getHeader(); return (ah!=null) ? ah.getPatientID() : this;}
	public Object getClosingKey() {VhdHeader ah=getHeader(); return (ah!=null) ? ah.getExpirationDate() : this;} // Hack!
}

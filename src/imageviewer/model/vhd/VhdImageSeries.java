/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.vhd;

import java.io.File;

import java.util.ArrayList;

import imageviewer.model.Image;
import imageviewer.model.ImageSequence;

// =======================================================================

/**
 * Basic management of a Visible Human Dataset(Vhd) image series. Adds handling
 * of series description and number of images.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 * @author Brian Burns, Jean Garcia, Kyle Singleton, Jamal Madni, Agatha Lee
 * @version $Revision: 1.1 $ $Date: 2008/12/05 10:23:00 $
 */

public class VhdImageSeries extends ImageSequence {

    	/**
	 * Builds image series information for an array of images.
	 *
	 * @param imageArray - an array of images to put into a series
	 */
	public VhdImageSeries(Image[] imageArray) {for (int loop=0; loop<imageArray.length; loop++) sequence.add(imageArray[loop]); initialize();}
    	/**
	 * Builds image series information for an array list of images
	 *
	 * @param images - an array list of images to put into a series
	 */
	public VhdImageSeries(ArrayList images) {sequence=images; initialize();}

    	/**
	 * Construction of the series information from the header of the first
	 * image. Sets the maximum pixel value found from the entire series.
	 *
	 */
	private void initialize() {

		if (!isEmpty()) {
			VhdImage image=(VhdImage)get(0);
			VhdHeader ah=image.getVhdHeader();
			String seriesDescription=ah.getDescription();
			if (seriesDescription!=null) setProperty(DESCRIPTION,seriesDescription);
			setProperty(FILENAME,ah.getFilename());
		}
		setProperty(NUMBER_IMAGES,new Double(sequence.size()));

		// Find maximum pixel value for the whole series - Corrects display errors from wide pixel ranges
		float seriesMaxPixelValue = 0;
		for (int count=0; count<sequence.size(); count++) {
		    VhdImage image = (VhdImage)get(count);
		    VhdHeader ah = image.getVhdHeader();
		    float currentMaxPixelValue = ah.getCalibrationMax();
		    if (currentMaxPixelValue > seriesMaxPixelValue)
			seriesMaxPixelValue = currentMaxPixelValue;
		}

		setProperty(MAX_PIXEL_VALUE, seriesMaxPixelValue);

		registerModel();
	}

	// =======================================================================

    	/**
	 * Constructs a brief description of the image series using the file name
	 * and header description.
	 *
	 * @return String - brief description of the series
	 */
	public String getShortDescription() {

		StringBuffer sb=new StringBuffer();

		File f=new File((String)getProperty(FILENAME));
		String filename=f.getName();
		int extIndex=filename.indexOf(".");
		if (extIndex>=0) filename=filename.substring(0,extIndex-1);
		sb.append(filename);
		String desc=(String)getProperty(DESCRIPTION);
		if ((desc!=null)&&(desc.length()!=0)) sb.append(" ("+desc+")");
		return (sb.toString().toUpperCase());
	}

    	/**
	 * Constructs a long description of the image series using the number
	 * of available images.
	 *
	 * @return String - long description of the series
	 */
	public String[] getLongDescription() {

		if (!isEmpty()) {
			String numImages=new String(sequence.size()+" "+((sequence.size()>1) ? "images" : "image"));
			String[] description=new String[] {"Vhd file ("+numImages+")"};
			return description;
		}
		return null;
	}

	// =======================================================================

	public Object getDependentKey() {

		if (!isEmpty()) {
			VhdImage image=(VhdImage)get(0);
			VhdHeader ah=image.getVhdHeader();
			return ah.getDescription();
		} else {
			return this;
		}
	}

	public Object getClosingKey() {return null;}
}

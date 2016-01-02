/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.analyze;

import java.io.File;

import java.util.ArrayList;

import imageviewer.model.Image;
import imageviewer.model.ImageSequence;

// =======================================================================

public class AnalyzeImageSeries extends ImageSequence {

	public AnalyzeImageSeries(Image[] imageArray) {for (int loop=0; loop<imageArray.length; loop++) sequence.add(imageArray[loop]); initialize();} 
	public AnalyzeImageSeries(ArrayList images) {sequence=images; initialize();}

	private void initialize() {

		if (!isEmpty()) {
			AnalyzeImage image=(AnalyzeImage)get(0);
			AnalyzeHeader ah=image.getAnalyzeHeader();
			String seriesDescription=ah.getDescription();
			if (seriesDescription!=null) setProperty(DESCRIPTION,seriesDescription);
			setProperty(FILENAME,ah.getFilename());
		}
		setProperty(NUMBER_IMAGES,new Double(sequence.size()));
		registerModel();
	}

	// =======================================================================

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

	public String[] getLongDescription() {

		if (!isEmpty()) {
			String numImages=new String(sequence.size()+" "+((sequence.size()>1) ? "images" : "image"));
			String[] description=new String[] {"Analyze 7.5 file ("+numImages+")"};
			return description;
		}
		return null;
	}

	// =======================================================================

	public Object getDependentKey() {

		if (!isEmpty()) {
			AnalyzeImage image=(AnalyzeImage)get(0);
			AnalyzeHeader ah=image.getAnalyzeHeader();
			return ah.getDescription();
		} else {
			return this;
		}
	}

	public Object getClosingKey() {return null;}
}

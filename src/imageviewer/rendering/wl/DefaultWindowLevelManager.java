/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering.wl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.lang.reflect.Constructor;

import java.util.Hashtable;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import imageviewer.model.Image;
import imageviewer.model.dicom.DICOMImage;
import imageviewer.model.dicom.DICOMHeader;

import imageviewer.ui.ApplicationContext;
import imageviewer.util.StringUtilities;
import imageviewer.util.XMLUtil;

// =======================================================================

public class DefaultWindowLevelManager {

	public static Hashtable<String,DefaultWindowLevelDescription> DEFAULTS=new Hashtable<String,DefaultWindowLevelDescription>();
	private static Logger LOG=Logger.getLogger("imageViewer.rendering");

	private static final String DEFAULT_FILENAME=new String("config/windowLevel.xml");

	// =======================================================================

	public DefaultWindowLevelManager() {

		String wlDefaultFile=(String)ApplicationContext.getContext().getProperty(ApplicationContext.CONFIG_WINDOW_LEVEL);
		if (wlDefaultFile==null) wlDefaultFile=DEFAULT_FILENAME;
		initialize(wlDefaultFile);
	}

	public DefaultWindowLevelManager(String filename) {initialize(filename);}

	private void initialize(String filename) {

		LOG.info("Loading default window/level settings: "+filename);
		
		try {
			WindowLevelXMLHandler handler=new WindowLevelXMLHandler();
			SAXParser parser=XMLUtil.getSAXParser();
			InputStream is=new FileInputStream(filename);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,handler);
			is.close();
			XMLUtil.releaseSAXParser(parser);
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+filename);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+filename);
			exc.printStackTrace();
		}
	}
	
	// =======================================================================

	public static WindowLevel getDefaultWindowLevel(Image i) {

		if (!(i instanceof DICOMImage)) {
			int maxPixel=i.getMaxPixelValue();
			return new WindowLevel(maxPixel,(int)Math.round((double)maxPixel/2));
		}
		DICOMImage di=(DICOMImage)i;
		DICOMHeader dh=di.getDICOMHeader();

		// A bit of a hack until we can properly handle the rescaling
		// slope and intercept in the DICOM information.

		double[] imageLevel=dh.getImageLevel();
		double[] imageWindow=dh.getImageWindow();
		String seriesDescription=dh.getSeriesDescription();
		boolean isHRCT=((seriesDescription!=null) ? seriesDescription.contains("HRCT") : false);
		if ((imageLevel!=null)&&(imageWindow!=null)&&(!isHRCT)) {

			// We sometimes need to guess at this point, becauese different
			// systems will specify the level either using the rescale
			// intercept or not using the rescale intercept value.  So, if
			// the rescale intercept is not 0, and the level is large, then
			// subtract the intercept, too.

			double rescaleSlope=dh.getImageRescaleSlope();
			double rescaleIntercept=dh.getImageRescaleIntercept();
			double scoutRescale=((seriesDescription!=null) ? ((seriesDescription.toUpperCase().contains("SCOUT")) ? rescaleIntercept : 0) : 0);
			int level=(int)(rescaleSlope*imageLevel[0]+scoutRescale);
			WindowLevel wl=new WindowLevel((int)imageWindow[0],level,rescaleSlope,rescaleIntercept);
			wl.setRescaled((rescaleIntercept==0) ? false : true);
			return wl;
		}
	
		String modality=dh.getSeriesImageModality();
		String anatomy=dh.getBodyPartExamined();
		if (anatomy==null) anatomy=StringUtilities.findAnatomyPhrase(dh.getStudyDescription());
		if (anatomy==null) anatomy=StringUtilities.findAnatomyPhrase(dh.getSeriesDescription());
		if (anatomy==null) anatomy=new String("*");

		DefaultWindowLevelDescription dwld=DEFAULTS.get(DefaultWindowLevelDescription.makeKey(modality,anatomy));
		if (dwld!=null) {
			String algorithmClass=dwld.getAlgorithmClass();
			WindowLevel defaultWL=new WindowLevel(dwld.getWindow(),dwld.getLevel());
			defaultWL.setRescaleSlope(dh.getImageRescaleSlope());
			defaultWL.setRescaleIntercept(dh.getImageRescaleIntercept());
			if (algorithmClass==null) return defaultWL;
			Class[] constructorParameterTypes={};
			Object[] constructorParameters={};
			try {
				Constructor newConstructor=Class.forName(algorithmClass).getConstructor(constructorParameterTypes);
				WindowLevelAlgorithm wla=(WindowLevelAlgorithm)newConstructor.newInstance(constructorParameters);
				WindowLevel wl=wla.computeWindowLevel(i);
				return (wl==null) ? defaultWL : wl;
			} catch (Exception exc) {
				LOG.error("Error attempting to instantiate window/level algorithm: "+algorithmClass);
				exc.printStackTrace();
			}
			return defaultWL;
		}
		int maxPixel=i.getMaxPixelValue();
		return new WindowLevel(maxPixel,(int)Math.round((double)maxPixel/2)); // If all else fails, return a strictly linear 1-1 map
	}

	// =======================================================================

	private static class WindowLevelXMLHandler extends DefaultHandler {

		public WindowLevelXMLHandler() {super();}
		
		// =======================================================================

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			try {
				if (localname.compareTo("windowLevel")==0) {
					DefaultWindowLevelDescription dwld=new DefaultWindowLevelDescription(attr.getValue("name"),attr.getValue("modality").toUpperCase(),
																																							 attr.getValue("anatomy").toUpperCase(),Integer.parseInt(attr.getValue("window")),
																																							 Integer.parseInt(attr.getValue("level")));
					if (attr.getIndex("algorithmClass")!=-1) dwld.setAlgorithmClass(attr.getValue("algorithmClass"));
					DEFAULTS.put(dwld.getKey(),dwld);
				}
			} catch (Exception exc) {
				LOG.error("Unable to parse sepcified file in LayoutFactory.");
				exc.printStackTrace();
			}
		}
	}
}

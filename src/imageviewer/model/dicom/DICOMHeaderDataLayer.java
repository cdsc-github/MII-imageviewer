/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import java.awt.Point;
import java.awt.Shape;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import imageviewer.model.DataLayer;
import imageviewer.model.dl.HeaderDataLayer;

import imageviewer.ui.VisualLayerRenderer;
import imageviewer.ui.image.vl.HeaderVisualLayer;

// =======================================================================
// Based on the given DICOM header, try and fill out some of the basic
// information that would normally show up on a given image, like the
// image number, etc.

public class DICOMHeaderDataLayer implements DataLayer, HeaderDataLayer {

	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	private static final SimpleDateFormat DATE_FORMAT3=new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat DATE_FORMAT4=new SimpleDateFormat("M/dd/yy h:mm a");
	private static final SimpleDateFormat DATE_FORMAT5=new SimpleDateFormat("yyyyMMdd HHmm");

	private static final VisualLayerRenderer HEADER_RENDERER=new HeaderVisualLayer();
	private static final String LAYER_NAME=new String("HEADER");

	// =======================================================================

	String[] topLeft=null, topRight=null, bottomLeft=null, bottomRight=null;

	public DICOMHeaderDataLayer(DICOMHeader dh) {

		try {
			String studyInstitution=(String)dh.doLookup(DICOMHeader.STUDY_INSTITUTION);
			String station=(String)dh.doLookup(DICOMHeader.SERIES_STATION_NAME);
			String seriesDesc=(String)dh.doLookup(DICOMHeader.SERIES_DESCRIPTION);
		  String str=((dh.getSeriesDate()==null) ? ((dh.getStudyDate()==null) ? "" : dh.getStudyDate()) : dh.getSeriesDate())+" "+
				((dh.getSeriesTime()==null) ? ((dh.getStudyTime()==null) ? "" : dh.getStudyTime()) : dh.getSeriesTime());
			if (str!=null) str=str.trim();
			String date=((str!=null)&&(str.length()!=0)) ? DATE_FORMAT4.format(((str.indexOf(":")>0) ? DATE_FORMAT2.parse(str) : ((str.indexOf(" ")>0) ? ((str.length()==15) ? DATE_FORMAT1.parse(str) : DATE_FORMAT5.parse(str)) : DATE_FORMAT3.parse(str)))) : null;
			topLeft=new String[] {(studyInstitution==null) ? "" : (studyInstitution+((station!=null) ? " ("+station+")" : "")),seriesDesc,date};
			topRight=new String[] {dh.getImageInstance()};

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public VisualLayerRenderer getRenderer() {return HEADER_RENDERER;}
	public String getName() {return LAYER_NAME;}

	// =======================================================================

	public void add(Object x) {}
	public void remove(Object x) {}

	public boolean canSelect() {return false;}
	public ArrayList getSelections(Point x) {return null;}
	public ArrayList getSelections(Shape x) {return null;}

	// =======================================================================

	public String[] getTopLeftText() {return topLeft;}
	public String[] getTopRightText() {return topRight;}
	public String[] getBottomLeftText() {return bottomLeft;}
	public String[] getBottomRightText() {return bottomRight;}

}

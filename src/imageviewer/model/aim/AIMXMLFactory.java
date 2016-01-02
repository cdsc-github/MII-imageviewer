/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import imageviewer.model.DataLayer;
import imageviewer.model.Image;
import imageviewer.model.aim.ImageAnnotation.imageAnnotationIdentifier;
import imageviewer.model.aim.imagereference.ImageReference;
import imageviewer.model.aim.imagereference.AIMImage;
import imageviewer.model.aim.imagereference.AIMSeries;
import imageviewer.model.aim.imagereference.AIMStudy;
import imageviewer.model.aim.markup.GeometricShape;
import imageviewer.model.dicom.DICOMHeader;
import imageviewer.model.dicom.DICOMImage;
import imageviewer.model.dicom.DICOMTagMap;
import imageviewer.model.dl.ShapeDataLayer;
import imageviewer.ui.ApplicationContext;
import imageviewer.ui.UserManager;
import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.AnnotationAIMObjectFactory;
import imageviewer.ui.annotation.StylizedShape;
public class AIMXMLFactory {
	
	private static final SimpleDateFormat DATE_FORMAT=new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat TIME_FORMAT=new SimpleDateFormat("HHmmss");
	
	static ArrayList<String[]> AIMXML;
	static ArrayList<String[]> markedImages;
	
	public static void populate(ArrayList<Image> theImages) throws ParseException {
		
		AIMXML = new ArrayList<String[]>();
		markedImages = new ArrayList<String[]>();
		
		int numImages = theImages.size();

		//Get Annotations
		GeometricShape geometricShape = null;
		TextAnnotation textAnnotation = null;
		
		for (int i=0; i<numImages; i++) {
			//Check if images is marked
			Image image = theImages.get(i);
			String isMarked=(String)image.getProperties().get(Image.MARKED);
			if (isMarked != null){
				Image img = theImages.get(i);
				DICOMImage dcmImg = null;
				if (img instanceof DICOMImage){
					dcmImg = (DICOMImage) img; 
				}
				DICOMHeader dcmHeader = dcmImg.getDICOMHeader();
				
				String[] args = {"Marked Image Slice "+i, dcmHeader.getStudyInstanceUID(), dcmHeader.getSeriesInstanceUID(), dcmHeader.getSOPInstanceUID(), Integer.toString(0)};
				markedImages.add(args);
			}
			// ApplicationContext.getContext().setMarkedImages(markedImages); ***
			
			//Check if image has shapes drawn
			ShapeDataLayer sdl=(ShapeDataLayer)theImages.get(i).findDataLayer(DataLayer.SHAPE);
			if (sdl!=null) {
				
				Image img = theImages.get(i);
				DICOMImage dcmImg = null;
				if (img instanceof DICOMImage){
					dcmImg = (DICOMImage) img; 
				}
				DICOMHeader dcmHeader = dcmImg.getDICOMHeader();
				
				Patient aimPatient = new Patient();
				String name = dcmHeader.getPatientName();
				if (name.length() > 3)
					aimPatient.setName(name.substring(0,3));
				else
					aimPatient.setName(name);
	//			Date date = DATE_FORMAT.parse((String) dcmHeader.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00101010))));		
	//			aimPatient.setBirthDate(date);
				aimPatient.setPatientID(dcmHeader.getPatientID());
				aimPatient.setSex(dcmHeader.getPatientSex());
				aimPatient.setEthnicGroup((String)dcmHeader.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00102160))));
	
				//Note: Can this info be pulled from TBP?
				User user = new User("Kyle Singleton", "kwsingleton");
				//User user = new User(UserManager.getCurrentUserName(), UserManager.getCurrentUserName());
				String manu = dcmHeader.getSeriesManufacturer();
				if (manu.length() > 3)
					manu = manu.substring(0,3);
				Equipment equipment = new Equipment(manu, dcmHeader.getSeriesManufacturerModel(), (String)dcmHeader.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00181020))));
				
				Date date = DATE_FORMAT.parse(dcmHeader.getStudyDate());
				Date time = TIME_FORMAT.parse(dcmHeader.getStudyTime());
				Date dayAndTime = new Date(date.getTime() + time.getTime());
				
				AIMImage aimImg = new AIMImage();
				aimImg.setAcquisitionDateTime(dayAndTime);
				aimImg.setSopClassUID(dcmHeader.getSOPClassUID());
				aimImg.setSopInstanceUID(dcmHeader.getSOPInstanceUID());
				
				double[] pixelSpacingArray=dcmHeader.getImagePixelSpacingArray();
				aimImg.setPixelSpacingHorizontal(pixelSpacingArray[0]);
				aimImg.setPixelSpacingVertical(pixelSpacingArray[1]);
				aimImg.setPatientOrientationColumn("");
				aimImg.setPatientOrientationRow("");
				
				AIMSeries series = new AIMSeries(aimImg, dcmHeader.getSeriesInstanceUID(), dcmHeader.getSeriesDescription(), dcmHeader.getSeriesImageModality());
				
				String se_date = dcmHeader.getSeriesDate();
				if (se_date == null)
					date = DATE_FORMAT.parse("20010101");
				else
					date = DATE_FORMAT.parse(se_date);
				String se_time = dcmHeader.getSeriesTime();
				if (se_time == null)
					time = TIME_FORMAT.parse("145037.484000");
				else
					time = TIME_FORMAT.parse(se_time);
				dayAndTime = new Date(date.getTime() + time.getTime());
				AIMStudy study = new AIMStudy(series, dcmHeader.getStudyInstanceUID(), dayAndTime);
				ImageReference imageReference = new ImageReference(study);
	
				ArrayList<StylizedShape> shapes=sdl.getShapes();
				if (shapes!=null) {
					for (int loop=0, n=shapes.size(); loop<n; loop++) {
						StylizedShape s=shapes.get(loop);
						if (s instanceof Annotation) {
							Object o = AnnotationAIMObjectFactory.createAIMObject((Annotation)s,DataLayer.SHAPE);
							if (o instanceof GeometricShape) {
								geometricShape = (GeometricShape) o;
								textAnnotation = null;
								ImageAnnotation theAnnotation = new ImageAnnotation(imageAnnotationIdentifier.Teaching, imageReference, aimPatient, geometricShape);
								theAnnotation.setUser(user);
								theAnnotation.setEquipment(equipment);
								String output = theAnnotation.toXML();
								//annotations a = new annotations(0, Integer.parseInt(aimPatient.getPatientID()), output, study.getInstanceUID(),
								//		series.getInstanceUID(), user.getLoginName(), new Date(), user.getLoginName(), new Date());
								String[] args = {output, study.getInstanceUID(), series.getInstanceUID(), aimImg.getSopInstanceUID(), Integer.toString(s.getID())};
								AIMXML.add(args);
								AIMParser.parseString(output);
							}
							else if (o instanceof TextAnnotation) {
								textAnnotation = (TextAnnotation) o;
								geometricShape = null;
								ImageAnnotation theAnnotation = new ImageAnnotation(imageAnnotationIdentifier.Teaching, imageReference, aimPatient, textAnnotation);
								theAnnotation.setUser(user);
								theAnnotation.setEquipment(equipment);
								String output = theAnnotation.toXML();
								//annotations a = new annotations(0, Integer.parseInt(aimPatient.getPatientID()), output, study.getInstanceUID(),
								//		series.getInstanceUID(), user.getLoginName(), new Date(), user.getLoginName(), new Date());
								String[] args = {output, study.getInstanceUID(), series.getInstanceUID(), aimImg.getSopInstanceUID(), Integer.toString(s.getID())};
								AIMXML.add(args);
							}
						}
					}
				}
			}
		}
		// ApplicationContext.getContext().setAIMXML(AIMXML); ***
	}
}

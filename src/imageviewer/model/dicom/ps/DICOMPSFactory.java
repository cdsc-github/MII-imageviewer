/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom.ps;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import imageviewer.model.DataLayer;
import imageviewer.model.dicom.DICOMHeader;
import imageviewer.model.dicom.DICOMTags;
import imageviewer.model.dicom.DICOMTagMap;
import imageviewer.model.dl.ShapeDataLayer;

import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.wl.WindowLevel;

import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.AnnotationPSObjectFactory;
import imageviewer.ui.annotation.StylizedShape;
import imageviewer.ui.image.BasicImagePanel;

// =======================================================================

public class DICOMPSFactory {

	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("HHmmss");

	public static void populate(DICOMPresentationState dps, DICOMHeader dh) {

		// Do the basic patient information first based on demographics

		dps.setEthnicGroup((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00102160))));
		dps.setOccupation((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00102180))));
		dps.setOtherPatientIDs((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00101000))));
		dps.setOtherPatientNames((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00101001))));
		dps.setPatientAge((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00101010))));
		dps.setPatientBirthDate(dh.getPatientBirthdate());
		dps.setPatientBirthTime(dh.getPatientBirthTime());
		dps.setPatientComments((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00104000))));
		dps.setPatientID(dh.getPatientID());
		dps.setPatientName(dh.getPatientName());
		dps.setPatientSex(dh.getPatientSex());
		dps.setPatientSize((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00101020))));
		dps.setPatientWeight((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00101030))));

		// Study information

		dps.setAccessionNumber(dh.getStudyAccessionNumber());
		dps.setAdditionalPatientHistory(dh.getAdditionalPatientHistory());
		dps.setAdmittingDiagnosesCodeSeq((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00081084))));
		dps.setAdmittingDiagnosesDescription((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00081080))));
		dps.setPhysicianOfRecord((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00081048))));
		dps.setProcedureCodeSequence((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00081032))));
		dps.setReferencedSOPClassUID(dh.getSOPClassUID());
		dps.setReferencedSOPInstanceUID(dh.getSOPInstanceUID());
		dps.setReferencedStudySequence((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00081110))));
		dps.setReferringPhysicianName(dh.getStudyReferringPhysician());
		dps.setStudyDate(dh.getStudyDate());
		dps.setStudyDescription(dh.getStudyDescription());
		dps.setStudyID(Integer.toString(dh.getStudyID()));
		dps.setStudyInstanceUID(dh.getStudyInstanceUID());
		dps.setStudyTime(dh.getStudyTime());

		// Scanner and institutional information
		
		dps.setDeviceSerialNumber((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00181000))));
		dps.setInstitutionAddress((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00080081))));
		dps.setInstitutionDepartmentName((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00081040))));
		dps.setInstitutionName(dh.getStudyInstitution());
		dps.setLastCalibrationDate((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00181200))));
		dps.setLastCalibrationTime((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00181201))));
		dps.setManufacturer(dh.getSeriesManufacturer());
		dps.setManufacturerModel(dh.getSeriesManufacturerModel());
		dps.setPixelPaddingValue((short)dh.getPixelPaddingValue());
		dps.setSoftwareVersion((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00181020))));
		dps.setSpatialResolution((String)dh.doLookup(DICOMTagMap.doLabelLookup(new Integer(0x00181050))));
		dps.setStationName(dh.getSeriesStationName());

		// Series information

		dps.setSeriesInstanceUID(dh.getSeriesInstanceUID());
		dps.setSeriesNumber((short)dh.getSeriesNumber());
		dps.setSeriesDate(dh.getSeriesDate());
		dps.setSeriesTime(dh.getSeriesTime());
		dps.setSeriesModality(dh.getSeriesImageModality());
		dps.setSeriesDescription(dh.getSeriesDescription());

		double[] pixelSpacingArray=dh.getImagePixelSpacingArray();
		dps.setPresentationPixelSpacing(new String[] {Double.toString(pixelSpacingArray[0]),Double.toString(pixelSpacingArray[1])});
	}

	// =======================================================================
	// Set image properties for presentation state

	public static void populate(DICOMPresentationState dps, BasicImagePanel bip) {
		
		RenderingProperties rp=bip.getPipelineRenderer().getRenderingProperties();
		WindowLevel wl=(WindowLevel)rp.getProperty(RenderingProperties.WINDOW_LEVEL);
		double rescaleSlope=wl.getRescaleSlope();
		double rescaleIntercept=wl.getRescaleIntercept();
		float rotationAngle=(float)Math.toDegrees(((Float)rp.getProperty(RenderingProperties.ROTATION)).floatValue());
		if (rotationAngle<0) rotationAngle=360+rotationAngle;
		boolean vFlipState=((Boolean)rp.getProperty(RenderingProperties.VERTICAL_FLIP)).booleanValue();
		boolean hFlipState=((Boolean)rp.getProperty(RenderingProperties.HORIZONTAL_FLIP)).booleanValue();
		double translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
		double translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
		double scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();

		dps.setImageHorizontalFlip(hFlipState ? "Y" : "N");
		dps.setRescaleSlope(rescaleSlope);
		dps.setRescaleIntercept(rescaleIntercept);
		dps.setWindowCenter(wl.getWindow());
		dps.setWindowLevel(wl.getLevel());
		dps.setPresentationLUTShape("IDENTITY");
		if (vFlipState) rotationAngle+=180;
		if (rotationAngle>=360) rotationAngle=rotationAngle % 360;
		dps.setImageRotation((short)rotationAngle);

		dps.setPresentationPixelAspectRatio(new String[] {"1","1"});
		if (scale==1) {
			dps.setPresentationSizeMode("TRUE SIZE");
			dps.setPresentationPixelMagnificationRatio(1f);
		} else {
			dps.setPresentationSizeMode("MAGNIFY");
			dps.setPresentationPixelMagnificationRatio((float)scale);
		}
		dps.setDisplayedAreaTopLeftHandCorner(new short[] {(short)translateX,(short)translateY});
		dps.setDisplayedAreaBottomRightHandCorner(new short[] {(short)bip.getWidth(),(short)bip.getHeight()});

		// Handle annotations

		ShapeDataLayer sdl=(ShapeDataLayer)bip.getSource().findDataLayer(DataLayer.SHAPE);
		if (sdl!=null) {
			GraphicLayer gl=new GraphicLayer();
			gl.setGraphicLayer(DataLayer.SHAPE);
			gl.setGraphicLayerOrder((short)1);
			dps.addGraphicLayer(gl);
			ArrayList<StylizedShape> shapes=sdl.getShapes();
			if (shapes!=null) {
				for (int loop=0, n=shapes.size(); loop<n; loop++) {
					StylizedShape s=shapes.get(loop);
					if (s instanceof Annotation) {
						Object o=AnnotationPSObjectFactory.createObject((Annotation)s,DataLayer.SHAPE);
						if (o instanceof GraphicObject) dps.addGraphicObject((GraphicObject)o);
						else if (o instanceof TextObject) dps.addTextObject((TextObject)o);
					}
				}
			}
		}
	}

	// =======================================================================

	public static DICOMPresentationState generate(String presentationCreatorName, String presentationDescription, String presentationLabel) {

		DICOMPresentationState dps=new DICOMPresentationState();
		dps.setPresentationCreatorName(presentationCreatorName);
		Date currentDate=new Date();
		dps.setPresentationCreationDate(DATE_FORMAT1.format(currentDate));
		dps.setPresentationCreationTime(DATE_FORMAT2.format(currentDate));
		dps.setPresentationLabel(presentationLabel);
		dps.setPresentationDescription(presentationDescription);
		return dps;
	}

	// =======================================================================

	public static ArrayList<Annotation> createAnnotations(DICOMPresentationState dps) {

		ArrayList<Annotation> annotationList=new ArrayList<Annotation>();
		ArrayList<GraphicObject> graphicObjectList=dps.getGraphicObjects();
		for (GraphicObject go : graphicObjectList) {
			Annotation a=AnnotationPSObjectFactory.createAnnotation(go);
			if (a!=null) annotationList.add(a);
		}
		return annotationList;
	}

	// =======================================================================
	
	private DICOMPSFactory() {}
}

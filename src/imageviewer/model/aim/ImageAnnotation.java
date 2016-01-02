/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

import java.util.ArrayList;

import imageviewer.model.aim.imagereference.ImageReference;
import imageviewer.model.aim.markup.GeometricShape;

public class ImageAnnotation extends AIMAnnotation{
	
	public static enum imageAnnotationIdentifier {
		
		RECIST_Baseline_Target_Lesion, RECIST_Baseline_NonTarget_Lesion, RECIST_Followup_Target_Lesion, 
		RECIST_Followup_NonTarget_Lesion, LIDC_Chest_CT_Nodule, Brain_Tumor_Baseline_Target_Lesion,
		Brain_Tumor_Followup_Target_Lesion, Teaching, Quality_Control, Clinical_Finding, Other;
		
	}
	
	imageAnnotationIdentifier type = null;
	ImageReference imageReference;
	Patient patient;
	ArrayList<GeometricShape> geometricShapeCollection = new ArrayList<GeometricShape>();
	ArrayList<TextAnnotation> textAnnotationCollection = new ArrayList<TextAnnotation>();
	//ArrayList<ImageReference> imageReferenceCollection;
	//ProbabilityMap[] probabilityMapCollection;
	
	public ImageAnnotation(imageAnnotationIdentifier type){ this.type = type; }
	public ImageAnnotation(imageAnnotationIdentifier type, ImageReference imageReference, Patient patient, GeometricShape geometricShape) {
		this.type = type;
		this.imageReference = imageReference;
		this.patient = patient;
		addGeometricShape(geometricShape);
	}
	public ImageAnnotation(imageAnnotationIdentifier type, ImageReference imageReference, Patient patient, TextAnnotation textAnnotation) {
		this.type = type;
		this.imageReference = imageReference;
		this.patient = patient;
		addTextAnnotation(textAnnotation);
	}

	public imageAnnotationIdentifier getImageAnnotationType() {
		return type;
	}

	public void setImageAnnotationType(imageAnnotationIdentifier type) {
		this.type = type;
	}

	public ImageReference getImageReference() {
		return imageReference;
	}

	public void setImageReference(ImageReference imageReference) {
		this.imageReference = imageReference;
	}

	public ArrayList<GeometricShape> getGeometricShapeCollection() {
		return geometricShapeCollection;
	}
	public void addGeometricShape(GeometricShape gs){
		geometricShapeCollection.add(gs);
	}

	public void setGeometricShapeCollection(ArrayList<GeometricShape> geometricShapeCollection) {
		this.geometricShapeCollection = geometricShapeCollection;
	}

	public ArrayList<TextAnnotation> getTextAnnotationCollection() {
		return textAnnotationCollection;
	}

	public void setTextAnnotationCollection(ArrayList<TextAnnotation> textAnnotationCollection) {
		this.textAnnotationCollection = textAnnotationCollection;
	}
	
	public void addTextAnnotation(TextAnnotation ta){
		textAnnotationCollection.add(ta);
	}
	
	public String toXML(){
		String xmlString = "<ImageAnnotation id=\"" + id + "\" xsi:type=\"" + getImageAnnotationType() + "\"" + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
		
		xmlString = xmlString + user.toXML();
		xmlString = xmlString + patient.toXML();
		xmlString = xmlString + equipment.toXML();
		
		xmlString = xmlString + "<imageReferenceCollection>\n";
		xmlString = xmlString + imageReference.toXML();
		xmlString = xmlString + "</imageReferenceCollection>\n";
		
		//Calculations
		xmlString = xmlString + "<CalculationCollection>";
		int coordCount = calculationCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + calculationCollection.get(i).toXML();
		}
		xmlString = xmlString + "</CalculationCollection>\n";

		//Anatomic Entity
		xmlString = xmlString + "<AnatomicEntityCollection>";
		coordCount = anatomicEntityCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + anatomicEntityCollection.get(i).toXML();
		}
		xmlString = xmlString + "</AnatomicEntityCollection>\n";
		
		//Imaging Observation
		xmlString = xmlString + "<ImagingObservationCollection>";
		coordCount = imagingObservationCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + imagingObservationCollection.get(i).toXML();
		}
		xmlString = xmlString + "</ImagingObservationCollection>\n";
		
		//Geometric Shapes
		xmlString = xmlString + "<GeometricShapeCollection>";
		coordCount = geometricShapeCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + geometricShapeCollection.get(i).toXML();
		}
		xmlString = xmlString + "</GeometricShapeCollection>\n";
		
		//Text Annotations
		xmlString = xmlString + "<TextAnnotationCollection>";
		coordCount = textAnnotationCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + textAnnotationCollection.get(i).toXML();
		}
		xmlString = xmlString + "</TextAnnotationCollection>\n";
		
		xmlString = xmlString + "</ImageAnnotation>\n";
		
		return xmlString;
	}

}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.calculation;

import java.util.ArrayList;

public class Calculation {
	
	public static enum AnnotationCalculationIdentifier {
		
		Absolute_Value, Angle, Surface_of_Revolution, Mean, Mode, Standard_Deviation, Standard_Error,
		Maximum, Minimum, Center_of_Area, Center_of_Mass_2D, Center_of_Mass_3D, Sum, Difference,
		Product, Quotient, SUV, Perfusion, Permeability, Blood_Volume, Percent_Stenosis, Length,
		Path_Length, Distance, Width, Depth, Diameter, Long_Axis, Short_Axis, Major_Axis, Minor_Axis,
		Perpendicular_Axis, Radius, Perimeter, Circumference, Diameter_of_Circumscribed_Circle, 
		Height,	Area, Area_of_Defined_Region, Volume, Volume_Estimated_from_Single_2D_Region, 
		Volume_Estimated_from_Two_NonCoplanar_2D_Regions, 
		Volume_Estimated_from_Three_or_More_NonCoplanar_2D_Regions, Volume_of_Sphere,
		Volume_of_Ellipsoid, Volume_of_Circumscribed_Sphere, 
		Volume_of_Bouding_Three_Dimensional_Region;
		
	}
	
	int id;
	String uid;
	String description;
	String mathML;
	AnnotationCalculationIdentifier type = null;
	ArrayList<CalculationResult> calculationResultCollection;
	
	public Calculation() {}
	public Calculation(String description, AnnotationCalculationIdentifier type) {
		setDescription(description);
		setType(type);
		
		setUid("0");
		setId(0);
		setMathML("");
	}
	
	public void addCalculationResult(CalculationResult cr){
		calculationResultCollection.add(cr);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMathML() {
		return mathML;
	}
	public void setMathML(String mathML) {
		this.mathML = mathML;
	}
	public AnnotationCalculationIdentifier getType() {
		return type;
	}
	public void setType(AnnotationCalculationIdentifier type) {
		this.type = type;
	}

	public String toXML() {
		String xmlString = "<Calculation description=\"" + getDescription() + "\" mathML=\"" + getMathML() + "\" "
								+ "type=\"" + getType() + "\" uid=\"" + getUid() + "\" id=\"" + id + "\">\n";
		
		xmlString = xmlString + "<CalculationResultCollection>";
		int coordCount = calculationResultCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + calculationResultCollection.get(i).toXML() + "\n";
		}
		xmlString = xmlString + "</CalculationResultCollection>\n";
		
		xmlString = xmlString + "</Calculation>\n";
		return xmlString;
	}
	
	

}

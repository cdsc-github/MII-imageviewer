/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.calculation;

import java.util.ArrayList;

public class CalculationResult {
	
	public static enum CalculationResultIdentifier {
		Scalar, Vector, Histogram, Matrix, Array;		
	}
	
	int id;
	int numberOfDimensions;
	String unitOfMeasure;
	CalculationResultIdentifier type = null;
	
	ArrayList<Dimension> dimensionCollection;
	ArrayList<Data> dataCollection;
	
	public CalculationResult() {}
	public CalculationResult(int numDim, String unit, CalculationResultIdentifier type, Dimension d) {
		setNumberOfDimensions(numDim);
		setUnitOfMeasure(unit);
		setType(type);
		addDimension(d);		
	}
	
	public ArrayList<Dimension> getDimensionCollection(){
		return dimensionCollection;
	}
	
	public void setDimensionCollection(ArrayList<Dimension> dim){
		dimensionCollection = dim;
	}
	
	public void addDimension(Dimension dim){
		dimensionCollection.add(dim);
	}
	
	public ArrayList<Data> getData(){
		return dataCollection;
	}
	
	public void setData(ArrayList<Data> data){
		this.dataCollection = data;
	}
	
	public void addData(Data data){
		dataCollection.add(data);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumberOfDimensions() {
		return numberOfDimensions;
	}

	public void setNumberOfDimensions(int numberOfDimensions) {
		this.numberOfDimensions = numberOfDimensions;
	}

	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	public CalculationResultIdentifier getType() {
		return type;
	}

	public void setType(CalculationResultIdentifier type) {
		this.type = type;
	}

	public String toXML() {

		String xmlString = "<CalculationResult id=\"" + id + "\" numberOfDimenions=\"" + getNumberOfDimensions() + "\" type=\"" + getType() + "\" "
								+ "unitOfMeasure=\"" + getUnitOfMeasure() + "\">\n";
		
		xmlString = xmlString + "<DataCollection>";
		int coordCount = dataCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + dataCollection.get(i).toXML() + "\n";
		}
		xmlString = xmlString + "</DataCollection>\n";
		
		xmlString = xmlString + "<DimensionCollection>";
		coordCount = dimensionCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + dimensionCollection.get(i).toXML() + "\n";
		}
		xmlString = xmlString + "</DimensionCollection>\n";
		
		xmlString = xmlString + "</CalculationResult>\n";
		return xmlString;
	}
	
		

}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

import java.util.ArrayList;

public class ImagingObservation {
	
	int id;
	String codeMeaning;
	String codingSchemeDesignator;
	String codeValue;
	String comment;
	
	ArrayList<ImagingObservationCharacteristic> imagingObservationCharacteristicCollection;
	
	public ImagingObservation() {}
	public ImagingObservation(String codeMeaning, String codingSchemeDesignator, String codeValue) {
		this.codeMeaning = codeMeaning;
		this.codingSchemeDesignator = codingSchemeDesignator;
		this.codeValue = codeValue;
		this.comment = "";
		this.id=0;
	}
	public ImagingObservation(String codeMeaning, String codingSchemeDesignator, String codeValue, String comment) {
		this.codeMeaning = codeMeaning;
		this.codingSchemeDesignator = codingSchemeDesignator;
		this.codeValue = codeValue;
		this.comment = comment;
		this.id=0;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCodeMeaning() {
		return codeMeaning;
	}
	public void setCodeMeaning(String codeMeaning) {
		this.codeMeaning = codeMeaning;
	}
	public String getCodingSchemeDesignator() {
		return codingSchemeDesignator;
	}
	public void setCodingSchemeDesignator(String codingSchemeDesignator) {
		this.codingSchemeDesignator = codingSchemeDesignator;
	}
	public String getCodeValue() {
		return codeValue;
	}
	public void setCodeValue(String codeValue) {
		this.codeValue = codeValue;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String toXML(){
		String xmlString = "<ImagingObservation codeMeaning=\"" + getCodeMeaning() + "\" codeValue=\"" + getCodeValue()
										+ "\" codingSchemeDesignator=\"" + getCodingSchemeDesignator() + "\" id=\"" + id + "\">\n";
		
		xmlString = xmlString + "<ImagingObservationCharacteristicCollection>";
		int coordCount = imagingObservationCharacteristicCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + imagingObservationCharacteristicCollection.get(i).toXML() + "\n";
		}
		xmlString = xmlString + "</ImagingObservationCharacteristicCollection>\n";
		
		xmlString = xmlString + "</ImagingObservation>\n";
		return xmlString;
	}

}

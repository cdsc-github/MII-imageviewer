/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

public class ImagingObservationCharacteristic {
	
	int id;
	String codeMeaning;
	String codingSchemeDesignator;
	String codeValue;
	String comment;
	
	public ImagingObservationCharacteristic() {}

	public ImagingObservationCharacteristic(String codeMeaning, String codingSchemeDesignator, String codeValue) {
		this.codeMeaning = codeMeaning;
		this.codingSchemeDesignator = codingSchemeDesignator;
		this.codeValue = codeValue;
		this.comment = "";
		this.id=0;
	}
	
	public ImagingObservationCharacteristic(String codeMeaning, String codingSchemeDesignator, String codeValue, String comment) {
		this.codeMeaning = codeMeaning;
		this.codingSchemeDesignator = codingSchemeDesignator;
		this.codeValue = codeValue;
		this.comment = comment;
		this.id=0;
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
		String xmlString = "<ImagingObservationCharacteristic codeMeaning=\"" + getCodeMeaning() + "\" codeValue=\"" + getCodeValue()
		+ "\" codingSchemeDesignator=\"" + getCodingSchemeDesignator() + "\" comment=\"" + getComment() + "\" id=\"" + id + "\">\n";
		xmlString = xmlString + "</ImagingObservationCharacteristic>\n";		
		return xmlString;
	}

}

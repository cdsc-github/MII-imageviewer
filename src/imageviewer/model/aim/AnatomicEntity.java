/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

import java.util.ArrayList;

public class AnatomicEntity {
	
	int id;
	String codeMeaning;
	String codingSchemeDesignator;
	String codeValue;
	
	ArrayList<AnatomicEntity> relatedAnatomicEntity;
	
	public AnatomicEntity() {}
	public AnatomicEntity(String codeMeaning, String codingSchemeDesignator, String codeValue) {
		super();
		this.codeMeaning = codeMeaning;
		this.codingSchemeDesignator = codingSchemeDesignator;
		this.codeValue = codeValue;
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
	
	public String toXML(){
		String xmlString = "<AnatomicEntity codeMeaning=\"" + getCodeMeaning() + "\" codeValue=\"" + getCodeValue() + "\""
											+ "codingSchemeDesignator=\""+ getCodingSchemeDesignator() + "\" + id=\"" + id + "\"";
		
		if(relatedAnatomicEntity.size() > 0){
			xmlString = xmlString + ">\n";
			xmlString = xmlString + "<relatedAnatomicEntity>";
			int coordCount = relatedAnatomicEntity.size();
			for(int i = 0; i<coordCount; i++){
				xmlString = xmlString + relatedAnatomicEntity.get(i).toXML() + "\n";
			}
			xmlString = xmlString + "</relatedAnatomicEntity>\n";
			xmlString = xmlString + "</AnatomicEntity>";
		} else
			xmlString = xmlString + " />\n";

		return xmlString;
	}

}

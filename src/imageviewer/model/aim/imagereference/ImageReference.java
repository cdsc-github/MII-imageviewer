/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.imagereference;

public class ImageReference {
	
	int id;
	AIMStudy study;
	
	public ImageReference(AIMStudy study) {
		this.study = study;
		this.id = 0;
	}
	
	public String toXML(){
		String xmlString = "<ImageReference id=\"" + id + "\" xsi:type=\"DICOMImageReference\">\n";
		xmlString = xmlString + study.toXML();
		xmlString = xmlString + "</ImageReference>\n";
		return xmlString;		
	}

}

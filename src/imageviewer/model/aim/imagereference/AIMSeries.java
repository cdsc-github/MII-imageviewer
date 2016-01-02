/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.imagereference;

import java.util.ArrayList;

public class AIMSeries {
	
	int id;
	String instanceUID;
	String protocalName;
	String modality;
	ArrayList<AIMImage> imageCollection = new ArrayList<AIMImage>();
	
	public AIMSeries(AIMImage img) {
		addImage(img);
		this.id = 0;
	}
	public AIMSeries(AIMImage img, String instanceUID, String protocalName, String modality) {
		addImage(img);
		setInstanceUID(instanceUID);
		setProtocalName(protocalName);
		setModality(modality);
		this.id = 0;
	}

	public void setImageCollection(ArrayList<AIMImage> imgC){
		imageCollection = imgC;
	}
	
	public void addImage(AIMImage img) {
		imageCollection.add(img);		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getInstanceUID() {
		return instanceUID;
	}

	public void setInstanceUID(String instanceUID) {
		this.instanceUID = instanceUID;
	}

	public String getProtocalName() {
		return protocalName;
	}

	public void setProtocalName(String protocalName) {
		this.protocalName = protocalName;
	}

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String toXML() {
		String xmlString = "<series>\n<Series id=\"" + id + "\" modality=\"" + getModality() + "\" protocalName=\"" + getProtocalName() + "\""
										+ " instanceUID=\"" + getInstanceUID() + "\">\n";
		
		xmlString = xmlString + "<ImageCollection>\n";
		int coordCount = imageCollection.size();
		for(int i = 0; i<coordCount; i++){
			xmlString = xmlString + imageCollection.get(i).toXML();
		}
		xmlString = xmlString + "</ImageCollection>\n";	
		xmlString = xmlString + "</Series>\n</series>\n";		
		return xmlString;
	}
	
	

}

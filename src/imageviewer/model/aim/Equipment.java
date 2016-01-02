/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

public class Equipment {
	
	int id = 0;
	String manufacturer = "";
	String manufacturerModelName = "";
	String softwareVersion = "";
	
	public Equipment() {}
	public Equipment(String manufacturer, String manufacturerModelName, String softwareVersion) {
		this.manufacturer = manufacturer;
		this.manufacturerModelName = manufacturerModelName;
		this.softwareVersion = softwareVersion;
		this.id=0;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getManufacturerModelName() {
		return manufacturerModelName;
	}
	public void setManufacturerModelName(String manufacturerModelName) {
		this.manufacturerModelName = manufacturerModelName;
	}
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public String toXML() {
		String xmlString = "<equipment>\n";
		xmlString = xmlString + "<Equipment id=\"" + id + "\" manufacturer=\"" + getManufacturer() + "\""
												   + " manufacturerModelName=\"" + getManufacturerModelName() + "\""
												   + " softwareVersion=\"" + getSoftwareVersion() + "\" />\n";
		xmlString =  xmlString + "</equipment>\n";
		return xmlString;
	}	

}

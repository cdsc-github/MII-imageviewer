/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

import java.util.Date;

public class Patient {
	
	int id = 0;
	String name;
	String patientID;
	Date birthDate;
	String sex;
	String ethnicGroup;
	
	public Patient() {}
	public Patient(String name, String patientID, Date birthDate, String sex, String ethnicGroup) {
		this.name = name;
		this.patientID = patientID;
		this.birthDate = birthDate;
		this.sex = sex;
		this.ethnicGroup = ethnicGroup;
		this.id=0;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPatientID() {
		return patientID;
	}
	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getEthnicGroup() {
		return ethnicGroup;
	}
	public void setEthnicGroup(String ethnicGroup) {
		this.ethnicGroup = ethnicGroup;
	}

	public String toXML() {
		String xmlString = "<patient>\n";
		xmlString = xmlString + "<Patient id=\"" + id + "\" birthDate=\"" + getBirthDate() + "\""
												 + " name=\"" + getName() + "\" patientID=\"" + getPatientID() + "\""
												 + " sex=\"" + getSex() + "\" />\n";
		xmlString = xmlString + "</patient>\n";
		return xmlString;
	}
	
	

}

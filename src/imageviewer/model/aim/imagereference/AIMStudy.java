/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.imagereference;

import java.util.Date;

public class AIMStudy {
	
	int id;
	String instanceUID;
	Date date;
	AIMSeries series;
	
	public AIMStudy(AIMSeries series){
		this.series = series;
	}
	public AIMStudy(AIMSeries series, String instanceUID, Date date){
		this.series = series;
		setInstanceUID(instanceUID);
		setDate(date);
		this.id=0;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String toXML() {
		String xmlString = "<study>\n";
		xmlString = xmlString + "<Study date=\"" + getDate() + "\" instanceUID=\"" + getInstanceUID() + "\" id=\"" + id + "\">\n";
		xmlString = xmlString + series.toXML();
		xmlString = xmlString + "</Study>\n</study>\n";
		return xmlString;
	}
	

}

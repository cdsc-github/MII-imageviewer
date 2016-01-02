/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.net;

public class CachedJPEGHeader {

	String patientID=null, description=null, studyUID=null, seriesUID=null, date=null;

	public CachedJPEGHeader(String patientID, String description, String studyUID, String seriesUID, String date) {

		this.patientID=patientID;
		this.description=description;
		this.studyUID=studyUID;
		this.seriesUID=seriesUID;
		this.date=date;
	}

	public String getPatientID() {return patientID;}
	public String getDescription() {return description;}
	public String getStudyUID() {return studyUID;}
	public String getSeriesUID() {return seriesUID;}
	public String getDate() {return date;}

	public void setPatientID(String x) {patientID=x;}
	public void setDescription(String x) {description=x;}
	public void setStudyUID(String x) {studyUID=x;}
	public void setSeriesUID(String x) {seriesUID=x;}
	public void setDate(String x) {date=x;}

}

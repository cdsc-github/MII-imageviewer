/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

// =======================================================================

public class TrackingItem {

	String patientID=null, modality=null, description=null, sourceNode=null, message=null, status=null, uid=null, retries=null;
	String finalTargetNode=null, elapsedTrackingTime=null, trackingLocation=null;
	double percentComplete=0;
	long requestTime=0, startTime=0, finalTime=0;

	public TrackingItem(long requestTime, String patientID, String modality, String description, String sourceNode, String uid) {

		this.requestTime=requestTime;
		this.patientID=patientID;
		this.modality=modality;
		this.description=description;
		this.sourceNode=sourceNode;
		this.uid=uid;
	}

	// =======================================================================

	public long getRequestTime() {return requestTime;}
	public long getStartTime() {return startTime;}
	public long getFinalTime() {return finalTime;}

	public double getPercentComplete() {return percentComplete;}

	public String getDescription() {return description;}
	public String getElapsedTrackingTime() {return elapsedTrackingTime;}
	public String getFinalTargetNode() {return finalTargetNode;}
	public String getMessage() {return message;}
	public String getModality() {return modality;}
	public String getPatientID() {return patientID;}
	public String getRetries() {return retries;}
	public String getSourceNode() {return sourceNode;}
	public String getStatus() {return status;}
	public String getTrackingLocation() {return trackingLocation;}
	public String getUID() {return uid;}

	public void setElapsedTrackingTime(String x) {elapsedTrackingTime=x;}
	public void setFinalTargetNode(String x) {finalTargetNode=x;}
	public void setFinalTime(long x) {finalTime=x;}
	public void setMessage(String x) {message=x;}
	public void setPercentComplete(double x) {percentComplete=x;}
	public void setRetries(String x) {retries=x;}
	public void setStartTime(long x) {startTime=x;}
	public void setStatus(String x) {status=x;}
	public void setTrackingLocation(String x) {trackingLocation=x;}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.imagereference;

import java.util.Date;

public class AIMImage {
	
	int id;
	String sopClassUID;
	String sopInstanceUID;
	String laterality;
	String patientOrientationRow;
	String patientOrientationColumn;
	double pixelSpacingHorizontal;
	double pixelSpacingVertical;
	Date acquisitionDateTime;
	
	public AIMImage() {}

	public AIMImage(String sopClassUID, String sopInstanceUID, String laterality, String patientOrientationRow, String patientOrientationColumn,
			double pixelSpacingHorizontal, double pixelSpacingVertical, Date acquisitionDateTime) {
		this.sopClassUID = sopClassUID;
		this.sopInstanceUID = sopInstanceUID;
		this.laterality = laterality;
		this.patientOrientationRow = patientOrientationRow;
		this.patientOrientationColumn = patientOrientationColumn;
		this.pixelSpacingHorizontal = pixelSpacingHorizontal;
		this.pixelSpacingVertical = pixelSpacingVertical;
		this.acquisitionDateTime = acquisitionDateTime;
		this.id=0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSopClassUID() {
		return sopClassUID;
	}

	public void setSopClassUID(String sopClassUID) {
		this.sopClassUID = sopClassUID;
	}

	public String getSopInstanceUID() {
		return sopInstanceUID;
	}

	public void setSopInstanceUID(String sopInstanceUID) {
		this.sopInstanceUID = sopInstanceUID;
	}

	public String getLaterality() {
		return laterality;
	}

	public void setLaterality(String laterality) {
		this.laterality = laterality;
	}

	public String getPatientOrientationRow() {
		return patientOrientationRow;
	}

	public void setPatientOrientationRow(String patientOrientationRow) {
		this.patientOrientationRow = patientOrientationRow;
	}

	public String getPatientOrientationColumn() {
		return patientOrientationColumn;
	}

	public void setPatientOrientationColumn(String patientOrientationColumn) {
		this.patientOrientationColumn = patientOrientationColumn;
	}

	public double getPixelSpacingHorizontal() {
		return pixelSpacingHorizontal;
	}

	public void setPixelSpacingHorizontal(double pixelSpacingHorizontal) {
		this.pixelSpacingHorizontal = pixelSpacingHorizontal;
	}

	public double getPixelSpacingVertical() {
		return pixelSpacingVertical;
	}

	public void setPixelSpacingVertical(double pixelSpacingVertical) {
		this.pixelSpacingVertical = pixelSpacingVertical;
	}

	public Date getAcquisitionDateTime() {
		return acquisitionDateTime;
	}

	public void setAcquisitionDateTime(Date acquisitionDateTime) {
		this.acquisitionDateTime = acquisitionDateTime;
	}

	public String toXML() {
		String xmlString = "<Image sopClassUID=\"" + getSopClassUID() + "\" sopInstanceUID=\"" + getSopInstanceUID() + "\""
									+ " aquisitionDateTime = \"" + getAcquisitionDateTime() + "\" laterality=\"" + getLaterality() + "\""
									+ " patientOrientationColumn=\"" + getPatientOrientationColumn() + "\" patientOrientationRow=\"" + getPatientOrientationRow()
									+ "\" pixelSpacingHorizontal=\"" + getPixelSpacingHorizontal() + "\" pixelSpacingVertical=\"" + getPixelSpacingVertical()
									+ "\" id=\"" + id + "\">\n";
		// Add ImageView XML Here
		xmlString = xmlString + "<imageView />\n";
		xmlString = xmlString + "</Image>\n";
		return xmlString;
	}
	
	

}

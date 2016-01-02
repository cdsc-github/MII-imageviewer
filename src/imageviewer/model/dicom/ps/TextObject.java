/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom.ps;

import java.io.Serializable;

/**
 * This class stores the plain text object annotation.  In the old imageviewer they
 * weren't storing the numbers as in the standard.  They were treating the two points (blhc,trhc)
 * as a vector and determining the axis of rotation for the text from it.    
 */
public class TextObject implements Serializable {

	private static final long serialVersionUID = -7564562647065595663L;

	public String toString() { return "to["+annotationUnits+","+textValue+","+graphicLayer+"]";}
	private String graphicLayer;
	private String annotationUnits;
	private String textValue;
	private float[] tlhc;
	private float[] brhc;
	private String thj;
	
	public String getAnnotationUnits() {
		return annotationUnits;
	}
	public void setAnnotationUnits(String annotationUnits) {
		this.annotationUnits = annotationUnits;
	}
	public float[] getBrhc() {
		return brhc;
	}
	public void setBrhc(float[] brhc) {
		this.brhc = brhc;
	}
	public String getTextValue() {
		return textValue;
	}
	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}
	public String getThj() {
		return thj;
	}
	public void setThj(String thj) {
		this.thj = thj;
	}
	public float[] getTlhc() {
		return tlhc;
	}
	public void setTlhc(float[] tlhc) {
		this.tlhc = tlhc;
	}
	
	/**
	 */
	public String getGraphicLayer() {
		return graphicLayer;
	}
	public void setGraphicLayer(String graphicLayer) {
		this.graphicLayer = graphicLayer;
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

import imageviewer.model.aim.markup.MultiPoint;
import imageviewer.model.aim.markup.TwoDimensionCoordinate;

public class TextAnnotation extends AIMAnnotation{
	
	int id;
	String text;
	
	String font;
	String fontColor;
	String fontEffect;
	String fontSize;
	String fontStyle;
	String textJustify;
	String fontOpacity;
	
	MultiPoint connectorPoints;
	
	public TextAnnotation() {}
	public TextAnnotation(String text){ setText(text); }
	public TextAnnotation(String text, MultiPoint mp){ setText(text); setConnectorPoints(mp); }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public MultiPoint getConnectorPoints() {
		return connectorPoints;
	}

	public void setConnectorPoints(MultiPoint connectorPoints) {
		this.connectorPoints = connectorPoints;
	}
	
	public String toXML() {
		String xmlString = "\n<TextAnnotation id=\"" + id + "\" text=\"" + getText() + "\">\n";
		if (connectorPoints != null)
			xmlString = xmlString + connectorPoints.toXML();
		xmlString = xmlString + "</TextAnnotation>\n";
		return xmlString;
	}
}

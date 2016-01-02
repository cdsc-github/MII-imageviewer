/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim.calculation;

public class Dimension {
	
	int id;
	int index;
	int size;
	String label;
	
	public Dimension() {}
	public Dimension(int index, int size, String label){
		setId(0);
		setIndex(index);
		setSize(size);
		setLabel(label);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String toXML() {
		String xmlString ="<Dimension id=\"" + id + "\" index=\"" + getIndex() + "\" label=\"" + getLabel() + "\" size=\"" + getSize() + "\" />\n";
		return xmlString;
	}
	
	

}

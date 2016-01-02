/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import java.util.Hashtable;

public class PanelDescription {

	Hashtable<String,Object> properties=new Hashtable<String,Object>();
	int width=0, height=0, x=0, y=0;
	ControlGroupDescription container=null;

	public PanelDescription() {}

	// =======================================================================

	public Hashtable<String,Object> getProperties() {return properties;}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public int getX() {return x;}
	public int getY() {return y;}

	public Object getProperty(String name) {

		Object o=properties.get(name);
		return (o!=null) ? o : container.getProperty(name);
	}

	public ControlGroupDescription getContainer() {return container;}

	public void setProperty(String x, Object o) {properties.put(x,o);}
	public void setWidth(int x) {width=x;}
	public void setHeight(int x) {height=x;}
	public void setX(int x) {this.x=x;}
	public void setY(int x) {y=x;}
	public void setContainer(ControlGroupDescription x) {container=x;}
}

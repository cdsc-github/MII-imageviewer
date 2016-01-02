/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import java.util.ArrayList;
import java.util.Hashtable;

public class ControlGroupDescription {

	Hashtable<String,Object> properties=new Hashtable<String,Object>();
	ArrayList<PanelDescription> panels=new ArrayList<PanelDescription>();
	String type=null, target=null;
	LayoutDescription container=null;
	int id=0, x=0, y=0;

	public ControlGroupDescription() {}

	// =======================================================================

	public Hashtable<String,Object> getProperties() {return properties;}
	public ArrayList<PanelDescription> getPanels() {return panels;}
	public LayoutDescription getContainer() {return container;}

	public int getID() {return id;}
	public int getX() {return x;}
	public int getY() {return y;}

	public Object getProperty(String name) {

		Object o=properties.get(name);
		return (o!=null) ? o : container.getProperty(name);
	}

	public String getType() {return type;}
	public String getTarget() {return target;}

	public void addPanel(PanelDescription x) {panels.add(x);}
	public void removePanel(PanelDescription x) {panels.remove(x);}
	public void setProperty(String x, Object o) {properties.put(x,o);}
	public void setID(int x) {id=x;}
	public void setContainer(LayoutDescription x) {container=x;}
	public void setX(int x) {this.x=x;}
	public void setY(int x) {y=x;}
	public void setType(String x) {type=x;}
	public void setTarget(String x) {target=x;}
}

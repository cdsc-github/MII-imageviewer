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

// =======================================================================

public class LayoutDescription {

	Hashtable<String,Object> properties=new Hashtable<String,Object>();
	ArrayList<ControlGroupDescription> controlGroups=new ArrayList<ControlGroupDescription>();
	String name=null, renderingPipeline=null;
	int resolutionWidth=0, resolutionHeight=0;

	public LayoutDescription() {}

	// =======================================================================

	public Hashtable<String,Object> getProperties() {return properties;}
	public ArrayList<ControlGroupDescription> getControlGroups() {return controlGroups;}
	public String getName() {return name;}
	public String getRenderingPipelineName() {return renderingPipeline;}
	public int getResolutionWidth() {return resolutionWidth;}
	public int getResolutionHeight() {return resolutionHeight;}

	public Object getProperty(String name) {return properties.get(name);}

	public void setProperty(String x, Object o) {properties.put(x,o);}
	public void addControlGroup(ControlGroupDescription x) {controlGroups.add(x);}
	public void removeControlGroup(ControlGroupDescription x) {controlGroups.remove(x);}	
	public void setName(String x) {name=x;}
	public void setRenderingPipelineName(String x) {renderingPipeline=x;}
	public void setResolutionWidth(int x) {resolutionWidth=x;}
	public void setResolutionHeight(int x) {resolutionHeight=x;}

	// =======================================================================

	public String getResolutionTag() {return new String(resolutionWidth+"x"+resolutionHeight);}

	// =======================================================================

	public boolean hasKeyFor(String target) {

		for (int loop=0, n=controlGroups.size(); loop<n; loop++) {
			ControlGroupDescription cgd=controlGroups.get(loop);
			if (cgd.getTarget().contains(target)) return true;
		}
		return false;
	}
}

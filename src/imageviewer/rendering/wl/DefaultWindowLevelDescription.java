/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering.wl;

public class DefaultWindowLevelDescription {

	String name=null, modality=null, anatomy=null;
	String algorithmClass=null;
	int window=0, level=0;

	public DefaultWindowLevelDescription() {}
	public DefaultWindowLevelDescription(String name, String modality, String anatomy, int window, int level) {initialize(name,modality,anatomy,window,level,null);}
	public DefaultWindowLevelDescription(String name, String modality, String anatomy, int window, int level, String algorithmClass) {initialize(name,modality,anatomy,window,level,algorithmClass);}

	public void initialize(String name, String modality, String anatomy, int window, int level, String algorithmClass) {

		this.name=name;
		this.modality=modality;
		this.anatomy=anatomy;
		this.window=window;
		this.level=level;
		this.algorithmClass=algorithmClass;
	}
	
	// =======================================================================

	public String getName() {return name;}
	public String getModality() {return modality;}
	public String getAnatomy() {return anatomy;}
	public String getAlgorithmClass() {return algorithmClass;}

	public int getWindow() {return window;}
	public int getLevel() {return level;}

	public void setName(String x) {name=x;}
	public void setModality(String x) {modality=x;}
	public void setAnatomy(String x) {anatomy=x;}
	public void setAlgorithmClass(String x) {algorithmClass=x;}

	public void setWindow(int x) {window=x;}
	public void setLevel(int x) {level=x;}

	// =======================================================================

	public static String makeKey(String modality, String anatomy) {return new String("__"+modality+"|"+anatomy);}
	public String getKey() {return new String("__"+modality+"|"+anatomy);}

	// =======================================================================
	
	public String toString() {return new String("Window/level setting "+name+" ["+window+","+level+"]: "+getKey());}

	// =======================================================================
}

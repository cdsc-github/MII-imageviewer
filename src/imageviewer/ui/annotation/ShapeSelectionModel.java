/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import java.util.ArrayList;

public class ShapeSelectionModel {

	ArrayList<Selectable> currentSelections=new ArrayList<Selectable>();

	public ShapeSelectionModel() {}

	public void add(Selectable s) {currentSelections.add(s);}
	public void add(ArrayList selections) {currentSelections.addAll(selections);}

	public void remove(Selectable s) {currentSelections.remove(s);}
	public void remove(ArrayList selections) {for (Object o: selections) currentSelections.remove(o);}
	public void clear() {currentSelections.clear();}

	public ArrayList<Selectable> getCurrentSelections() {return currentSelections;}
}

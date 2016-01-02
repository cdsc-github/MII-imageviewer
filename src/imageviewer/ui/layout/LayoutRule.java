/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import java.util.ArrayList;

import imageviewer.model.PropertiedObject;

// =======================================================================

public class LayoutRule implements RuleConditional {

	String name=null, selectedLayout=null;
	ArrayList<RuleConditional> conditions=new ArrayList<RuleConditional>();

	public LayoutRule(String name, String selectedLayout) {this.name=name; this.selectedLayout=selectedLayout;}

	// =======================================================================

	public String getName() {return name;}
	public String getSelectedLayout() {return selectedLayout;}
	public ArrayList<RuleConditional> getConditions() {return conditions;}

	public void setName(String x) {name=x;}
	public void setSelectedLayout(String x) {selectedLayout=x;}
	public void setConditions(ArrayList<RuleConditional> x) {conditions=x;}

	public void addConditional(RuleConditional x) {conditions.add(x);}
	public void removeConditional(RuleConditional x) {conditions.remove(x);}

	// =======================================================================

	public boolean evaluate(PropertiedObject po) {

		// Run through each of the conditions associated with this rule.
		// This works as an if-then-else statement; stop when there's a
		// condition set that evaluates to true.

		for (int loop=0, n=conditions.size(); loop<n; loop++) {
			RuleConditional rc=conditions.get(loop);
			boolean result=rc.evaluate(po);
			if (result) return true;
		}
		return false;
	}
}

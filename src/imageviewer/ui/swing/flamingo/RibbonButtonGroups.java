/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

public class RibbonButtonGroups {

	private static RibbonButtonGroups INSTANCE=new RibbonButtonGroups();

	public static RibbonButtonGroups getInstance() {return INSTANCE;}

	Hashtable<String,ButtonGroup> groups=new Hashtable<String,ButtonGroup>();
	HashMap<String,ButtonModel> lastSelection=new HashMap<String,ButtonModel>();

	private RibbonButtonGroups() {}

	public void addButton(final String group, AbstractButton ab) {

		ButtonGroup bg=groups.get(group);
		if (bg==null) {
			bg=new ButtonGroup() {public void setSelected(ButtonModel bm, boolean b) {lastSelection.put(group,getSelection()); super.setSelected(bm,b);}};
			groups.put(group,bg); 
			lastSelection.put(group,null);
		}
		bg.add(ab);
	}

	public ButtonGroup getGroup(String name) {return groups.get(name);}

	public void restorePreviousSelection(String group) {

		ButtonGroup bg=groups.get(group); 
		ButtonModel bm=lastSelection.get(group);
		if (bm!=null) bg.setSelected(bm,true); else bg.clearSelection();
	}
}

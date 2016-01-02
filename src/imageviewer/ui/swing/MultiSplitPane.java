/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import javax.swing.border.EmptyBorder;

// =======================================================================
// Adapated from org.csgc.awt.MultiSplitPane in the NT:TNG package.

public class MultiSplitPane extends JPanel {

	private static final int DIVIDER_SIZE=2;
	private static final EmptyBorder EMPTY_BORDER=new EmptyBorder(0,0,0,0);

	// =======================================================================

	int orientation=JSplitPane.HORIZONTAL_SPLIT;

	public MultiSplitPane() {super();	setBackground(Color.black); setLayout(new GridLayout());}

	public MultiSplitPane(int orientation) {

		super();
		setBackground(Color.black); 
		setLayout(new GridLayout());
		this.orientation=orientation;
	}

	// =======================================================================

	public void add(JComponent component) {addComponent(component); revalidate();} //SwingUtilities.invokeLater(new AddComponent(component));}
	public void remove(JComponent component) {removeComponent(component); revalidate();} //SwingUtilities.invokeLater(new RemoveComponent(component));}

	// =======================================================================

	public int[] getDividerLocations() {return (getComponentCount()==1) ? getDividerLocations(getComponent(0)) : null;}

	private int[] getDividerLocations(Component c) {

		if (c instanceof JSplitPane) {
	    JSplitPane jsp=(JSplitPane)c;
			int[] topLocations=getDividerLocations(jsp.getTopComponent());
	    int[] bottomLocations=getDividerLocations(jsp.getBottomComponent());
			int arrayLength=(topLocations!=null) ? (1+topLocations.length) : 1;
			arrayLength+=(bottomLocations!=null) ? bottomLocations.length : 0;
			int[] allLocations=new int[arrayLength];
			int index=0;
			allLocations[0]=jsp.getDividerLocation(); index++;
			if (topLocations!=null) {
				System.arraycopy(topLocations,0,allLocations,index,topLocations.length);
				index+=topLocations.length;
			}
			if (bottomLocations!=null) System.arraycopy(bottomLocations,0,allLocations,index,bottomLocations.length);
			return allLocations;
		}
		return null;
	}

	// =======================================================================

	public void setDividerLocations(final int[] locations) {

		if (getComponentCount()>0) setDividerLocations(locations,0,getComponent(0));
		//SwingUtilities.invokeLater(new Runnable() {public void run() {if (getComponentCount()>0) setDividerLocations(locations,0,getComponent(0));}});
	}

	private int setDividerLocations(int[] locations, int index, Component c) {

		if (c instanceof JSplitPane) {
	    JSplitPane jsp=(JSplitPane)c;
	    if (index<locations.length) {
				jsp.setDividerLocation(locations[index++]);
				index=setDividerLocations(locations,index,jsp.getTopComponent());
				index=setDividerLocations(locations,index,jsp.getBottomComponent());
	    }
		}
		return index;
	}

	// =======================================================================

	public Component[] getComponents() {

		ArrayList<Component> al=new ArrayList<Component>();
		if (getComponentCount()>0) getComponents(al,getComponent(0));
		Component[] components=new Component[al.size()];
		return (Component[])al.toArray(components);
	}

	private void getComponents(ArrayList<Component> al, Component c) {

		if (c instanceof JSplitPane) {
	    JSplitPane jsp=(JSplitPane)c;
	    getComponents(al,jsp.getTopComponent());
	    getComponents(al,jsp.getBottomComponent());
		} else {
	    al.add(c);
		}
	}

	// =======================================================================
    
	private void setSplitWidth(Component c) {

		if (c instanceof JSplitPane) {
	    JSplitPane jsp=(JSplitPane)c;
	    jsp.setDividerSize(DIVIDER_SIZE);
	    setSplitWidth(jsp.getTopComponent());
	    setSplitWidth(jsp.getBottomComponent());
		}
	}

	// =======================================================================

	private void addComponent(JComponent c) {

		int n=getComponentCount();
		if (n==0) {
	    super.add(c);
		} else {
	    Component oldComponent=getComponent(0);
	    remove(oldComponent);
	    JSplitPane split=new JSplitPane(orientation,true);
			split.setBackground(Color.black);
	    split.setTopComponent(oldComponent);
	    split.setBottomComponent(c);
	    split.setBorder(EMPTY_BORDER);
	    split.setDividerSize(DIVIDER_SIZE);
			split.setResizeWeight(0.5);
	    super.add(split);
		}
	}
	// =======================================================================

	private void removeComponent(JComponent c) {

		Container container=c.getParent();
		if (container instanceof JPanel) {
	    super.remove(c);
		} else {
	    JSplitPane split=(JSplitPane)container;
	    Container parent=split.getParent();
	    if (split.getTopComponent()==c) {
				replaceInParent(parent,split,split.getBottomComponent());
	    } else {
				replaceInParent(parent,split,split.getTopComponent());
	    }
		}
	}

	// =======================================================================

  private void replaceInParent(Container parent, JSplitPane split, Component c) {

		if (parent instanceof JPanel) {
	    super.remove(split);
	    super.add(c);
		} else {
	    JSplitPane splitParent=(JSplitPane)parent;
	    if (splitParent.getTopComponent()==split) {
				splitParent.setTopComponent(c);
	    } else {
				splitParent.setBottomComponent(c);
	    }
		}
	}

	// =======================================================================
	/*
	private class AddComponent implements Runnable {
		JComponent jc=null;
		AddComponent(JComponent jc) {this.jc=jc;}
		public void run() {addComponent(jc); revalidate();}
	}

	private class RemoveComponent implements Runnable {
		JComponent jc=null;
		RemoveComponent(JComponent jc) {this.jc=jc;}
		public void run() {removeComponent(jc); revalidate();}
	}
	*/
}

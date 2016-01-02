/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.Color;
import java.awt.Graphics;

import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;

import imageviewer.ui.swing.event.MenuActionListenerFactory;

// =======================================================================

public abstract class DataPanelGroup extends UIPanel implements GroupedComponent, PropertyChangeListener, HierarchyBoundsListener {

	protected ArrayList<GroupedComponent> children=new ArrayList<GroupedComponent>();
	protected GroupedComponent parent=null;
	protected String tag=null;

	public DataPanelGroup() {super(); initialize();}
	public DataPanelGroup(GroupedComponent parent) {super(); this.parent=parent; initialize();}
	public DataPanelGroup(GroupedComponent parent, ArrayList children) {super(); this.parent=parent; this.children=children; initialize();}

	private void initialize() {((ViewManager)(MenuActionListenerFactory.getListener(ViewManager.class))).addGroupPropertyChangeListener(this);}

	// =======================================================================

	public ArrayList<GroupedComponent> getChildren() {return children;}
	public GroupedComponent getGroupParent() {return parent;}
	public VisualProperties getVisualProperties(int index, boolean copy) {return null;}
	public String getTag() {return tag;}

	public void setChildren(ArrayList<GroupedComponent> x) {children=x;}
	public void setGroupParent(GroupedComponent x) {parent=x;}
	public void addChild(GroupedComponent x) {children.add(x);}
	public void removeChild(GroupedComponent x) {children.remove(x);}
	public void setTag(String x) {tag=x;}

	// =======================================================================

	public void paintComponent(Graphics g) {super.paintComponent(g);}

	public void mouseClicked(MouseEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).mouseClicked(e);}
	public void mousePressed(MouseEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).mousePressed(e);}
	public void mouseReleased(MouseEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).mouseReleased(e);}
	public void mouseEntered(MouseEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).mouseEntered(e);}
	public void mouseExited(MouseEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).mouseExited(e);}
	public void mouseMoved(MouseEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).mouseMoved(e);}
	public void mouseDragged(MouseEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).mouseDragged(e);}
	public void mouseWheelMoved(MouseWheelEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).mouseWheelMoved(e);}

	public void keyPressed(KeyEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).keyPressed(e);}
	public void keyReleased(KeyEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).keyReleased(e);}
	public void keyTyped(KeyEvent e) {if (parent==null) e.consume(); else if (parent instanceof UIPanel) ((UIPanel)parent).keyTyped(e);}

	// =======================================================================

	public DataPanelGroup copy(GroupedComponent parent) {return null;}

	// =======================================================================

	public void ancestorMoved(HierarchyEvent he) {}
	public void ancestorResized(HierarchyEvent he) {}

	// =======================================================================

	public void propertyChange(PropertyChangeEvent pce) {}

	// =======================================================================

	protected void doCleanup() {((ViewManager)(MenuActionListenerFactory.getListener(ViewManager.class))).removeGroupPropertyChangeListener(this);}

}

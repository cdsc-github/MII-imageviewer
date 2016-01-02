/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Shape;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;

import imageviewer.ui.annotation.StylizedShape;
import imageviewer.ui.swing.event.MenuActionListenerFactory;

// =======================================================================

public abstract class DataPanel extends UIPanel implements GroupedComponent, PropertyChangeListener {

	protected static final Cursor HOURGLASS_CURSOR=new Cursor(Cursor.WAIT_CURSOR);
	protected static final Cursor DEFAULT_CURSOR=new Cursor(Cursor.DEFAULT_CURSOR);

	// =======================================================================

	protected GroupedComponent parent=null;	
	protected ArrayList<StylizedShape> temporaryShapes=new ArrayList<StylizedShape>();        // These shapes are transformed with the data's rendering properties
	protected ArrayList<StylizedShape> panelOverlayShapes=new ArrayList<StylizedShape>();     // These shapes are not transformed and are in the panel space

	public DataPanel() {super(); initialize();}
	public DataPanel(DataPanelGroup parent) {super(); this.parent=parent; initialize();}

	private void initialize() {((ViewManager)(MenuActionListenerFactory.getListener(ViewManager.class))).addPanelPropertyChangeListener(this);}

	// =======================================================================

	public abstract void paintLayers(Graphics g);

	public GroupedComponent getGroupParent() {return parent;}
	public ArrayList<GroupedComponent> getChildren() {return null;}
	public ArrayList<StylizedShape> getTemporaryShapes() {return temporaryShapes;}
	public ArrayList<StylizedShape> getPanelOverlayShapes() {return panelOverlayShapes;}

	public void setGroupParent(GroupedComponent x) {parent=x;}
	public void addChild(GroupedComponent x) {}
	public void removeChild(GroupedComponent x) {}

	public void addTemporaryShape(StylizedShape x) {temporaryShapes.add(x);}
	public void addTemporaryShape(Shape x) {temporaryShapes.add(new StylizedShape(x));}
	public void removeTemporaryShape(StylizedShape x) {temporaryShapes.remove(x);}
	public void clearTemporaryShapes() {temporaryShapes.clear();}

	public void addPanelOverlayShape(StylizedShape x) {panelOverlayShapes.add(x);}
	public void addPanelOverlayShape(Shape x) {panelOverlayShapes.add(new StylizedShape(x));}
	public void removePanelOverlayShape(StylizedShape x) {panelOverlayShapes.remove(x);}
	public void clearPanelOverlayShapes() {panelOverlayShapes.clear();}

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

	public void paintComponent(Graphics g) {super.paintComponent(g);}

	public void propertyChange(PropertyChangeEvent pce) {repaint();}

	// =======================================================================

	protected void doCleanup() {((ViewManager)(MenuActionListenerFactory.getListener(ViewManager.class))).removePanelPropertyChangeListener(this);}

	// =======================================================================

	public DataPanel copy() {return null;}

}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;
import imageviewer.model.ModelEvent;
import imageviewer.model.ModelEvent.EventType;

import imageviewer.ui.layout.GroupedLayout;
import imageviewer.ui.layout.Layout;
import imageviewer.ui.layout.LayoutFactory;
import imageviewer.ui.layout.LayoutPlan;
import imageviewer.ui.swing.border.AlphaFilledCurvedBorder;

// =======================================================================

public class TabbedDataPanel extends JTabbedPane implements ActionListener, ChangeListener {

	private static final JCheckBox ASK_CB=new JCheckBox("Do not show this message again.",false);

	protected static final Cursor HOURGLASS_CURSOR=new Cursor(Cursor.WAIT_CURSOR);
	protected static final Cursor DEFAULT_CURSOR=new Cursor(Cursor.DEFAULT_CURSOR);

	// =======================================================================

	ApplicationPanel ap=null;
	JMenu moveSubMenu=null, copySubMenu=null, closeSubMenu=null;
	JPopupMenu popupMenu=null;
	boolean mouseOverCloseButton=false;
	int selectedTabIndex=-1;

	public TabbedDataPanel(ApplicationPanel ap) {
		
		super(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
		this.ap=ap;
		setBackground(Color.black);
		addMouseListener(new TabbedPaneListener());
		addMouseMotionListener(new TabbedPaneListener());
		setMinimumSize(new Dimension(128,0));
		setBorder(new EmptyBorder(0,2,0,2));
		setRequestFocusEnabled(false);                       // Need to hack for Java 1.5 (Bug ID 5056403), as it will cause problems when removing tabs; OK in 1.6

		// Create the required popup menu

		moveSubMenu=new JMenu("Move");
		moveSubMenu.add(Box.createRigidArea(new Dimension(0,2))); 
		addMenuCommand(moveSubMenu,"Move to new panel");
		moveSubMenu.addSeparator();
		addMenuCommand(moveSubMenu,"Move to left panel");
		addMenuCommand(moveSubMenu,"Move to right panel");
	
		copySubMenu=new JMenu("Copy");
		copySubMenu.add(Box.createRigidArea(new Dimension(0,2))); 
		addMenuCommand(copySubMenu,"Copy to new panel");
		copySubMenu.addSeparator();
		addMenuCommand(copySubMenu,"Copy to left panel");
		addMenuCommand(copySubMenu,"Copy to right panel");

		closeSubMenu=new JMenu("Close");
		closeSubMenu.add(Box.createRigidArea(new Dimension(0,2))); 
		addMenuCommand(closeSubMenu,"Close tab");
		closeSubMenu.addSeparator();
		addMenuCommand(closeSubMenu,"Close series");
		addMenuCommand(closeSubMenu,"Close study");
	
		// Add some extra space to the top of this popupmenu; it seems
		// shrunken for some reason, otherwise.

		popupMenu=new JPopupMenu();
		popupMenu.setBorder(new CompoundBorder(new AlphaFilledCurvedBorder(5),new EmptyBorder(2,0,0,0)));
		addMenuCommand(popupMenu,moveSubMenu);
		addMenuCommand(popupMenu,copySubMenu);
		popupMenu.addSeparator();
		addMenuCommand(popupMenu,closeSubMenu);

		addChangeListener(this); 
		putClientProperty("TABBED_CLOSE_BUTTONS",Boolean.TRUE);
		setVisible(false);
	}		

	// =======================================================================

	public void addImages(ArrayList<? extends ImageSequenceGroup> imageStudies) {

		LayoutPlan lp=LayoutFactory.selectLayouts(imageStudies);
		for (int i=0, n=lp.size(); i<n; i++) {
			GroupedLayout gl=lp.getGroupedLayout(i);
			Layout l=new Layout(gl);
			JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); 
			panel.setBackground(Color.black);
			panel.add(l);
			addTab(gl.getShortTabDescription(),panel);
			setToolTipTextAt(getTabCount()-1,gl.getLongTabDescription());
			ap.setActiveLayout(l);
		}
	}

	// =======================================================================

	public void insertTab(String title, Icon icon, Component component, String tip, int index) {super.insertTab(title,icon,component,tip,index); setVisible(true);}
	public void removeTabAt(int index) {super.removeTabAt(index); int n=getComponentCount(); if (n<=3) setVisible(false);}

	private void addMenuCommand(JMenu menu, String actionCommand) {JMenuItem mi=menu.add(actionCommand); mi.setOpaque(false); mi.addActionListener(this);}
	private void addMenuCommand(JPopupMenu menu, JMenuItem submenu) {JMenuItem mi=menu.add(submenu); mi.setOpaque(false); mi.addActionListener(this);}

	// =======================================================================

	private void updatePopupMenu() {

		moveSubMenu.getItem(1).setEnabled((getTabCount()==1) ? false : true);                      // If there's only one tab, you can't move it to a new panel
		boolean hasLeft=(ap.getLeftComponent(this)!=null);
		boolean hasRight=(ap.getRightComponent(this)!=null);
		moveSubMenu.getItem(3).setEnabled(hasLeft);                                                // Is there a component to the left that we can move to?
		moveSubMenu.getItem(4).setEnabled(hasRight);                                               // Is there a component to the right that we can move to?
		copySubMenu.getItem(3).setEnabled(hasLeft);                                                // Is there a component to the left that we can copy to?
		copySubMenu.getItem(4).setEnabled(hasRight);                                               // Is there a component to the right that we can copy to?
		closeSubMenu.getItem(3).setEnabled(true);
		closeSubMenu.getItem(4).setEnabled(true);
	}

	// =======================================================================

	public void showPopup(MouseEvent e) {

		if (!e.isPopupTrigger()) return;
		TabbedPaneUI ui=getUI();
		selectedTabIndex=ui.tabForCoordinate(this,e.getX(),e.getY());
		updatePopupMenu();
		popupMenu.show(e.getComponent(),e.getX(),e.getY());
	}

	// =======================================================================
	/**
	 * Set the application context to a move in progress state so we
	 * don't nuke any of the underlying components; they'll be re-added.
	 * Have to set the value back to false in a runnable to make sure it
	 * happens *AFTER* the AWT eventQueue processes all of the
	 * removeNotify calls that will occur. Grrr...
	 *
	 * @param target a value of type 'TabbedDataPanel'
	 */

	private void moveTab(TabbedDataPanel target) {

		String tabTitle=getTitleAt(selectedTabIndex);
		String tooltip=getToolTipTextAt(selectedTabIndex);
		Component c=getComponentAt(selectedTabIndex);
		ApplicationContext.getContext().setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(true));
		remove(selectedTabIndex);
		target.addTab(tabTitle,null,c,tooltip);
		if (getTabCount()==0) ap.removeFromMultiSplitPane(this);
		ApplicationContext.getContext().setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(false));
}

	// =======================================================================
	/**
	 * Copy the current tab into a new panel...execute a deep copy, in
	 * essence.
	 *
	 * @param target a value of type 'TabbedDataPanel' for which the copy should occur.
	 */
	
	private void copyTab(TabbedDataPanel target) {

		Component c=getComponentAt(selectedTabIndex);                                              // This should be the containing JPanel...
		if (c instanceof JPanel) {
			getParent().setCursor(HOURGLASS_CURSOR); 
			JPanel container=(JPanel)c;
			Layout l=(Layout)container.getComponent(0);
			String tabTitle=getTitleAt(selectedTabIndex);
			String tooltip=getToolTipTextAt(selectedTabIndex);
			JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); 
			panel.setBackground(Color.black);
			panel.add(l.copy());
			target.addTab(tabTitle,null,panel,tooltip);
			getParent().setCursor(DEFAULT_CURSOR); 
		}
	}

	// =======================================================================

	private boolean closeTabConfirm(String obj) {

		// Confirm the close with the user for the currently selected tab...
		
		Boolean askLayoutChange=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.ASK_CLOSE_TAB);
		if ((askLayoutChange==null)||(askLayoutChange.booleanValue())) {
			int response=ApplicationPanel.getInstance().showDialog("Are you sure you want to close this "+obj+"? All associated changes and data will be lost.",
																														 new JComponent[] {ASK_CB},JOptionPane.WARNING_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
			if (ASK_CB.isSelected()) ApplicationContext.getContext().setProperty(ApplicationContext.ASK_CLOSE_TAB,new Boolean(false));
			if (response!=JOptionPane.OK_OPTION) return false;
		}
		return true;
	}

	// =======================================================================
	/**
	 * To properly close a given panel, we need to ensure that any undos
	 * associated with the panel are removed from the undo manager. All
	 * potential references must be eliminated, otherwise, we may still
	 * hand on to a given image set and have a memory leak. Bad!
	 *
	 */
	public void closeTab(int tabIndex) {

		Component c=getComponentAt(tabIndex);
		if (c instanceof JPanel) {
			JPanel container=(JPanel)c;
			Layout l=(Layout)container.getComponent(0);
			if (l==ap.getActiveLayout()) ap.setActiveLayout(null);
		}
		ApplicationContext.getContext().removeComponentEdits(c);
		remove(tabIndex);
		if (selectedTabIndex==tabIndex) selectedTabIndex=-1;
		if (getTabCount()==0) {
			ApplicationContext.getContext().setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(true));
			ap.removeFromMultiSplitPane(this);
			ApplicationContext.getContext().setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(false));
		}
		c=null;
	}

	public void closeTab(Component c) {int tabIndex=indexOfComponent(c); if (tabIndex!=-1) closeTab(tabIndex);}

	private void processCloseEvent(int tabIndex, EventType e) {

		if (e==EventType.CLOSE) {
			closeTab(tabIndex);
		} else {
			Component c=getComponentAt(tabIndex);
			if (c instanceof JPanel) {
				JPanel container=(JPanel)c;
				Layout l=(Layout)container.getComponent(0);
				ImageSequence[] isArray=l.getImageSequences();
				ModelEvent me=new ModelEvent(this,e);
				for (int loop=0; loop<isArray.length; loop++) isArray[loop].fireModelEvent(me);
			}
		}
	}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		String actionCommand=e.getActionCommand();
		if ((actionCommand==null)||(selectedTabIndex<0)) return;

		if (actionCommand.compareTo("Move to new panel")==0) {                         // Handle creating new tabbed panel

			TabbedDataPanel tdp=new TabbedDataPanel(ap);
			String tabTitle=getTitleAt(selectedTabIndex);
			String tooltip=getToolTipTextAt(selectedTabIndex);
			Component c=getComponentAt(selectedTabIndex);
			ApplicationContext.getContext().setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(true));
			remove(selectedTabIndex);
			tdp.addTab(tabTitle,null,c,tooltip);
			ap.addToMultiSplitPane(tdp);
			ApplicationContext.getContext().setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(false));

		} else if (actionCommand.compareTo("Move to left panel")==0) {                 // Move to left panel
			TabbedDataPanel tdp=(TabbedDataPanel)ap.getLeftComponent(this);
			moveTab(tdp);
		} else if (actionCommand.compareTo("Move to right panel")==0) {                // Move to right panel
			TabbedDataPanel tdp=(TabbedDataPanel)ap.getRightComponent(this);
			moveTab(tdp);
		} else if (actionCommand.compareTo("Copy to new panel")==0) {
			TabbedDataPanel tdp=new TabbedDataPanel(ap);
			copyTab(tdp);
			ApplicationContext.getContext().setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(true));
			ap.addToMultiSplitPane(tdp);
			ApplicationContext.getContext().setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(false));
		} else if (actionCommand.compareTo("Copy to left panel")==0) {                 // Copy to left panel
			TabbedDataPanel tdp=(TabbedDataPanel)ap.getLeftComponent(this);
			copyTab(tdp);
		} else if (actionCommand.compareTo("Copy to right panel")==0) {                // Copy to right panel
			TabbedDataPanel tdp=(TabbedDataPanel)ap.getRightComponent(this);
			copyTab(tdp);
		} else if (actionCommand.compareTo("Close tab")==0) {                          // Close only this tab, irrespective of other copies
			if (closeTabConfirm("tab")) processCloseEvent(selectedTabIndex,EventType.CLOSE);
		} else if (actionCommand.compareTo("Close series")==0) {                       // Close this series, and all copies
			if (closeTabConfirm("series")) processCloseEvent(selectedTabIndex,EventType.CLOSE_ALL);
		} else if (actionCommand.compareTo("Close study")==0) {                        // Close this study, and all copies
			if (closeTabConfirm("study")) processCloseEvent(selectedTabIndex,EventType.CLOSE_PARENT);
		}
	}

	// =======================================================================

	public boolean isMouseOverCloseButton() {return mouseOverCloseButton;}

	protected boolean isMouseOverCloseButton(MouseEvent e) {

		TabbedPaneUI ui=getUI();
		int tabIndex=ui.tabForCoordinate(this,e.getX(),e.getY());
		if ((tabIndex==-1)||(tabIndex!=getSelectedIndex())) return false;
		Rectangle r=ui.getTabBounds(this,tabIndex);
		Rectangle closeBounds=new Rectangle(r.x+r.width-17,r.y+4,13,13);           
		return (closeBounds.contains(e.getPoint()));
	}

	// =======================================================================

	protected void processMouseEvent(MouseEvent e) {

		processCloseButtonEvent(e);
		if ((e.getID()==MouseEvent.MOUSE_CLICKED)&&(isMouseOverCloseButton(e))) {
			TabbedPaneUI ui=getUI();
			selectedTabIndex=ui.tabForCoordinate(this,e.getX(),e.getY());	
			mouseOverCloseButton=false;
			if (closeTabConfirm("tab")) processCloseEvent(selectedTabIndex,EventType.CLOSE);
		} else if (e.getID()==MouseEvent.MOUSE_CLICKED) {
			stateChanged(null);
		} else {
			super.processMouseEvent(e); 
		}
	}

	protected void processMouseMotionEvent(MouseEvent e) {super.processMouseMotionEvent(e); processCloseButtonEvent(e);}

	private void processCloseButtonEvent(MouseEvent e) {

		boolean b=isMouseOverCloseButton(e);
		if ((!mouseOverCloseButton && b)||(mouseOverCloseButton && !b)) {
			mouseOverCloseButton=b;
			repaint();
		} 
	}

	// =======================================================================
	// Detect a selection in the tabs; set the activeLayout accordingly.

	public void stateChanged(ChangeEvent evt) {

		int i=getSelectedIndex();
		if (i<0) return;
		Component c=getComponentAt(getSelectedIndex());                   
		if (c instanceof JPanel) {
			JPanel container=(JPanel)c;
			Layout l=(Layout)container.getComponent(0);
			ap.setActiveLayout(l);
		}
	}

	// =======================================================================

	private class TabbedPaneListener extends MouseAdapter {

		public void mousePressed(MouseEvent e) {showPopup(e);}
		public void mouseReleased(MouseEvent e) {showPopup(e);}
	}
}

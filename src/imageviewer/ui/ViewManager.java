/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.ImagePanel;

import imageviewer.ui.layout.GroupedLayout;
import imageviewer.ui.layout.LayoutDescription;
import imageviewer.ui.layout.LayoutFactory;

import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.event.MenuActionEvent;
import imageviewer.ui.swing.event.MenuActionListener;

// =======================================================================

public class ViewManager implements MenuActionListener {

	PropertyChangeSupport pcsPanel=new PropertyChangeSupport(this);
	PropertyChangeSupport pcsGroup=new PropertyChangeSupport(this);

	public ViewManager() {}

	// =======================================================================
	
	public void actionPerformed(MenuActionEvent mae) {

		ActionEvent ae=mae.getActionEvent();
		String actionCommand=ae.getActionCommand();
		if (actionCommand==null) {
			MenuAction ma=mae.getMenuAction();
			if (ma!=null) actionCommand=ma.getCommandName(); else return;
		}

		if (actionCommand.compareTo(ApplicationContext.DISPLAY_GRID_COMMAND)==0) {
			boolean b=mae.getMenuAction().getMenuItem().isSelected();
			Boolean newValue=new Boolean(b);
			ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_GRID,newValue);
			pcsPanel.firePropertyChange("showGrid",null,newValue);
			return;
		} else if (actionCommand.compareTo(ApplicationContext.DISPLAY_IMAGE_INFORMATION_COMMAND)==0) {
			boolean b=mae.getMenuAction().getMenuItem().isSelected();
			Boolean newValue=new Boolean(b);
			ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_IMAGE_INFORMATION,newValue);
			pcsPanel.firePropertyChange("showHeaders",null,newValue);
			return;
		} else if (actionCommand.compareTo(ApplicationContext.DISPLAY_PARTIAL_PANELS_COMMAND)==0) {
			boolean b=mae.getMenuAction().getMenuItem().isSelected();
			Boolean newValue=new Boolean(b);
			ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_PARTIAL_PANELS,newValue);
			pcsGroup.firePropertyChange("showPartial",null,newValue);
			return;
		} else if (actionCommand.compareTo(ApplicationContext.DISPLAY_IMAGE_PROP_WINDOW_COMMAND)==0) {
			boolean b=mae.getMenuAction().getMenuItem().isSelected();
			Boolean newValue=new Boolean(b);
			ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_IMAGE_PROP_WINDOW,newValue);
			ImagePanel.toggleImagePropertyWindow();
		} else if (actionCommand.compareTo(ApplicationContext.DISPLAY_PRESENTATION_STATES_COMMAND)==0) {
			boolean b=mae.getMenuAction().getMenuItem().isSelected();
			Boolean newValue=new Boolean(b);
			ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_PRESENTATION_STATES,newValue);
			ImagePanel.togglePSWindow();
		}

		// Check and see if maybe the command corresponds to a layout? If
		// we find one, pass it to the current tab in the tabbedDataPanel.

		LayoutDescription ld=LayoutFactory.doLookup(actionCommand);
		if (ld!=null) {
			ApplicationPanel.getInstance().changeCurrentLayout(ld);
		}
	}

	// =======================================================================

	public void addPanelPropertyChangeListener(PropertyChangeListener l) {pcsPanel.addPropertyChangeListener(l);}
	public void addGroupPropertyChangeListener(PropertyChangeListener l) {pcsGroup.addPropertyChangeListener(l);}

	public void removePanelPropertyChangeListener(PropertyChangeListener l) {pcsPanel.removePropertyChangeListener(l);}
	public void removeGroupPropertyChangeListener(PropertyChangeListener l) {pcsGroup.removePropertyChangeListener(l);}

}

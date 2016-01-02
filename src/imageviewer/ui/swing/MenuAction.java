/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AWTEvent;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;

import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;

import imageviewer.ui.swing.event.MenuActionEvent;
import imageviewer.ui.swing.event.MenuActionListener;

// =======================================================================

public class MenuAction extends AbstractAction {

	public static final Hashtable<String,MenuAction> ACTIONS=new Hashtable<String,MenuAction>();

	protected static final Hashtable<String,ButtonGroup> MENU_BUTTON_GROUPS=new Hashtable<String,ButtonGroup>();
	protected static final Hashtable<String,ButtonGroup> TOOLBAR_BUTTON_GROUPS=new Hashtable<String,ButtonGroup>();
	protected static final Hashtable<AbstractButton,ButtonGroup> BUTTON_GROUP_MAP=new Hashtable<AbstractButton,ButtonGroup>();

	// =======================================================================
	
	MenuActionListener mal=null;
	AbstractButton menuItem=null, toolbarItem=null;
	String commandName=null;

	public MenuAction(String commandName) {super(commandName);}

	public MenuAction(String commandName, String toggleType, String buttonGroup, boolean isInitiallyEnabled, boolean useMenuIcon) {

		super(commandName);
		initialize(commandName,toggleType,buttonGroup,isInitiallyEnabled,useMenuIcon);
	}

	public MenuAction(String commandName, Icon icon, String toggleType, String buttonGroup, boolean isInitiallyEnabled, boolean useMenuIcon) {

		super(commandName,icon);
		initialize(commandName,toggleType,buttonGroup,isInitiallyEnabled,useMenuIcon);
	}

	// =======================================================================
	
	private void initialize(String commandName, String toggleType, String buttonGroup, boolean isInitiallyEnabled, boolean useMenuIcon) {

		this.commandName=commandName;
		setEnabled(isInitiallyEnabled);
	
		// Dependent on toggleState, either create a checkBox or
    // toggleButton for the menu and toolbar, or create regular menu
    // and button items.  Note that changes in the toggle constructs
    // need to be propogated to the other for visual consistency.

		if (toggleType==null) toggleType=new String("none");
    if (toggleType.compareTo("checkbox")==0) {
			menuItem=new JCheckBoxMenuItem(this);
			toolbarItem=new ToolbarButton(this);
    } else if (toggleType.compareTo("radiobutton")==0) {
			menuItem=new JRadioButtonMenuItem(this);
			toolbarItem=new ToolbarButton(this);
		} else {
      menuItem=new JMenuItem(this);
      toolbarItem=new JButton(this);
    }
    
    menuItem.setRolloverEnabled(true);
    menuItem.setMargin(new Insets(0,0,0,0));
		menuItem.setOpaque(false);
		menuItem.setSelected(false);
		if (!useMenuIcon) menuItem.setIcon(null);
 
    toolbarItem.setToolTipText("<html><body>"+commandName+"</body></html>");
    toolbarItem.setMargin(new Insets(1,1,1,1));
		toolbarItem.setSelected(false);
		toolbarItem.setText(null);
		toolbarItem.setRolloverEnabled(true);
		toolbarItem.setBorderPainted(false);
			
		if (buttonGroup!=null) {
			if (buttonGroup.length()!=0) {
				ButtonGroup mbg=MENU_BUTTON_GROUPS.get(buttonGroup);
				if (mbg==null) {
					mbg=new ButtonGroup();
					MENU_BUTTON_GROUPS.put(buttonGroup,mbg);
				}
				mbg.add(menuItem);
				BUTTON_GROUP_MAP.put(menuItem,mbg);
				ButtonGroup tbg=TOOLBAR_BUTTON_GROUPS.get(buttonGroup);
				if (tbg==null) {
					tbg=new ButtonGroup();
					TOOLBAR_BUTTON_GROUPS.put(buttonGroup,tbg);
				}
				tbg.add(toolbarItem);
				BUTTON_GROUP_MAP.put(toolbarItem,tbg);
			}
		}
	}

	// =======================================================================

	public static ButtonGroup getButtonGroup(AbstractButton ab) {return BUTTON_GROUP_MAP.get(ab);}

	// =======================================================================

	public void actionPerformed(ActionEvent ae) {

		if ((ae.getSource() instanceof JRadioButtonMenuItem)||(ae.getSource() instanceof JCheckBoxMenuItem)) {
			boolean newState=((AbstractButton)(ae.getSource())).isSelected();
			toolbarItem.setSelected(newState);
		} else if (ae.getSource() instanceof JToggleButton) {
			boolean newState=((AbstractButton)(ae.getSource())).isSelected();
			menuItem.setSelected(newState);
		}
		if (mal!=null) {
			MenuActionEvent mae=new MenuActionEvent(ae,this);
			mal.actionPerformed(mae);
		}
	}
	
	// =======================================================================

	public AbstractButton getMenuItem() {return menuItem;}
	public AbstractButton getToolbarItem() {return toolbarItem;}

	public String getCommandName() {return commandName;}
	public MenuActionListener getMenuActionListener() {return mal;}

	public void setMenuItem(AbstractButton x) {menuItem=x;}
	public void setToolbarItem(AbstractButton x) {toolbarItem=x;}
	public void setCommandName(String x) {commandName=x;}
	public void setMenuActionListener(MenuActionListener x) {mal=x;}

	// =======================================================================

	public class ToolbarButton extends JToggleButton {

		public ToolbarButton(Action a) {super(a);}

		protected void processMouseEvent(MouseEvent e) {

			super.processMouseEvent(e);
			switch (e.getID()) {
			  case MouseEvent.MOUSE_ENTERED: revalidate(); repaint();	break;
			  case MouseEvent.MOUSE_EXITED:	revalidate(); repaint(); break;
			}
		}
	}
}

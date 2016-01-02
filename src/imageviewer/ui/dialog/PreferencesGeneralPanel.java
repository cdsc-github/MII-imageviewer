/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import imageviewer.ui.ApplicationContext;

import utility.tools.ConfigWriter;

// =======================================================================

public class PreferencesGeneralPanel extends JPanel implements ChangeListener, ItemListener {

	// Enumeration so checkboxes can be treated as a list.

	private static enum Options {ASK_CHANGE_LAYOUT, ASK_CLOSE_TAB, DISPLAY_IMAGE_INFORMATION, DISPLAY_GRID, DISPLAY_PARTIAL_PANELS};

	private static final Object[] OPTION_LIST=new Object[] {new Object[] {Options.ASK_CHANGE_LAYOUT,ApplicationContext.ASK_CHANGE_LAYOUT,ApplicationContext.ASK_CHANGE_LAYOUT_COMMAND,
																																				"Confirm when the user selects a layout change"},
																													new Object[] {Options.ASK_CLOSE_TAB,ApplicationContext.ASK_CLOSE_TAB,ApplicationContext.ASK_CLOSE_TAB_COMMAND,
																																				"Confirm an image tab being closed"},
																													new Object[] {Options.DISPLAY_IMAGE_INFORMATION,ApplicationContext.DISPLAY_IMAGE_INFORMATION,ApplicationContext.DISPLAY_IMAGE_INFORMATION_COMMAND,
																																				"Display image slice information in each panel"},
																													new Object[] {Options.DISPLAY_GRID,ApplicationContext.DISPLAY_GRID,ApplicationContext.DISPLAY_GRID_COMMAND,
																																				"Show a grid overlay on the image based on image pixel size"},
																													new Object[] {Options.DISPLAY_PARTIAL_PANELS,ApplicationContext.DISPLAY_PARTIAL_PANELS,ApplicationContext.DISPLAY_PARTIAL_PANELS_COMMAND,
																																				"Show partially obscured images in a layout"}};

	// =======================================================================

	Hashtable changedProperties=new Hashtable();
	JSpinner undoLevelsSpinner=new JSpinner(new SpinnerNumberModel(15,1,50,1));
	JCheckBox[] optionBoxes=null;
	PreferencesDialog pd=null;

	public PreferencesGeneralPanel(PreferencesDialog pd) {
      
		super();
		initializeCheckBoxes();
		setPreferredSize(new Dimension(385,360));

		JTextArea initialPrefsDescription=DialogUtil.createTextArea("Preferences shown below are used on starting the imageViewer application. These preferences are stored "+
																										 "in the corresponding XML configuration file.  Specific information on each preference is given in the tooltip.");
		JTextArea warningDescription=DialogUtil.createTextArea("You can selectively toggle the state of application warnings that are given on various actions.");
		JTextArea undoDescription=DialogUtil.createTextArea("The number of undo levels stored by the application determines the number of actions that are stored in an undo/redo stack.  "+
																						 "Increasing the number of levels will permit more changes, but also requires more memory.");

		FormLayout fl=new FormLayout("10px,pref,10px,pref:grow,10px",
																 "10px,pref,5px,pref,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,10px,pref");
		setLayout(fl);

		CellConstraints cc=new CellConstraints();
		add(initialPrefsDescription,cc.xywh(2,2,3,1));
		add(optionBoxes[Options.DISPLAY_IMAGE_INFORMATION.ordinal()],cc.xy(2,4));
		add(optionBoxes[Options.DISPLAY_GRID.ordinal()],cc.xy(4,4));
		add(optionBoxes[Options.DISPLAY_PARTIAL_PANELS.ordinal()],cc.xy(2,5));
		add(new JSeparator(),cc.xywh(2,7,3,1));
		add(warningDescription,cc.xywh(2,9,3,1));
		add(optionBoxes[Options.ASK_CHANGE_LAYOUT.ordinal()],cc.xy(2,11));
		add(optionBoxes[Options.ASK_CLOSE_TAB.ordinal()],cc.xy(4,11));
		add(new JSeparator(),cc.xywh(2,13,3,1));
		add(undoDescription,cc.xywh(2,15,3,1));
	
		Integer ul=(Integer)ApplicationContext.getContext().getProperty(ApplicationContext.UNDO_LEVEL);
		if (ul!=null) undoLevelsSpinner.setValue(ul); else undoLevelsSpinner.setEnabled(false);
		undoLevelsSpinner.addChangeListener(this);

		JLabel undoLevelsLabel=new JLabel("Number of undo levels");
		JPanel undoPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		undoPanel.add(undoLevelsLabel);
		undoPanel.add(undoLevelsSpinner);
		add(undoPanel,cc.xywh(2,17,3,1,CellConstraints.CENTER,CellConstraints.CENTER));
		this.pd=pd;
	}

	// =======================================================================

	private void initializeCheckBoxes() {

		optionBoxes=new JCheckBox[OPTION_LIST.length];
		for (int loop=0; loop<OPTION_LIST.length; loop++) {
	    Object[] o=(Object[])OPTION_LIST[loop];
	    Boolean b=(Boolean)ApplicationContext.getContext().getProperty((String)o[1]);
	    if (b!=null) {
				optionBoxes[loop]=new JCheckBox((String)o[2],b.booleanValue());
	    } else {
				optionBoxes[loop]=new JCheckBox((String)o[2],false);
				optionBoxes[loop].setEnabled(false);
	    }
	    optionBoxes[loop].setToolTipText((String)o[3]);
	    optionBoxes[loop].addItemListener(this);
		}
	}

	// =======================================================================

	public void stateChanged(ChangeEvent e) {

		JSpinner spinner=(JSpinner)e.getSource();
		Integer value=(Integer)spinner.getValue();
		changedProperties.put(ApplicationContext.UNDO_LEVEL,value);
		pd.enableApplyButton(true);
	}

	// =======================================================================

	public void itemStateChanged(ItemEvent e) {

		JCheckBox box=(JCheckBox)e.getItem();
		for (int loop=0; loop<optionBoxes.length; loop++) {
	    if (optionBoxes[loop]==box) {
				Object[] o=(Object[])OPTION_LIST[loop];
				changedProperties.put(o[1],box.isSelected());
				pd.enableApplyButton(true);
				return;
	    }
		}
	}

	// =======================================================================

	public void applyChanges() {

		for (Enumeration e=changedProperties.keys(); e.hasMoreElements();) {
	    String s=(String)e.nextElement();
	    ApplicationContext.getContext().setProperty(s,changedProperties.get(s));
		}
		changedProperties.clear();
		pd.enableApplyButton(false);
	}

	// ========================================================================
	public void writeChanges() {

		java.io.File f=new java.io.File("config/config.xml");
		try {
	    ConfigWriter cw=new ConfigWriter("config/config.xml",ApplicationContext.getContext().getApplicationContextProperties(),"config.xml");
		} catch (Exception ex) {
	    ex.printStackTrace();
		}
	}
}


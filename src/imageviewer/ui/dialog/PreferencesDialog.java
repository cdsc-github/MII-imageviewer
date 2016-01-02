/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;

import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.ApplicationContext;
import imageviewer.ui.FloatingPanel;
import imageviewer.ui.swing.OptionPaneButtonAreaLayout;

import utility.tools.ConfigWriter;

// =======================================================================

public class PreferencesDialog extends JPanel implements ActionListener {

	JButton okButton=new JButton("OK");
	JButton cancelButton=new JButton("Cancel");
	JButton applyButton=new JButton("Apply");

	PreferencesGeneralPanel pgp=new PreferencesGeneralPanel(this);
	PreferencesMemoryPanel pmp=new PreferencesMemoryPanel(this);

	public PreferencesDialog() {

		super(new BorderLayout());
		setBorder(new EmptyBorder(5,5,5,5));
		JTabbedPane tp=new JTabbedPane(JTabbedPane.TOP,JTabbedPane.WRAP_TAB_LAYOUT);	

		tp.add("General",pgp);
		tp.add("Memory",pmp);
		tp.add("File locations",new JPanel());
		tp.add("Plugins",new JPanel());
		add(tp,BorderLayout.CENTER);

		applyButton.setEnabled(false);
		JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
		JPanel buttonPanel=new JPanel(new OptionPaneButtonAreaLayout(true,5));
		buttonPanel.setBorder(UIManager.getBorder("OptionPane.buttonAreaBorder"));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(applyButton);
		bottom.add(buttonPanel);
		add(bottom,BorderLayout.SOUTH);

		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		applyButton.setActionCommand("apply");
		applyButton.addActionListener(this);
	}

	// =======================================================================

	public void enableApplyButton(boolean b) {applyButton.setEnabled(b);}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		String s=e.getActionCommand();
		if ("cancel".equals(s)) {
	    doClose();
		} else if ("apply".equals(s)||("ok".equals(s))) {
	    pgp.applyChanges();
	    pmp.applyChanges();
	    writeChanges();
	    if ("ok".equals(s)) doClose();
		} 
	}

	// ==============================================================

	private void writeChanges() {

		// Delete any existing backup before trying to write a backup again.
		File backup=new File("config/config.xml.backup");
		File config=new File("config/config.xml");
		boolean deleteSucceed=false;
		if (backup.exists()) {
	    deleteSucceed=backup.delete();
		} else {
	    deleteSucceed=true;
		}
		boolean backupSucceed=config.renameTo(backup);
		// This should go in a popup window...
		if (!deleteSucceed || !backupSucceed) {
	    System.err.println("A problem occurred trying to back up config/config.xml before writing: deleteSucceed="+deleteSucceed+", backupSucceed="+backupSucceed);
	    return;
		}

		try {
	    ConfigWriter cw=new ConfigWriter("config/config.xml.backup",ApplicationContext.getContext().getApplicationContextProperties(),"config.xml");
	    ApplicationPanel.getInstance().addStatusMessage("File written.");
	    Timer timer=new Timer();
	    TimerTask tt=ApplicationPanel.getInstance().getNewTimerTask("Ready");
	    timer.schedule(tt,2500);
		} catch (Exception ex) {
	    ex.printStackTrace();
		}
	}

	// =======================================================================
	// Find the parent floating panel that contains this panel and tell
	// it to close.  Used by the close button explicitly and if the
	// dialog closes when an image move has been initiated.

	private void doClose() {
		
		FloatingPanel fp=(FloatingPanel)SwingUtilities.getAncestorOfClass(FloatingPanel.class,this);
		if (fp!=null) fp.actionPerformed(new ActionEvent(this,1,"close"));
	}
} 

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import com.l2fprod.common.swing.JDirectoryChooser;
import com.l2fprod.common.swing.plaf.windows.WindowsDirectoryChooserUI;

import imageviewer.system.ImageViewerClientNode;
import imageviewer.ui.ApplicationContext;
import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.FloatingPanel;

// =======================================================================

public class OpenOptionsPanel extends JPanel implements ActionListener {

	JCheckBox openStudyCheckBox=new JCheckBox("Apply study layout rules to series",false);
	JCheckBox updateArchiveOnStartCheckBox=new JCheckBox("Add new files to local archive on start",false);
	JCheckBox purgeArchiveOnStartCheckBox=new JCheckBox("Verify local archive on start",false);
	JCheckBox allowFindCheckBox=new JCheckBox("Allow queries against local archive",false);

	JButton purgeButton=null, updateDirButton=null, selectStartDirButton=null, runUpdateButton=null;

	JTextField updateDir=new JTextField(30);
	JTextField startupDirectory=new JTextField(30);

	// =======================================================================
	
	public OpenOptionsPanel() {

		super(new BorderLayout(10,0));
		setPreferredSize(new Dimension(1000,450));
		setBorder(new EmptyBorder(5,5,5,5));		

		purgeButton=new JButton("Start verification");
		purgeButton.setActionCommand("purge");
		purgeButton.addActionListener(this);
		updateDirButton=new JButton(new ImageIcon("resources/icons/swing/tree/closed.png"));
		updateDirButton.setActionCommand("updateDir");
		updateDirButton.addActionListener(this);
		selectStartDirButton=new JButton(new ImageIcon("resources/icons/swing/tree/closed.png"));
		selectStartDirButton.setActionCommand("selectStart");
		selectStartDirButton.addActionListener(this);
		runUpdateButton=new JButton("Run update");
		runUpdateButton.setActionCommand("runUpdate");
		runUpdateButton.addActionListener(this);

		add(createRightPanel(),BorderLayout.CENTER);
		add(createLeftPanel(),BorderLayout.WEST);
		add(createBottomPanel(),BorderLayout.SOUTH);
		setSize(1000,450);
	}

	// =======================================================================

	private JPanel createRightPanel() {

		JPanel containerPanel=new JPanel(new BorderLayout(10,0));
	
		JLabel localArchiveLabel=new JLabel("Local archive properties",JLabel.LEFT);
		JLabel purgeNowLabel=new JLabel("Verify archive",JLabel.LEFT);
		JLabel updateLabel=new JLabel("Update archive",JLabel.LEFT);
		JLabel archiveDirectoryLabel=new JLabel("Local archive directory",JLabel.LEFT);

		JTextArea purgeDescription=DialogUtil.createTextArea("Verifying the archive allows the system to check that the contents of the archive are valid.  File paths and images "+
																												 "are checked against the local hard drive.  Images that cannot be found are removed.  Completing the verify process "+
																												 "may take several minutes dependent on the size of your archive, and will occur in the background.");
		JTextArea updateDescription=DialogUtil.createTextArea("Specify a directory for immediate updating into the local archive.");
		JTextArea startupDirectoryDescription=DialogUtil.createTextArea("Specify the directory that will be used to update the local archive when imageViewer starts.");

		JSeparator separator1=new JSeparator();
		separator1.setPreferredSize(new Dimension(225,5));
		JSeparator separator2=new JSeparator();
		separator2.setPreferredSize(new Dimension(225,5));
		JSeparator separator3=new JSeparator();
		separator3.setPreferredSize(new Dimension(225,5));
		JSeparator separator4=new JSeparator();
		separator4.setPreferredSize(new Dimension(225,5));

		FormLayout fl=new FormLayout("pref,2dlu,22px,2dlu,pref",
																 "pref,5px,pref,5px,pref,2px,pref,2px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref");
		CellConstraints cc=new CellConstraints();

		JPanel archivePropertyPanel=new JPanel(fl);
		archivePropertyPanel.add(localArchiveLabel,cc.xywh(1,1,5,1));
		archivePropertyPanel.add(separator1,cc.xywh(1,3,5,1));
		archivePropertyPanel.add(updateArchiveOnStartCheckBox,cc.xywh(1,5,5,1));
		archivePropertyPanel.add(purgeArchiveOnStartCheckBox,cc.xywh(1,7,5,1));
		archivePropertyPanel.add(allowFindCheckBox,cc.xywh(1,9,5,1));
		archivePropertyPanel.add(purgeNowLabel,cc.xywh(1,11,5,1));
		archivePropertyPanel.add(separator2,cc.xywh(1,13,5,1));
		archivePropertyPanel.add(purgeDescription,cc.xywh(1,15,5,1));
		archivePropertyPanel.add(purgeButton,cc.xywh(1,17,5,1,CellConstraints.RIGHT,CellConstraints.BOTTOM));
		archivePropertyPanel.add(archiveDirectoryLabel,cc.xywh(1,19,5,1));
		archivePropertyPanel.add(separator4,cc.xywh(1,21,5,1));
		archivePropertyPanel.add(startupDirectoryDescription,cc.xywh(1,23,5,1));
		archivePropertyPanel.add(startupDirectory,cc.xy(1,25));
		archivePropertyPanel.add(selectStartDirButton,cc.xy(3,25));
		archivePropertyPanel.add(updateLabel,cc.xywh(1,27,5,1));
		archivePropertyPanel.add(separator3,cc.xywh(1,29,5,1));
		archivePropertyPanel.add(updateDescription,cc.xywh(1,31,5,1));
		archivePropertyPanel.add(updateDir,cc.xy(1,33));
		archivePropertyPanel.add(updateDirButton,cc.xy(3,33));
		archivePropertyPanel.add(runUpdateButton,cc.xy(5,33));

		updateDir.setActionCommand("runUpdate");
		updateDir.addActionListener(this);

		containerPanel.add(archivePropertyPanel,BorderLayout.WEST);
		return containerPanel;
	}

	// =======================================================================

	private JPanel createLeftPanel() {

		JLabel openBehaviorLabel=new JLabel("Image opening properties",JLabel.LEFT);
		JLabel openLayoutRulesLabel=new JLabel("Layout rules",JLabel.LEFT);
		JSeparator separator1=new JSeparator();
		separator1.setPreferredSize(new Dimension(250,5));
		JSeparator separator2=new JSeparator();
		separator2.setPreferredSize(new Dimension(250,5));
		JTextArea openDescription=DialogUtil.createTextArea("When selecting series or a complete study for opening, layout rules can be applied to merge "+
																												"studies together and/or trigger appropriate visualizations. For these rules to be applied, all "+
																												"series within a study must be available before rule processing can be applied.");
		JList layoutRuleList=new JList();
		JScrollPane ruleListScrollPane=new JScrollPane(layoutRuleList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		ruleListScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		ruleListScrollPane.setPreferredSize(new Dimension(250,225));
		
		FormLayout fl=new FormLayout("pref","pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref");
		CellConstraints cc=new CellConstraints();
		JPanel checkPanel=new JPanel(fl);
		checkPanel.add(openBehaviorLabel,cc.xy(1,1));
		checkPanel.add(separator1,cc.xy(1,3));
		checkPanel.add(openDescription,cc.xy(1,5));
		checkPanel.add(openStudyCheckBox,cc.xy(1,7));
		checkPanel.add(openLayoutRulesLabel,cc.xy(1,9));
		checkPanel.add(separator2,cc.xy(1,11));
		checkPanel.add(ruleListScrollPane,cc.xy(1,13));
	
		return checkPanel;		
	}

	// =======================================================================

	private JPanel createBottomPanel() {

		JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
		buttonPanel.setBorder(null);
		JButton closeButton=new JButton("Close");
		buttonPanel.add(closeButton);
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);
		return buttonPanel;
	}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		if ("updateDir".equals(e.getActionCommand())) {
			String startDir=(String)ApplicationContext.getContext().getProperty(ApplicationContext.CURRENT_DIRECTORY);
			JDirectoryChooser dc=(startDir!=null) ? new JDirectoryChooser(startDir) : new JDirectoryChooser();
			dc.setUI(new WindowsDirectoryChooserUI(dc) {protected String getToolTipText(MouseEvent event) {return null;}});   // Override tooltips; they show up behind the panel for some reason...
			dc.setShowingCreateDirectory(false);
			dc.setFileSelectionMode(JDirectoryChooser.DIRECTORIES_ONLY);
			dc.setApproveButtonText("Select directory");
			int dcChoice=ApplicationPanel.getInstance().showDialog(dc,"Select directory for archive upload");
			if (dcChoice==JDirectoryChooser.APPROVE_OPTION) {
				File file=dc.getSelectedFile();
				updateDir.setText(file.toString());
				file=file.getParentFile();
				ApplicationContext.getContext().setProperty(ApplicationContext.CURRENT_DIRECTORY,file.toString());
			}
		} else if ("runUpdate".equals(e.getActionCommand())) {
			final String s=updateDir.getText();
			updateDir.setEnabled(false);
			updateDirButton.setEnabled(false);
			runUpdateButton.setEnabled(false);
			if ((s!=null)&&(s!="")) {
				Thread t=new Thread(new Runnable() {
					public void run() {
						try {
							ApplicationPanel.getInstance().addStatusMessage("Update of archive started");
							ImageViewerClientNode.getInstance().beginTransaction();
							ImageViewerClientNode.getInstance().getArchive().updateArbitrary(s);
							ApplicationPanel.getInstance().addStatusMessage("Update of archive completed",5000);
						} catch (Exception exc) {
							ApplicationPanel.getInstance().addStatusMessage("Update of archive failed",5000);
							ImageViewerClientNode.getInstance().rollbackTransaction();
							exc.printStackTrace();
						} finally {
							try {
								ImageViewerClientNode.getInstance().commitTransaction();
							} catch (Exception e) {}
							updateDir.setEnabled(true);
							updateDirButton.setEnabled(true);
							runUpdateButton.setEnabled(true);
						} 
					}
				});
				t.setPriority(4);
				t.start();
			}
		} else if ("close".equals(e.getActionCommand())) {
			FloatingPanel fp=(FloatingPanel)SwingUtilities.getAncestorOfClass(FloatingPanel.class,this);
			if (fp!=null) fp.actionPerformed(new ActionEvent(this,1,"close"));			
		}
	}
}

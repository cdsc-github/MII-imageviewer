/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import javax.swing.border.EmptyBorder;

import javax.swing.event.TableModelListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.filechooser.FileSystemView;

import javax.swing.plaf.TabbedPaneUI;

import javax.swing.table.TableColumn;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HierarchicalColumnHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import com.l2fprod.common.swing.JOutlookBar;

import imageserver.model.ImageServerFindDescription;
import imageserver.model.ImageServerFindDescription.Compression;
import imageserver.model.ImageServerFindDescription.Scope;
import imageserver.model.ImageServerNodeDescription;
import imageserver.model.ImageServerNodeDescription.QueryParameters;

import imageviewer.system.ImageReaderManager;
import imageviewer.system.ImageViewerClientNode;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.FloatingPanel;
import imageviewer.ui.swing.ImageViewerOutlookBarUI;
import imageviewer.ui.swing.TableHeaderRenderer;

// =======================================================================

public class OpenDialogPanel extends JPanel implements ActionListener, ListSelectionListener {

	public static final int[] ARCHIVE_COLUMN_WIDTH=new int[] {255,250,50,75,50,60,75,50,300};

	private static final int[] DIRECTORY_COLUMN_WIDTH=new int[] {340,75,175,180};

	private static final String SEARCH_RESULT_PANEL=new String("SEARCH RESULT PANEL");
	private static final String DIRECTORY_PANEL=new String("DIRECTORY PANEL");

	private static final Color ROW_COLOR1=new Color(20,30,45);
	private static final Color ROW_COLOR2=new Color(0,10,25);
	private static final Color COL_COLOR1=new Color(40,50,65);

	// =======================================================================
	// GUI components galore...

	ListSelectionModel localSelectionModel=null, networkSelectionModel=null;
	JXTreeTable treeTable=null, dirTreeTable=null;
	ImageViewerClientNode ivcn=null;
	CardLayout rightPanelLayout=null;
	JPanel rPanel=null, lPanel=null;
	JXList localList=null, networkList=null;

	JButton openArchiveButton=new JButton("Open selected images");
	JButton openDirButton=new JButton("Open selected images"); 
	JButton closeArchiveButton=new JButton("Close"); 
	JButton closeDirButton=new JButton("Close");
	JButton searchButton=new JButton("Run search");
	JButton refreshButton=new JButton("Refresh resource lists");
	JButton clearFieldButton=new JButton("Clear fields");

	JTextField imageWidthTextField=new JTextField(5);
	JTextField imageHeightTextField=new JTextField(5);
	JTextField bitsAllocatedTextField=new JTextField(3);
	JTextField bitsStoredTextField=new JTextField(3);
	JTextField fileOffsetTextField=new JTextField(7);
	JTextField numImagesTextField=new JTextField(7);
	JTextField patientNameField=new JTextField(19);
	JTextField patientIDField=new JTextField(19);
	JTextField modalityField=new JTextField(5);
	JTextField anatomyField=new JTextField(10);
	JTextField descriptionField=new JTextField(20);

	JXDatePicker startDatePicker=new JXDatePicker();
	JXDatePicker endDatePicker=new JXDatePicker();

	JLabel imageWidthLabel=new JLabel("Image width",JLabel.TRAILING);             
	JLabel imageHeightLabel=new JLabel("Image height",JLabel.TRAILING);
	JLabel bitsAllocatedLabel=new JLabel("Bits allocated",JLabel.TRAILING);
	JLabel bitsStoredLabel=new JLabel("Bits stored",JLabel.TRAILING);
	JLabel fileOffsetLabel=new JLabel("Byte offset",JLabel.TRAILING);
	JLabel numImagesLabel=new JLabel("Number of images",JLabel.TRAILING);
	JLabel resultsLabel=new JLabel("Results",JLabel.LEFT);
	JLabel patientNameLabel=new JLabel("Patient name",JLabel.TRAILING);
	JLabel patientIDLabel=new JLabel("ID",JLabel.TRAILING);
	JLabel modalityLabel=new JLabel("Modality",JLabel.TRAILING);
	JLabel anatomyLabel=new JLabel("Anatomy",JLabel.TRAILING);
	JLabel descriptionLabel=new JLabel("Description",JLabel.TRAILING);
	JLabel startDateLabel=new JLabel("Start date",JLabel.TRAILING);
	JLabel endDateLabel=new JLabel("End date",JLabel.TRAILING);
	
	JComboBox openAsComboBox=new JComboBox(ImageReaderManager.getInstance().getImageReaderTypes());
	JCheckBox recurseCheckBox=new JCheckBox("Recursively open subdirectories",false);
	JCheckBox closeOnOpenCheckBox=new JCheckBox("Close window after open selection",false);
	JCheckBox transferOnlyCheckBox=new JCheckBox("Transfer images only",false);

	StudyListTreeTableModel studyList=new StudyListTreeTableModel();

	// =======================================================================

	public OpenDialogPanel() {

		super(new BorderLayout());
		setPreferredSize(new Dimension(1000,450));
		setBorder(new EmptyBorder(5,5,5,5));
		ivcn=ImageViewerClientNode.getInstance();
	
		openArchiveButton.setActionCommand("open");
		openArchiveButton.addActionListener(this);
		closeArchiveButton.setActionCommand("close");
		closeArchiveButton.addActionListener(this);
		openDirButton.setActionCommand("open");
		openDirButton.addActionListener(this);
		closeDirButton.setActionCommand("close");
		closeDirButton.addActionListener(this);
		refreshButton.setActionCommand("refreshNetwork");
		refreshButton.addActionListener(this);

		openAsComboBox.setSelectedItem("DICOM");

		createArchiveTreeTable();
		rPanel=createRightPanel();
		lPanel=createLeftPanel();
		add(rPanel,BorderLayout.CENTER);
		add(lPanel,BorderLayout.WEST);
		setOpaque(false);
		setSize(1000,450);
	}

	// =======================================================================

	private void createArchiveTreeTable() {

		List l=ivcn.localFindAll();
		treeTable=new JXTreeTable(studyList);
		treeTable.setHorizontalScrollEnabled(true);
		treeTable.setRootVisible(false);
		treeTable.setShowsRootHandles(true);
		treeTable.setShowHorizontalLines(true);
		treeTable.setShowVerticalLines(true);
		treeTable.setColumnMargin(1);
		treeTable.setColumnControlVisible(false);
		treeTable.setRolloverEnabled(true);
		treeTable.setScrollsOnExpand(true);
		treeTable.setTreeCellRenderer(new TreeTableCellRenderer());
		treeTable.setAutoCreateColumnsFromModel(false);
		treeTable.setColumnFactory(new ColumnFactory() {public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {}});
		// treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		ListSelectionModel lsm=treeTable.getSelectionModel();
		lsm.addListSelectionListener(this);

		CompoundHighlighter cp=new CompoundHighlighter();
		cp.addHighlighter(new AlternateRowHighlighter(ROW_COLOR1,ROW_COLOR2,Color.white));
		cp.addHighlighter(new HierarchicalColumnHighlighter(COL_COLOR1,Color.white));

		treeTable.setHighlighters(cp);
		for (int i=0; i<ARCHIVE_COLUMN_WIDTH.length; i++) {
			TableColumn column=treeTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(ARCHIVE_COLUMN_WIDTH[i]);
			column.setHeaderRenderer(new TableHeaderRenderer(Color.black));
    }
	}

	// =======================================================================
	// Right side of open dialog panel with a cardLayout.

	private JPanel createRightPanel() {

		JPanel archivePanel=createRightArchiveSearchPanel();
		JPanel directoryPanel=createDirectoryPanel();

		rightPanelLayout=new CardLayout();
		JPanel rightPanel=new JPanel(rightPanelLayout);
		rightPanel.add(archivePanel,SEARCH_RESULT_PANEL);
		rightPanel.add(directoryPanel,DIRECTORY_PANEL);
		return rightPanel;
	}

	// =======================================================================
	// Right side of open dialog panel with treetable, filters, and
	// buttons, oh my.

	private JPanel createRightArchiveSearchPanel() {

		JScrollPane treeTableScrollPane=new JScrollPane(treeTable,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		treeTableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		treeTableScrollPane.setPreferredSize(new Dimension(807,450));
		JLabel searchLabel=new JLabel("Search",JLabel.LEFT);
		JPanel searchPanel=createArchiveSearchFieldSubPanel();
		JSeparator separator1=new JSeparator();
		JSeparator separator2=new JSeparator();
		separator1.setPreferredSize(new Dimension(200,5));
		separator2.setPreferredSize(new Dimension(785,5));

		searchButton.setEnabled(false);
		clearFieldButton.setEnabled(true);
		openArchiveButton.setEnabled(false);
		closeArchiveButton.setSelected(true);

		FormLayout fl=new FormLayout("left:pref,left:pref,270px:grow,right:pref,5px,right:pref","pref");
		CellConstraints cc=new CellConstraints();
		JPanel buttonPanel=new JPanel(fl);
		buttonPanel.setOpaque(false);
		buttonPanel.add(closeOnOpenCheckBox,cc.xy(1,1));
		buttonPanel.add(transferOnlyCheckBox,cc.xy(2,1));
		buttonPanel.add(openArchiveButton,cc.xy(4,1));
		buttonPanel.add(closeArchiveButton,cc.xy(6,1));
		
		SpringLayout rightLayout=new SpringLayout();
		JPanel tablePanel=new JPanel(rightLayout);		
		tablePanel.add(searchLabel);
		tablePanel.add(separator1);
		tablePanel.add(searchPanel);
		tablePanel.add(resultsLabel);
		tablePanel.add(separator2);
		tablePanel.add(treeTableScrollPane);
		tablePanel.add(buttonPanel);

		rightLayout.putConstraint(SpringLayout.NORTH,searchLabel,0,SpringLayout.NORTH,tablePanel);
		rightLayout.putConstraint(SpringLayout.WEST,searchLabel,0,SpringLayout.WEST,tablePanel);
		rightLayout.putConstraint(SpringLayout.NORTH,separator1,5,SpringLayout.SOUTH,searchLabel);
		rightLayout.putConstraint(SpringLayout.WEST,separator1,0,SpringLayout.WEST,tablePanel);
		rightLayout.putConstraint(SpringLayout.NORTH,searchPanel,0,SpringLayout.SOUTH,separator1);
		rightLayout.putConstraint(SpringLayout.WEST,searchPanel,0,SpringLayout.WEST,tablePanel);
		rightLayout.putConstraint(SpringLayout.NORTH,resultsLabel,4,SpringLayout.SOUTH,searchPanel);
		rightLayout.putConstraint(SpringLayout.WEST,resultsLabel,0,SpringLayout.WEST,tablePanel);
		rightLayout.putConstraint(SpringLayout.NORTH,separator2,5,SpringLayout.SOUTH,resultsLabel);
		rightLayout.putConstraint(SpringLayout.WEST,separator2,0,SpringLayout.WEST,tablePanel);
		rightLayout.putConstraint(SpringLayout.NORTH,treeTableScrollPane,5,SpringLayout.SOUTH,separator2);
		rightLayout.putConstraint(SpringLayout.WEST,treeTableScrollPane,0,SpringLayout.WEST,tablePanel);
		rightLayout.putConstraint(SpringLayout.NORTH,buttonPanel,5,SpringLayout.SOUTH,treeTableScrollPane);
		rightLayout.putConstraint(SpringLayout.EAST,buttonPanel,0,SpringLayout.EAST,tablePanel);
		rightLayout.putConstraint(SpringLayout.EAST,tablePanel,0,SpringLayout.EAST,separator1);
		rightLayout.putConstraint(SpringLayout.SOUTH,tablePanel,0,SpringLayout.SOUTH,buttonPanel);
		return tablePanel;
	}

	// =======================================================================

	private JPanel createDirectoryPanel() {

		dirTreeTable=new JXTreeTable(new FileSystemModel());
		dirTreeTable.setHorizontalScrollEnabled(true);
		dirTreeTable.setRootVisible(false);
		dirTreeTable.setShowsRootHandles(true);
		dirTreeTable.setShowHorizontalLines(true);
		dirTreeTable.setShowVerticalLines(true);
		dirTreeTable.setColumnMargin(1);
		dirTreeTable.setColumnControlVisible(false);
		dirTreeTable.setRolloverEnabled(true);
		dirTreeTable.setScrollsOnExpand(true);
		dirTreeTable.setTreeCellRenderer(new TreeTableCellRenderer());
		dirTreeTable.setAutoCreateColumnsFromModel(false);
		// dirTreeTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		ListSelectionModel lsm=dirTreeTable.getSelectionModel();
		lsm.addListSelectionListener(this);

		CompoundHighlighter cp=new CompoundHighlighter();
		cp.addHighlighter(new AlternateRowHighlighter(new Color(20,30,45),new Color(0,10,25),Color.white));
		cp.addHighlighter(new HierarchicalColumnHighlighter(new Color(40,50,65),Color.white));

		dirTreeTable.setHighlighters(cp);
		for (int i=0; i<DIRECTORY_COLUMN_WIDTH.length; i++) {
			TableColumn column=dirTreeTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(DIRECTORY_COLUMN_WIDTH[i]);			
			column.setHeaderRenderer(new TableHeaderRenderer(Color.black));
    }

		JScrollPane treeTableScrollPane=new JScrollPane(dirTreeTable,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		treeTableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		treeTableScrollPane.setPreferredSize(new Dimension(807,450));
		JLabel directoryLabel=new JLabel("File explorer");
		JSeparator separator1=new JSeparator();
		separator1.setPreferredSize(new Dimension(785,5));

		FormLayout fl=new FormLayout("pref,2dlu,pref,10px,pref,2dlu,pref,5px,pref,2dlu,pref,5px,pref,2dlu,pref,pref:grow","pref,5px,pref,2dlu,pref,2px,pref");
		CellConstraints cc=new CellConstraints();

		JPanel dirOptionPanel=new JPanel(fl);
		JLabel dirOptionsLabel=new JLabel("File explorer options");
		JSeparator separator2=new JSeparator();
		separator2.setPreferredSize(new Dimension(300,5));
		JLabel openAsLabel=new JLabel("Open as",JLabel.TRAILING);

		JLabel rawFileLabel=new JLabel("Raw file parameters");
		JSeparator separator3=new JSeparator();
		separator3.setPreferredSize(new Dimension(380,5));
		
		imageWidthLabel.setEnabled(false);
		imageHeightLabel.setEnabled(false);
		bitsAllocatedLabel.setEnabled(false);
		bitsStoredLabel.setEnabled(false);
		fileOffsetLabel.setEnabled(false);
		numImagesLabel.setEnabled(false);

		imageWidthTextField.setEnabled(false);
		imageHeightTextField.setEnabled(false);
		bitsAllocatedTextField.setEnabled(false);
		bitsStoredTextField.setEnabled(false);
		fileOffsetTextField.setEnabled(false);
		numImagesTextField.setEnabled(false);
		openAsComboBox.addActionListener(this);
		
		dirOptionPanel.add(dirOptionsLabel,cc.xywh(1,1,3,1));
		dirOptionPanel.add(separator2,cc.xywh(1,3,3,1));
		dirOptionPanel.add(openAsLabel,cc.xy(1,5));
		dirOptionPanel.add(openAsComboBox,cc.xy(3,5));
		dirOptionPanel.add(recurseCheckBox,cc.xy(3,7));
			
		dirOptionPanel.add(rawFileLabel,cc.xywh(5,1,11,1));
		dirOptionPanel.add(separator3,cc.xywh(5,3,12,1));
		dirOptionPanel.add(imageWidthLabel,cc.xy(5,5));
		dirOptionPanel.add(imageWidthTextField,cc.xy(7,5));
		dirOptionPanel.add(imageHeightLabel,cc.xy(5,7));
		dirOptionPanel.add(imageHeightTextField,cc.xy(7,7));
		dirOptionPanel.add(bitsAllocatedLabel,cc.xy(9,5));
		dirOptionPanel.add(bitsAllocatedTextField,cc.xy(11,5));
		dirOptionPanel.add(bitsStoredLabel,cc.xy(9,7));
		dirOptionPanel.add(bitsStoredTextField,cc.xy(11,7));
		dirOptionPanel.add(fileOffsetLabel,cc.xy(13,5));
		dirOptionPanel.add(fileOffsetTextField,cc.xy(15,5));		
		dirOptionPanel.add(numImagesLabel,cc.xy(13,7));
		dirOptionPanel.add(numImagesTextField,cc.xy(15,7));		
	
		openDirButton.setEnabled(false);
		closeDirButton.setSelected(true);
		SpringLayout buttonLayout=new SpringLayout();
		JPanel buttonPanel=new JPanel(buttonLayout);
		buttonPanel.setOpaque(false);
		buttonPanel.add(openDirButton);
		buttonPanel.add(closeDirButton);
		buttonLayout.putConstraint(SpringLayout.NORTH,openDirButton,0,SpringLayout.NORTH,buttonPanel);
		buttonLayout.putConstraint(SpringLayout.WEST,openDirButton,0,SpringLayout.WEST,buttonPanel);
		buttonLayout.putConstraint(SpringLayout.NORTH,closeDirButton,0,SpringLayout.NORTH,buttonPanel);
		buttonLayout.putConstraint(SpringLayout.WEST,closeDirButton,5,SpringLayout.EAST,openDirButton);
		buttonLayout.putConstraint(SpringLayout.EAST,buttonPanel,0,SpringLayout.EAST,closeDirButton);
		buttonLayout.putConstraint(SpringLayout.SOUTH,buttonPanel,0,SpringLayout.SOUTH,closeDirButton);

		SpringLayout layout=new SpringLayout();
		JPanel dirPanel=new JPanel(layout);
		dirPanel.add(dirOptionPanel);
		dirPanel.add(directoryLabel);
		dirPanel.add(separator1);
		dirPanel.add(treeTableScrollPane);
		dirPanel.add(buttonPanel);

		layout.putConstraint(SpringLayout.NORTH,dirOptionPanel,0,SpringLayout.NORTH,dirPanel);
		layout.putConstraint(SpringLayout.WEST,dirOptionPanel,0,SpringLayout.WEST,dirPanel);
		layout.putConstraint(SpringLayout.NORTH,directoryLabel,0,SpringLayout.SOUTH,dirOptionPanel);
		layout.putConstraint(SpringLayout.WEST,directoryLabel,0,SpringLayout.WEST,dirPanel);
		layout.putConstraint(SpringLayout.NORTH,separator1,5,SpringLayout.SOUTH,directoryLabel);
		layout.putConstraint(SpringLayout.WEST,separator1,0,SpringLayout.WEST,dirPanel);
		layout.putConstraint(SpringLayout.NORTH,treeTableScrollPane,5,SpringLayout.SOUTH,separator1);
		layout.putConstraint(SpringLayout.WEST,treeTableScrollPane,0,SpringLayout.WEST,dirPanel);
		layout.putConstraint(SpringLayout.NORTH,buttonPanel,5,SpringLayout.SOUTH,treeTableScrollPane);
		layout.putConstraint(SpringLayout.EAST,buttonPanel,0,SpringLayout.EAST,dirPanel);
		layout.putConstraint(SpringLayout.EAST,dirPanel,5,SpringLayout.EAST,separator1);
		layout.putConstraint(SpringLayout.SOUTH,dirPanel,0,SpringLayout.SOUTH,buttonPanel);
		return dirPanel;
	}

	// =======================================================================

	private JPanel createLeftPanel() {

		ImageViewerClientNode ivcn=ImageViewerClientNode.getInstance();
		String[] localListArray=(ivcn.hasLocalArchive()) ? (new String[] {"Local archive","Local directory"}) : (new String[] {"Local directory"});
		localList=new JXList(localListArray);
		localList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		localSelectionModel=localList.getSelectionModel();
		localSelectionModel.addListSelectionListener(this);
		JScrollPane localListScrollPane=new JScrollPane(localList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		localListScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		localListScrollPane.setPreferredSize(new Dimension(194,300));
		localList.setSelectionForeground(Color.white);
		localList.setSelectionBackground(Color.darkGray);
		localList.setBackground(ROW_COLOR1); //Color.darkGray);
		localList.setForeground(Color.white);

		List<ImageServerNodeDescription> nodes=ImageViewerClientNode.getInstance().getQueryableNodes();
		networkList=(nodes!=null) ? (new JXList(nodes.toArray())) : (new JXList());
		networkList.setCellRenderer(new NodeDescriptionRenderer());
		networkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		networkSelectionModel=networkList.getSelectionModel();
		networkSelectionModel.addListSelectionListener(this);
		JScrollPane networkListScrollPane=new JScrollPane(networkList,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		networkListScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		networkListScrollPane.setPreferredSize(new Dimension(194,300));
		networkList.setSelectionForeground(Color.white);
		networkList.setSelectionBackground(Color.darkGray); 
		networkList.setBackground(ROW_COLOR1); //Color.darkGray);
		networkList.setForeground(Color.white);

		JPanel listPanel=new JPanel(new BorderLayout(0,5)); 
		JOutlookBar job=new JOutlookBar();
		job.addTab("Local resources",new ImageIcon("resources/icons/swing/computer.png"),localListScrollPane);
		job.addTab("Network resources",new ImageIcon("resources/icons/swing/network.png"),networkListScrollPane);
		job.setUI((TabbedPaneUI)(new ImageViewerOutlookBarUI()));

		listPanel.add(job,BorderLayout.CENTER);
		listPanel.add(refreshButton,BorderLayout.SOUTH);
		listPanel.setBorder(new EmptyBorder(0,0,0,5));
		return listPanel;
	}

	// =======================================================================

	private JPanel createArchiveSearchFieldSubPanel() {

		FormLayout fl=new FormLayout("pref,2dlu,pref:grow,5dlu,pref,2dlu,pref,5dlu,pref,2dlu,pref,5dlu,pref,2dlu,120px,5dlu,pref","pref,2dlu,pref");
		JPanel searchPanel=new JPanel(fl);

		startDatePicker.getMonthView().setAntialiased(true);
		startDatePicker.getMonthView().setBoxPaddingX(1);
		startDatePicker.getMonthView().setBoxPaddingY(1);
		startDatePicker.getMonthView().setSelectionMode(JXMonthView.SelectionMode.SINGLE_SELECTION);
		startDatePicker.getEditor().setValue(null);
		startDatePicker.setLinkPanel(null);
		endDatePicker.getMonthView().setAntialiased(true);
		endDatePicker.getMonthView().setBoxPaddingX(1);
		endDatePicker.getMonthView().setBoxPaddingY(1);
		endDatePicker.getMonthView().setSelectionMode(JXMonthView.SelectionMode.SINGLE_SELECTION);
		endDatePicker.getEditor().setValue(null);
		endDatePicker.setLinkPanel(null);

		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);
		clearFieldButton.setActionCommand("clear");
		clearFieldButton.addActionListener(this);

		CellConstraints cc=new CellConstraints();
		searchPanel.add(patientNameLabel,cc.xy(1,1));
		searchPanel.add(patientNameField,cc.xy(3,1));
		searchPanel.add(descriptionLabel,cc.xy(5,1));
		searchPanel.add(descriptionField,cc.xywh(7,1,5,1));
		searchPanel.add(startDateLabel,cc.xy(13,1)); 
		searchPanel.add(startDatePicker,cc.xy(15,1)); 
		searchPanel.add(clearFieldButton,cc.xy(17,1));
		searchPanel.add(patientIDLabel,cc.xy(1,3)); 
		searchPanel.add(patientIDField,cc.xy(3,3)); 
		searchPanel.add(modalityLabel,cc.xy(5,3));
		searchPanel.add(modalityField,cc.xy(7,3));
		searchPanel.add(anatomyLabel,cc.xy(9,3)); 
		searchPanel.add(anatomyField,cc.xy(11,3)); 
		searchPanel.add(endDateLabel,cc.xy(13,3)); 
		searchPanel.add(endDatePicker,cc.xy(15,3));
		searchPanel.add(searchButton,cc.xy(17,3));

		patientIDField.setActionCommand("search");
		patientIDField.addActionListener(this);
		patientNameField.setActionCommand("search");
		patientNameField.addActionListener(this);
		descriptionField.setActionCommand("search");
		descriptionField.addActionListener(this);
		modalityField.setActionCommand("search");
		modalityField.addActionListener(this);
		anatomyField.setActionCommand("search");
		anatomyField.addActionListener(this);
		
		return searchPanel;
	}

	// =======================================================================

	private void updateSearchLabels(ImageServerNodeDescription isnd) {

		if (isnd==null) return;
		List<String> requiredParams=isnd.getRequiredQueryParams();
		patientNameLabel.setForeground(requiredParams.contains(QueryParameters.PATIENT_NAME.toString()) ? Color.red : Color.white);
		patientIDLabel.setForeground(requiredParams.contains(QueryParameters.PATIENT_MRN.toString()) ? Color.red : Color.white);
		modalityLabel.setForeground(requiredParams.contains(QueryParameters.MODALITY.toString()) ? Color.red : Color.white);
		anatomyLabel.setForeground(requiredParams.contains(QueryParameters.BODY_PART.toString()) ? Color.red : Color.white);
		startDateLabel.setForeground(requiredParams.contains(QueryParameters.STUDY_BEGIN_DATE.toString()) ? Color.red : Color.white);
		endDateLabel.setForeground(requiredParams.contains(QueryParameters.STUDY_END_DATE.toString()) ? Color.red : Color.white);
	}

	private void resetSearchLabels() {

		patientNameLabel.setForeground(Color.white);
		patientIDLabel.setForeground(Color.white);
		modalityLabel.setForeground(Color.white);
		anatomyLabel.setForeground(Color.white);
		descriptionLabel.setForeground(Color.white);
		startDateLabel.setForeground(Color.white);
		endDateLabel.setForeground(Color.white);
	}

	// =======================================================================

	public void valueChanged(ListSelectionEvent e) {

		boolean isAdjusting=e.getValueIsAdjusting();
		if (isAdjusting) return;

		searchButton.setEnabled(((networkSelectionModel.isSelectionEmpty())&&(localSelectionModel.isSelectionEmpty())) ? false : true);

		ListSelectionModel lsm=(ListSelectionModel)e.getSource();
		if (lsm==localSelectionModel) {
			networkSelectionModel.removeListSelectionListener(this);
			networkList.clearSelection();
			networkSelectionModel.addListSelectionListener(this);
			String localSelection=(String)localList.getSelectedValue();
			if (localSelection!=null) {
				if ("local archive".equalsIgnoreCase(localSelection)) {
					rightPanelLayout.show(rPanel,SEARCH_RESULT_PANEL);
				} else if ("local directory".equalsIgnoreCase(localSelection)) {
					rightPanelLayout.show(rPanel,DIRECTORY_PANEL);
				}
				networkSelectionModel.clearSelection();
				resetSearchLabels();
			}
		} else if (lsm==networkSelectionModel) {
			rightPanelLayout.show(rPanel,SEARCH_RESULT_PANEL);
			localSelectionModel.removeListSelectionListener(this);
			localList.clearSelection();
			localSelectionModel.addListSelectionListener(this);
			updateSearchLabels((ImageServerNodeDescription)networkList.getSelectedValue());
	
		} else {
			if (lsm.isSelectionEmpty()) {
				if (e.getSource()==treeTable.getSelectionModel()) openArchiveButton.setEnabled(false); else openDirButton.setEnabled(false);
			} else {
				if (e.getSource()==treeTable.getSelectionModel()) openArchiveButton.setEnabled(true); else openDirButton.setEnabled(true);
			}
		}
	}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		// Spawn thread to handle the results and loading...

		if (e.getSource()==openAsComboBox) {
			String selection=(String)openAsComboBox.getSelectedItem();
			if ("Raw images".equals(selection)) {
				imageWidthTextField.setEnabled(true);
				imageHeightTextField.setEnabled(true);
				bitsAllocatedTextField.setEnabled(true);
				bitsStoredTextField.setEnabled(true);
				fileOffsetTextField.setEnabled(true);
				numImagesTextField.setEnabled(true);
				imageWidthLabel.setEnabled(true);
				imageHeightLabel.setEnabled(true);
				bitsAllocatedLabel.setEnabled(true);
				bitsStoredLabel.setEnabled(true);
				fileOffsetLabel.setEnabled(true);
				numImagesLabel.setEnabled(true);
			} else {
				imageWidthTextField.setEnabled(false);
				imageHeightTextField.setEnabled(false);
				bitsAllocatedTextField.setEnabled(false);
				bitsStoredTextField.setEnabled(false);
				fileOffsetTextField.setEnabled(false);
				numImagesTextField.setEnabled(false);
				imageWidthLabel.setEnabled(false);
				imageHeightLabel.setEnabled(false);
				bitsAllocatedLabel.setEnabled(false);
				bitsStoredLabel.setEnabled(false);
				fileOffsetLabel.setEnabled(false);
				numImagesLabel.setEnabled(false);
			}
		} else if ("open".equals(e.getActionCommand())) {
			if (e.getSource()==openArchiveButton) {
				TreeSelectionModel tsm=treeTable.getTreeSelectionModel();
				TreePath[] selectPaths=tsm.getSelectionPaths();
				StudyListTreeTableModel slttm=(StudyListTreeTableModel)treeTable.getTreeTableModel();
				
				// If it's a local archive, we don't need to do a move;
				// otherwise we execute a move thread and once that's
				// complete, invoke the openDialogArchive thread.

				if (!localSelectionModel.isSelectionEmpty()) {
					OpenDialogArchiveThread odat=new OpenDialogArchiveThread(slttm.computeSeries(selectPaths),false); //openStudyCheckBox.isSelected());
					odat.start();
				} else {
					ImageServerNodeDescription isnd=(ImageServerNodeDescription)networkList.getSelectedValue();
					OpenDialogArchiveMoveThread odamt=((isnd.getSupportedMoveScope()==Scope.SERIES)||(isnd.getSupportedMoveScope()==Scope.INSTANCE)) ? 
						new OpenDialogArchiveMoveThread(isnd,slttm.computeSeries(selectPaths),false,transferOnlyCheckBox.isSelected()) : //openStudyCheckBox.isSelected());
						new OpenDialogArchiveMoveThread(isnd,slttm.computeStudies(selectPaths),false,transferOnlyCheckBox.isSelected()); //openStudyCheckBox.isSelected());
					odamt.start();
				}
			} else {
				TreeSelectionModel tsm=dirTreeTable.getTreeSelectionModel();
				TreePath[] selectPaths=tsm.getSelectionPaths();
				File[] selectedFiles=new File[selectPaths.length];
				for (int loop=0; loop<selectPaths.length; loop++) selectedFiles[loop]=((FileNode)(selectPaths[loop].getLastPathComponent())).getFile();

				// Ultimately need to make this part dependent or specific to
				// a given image reader...

				ArrayList<Integer> paramList=new ArrayList<Integer>();
				if (imageWidthTextField.isEnabled()) paramList.add(Integer.parseInt(imageWidthTextField.getText()));
				if (imageHeightTextField.isEnabled()) paramList.add(Integer.parseInt(imageHeightTextField.getText()));
				if (bitsAllocatedTextField.isEnabled()) paramList.add(Integer.parseInt(bitsAllocatedTextField.getText()));
				if (bitsStoredTextField.isEnabled()) paramList.add(Integer.parseInt(bitsStoredTextField.getText()));
				if (fileOffsetTextField.isEnabled()) paramList.add(Integer.parseInt(fileOffsetTextField.getText()));
				if (numImagesTextField.isEnabled()) paramList.add(Integer.parseInt(numImagesTextField.getText()));
				OpenDialogFileThread odft=new OpenDialogFileThread(selectedFiles,(String)openAsComboBox.getSelectedItem(),
																													 recurseCheckBox.isSelected(),false,paramList); //openStudyCheckBox.isSelected());
				odft.start();				
			}

			if (closeOnOpenCheckBox.isSelected()) doClose();

		} else if ("search".equals(e.getActionCommand())) {

			if ((networkList.isSelectionEmpty())&&(localList.isSelectionEmpty())) return;

			try {

				String patientID=patientIDField.getText();
				String modality=modalityField.getText();
				Date startDate=startDatePicker.getDate();
				Date endDate=endDatePicker.getDate();
				ImageServerFindDescription isfd=new ImageServerFindDescription(patientID);

				if (startDate!=null) isfd.setStartDate(startDate);
				if (endDate!=null) isfd.setEndDate(endDate);
				isfd.setModality(modality);
				isfd.setResultScope(Scope.SERIES);
				String s=(String)localList.getSelectedValue();
				boolean isLocal=((s!=null)&&("local archive".equalsIgnoreCase(s)));
				if (!isLocal) {
					isfd.setNodeDescription((ImageServerNodeDescription)networkList.getSelectedValue());
					Boolean b=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.USE_XML_COMPRESSION);
					if ((b!=null)&&(b.booleanValue())) isfd.setCompression(Compression.GZIP);
				}
				treeTable.setEnabled(false);
				treeTable.setVisible(false);     // Hack because the cleared treetable doesn't update the display correctly and has leftovers
				studyList.clear();
				searchButton.setEnabled(false);
				clearFieldButton.setEnabled(false);
				OpenDialogFindThread odft=new OpenDialogFindThread(isfd,treeTable,searchButton,clearFieldButton,resultsLabel,isLocal);
				odft.start();
				
			} catch (Exception exc) {
				exc.printStackTrace();
			}

		} else if ("refreshNetwork".equals(e.getActionCommand())) {
			boolean networkStatus=ImageViewerClientNode.getInstance().connect();
			if (networkStatus) {
				List<ImageServerNodeDescription> nodes=ImageViewerClientNode.getInstance().getQueryableNodes();
				if (nodes!=null) networkList.setListData(nodes.toArray());
			}
		} else if ("clear".equals(e.getActionCommand())) {
			clearFields();
		} else if ("close".equals(e.getActionCommand())) {
			doClose();
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

	// =======================================================================

	private void clearFields() {

		patientNameField.setText(null);
		patientIDField.setText(null);
		modalityField.setText(null);
		anatomyField.setText(null);
		descriptionField.setText(null);
		startDatePicker.getEditor().setValue(null);
		endDatePicker.getEditor().setValue(null);
	}

	// =======================================================================

	private class TreeTableCellRenderer extends DefaultTreeCellRenderer {

		public TreeTableCellRenderer() {super();}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			
			Component c=super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
			setOpaque(false);
			setBackground(COL_COLOR1);
			setBackgroundNonSelectionColor(null);
			setBackgroundSelectionColor(null);
			if ((value instanceof FileNode)&&(c instanceof JLabel)) {
				File f=((FileNode)value).getFile();
				if ((f!=null)&&(f.exists())) {
					FileSystemView fsv=FileSystemView.getFileSystemView();
					if (fsv!=null) {
						Icon i=fsv.getSystemIcon(f);
						if (i!=null) ((JLabel)c).setIcon(i);
					}
				}
			}
			return c;
		}
	}

	// =======================================================================
	
	private class NodeDescriptionRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			
			String name=((ImageServerNodeDescription)value).getDisplayName();
			if ((name==null)||("null".equals(name))) name=((ImageServerNodeDescription)value).getIPAddress();
			return super.getListCellRendererComponent(list,name,index,isSelected,cellHasFocus);
		}
	}
}

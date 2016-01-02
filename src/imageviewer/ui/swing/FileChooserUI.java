/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.LayoutManager;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.filechooser.FileSystemView;

import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.plaf.basic.BasicFileChooserUI;

import sun.awt.shell.ShellFolder;
import sun.swing.FilePane;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

// =======================================================================
// Taken from the MetalFileChooserUI in Sun's Swing package. Adapated
// to handle different UI components in imageViewer. Couldn't subclass
// as the metal version of this class keeps everything private...Also,
// replaced components with the JGoodies formLayout for a cleaner
// layout of the involved components.

public class FileChooserUI extends BasicFileChooserUI {

	private static final Dimension hstrut5=new Dimension(10,1);
	private static final Dimension hstrut11=new Dimension(11,1);
	private static final Dimension vstrut5=new Dimension(1,5);

	private static final Insets shrinkwrap=new Insets(3,3,3,3);

	// Preferred and Minimum sizes for the dialog box

	private static int LIST_PREF_WIDTH=405;
	private static int LIST_PREF_HEIGHT=135;
	private static int MIN_WIDTH=500;
	private static int MIN_HEIGHT=326;
	private static int PREF_WIDTH=500;
	private static int PREF_HEIGHT=326;

	private final static int space=10;
	
	private static Dimension PREF_SIZE=new Dimension(PREF_WIDTH,PREF_HEIGHT);
	private static Dimension MIN_SIZE=new Dimension(MIN_WIDTH,MIN_HEIGHT);
	private static Dimension LIST_PREF_SIZE=new Dimension(LIST_PREF_WIDTH,LIST_PREF_HEIGHT);

	// =======================================================================
	// Labels, mnemonics, and tooltips (oh my!)

	int lookInLabelMnemonic=0;
	int fileNameLabelMnemonic=0;
	int filesOfTypeLabelMnemonic=0;

	String lookInLabelText=null;
	String saveInLabelText=null;
	String fileNameLabelText=null;
	String filesOfTypeLabelText=null;
	String upFolderToolTipText=null;
	String upFolderAccessibleName=null;
	String homeFolderToolTipText=null;
	String homeFolderAccessibleName=null;
	String newFolderToolTipText=null;
	String newFolderAccessibleName=null;
	String listViewButtonToolTipText=null;
	String listViewButtonAccessibleName=null;
	String detailsViewButtonToolTipText=null;
	String detailsViewButtonAccessibleName=null;

	DirectoryComboBoxModel directoryComboBoxModel=null;
	Action directoryComboBoxAction=new DirectoryComboBoxAction();
	FilterComboBoxModel filterComboBoxModel=null;
	FilePane filePane=null;

	JComboBox filterComboBox=null;	
	JComboBox directoryComboBox=null;
	JLabel lookInLabel=null;
	JToggleButton listViewButton=null;
	JToggleButton detailsViewButton=null;
	JButton approveButton=null;
	JButton cancelButton=null;
	JPanel buttonPanel=null;
	JPanel bottomPanel=null;
	JTextField fileNameTextField=null;

  BasicFileView fileView=new SystemFileView();

	boolean useShellFolder;

	// =======================================================================

	public static ComponentUI createUI(JComponent c) {return new FileChooserUI((JFileChooser)c);}
	
	// =======================================================================

	public FileChooserUI(JFileChooser filechooser) {super(filechooser);}

	public void installUI(JComponent c) {super.installUI(c);}
	public void uninstallComponents(JFileChooser fc) {fc.removeAll();	bottomPanel=null;	buttonPanel=null;}

	// =======================================================================

	private class FileChooserUIAccessor implements FilePane.FileChooserUIAccessor {

		public JFileChooser getFileChooser() {return FileChooserUI.this.getFileChooser();}
		public BasicDirectoryModel getModel() {return FileChooserUI.this.getModel();}

		public JPanel createList() {return FileChooserUI.this.createList(getFileChooser());}
		public JPanel createDetailsView() {return FileChooserUI.this.createDetailsView(getFileChooser());}

		public boolean isDirectorySelected() {return FileChooserUI.this.isDirectorySelected();}
		public File getDirectory() {return FileChooserUI.this.getDirectory();}

		public Action getChangeToParentDirectoryAction() {return FileChooserUI.this.getChangeToParentDirectoryAction();}
		public Action getApproveSelectionAction() {return FileChooserUI.this.getApproveSelectionAction();}
		public Action getNewFolderAction() {return FileChooserUI.this.getNewFolderAction();}

		public MouseListener createDoubleClickListener(JList list) {return FileChooserUI.this.createDoubleClickListener(getFileChooser(),list);}
		public ListSelectionListener createListSelectionListener() {return FileChooserUI.this.createListSelectionListener(getFileChooser());}

		public boolean usesShellFolder() {return true;}
	}

	// =======================================================================

	public void installComponents(JFileChooser fc) {

		FormLayout fl=new FormLayout("right:pref,2dlu,pref:grow,2dlu,26px,2px,26px,5px,26px,5px,26px,2px,26px","pref,5px,fill:pref:grow,5px,pref,5px,pref,5px,pref");
		CellConstraints cc=new CellConstraints();

		FileSystemView fsv=fc.getFileSystemView();
		fc.setBorder(new EmptyBorder(5,5,5,5));
		fc.setLayout(fl);
		filePane=new FilePane(new FileChooserUIAccessor());
		fc.addPropertyChangeListener(filePane);
		updateUseShellFolder();

		// Directory manipulation buttons

		JPanel topPanel=new JPanel(new BorderLayout(3,0));
		lookInLabel=new JLabel(lookInLabelText);
		lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
		topPanel.add(lookInLabel,BorderLayout.WEST);

		// CurrentDir ComboBox

		directoryComboBox=new JComboBox() {
			public Dimension getPreferredSize() {
				Dimension d=super.getPreferredSize();
				d.width=150;
				return d;
			}
		};

		directoryComboBox.setLightWeightPopupEnabled(false);
		directoryComboBox.getAccessibleContext().setAccessibleDescription(lookInLabelText);
		directoryComboBox.putClientProperty("JComboBox.isTableCellEditor",Boolean.TRUE);
		lookInLabel.setLabelFor(directoryComboBox);
		directoryComboBoxModel=createDirectoryComboBoxModel(fc);
		directoryComboBox.setModel(directoryComboBoxModel);
		directoryComboBox.addActionListener(directoryComboBoxAction);
		directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
		directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		directoryComboBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
		directoryComboBox.setMaximumRowCount(8);
		topPanel.add(directoryComboBox,BorderLayout.CENTER);
		fc.add(topPanel,cc.xywh(1,1,3,1));

		// Up Button

		JButton upFolderButton=new JButton(getChangeToParentDirectoryAction());
		upFolderButton.setText(null);
		upFolderButton.setIcon(upFolderIcon);
		upFolderButton.setToolTipText(upFolderToolTipText);
		upFolderButton.getAccessibleContext().setAccessibleName(upFolderAccessibleName);
		upFolderButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		upFolderButton.setMargin(shrinkwrap);
		fc.add(upFolderButton,cc.xy(5,1));

		// Home Button

		File homeDir=fsv.getHomeDirectory();
		String toolTipText=homeFolderToolTipText;
		if (fsv.isRoot(homeDir)) toolTipText=getFileView(fc).getName(homeDir);                     // Probably "Desktop"
	
		JButton b=new JButton(homeFolderIcon);
		b.setToolTipText(toolTipText);
		b.getAccessibleContext().setAccessibleName(homeFolderAccessibleName);
		b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		b.setMargin(shrinkwrap);
		b.addActionListener(getGoHomeAction());
		fc.add(b,cc.xy(7,1));

		// New Directory Button
		
		if (!UIManager.getBoolean("FileChooser.readOnly")) {
			b=new JButton(filePane.getNewFolderAction());
			b.setText(null);
			b.setIcon(newFolderIcon);
			b.setToolTipText(newFolderToolTipText);
			b.getAccessibleContext().setAccessibleName(newFolderAccessibleName);
			b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
			b.setMargin(shrinkwrap);
			fc.add(b,cc.xy(9,1));
		}

		// View button group

		ButtonGroup viewButtonGroup=new ButtonGroup();

		// List Button

		listViewButton=new JToggleButton(listViewIcon);
		listViewButton.setToolTipText(listViewButtonToolTipText);
		listViewButton.getAccessibleContext().setAccessibleName(listViewButtonAccessibleName);
		listViewButton.setSelected(true);
		listViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		listViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		listViewButton.setMargin(shrinkwrap);
		listViewButton.setBorderPainted(false);
		listViewButton.setRolloverEnabled(true);
		listViewButton.addActionListener(filePane.getViewTypeAction(FilePane.VIEWTYPE_LIST));
		viewButtonGroup.add(listViewButton);
		fc.add(listViewButton,cc.xy(11,1));

		// Details Button

		detailsViewButton=new JToggleButton(detailsViewIcon);
		detailsViewButton.setToolTipText(detailsViewButtonToolTipText);
		detailsViewButton.getAccessibleContext().setAccessibleName(detailsViewButtonAccessibleName);
		detailsViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		detailsViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		detailsViewButton.setBorderPainted(false);
		detailsViewButton.setRolloverEnabled(true);
		detailsViewButton.setMargin(shrinkwrap);
		detailsViewButton.addActionListener(filePane.getViewTypeAction(FilePane.VIEWTYPE_DETAILS));
		viewButtonGroup.add(detailsViewButton);
		fc.add(detailsViewButton,cc.xy(13,1));

		filePane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if ("viewType".equals(e.getPropertyName())) {
					int viewType=filePane.getViewType();
					switch (viewType) {
						case FilePane.VIEWTYPE_LIST: listViewButton.setSelected(true); break;
					  case FilePane.VIEWTYPE_DETAILS: detailsViewButton.setSelected(true); break;
					}
				}
			}
		});

		filePane.setPreferredSize(LIST_PREF_SIZE);
		if (getAccessoryPanel()!=null) {
			JPanel midPanel=new JPanel(new BorderLayout(0,0));
			midPanel.add(getAccessoryPanel(),BorderLayout.AFTER_LINE_ENDS);
			JComponent accessory=fc.getAccessory();
			if (accessory!=null) getAccessoryPanel().add(accessory);
			midPanel.add(filePane,BorderLayout.CENTER);
			fc.add(midPanel,cc.xywh(1,3,13,1));
		} else {
			fc.add(filePane,cc.xywh(1,3,13,1));
		}

		// FileName label and textfield

		AlignedLabel fileNameLabel=new AlignedLabel(fileNameLabelText);
		fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
		fc.add(fileNameLabel,cc.xy(1,5));

		fileNameTextField=new JTextField(35) {public Dimension getMaximumSize() {return new Dimension(Short.MAX_VALUE,super.getPreferredSize().height);}};
		fc.add(fileNameTextField,cc.xywh(3,5,11,1));
		fileNameLabel.setLabelFor(fileNameTextField);
		fileNameTextField.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				if (!getFileChooser().isMultiSelectionEnabled()) {
					filePane.clearSelection();
				}
			}
		});

		setFileName((fc.isMultiSelectionEnabled()) ? fileNameString(fc.getSelectedFiles()) : fileNameString(fc.getSelectedFile()));

		// Filetype label and combobox

		AlignedLabel filesOfTypeLabel=new AlignedLabel(filesOfTypeLabelText);
		filesOfTypeLabel.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
		fc.add(filesOfTypeLabel,cc.xy(1,7));

		filterComboBoxModel=createFilterComboBoxModel();
		fc.addPropertyChangeListener(filterComboBoxModel);
		filterComboBox=new JComboBox(filterComboBoxModel);
		filterComboBox.getAccessibleContext().setAccessibleDescription(filesOfTypeLabelText);
		filterComboBox.setLightWeightPopupEnabled(false);
		filesOfTypeLabel.setLabelFor(filterComboBox);
		filterComboBox.setRenderer(createFilterComboBoxRenderer());
		fc.add(filterComboBox,cc.xywh(3,7,11,1));

		// Buttons

		approveButton=new JButton(getApproveButtonText(fc));
		approveButton.addActionListener(getApproveSelectionAction());
		approveButton.setToolTipText(getApproveButtonToolTipText(fc));
		cancelButton=new JButton(cancelButtonText);
		cancelButton.setToolTipText(cancelButtonToolTipText);
		cancelButton.addActionListener(getCancelSelectionAction());

		getButtonPanel().add(approveButton);
		getButtonPanel().add(Box.createHorizontalStrut(5));
		getButtonPanel().add(cancelButton); 
		getBottomPanel().add(getButtonPanel());
		fc.add(getBottomPanel(),cc.xywh(1,9,13,1));

		if (fc.getControlButtonsAreShown()) addControlButtons();
		groupLabels(new AlignedLabel[] {fileNameLabel,filesOfTypeLabel});
	}

	// =======================================================================
	// Decide whether to use the ShellFolder class to populate shortcut
	// panel and combobox.

	private void updateUseShellFolder() {

		JFileChooser fc=getFileChooser();
		Boolean prop=(Boolean)fc.getClientProperty("FileChooser.useShellFolder");
		if (prop != null) {
	    useShellFolder=prop.booleanValue();
		} else {
	    useShellFolder=false;
	    File[] roots=fc.getFileSystemView().getRoots();
	    if ((roots!=null)&&(roots.length==1)) {
				File[] cbFolders=(File[])ShellFolder.get("fileChooserComboBoxFolders");
				if ((cbFolders!=null)&&(cbFolders.length>0)&&(roots[0]==cbFolders[0])) useShellFolder=true;
	    }
		}
	}

	// =======================================================================
	
	protected JPanel getButtonPanel() {if (buttonPanel==null) buttonPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); return buttonPanel;}
	protected JPanel getBottomPanel() {if (bottomPanel==null) bottomPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); return bottomPanel;}

	// =======================================================================

	protected void installStrings(JFileChooser fc) {

		super.installStrings(fc);
		Locale l=fc.getLocale();
		lookInLabelMnemonic=UIManager.getInt("FileChooser.lookInLabelMnemonic"); 
		lookInLabelText=UIManager.getString("FileChooser.lookInLabelText",l);
		saveInLabelText=UIManager.getString("FileChooser.saveInLabelText",l);
		fileNameLabelMnemonic=UIManager.getInt("FileChooser.fileNameLabelMnemonic");  
		fileNameLabelText=UIManager.getString("FileChooser.fileNameLabelText",l); 
		filesOfTypeLabelMnemonic=UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");  
		filesOfTypeLabelText=UIManager.getString("FileChooser.filesOfTypeLabelText",l); 
		upFolderToolTipText= UIManager.getString("FileChooser.upFolderToolTipText",l);
		upFolderAccessibleName=UIManager.getString("FileChooser.upFolderAccessibleName",l); 
		homeFolderToolTipText= UIManager.getString("FileChooser.homeFolderToolTipText",l);
		homeFolderAccessibleName=UIManager.getString("FileChooser.homeFolderAccessibleName",l); 
		newFolderToolTipText=UIManager.getString("FileChooser.newFolderToolTipText",l);
		newFolderAccessibleName=UIManager.getString("FileChooser.newFolderAccessibleName",l); 
		listViewButtonToolTipText=UIManager.getString("FileChooser.listViewButtonToolTipText",l); 
		listViewButtonAccessibleName=UIManager.getString("FileChooser.listViewButtonAccessibleName",l); 
		detailsViewButtonToolTipText=UIManager.getString("FileChooser.detailsViewButtonToolTipText",l); 
		detailsViewButtonAccessibleName=UIManager.getString("FileChooser.detailsViewButtonAccessibleName",l); 
	}

	// =======================================================================

	protected void installListeners(JFileChooser fc) {
		super.installListeners(fc);
		ActionMap actionMap=getActionMap();
		SwingUtilities.replaceUIActionMap(fc,actionMap);
	}

	// =======================================================================

	protected ActionMap getActionMap() {return createActionMap();}

	protected ActionMap createActionMap() {
		ActionMap map=new ActionMapUIResource();
		FilePane.addActionsToMap(map,filePane.getActions());
		return map;
	}

	// =======================================================================

	protected JPanel createList(JFileChooser fc) {return filePane.createList();}
	protected JPanel createDetailsView(JFileChooser fc) {return filePane.createDetailsView();}

	public FileView getFileView(JFileChooser fc) {return fileView;}

	// =======================================================================

	public ListSelectionListener createListSelectionListener(JFileChooser fc) {return super.createListSelectionListener(fc);}

	public void uninstallUI(JComponent c) {

		c.removePropertyChangeListener(filterComboBoxModel);
		c.removePropertyChangeListener(filePane);
		cancelButton.removeActionListener(getCancelSelectionAction());
		approveButton.removeActionListener(getApproveSelectionAction());
		fileNameTextField.removeActionListener(getApproveSelectionAction());
		super.uninstallUI(c);
	}

	// =======================================================================
	/* Returns the preferred size of the specified
	 * <code>JFileChooser</code>.  The preferred size is at least as
	 * large, in both height and width, as the preferred size
	 * recommended by the file chooser's layout manager.
	 *
	 * @param c  a <code>JFileChooser</code>
	 * @return   a <code>Dimension</code> specifying the preferred
	 *           width and height of the file chooser
	 */

	public Dimension getPreferredSize(JComponent c) {

		int prefWidth=PREF_SIZE.width;
		Dimension d=c.getLayout().preferredLayoutSize(c);
		return (d!=null) ? new Dimension((d.width<prefWidth) ? prefWidth : d.width, (d.height<PREF_SIZE.height) ? PREF_SIZE.height : d.height) : 
	    new Dimension(prefWidth,PREF_SIZE.height);
	}

	// =======================================================================

	public Dimension getMinimumSize(JComponent c) {return MIN_SIZE;}
	public Dimension getMaximumSize(JComponent c) {return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);}

	// =======================================================================

	private String fileNameString(File file) {

		if (file==null) return null;
		JFileChooser fc=getFileChooser();
		return ((fc.isDirectorySelectionEnabled())&&(!fc.isFileSelectionEnabled())) ? file.getPath() : file.getName();
	}

	private String fileNameString(File[] files) {

		StringBuffer buf=new StringBuffer();
		for (int i=0; ((files!=null)&&(i<files.length)); i++) {
	    if (i>0) buf.append(" ");
	    if (files.length>1) buf.append("\"");
	    buf.append(fileNameString(files[i]));
	    if (files.length>1) buf.append("\"");
		}
		return buf.toString();
	}

	// =======================================================================
	// The following methods are used by the PropertyChange Listener 

	private void doSelectedFileChanged(PropertyChangeEvent e) {

		File f=(File) e.getNewValue();
		JFileChooser fc=getFileChooser();
		if ((f!=null)&&((fc.isFileSelectionEnabled() && !f.isDirectory()) || (f.isDirectory() && fc.isDirectorySelectionEnabled()))) {
			setFileName(fileNameString(f));
		}
	}
    
	private void doSelectedFilesChanged(PropertyChangeEvent e) {

		File[] files=(File[]) e.getNewValue();
		JFileChooser fc=getFileChooser();
		if ((files!=null) && (files.length>0) && (files.length>1 || fc.isDirectorySelectionEnabled() || !files[0].isDirectory())) {
	    setFileName(fileNameString(files));
		}
	}
    
	private void doDirectoryChanged(PropertyChangeEvent e) {

		JFileChooser fc=getFileChooser();
		FileSystemView fsv=fc.getFileSystemView();
		clearIconCache();
		File currentDirectory=fc.getCurrentDirectory();
		if (currentDirectory!=null) {
	    directoryComboBoxModel.addItem(currentDirectory);
	    if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) setFileName((fsv.isFileSystem(currentDirectory)) ? currentDirectory.getPath() : null);
		}
	}

	private void doFilterChanged(PropertyChangeEvent e) {clearIconCache();}

	private void doFileSelectionModeChanged(PropertyChangeEvent e) {

		clearIconCache();
		JFileChooser fc=getFileChooser();
		File currentDirectory=fc.getCurrentDirectory();
		if ((currentDirectory!=null)&&(fc.isDirectorySelectionEnabled())&&(!fc.isFileSelectionEnabled())&&(fc.getFileSystemView().isFileSystem(currentDirectory))) {
	    setFileName(currentDirectory.getPath());
		} else {
	    setFileName(null);
		}
	}

	private void doAccessoryChanged(PropertyChangeEvent e) {

		if (getAccessoryPanel()!=null) {
	    if (e.getOldValue()!=null) getAccessoryPanel().remove((JComponent) e.getOldValue());
	    JComponent accessory=(JComponent) e.getNewValue();
	    if (accessory!=null) getAccessoryPanel().add(accessory,BorderLayout.CENTER);
		}
	}

	private void doApproveButtonTextChanged(PropertyChangeEvent e) {

		JFileChooser chooser=getFileChooser();
		approveButton.setText(getApproveButtonText(chooser));
		approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
	}

	private void doDialogTypeChanged(PropertyChangeEvent e) {

		JFileChooser chooser=getFileChooser();
		approveButton.setText(getApproveButtonText(chooser));
		approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
		lookInLabel.setText((chooser.getDialogType()==JFileChooser.SAVE_DIALOG) ? saveInLabelText : lookInLabelText);
	}

	private void doApproveButtonMnemonicChanged(PropertyChangeEvent e) {}

	private void doControlButtonsChanged(PropertyChangeEvent e) {if (getFileChooser().getControlButtonsAreShown()) addControlButtons(); else removeControlButtons();}

	public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {

		return new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent e) {
				String s=e.getPropertyName();
				if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
					doSelectedFileChanged(e);
				} else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
					doSelectedFilesChanged(e);
				} else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
					doDirectoryChanged(e);
				} else if (s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
					doFilterChanged(e);
				} else if (s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
					doFileSelectionModeChanged(e);
				} else if (s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
					doAccessoryChanged(e);
				} else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) ||
									 s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) { 
					doApproveButtonTextChanged(e);
				} else if (s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
					doDialogTypeChanged(e);
				} else if (s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
					doApproveButtonMnemonicChanged(e);
				} else if (s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
					doControlButtonsChanged(e);
				} else if (s.equals("componentOrientation")) {
					ComponentOrientation o=(ComponentOrientation)e.getNewValue();
					JFileChooser cc=(JFileChooser)e.getSource();
					if (o!=(ComponentOrientation)e.getOldValue()) {
						cc.applyComponentOrientation(o);
					}
				} else if (s=="FileChooser.useShellFolder") {
					updateUseShellFolder();
					doDirectoryChanged(e);
				} else if (s.equals("ancestor")) {
					if (e.getOldValue()==null && e.getNewValue()!=null) {
						fileNameTextField.selectAll();
						fileNameTextField.requestFocus();
					}
				}
			}
		};
	}

	// =======================================================================

	protected void removeControlButtons() {getBottomPanel().remove(getButtonPanel());}
	protected void addControlButtons() {getBottomPanel().add(getButtonPanel());}

	public void ensureFileIsVisible(JFileChooser fc, File f) {filePane.ensureFileIsVisible(fc,f);}
	public void rescanCurrentDirectory(JFileChooser fc) {filePane.rescanCurrentDirectory();}
	public void setFileName(String filename) {if (fileNameTextField!=null) fileNameTextField.setText(filename);}

	public String getFileName() {return (fileNameTextField!=null) ? fileNameTextField.getText() : null;}
	
	// =======================================================================

	protected void setDirectorySelected(boolean directorySelected) {

		super.setDirectorySelected(directorySelected);
		JFileChooser chooser=getFileChooser();
		if (directorySelected) {
			if (approveButton!=null) {
				approveButton.setText(directoryOpenButtonText);
				approveButton.setToolTipText(directoryOpenButtonToolTipText);
			}
		} else {
			if (approveButton!=null) {
				approveButton.setText(getApproveButtonText(chooser));
				approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
			}
		}
	}

	// =======================================================================

	protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {return new DirectoryComboBoxRenderer();}

	// =======================================================================

	private class DirectoryComboBoxRenderer extends DefaultListCellRenderer  {

		IndentIcon ii=new IndentIcon();

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

	    super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
	    if (value==null) {
				setText("");
				return this;
	    }
	    File directory=(File)value;
	    setText(getFileChooser().getName(directory));
	    Icon icon=getFileChooser().getIcon(directory);
	    ii.icon=icon;
	    ii.depth=directoryComboBoxModel.getDepth(index);
	    setIcon(ii);
	    return this;
		}
	}

	// =======================================================================

	private class IndentIcon implements Icon {

		Icon icon=null;
		int depth=0;

		public void paintIcon(Component c, Graphics g, int x, int y) {icon.paintIcon(c,g,(c.getComponentOrientation().isLeftToRight()) ? x+depth*space : x,y);}

		public int getIconWidth() {return (icon.getIconWidth()+(depth*space));}
		public int getIconHeight() {return icon.getIconHeight();}
	}

	// =======================================================================

	protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {return new DirectoryComboBoxModel();}

	protected class DirectoryComboBoxModel extends AbstractListModel implements ComboBoxModel {

		Vector directories=new Vector();
		int[] depths=null;
		File selectedDirectory=null;
		JFileChooser chooser=getFileChooser();
		FileSystemView fsv=chooser.getFileSystemView();

		public DirectoryComboBoxModel() {

	    File dir=getFileChooser().getCurrentDirectory();
	    if (dir!=null) addItem(dir);
		}

		/**
		 * Adds the directory to the model and sets it to be selected,
		 * additionally clears out the previous selected directory and the
		 * paths leading up to it,if any.
		 */

		private void addItem(File directory) {

	    if (directory==null) return;
	    directories.clear();
	    File[] baseFolders;
	    baseFolders=(useShellFolder) ? (File[])ShellFolder.get("fileChooserComboBoxFolders") : fsv.getRoots();
	    directories.addAll(Arrays.asList(baseFolders));

	    // Get the canonical (full) path. This has the side benefit of
	    // removing extraneous chars from the path, for example
	    // /foo/bar/ becomes /foo/bar

	    File canonical=null;
	    try {
				canonical=directory.getCanonicalFile();
	    } catch (IOException e) {
				canonical=directory;
	    }

	    // Create File instances of each directory leading up to the top

	    try {
				File sf=(useShellFolder) ? ShellFolder.getShellFolder(canonical) : canonical;
				File f=sf;
				Vector path=new Vector(10);
				do {
					path.addElement(f);
				} while ((f=f.getParentFile())!=null);

				int pathCount=path.size();
				for (int i=0; i<pathCount; i++) {
					f=(File)path.get(i);
					if (directories.contains(f)) {
						int topIndex=directories.indexOf(f);
						for (int j=i-1; j>=0; j--) directories.insertElementAt(path.get(j),topIndex+i-j);
						break;
					}
				}
				calculateDepths();
				setSelectedItem(sf);
	    } catch (FileNotFoundException ex) {
				calculateDepths();
	    }
		}

		private void calculateDepths() {

	    depths=new int[directories.size()];
	    for (int i=0; i<depths.length; i++) {
				File dir=(File)directories.get(i);
				File parent=dir.getParentFile();
				depths[i]=0;
				if (parent!=null) {
					for (int j=i-1; j>=0; j--) {
						if (parent.equals((File)directories.get(j))) {
							depths[i]=depths[j]+1;
							break;
						}
					}
				}
	    }
		}

		public int getDepth(int i) {return (depths!=null && i >= 0 && i<depths.length) ? depths[i] : 0;}
		public int getSize() {return directories.size();}
		public Object getSelectedItem() {return selectedDirectory;}
		public Object getElementAt(int index) {return directories.elementAt(index);}
		public void setSelectedItem(Object selectedDirectory) {this.selectedDirectory=(File)selectedDirectory; fireContentsChanged(this,-1,-1);}
	}

	// =======================================================================

	protected FilterComboBoxRenderer createFilterComboBoxRenderer() {return new FilterComboBoxRenderer();}

	public class FilterComboBoxRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

	    super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
	    if (value!=null && value instanceof FileFilter) setText(((FileFilter)value).getDescription());
	    return this;
		}
	}

	// =======================================================================

	protected FilterComboBoxModel createFilterComboBoxModel() {return new FilterComboBoxModel();}

	protected class FilterComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {

		protected FileFilter[] filters;

		protected FilterComboBoxModel() {super(); filters=getFileChooser().getChoosableFileFilters();}

		public void propertyChange(PropertyChangeEvent e) {

	    String prop=e.getPropertyName();
	    if (prop==JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
				filters=(FileFilter[]) e.getNewValue();
				fireContentsChanged(this,-1,-1);
	    } else if (prop==JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
				fireContentsChanged(this,-1,-1);
	    }
		}

		public void setSelectedItem(Object filter) {

	    if (filter!=null) {
				getFileChooser().setFileFilter((FileFilter) filter);
				setFileName(null);
				fireContentsChanged(this,-1,-1);
	    }
		}

		public Object getSelectedItem() {

	    // Ensure that the current filter is in the list.  NOTE: we
	    // shouldnt' have to do this,since JFileChooser adds the filter
	    // to the choosable filters list when the filter is set. Lets be
	    // paranoid just in case someone overrides setFileFilter in
	    // JFileChooser.

	    FileFilter currentFilter=getFileChooser().getFileFilter();
	    boolean found=false;
	    if (currentFilter!=null) {
				for(int i=0; i<filters.length; i++) if (filters[i]==currentFilter) found=true;
				if (found==false) getFileChooser().addChoosableFileFilter(currentFilter);
	    }
	    return getFileChooser().getFileFilter();
		}

		public int getSize() {return (filters!=null) ? filters.length : 0;}

		public Object getElementAt(int index) {if (index>getSize()-1) return getFileChooser().getFileFilter(); return (filters!=null) ? filters[index] : null;}
	}

	// =======================================================================

	public void valueChanged(ListSelectionEvent e) {

		JFileChooser fc=getFileChooser();
		File f=fc.getSelectedFile();
		if (!e.getValueIsAdjusting() && f!=null && !getFileChooser().isTraversable(f)) setFileName(fileNameString(f));
	}

	// =======================================================================

	protected class DirectoryComboBoxAction extends AbstractAction {

		protected DirectoryComboBoxAction() {super("DirectoryComboBoxAction");}

		public void actionPerformed(ActionEvent e) {
			directoryComboBox.hidePopup();
	    File f=(File)directoryComboBox.getSelectedItem();
			if (!getFileChooser().getCurrentDirectory().equals(f)) getFileChooser().setCurrentDirectory(f);
		}
	}

	protected JButton getApproveButton(JFileChooser fc) {return approveButton;}

	private static void groupLabels(AlignedLabel[] group) {for (int i=0; i<group.length; i++) group[i].group=group;}

	// =======================================================================

	private class AlignedLabel extends JLabel {

		private AlignedLabel[] group;
		private int maxWidth=0;

		AlignedLabel(String text) {super(text,JLabel.TRAILING);}

		public Dimension getPreferredSize() {

			Dimension d=super.getPreferredSize();
	    return new Dimension(getMaxWidth()+11,d.height);
		}

		private int getMaxWidth() {

	    if ((maxWidth==0)&&(group!=null)) {
				int max=0;
				for (int i=0; i<group.length; i++) max=Math.max(group[i].getSuperPreferredWidth(),max);
				for (int i=0; i<group.length; i++) group[i].maxWidth=max;
	    }
	    return maxWidth;
		}

		private int getSuperPreferredWidth() {return super.getPreferredSize().width;}
	}

	// =======================================================================
	// Override default behavior to use system icons, as per the windows L&F.

	protected class SystemFileView extends BasicFileView {

		public Icon getIcon(File f) {
	    Icon icon=getCachedIcon(f);
	    if (icon!=null) return icon;
	    if (f!=null) icon=getFileChooser().getFileSystemView().getSystemIcon(f);
	    if (icon==null) icon=super.getIcon(f);
	    cacheIcon(f,icon);
	    return icon;
		}
	}
}

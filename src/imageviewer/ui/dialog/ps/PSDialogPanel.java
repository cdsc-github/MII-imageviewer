/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog.ps;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.painter.Painter;

import imageserver.model.Associable;
import imageserver.model.AssociatedData;
import imageserver.model.AssociablePresentationState;
import imageserver.model.AssociatedData.AssocDataType;
import imageserver.model.Series;

import imageviewer.model.Image;
import imageviewer.model.PresentationState;

import imageviewer.system.ImageViewerClientNode;
import imageviewer.system.SaveStack;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.FloatingPanel;
import imageviewer.ui.FloatingPanelActionListener;
import imageviewer.ui.UserManager;

import imageviewer.ui.dialog.DialogUtil;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.TableHeaderRenderer;
import imageviewer.ui.swing.VerticalLabelUI;

// =======================================================================

public class PSDialogPanel extends JXPanel implements FloatingPanelActionListener, ActionListener, ItemListener, CaretListener {

	private static final JCheckBox ASK_CB_AUTHOR=new JCheckBox("Do not show this message again.",false);
	private static final JCheckBox ASK_CB_DELETE=new JCheckBox("Do not show this message again.",false);

	private static final SimpleDateFormat DATE_FORMAT_SRC=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT_TRG=new SimpleDateFormat("MM/dd/yy h:mm a");

	private static final int[] COLUMN_WIDTH=new int[] {100,200};
	private static PSDialogPanel PSDP_INSTANCE=null;

	public static PSDialogPanel getInstance() {if (PSDP_INSTANCE==null) PSDP_INSTANCE=new PSDialogPanel(); return PSDP_INSTANCE;}

	// =======================================================================

	PSCollectionTableModel pctm=new PSCollectionTableModel();
	JXTable collectionTable=null, fixedColumnTable=null;
	PSViewPanel psView=new PSViewPanel();
	String currentAssocKey=null;
	PSThumbnailPanel currentSelection=null;
	JScrollPane studyScrollPane=null;
	JButton deleteListButton=new JButton("Delete current collection");
	JButton deletePSButton=new JButton("Delete current presentation state");
	JTextField authorField=new JTextField(25);
	JTextField timestampField=new JTextField(30);
	JTextField descriptionField=new JTextField(256);
	
	private PSDialogPanel() {

		super();
		FormLayout fl=new FormLayout("fill:pref:grow,2dlu,fill:pref:grow,4dlu,pref,2dlu,fill:pref:grow,2dlu,pref,2dlu,pref,2dlu,right:pref:grow",
																 "pref,4px,pref,2px,pref,10px,fill:pref:grow,2px,pref,2px,pref,2px,top:pref");
		setLayout(fl);
		setBorder(new EmptyBorder(5,5,5,5));
		setPreferredSize(new Dimension(995,675));
		setLocation(50,50);
		
		JTextArea psPanelDescription=DialogUtil.createTextArea("Click on the thumbnails to view the presentation state in full.  Drag images (ctrl-left-click drag) "+
																													 "to rearrange thumbnails within a row.");
		JTextArea psViewDescription=DialogUtil.createTextArea("Drag images (ctrl-left-click drag) into the panel below to capture presentation states.  "+
																													"You can enter additional descriptive information into associated the text fields.");

		JButton closeButton=new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);
		JLabel studyLabel=new JLabel("Collection navigation",JLabel.LEFT);
		JLabel viewLabel=new JLabel("Presentation state viewer",JLabel.LEFT);
		JSeparator separator1=new JSeparator();
		separator1.setPreferredSize(new Dimension(415,5));
		JSeparator separator2=new JSeparator();
		separator2.setPreferredSize(new Dimension(525,5));

		PSTableCellRenderer pstcr=new PSTableCellRenderer();
		PSTableCellEditor pstce=new PSTableCellEditor();
		TableHeaderRenderer thr=new TableHeaderRenderer(Color.black);

		collectionTable=new JXTable(pctm) {public void toggleSortOrder(int columnIndex) {pctm.toggleSort();}};
		collectionTable.setHorizontalScrollEnabled(true);
		collectionTable.setShowHorizontalLines(true);
		collectionTable.setShowVerticalLines(false);
		collectionTable.setColumnMargin(1);
		collectionTable.setColumnControlVisible(false);
		collectionTable.setRolloverEnabled(false);
		collectionTable.setRowSelectionAllowed(true);
		collectionTable.setColumnSelectionAllowed(false);
		collectionTable.setRowHeight(132);
		collectionTable.getTableHeader().setReorderingAllowed(false);
		collectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (int i=0; i<COLUMN_WIDTH.length; i++) {
			TableColumn column=collectionTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(COLUMN_WIDTH[i]);
			column.setHeaderRenderer(thr);
			column.setCellRenderer(pstcr);
			column.setCellEditor(pstce);
    }

		// Mutilate the collectionTable and its model so that we can put
		// just the thumbnails into a scrollpane and freeze the first
		// column.  Support for fixed columns not forthcoming anytime soon
		// from swingx or sun...Override the togglesortorder so we know
		// when a sort even occurs, and then we can also sort the
		// collectionTable, too.

		fixedColumnTable=new JXTable(collectionTable.getModel()) {public void toggleSortOrder(int columnIndex) {pctm.toggleSort();}};
		fixedColumnTable.setFocusable(false);
		fixedColumnTable.setSelectionModel(collectionTable.getSelectionModel());
		fixedColumnTable.getTableHeader().setReorderingAllowed(false);
		fixedColumnTable.getTableHeader().setResizingAllowed(false);
		fixedColumnTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		fixedColumnTable.getColumnModel().getColumn(0).setHeaderRenderer(thr);
		fixedColumnTable.getColumnModel().getColumn(0).setCellRenderer(pstcr);
		fixedColumnTable.setHorizontalScrollEnabled(true);
		fixedColumnTable.setShowHorizontalLines(true);
		fixedColumnTable.setShowVerticalLines(true);
		fixedColumnTable.setColumnMargin(1);
		fixedColumnTable.setColumnControlVisible(false);
		fixedColumnTable.setRolloverEnabled(false);
		fixedColumnTable.setRowSelectionAllowed(true);
		fixedColumnTable.setColumnSelectionAllowed(false);
		fixedColumnTable.setRowHeight(132);
		fixedColumnTable.setAutoscrolls(false);
		fixedColumnTable.getTableHeader().setReorderingAllowed(false);
		fixedColumnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	
		fixedColumnTable.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
		collectionTable.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
		collectionTable.setDragEnabled(true);
 
		// Remove the first column from the main table
 
		TableColumnModel columnModel=collectionTable.getColumnModel();
		columnModel.removeColumn(columnModel.getColumn(0));

		// Remove the non-fixed columns from the fixed table
 
		while (fixedColumnTable.getColumnCount()>1) {
			columnModel=fixedColumnTable.getColumnModel();
			columnModel.removeColumn(columnModel.getColumn(1));
		}
 
		// Add the fixed table to the scroll pane
 
		fixedColumnTable.setPreferredScrollableViewportSize(fixedColumnTable.getPreferredSize());

		studyScrollPane=new JScrollPane(collectionTable,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		studyScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		studyScrollPane.getViewport().setBackground(Color.black);
		studyScrollPane.setRowHeaderView(fixedColumnTable);
		studyScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,fixedColumnTable.getTableHeader());
		studyScrollPane.setPreferredSize(new Dimension(425,525));

		// Use a containing JPanel so that the contents are centered in
		// the scrollPane. Grr...

		JPanel viewPanel=new JPanel(new GridBagLayout());
		viewPanel.add(psView,new GridBagConstraints());

		JScrollPane psScrollPane=new JScrollPane(viewPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		psScrollPane.getViewport().setBackground(Color.black);
		psScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		psScrollPane.setPreferredSize(new Dimension(525,525));

		JComboBox groupByComboBox=new JComboBox(new String[] {"Series","Study"});
		deleteListButton.setActionCommand("deleteList");
		deleteListButton.addActionListener(this);
		deleteListButton.setEnabled(false);
		deletePSButton.setActionCommand("deletePS");
		deletePSButton.addActionListener(this);
		deletePSButton.setEnabled(false);
		JLabel groupByLabel=new JLabel("Group collections by",JLabel.TRAILING);
		JLabel authorLabel=new JLabel("Author",JLabel.TRAILING);
		JLabel timestampLabel=new JLabel("Created on",JLabel.TRAILING);
		JLabel descriptionLabel=new JLabel("Description",JLabel.TRAILING);
		authorField.setEditable(false);
		timestampField.setEditable(false);

		CellConstraints cc=new CellConstraints();
		add(studyLabel,cc.xy(1,1));
		add(viewLabel,cc.xywh(5,1,9,1));
		add(separator1,cc.xywh(1,3,3,1));
		add(psPanelDescription,cc.xywh(1,5,3,1));
		add(separator2,cc.xywh(5,3,9,1));
		add(psViewDescription,cc.xywh(5,5,9,1));
		add(studyScrollPane,cc.xywh(1,7,3,1));
		add(groupByLabel,cc.xy(1,9));
		add(groupByComboBox,cc.xy(3,9));
		add(deleteListButton,cc.xy(1,11));
		add(deletePSButton,cc.xy(3,11));
		add(psScrollPane,cc.xywh(5,7,9,1));
		add(authorLabel,cc.xy(5,9));
		add(authorField,cc.xy(7,9));
		add(timestampLabel,cc.xy(9,9));
		add(timestampField,cc.xywh(11,9,3,1));
		add(descriptionLabel,cc.xy(5,11));
		add(descriptionField,cc.xywh(7,11,7,1));
		add(closeButton,cc.xy(13,13));

		setBackgroundPainter((Painter)UIManager.get("FloatingPanel.backgroundPainter"));
		setDropTarget(psView.getDropTarget());

		descriptionField.addCaretListener(this);
		groupByComboBox.addItemListener(this);
	}

	// =======================================================================
	// Attempt to update the current set of information from the (local)
	// archive's information on presentation states.  This action will
	// occur whenever we "switch" to a different patient/object.

	public void update(String assocKey) {

		if ((assocKey!=null)&&(currentAssocKey!=null)&&(assocKey.compareTo(currentAssocKey)==0)) return;
		psView.setPresentationState(null);
		psView.setSource(null);
		currentAssocKey=assocKey;
		List<Associable> ls=ImageViewerClientNode.getInstance().localFindAssociatedData(assocKey,AssocDataType.PRESENTATION_STATE.toString());

		// The list that is sent to us contains the presentation states;
		// what we need are the series that they are associated with.
		// From these series, we should then get the list of presentation
		// states; this will provide the ordering information that we
		// need.

		ArrayList<Series> psSeriesList=new ArrayList<Series>();
		try {
			for (Associable a : ls) {
				if (a instanceof AssociablePresentationState) {
					PresentationState ps=((AssociablePresentationState)a).getPresentationState(); 
					List<Series> series=ImageViewerClientNode.getInstance().localFindSeriesByID(ps.getReferencedSeriesKey());
					if ((series!=null)&&(!series.isEmpty())) {
						Series s=series.get(0);
						if (!psSeriesList.contains(s)) psSeriesList.add(s);
					}
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		clearFields();
		pctm.update(psSeriesList);
	}

	// =======================================================================

	public void setViewPanelAuthor(String author) {authorField.setText(author);}
	public void setViewPanelTimestamp(String timestamp) {timestampField.setText(timestamp);}
	public void setViewPanelTimestamp(String date, String time) {timestampField.setText(formatDate(date,time));}
	public void setViewPanelDescription(String description) {descriptionField.setText(description);}
	public void setViewPanelEditable(boolean editable) {descriptionField.setEditable(editable);}

	private void clearFields() {authorField.setText(""); timestampField.setText(""); descriptionField.setText("");}

	// =======================================================================
	/**
	 * Create the presentation for the given imagePanel.  The
	 * presentation state (PS) asks the underlying image to generate the
	 * information, and then creates the thumbnail that is displayed in
	 * this dialog panel.  When a new PS is created, it goes on the
	 * saveStack, and the dialog is updated to show this new PS object.
	 * If no current user is known, we send up a warning dialog to state
	 * that a guest author will be specified.
	 *
	 * @param ip a value of type 'ImagePanel'
	 */

	public void createNewPresentationState(ImagePanel ip) {

		String currentUser=UserManager.getCurrentUserName();
		if (currentUser==UserManager.NO_LOGIN) {
			currentUser="";
			Boolean warnNoLogin=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.WARN_NO_LOGIN);
			if ((warnNoLogin==null)||(warnNoLogin.booleanValue())) {
				int response=ApplicationPanel.getInstance().showDialog("No login to imageviewer has been supplied. As a result the author field for this presentation state "+
																															 "will not be filled in.",new JComponent[] {ASK_CB_AUTHOR},JOptionPane.INFORMATION_MESSAGE,JOptionPane.DEFAULT_OPTION);
				if (ASK_CB_AUTHOR.isSelected()) ApplicationContext.getContext().setProperty(ApplicationContext.WARN_NO_LOGIN,new Boolean(false));
			}
		}

		Image i=ip.getSource();
		PresentationState ps=i.generatePresentationState(ip,currentUser,"","");
		authorField.setText(ps.getPresentationCreatorName());
		timestampField.setText(formatDate(ps.getPresentationCreationDate(),ps.getPresentationCreationTime()));
		descriptionField.setText("");
		setViewPanelEditable(true);
		PSThumbnailPanel pstp=pctm.addPresentationState(ps,i);
		pstp.setSaved(false);
		SaveStack.getInstance().addSaveable((PSCollection)pstp.getParent());
		if (currentSelection!=null) currentSelection.setThumbnailSelected(false);
		currentSelection=pstp;
		pstp.setThumbnailSelected(true);
		psView.setSource(i);
		psView.setPresentationState(ps);
	}

	// =======================================================================

	public void closeAction() {MenuAction ma=MenuAction.ACTIONS.get(ApplicationContext.DISPLAY_PRESENTATION_STATES_COMMAND); ma.getMenuItem().doClick();}

	// =======================================================================

	public JXTable getCollectionTable() {return collectionTable;}
	public JScrollPane getThumbnailScrollPane() {return studyScrollPane;}
	public PSThumbnailPanel getCurrentSelection() {return currentSelection;}
	public PSViewPanel getViewPanel() {return psView;}

	public void setCurrentSelection(PSThumbnailPanel x) {currentSelection=x; deletePSButton.setEnabled((x==null) ? false : true); deleteListButton.setEnabled((x==null) ? false : true);}

	// =======================================================================

	private String formatDate(String date, String time) {try {return DATE_FORMAT_TRG.format(DATE_FORMAT_SRC.parse(date+" "+time));} catch (Exception exc) {return null;}}

	// =======================================================================

	public PSThumbnailPanel findThumbnail(Point p) {

		// Play some trickery to try and figure out which of the thumbnail
		// panels we are hitting within the table.  Basically, the x
		// coordinate of the mouse event will be correct, but the y will
		// be screwed up.  And because of all the issues with the JTable
		// and mouse events, we need to forward to the right one by
		// computing it explicitly.  Use the X value divided by the width
		// of thumbnail to get to the right component.

    int row=p.y/collectionTable.getRowHeight();
		if (row>=collectionTable.getRowCount() || row<0) return null;
		Object value=collectionTable.getValueAt(row,0);
		if (value instanceof PSCollection) {
			PSCollection psc=(PSCollection)value;
			if (p.x<(128*psc.getComponentCount())) {
				int thumbnailIndex=(int)Math.floor(p.x/128);
				return (PSThumbnailPanel)psc.getComponent(thumbnailIndex);
			}
		}
		return null;
	}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		if ("close".equals(e.getActionCommand())) {
			FloatingPanel fp=(FloatingPanel)SwingUtilities.getAncestorOfClass(FloatingPanel.class,this);
			if (fp!=null) fp.actionPerformed(new ActionEvent(this,1,"close"));
		} else if ("deletePS".equals(e.getActionCommand())) {
			if (currentSelection==null) return;
			Boolean warnDelete=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.WARN_PS_DELETE);
			if ((warnDelete==null)||(warnDelete.booleanValue())) {
				int response=ApplicationPanel.getInstance().showDialog("Are you sure you want to delete the currently selected presentation state? It will be removed this collection.",
																															 new JComponent[] {ASK_CB_DELETE},JOptionPane.QUESTION_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
				if (ASK_CB_DELETE.isSelected()) ApplicationContext.getContext().setProperty(ApplicationContext.WARN_PS_DELETE,new Boolean(false));
				if (response==JOptionPane.CANCEL_OPTION) return;
			}
			currentSelection.delete();
			psView.setSource(null);
			psView.setPresentationState(null);
			clearFields();
			setViewPanelEditable(false);
			setCurrentSelection(null);
		} else if ("deleteList".equals(e.getActionCommand())) {

		}
	}

	// =======================================================================

	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange()==ItemEvent.SELECTED) {
			String groupBy=(String)e.getItem();
			pctm.updateGrouping(("Study".equals(groupBy)) ? PSCollectionTableModel.GroupByType.STUDY : PSCollectionTableModel.GroupByType.SERIES);
		}
	}

	// =======================================================================

	public void caretUpdate(CaretEvent ce) {
		
		if ((currentSelection!=null)&&(descriptionField.isEditable())) {
			PresentationState ps=currentSelection.getPresentationState();
			ps.setPresentationDescription(descriptionField.getText());
		}
	} 

	// =======================================================================

	private class PSTableCellRenderer extends DefaultTableCellRenderer {

		FontMetrics fm=getFontMetrics(getFont());
		String ellipses=new String("...");

		private String createHTMLString(String[] description) {

			String htmlString=new String("<html><p align=\"right\">");
			for (int loop=0, n=description.length; loop<n; loop++) {
				String s=description[loop];
				if (fm.stringWidth(s)>120) {
					int textWidth=fm.stringWidth(ellipses);
					int i=0;
					for (int nChars=s.length()-1; i<nChars; i++) {
						textWidth+=fm.charWidth(s.charAt(i));
						if (textWidth>120) break;
					}
					s=s.substring(0,i+1)+ellipses;
				}
				htmlString+=(loop==0) ? s : ("<br>"+s);
			}
			return htmlString+="</p></html>";
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			
			if (value instanceof PSCollection) return (Component)value;
			JXPanel jxp=new JXPanel(new FlowLayout(FlowLayout.LEFT,2,5)); 
			Painter p=(Painter)(UIManager.get((isSelected) ? "Table.selectedRowHeaderPainter": "Table.rowHeaderPainter"));
			jxp.setBackgroundPainter(p);
			JLabel jl=new JLabel(createHTMLString((String[])value),JLabel.RIGHT);
			jl.setUI(new VerticalLabelUI(false));
			jl.setBorder(new EmptyBorder(0,0,0,6));
			jxp.add(jl);
			return jxp;
		}
	}	

	// =======================================================================
	// Class used to enable mouse events to pass through from the table
	// to the child component (otherwise JTable will absorb it all).
	// The isCellEditable() method is basically a hack so that we can
	// use single-clicks to select a thumbnail.  We have to explicitly
	// send the event for some reason...

	private class PSTableCellEditor extends AbstractCellEditor implements TableCellEditor {

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {return (value instanceof PSCollection) ? (Component)value : null;}
		public Object getCellEditorValue() {return null;}

		public boolean isCellEditable(EventObject evt) {

			if (evt instanceof MouseEvent) {
				MouseEvent me=(MouseEvent)evt;
				if (me.getClickCount()>0) {
					PSThumbnailPanel pstp=findThumbnail(me.getPoint());
					if (pstp!=null) pstp.mouseClicked(me);
				}
			}
			return true;
		}
	}
}

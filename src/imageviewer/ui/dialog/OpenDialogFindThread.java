/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.Cursor;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;

import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTreeTable;

import imageserver.model.ImageServerFindDescription;

import imageviewer.system.ImageViewerClientNode;
import imageviewer.ui.ApplicationPanel;

// =======================================================================
// This class should be updated to use the SwingWorker, not extend a
// Thread.  Will need to handle classpath differences for 1.5 vs. 1.6,
// though.

public class OpenDialogFindThread extends Thread {

	protected static final Cursor HOURGLASS_CURSOR=new Cursor(Cursor.WAIT_CURSOR);
	protected static final Cursor DEFAULT_CURSOR=new Cursor(Cursor.DEFAULT_CURSOR);

	// =======================================================================

	ImageServerFindDescription isfd=null;
	JXTreeTable treeTable=null;
	JButton searchButton=null, clearFieldButton=null;
	boolean isLocal=false;
	JLabel resultsLabel=null;

	public OpenDialogFindThread(ImageServerFindDescription isfd, JXTreeTable treeTable, JButton searchButton, JButton clearFieldButton, JLabel resultsLabel, boolean isLocal) {

		this.isfd=isfd;
		this.treeTable=treeTable;
		this.isLocal=isLocal;
		this.searchButton=searchButton;
		this.clearFieldButton=clearFieldButton;
		this.resultsLabel=resultsLabel;
	}

	// =======================================================================

	public void run() {

		StudyListTreeTableModel studyList=(StudyListTreeTableModel)treeTable.getTreeTableModel();
		treeTable.getParent().setCursor(HOURGLASS_CURSOR);
		treeTable.setVisible(false);
		String resultText="Results ";
		if (isLocal) {
			studyList.setRoot("Local archive",ImageViewerClientNode.getInstance().localFind(isfd));
			resultText+="from local archive: ";
		} else {
			ApplicationPanel.getInstance().addStatusMessage("Executing network archive search...");
			List l=ImageViewerClientNode.getInstance().sendFindRequest(isfd);
			ApplicationPanel.getInstance().addStatusMessage("Archive responded, receiving results...");
			studyList.setRoot("Network search",l);
			String name=isfd.getNodeDescription().getDisplayName();
			if ((name==null)||("null".equals(name))) name=isfd.getNodeDescription().getIPAddress();
			resultText+=("from "+name+": ");
		}
		resultText+=(studyList.getPatientCount()==0) ? "No matches" : (studyList.getPatientCount()+" patients with "+studyList.getStudyCount()+" studies");
		resultsLabel.setText(resultText);
		ApplicationPanel.getInstance().addStatusMessage("Search completed",5000);

		((AbstractTableModel)treeTable.getModel()).fireTableDataChanged();                 // This is a hack to trigger updates to the scrollpane...
		treeTable.getParent().setCursor(DEFAULT_CURSOR);
		treeTable.setEnabled(true);
		treeTable.repaint();
		treeTable.setVisible(true);
		searchButton.setEnabled(true);
		clearFieldButton.setEnabled(true);
	}
}


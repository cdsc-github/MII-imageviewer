/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.io.File;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

// =======================================================================
// Customized FileSystemModel with filtering; loosely based on the
// example FileSystemModel from prior JTreeTables and the one supplied
// in the SwingLabs source code.

public class FileSystemModel extends AbstractTreeTableModel {

	private static final Integer ZERO=new Integer(0);

	// =======================================================================

	public FileSystemModel() {this(new FileNode());}
	public FileSystemModel(FileNode root) {super(root);}

	// =======================================================================

	public Object getChild(Object parent, int index) {return ((FileNode)parent).getChildren().get(index);}
	public int getChildCount(Object parent) {List l=((FileNode)parent).getChildren();	return (l!=null) ? l.size() : 0;}
	public int getIndexOfChild(Object parent, Object child) {List l=((FileNode)parent).getChildren();	if (l==null) return -1;	for (int i=0, n=l.size(); i<n; i++) if (l.get(i)==child) return i; return 0;}	
	public FileNode getRoot() {return (FileNode)root;}
	public boolean isLeaf(Object node) {return (getChildCount(node)==0);}
	public void setRoot(FileNode fn) {root=fn; modelSupport.fireNewRoot();}

	// =======================================================================

	public int getColumnCount() {return 4;}

	public String getColumnName(int column) {

		switch(column) {
		  case 0: return "Name";
		  case 1: return "Size";
		  case 2: return "Type";
		  case 3: return "Date Modified";
		 default: return null;
		}
	}

	// =======================================================================

	public Object getValueAt(Object node, int column) {

		final File file=((FileNode)node).getFile();
		if (file==null) return null;
		try {
	    switch(column) {
			  case 0:	return FileSystemView.getFileSystemView().getSystemDisplayName(file);
	      case 1:	return (file.isFile()) ? new Integer((int)file.length()) : ZERO;
			  case 2:	return FileSystemView.getFileSystemView().getSystemTypeDescription(file);
	      case 3:	return new java.util.Date(file.lastModified());
	    }
		}	catch  (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}
}

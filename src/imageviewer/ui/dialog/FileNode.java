/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

// =======================================================================

public class FileNode extends DefaultMutableTreeTableNode {

	File file=null;
	ArrayList<FileNode> al=null;
	boolean	isDir=false, isRootNode=false;

	public FileNode() {isRootNode=true; isDir=true;}
	public FileNode(File file) {this.file=file; this.isDir=(file.isDirectory()||(FileSystemView.getFileSystemView().isDrive(file)));}

	// =======================================================================
	
	protected List getChildren() {
		
		if (isRootNode) return getDriveList();
		if (al==null) {
			try {
				final String[] files=file.list();
				if (files!=null) {
					al=new ArrayList<FileNode>();
					final String path=file.getPath();
					for (int i=0; i<files.length; i++) {
						final File childFile=new File(path,files[i]);
						if (!childFile.isHidden()) al.add(new FileNode(childFile));
						if (childFile.isDirectory()||(FileSystemView.getFileSystemView().isDrive(childFile)));
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return al;
	}

	// =======================================================================

	protected List getDriveList() {

		File[] rootFiles=File.listRoots();
		al=new ArrayList<FileNode>();
		for (int loop=0; loop<rootFiles.length; loop++) al.add(new FileNode(rootFiles[loop]));
		return al;
	}

	// =======================================================================
	
	public File getFile() {return file;}
	public boolean isLeaf() {return !isDir;}
	public String toString() {return (isRootNode) ? "Computer" : FileSystemView.getFileSystemView().getSystemDisplayName(file);}
}

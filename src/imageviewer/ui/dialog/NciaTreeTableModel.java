/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.net.URLDecoder;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import org.jdesktop.swingx.treetable.AbstractTreeTableModel;


//=======================================================================
/**
 *  Extends the AbstractTreeTableModel class to implement NCIA specific
 *  data model. Mostly non-functioning at this point. Left in as place holder 
 *  for possible future development
 *
 * @author Brian Burns, Jean Garcia, Agatha Lee, Jamal Madni
 * @version $Revision: 1.0 $ $Date: 2008/12/05 10:13:34 $
 */
public class NciaTreeTableModel extends AbstractTreeTableModel {

	String rootName=null;
	int columnCount=1;
	HashMap<Integer, String> columnNames = new HashMap<Integer, String>();
	HashMap<Integer, Object> columnTypes = new HashMap<Integer, Object>();
	
	// =======================================================================
	/**
	 * NciaTreeTableModel constructor.
	 * 
 	 */
	public NciaTreeTableModel() {super();}
	
	// =======================================================================
	/**
	 * NciaTreeTableModel constructor. Creates a new NciaTreeTableModel from the 
	 * specified root node
	 * 
	 * @param rootName - the new root node 
 	 */ 
	public NciaTreeTableModel(String rootName) {this.rootName=rootName;}
	
	// =======================================================================
	/**
	 * NciaTreeTableModel constructor. Creates a new NciaTreeTableModel from the 
	 * specified column names
	 * 
	 * @param columns - the new column names
 	 */ 
	public NciaTreeTableModel(String[] columns) {
		this();
		columnCount = columns.length + 1;
		columnNames.put(0, "PatientID");
		for(int i=1;i<columnCount;i++){
			columnNames.put(i, columns[i-1]);
		}
	}

	// =======================================================================
	/**
	 * Clears the tree table model nodes
	 * 
 	 */ 
	public void clear() {root=null; rootName=null;}

	// =======================================================================
	/**
	 * Returns the tree table model root node
	 * 
	 * @return Object - the root node 
 	 */ 
	public Object getRoot() {return root;}

	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public void setRoot(String rootName) {

	}
	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public Object getChild(Object parent, int index) {return null;}
	
	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public int getChildCount(Object parent) {return 0;}

	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public boolean isLeaf(Object node) {return (getChildCount(node)==0);}

	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public int getIndexOfChild(Object parent, Object child) {return 0;}
	
	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public int getColumnCount() {return columnCount;}
	
	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public String getColumnName(int column) {

		//get() checks hash table bounds automatically for us		
		return columnNames.get(column);
	}
	
	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public Object getValueAt(Object node, int column) {return null;}
	
	// =======================================================================
	/**
	 * Non-functional
	 * 
 	 */
	public void setValueAt(Object value, Object node, int column) {}

}




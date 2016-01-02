/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.util.List;
import java.net.URL;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

// =======================================================================
// Customized MircTreeTableModel; loosely based on the
// example FileSystemModel from prior JTreeTables and the one supplied
// in the SwingLabs source code.
//=======================================================================
/**
 * Creates the MIRC specific tree table model and implements the 
 * corresponding data manipulation methods required by the JXTreeTable class
 *
 * @author Brian Burns, Jean Garcia, Agatha Lee, Jamal Madni
 * @version $Revision: 1.0 $ $Date: 2008/12/05 10:13:34 $
 */
public class MircTreeTableModel extends AbstractTreeTableModel{
	public enum dataTypes {STRING, HTML, URL}; 
	public static final int NUM_COLUMNS=8;
	public static final String[] COLUMN_NAMES = {"Name", "URL","Author", "Abstract", "Level", "Access", "Peer Review", "Category"}; 
	public static final dataTypes[] COLUMN_TYPES = {dataTypes.STRING,dataTypes.URL,dataTypes.STRING,dataTypes.STRING,dataTypes.STRING,dataTypes.STRING,dataTypes.STRING,dataTypes.STRING};

	//=======================================================================
	/**
	 * MircTreeTableModel constructor. Creates new tree table model with a MircNode as root node
	 *
	 */
	public MircTreeTableModel() {this(new MircNode());}
	
	//=======================================================================
	/**
	 * MircTreeTableModel constructor. Creates new tree table model with the specified MircNode as the root node
	 *
	 *@param newRoot - the root MircNode
	 */
	public MircTreeTableModel(MircNode newRoot) {super(newRoot); ((MircNode)root).setRoot(true); ((MircNode)root).setValue(new String [] {"Root"});}

	//=======================================================================
	/**
	 * Returns a specific child node of a parent node, or null if child can't be found
	 *
	 * @param parent - the parent MircNode being queried 
	 * @param int - the index of the child MircNode to return
	 * @return MircNode - the child MircNode that corresponds to index, or null if not found
	 */
	public Object getChild(Object parent, int index) {
		List l=((MircNode)parent).getChildren(); 
		
		//if children do not exist or array out of bounds return null
		if(l == null){
			return null;
		}else if (l.size() < index){
			return null;
		}
		
		return ((MircNode)parent).getChildren().get(index);
	}
	
	//=======================================================================
	/**
	 * Returns the total number of children of a MircNode
	 *
	 * @param parent - the parent MircNode being queried 
	 * @return int - the number of child nodes
	 */
	public int getChildCount(Object parent) {
		List l=((MircNode)parent).getChildren();	
		return (l!=null) ? l.size() : 0;
	}
	
	//=======================================================================
	/**
	 * Returns the index of a child MircNode 
	 *
	 * @param parent - the parent MircNode being queried 
	 * @param child - the child MircNode whose index is needed
	 * @return int - the index of the child MircNode, or -1 if parent is invalid, or 0 if child can't be found
	 */
	public int getIndexOfChild(Object parent, Object child) {
		List l=((MircNode)parent).getChildren();	
		if (l==null) return -1;	
		for (int i=0, n=l.size(); i<n; i++) 
			if (l.get(i)==child) return i; 
		return 0;
	}	
	
	//=======================================================================
	/**
	 * Returns the root MircNode of the tree table model
	 *
	 * @return MircNode - the tree table root
	 */
	public MircNode getRoot() {return (MircNode)root;}
	
	//=======================================================================
	/**
	 * Returns a boolean that specifies whether a node is a leaf MircNode or not
	 *
	 * @param node - the MircNode whose leaf status is being queried
	 * @return boolean - True if node is a leaf, false otherwise
	 */
	public boolean isLeaf(MircNode node) {
		List l=((MircNode)node).getChildren(); 
		return (l==null) ? true : false;
	}
	
	//=======================================================================
	/**
	 * Sets a new root MircNode in the tree table model
	 *
	 * @param node - the new root node
	 */
	public void setRoot(MircNode node) {
		((MircNode)root).setRoot(false);
		
		root=node; 
		node.setRoot(true); 
		modelSupport.fireNewRoot();
	}

	//=======================================================================
	/**
	 * Returns the number of columns of data in the tree table model
	 *
	 * @return int - the number of columns of data
	 */
	public int getColumnCount() {return NUM_COLUMNS;}

	//=======================================================================
	/**
	 * Returns the display name of data column in the tree table model. Column must be 1 or COLUMN_NAMES.length() 
	 *
	 * @param column - the column whose name is being queried
	 * @return String - the column display name
	 */
	public String getColumnName(int column) {

		if(column >= NUM_COLUMNS || column < 0){
			return null;
		}else{
			return COLUMN_NAMES[column];
		}
	}
	
	//=======================================================================
	/**
	 * Sets all of the column values of a MircNode.
	 *
	 * @param node - the MircNode whose column data is being set
	 * @param columnValues - an array of Strings that specifies the column data values. Must be 1 or COLUMN_NAMES.length() in length.
	 * Column String values correspond to COLUMN_NAMES array. 
	 */
	public void setValue(Object node, String[] columnValues) {
		if (node!=null){
			//if columnValues is malformed, node value is set to null in setValue
			((MircNode)node).setValue(columnValues);
		}
	}
	
	//=======================================================================
	/**
	 * Returns the data within a column from a specific MircNode
	 *
	 * @param node - the MircNode whose column data is being queried
	 * @param column - the column index of the data
	 * @return Object - the data stored at index, column, in MircNode, node. Data type of returned data 
	 * corresponds to the COLUMN_TYPES array.
	 */
	public Object getValueAt(Object node, int column) {
		
		// if node or column index does not exist, return null
		if (node==null || getColumnName(column)==null || ((MircNode)node).isImage() == false) return null;
	    switch(COLUMN_TYPES[column]) {
			case STRING:	return ((MircNode)node).getValue()[column];		//return a string value
			case HTML:		return ((MircNode)node).getValue()[column]; 	//modify to handle HTML
			case URL:		try{ 	
								return new URL(((MircNode)node).getValue()[column]); //create URL from text String
							}catch (Exception e){
								return ((MircNode)node).getValue()[column];  // bad URL, return only string
							}
			default:
							return null;
	    }
	}
}

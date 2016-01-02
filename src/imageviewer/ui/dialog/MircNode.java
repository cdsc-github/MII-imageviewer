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
import java.util.HashMap;
import java.util.List;

import javax.swing.filechooser.FileSystemView;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

//=======================================================================
/**
 * A MircTreeTableModel node that contains the property information of either one database
 * or image node. All of the tree column data for a single node is stored in the columnValues array
 * and the ordering of values corresponds to the MircTreeTableModel.COLUMN_NAMES[] array. This class 
 * extends the DefaultMutableTreeTableNode class.
 *
 * @author Brian Burns, Jean Garcia, Agatha Lee, Jamal Madni
 * @version $Revision: 1.0 $ $Date: 2008/12/05 10:13:34 $
 */

public class MircNode extends DefaultMutableTreeTableNode {

	private String[] columnValues=null;
	private boolean isRootNode=false;
	private boolean isImage=true; 
	
	//the children variables are inherited from DefaultMutableTreeTableNode 
	//it holds an array of MircNodes subordinate to this one
	// =======================================================================
	/**
	 * Basic MircNode constructor.
	 *  
 	 */
	public MircNode() {super();}
	
	//do not copy isRootNode to new node, there can be only one if the root is used to construct a new node. 
	// =======================================================================
	/**
	  * MircNode constructor that creates a new node from another. Will not copy root node.
	 * 
	 * @param node - the node to copy 
 	 */
	public MircNode(MircNode node) {this(); children=node.children; isImage=node.isImage;} 
	
	// =======================================================================
	/**
	 * MircNode constructor that sets all of the column values of this node
	 * 
	 * @param newValue - a String[] array of new column values for this node. This array corresponds to
	 *  the MircTreeTableModel.COLUMN_NAMES[] array and contains all of the image properties.
 	 */
	public MircNode(String[] newValue){
		this();
		setValue(newValue);
	}
	
	// =======================================================================
	/**
	 * MircNode constructor that sets all of the column values of this node
	 * 
	 * @param newValue - a HashMap<String, String> of new column values for this node. This HashMap
	 * is keyed off of the MircTreeTableModel.COLUMN_NAMES[] array and contains all of the image properties.
 	 */
	public MircNode(HashMap<String, String> newValue){
		this();
		setValue(newValue);
	}

	// =======================================================================
	/**
	 * Returns a boolean that specifies whether or not this node is the MircTreeTable root
	 * 
	 * @return boolean - True if this is the root node, false otherwise
 	 */
	public boolean isRoot(){
		return isRootNode;
	}
	
	// =======================================================================
	/**
	 * Returns a boolean that specifies whether or not this node is an image node or a database node
	 * 
	 * @return boolean - True if this is an image node, false otherwise
 	 */
	public boolean isImage(){
		return isImage;
	}
	
	// =======================================================================
	/**
	 * Appends a new child to the list of children for this node. If the parent
	 * is the root node, isImage is set to false for the new child since it must 
	 * be a database name.
	 * 
	 * @param child - the new node to add as a child
 	 */
	public void addChild(MircNode child){
		if(children == null){
		//	(MircNode)children = new ArrayList<MircNode>();
		}
		child.parent = this;
		children.add(child);
		
		//children of the root are database names. Images are only children of database names. 
		if(isRootNode){
			child.isImage = false;
		}
	}
	
	// =======================================================================
	/**
	 * Returns the list of child nodes for this node
	 * 
	 * @return List - all of the child nodes of this node
 	 */
	public List getChildren() {
		return children;
	}
	
	// =======================================================================
	/**
	 * Sets all of the column values of this node
	 * 
	 * @param newValue - a String[] array of new column values for this node. This array corresponds to
	 *  the MircTreeTableModel.COLUMN_NAMES[] array and contains all of the image properties.
 	 */
	protected void setValue(String[] newValue){
		// database nodes have only their name in the first column, everything else is null. 
		// Column lengths can only be 1 or NUM_COLUMNS.
		if(newValue.length == 1){
			isImage=false;
		}else if(newValue.length != MircTreeTableModel.NUM_COLUMNS){
			columnValues = null;
		}
		columnValues=newValue;
	}
	
	// =======================================================================
	/**
	 * Sets all of the column values of this node
	 * 
	 * @param newValue - a HashMap<String, String> of new column values for this node. This HashMap
	 * is keyed off of the MircTreeTableModel.COLUMN_NAMES[] array and contains all of the image properties.
 	 */
	protected void setValue(HashMap<String, String> newValue){
		//this method should only be used to add images
		columnValues = new String[MircTreeTableModel.NUM_COLUMNS];
		for(int i=0; i < MircTreeTableModel.NUM_COLUMNS;i++){
			columnValues[i]=newValue.get(MircTreeTableModel.COLUMN_NAMES[i]);
		}
	}
	
	// =======================================================================
	/**
	 * Returns all image properties stored by this node
	 * 
	 * @return String[] - an array of string image properties that correspond to the table column values 
 	 */
	protected String[] getValue(){
		return columnValues;
	}

	// =======================================================================
	/**
	 * Sets this node to be/or not be the root node of the MircTreeTable
	 * 
	 * @param isRoot - a boolean that specifies whether this node is the root 
 	 */
	protected void setRoot(boolean isRoot){
		isRootNode = isRoot;
	}
	
	// =======================================================================
	/**
	 * Removes all child nodes from this node
	 * 
 	 */
	protected void clearChildren(){
		children.clear();
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import javax.swing.table.AbstractTableModel;

import imageviewer.model.Image;

// =======================================================================

public class ImageInfoTableModel extends AbstractTableModel {

	private static final String[] COLUMN_NAMES=new String[]{"Property","Description","Value"};

	// =======================================================================

	Image i=null;
	ArrayList<String> sortedKeys=new ArrayList();

	public ImageInfoTableModel() {}
	public ImageInfoTableModel(Image i) {setImage(i);}

	// =======================================================================

	public String getColumnName(int x) {return COLUMN_NAMES[x];}
	public boolean isCellEditable(int row, int col) {return false;}

	public void setValueAt(Object value, int row, int col) {}	

	// =======================================================================

	public Image getImage() {return i;}

	public void setImage(Image i) {

		this.i=i; 
		sortedKeys.clear(); 
		if (i!=null) {
			sortedKeys.addAll(i.getProperties().keySet()); 
			Collections.sort(sortedKeys);
		} 
		fireTableDataChanged();
	}

	// =======================================================================
	
	public int getColumnCount() {return COLUMN_NAMES.length;}
	public int getRowCount() {return (i!=null) ? i.getProperties().size() : 0;}
	
	public Object getValueAt(int row, int col) {
		
		switch (col) {
		  case 0: return sortedKeys.get(row);
		  case 1: return i.getPropertyDescription((String)sortedKeys.get(row));
		  case 2: Object o=i.getProperties().get(sortedKeys.get(row));
				      if (o instanceof double[]) {
								double[] dArray=(double[])o;
								String s=new String("[");
								for (int i=0; i<dArray.length; i++) s+=(dArray[i]+",");
								s=s.substring(0,s.length()-1);
								s+="]";
								return s;
							} else if (o instanceof int[]) {
								int[] iArray=(int[])o;
								String s=new String("[");
								for (int i=0; i<iArray.length; i++) s+=(iArray[i]+",");
								s=s.substring(0,s.length()-1);
								s+="]";
								return s;
							}
				      return o;
		}
		return null;
	}
}

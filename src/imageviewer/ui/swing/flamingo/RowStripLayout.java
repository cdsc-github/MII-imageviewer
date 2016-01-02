/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RowStripLayout implements LayoutManager {

	int hgap=2, targetNumberOfRows=2;

	public RowStripLayout() {}
	public RowStripLayout(int hgap, int targetNumberOfRows) {this.hgap=hgap; this.targetNumberOfRows=targetNumberOfRows;}

	// =======================================================================
	// Add information for grouping in the row layout

	public void addLayoutComponent(String name, Component comp) {}
	public void removeLayoutComponent(Component comp) {}

	// =======================================================================
	// Iterate through the children and split them across n rows. Add
	// each child successively to minimize the width of a given
	// row. This is not going to necessarily give an optimal solution...

	private ArrayList<ArrayList<Component>> heuristicPartition(Container parent, int n) {

		ArrayList<ArrayList<Component>> rows=new ArrayList<ArrayList<Component>>();
		for (int loop=0; loop<n; loop++) rows.add(new ArrayList<Component>());
		int[] rowWidths=new int[n];
		for (int i=0; i<n; i++) rowWidths[i]=0;
		Component[] children=parent.getComponents();
		Arrays.sort(children,new ComponentComparator());
		for (int loop=0; loop<children.length; loop++) {
			Dimension d=children[loop].getPreferredSize();
			int[] proposedRowWidths=new int[n];
			for (int i=0; i<n; i++) {
				proposedRowWidths[i]=rowWidths[i]+d.width;
			}
			int minimizedIndex=0;
			for (int i=1; i<n; i++) {
				if (proposedRowWidths[i]<proposedRowWidths[minimizedIndex]) minimizedIndex=i;
			}
			rowWidths[minimizedIndex]+=d.width;
			rows.get(minimizedIndex).add(children[loop]);
		}
		
		// Remove empty rows, reverse the list, and return
		
		ArrayList<ArrayList<Component>> finalRows=new ArrayList<ArrayList<Component>>();
		for (ArrayList<Component> row : rows) if (row.size()!=0) finalRows.add(row);
		Collections.reverse(finalRows);
		return finalRows; 
	}

	// =======================================================================

	public Dimension preferredLayoutSize(Container parent) {

		ArrayList<ArrayList<Component>> rows=heuristicPartition(parent,targetNumberOfRows);
		int maxWidth=0;
		for (ArrayList<Component> row : rows) {
			int rowWidth=0;
			for (Component c : row) rowWidth+=c.getPreferredSize().width+hgap;
			if (rowWidth>maxWidth) maxWidth=rowWidth;
		}
		return new Dimension((int)maxWidth,RibbonConstants.TASKBAR_HEIGHT+RibbonConstants.BAND_HEADER_HEIGHT+
												 (RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET));
	}

	// =======================================================================

	public Dimension minimumLayoutSize(Container parent) {return preferredLayoutSize(parent);}

	// =======================================================================

	public void layoutContainer(Container parent) {

		ArrayList<ArrayList<Component>> rows=heuristicPartition(parent,targetNumberOfRows);
		int numRows=rows.size();
		if (numRows==3) {
			int y=1;
			for (ArrayList<Component> row : rows) {
				int x=0;
				for (Component c : row) {
					Dimension d=c.getPreferredSize();
					c.setBounds(x,y,d.width,22);
					x+=d.width+hgap;
				}
				y+=23;
			}
		} else {
			int totalHeight=(RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET);
			int rowHeight=(int)Math.floor((double)(totalHeight+numRows-1)/(double)numRows);
			int rowOffset=(int)Math.floor(rowHeight-20)/2;
			if (rowOffset<0) rowOffset=0;
			int y=(numRows==1) ? ((totalHeight/2)-10) : ((numRows==3) ? 1 : rowOffset);
			for (ArrayList<Component> row : rows) {
				int x=0;
				for (Component c : row) {
					Dimension d=c.getPreferredSize();
					c.setBounds(x,y,d.width,22);
					x+=d.width+hgap;
				}
				y+=21+rowOffset;
			}
		}
	}

	// =======================================================================

	private class ComponentComparator implements Comparator<Component> {

		public int compare(Component c1, Component c2) {return c2.getPreferredSize().width-c1.getPreferredSize().width;}
		public boolean equals(Object o) {return false;}
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog.ps;

import java.awt.FontMetrics;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingworker.SwingWorker;

import imageserver.model.Associable;
import imageserver.model.AssociablePresentationState;
import imageserver.model.AssociatedData;
import imageserver.model.AssociatedData.AssocDataType;
import imageserver.model.Series;

import imageviewer.model.Image;
import imageviewer.model.PresentationState;
import imageviewer.system.Saveable;

// =======================================================================

public class PSCollectionTableModel extends AbstractTableModel implements Saveable {

	public static enum GroupByType {
		
		SERIES, STUDY, OTHER;
		
		public static GroupByType getGroupByType(String s) {
			GroupByType gbt=GroupByType.OTHER;
			if (s.equalsIgnoreCase("SERIES")) {
				gbt=GroupByType.SERIES;
			} else if (s.equalsIgnoreCase("STUDY")) {
				gbt=GroupByType.STUDY;
			} 
			return gbt;
		}
	}

	private static final String[] COLUMN_NAMES=new String[] {" "," "};
	private static final SimpleDateFormat DATE_FORMAT=new SimpleDateFormat("M/dd/yy h:mm a");

	// =======================================================================

	Hashtable<String,PSCollection> collectionListIndex=new Hashtable<String,PSCollection>();
	ArrayList<PSCollection> psCollectionList=new ArrayList<PSCollection>();
	ArrayList<PSThumbnailPanel> psList=new ArrayList<PSThumbnailPanel>();
	Comparator<PSCollection> rowComparator=new PSTableComparator();
	boolean sortAscending=true, saved=true;
	GroupByType groupBy=GroupByType.SERIES;

	public PSCollectionTableModel() {super();}

	// =======================================================================

	public void update(final ArrayList<Series> psSeriesList) {

		// Clear the existing information; anything that is saveable will
		// still be in the general memory, though.  Spawn generation off
		// to a swingWorker, rather than doing this in the immediate
		// thread.

		collectionListIndex.clear();
		psList.clear();
		for (PSCollection psc : psCollectionList) psc.flush();
		psCollectionList.clear();
		for (PSThumbnailPanel pstp : psList) pstp.flush();
		psList.clear();

		SwingWorker sw=new SwingWorker<Boolean,Void>() {
			public Boolean doInBackground() {
				for (Series s : psSeriesList) {
					List<AssociatedData> ls=s.getAssociatedData();

					// Sort the associated data based on any order indicies...

					Collections.sort(ls,new Comparator<AssociatedData>() {
						public int compare(AssociatedData a1, AssociatedData a2) {
							Integer orderIndex1=a1.getOrderIndex();
							Integer orderIndex2=a2.getOrderIndex();
							if ((orderIndex1==null)&&(orderIndex2==null)) return 0;
							if ((orderIndex1!=null)&&(orderIndex2==null)) return 1;
							if ((orderIndex1==null)&&(orderIndex2!=null)) return -1;
							return orderIndex1.intValue()-orderIndex2.intValue();
						}
						public boolean equals(Object o) {return false;}
					});

					for (AssociatedData a : ls) {

						// Retrieve only the presentation states; link the
						// thumbnails to the associatedData objects used to
						// generate the associablePresentationState.

						if (AssociatedData.AssocDataType.PRESENTATION_STATE.getTypeNumber().equals(a.getType())) {
							AssociablePresentationState aps=new AssociablePresentationState(a);
							addPresentationState(aps.getPresentationState()).setAssociatedData(a); 
						}
					}
				}
				return Boolean.TRUE;
			}
		};
		sw.execute();
	}

	// =======================================================================

	private String[] getGroupingKeys(PresentationState ps) {

		switch (groupBy) {
		  case SERIES: return new String[] {ps.getReferencedSeriesKey()};
		   case STUDY: return new String[] {ps.getReferencedStudyKey()};
		      default: return ps.getGroupingKeys(); 
		}
	}

	private PSCollection findPSCollection(PresentationState ps) {

		String[] groupingKeys=getGroupingKeys(ps);
		String psKey=new String();
		for (int loop=0, n=groupingKeys.length; loop<n; loop++) psKey+="_"+groupingKeys[loop];
		PSCollection psc=collectionListIndex.get(psKey);
		if (psc==null) {
			String[] sortingKeys=ps.getSortingKeys();
			String sortingKey=new String();
			for (int loop=0, n=sortingKeys.length; loop<n; loop++) sortingKey+="_"+sortingKeys[loop];
			switch (groupBy) {
			   case STUDY: {String[] collectionDescription=ps.getPSStudyDescription();
					            psc=new PSCollection(psKey,sortingKey,ps.getStudyTimestamp(),collectionDescription,groupBy);
				              break;}
			  case SERIES:
			      default: {String[] collectionDescription=ps.getPSSeriesDescription();
					            psc=new PSCollection(psKey,sortingKey,ps.getSeriesTimestamp(),collectionDescription,groupBy);}
			}
			collectionListIndex.put(psKey,psc);
			psCollectionList.add(psc);
			fireTableRowsInserted(psCollectionList.size()-1,psCollectionList.size()-1);
		}
		return psc;
	}

	// =======================================================================

	public PSThumbnailPanel addPresentationState(PresentationState ps) {PSThumbnailPanel pstp=findPSCollection(ps).addPresentationState(ps); fireTableDataChanged(); psList.add(pstp); return pstp;}
	public PSThumbnailPanel addPresentationState(PresentationState ps, Image i) {PSThumbnailPanel pstp=findPSCollection(ps).addPresentationState(ps,i,false); fireTableDataChanged(); psList.add(pstp); return pstp;}

	// =======================================================================
	// Change the grouping, for example, from series to study.  This
	// action requires that the table model be updated and the
	// underlying PSCollections regrouped.  Because we don't want to
	// waste the formation of the thumbnails we already, have, we go
	// through the current set of PSCollections and all those
	// thumbnails, and then create new collections based on the new
	// groupBy paraemeter.  Have to remember to fire an event for the
	// table model to update everything.

	public void updateGrouping(GroupByType groupBy) {

		this.groupBy=groupBy;
		ArrayList<PSCollection> originalCollection=(ArrayList<PSCollection>)psCollectionList.clone();
		collectionListIndex.clear();
		psCollectionList.clear();
		for (PSCollection psc : originalCollection) {
			ArrayList<PSThumbnailPanel> thumbnails=psc.getThumbnails();
			for (PSThumbnailPanel pstp : thumbnails) {
				PresentationState ps=pstp.getPresentationState();
				findPSCollection(ps).addPSThumbnailPanel(pstp);
			}
		}
		fireTableDataChanged(); 
	}

	// =======================================================================

	public String getColumnName(int x) {return COLUMN_NAMES[x];}
	public boolean isCellEditable(int row, int col) {return (col==1) ? true : false;}

	public void setValueAt(Object value, int row, int col) {}	

	public int getColumnCount() {return COLUMN_NAMES.length;}
	public int getRowCount() {return psCollectionList.size();}

	public Object getValueAt(int row, int col) {

		PSCollection psc=psCollectionList.get(row);	
		switch (col) {
		  case 0: return new String[] {psc.getDate(),psc.getDescription()[0],psc.getDescription()[1]};
		  case 1: return psc;
		}
		return null;
	}

	// =======================================================================

	public void toggleSort() {

		Collections.sort(psCollectionList,rowComparator); 
		if (sortAscending) {sortAscending=false; Collections.reverse(psCollectionList);} else sortAscending=true; 
		fireTableDataChanged();
	}

	// =======================================================================
	// Save the presentation state information, with additions,
	// deletions, and re-orderings accordingly.

	public boolean isSaved() {return saved;}
	public void setSaved(boolean x) {saved=x;}

	public boolean save() {

		try {
			
			// First, we need to see if any of the presentation states are
			// marked for deletion. We remove these from processing.


			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return false;
	}	

	// =======================================================================

	public class PSTableComparator implements Comparator<PSCollection> {

		public int compare(PSCollection psc1, PSCollection psc2) {

			try {
				Date d1=DATE_FORMAT.parse(psc1.getDate());
				Date d2=DATE_FORMAT.parse(psc2.getDate());
				int index=d1.compareTo(d2);
				if (index!=0) return index;
				String[] s1=psc1.getDescription();
				String[] s2=psc2.getDescription();
				index=s1[0].compareTo(s2[0]);
				if (index!=0) return index;
				return s1[1].compareTo(s2[1]);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			return 0;
		}

		public boolean equals(Object o) {return (o==this);}
	}
}

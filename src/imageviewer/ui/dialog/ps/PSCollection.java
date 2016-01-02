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
import java.awt.Container;
import java.awt.FlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import imageserver.model.AssociatedData;
import imageserver.model.AssociatedData.AssocDataType;
import imageserver.model.ImageInstance;
import imageserver.model.Series;

import imageviewer.model.Image;
import imageviewer.model.ImageReader;
import imageviewer.model.PresentationState;

import imageviewer.system.ImageReaderManager;
import imageviewer.system.ImageViewerClientNode;
import imageviewer.system.Saveable;
import imageviewer.system.SaveStack;

// =======================================================================

public class PSCollection extends JPanel implements Saveable {

	private static Logger LOG=Logger.getLogger("imageviewer.psCollection");

	// =======================================================================

	String[] description=null;
	String key=null, sortKey=null, date=null;
	ArrayList<PSThumbnailPanel> thumbnails=new ArrayList<PSThumbnailPanel>();
	boolean saved=true, reordered=false;
	PSCollectionTableModel.GroupByType groupBy=PSCollectionTableModel.GroupByType.SERIES;

	public PSCollection(String key, String sortKey, String date, String[] description, PSCollectionTableModel.GroupByType groupBy) {

		super(new FlowLayout(FlowLayout.LEFT,1,1));
		setBackground(Color.black);
		setOpaque(true);
		this.key=key;
		this.sortKey=sortKey;
		this.date=date;
		this.description=description;
		this.groupBy=groupBy;
	}

	// =======================================================================

	public String getKey() {return key;}
	public String getSortKey() {return sortKey;}
	public String getDate() {return date;}

	public String[] getDescription() {return description;}

	public boolean isSaved() {return saved;}
	public boolean isReordered() {return reordered;}

	public void setKey(String x) {key=x;}
	public void setSortKey(String x) {sortKey=x;}
	public void setDate(String x) {date=x;}
	public void setDescription(String[] x) {description=x;}
	public void setSaved(boolean x) {saved=x; if (!saved) SaveStack.getInstance().addSaveable(this);}
	public void setReordered(boolean x) {reordered=x;}

	public ArrayList<PSThumbnailPanel> getThumbnails() {return thumbnails;}

	// =======================================================================

	public void addPSThumbnailPanel(PSThumbnailPanel pstp) {

		thumbnails.add(pstp);	
		add(pstp);
		int w1=PSDialogPanel.getInstance().getCollectionTable().getColumnModel().getColumn(0).getWidth();
		int w2=(int)getPreferredSize().getWidth();
		if (w1<w2) PSDialogPanel.getInstance().getCollectionTable().getColumnModel().getColumn(0).setPreferredWidth(w2);
		revalidate();
		repaint();
	}

	// =======================================================================

	public PSThumbnailPanel addPresentationState(final PresentationState ps) {

		// Find the image that is referenced by this presentation state,
		// and load it.  Create a thumbnail for the image.

		final ImageInstance ii=ImageViewerClientNode.getInstance().localFindImageByID(ps.getReferencedImageKey());
		if (ii!=null) {
			ImageReader ir=ImageReaderManager.getInstance().getImageReader(ps.getImageType());
			ArrayList<? extends Image> al=ir.readFile(ii.getFilePath());
			return ((al!=null)&&(!al.isEmpty())) ? addPresentationState(ps,al.get(0),true) : null;
		}	else {
			LOG.debug("Unable to find image in local archive for presentation state.");
			LOG.debug(ps.getReferencedImageKey());
		}	
		return null;
	}

	public PSThumbnailPanel addPresentationState(PresentationState ps, Image i, boolean saveState) {

		PSViewPanel tmpPanel=new PSViewPanel();
		tmpPanel.setSize(128,128);
		tmpPanel.setSource(i);
		tmpPanel.setPresentationState(ps);
		PSThumbnailPanel thumbnail=new PSThumbnailPanel(tmpPanel);
		thumbnail.setSource(i);
		thumbnail.setPresentationState(ps);
		thumbnail.setSaved(saveState);
		addPSThumbnailPanel(thumbnail);
		tmpPanel.flush();
		tmpPanel=null;
		return thumbnail;
	}

	// =======================================================================
	// Tell all the children to flush themselves.

	public void flush() {for (PSThumbnailPanel pstp : thumbnails) pstp.flush(); thumbnails.clear();}

	// =======================================================================

	private class ZComponentComparator implements Comparator<Component> {

		Container c=null;

		public ZComponentComparator(Container c) {this.c=c;}

		public int compare(Component o1, Component o2) {
			return ((o1 instanceof Container)&&(o2 instanceof Container)) ? 
				((Container)o1).getComponentZOrder(c)-((Container)o2).getComponentZOrder(c) : 0;
		}

		public boolean equals(Object o) {return (o==this);}
	}

	// =======================================================================

	public void delete(PSThumbnailPanel pstp) {thumbnails.remove(pstp); remove(pstp);}

	// =======================================================================

	public boolean save() {

		// Make sure all the children have been saved.
		
		for (PSThumbnailPanel pstp : thumbnails) {
			if (!pstp.isSaved()) {
				boolean flag=pstp.save();
				if (!flag) return false;
			}
		}

		// If a collection was re-ordered, then we need to traverse the
		// list of child thumbnails and recreate the new sequence.
		// Because of some weird Hibernate issues, we need to clear the
		// previous list of these items and then add all the components,
		// rather than specifically setting a new list.

		if (reordered) {
			try {
				ArrayList<Component> tmpList=new ArrayList<Component>();
				Component[] child=getComponents();
				for (int loop=0, n=child.length; loop<n; loop++) tmpList.add(child[loop]);
				Collections.sort(tmpList,new ZComponentComparator(this));
				
				if (groupBy==PSCollectionTableModel.GroupByType.STUDY) {

					// We know that each of the objects in a study collection
					// must come from a series, ultimately.  So we need to go
					// through all the components, and see which series they
					// belong to, and then process each series separately.

					Hashtable<String,ArrayList<Component>> series=new Hashtable<String,ArrayList<Component>>();
					for (Component c : tmpList) {
						PresentationState ps=((PSThumbnailPanel)c).getPresentationState();
						String key=ps.getReferencedSeriesKey();
						ArrayList<Component> al=series.get(key);
						if (al==null) {al=new ArrayList<Component>(); series.put(key,al);}
						al.add(c);
					}

					// Now, traverse the hashtable. It won't matter which series
					// we do first...they're independent of each other.

					ImageViewerClientNode.getInstance().beginTransaction();
					for (Enumeration<String> e=series.keys(); e.hasMoreElements();) {
						ArrayList<Component> componentList=series.get(e.nextElement());
						ArrayList<AssociatedData> al=new ArrayList<AssociatedData>();
						for (Component c : componentList) al.add(((PSThumbnailPanel)c).getAssociatedData());
						String imageKey=((PSThumbnailPanel)componentList.get(0)).getPresentationState().getReferencedImageKey();
						Series s=ImageViewerClientNode.getInstance().getArchive().findInstance(imageKey).getSeries();
						for (AssociatedData ad : s.getAssociatedData()) {
							if (ad.getType()==0) {
								int orderIndex=al.indexOf(ad);
								if (orderIndex>=0) ad.setOrderIndex(1+orderIndex);
							}
						}
					}
					ImageViewerClientNode.getInstance().flushTransactions();
					ImageViewerClientNode.getInstance().commitTransaction();
					
				} else {

					ArrayList<AssociatedData> al=new ArrayList<AssociatedData>();
					for (Component c : tmpList) al.add(((PSThumbnailPanel)c).getAssociatedData());
					PSThumbnailPanel pstp=thumbnails.get(0);
					
					// We have to do some maneuvering here...the list we get
					// back from a series will contain *all* associated data,
					// not just the presentation states.  Set the order index
					// according the the zOrdering computed above.

					ImageViewerClientNode.getInstance().beginTransaction();
					Series s=ImageViewerClientNode.getInstance().getArchive().findInstance(pstp.getPresentationState().getReferencedImageKey()).getSeries();
					for (AssociatedData ad : s.getAssociatedData()) {
						if (ad.getType()==0) {
							int orderIndex=al.indexOf(ad);
							if (orderIndex>=0) ad.setOrderIndex(1+orderIndex);
						}
					}
					ImageViewerClientNode.getInstance().flushTransactions();
					ImageViewerClientNode.getInstance().commitTransaction();
					reordered=false;
				}
			} catch (Exception exc) {
				exc.printStackTrace();
				return false;
			}
		}
		saved=true;
		return true;
	}
}

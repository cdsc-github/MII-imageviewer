/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.net.URLDecoder;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import imageserver.client.ImageStatus;
import imageserver.client.MoveStatus;
import imageserver.client.Ticket;
import imageserver.client.node.DownloadInfo;

import imageserver.model.ImageServerFindDescription.Scope;
import imageserver.model.ImageServerMoveDescription;
import imageserver.model.ImageServerMoveDescription.Compression;
import imageserver.model.ImageServerNodeDescription;
import imageserver.model.Series;
import imageserver.model.Study;

import imageviewer.system.ImageViewerClientNode;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.ApplicationPanel;

import imageviewer.util.StringUtilities;

// =======================================================================

public class OpenDialogArchiveMoveThread extends Thread {

	private static final String UNAVAILABLE_NO_DESCRIPTION=new String("(No description available)");

	private static final int POLLING_TIME_MS=2500;
	private static final int MAX_POLLS=960;

	// =======================================================================

	ImageServerNodeDescription remoteNode=null;
	boolean applyLayoutRules=false, transferOnly=false;
	TreeSet seriesList=null;
	TreeSet<Study> studyList=null;
	Hashtable<String,TrackingItem> trackTable=new Hashtable<String,TrackingItem>();

	public OpenDialogArchiveMoveThread(ImageServerNodeDescription remoteNode, TreeSet seriesList, boolean applyLayoutRules, boolean transferOnly) {

		this.remoteNode=remoteNode;
		this.seriesList=seriesList;
		this.applyLayoutRules=applyLayoutRules;
		this.transferOnly=transferOnly;
	}

	// =======================================================================

	private boolean executeMove(ImageViewerClientNode ivcn, ImageServerMoveDescription ismd) {

		// Get a ticket for the execution of the move request through the
		// client node.  We poll every few seconds to see if the move 
		// status is done.  If it's done, then we have to search the local
		// archive to get the stuff (and it's location) and then we open
		// it.
		
		ApplicationPanel.getInstance().addStatusMessage("Initiating move request...");
		try {
			Ticket t=ivcn.executeMoveRequest(ismd);
			if ((t!=null)&&(!t.getTicketNumber().equals("null"))) {
				MoveStatus status=ivcn.getStatusUpdate(t);

				// Grab the statuses from the move and update the tracker for Greg.

				ImageStatus is=status.getActiveMoveStatus();
				if (is==null) {
					List<ImageStatus> lis=status.getImageStatuses();
					is=lis.get(lis.size()-1);
				}
				long startTime=System.currentTimeMillis();

				String uid=is.getImage();
				TrackingItem ti=trackTable.get(uid);
				if (ti!=null) {
					ti.setStartTime(startTime);
					ti.setFinalTargetNode(ImageViewerClientNode.getInstance().getNodeDisplayName(is.getEndNode()));
					OpenDialog.getInstance().getOpenTrackingPanel().updated(ti);
				}
			
				ApplicationPanel.getInstance().addStatusMessage("Move request in progress...");
				int counter=0;
				while (!status.isDone()&&(counter<MAX_POLLS)) {

					is=status.getActiveMoveStatus();
					if (is==null) {
						List<ImageStatus> lis=status.getImageStatuses();
						is=lis.get(lis.size()-1);
					}
					if (ti!=null) {
						ti.setStatus(is.getStatus());
						ti.setMessage(is.getErrorMsg());
						ti.setRetries(is.getTryNo());
						ti.setTrackingLocation(ImageViewerClientNode.getInstance().getNodeDisplayName(is.getSource())+" >>> "+
																	 ImageViewerClientNode.getInstance().getNodeDisplayName(is.getTarget()));
						ti.setElapsedTrackingTime(is.getTimestamp());
						DownloadInfo di=ImageViewerClientNode.getInstance().getDownloadInfo(uid);
						if (di!=null) {
							ti.setPercentComplete(di.getDownloadPercentage());
						}
						OpenDialog.getInstance().getOpenTrackingPanel().updated(ti);
					}
					//					 }
					try {Thread.sleep(POLLING_TIME_MS);} catch (Exception exc) {exc.printStackTrace();}
					counter++; 
					try {
						status=ivcn.getStatusUpdate(t);
					} catch (RuntimeException rexc) {}
				}
				return ((counter!=MAX_POLLS)&&(status.isDone())); //MoveStatus.SUCCESS.equals(status.getStatus())));
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return false;
	}

	// =======================================================================

	public void run() {

		// Create the corresponding objects to fill out in the tracker model...

		for (Iterator i=seriesList.iterator(); i.hasNext();) {
			try {
				String name=remoteNode.getDisplayName();
				if ((name==null)||("null".equals(name))) name=remoteNode.getIPAddress();
				Object o=i.next();
				if (o instanceof Series) {
					Series s=(Series)o;
					String description=(s.getDescription()!=null) ? StringUtilities.replaceAndCapitalize(URLDecoder.decode(s.getDescription(),"UTF-8"),true) : UNAVAILABLE_NO_DESCRIPTION;
					TrackingItem ti=new TrackingItem(System.currentTimeMillis(),StringUtilities.formatPatientID(s.getStudy().getPatient().getPatID()),
																					 s.getModality(),description,name,s.getSeriesIUID());
					trackTable.put(s.getSeriesIUID(),ti);
					OpenDialog.getInstance().getOpenTrackingPanel().addItem(ti);
				} else {
					Study s=(Study)o;
					String description=(s.getDescription()!=null) ? StringUtilities.replaceAndCapitalize(URLDecoder.decode(s.getDescription(),"UTF-8"),true) : UNAVAILABLE_NO_DESCRIPTION;
					TrackingItem ti=new TrackingItem(System.currentTimeMillis(),StringUtilities.formatPatientID(s.getPatient().getPatID()),"",description,name,s.getStudyUID());
					trackTable.put(s.getStudyUID(),ti);
					OpenDialog.getInstance().getOpenTrackingPanel().addItem(ti);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}

		// First, create the move description based on the selected remote
		// node, the local node, and the study information.  Extract out
		// the study/series UIDs that we're going to request.

		ImageViewerClientNode ivcn=ImageViewerClientNode.getInstance();
		ImageServerNodeDescription localNode=ivcn.getLocalNodeDescription();
		Boolean b=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.USE_IMAGE_COMPRESSION);

		if (!applyLayoutRules) {
			
			for (Iterator i=seriesList.iterator(); i.hasNext();) {

				ImageServerMoveDescription ismd=new ImageServerMoveDescription(remoteNode.getName());
				if ((b!=null)&&(b.booleanValue())) ismd.setCompression(Compression.ZIP);
				ismd.addTargetNode(localNode.getName());

				Object o=i.next();
				TrackingItem ti=null;
				if (o instanceof Series) {
					Series s=(Series)o;
					if (s!=null) ismd.addTargetImage(s.getSeriesIUID(),Scope.SERIES.toString());
					ti=trackTable.get(s.getSeriesIUID());
				} else {
					Study s=(Study)o;
					if (s!=null) ismd.addTargetImage(s.getStudyUID(),Scope.STUDY.toString());
					Map<String,String> params=s.getParameters();
					Set<String> paramKeys=(Set<String>)params.keySet();
					for (String key : paramKeys) {
						String value=(String)params.get(key);
						if (value!=null) ismd.addParameter(key,value);
					}
					ti=trackTable.get(s.getStudyUID());
				}

				if (executeMove(ivcn,ismd)) {
					ti.setStatus(MoveStatus.SUCCESS);
					ti.setPercentComplete(100);
					if (!transferOnly) {
						TreeSet<Series> seriesSet=new TreeSet<Series>();
						ApplicationPanel.getInstance().addStatusMessage("Requested move completed, opening...",5000);
						TreeSet<Series> ts=new TreeSet<Series>();
						if (o instanceof Series) {
							Series s=(Series)o;
							List<Series> ls=ivcn.localFindSeriesByID(s.getSeriesIUID());
							if ((ls!=null)&&(!ls.isEmpty())) seriesSet.addAll(ls);
						} else {
							Study s=ivcn.localFindStudyByID(((Study)o).getStudyUID());
							if (s!=null) {
								try {
									if ((((Study)s).getId()!=null)&&(((Study)s).getId()!=-1)) {
										ImageViewerClientNode.getInstance().beginTransaction();
										ImageViewerClientNode.getInstance().load(s,s.getId());
									}
									s.load();
									seriesSet.addAll(s.getSeries());
								} catch (Exception exc) {
									exc.printStackTrace();
								} finally {
									ImageViewerClientNode.getInstance().rollbackTransaction();
								}
							}
						}
						OpenDialogArchiveThread odat=new OpenDialogArchiveThread(seriesSet,applyLayoutRules);
						odat.start();
					}
				} else {
					ti.setStatus(MoveStatus.ERROR);
					ti.setPercentComplete(0);
					ApplicationPanel.getInstance().addStatusMessage("A move failed to complete in a timely fashion. Check log.",5000);
				}
				ti.setFinalTime(System.currentTimeMillis());
			}

		} else {
			ImageServerMoveDescription ismd=new ImageServerMoveDescription(remoteNode.getName());
			ismd.addTargetNode(localNode.getName());
			if ((b!=null)&&(b.booleanValue())) ismd.setCompression(Compression.ZIP);
			for (Iterator i=seriesList.iterator(); i.hasNext();) {
				Series s=(Series)i.next();
				if (s!=null) ismd.addTargetImage(s.getSeriesIUID(),Scope.SERIES.toString());
			}
			if (executeMove(ivcn,ismd)) {
				if (!transferOnly) {
					ApplicationPanel.getInstance().addStatusMessage("Requested move completed, opening images...",5000);
					TreeSet<Series> seriesSet=new TreeSet<Series>();
					for (Iterator i=seriesList.iterator(); i.hasNext();) {
						Series s=(Series)i.next();
						List<Series> ls=ivcn.localFindSeriesByID(s.getSeriesIUID());
						if ((ls!=null)&&(!ls.isEmpty())) seriesSet.addAll(ls);
					}
					OpenDialogArchiveThread odat=new OpenDialogArchiveThread(seriesSet,applyLayoutRules);
					odat.start();
				}
			} else {
				ApplicationPanel.getInstance().addStatusMessage("A move failed to complete in a timely fashion. Check log.",5000);
			}
		}
		
		// Remove the tracking items...

		if (OpenDialog.getInstance().getOpenTrackingPanel().shouldRemoveOnComplete()) {
			for (Enumeration e=trackTable.keys(); e.hasMoreElements();) {
				String s=(String)e.nextElement();
				OpenDialog.getInstance().getOpenTrackingPanel().removeItem((TrackingItem)trackTable.get(s));			
			}
		}
		trackTable.clear();
	}
}


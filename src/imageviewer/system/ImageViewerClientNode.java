/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.system;

import java.io.File;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import imageserver.archive.ImageServerArchive;

import imageserver.client.BaseClient;
import imageserver.client.Ticket;
import imageserver.client.node.ClientNode;
import imageserver.client.node.DownloadInfo;

import imageserver.model.Associable;
import imageserver.model.ImageInstance;
import imageserver.model.ImageServerFindDescription;
import imageserver.model.ImageServerFindDescription.Scope;
import imageserver.model.AssociatedData;
import imageserver.model.ImageServerDataServiceDescription;
import imageserver.model.ImageServerDataQueryDescription;
import imageserver.model.ImageServerMoveDescription;
import imageserver.model.ImageServerNodeDescription;
import imageserver.model.Patient;
import imageserver.model.Study;
import imageserver.model.Series;

import imageviewer.model.PresentationState;

import utility.java.DateHelper;

// =======================================================================

public class ImageViewerClientNode extends BaseClient {

	private static Logger LOG=Logger.getLogger("imageviewer.client.node");
	private static ImageViewerClientNode IVCN=null;

	public static ImageViewerClientNode getInstance() {return IVCN;}
	public static ImageViewerClientNode getInstance(String gatewayConfigFile, String localNodeConfig, boolean useNetwork) {

		if (IVCN==null) IVCN=new ImageViewerClientNode(gatewayConfigFile,localNodeConfig,useNetwork); 
		return IVCN;
	}

	// =======================================================================

	ClientNode cn=null;
	boolean localArchiveStatus=false, connectionStatus=false;

	private ImageViewerClientNode(final String gatewayConfigFile, final String localNodeConfig, final boolean useNetwork) {

		super();
		try {
			cn=ClientNode.loadFromFile(new File(localNodeConfig));
			if (useNetwork) {
				super.initialize(gatewayConfigFile);
				LOG.info("Joining imageserver network...");
				cn.startSocketServer();
				connectionStatus=isGatewayConnected();				
			}
			LOG.info("Starting imageServer local archive...");
			localArchiveStatus=cn.startArchive();
			if (localArchiveStatus) {
				beginTransaction();
				Object[] stats=cn.getArchive().getStatistics();
				LOG.info("Local archive statistics: "+stats[0]+"="+stats[1]+", "+stats[2]+"="+stats[3]);
				rollbackTransaction();
			}
			if ((localArchiveStatus)&&(connectionStatus)) {
				LOG.info("Connections to archive and imageserver initialized.");
			} else if (localArchiveStatus) {
				LOG.warn("Unable to connect to imageserver gateway; only local archive available.");
			} else if (connectionStatus) {
				LOG.warn("Unable to establish local archive; only network resources available.");
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	// =======================================================================

	public boolean hasLocalArchive() {return localArchiveStatus;}
	public boolean isConnected() {return connectionStatus;}
	public boolean connect() {connectionStatus=isGatewayConnected(); return connectionStatus;}

	// =======================================================================

	public void beginTransaction() {cn.getArchive().beginTransaction();}
	public void commitTransaction() {cn.getArchive().commitTransaction();}
	public void rollbackTransaction() {cn.getArchive().rollbackTransaction();}

	public void flushTransactions() throws Exception {if (cn!=null) cn.getArchive().getSessionFactory().getCurrentSession().flush();}
	public void clearSession() throws Exception {if (cn!=null) cn.getArchive().getSessionFactory().getCurrentSession().clear();}

	public void delete(Object o) throws Exception {if (cn!=null) cn.getArchive().getSessionFactory().getCurrentSession().delete(o);}
	public void refresh(Object o) throws Exception {if (cn!=null) cn.getArchive().getSessionFactory().getCurrentSession().refresh(o);}
	public void update(Object o) throws Exception {if (cn!=null) cn.getArchive().getSessionFactory().getCurrentSession().saveOrUpdate(o);}

	// =======================================================================

	public void shutdown() throws Exception {

		if (cn!=null) {
			if (cn.getArchive().getSessionFactory().getCurrentSession().getTransaction().isActive()) {
				try {
					LOG.debug("Flushing an active transaction in the archive...");
					cn.getArchive().getSessionFactory().getCurrentSession().flush();
					cn.getArchive().commitTransaction();
				}	catch (Exception exc) {
					LOG.debug("Commit transaction failed, rolling back archive for safety...");
					cn.getArchive().rollbackTransaction();
				}
			}
			cn.getArchive().getSessionFactory().getCurrentSession().close();
		}
	}

	// =======================================================================

	public void load(Object o, Serializable id) {if (id!=null) cn.getArchive().load(o,id);}

	// =======================================================================

	public ImageServerArchive getArchive() {return cn.getArchive();}
	public ImageServerNodeDescription getLocalNodeDescription() {return getLocalPACSNode();}

	// =======================================================================

	public DownloadInfo getDownloadInfo(String uid) {return cn.getDownloadInfo(uid);}

	// =======================================================================
	// Basic methods for querying against the local archive

	public List<Study> localFindAll() {return localFind(new ImageServerFindDescription(""));}

	public List<Study> localFind(ImageServerFindDescription isfd) {

		// Create an empty study object, and copy the values needed from
		// the find description over into the object; tell the local
		// archive to find such studies and return the list accordingly.

		if (!localArchiveStatus) return null;
		Study s=new Study();
		if ((isfd.getPatientID()!=null)&&(!isfd.getPatientID().equals(""))) {
			Patient p=new Patient();
			p.setPatID(isfd.getPatientID());
			s.setPatient(p);
		}		
		if ((isfd.getModality()!=null)&&(!isfd.getModality().equals(""))) s.setModsInStudy(isfd.getModality());
		if (!isfd.getRequestedUIDs().isEmpty()) s.setStudyUID(isfd.getRequestedUIDs().get(0));
		if ((isfd.getStartDate()!=null)&&(isfd.getStartDate().equals(""))) isfd.setStartDate(null);
		if ((isfd.getEndDate()!=null)&&(isfd.getEndDate().equals(""))) isfd.setEndDate(null);
		cn.getArchive().beginTransaction();
		Date d1=isfd.getStartDate();
		Date d2=isfd.getEndDate();
		List<Study> studies=cn.getArchive().findStudy(s,((d1==null) ? null : DateHelper.format(d1,0)),((d2==null) ? null : DateHelper.format(d2,0)),isfd.getResultScope());
		cn.getArchive().rollbackTransaction();
		return studies;
	}

	public ImageInstance localFindImageByID(String id) {

		if (!localArchiveStatus) return null;
		cn.getArchive().beginTransaction();
		ImageInstance ii=cn.getArchive().findInstance(id);
		cn.getArchive().rollbackTransaction();
		return ii;
	}

	public List<Series> localFindSeriesByID(String id) {

		if (!localArchiveStatus) return null;
		cn.getArchive().beginTransaction();
		List<Series> ls=cn.getArchive().findSeries(id,null,null);
		for (Series s : ls)
			for (AssociatedData d : s.getAssociatedData())
				d.load();
		cn.getArchive().rollbackTransaction();
		return ls;
	}

	public Study localFindStudyByID(String id) {

		if (!localArchiveStatus) return null;
		cn.getArchive().beginTransaction();
		Study s=cn.getArchive().findStudy(id);
		cn.getArchive().rollbackTransaction();
		return s;
	}

	public List<Associable> localFindAssociatedData(String assocKey, String type) {

		if (!localArchiveStatus) return null;
		cn.getArchive().beginTransaction();
		List<Associable> ls=cn.getArchive().getAssociatedDataForPatient(assocKey,type);
		cn.getArchive().rollbackTransaction();
		return ls;
	}

	// =======================================================================

	public Ticket executeMoveRequest(ImageServerMoveDescription ismd) {return sendMoveRequest(ismd);}

	// =======================================================================
	// Return a list of available nodes for retrieval.

	public List<ImageServerNodeDescription> getQueryableNodes() {

		if (!connectionStatus) return null;
		try {
			List<ImageServerNodeDescription> l=retrieveAvailableNodes();
			ArrayList<ImageServerNodeDescription> queryableList=new ArrayList<ImageServerNodeDescription>();
			for (int loop=0, n=l.size(); loop<n; loop++) {
				ImageServerNodeDescription isnd=l.get(loop);
				List capabilityList=isnd.getCapability(ImageServerNodeDescription.IMAGE_FIND);
				if ((capabilityList!=null)&&(!capabilityList.isEmpty())) queryableList.add(isnd);
			}
			return queryableList;
		} catch (Exception exc) {}
		return null;
	}

	// =======================================================================
	// Check and see if the service is available; if so, then execute a query.

	public String executeDataServiceQuery(ImageServerDataQueryDescription isdqd) {

		List<ImageServerDataServiceDescription> services=retrieveAvailableServices();
		String site=isdqd.getSite();
		for (ImageServerDataServiceDescription isdsd : services) {
			if (site.compareTo(isdsd.getName())==0) {
				return sendDataQueryRequest(isdqd);
			}
		}
		return null;
	}

	// =======================================================================
	// Utility method to return the display name for a given node

	public String getNodeDisplayName(String name) {

		if (!connectionStatus) return null;
		try {
			List<ImageServerNodeDescription> l=retrieveAvailableNodes();
			for (ImageServerNodeDescription isnd : l) if (name.compareToIgnoreCase(isnd.getName())==0) return isnd.getDisplayName();
		} catch (Exception exc) {}
		return null;
	}
}

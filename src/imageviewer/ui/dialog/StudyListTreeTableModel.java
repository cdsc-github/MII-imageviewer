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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import imageserver.model.ImageInstance;
import imageserver.model.Study;
import imageserver.model.Series;
import imageserver.model.Patient;

import imageviewer.system.ImageViewerClientNode;
import imageviewer.util.StringUtilities;

// =======================================================================

public class StudyListTreeTableModel extends AbstractTreeTableModel {

	private static final String DEFAULT_UNKNOWN_ID=new String("Unknown");
	private static final String UNAVAILABLE_NO_DESCRIPTION=new String("(No description available)");
	private static final Patient DEFAULT_UNKNOWN_PATIENT=new Patient();
	private static final Comparator DEFAULT_PATIENT_COMPARATOR=new PatientComparator();

	private static final SimpleDateFormat DATE_FORMAT=new SimpleDateFormat("M/dd/yyyy h:mm a");

	// =======================================================================

	TreeMap<Patient,TreeSet> patientMap=new TreeMap<Patient,TreeSet>(DEFAULT_PATIENT_COMPARATOR);
	int patientCount=0, studyCount=0;
	String rootName=null;

	public StudyListTreeTableModel() {super();}
	public StudyListTreeTableModel(String rootName, List studies) {super(studies); this.rootName=rootName; map(studies);}

	// =======================================================================

	public void clear() {root=null; rootName=null; patientCount=0; studyCount=0; patientMap.clear();}

	public Object getRoot() {return root;}

	public void setRoot(String rootName, List studies) {

		root=studies; 
		this.rootName=rootName; 
		map(studies);
		modelSupport.fireNewRoot();
	}

	// =======================================================================

	public int getPatientCount() {return patientCount;}
	public int getStudyCount() {return studyCount;}

	// =======================================================================
	// Convert the given data structure into an internal representation
	// that can be readily traversed.  The problem is that we need to
	// re-organize the data as patient -> study -> series, and we're
	// only given the middle point.  So traverse the list of studies, and
	// fill out the patientMap.

	private void map(List studies) {
		
		for (Iterator i=studies.iterator(); i.hasNext();) {
			Study s=(Study)i.next();
			studyCount++;
			try {
				if ((((Study)s).getId()!=null)&&(((Study)s).getId()!=-1)) {
					ImageViewerClientNode.getInstance().beginTransaction();
					ImageViewerClientNode.getInstance().load(s,((Study)s).getId());
				}
				Patient p=s.getPatient();				
				if (p!=null) {
					p.load();
					TreeSet patientStudies=patientMap.get(p);
					if (patientStudies==null) {
						patientStudies=new TreeSet();
						patientMap.put(p,patientStudies);
						patientCount++;
					} 
					patientStudies.add(s);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			} finally {
				if ((((Study)s).getId()!=null)&&(((Study)s).getId()!=-1)) {
					ImageViewerClientNode.getInstance().rollbackTransaction();
				}
			}
		}
	}

	// =======================================================================
  // TreeModel interface methods...

	public Object getChild(Object parent, int index) {

		if (parent instanceof List) {
			Set s=patientMap.keySet();
			Iterator i=s.iterator();
			for (int counter=0; counter<index; counter++) i.next();
			return i.next();
		} else if (parent instanceof Patient) {
			Patient p=(Patient)parent;
			TreeSet ts=patientMap.get(p);
			if ((ts!=null)&&(index<ts.size())) {
				Iterator i=ts.iterator();
				for (int counter=0; counter<index; counter++) i.next();
				return i.next();
			} else {
				return null;
			}
		} else if (parent instanceof Study) {
			try {
				Study s=(Study)parent;
				if ((((Study)s).getId()!=null)&&(((Study)s).getId()!=-1)) {
					ImageViewerClientNode.getInstance().beginTransaction();
					ImageViewerClientNode.getInstance().load(parent,s.getId());
				}
				s.load();
				Set<Series> series=s.getSeries();
				if (index<series.size()) {
					ArrayList<Series> al=new ArrayList<Series>();
					al.addAll(series);
					Collections.sort(al);
					return (al.get(index));
				} else {
					return null;
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			} finally {
				ImageViewerClientNode.getInstance().rollbackTransaction();
			}
		}
		return null;
	}
	
	public int getChildCount(Object parent) {

		if (parent instanceof List) return patientMap.size();
		if (parent instanceof Patient) {
			Patient p=(Patient)parent;
			TreeSet ts=patientMap.get(p);
			return (ts!=null) ? ts.size() : 0;
		}
		if (parent instanceof Study) {
			int i=((Study)parent).getNumberOfSeries().intValue();
			if (i<=0){
				Set<Series> series=((Study)parent).getSeries();
				i=(series!=null) ? series.size() : 0;
			}
			return i;
		}
		return 0;
	}

	public boolean isLeaf(Object node) {return (getChildCount(node)==0);}

	public int getIndexOfChild(Object parent, Object child) {

		int index=0;
		if (parent instanceof List) {
			Set s=patientMap.keySet();
			for (Iterator i=s.iterator(); i.hasNext(); index++) {
				Object o=i.next();
				if (o==child) return index;
			}
		} else if (parent instanceof Patient) {
			Patient p=(Patient)parent;
			TreeSet ts=patientMap.get(p);
			for (Iterator i=ts.iterator(); i.hasNext(); index++) {
				Object o=i.next();
				if (o==child) return index;
			}
		} else if (parent instanceof Study) {
			Set<Series> series=((Study)parent).getSeries();
			ArrayList<Series> al=new ArrayList<Series>();
			al.addAll(series);
			Collections.sort(al);
			return al.indexOf(child);
		}
		return -1;
	}

	public String convertValueToText(Object node) {

		if (node instanceof List) {
			return rootName;
		} else if (node instanceof Patient) {
			Patient p=(Patient)node;
			String patientName=p.getPatientName();
			String patientID=StringUtilities.formatPatientID(p.getPatID());
			String patientNodeText=new String();
			patientNodeText+=((patientName!=null)&&(patientName.length()!=0)) ? patientName : new String("Unknown");
			patientNodeText=StringUtilities.formatName(patientNodeText,true);
			patientNodeText+=((patientID!=null)&&(patientID.length()!=0)) ? (" ("+patientID+")") : null;
			return patientNodeText;
		} else if (node instanceof Study) {
			Study s=(Study)node;
			return ((s.getStudyDate()!=null) ? (new String(DATE_FORMAT.format(s.getStudyDate()))) : (new String("(No study date available)")));
		} else if (node instanceof Series) {
			Series s=(Series)node;
			try {
				String description=(s.getDescription()!=null) ? StringUtilities.replaceAndCapitalize(URLDecoder.decode(s.getDescription(),"UTF-8"),true) : UNAVAILABLE_NO_DESCRIPTION;
				if ((description!=null)&&(description!="")) return description;
			} catch (Exception exc) {
				exc.printStackTrace();
				return UNAVAILABLE_NO_DESCRIPTION;
			}
		}
		return UNAVAILABLE_NO_DESCRIPTION;
	}

	// =======================================================================
  // TreeTableModel interface components...

	public Class getColumnClass(int column) {return (column==0) ? hierarchicalColumnClass : String.class;}

  public int getColumnCount() {return 9;}
    
	public String getColumnName(int column) {

		switch (column) {
		  case 0: return new String("Patient list");
		  case 1: return new String("Description");
		  case 2: return new String("Modality");
		  case 3: return new String("Anatomy");
		  case 4: return new String("# images");
		  case 5: return new String("Associated");
		  case 6: return new String("Location");
		  case 7: return new String("Type");
		  case 8: return new String("ID");
		}
		return null;
	}

	public Object getValueAt(Object node, int column) {

		switch (column) {
		  case 0: return node;
		  case 1: String description=(node instanceof Study) ? ((Study)node).getDescription() : null;
				      try {
								if (description!=null) description=StringUtilities.replaceAndCapitalize(URLDecoder.decode(description,"UTF-8"),true);
								return description;
							} catch (Exception exc) {}
							return null;
		  case 2: if (node instanceof Series) return ((Series)node).getModality();
				      return null;
		  case 3: String anatomy=(node instanceof Series) ? ((Series)node).getBodyPart() : null;
				      try {
								if (node instanceof Study) {
									anatomy=((Study)node).getDescription();
									if (anatomy!=null) anatomy=StringUtilities.findAnatomyPhrase(URLDecoder.decode(anatomy,"UTF-8"));
								}
							if (anatomy!=null) anatomy=StringUtilities.replaceAndCapitalize(anatomy,true);
				      return anatomy;
							} catch (Exception exc) {}
		  case 4: if (node instanceof Series) {
				        Integer i=((Series)node).getNumInstances();
								if (i.intValue()>0) return i;
			         }
				      return null;
		  case 5: if (node instanceof Study) {
								List l=((Study)node).getAssociatedData();
								int i=(l!=null) ? l.size() : 0;
								if (i>0) return new String (i+" items");
							} else if (node instanceof Series) {
								List l=((Series)node).getAssociatedData();
								int i=(l!=null) ? l.size() : 0;
								if (i>0) return new String (i+" items");
							} 
			        return null;
		  case 6: String institution=(node instanceof Study) ? ((Study)node).getInstitution() : null;
				      if (institution!=null) return StringUtilities.replaceAndCapitalize(institution,true);
				      return null;
		  case 7: if (node instanceof Series) return ((Series)node).getImageType();
				      return null;
		  case 8: if (node instanceof Series) return ((Series)node).getSeriesIUID();
				      if (node instanceof Study) return ((Study)node).getStudyUID();
				      return null;
		}
		return null;
	}

	public void setValueAt(Object value, Object node, int column) {}

	// =======================================================================
	// Methods for accessing the study->series->image links to get to
	// the file paths.

	public TreeSet<Series> computeSeries(TreePath[] paths) {

		TreeSet<Series> seriesList=new TreeSet<Series>();
		for (int loop=0; loop<paths.length; loop++) {
			Object node=paths[loop].getLastPathComponent();
			if (node instanceof Patient) {
				Patient p=(Patient)node;
				TreeSet ts=patientMap.get(p);
				if (ts!=null) {
					for (Iterator i=ts.iterator(); i.hasNext();) {
						Study s=(Study)i.next();
						if ((((Study)s).getId()!=null)&&(((Study)s).getId()!=-1)) {
							ImageViewerClientNode.getInstance().beginTransaction();
							ImageViewerClientNode.getInstance().load(s,s.getId());
						}
						s.load();
						Set<Series> series=s.getSeries();
						seriesList.addAll(series);
					}
				}
			} else if (node instanceof Study) {
				try {
					Study s=(Study)node;
					if ((((Study)s).getId()!=null)&&(((Study)s).getId()!=-1)) {
						ImageViewerClientNode.getInstance().beginTransaction();
						ImageViewerClientNode.getInstance().load(s,s.getId());
					}
					s.load();
					Set<Series> series=s.getSeries();
					seriesList.addAll(series);
				} catch (Exception exc) {
					exc.printStackTrace();
				} finally {
					ImageViewerClientNode.getInstance().rollbackTransaction();
				}
			} else if (node instanceof Series) {
				seriesList.add((Series)node);
			}
		}
		Set noDuplicates=new HashSet(seriesList);
		seriesList.clear();
		seriesList.addAll(noDuplicates);
		return seriesList; 
	}

	public TreeSet<Study> computeStudies(TreePath[] paths) {

		TreeSet<Study> studyList=new TreeSet<Study>();
		for (int loop=0; loop<paths.length; loop++) {
			Object node=paths[loop].getLastPathComponent();
			if (node instanceof Patient) {
				Patient p=(Patient)node;
				TreeSet ts=patientMap.get(p);
				if (ts!=null) {
					for (Iterator i=ts.iterator(); i.hasNext();) {
						Study s=(Study)i.next();
						if ((((Study)s).getId()!=null)&&(((Study)s).getId()!=-1)) {
							ImageViewerClientNode.getInstance().beginTransaction();
							ImageViewerClientNode.getInstance().load(s,s.getId());
						}
						s.load();
						studyList.add(s);
					}
				}
			} else if (node instanceof Study) {
				try {
					Study s=(Study)node;
					if ((((Study)s).getId()!=null)&&(((Study)s).getId()!=-1)) {
						ImageViewerClientNode.getInstance().beginTransaction();
						ImageViewerClientNode.getInstance().load(s,s.getId());
					}
					s.load();
					studyList.add(s);
				} catch (Exception exc) {
					exc.printStackTrace();
				} finally {
					ImageViewerClientNode.getInstance().rollbackTransaction();
				}
			} else if (node instanceof Series) {
				try {
					Series s=(Series)node;
					if ((((Series)s).getId()!=null)&&(((Series)s).getId()!=-1)) {
						ImageViewerClientNode.getInstance().beginTransaction();
						ImageViewerClientNode.getInstance().load(s,s.getId());
					}
					s.load();
					studyList.add(s.getStudy());
				} catch (Exception exc) {
					exc.printStackTrace();
				} finally {
					ImageViewerClientNode.getInstance().rollbackTransaction();
				}
			}
		}
		return studyList; 
	}

	// =======================================================================

	private static class PatientComparator implements Comparator {

		public int compare(Object o1, Object o2) {

			if ((o1==null)||(o2==null)) return 0;
			if ((((Patient)o1).getPatientName()==null)||(((Patient)o2).getPatientName()==null)) return 0;
			return ((Patient)o1).getPatientName().compareTo(((Patient)o2).getPatientName());
		}
		public boolean equals(Object obj) {return (obj==DEFAULT_PATIENT_COMPARATOR);}

	}
}



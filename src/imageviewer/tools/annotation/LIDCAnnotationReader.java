/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.annotation;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import imageviewer.model.Image;
import imageviewer.model.DataLayer;
import imageviewer.model.dl.ShapeDataLayer;
import imageviewer.tools.AnnotationReader;
import imageviewer.ui.annotation.StylizedShape;

// =======================================================================

public class LIDCAnnotationReader extends AnnotationReader {

	private static final Color FILL_COLOR=new Color(32,128,220);
	private static final Color STROKE_COLOR= Color.yellow;

	public boolean canRead(File f) {

		boolean b=false;
		FileReader fr=null;
		BufferedReader br=null;
		try {
			fr=new FileReader(f);
			br=new BufferedReader(fr);
			String s1=br.readLine();
			String s2=br.readLine();
			if ((s2!=null)&&(s2.contains("LidcReadMessage.xsd"))) b=true;
		} catch (Exception exc) {
		}	finally { 
			try {
				br.close();
				fr.close();
			} catch (Exception exc2) {}
		}
		return b;
	}

	// =======================================================================

	public void readFile(File f, ArrayList<? extends Image> images) {

		SeriesAnnotation sa=new SeriesAnnotation();
		
		try {
			SAXReader saxReader=new SAXReader();
			Document doc=saxReader.read(f);
			Element root=doc.getRootElement();
			Iterator responseHeaderIter=root.elementIterator("ResponseHeader");
			String seriesUID="";
			String studyUID="";
			while (responseHeaderIter.hasNext()) {
				Element responseHeader=(Element) responseHeaderIter.next();
				sa.setUID(responseHeader.elementText("SeriesInstanceUid"));
				//sa.setUID(responseHeader.elementText("StudyInstanceUID"));
			}
			Iterator readingSessionIter=root.elementIterator("readingSession");
			while (readingSessionIter.hasNext()) {
				Element readingSessionElem=(Element)readingSessionIter.next();
				Iterator unblindedIter=readingSessionElem.elementIterator("unblindedReadNodule");
				while (unblindedIter.hasNext()) {
						Element unblindedElem=(Element)unblindedIter.next();
						Iterator roiIter=unblindedElem.elementIterator("roi");
						while (roiIter.hasNext()) {
							Element roi=(Element) roiIter.next();
							String imageUID=roi.elementText("imageSOP_UID");
							String inclusion=roi.elementText("inclusion");
							ImageAnnotation imageA=new ImageAnnotation(imageUID);
							imageA.setNodule(true);
							if(inclusion.equalsIgnoreCase(("true"))){
								imageA.setInclusion(true);
							} else {
								imageA.setInclusion(false);
							}
							Iterator edgeMapIter=roi.elementIterator("edgeMap");
							while (edgeMapIter.hasNext()) {
								Element edgeMapElem=(Element) edgeMapIter.next();
								String x=edgeMapElem.elementText("xCoord");
								String y=edgeMapElem.elementText("yCoord");
								// System.out.println("[debug] nodule adding for " + imageUID + ": " + x + "," + y);
								imageA.addPoint(Integer.valueOf(x), Integer.valueOf(y));
							}							
							sa.addAnnotation(imageA);
						}
					}
				Iterator nonNoduleIter=readingSessionElem.elementIterator("nonNodule");
				while (nonNoduleIter.hasNext()) {
					Element nonNoduleElem=(Element) nonNoduleIter.next();
					String imageUID=nonNoduleElem.elementText("imageSOP_UID");
					ImageAnnotation imageA=new ImageAnnotation(imageUID);
					imageA.setNodule(false);
					Iterator locusIter=nonNoduleElem.elementIterator("locus");
					while (locusIter.hasNext()) {
						Element locusElem=(Element) locusIter.next();
						String x=locusElem.elementText("xCoord");
						String y=locusElem.elementText("yCoord");
						// System.out.println("[debug] nonNodule adding for " + imageUID + ": " + x + "," + y);
						imageA.addPoint(Integer.valueOf(x), Integer.valueOf(y));
					}
					sa.addAnnotation(imageA);
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		// Given the series annotation, create the appropriate data layers
		// per annotation

		for (int loop=0, n=sa.numAnnotations(); loop<n; loop++){
			ImageAnnotation ia=sa.getAnnotation(loop);
			String annotationUID=ia.getUID();
			for (Iterator i=images.iterator(); i.hasNext();) {
				Image img=(Image)i.next();
				String imageUID=(String)img.getProperties().get("SOPInstanceUID");
				if ((imageUID!=null)&&(imageUID.equals(annotationUID))) {
					ShapeDataLayer sdl=(ShapeDataLayer)img.findDataLayer(DataLayer.SHAPE);
					if (sdl==null) {
						sdl=new ShapeDataLayer();
						img.addDataLayer(sdl);
					}
					Polygon polygon=new Polygon();
					for (int j=0, m=ia.numPoints(); j<m; j++){
						Point pt=ia.getPoint(j);
						polygon.addPoint(pt.x,pt.y);
					}
					StylizedShape ss=new StylizedShape(polygon,null,1.0f,0.75f);
					// ss.setFilled(isFilled);
					ss.setStrokeColor(STROKE_COLOR);
					ss.setFillColor(FILL_COLOR);
					ss.setFillAlphaComposite(0.5f);
					sdl.addShape(ss);
				}
			}
		}
	}

	// =======================================================================

	private class SeriesAnnotation {

		ArrayList<ImageAnnotation> annotations;
		Hashtable<String,String> properties;
		String seriesUID;
		
		public SeriesAnnotation() {this("");}
		
		public SeriesAnnotation(String uid) {
			seriesUID=uid;
			properties=new Hashtable();
			annotations=new ArrayList();
		}		
		
		public String getProperty(String propertyName){return properties.get(propertyName);}
		
		public Hashtable<String,String> getProperties() {return properties;}
		
		public int numAnnotations() {return annotations.size();}
		
		public ImageAnnotation getAnnotation(int i) {return annotations.get(i);}
		
		public void setUID(String uid) {seriesUID=uid;}
		public void addProperty(String propertyName, String value) {properties.put(propertyName,value);}
		public void addAnnotation(ImageAnnotation a) {annotations.add(a);}
	} 
	
	// =======================================================================
	/**
	 * Private class to handle annotations for each slice
	 * 
	 * @author willhsu
	 *
	 */
	private class ImageAnnotation {

		ArrayList<Point> points;
		String imageUID;
		boolean isNodule;
		boolean inclusion;
		
		public ImageAnnotation(String uid) {
			imageUID=uid;
			points=new ArrayList();
			isNodule=false;
			inclusion=false;
		}
		
		public int numPoints() {return points.size();}
		public Point getPoint(int i) {return points.get(i);}
		public String getUID() {return imageUID;}
		public boolean isNodule() {return isNodule;}
		public boolean isInclusion() {return inclusion;}

		public void addPoint(int x, int y) {points.add(new Point(x,y));}
		public void setInclusion(boolean inclusion) {this.inclusion=inclusion;}
		public void setNodule(boolean isNodule) {this.isNodule=isNodule;}
	} 
}

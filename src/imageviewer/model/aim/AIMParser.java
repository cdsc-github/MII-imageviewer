/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

import imageviewer.model.aim.ImageAnnotation.imageAnnotationIdentifier;
import imageviewer.model.aim.markup.*;
import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.AnnotationAIMObjectFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import utility.xml.SAXParserPool;

public class AIMParser {
	
	private static final Logger LOG=Logger.getLogger("imageViewer.config");
	
	private static final String POLYLINE="Polyline";
	
	public AIMParser() {}

	// =======================================================================

	public static Annotation parseString(String xmlString) {

		LOG.info("Parsing AIM Annotation XML");
		try {
			AIMHandler handler=new AIMHandler();
		    SAXParser parser=SAXParserPool.getSAXParser();
		    StringReader sr = new StringReader(xmlString);
		    //InputStream is=FileResolver.resolveResource(TOCDataClassifiers.class,filename);
		    InputSource aSource=new InputSource(sr);
		    parser.parse(aSource,handler);
		    Annotation a = handler.getAnnotation();
		    sr.close();
		    SAXParserPool.releaseSAXParser(parser);
		    return a;
		} catch (NullPointerException exc) {
		    LOG.error("Specified string could not be accessed");
		} catch (Exception exc) {
		    LOG.error("Error attempting to parse AIM string for base data collection parameters");
		    exc.printStackTrace();
		}
		return null;
	}
	
	public static class AIMHandler extends DefaultHandler {
		
		private enum annotationType { CIRCLE, ELLIPSE, POINT, MULTIPOINT, POLYLINE; }
		
		Annotation currentAnnotation = null;
		String studyUID = null;
		String seriesUID = null;
		String imageUID = null;
		
		String shapeType = null;
		TextAnnotation ta = null;
		GeometricShape gs = null;
	
		// =======================================================================

		public AIMHandler() {super();}
		
		public Annotation getAnnotation(){return this.currentAnnotation;}

		public void startDocument() {

		}

		public void startElement(String uri, String localname, String qname, Attributes attr) {

		    if (localname.compareTo("ImageAnnotation")==0) {
		    	String type = new String(attr.getValue("xsi:type"));
		    	AIMAnnotation aimAnnotation = new ImageAnnotation(imageAnnotationIdentifier.Teaching);
				return;
			}
		    if (localname.compareTo("TextAnnotation")==0) {
		    	String text = new String(attr.getValue("text"));
		    	ta = new TextAnnotation(text);
				return;
			}
//		    if (localname.compareTo("ImageReference")==0) {
//				studyUID=new String(attr.getValue("instanceUID"));
//				return;
//			}
		    if (localname.compareTo("Study")==0) {
				studyUID=new String(attr.getValue("instanceUID"));
				return;
			}
		    if (localname.compareTo("Study")==0) {
				studyUID=new String(attr.getValue("instanceUID"));
				return;
			}
		    if (localname.compareTo("Series")==0) {
				seriesUID=new String(attr.getValue("instanceUID"));
				return;
			}
		    if (localname.compareTo("Image")==0) {
				imageUID=new String(attr.getValue("sopInstanceUID"));
				return;
			}
		    if (localname.compareTo("GeometricShape")==0) {
				shapeType=new String(attr.getValue("xsi:type"));
				switch (annotationType.valueOf(shapeType.toUpperCase())){
					case CIRCLE: gs = new Circle(); break;
					case ELLIPSE: gs = new Ellipse(); break;
					case POINT: gs = new Point(); break;
					case MULTIPOINT: gs = new MultiPoint(); break;
					case POLYLINE: gs = new Polyline(); break;
				}
				return;
		    }
		    if (localname.compareTo("SpatialCoordinate")==0) {
				String type=new String(attr.getValue("xsi:type"));
				if(type.compareTo("TwoDimensionSpatialCoordinate")==0){
					String x = new String(attr.getValue("x"));
					String y = new String(attr.getValue("y"));
					TwoDimensionCoordinate aCoord = new TwoDimensionCoordinate(Double.parseDouble(x), Double.parseDouble(y));
					gs.addSpatialCoordinate(aCoord);
				}
				if(type.compareTo("ThreeDimensionSpatialCoordinate")==0){
					String x = new String(attr.getValue("x"));
					String y = new String(attr.getValue("y"));
					String z = new String(attr.getValue("z"));
					ThreeDimensionCoordinate aCoord = new ThreeDimensionCoordinate(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
					gs.addSpatialCoordinate(aCoord);
				}
				return;
		    }

		}

		// =======================================================================

		public void endElement(String uri, String localname, String qname) {

		    if (localname.compareTo("GeometricShape")==0) {
				if (!(gs instanceof MultiPoint))
					currentAnnotation = AnnotationAIMObjectFactory.createAnnotation(gs);
		    } else if (localname.compareTo("TextAnnotation")==0) {
		    	ta.setConnectorPoints((MultiPoint)gs);
		    	currentAnnotation = AnnotationAIMObjectFactory.createAnnotation(ta);
		    } 
			
		}
	}

}

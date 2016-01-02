/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.Hashtable;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import imageviewer.util.XMLUtil;

public class LayoutDescriptionReader {

	private static Logger LOG=Logger.getLogger("imageViewer.ui");

	// =======================================================================

	public static LayoutDescription load(String filename) {

		try {
			LayoutDefinitionXMLHandler handler=new LayoutDefinitionXMLHandler();
			SAXParser parser=XMLUtil.getSAXParser();
			InputStream is=new FileInputStream(filename);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,handler);
			is.close();
			XMLUtil.releaseSAXParser(parser);
			return handler.getLayoutDescription();
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+filename);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+filename);
			exc.printStackTrace();
		}
		return null;
	}
	
	// =======================================================================

	private static class LayoutDefinitionXMLHandler extends DefaultHandler {

		LayoutDescription ld=null;
		ControlGroupDescription cgd=null;
		PanelDescription pd=null;

		public LayoutDefinitionXMLHandler() {super();}
		
		// =======================================================================

		public LayoutDescription getLayoutDescription() {return ld;}

		// =======================================================================

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			try {
				if (localname.compareTo("layout")==0) {
					ld=new LayoutDescription();
					ld.setName(attr.getValue("name"));
					ld.setResolutionWidth(Integer.parseInt(attr.getValue("resolutionWidth")));
					ld.setResolutionHeight(Integer.parseInt(attr.getValue("resolutionHeight")));
					return;
				}
				if (localname.compareTo("controlGroup")==0) {
					cgd=new ControlGroupDescription();
					cgd.setID(Integer.parseInt(attr.getValue("id")));
					cgd.setContainer(ld);
					cgd.setX(Integer.parseInt(attr.getValue("x")));
					cgd.setY(Integer.parseInt(attr.getValue("y")));
					cgd.setType(attr.getValue("type"));
					cgd.setTarget(attr.getValue("target"));
					return;
				}
				if (localname.compareTo("groupProperty")==0) {
					cgd.setProperty(attr.getValue("name"),attr.getValue("value"));
					return;
				}
				if (localname.compareTo("panel")==0) {
					pd=new PanelDescription();
					pd.setContainer(cgd);
					pd.setX(Integer.parseInt(attr.getValue("x")));
					pd.setY(Integer.parseInt(attr.getValue("y")));
					pd.setWidth(Integer.parseInt(attr.getValue("width")));
					pd.setHeight(Integer.parseInt(attr.getValue("height")));
					return;
				}
				if (localname.compareTo("panelProperty")==0) {
					pd.setProperty(attr.getValue("name"),attr.getValue("value"));
					return;
				}
				if (localname.compareTo("renderingPipeline")==0) {
					ld.setRenderingPipelineName(attr.getValue("name"));
					return;
				}
			} catch (Exception exc) {
				LOG.error("Unable to parse sepcified file in LayoutDescriptionReader.");
				exc.printStackTrace();
			}
		}

		public void endElement(String uri, String localname, String qname) {

			try {
				if (localname.compareTo("controlGroup")==0) {
					ld.addControlGroup(cgd);
					cgd=null;
					return;
				}
				if (localname.compareTo("panel")==0) {
					cgd.addPanel(pd);
					pd=null;
				}
			} catch (Exception exc) {
				LOG.error("Unable to parse sepcified file in LayoutDescriptionReader.");
				exc.printStackTrace();
			}
		}
	}
}

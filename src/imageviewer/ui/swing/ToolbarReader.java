/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Dimension;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import imageviewer.util.XMLUtil;

// =======================================================================

public class ToolbarReader {
	
	private static final Logger LOG=Logger.getLogger("imageViewer.config");
	private static final Dimension SPACER=new Dimension(3,0);
	
	public ToolbarReader() {}

	// =======================================================================

	public static JToolBar parseFile(String filename) {

		LOG.info("Loading toolbar configuration file: "+filename);
		
		try {
			ToolbarHandler handler=new ToolbarHandler();
			SAXParser parser=XMLUtil.getSAXParser();
			InputStream is=new FileInputStream(filename);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,handler);
			is.close();
			XMLUtil.releaseSAXParser(parser);
			return handler.getToolbar();
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+filename);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+filename);
			exc.printStackTrace();
		}
		return null;
	}

	// =======================================================================

	public static class ToolbarHandler extends DefaultHandler {

		JToolBar toolbar=new JToolBar();
		MultiButton mb=null;
	
		// =======================================================================

		public ToolbarHandler() {super(); toolbar.setFloatable(false);}

		public JToolBar getToolbar() {return toolbar;}

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			if (localname.compareTo("button")==0) {
				String targetFunction=attr.getValue("functionName");
				if (targetFunction!=null) {
					MenuAction ma=(MenuAction)MenuAction.ACTIONS.get(targetFunction);
					if (ma!=null) {
						if (mb!=null) mb.addButton(ma.getToolbarItem(),ma.getCommandName()); else toolbar.add(ma.getToolbarItem());
					}
				}
				return;
			}

			if (localname.compareTo("separator")==0) {
				JSeparator js=new JSeparator(SwingConstants.VERTICAL);
				js.setMaximumSize(new Dimension(2,30));
				js.setBackground(new Color(20,20,20));
				js.setForeground(new Color(90,90,90,128));
				// js.setMaximumSize(new Dimension(1,30));
				// js.setForeground(Color.darkGray);
				toolbar.addSeparator(SPACER);
				toolbar.add(js);
				toolbar.addSeparator(SPACER);
				return;
			}

			if (localname.compareTo("multiButton")==0) {
				if (mb!=null) {
					LOG.error("Error in toolbar configuration: multiButton cannot be nested.");
				} else {
					boolean showText=(attr.getIndex("showText")>=0) ? Boolean.parseBoolean(attr.getValue("showText")) : false;
					mb=new MultiButton(showText);
				}
			}
		}

		public void endElement(String uri, String localname, String qname) {

			if (localname.compareTo("multiButton")==0) {toolbar.add(mb); mb=null; return;}
		}
	}
}


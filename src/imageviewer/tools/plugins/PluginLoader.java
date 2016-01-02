/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.plugins;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.swing.MenuReader;
import imageviewer.util.XMLUtil;

// =======================================================================

public class PluginLoader {

	private static final Logger LOG=Logger.getLogger("imageViewer.plugin");

	public static void initialize(String filename) {parseFile(filename);}

	public static void parseFile(String filename) {

		LOG.info("Loading plugin configuration file: "+filename);
		try {
			PluginHandler handler=new PluginHandler();
			SAXParser parser=XMLUtil.getSAXParser();
			InputStream is=new FileInputStream(filename);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,handler);
			is.close();
			XMLUtil.releaseSAXParser(parser);
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+filename);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+filename);
			exc.printStackTrace();
		}
	}

	// =======================================================================

	public static class PluginHandler extends DefaultHandler {

		public PluginHandler() {super();}

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			if (localname.compareTo("plugin")==0) {
				String targetPluginClass=attr.getValue("classname");
				try {
					Plugin p=PluginManager.getPlugin(Class.forName(targetPluginClass));
					String menuFilename=p.getMenuFilename();
					if (menuFilename!=null) {
						JMenu menu=MenuReader.parseFile(menuFilename);
						if (menu!=null) {
							String[] menuLocation=p.getMenuLocation();
							if (menuLocation==null) menuLocation=new String[] {"Tools","Plugins"};
							JMenuItem jmi=MenuReader.findMenu(ApplicationContext.getContext().getApplicationMenuBar(),menuLocation);
							if (jmi instanceof JMenu) ((JMenu)jmi).add(menu);
						} 
					}
				} catch (Exception exc) {
					LOG.error("Unable to process specified plugin: "+targetPluginClass);
					exc.printStackTrace();
				}
				return;
			}
			
			if (localname.compareTo("pluginDirectory")==0) {}
		}
	}
}

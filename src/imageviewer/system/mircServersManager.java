/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.system;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.lang.reflect.Constructor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import utility.xml.SAXParserPool;

import java.util.ArrayList;

//import imageviewer.system.NCIAServers;

import imageviewer.ui.ApplicationContext;

/**
 * This class is responsible for parsing the XML list of MIRC servers.
 * It contains a hash map associating the server name with a URL.
 */

public class mircServersManager {

	private static final String DEFAULT_CONFIG=new String("config/mircServers.xml");
	private static Logger LOG=Logger.getLogger("imageviewer.config");
	private static mircServersManager INSTANCE=null;

    /**
     * Static method which returns the instance of the mircServersManager.
     * Because the constructor is private, this is how you access the
     * instance of mircServersManager.
     */
	public static mircServersManager getInstance() {if (INSTANCE==null) INSTANCE=new mircServersManager(); return INSTANCE;}

	// =======================================================================

    /**
     * Hash map tying sever name to URL.
     */

	HashMap<String,String> mircServersMap=new HashMap<String,String>();

    /**
     * Private constructor which instantiates a handler to perform parsing.
     */

	private mircServersManager() {

		String configFile=(String)ApplicationContext.getContext().getProperty("CONFIG_READERS");
		if (configFile==null) configFile=DEFAULT_CONFIG;

		try {
			mircServersHandler rch=new mircServersHandler();
			SAXParser parser=SAXParserPool.getSAXParser();
			InputStream is=new FileInputStream(configFile);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,rch);
			SAXParserPool.releaseSAXParser(parser);
			is.close();
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+configFile);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+configFile);
			exc.printStackTrace();
		}
	}

	// =======================================================================

    /**
     * Given the server name, returns the URL.
     */

        public String getmircURL(String key) {return mircServersMap.get(key);}

	// =======================================================================

    /**
     * Returns a list of server names.
     */
	public String[] getmircServers() {

		TreeMap sortedKeys=new TreeMap(mircServersMap);
		String[] keys=new String[sortedKeys.size()];
		Iterator i=sortedKeys.keySet().iterator();
		for (int loop=0; i.hasNext(); loop++) keys[loop]=(String)i.next();
		return keys;
	}

	// =======================================================================

    /**
     * Private class which performs the XML parsing.
     */
	private class mircServersHandler extends DefaultHandler {

		Class[] constructorParameterTypes={};
		Object[] constructorParameters={};
		
		public mircServersHandler() {super();}

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			if (localname.compareTo("server")==0) {
			    String serverName=new String(attr.getValue("name"));
				String url=new String(attr.getValue("url"));
				mircServersMap.put(serverName,url); 
			}

		}

	public void endElement(String uri, String localname, String qname) {

	}
	}
}

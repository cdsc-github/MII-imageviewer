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

//import imageviewer.system.NCIAParams;

import imageviewer.ui.ApplicationContext;

// =======================================================================

/**
 * This class parses the XML file of NCIA params and stores them in
 * a hash map.
 */
public class NCIAParamsManager {

	private static final String DEFAULT_CONFIG=new String("config/NCIAParams.xml");
	private static Logger LOG=Logger.getLogger("imageviewer.config");
	private static NCIAParamsManager INSTANCE=null;
        private static String LAST_CONFIG = "";

	public static NCIAParamsManager getInstance() {if (INSTANCE==null) INSTANCE=new NCIAParamsManager(); return INSTANCE;}

    /**
     * This function allows us to switch dynamically between different 
     * config files (NCIA or MIRC).
     */
        public static NCIAParamsManager getInstance(String xyz) {
	    if (!xyz.equals(LAST_CONFIG)) {
		INSTANCE=null;
		INSTANCE=new NCIAParamsManager(xyz);
	    }
	    return INSTANCE;
	}

	// =======================================================================

    /**
     * Hash map of parameter name to list of values for that parameter.
     */

	HashMap<String,ArrayList<String>> NCIAParamsMap=new HashMap<String,ArrayList<String>>();

    /**
     * Hash map of parameter name to parameter type.
     */
    HashMap<String,String> NCIATypesMap=new HashMap<String,String>();
    String searchItemName = new String ();
    ArrayList <String> listItemNames = new ArrayList <String>();
    ArrayList <ArrayList <String>> fullParamArray = new ArrayList<ArrayList <String>>();
    int index = 0;

	private NCIAParamsManager() {

		String configFile=(String)ApplicationContext.getContext().getProperty("CONFIG_READERS");
		LAST_CONFIG = DEFAULT_CONFIG;
		if (configFile==null) configFile=LAST_CONFIG;

		try {
			NCIAParamsHandler rch=new NCIAParamsHandler();
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

	private NCIAParamsManager(String xyz) {

		String configFile=(String)ApplicationContext.getContext().getProperty("CONFIG_READERS");
		LAST_CONFIG = xyz;
		if (configFile==null) configFile=LAST_CONFIG;

		try {
			NCIAParamsHandler rch=new NCIAParamsHandler();
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
     * Given a parameter name, return the list of values for that param.
     */
	public ArrayList<String> getNCIAParams(String paramType) {return NCIAParamsMap.get(paramType);}
       
    /**
     * Given a parameter name, return the type for that param.
     */
        public String getNCIATypes(String paramType) {return NCIATypesMap.get(paramType);}

	// =======================================================================

    /**
     * Return the list of NCIA parameters.
     */
	public String[] getNCIAParamsKeys() {

		TreeMap sortedKeys=new TreeMap(NCIATypesMap);
		String[] keys=new String[sortedKeys.size()];
		Iterator i=sortedKeys.keySet().iterator();
		for (int loop=0; i.hasNext(); loop++) keys[loop]=(String)i.next();
		return keys;
	}

	// =======================================================================

	private class NCIAParamsHandler extends DefaultHandler {

		Class[] constructorParameterTypes={};
		Object[] constructorParameters={};
		
		public NCIAParamsHandler() {super();}

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			if (localname.compareTo("searchItem")==0) {
				searchItemName=attr.getValue("name");
				String typeName=new String(attr.getValue("type"));
				ArrayList<String> tempList = new ArrayList<String>();
				fullParamArray.add(tempList);
				NCIATypesMap.put(searchItemName,typeName); 
			}

			else if (localname.compareTo ("listItem") == 0)  {
			    String listItem = attr.getValue ("value");
			    fullParamArray.get(index).add (listItem);
			}

			

		}

	public void endElement(String uri, String localname, String qname) {
	    if (localname.compareTo ("searchItem") == 0)  {
		NCIAParamsMap.put(searchItemName,fullParamArray.get(index));
		index++;
		//listItemNames.clear();

	    }

	}

	}

}

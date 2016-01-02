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
 * This class parses the XML list of MIRC search parameters.
 * It maintains 2 hashmaps -- one linking parameter name to data type (list
 * or free text); another linking parameter name to list of possible values.
 */
public class mircParamsManager {

	private static final String DEFAULT_CONFIG=new String("config/mircParams.xml");
	private static Logger LOG=Logger.getLogger("imageviewer.config");
	private static mircParamsManager INSTANCE=null;

    /**
     * Static method for accessing instance of mircParamsManager.
     */
	public static mircParamsManager getInstance() {if (INSTANCE==null) INSTANCE=new mircParamsManager(); return INSTANCE;}

	// =======================================================================

    /**
     * Hash map of parameter name to list of possible values.
     */
	HashMap<String,ArrayList<String>> mircParamsMap=new HashMap<String,ArrayList<String>>();

    /**
     * Hash map of parameter name to parameter type.
     */
    HashMap<String,String> mircTypesMap=new HashMap<String,String>();
    String searchItemName = new String ();

    /**
     * List of parameter names.
     */
    ArrayList <String> listItemNames = new ArrayList <String>();

    /**
     * List of list of possible values for each parameter.
     */
    ArrayList <ArrayList <String>> fullParamArray = new ArrayList<ArrayList <String>>();
    int index = 0;

    /**
     * Private constructor which instantiates handler for parsing.
     */
	private mircParamsManager() {

		String configFile=(String)ApplicationContext.getContext().getProperty("CONFIG_READERS");
		if (configFile==null) configFile=DEFAULT_CONFIG;

		try {
			mircParamsHandler rch=new mircParamsHandler();
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
     * Given a parameter name, returns list of possible values for that parameter.
     */
	public ArrayList<String> getmircParams(String paramType) {return mircParamsMap.get(paramType);}

    /**
     * Given a parameter name, returns type for that parameter (list or text).
     */
        public String getmircTypes(String paramType) {return mircTypesMap.get(paramType);}

	// =======================================================================

    /**
     * Returns list of parameter types.
     */
	public String[] getmircParamsKeys() {

		TreeMap sortedKeys=new TreeMap(mircTypesMap);
		String[] keys=new String[sortedKeys.size()];
		Iterator i=sortedKeys.keySet().iterator();
		for (int loop=0; i.hasNext(); loop++) keys[loop]=(String)i.next();
		return keys;
	}

	// =======================================================================

    /** 
     * Private class which handles the XML parsing.
     */
	private class mircParamsHandler extends DefaultHandler {

		Class[] constructorParameterTypes={};
		Object[] constructorParameters={};
		
		public mircParamsHandler() {super();}

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			if (localname.compareTo("searchItem")==0) {
				searchItemName=attr.getValue("name");
				String typeName=new String(attr.getValue("type"));
				ArrayList<String> tempList = new ArrayList<String>();
				fullParamArray.add(tempList);
				mircTypesMap.put(searchItemName,typeName); 
			}

			else if (localname.compareTo ("listItem") == 0)  {
			    String listItem = attr.getValue ("value");
			    fullParamArray.get(index).add (listItem);
			}

			

		}

	public void endElement(String uri, String localname, String qname) {
	    if (localname.compareTo ("searchItem") == 0)  {
		mircParamsMap.put(searchItemName,fullParamArray.get(index));
		index++;
		//listItemNames.clear();

	    }

	}

	}

}

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

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import utility.xml.SAXParserPool;

import imageviewer.model.ImageReader;
import imageviewer.ui.ApplicationContext;

// =======================================================================

public class ImageReaderManager {

	private static final String DEFAULT_CONFIG=new String("config/imageReaders.xml");
	private static Logger LOG=Logger.getLogger("imageviewer.config");
	private static ImageReaderManager INSTANCE=null;

	public static ImageReaderManager getInstance() {if (INSTANCE==null) INSTANCE=new ImageReaderManager(); return INSTANCE;}

	// =======================================================================

	HashMap<String,ImageReader> imageReaderMap=new HashMap<String,ImageReader>();

	private ImageReaderManager() {

		String configFile=(String)ApplicationContext.getContext().getProperty("CONFIG_READERS");
		if (configFile==null) configFile=DEFAULT_CONFIG;

		try {
			ReaderConfigHandler rch=new ReaderConfigHandler();
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

	public ImageReader getImageReader(String imageType) {return imageReaderMap.get(imageType);}

	// =======================================================================

	public String[] getImageReaderTypes() {

		TreeSet sortedKeys=new TreeSet(imageReaderMap.keySet());
		String[] keys=new String[sortedKeys.size()];
		Iterator i=sortedKeys.iterator();
		for (int loop=0; i.hasNext(); loop++) keys[loop]=(String)i.next();
		return keys;
	}

	// =======================================================================

	private class ReaderConfigHandler extends DefaultHandler {

		Class[] constructorParameterTypes={};
		Object[] constructorParameters={};
		
		public ReaderConfigHandler() {super();}

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			if (localname.compareTo("reader")==0) {
				String readerName=attr.getValue("fileType");
				String className=attr.getValue("class");
				try {
					Constructor newConstructor=Class.forName(className).getConstructor(constructorParameterTypes);
					ImageReader ir=(ImageReader)newConstructor.newInstance(constructorParameters);
					imageReaderMap.put(readerName,ir);
					} catch (Exception exc) {
						LOG.error("Error attempting to create: "+className);
						exc.printStackTrace();
					}
				
			}
		}
	}
}

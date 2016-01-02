/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import utility.xml.SAXParserPool;

import imageviewer.model.Image;
import imageviewer.ui.ApplicationContext;

// =======================================================================

public class RenderingOpPipelineFactory {

	private static final String DEFAULT_CONFIG=new String("config/pipeline.xml");
	private static Logger LOG=Logger.getLogger("imageviewer.rendering");

	public static Hashtable<String,RenderingOpPipeline> PIPELINES=new Hashtable<String,RenderingOpPipeline>();

	// =======================================================================

	public static RenderingOpPipeline create() {

		String targetFile=(String)ApplicationContext.getContext().getProperty(ApplicationContext.CONFIG_PIPELINE);
		if (targetFile==null) targetFile=DEFAULT_CONFIG;
		return create(targetFile);
	}

	public static RenderingOpPipeline create(String configFile) {

		try {
			RenderingOpPipelineXMLConfigHandler rpxch=new RenderingOpPipelineXMLConfigHandler();
			SAXParser parser=SAXParserPool.getSAXParser();
			InputStream is=new FileInputStream(configFile);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,rpxch);
			RenderingOpPipeline rop=rpxch.getOpPipeline();
			PIPELINES.put(rop.getName(),rop);
			SAXParserPool.releaseSAXParser(parser);
			is.close();
			return rop;
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+configFile);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+configFile);
			exc.printStackTrace();
		}
		return null;
	}

	// =======================================================================

	public static RenderingOpPipeline lookup(String name) {return PIPELINES.get(name);}

	// =======================================================================

	private static class RenderingOpPipelineXMLConfigHandler extends DefaultHandler {

		Class[] constructorParameterTypes={};
		Object[] constructorParameters={};
		RenderingOpPipeline rop=new RenderingOpPipeline();

		public RenderingOpPipelineXMLConfigHandler() {super();}

		// =======================================================================

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			try {
				if (localname.compareTo("pipeline")==0) {rop.setName(attr.getValue("name")); return;}
				if (localname.compareTo("operation")==0) {
					String opName=attr.getValue("name");
					String className=attr.getValue("className");
					try {
						Constructor newConstructor=Class.forName(className).getConstructor(constructorParameterTypes);
						rop.add((RenderedOperation)newConstructor.newInstance(constructorParameters));
					} catch (Exception e) {
						LOG.error("Error attempting to create RenderedOperation: "+className);
						e.printStackTrace();
					}
				}
			} catch (Exception exc) {
				LOG.error("Error attempting to parse pipeline at element <"+localname+">");
				exc.printStackTrace();
			}
		}

		// =======================================================================

		public RenderingOpPipeline getOpPipeline() {return rop;}
	}
}

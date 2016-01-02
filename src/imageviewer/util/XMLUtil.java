/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.util;

import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

// =======================================================================

public class XMLUtil {

	private static SAXParserFactory factory=null;
	private static Stack stack=null;

	// =======================================================================

	static {

		// Initialize the factory and the stack...

			try {
				factory = SAXParserFactory.newInstance();
				factory.setValidating(true);
				factory.setNamespaceAware(true);
				stack = new Stack();
			} catch (Exception exc) {
				System.err.println("Error attempting to initialize SAXParserFactory: " + exc);
				exc.printStackTrace();
				System.exit(1);
			}
	}

	// =======================================================================

	public static SAXParserFactory getSAXParserFactory() {return factory;}

	// =======================================================================

	public static synchronized SAXParser getSAXParser() {
		
		if (!stack.empty()) return (SAXParser)stack.pop();
		try {
			SAXParser parser=factory.newSAXParser();
			XMLReader reader=parser.getXMLReader();
			reader.setProperty("http://apache.org/xml/properties/input-buffer-size",new Integer(8192));
			return parser;
		} catch (Exception exc) {
			System.err.println("Error attempting to initialize SAXParserFactory: "+exc);
			exc.printStackTrace();
			return null;
		}
	}

	// =======================================================================

	public static synchronized void releaseSAXParser(SAXParser parser) {stack.push(parser);}

}

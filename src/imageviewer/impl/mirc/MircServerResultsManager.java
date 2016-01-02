/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.impl.mirc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import javax.xml.parsers.SAXParser;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import utility.xml.SAXParserPool;
import imageviewer.ui.dialog.MircTreeTableModel;
import java.io.StringReader;

//=======================================================================
/**
 * Parses the XML-based results string of the MIRC server query. One instance of this class
 * is instantiated at a time. The constructor calls the SAX parser and creates a HashMap of 
 * all of the image results returned. This class does not retrieve the image data from the MIRC
 * server, only their URLs and other properties. 
 *
 * @author Brian Burns, Jean Garcia, Agatha Lee, Jamal Madni
 * @version $Revision: 1.0 $ $Date: 2008/12/05 10:13:34 $
 */
public class MircServerResultsManager {
	
	private HashMap<String,HashMap <String, String>> MircResultsMap=new HashMap<String,HashMap <String, String>>();
    private HashMap<Integer,String> MircKeysMap=new HashMap<Integer,String>();
    
	private static Logger LOG=Logger.getLogger("imageviewer.config");
	private static MircServerResultsManager INSTANCE=null;    

	private int DatabaseIndex = 0;
    private int DatabaseTotal = 0;
		
    // =======================================================================
	/**
	 * MircServerResultsManager dummy constructor. Not used.
	 * 
 	 */
	private MircServerResultsManager() { /*empty constructor*/ }
	
	// =======================================================================
	/**
	 * MircServerResultsManager constructor. It parses the XML in the query results
	 * string and stores the element values 
	 * 
	 * @param results - the query results string from the MIRC server that contains all of the database image information
 	 */
	private MircServerResultsManager(String results) {

		try {
			MircServeResultsHandler rch=new MircServeResultsHandler();
			SAXParser parser=SAXParserPool.getSAXParser();
			InputSource aSource=new InputSource(new StringReader(results));
			parser.parse(aSource,rch);
			SAXParserPool.releaseSAXParser(parser);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse query results");
			exc.printStackTrace();
		}
	}
    // =======================================================================
    /**
	 * Returns the current instance of MircServerResultsManager if the MircServerResultsManager(String)
	 * constructor was used. Null otherwise
	 * 
	 * @return null or the current instance of MircServerResultsManager
 	 */
	public static MircServerResultsManager getInstance() { if (INSTANCE!=null) return INSTANCE; else return null; }
	
	// =======================================================================
	/**
	 * Creates a new instance of MircServerResultsManager from the xyz query results string
	 * 
	 * @param xyz - the query results string from the MIRC server that contains all of the database image information
	 * @return the new instance of MircServerResultsManager
 	 */
	public static MircServerResultsManager getInstance(String xyz) { INSTANCE=new MircServerResultsManager(xyz); return INSTANCE; }

	
	// =======================================================================
	/**
	 * Returns the key map used to access the query results 
	 * 
	 * @return an array of strings that contains the keys to access the query results
 	 */
	public String[] getMircResultKeys() {
		TreeMap sortedKeys=new TreeMap(MircKeysMap);
		String[] keys=new String[sortedKeys.size()];
		Iterator i=sortedKeys.keySet().iterator();
		for (int loop=0; i.hasNext(); loop++) keys[loop]=(String)i.next();
		return keys;
	}
	
	// =======================================================================
	/**
	 * Returns the HashMap that contains all of an individual image's properties (eg, author, URL, title, etc)
	 * 
	 * @param key - a hash key from getMircResultKeys()
	 * @return HashMap<String, String> - a hash of image properties that are key off of the MircTreeTableModel.COLUMN_NAMES[] array
 	 */
	public HashMap<String, String> getMircImage(String key){
		return MircResultsMap.get(key);
	}
	
	// =======================================================================
	/**
	 * Returns the total number of databases that returned query results
	 * 
	 * @return int - total number of databases
 	 */
    public int getDatabaseTotal()  {
    	return DatabaseTotal;
    }
    
    //=======================================================================
    /**
     * Helper class that parses the XML query result string, stores image URL properties, 
     * and populates the MircResultsMap and MircResultsKeys properties of MircServerResultsManager
     *
     * @author Brian Burns, Jean Garcia, Agatha Lee, Jamal Madni
     * @version $Revision: 1.0 $ $Date: 2008/12/05 10:13:34 $
     */
	private class MircServeResultsHandler extends DefaultHandler {

		private Class[] constructorParameterTypes={};
		private Object[] constructorParameters={};
		
		private HashMap <String, String> ImageResultMap = new HashMap<String,String>();
		 
		private String currentElement = new String ();
		private String preamble = new String();
		private Integer key=0;
		private Integer imageNumber = 0;
		private Boolean inElement = false;
		private Boolean inAffiliation = false;
		
		// =======================================================================
		/**
		 * MircServeResultsHandler constructor.
	 	 */
		public MircServeResultsHandler() {super();}
		
		// =======================================================================
		/**
		 * Triggered by the SAX parser when the start tag of an element is found. 
		 * Sets the which XML element the parser is currently within.
		 * 
		 * @param uri - not used
		 * @param localname - the start tag name
		 * @param attr - not used
	 	 */
		public void startElement(String uri, String localname, String qname, Attributes attr) {
			
			if(localname.compareTo("p")!=0){
				currentElement = localname;
				inElement = true;
				
				if (localname.compareTo ("MIRCdocument")==0){
					ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[1], attr.getValue("docref")); // URL
					
				}else if (localname.compareTo ("affiliation")==0){
					inAffiliation=true;
				}

				//increment database total
				else if (localname.compareTo ("MIRCqueryresult") == 0)  {
				    DatabaseTotal++;
				}
			}
		}
		
		// =======================================================================
		/**
		 * Triggered by the SAX parser when the end tag of an element is found. Image
		 * properties are committed to storage here.
		 * 
		 * @param uri - not used
		 * @param localname - the end tag name
		 * @param qname - not used
	 	 */
		public void endElement(String uri, String localname, String qname) {
		    if (localname.compareTo ("MIRCdocument") == 0)  {

		    	//images are added to results hash based on database they are in and 
		    	//a monotonically increasing image identifier that resets for each
		    	//database. Images are keyed based off a monotonically increasing number 
		    	//that is unique across all databases
		    	MircResultsMap.put(DatabaseIndex+"+"+imageNumber, ImageResultMap);
		    	MircKeysMap.put(key++, DatabaseIndex+"+"+imageNumber++);
		    	ImageResultMap = new HashMap<String,String>();
		    	
		    }else if (localname.compareTo ("affiliation")==0){
		    	inAffiliation=false;
		    }else if (localname.compareTo("MIRCqueryresult")==0){
		    	//reset image counter and preamble for new database
		    	preamble = new String();
		    	imageNumber=0;

		    	//increment database index
		    	DatabaseIndex++;
		    }
		    inElement = false;
		}
		
		// =======================================================================
		/**
		 * Triggered by the SAX parser when characters are found between tags. This method pulls out
		 * the character strings in between tags and places that text into the ImageResultsMap for the current
		 * image according to which tag the characters were found between.
		 * 
		 * @param ch - an array of characters found between XML tags
		 * @param start - the starting index of the current characters in ch[]
		 * @param length - the length, in elements, of the characters found between XML tags
	 	 */ 
		public void characters(char[] ch, int start, int length){

			if(!inElement) return;
					
			int newStart = start;
			int newLength = length;
			
			for(int i=0; i<length;i++){
				if(Character.isLetterOrDigit(ch[start+i])){break;}
				newLength--;
				newStart++;
			}
			
			if(newLength == 0){return;}
			
			if (currentElement.compareTo ("preamble")==0)  {
				preamble = new String(ch, start, newLength);
				
			}else if (currentElement.compareTo ("title")==0){
				ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[0], new String(ch, start, newLength)); // Document name
			
			}else if (currentElement.compareTo ("name")==0){
				String authors = ImageResultMap.get(MircTreeTableModel.COLUMN_NAMES[2]);
				if(authors == null){
					ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[2], new String(ch, start, newLength)); // 1st Author
				}else{
					authors = authors + " and " + new String(ch, start, newLength);
					ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[2], authors); // >1 Authors
				}
			
			}else if (currentElement.compareTo ("abstract")==0){
				ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[3], new String(ch, start, newLength)); // Abstract
			
			}else if (currentElement.compareTo ("level")==0){
				ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[4], new String(ch, start, newLength)); // Level
			
			}else if (currentElement.compareTo ("access")==0){
				ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[5], new String(ch, start, newLength)); // Access
			
			}else if (currentElement.compareTo ("peer review")==0){
				ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[6], new String(ch, start, newLength)); // Peer Review
			
			}else if (currentElement.compareTo ("category")==0){
				ImageResultMap.put(MircTreeTableModel.COLUMN_NAMES[7], new String(ch, start, newLength)); // Category
			
			}
		}
	}
}


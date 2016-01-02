/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JMenuBar;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import org.jvnet.flamingo.ribbon.JRibbon;

import utility.java.MaptoProperties;
import utility.xml.SAXParserPool;

import imageviewer.system.SaveStack;
import imageviewer.system.SaveStackListener;
import imageviewer.tools.Tool;

import imageviewer.ui.annotation.Selectable;
import imageviewer.ui.annotation.ShapeSelectionModel;
import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.undo.ExtendedUndoManager;

// =======================================================================
// Class for remembering and referencing global application context,
// such as the last directory that was opened, an undo/redo stack, the
// current tool, etc.

public class ApplicationContext implements ApplicationConstants, SaveStackListener {

	private static final String DEFAULT_CONFIG=new String("config/config.xml");
	private static final Logger LOG=Logger.getLogger("imageviewer.config");
	private static final ApplicationContext CONTEXT=new ApplicationContext();

	// =======================================================================

	HashMap properties=new HashMap();
	UndoableEditSupport ues=new UndoableEditSupport();
	ExtendedUndoManager eum=new ExtendedUndoManager();
	JMenuBar applicationMenuBar=null;
	JRibbon applicationRibbon=null;
	ShapeSelectionModel ssm=new ShapeSelectionModel();

	private ApplicationContext() {initialize(DEFAULT_CONFIG);}
	private ApplicationContext(String configFile) {initialize(configFile);}

	private void initialize(String configFile) {
		
		try {
	    ApplicationContextHandler ach=new ApplicationContextHandler();
	    SAXParser parser=SAXParserPool.getSAXParser();
	    InputStream is=new FileInputStream(configFile);
	    InputSource aSource=new InputSource(is);
	    parser.parse(aSource,ach);
	    SAXParserPool.releaseSAXParser(parser);
	    is.close();
			try {
				GraphicsConfiguration gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
				properties.put(SCREEN_RESOLUTION,gc.getBounds());
			} catch (HeadlessException hexc) {}
	    String jvmVersion=System.getProperty("java.version");
	    properties.put(JAVA_VERSION,jvmVersion.substring(0,3));
	    LOG.info("Application context/config created ("+configFile+")");
		} catch (FileNotFoundException exc) {
	    LOG.error("Specified filename could not be accessed: "+configFile);
		} catch (Exception exc) {
	    LOG.error("Error attempting to parse file for configuration: "+configFile);
	    exc.printStackTrace();
		}
		
		// Initialize the undo manager and listeners for the application.

		Object undoLevel=getProperty("UNDO_LEVEL");
		eum.setLimit((undoLevel!=null) ? ((Integer)undoLevel).intValue() : 15);
		ues.addUndoableEditListener(eum);
		setProperty(ApplicationContext.MOVE_IN_PROGRESS,new Boolean(false));
		SaveStack.getInstance().addListener(this);
	}
	
	// =======================================================================

	public static ApplicationContext getContext() {return CONTEXT;}

	public static Tool getCurrentTool() {return CONTEXT.getTool();}
	public static void setCurrentTool(Tool x) {CONTEXT.setTool(x);}

	public static void postEdit(UndoableEdit x) {CONTEXT.setEdit(x);}
	public static void undo() {CONTEXT.doUndo();}
	public static void redo() {CONTEXT.doRedo();}

	// =======================================================================

	public void saveStackUpdate(int stackSize) {

		MenuAction ma=MenuAction.ACTIONS.get("Save");
		if (ma!=null) ma.setEnabled((stackSize==0) ? false : true);
	}

	// =======================================================================

	public Object getProperty(String key) {return properties.get(key);}
	public Object setProperty(String key, Object o) {return properties.put(key,o);}

	public Properties getApplicationContextProperties() {return MaptoProperties.mapToProperties(properties);}
	public Tool getTool() {return (Tool)properties.get("_CURRENT_TOOL");}
	public JMenuBar getApplicationMenuBar() {return applicationMenuBar;}

	public void setTool(Tool x) {properties.put("_CURRENT_TOOL",x);}
	public void setApplicationMenuBar(JMenuBar x) {applicationMenuBar=x;}
	public void setApplicationRibbon(JRibbon x) {applicationRibbon=x;}

	// =======================================================================

	public void setLastActionStatus(int status) {properties.put(LAST_ACTION_STATUS,new Integer(status));}
	public int getLastActionStatus() {Integer i=(Integer)properties.get(LAST_ACTION_STATUS); properties.remove(LAST_ACTION_STATUS); return (i==null) ? -1 : i.intValue();}

	// =======================================================================
	// Handle basic undo/redo stack requirements in the application.
	// Determine how the menuActions associated with an undo/redo must
	// be enabled or disabled based on whether the undoManager says it
	// can perform a corresponding undo/redo. Also update whenever a
	// setEdit occurs to post a possible editable.

	public void setEdit(UndoableEdit x) {ues.postEdit(x); updateUndoActionStates();}
	public void removeComponentEdits(Component c) {eum.removeComponentEdits(c); updateUndoActionStates();}

	public void doUndo() {
		
		try {
	    if (eum.canUndo()) eum.undo();
		} catch (Exception exc) {
	    LOG.error("Unspecified error in undo stack: "+exc);
	    exc.printStackTrace();
		}
		updateUndoActionStates();
	}

	public void doRedo() {

		try {
	    if (eum.canRedo()) eum.redo();
		} catch (Exception exc) {
	    LOG.error("Unspecified error in redo stack: "+exc);
	    exc.printStackTrace();
		}
		updateUndoActionStates();
	}

	private void updateUndoActionStates() {

		boolean undoFlag=(eum.canUndo()) ? true : false;
		MenuAction ma=MenuAction.ACTIONS.get("Undo");
		if (ma!=null) ma.setEnabled(undoFlag);
		boolean redoFlag=(eum.canRedo()) ? true : false;
		ma=MenuAction.ACTIONS.get("Redo");
		if (ma!=null) ma.setEnabled(redoFlag);
	}

	// =======================================================================

	public void addSelection(Selectable s) {ssm.add(s);}
	public void addSelections(ArrayList selections) {ssm.add(selections);}
	public void removeSelection(Selectable s) {ssm.remove(s);}
	public void removeSelections(ArrayList selections) {ssm.remove(selections);}
	public void clearSelections() {ssm.clear();}
	
	public ArrayList<Selectable> getSelections() {return ssm.getCurrentSelections();}

	// =======================================================================
	// Parse the configuration file, and cast the property values in
	// accordance to how they are specified in the XML file.

	private class ApplicationContextHandler extends DefaultHandler {

		public ApplicationContextHandler() {super();}

		// =======================================================================

		public void startElement(String uri, String localname, String qname, Attributes attr) {
			
	    try {
				if (localname.compareTo("property")==0) {
					String propertyName=attr.getValue("name");
					String dataType=attr.getValue("type");
					if (dataType!=null) {
						Class c=Class.forName(dataType);
						Class[] args=new Class[] {String.class};
						Constructor constructor=null;
						try {
							constructor=c.getConstructor(args);
						} catch (NoSuchMethodException nsmex) {
							constructor=c.getConstructor();
						}
						String value=attr.getValue("value");
						if (value==null) {
							properties.put(propertyName,null);
						} else if ((value.length()==0)||(value.compareTo("undefined")==0)) {
							properties.put(propertyName,null);
						} else {
							Object[] param=new Object[] {value};
							properties.put(propertyName,constructor.newInstance(param));
						}
					} else {
						LOG.error("Error attempting to parse property at element <"+localname+">");
					}
				}
	    } catch (Exception exc) {
				LOG.error("Error attempting to parse property at element <"+localname+">");
				exc.printStackTrace();
	    }
		}
	}
}

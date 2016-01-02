/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.lang.reflect.Field;

import java.util.Stack;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.KeyStroke;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import imageviewer.tools.Tool;
import imageviewer.tools.ToolManager;
import imageviewer.tools.plugins.Plugin;
import imageviewer.tools.plugins.PluginManager;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.swing.event.BasicMenuActionListener;
import imageviewer.ui.swing.event.MenuActionListener;
import imageviewer.ui.swing.event.MenuActionListenerFactory;

import imageviewer.util.XMLUtil;

// =======================================================================

public class MenuReader {
	
	private static final Logger LOG=Logger.getLogger("imageViewer.config");
	
	public MenuReader() {}

	// =======================================================================

	public static void parseFile(String filename, JMenuBar menubar) {

		LOG.info("Loading menu configuration file: "+filename);
		try {
			MenuHandler handler=new MenuHandler(menubar);
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

	public static JMenu parseFile(String filename) {

		LOG.info("Loading menu configuration file: "+filename);
		try {
			MenuHandler handler=new MenuHandler(null);
			SAXParser parser=XMLUtil.getSAXParser();
			InputStream is=new FileInputStream(filename);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,handler);
			is.close();
			XMLUtil.releaseSAXParser(parser);
			return handler.getMenu();
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+filename);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+filename);
			exc.printStackTrace();
		}
		return null;
	}

	// =======================================================================
	// Convenience method to find a given JMenuItem from a toolbar.
	// Primarily used by plugins to figure out where to insert
	// themselves...Finds the first occurence of the given hierarchy.

	public static JMenuItem findMenu(JMenuBar menubar, String[] hierarchy) {

		for (int loop=0, n=menubar.getMenuCount(); loop<n; loop++) {
			JMenu rootMenu=menubar.getMenu(loop);
			if (rootMenu.getText().compareToIgnoreCase(hierarchy[0])==0) {
				if (hierarchy.length==1) return rootMenu;
				return findMenu(rootMenu,hierarchy,1);
			}
		}
		return null;
	}

	private static JMenuItem findMenu(JMenu menu, String[] hierarchy, int keyIndex) {

		for (int loop=0, n=menu.getItemCount(); loop<n; loop++) {
			JMenuItem jmi=menu.getItem(loop);
			if (jmi!=null) {
				if (jmi.getText().compareToIgnoreCase(hierarchy[keyIndex])==0) {
					if (keyIndex==(hierarchy.length-1)) return jmi;
					if (jmi instanceof JMenu) return findMenu((JMenu)jmi,hierarchy,keyIndex+1);
				}
			}
		}
		return null;
	}

	// =======================================================================
	// Convenience methods to print the contents of a menubar and the
	// menus themselves.  Used to validate a given menuReader's parse
	// of the XML file.

	protected static String printMenu(JMenu menu, int indent) {

		StringBuffer sb=new StringBuffer();
		for (int loop=0, n=menu.getItemCount(); loop<n; loop++) {
			JMenuItem mi=menu.getItem(loop);
			if (mi!=null) {
				for (int i=0; i<indent; i++) sb.append(" ");
				if (mi instanceof JMenu) {
					sb.append("| "+mi.getText()+"\n");
					sb.append(printMenu((JMenu)mi,indent+2)); 
				} else {
					sb.append("| "+mi.getText()+" ["+mi.getAccelerator()+"]\n");
				}
			} else {
				for (int i=0; i<indent; i++) sb.append(" ");
				sb.append("| ---\n");
			}
		}
		return sb.toString();
	}

	public static String printMenuBar(JMenuBar menubar) {

		StringBuffer sb=new StringBuffer();
		MenuElement[] menus=menubar.getSubElements();
		for (int loop=0; loop<menus.length; loop++) {
			MenuElement me=menus[loop];
			if (me instanceof JMenu) sb.append(((JMenu)me).getText()+"\n"+printMenu((JMenu)me,1)+"\n");
		}
		return sb.toString();
	}

	// =======================================================================

	public static class MenuHandler extends DefaultHandler {

		MenuAction ma=null;
		Stack<JMenu> menuStack=null;
		Stack<MenuActionListener> listenerStack=null;
		JMenuBar menubar=null;
		JMenu menu=null;
	
		// =======================================================================

		public MenuHandler(JMenuBar menubar) {super(); this.menubar=menubar;}

		public JMenu getMenu() {return menu;}

		public void startDocument() {

			menuStack=new Stack<JMenu>(); 
			listenerStack=new Stack<MenuActionListener>();
			listenerStack.push(MenuActionListenerFactory.getListener(BasicMenuActionListener.class));
		}

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			if (localname.compareTo("menu")==0) {

				// Determine if we have a submenu or not; push any current
				// menus on to the stack.  A (sub)menu item can have an icon
				// associated with it.

				if (menu!=null) menuStack.push(menu); 
				menu=new JMenu(attr.getValue("name")); 
				menu.add(Box.createRigidArea(new Dimension(0,2)));                     // For some reason, the layouts are starting at y=0, which looks bad.

				if (attr.getIndex("shortcut")>=0) {

					// Java does not allow us to lookup the correct keyCodes
					// without a keyEvent, so bypass this error by doing a
					// direct class access on the desired key; also allows us to
					// handle function key assignments.

					String mnemonic=attr.getValue("shortcut");
					try {
						Class c=KeyEvent.class;
						Field field=(c.getField("VK_"+mnemonic));
						int keyCode=field.getInt(c);
						if (keyCode!=KeyEvent.VK_UNDEFINED) menu.setMnemonic(keyCode);
					} catch (Exception exc) {
						LOG.error("Unknown key code for menu: "+mnemonic);
					}
				}

				menu.setRolloverEnabled(true);
				return;
			}

			if (localname.compareTo("file")==0) {
				if (menu!=null) menuStack.push(menu); 
				menu=parseFile(attr.getValue("filename"));
				return;
			} 

			if (localname.compareTo("command")==0) {

				String commandName=attr.getValue("name");

				boolean isInitiallyEnabled=(attr.getIndex("isInitiallyEnabled")>0) ? Boolean.parseBoolean(attr.getValue("isInitiallyEnabled")) : false;
				boolean useMenuIcon=(attr.getIndex("useMenuIcon")>0) ? Boolean.parseBoolean(attr.getValue("useMenuIcon")) : false;
				Icon ii=null;
				if (attr.getIndex("icon")>=0) {
					ii=new ImageIcon(attr.getValue("icon"));
					ii=(ii==null) ? new ImageIcon("resources/icons/Unknown.png") : new MinimumSizedIcon(ii);
				}
				ma=(ii==null) ? new MenuAction(commandName,attr.getValue("toggleType"),attr.getValue("buttonGroup"),isInitiallyEnabled,useMenuIcon) :
					new MenuAction(commandName,ii,attr.getValue("toggleType"),attr.getValue("buttonGroup"),isInitiallyEnabled,useMenuIcon);				
				if (attr.getIndex("shortcut")>=0) {
					String keySequence=attr.getValue("shortcut");
					KeyStroke ks=KeyStroke.getKeyStroke(keySequence);
					if (ks!=null) ((JMenuItem)(ma.getMenuItem())).setAccelerator(ks);
				}

				// If "isDefault" is set, parse the value; if it's true, then
				// set the menuitem to trigger the appropriate state.

				boolean isDefault=(attr.getIndex("isDefault")>0) ? Boolean.parseBoolean(attr.getValue("isDefault")) : false;
				if (isDefault) ma.getMenuItem().doClick();

				// Also, look and see if there's a value set for this menu
				// action in the application context (e.g., as part of a user
				// preference overriding the default). As the default for
				// things is false, only make a change if the boolean value is
				// set to true.

				// Note: We introduced underscores to separate the words in
				// the property name, so we need to do an extra conversion
				// from blank to underscore in order to go from command to
				// property name.

				String propName=commandName.trim().toUpperCase();
				propName=propName.replaceAll(" ","_");
				Object o=ApplicationContext.getContext().getProperty(propName);
				if (o!=null) {
					if (o instanceof Boolean) {
						boolean b=((Boolean)o).booleanValue();
						if (b) ma.getMenuItem().doClick();
					}
				}

				ma.setMenuActionListener(listenerStack.peek());
				MenuAction.ACTIONS.put(commandName,ma);
				menu.add(ma.getMenuItem());
				return;
			}

			if (localname.compareTo("eventListener")==0) {
				String className=attr.getValue("class");
				try {
					Class c=Class.forName(className);
					
					// Determine whether this is possibly a plugin or a tool, in
					// which case we need to look at the different managers to
					// see if an instance has already been created, and use that
					// instead...do a check in the following order: plugin,
					// tool, other.

					MenuActionListener mal=null;
					if (c.isAssignableFrom(Plugin.class)) {
						mal=(MenuActionListener)PluginManager.getPlugin(c);
					} else if (c.isAssignableFrom(Tool.class)) {
						mal=(MenuActionListener)ToolManager.getTool(c);
					} else {
						mal=MenuActionListenerFactory.getListener(c);
					}
					listenerStack.push(mal);
				} catch (Exception exc) {
					LOG.error("Error handling event listener: "+className);
					exc.printStackTrace();
				}
				return;
			}
			
			if (localname.compareTo("separator")==0) {menu.addSeparator(); return;}
		}

		// =======================================================================

		public void endElement(String uri, String localname, String qname) {

			if (localname.compareTo("command")==0) {ma=null; return;} 
			if (localname.compareTo("eventListener")==0) {if (listenerStack.size()!=1) listenerStack.pop();	return;}

			if ((localname.compareTo("menu")==0)||(localname.compareTo("file")==0)) {
				if (!menuStack.empty()) {
					JMenu nextMenu=menuStack.pop(); 
					if (menu!=null) nextMenu.add(menu);
					menu=nextMenu;
					return;
				} else {
					if (menubar!=null) {
						if (menu!=null) menubar.add(menu);
						menu=null;
					}
				}
			}
		}
	}
}


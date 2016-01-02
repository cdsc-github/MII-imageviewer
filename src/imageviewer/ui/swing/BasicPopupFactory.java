/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.PopupFactory;

// =======================================================================
/**
 * PoputFactory for building a BasicPopup. 
 */
public class BasicPopupFactory extends PopupFactory {

	public static final String BACKGROUND=new String("__BACKGROUND");

	// =======================================================================

	private final PopupFactory storedFactory;

	private BasicPopupFactory(PopupFactory storedFactory) {this.storedFactory=storedFactory;}
  
	/**
	 * Install this popup factory as THE one for our system. 
	 */
	public static void install() {  

		PopupFactory factory=PopupFactory.getSharedInstance();
		if (factory instanceof BasicPopupFactory) return;
		PopupFactory.setSharedInstance(new BasicPopupFactory(factory));
	}
	
	/**
	 * We're no longer THE popup factory to end all popup factories. 
	 */
	public static void uninstall() {
		PopupFactory factory=PopupFactory.getSharedInstance();
		if (!(factory instanceof BasicPopupFactory)) return;
		PopupFactory stored=((BasicPopupFactory)factory).storedFactory;
		PopupFactory.setSharedInstance(stored);
	}
    
	// =======================================================================
	/* Get ourselves a popup. Changed to ensure that the contents of a
	 * JPopupMenu are set to be non-opaque, otherwise the rendering goes
	 * bad for some reason.
	 * 
	 * @see javax.swing.PopupFactory#getPopup(java.awt.Component, java.awt.Component, int, int)
	 */

	public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {

		if (contents instanceof JPopupMenu) {
			Container c=(Container)contents;
			Component[] comp=c.getComponents();
			for (int i=0; i<comp.length; i++) {
				Component child=comp[i];
				if (child instanceof JComponent) ((JComponent)child).setOpaque(false);
			}
		}
		Popup popup=super.getPopup(owner,contents,x,y);
		return BasicPopup.getInstance(owner,contents,x,y,popup);
	}
}

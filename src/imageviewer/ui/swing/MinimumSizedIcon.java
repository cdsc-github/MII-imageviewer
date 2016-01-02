/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;

// =======================================================================
/**
 * Adapted from JGoodies. 
 */
public class MinimumSizedIcon implements Icon {

	protected static final Dimension DEFAULT_SIZE=new Dimension(22,22);

	// =======================================================================
	
	Icon icon=null;
	int  width=0, height=0, xOffset=0, yOffset=0;
	
	/**
	 * Default Constructor. 
	 */
	public MinimumSizedIcon() {this(null);}
	
	/**
	 * Create a new icon.
	 * 
	 * @param icon
	 */
	public MinimumSizedIcon(Icon icon) {

		Dimension minimumSize=DEFAULT_SIZE;
		this.icon=icon;
		int iconWidth=(icon==null) ? 0 : icon.getIconWidth();
		int iconHeight=(icon==null) ? 0 : icon.getIconHeight();
		width=Math.max(iconWidth,minimumSize.width);
		height=Math.max(iconHeight,minimumSize.height);
		xOffset=Math.max(0,(width-iconWidth)/2);
		yOffset=Math.max(0,(height-iconHeight)/2);
	}

	public MinimumSizedIcon(Icon icon, Dimension minimumSize) {

		this.icon=icon;
		int iconWidth=(icon==null) ? 0 : icon.getIconWidth();
		int iconHeight=(icon==null) ? 0 : icon.getIconHeight();
		width=Math.max(iconWidth,minimumSize.width);
		height=Math.max(iconHeight,minimumSize.height);
		xOffset=Math.max(0,(width-iconWidth)/2);
		yOffset=Math.max(0,(height-iconHeight)/2);
	}

	// =======================================================================

	/**
	 * Get our Icon. 
	 * 
	 * @return - Icon
	 */
	public Icon getIcon() {return icon;}
	
	/**
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {if (icon!=null)	icon.paintIcon(c,g,(x+xOffset),(y+yOffset));}

	
	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	public int getIconHeight() {return height;}
	
	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	public int getIconWidth()	{return width;}
			
}

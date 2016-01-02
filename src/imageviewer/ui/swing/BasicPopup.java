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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;

import java.awt.image.BufferedImage;

import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.Popup;

import javax.swing.border.Border;

import imageviewer.ui.swing.border.DropShadowRectangularBorder;

// =======================================================================
/**
 * A Basic popup, adapated from JGoodies L&F.  
 * 
 * http://www.jgoodies.com/freeware/looks/index.html
 */
public class BasicPopup extends Popup {

	private static final Border BORDER=DropShadowRectangularBorder.getInstance();
	private static final ArrayList<BasicPopup> CACHE=new ArrayList<BasicPopup>(5);

	private static BufferedImage LAST_IMAGE=null;
	private static Rectangle LAST_REGION=new Rectangle();
	private static Robot ROBOT=null;
	private static boolean IS_LINUX=false;

	static {

		try {if (ROBOT==null) ROBOT=new Robot();} catch (Exception exc) {exc.printStackTrace();}
		String osName=System.getProperty("os.name");
		if (osName.compareToIgnoreCase("Linux")==0) IS_LINUX=true;
	}

	// =======================================================================

	Component owner=null, contents=null;
	Container heavyWeightContainer=null;
	Border oldBorder=null;
	boolean oldOpaque=false;
	int x=0, y=0;
	Popup popup=null;

	// =======================================================================
	/**
	 * Get an instance of our popup.  We cache these to improve performance.
	 * 
	 * @param owner
	 * @param contents
	 * @param x
	 * @param y
	 * @param delegate
	 * @return
	 */
	public static Popup getInstance(Component owner, Component contents, int x, int y, Popup delegate) {

		synchronized (BasicPopup.class) {
			BasicPopup result=(CACHE.size()>0) ? (BasicPopup)CACHE.remove(0) : (new BasicPopup());
			result.reset(owner,contents,x,y,delegate);
			return result;
		}
	}

	/**
	 * Return a popup to the fold.  Use this to achive some caching benefits.  
	 * 
	 * @param popup
	 */
	private static void recycle(BasicPopup popup) {synchronized (BasicPopup.class) {if (CACHE.size()<5) CACHE.add(popup);}}
    
	// =======================================================================
	/**
	 * Changed to also flush the buffered image stored as part of the
	 * parent component; make sure that we don't tax the memory.
	 * 
	 * @see javax.swing.Popup#hide()
	 */
	public void hide() {

		if (contents==null) return;
		JComponent parent=(JComponent)contents.getParent();
		popup.hide();
		// if (parent.getBorder()==BORDER) {
			// parent.setBorder(oldBorder);
			parent.setOpaque(oldOpaque);
			oldBorder=null;
			BufferedImage bi=(BufferedImage)parent.getClientProperty(BasicPopupFactory.BACKGROUND);
			if (bi!=null) bi.flush();
			parent.putClientProperty(BasicPopupFactory.BACKGROUND,null);
			// }
		heavyWeightContainer=null;
		owner=null;
		contents=null;
		popup=null;
		recycle(this);
	}

	// =======================================================================
	/**
	 * Show our popup.
	 * 
	 * @see javax.swing.Popup#show()
	 */
	public void show() {snapshot(); popup.show();}

	// =======================================================================

	private void reset(Component owner, Component contents, int x, int y, Popup popup) {

		this.contents=contents;
		this.owner=owner;
		this.popup=popup;
		this.x=x;
		this.y=y;

		if (owner instanceof JComboBox) return;
		for (Container p=contents.getParent(); p!=null; p=p.getParent()) {
			if ((p instanceof JWindow)||(p instanceof Panel)||(IS_LINUX)) {
				p.setBackground(contents.getBackground());
				heavyWeightContainer=p;
				break;
			}
		}
		JComponent parent=(JComponent)contents.getParent();
		oldOpaque=parent.isOpaque();
		// oldBorder=parent.getBorder();
		parent.setOpaque(false);
		// parent.setBorder(BORDER);
		if (heavyWeightContainer!=null) {
			heavyWeightContainer.setSize(heavyWeightContainer.getPreferredSize());
		} else {
			parent.setSize(parent.getPreferredSize());
		}
	}

	// =======================================================================
	// This method largely differs somewhat from the JGoodies technique;
	// instead of grabbing only the drop shadow portions of the screen,
	// we grab the entire region encompsasing the actual popup.  By
	// doing so, we can potentially redraw the background region of the
	// popup with tranlucency (this drawing would be done in the
	// specific UI component). Also forces the computation of the
	// "redrawn" region to be done in the border components, etc.

	private void snapshot() {

		try {

			Dimension size=(heavyWeightContainer==null) ? contents.getPreferredSize() : heavyWeightContainer.getPreferredSize();
			Rectangle r=new Rectangle(x,y,size.width,size.height);
			BufferedImage bi=ROBOT.createScreenCapture(r);

			// Because sometimes things happen too quickly, we need to
			// consider if the affected region is "dirty" and if so, we need
			// to use the previous region; we do this check by looking at
			// the intersection of the immediately prior region and the
			// current region.

			Rectangle intersection=LAST_REGION.intersection(r);
			if (!intersection.isEmpty()) {
				Graphics g=bi.getGraphics();
				BufferedImage overlap=LAST_IMAGE.getSubimage(intersection.x-LAST_REGION.x,intersection.y-LAST_REGION.y,intersection.width,intersection.height);
				g.drawImage(overlap,intersection.x-x,intersection.y-y,null);
				overlap.flush();
				g.dispose();
			}
			((JComponent)contents).putClientProperty(BasicPopupFactory.BACKGROUND,null);
			((JComponent)contents).putClientProperty(BasicPopupFactory.BACKGROUND,bi);
			LAST_REGION=r;
			if (LAST_IMAGE!=null) LAST_IMAGE.flush();
			LAST_IMAGE=bi;
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}

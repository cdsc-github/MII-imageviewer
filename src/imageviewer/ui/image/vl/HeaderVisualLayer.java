/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.vl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

import imageviewer.model.Image;
import imageviewer.model.dl.HeaderDataLayer;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.DataPanel;
import imageviewer.ui.VisualLayerRenderer;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.TextRenderer;

import imageviewer.util.StringUtilities;

// =======================================================================

public class HeaderVisualLayer implements VisualLayerRenderer {

	private static final Font DEFAULT_FONT=new Font("Tahoma",Font.PLAIN,9);

	private static final int TOP_LEFT=0;
	private static final int TOP_RIGHT=1;
	private static final int BOTTOM_LEFT=2;
	private static final int BOTTOM_RIGHT=3;

	private static BufferedImage MARKED_ICON=null;

	static {
		try {
			MARKED_ICON=ImageIO.read(new File("resources/icons/bookmark.png"));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	// =======================================================================

	public void paintLayer(Object dataLayer, DataPanel dp, Graphics g) {

		Boolean showHeader=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.DISPLAY_IMAGE_INFORMATION);
		if (!showHeader.booleanValue()) return;
		if (dataLayer instanceof HeaderDataLayer) {
			HeaderDataLayer hdl=(HeaderDataLayer)dataLayer;
			renderText(dp,g,hdl.getTopLeftText(),TOP_LEFT);
			renderText(dp,g,hdl.getTopRightText(),TOP_RIGHT);
			renderText(dp,g,hdl.getBottomLeftText(),BOTTOM_LEFT);
			renderText(dp,g,hdl.getBottomRightText(),BOTTOM_RIGHT);
			if (dp instanceof ImagePanel) {
				String isMarked=(String)((ImagePanel)dp).getSource().getProperties().get(Image.MARKED);
				if (isMarked!=null) {
					renderImage(dp,g,MARKED_ICON,BOTTOM_RIGHT);
				}
			}
		}
	}

	// =======================================================================

	private void renderImage(DataPanel dp, Graphics g, BufferedImage bi, int location) {

		int x=0, y=0;
		int panelWidth=dp.getWidth();
		int panelHeight=dp.getHeight();
		switch (location) {
		  case TOP_LEFT: case BOTTOM_LEFT: x=5; break;
		  case TOP_RIGHT: case BOTTOM_RIGHT: x=panelWidth-3-bi.getWidth(); break;
		}
		switch (location) {
		  case TOP_LEFT: case TOP_RIGHT: y=bi.getHeight(); break;
		  case BOTTOM_LEFT: case BOTTOM_RIGHT: y=panelHeight-3-bi.getHeight(); break;
		}
		((Graphics2D)g).drawImage(bi,null,x,y);
	}

	// =======================================================================

	private void renderText(DataPanel dp, Graphics g, String[] text, int location) {

		if (text==null) return;
		FontMetrics fm=g.getFontMetrics(DEFAULT_FONT);
		int lineHeight=fm.getHeight();
		int panelWidth=dp.getWidth();
		int panelHeight=dp.getHeight();
		g.setFont(DEFAULT_FONT);
		g.setColor(Color.white);
		for (int loop=0, counter=0; loop<text.length; loop++) {
			String s=text[loop];
			if ((s!=null)&&(s.length()!=0)) {
				String cleanLine=StringUtilities.replaceAndCapitalize(s,true);
				int stringWidth=fm.stringWidth(cleanLine);
				int x=0, y=0;

				// Compute the x,y coordinate of the text dependent on what
				// portion of the panel is being shown. Note that the bottom
				// coordinates are probably not quite right, because we don't
				// know about the nulls when we count...probably need to look
				// at the text[] and discount nulls to get a hint before
				// computing everything?

				switch (location) {
			    case TOP_LEFT: case BOTTOM_LEFT: x=5; break;
			    case TOP_RIGHT: case BOTTOM_RIGHT: x=panelWidth-3-stringWidth; break;
				}
			  switch (location) {
			    case TOP_LEFT: case TOP_RIGHT: y=(lineHeight*(1+counter)); break;
			    case BOTTOM_LEFT: case BOTTOM_RIGHT: y=panelHeight-6-((text.length-counter-1)*lineHeight); break;
			  }
				TextRenderer.drawString(dp,g,cleanLine,x,y,true); 	 // Render with an outline to improve readability
				counter++;
			}
		}
	}
}

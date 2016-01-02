/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.PrintGraphics;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import java.awt.geom.AffineTransform;
import java.awt.print.PrinterGraphics;

import java.lang.reflect.Field;

import java.security.AccessController;

import javax.swing.JComponent;

import sun.security.action.GetPropertyAction;

// =======================================================================
// Convenience class to handle some issues between Java 5 and Java 6.
// Java 6 enables a new type of text rendeirng, specific for LCDs and
// CRTs, which looks a whole lot better than previous text
// anti-aliasing. However, prior versions don't do this...

public class TextRenderer {

	private static final boolean AA_TEXT;
  private static final boolean AA_TEXT_DEFINED;

	private static final Object AA_TEXT_PROPERTY_KEY=new StringBuffer("AATextPropertyKey");

	private static final FontRenderContext DEFAULT_FRC=new FontRenderContext(null,false,false);
	private static final FontRenderContext AA_FRC;

	private static final String AA_TEXT_JDK6=new String("VALUE_TEXT_ANTIALIAS_LCD_HRGB");
	private static final String AA_TEXT_JDK5=new String("VALUE_TEXT_ANTIALIAS_ON");
	private static final String AA_TEXT_LCD=new String("KEY_TEXT_LCD_CONTRAST");

	public static Object AA_VALUE;

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC,1.0f);
	private static final Color DARK_GRAY_ALPHA=new Color(32,32,32,128);

	static {

		Object aa=AccessController.doPrivileged(new GetPropertyAction("swing.aatext"));
		AA_TEXT_DEFINED=(aa!=null);
		AA_TEXT="true".equals(aa);
		AA_FRC=new FontRenderContext(null,true,false);
		String jvmVersion=(System.getProperty("java.version")).substring(0,3);
		try {
			Class c=RenderingHints.class;
			Field f=(jvmVersion.compareTo("1.6")==0) ? c.getField(AA_TEXT_JDK6) : c.getField(AA_TEXT_JDK5);
			AA_VALUE=f.get(c);
		} catch (Exception exc) {
			exc.printStackTrace();
			AA_VALUE=RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
		} 
	}

	// =======================================================================

	public static Object getTextAntiAliasingValue() {return AA_VALUE;}

	// =======================================================================

	private static boolean isPrinting(Graphics g) {return (g instanceof PrinterGraphics || g instanceof PrintGraphics);}

	// =======================================================================

	private static boolean drawTextAntialiased(JComponent c) {
	
		if (!AA_TEXT_DEFINED) return (c!=null) ? (c.getClientProperty(AA_TEXT_PROPERTY_KEY) !=null ) ? ((Boolean)c.getClientProperty(AA_TEXT_PROPERTY_KEY)).booleanValue() : false : false;
		return AA_TEXT;
	}

	// =======================================================================

	public static Graphics2D getGraphics2D(Graphics g) {

		if (g instanceof Graphics2D) {
			return (Graphics2D)g;
		} else if (g instanceof sun.print.ProxyPrintGraphics) {
			return (Graphics2D)(((sun.print.ProxyPrintGraphics)g).getGraphics());
		} else {
			return null;
		}
	}

	// =======================================================================

	public static void drawString(JComponent c, Graphics g, String text, int x, int y) {drawString(c,g,text,x,y,false);}

	public static void drawString(JComponent c, Graphics g, String text, int x, int y, boolean outline) {

		if (text==null || text.length()<=0) return;
		if (isPrinting(g)) {
			Graphics2D g2d=getGraphics2D(g);
			if (g2d!=null) {
				TextLayout layout=new TextLayout(text,g2d.getFont(),DEFAULT_FRC);
				layout.draw(g2d,x,y);
				return;
			}
		} 

		if (drawTextAntialiased(c) && (g instanceof Graphics2D)) {
			Graphics2D g2=(Graphics2D)g;
			Object oldAAValue=g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);			
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,AA_VALUE);
			Composite oldComposite=g2.getComposite();
			g2.setComposite(DEFAULT_AC);

			// Render a black outline to the text with an alpha composite.
			// This probably needs some tweaking, as I'm sure there must be
			// better ways to do this; right now, the text outline is
			// somewhat ragged.

			if (outline) {
				Color oldColor=g2.getColor();
				g2.setColor(DARK_GRAY_ALPHA);
				g2.drawString(text,x-1,y-1);
				g2.drawString(text,x+1,y-1);
				g2.drawString(text,x+1,y+1);
				g2.drawString(text,x-1,y+1);
				g2.setColor(oldColor); 
			}
			g2.drawString(text,x,y);
			g2.setComposite(oldComposite);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,oldAAValue);
		}	else {
			g.drawString(text,x,y);
		}
	}

	// =======================================================================
	/**
     * Returns the width of the passed in String.
     *
     * @param c JComponent that will display the string, may be null
     * @param fm FontMetrics used to measure the String width
     * @param string String to get the width of
     */

    public static int stringWidth(JComponent c, FontMetrics fm, String string) {return fm.stringWidth(string);}

	// =======================================================================
	/**
	 * Draws the string at the specified location underlining the specified
	 * character.
	 *
	 * @param c JComponent that will display the string, may be null
	 * @param g Graphics to draw the text to
	 * @param text String to display
	 * @param underlinedIndex Index of a character in the string to underline
	 * @param x X coordinate to draw the text at
	 * @param y Y coordinate to draw the text at
	 */

	public static void drawStringUnderlineCharAt(JComponent c, Graphics g, String text, int underlinedIndex, int x, int y) {

		drawString(c,g,text,x,y);
		if (underlinedIndex>=0 && underlinedIndex<text.length()) {
			FontMetrics fm=g.getFontMetrics();
			int underlineRectX=x+stringWidth(c,fm, text.substring(0,underlinedIndex));
			int underlineRectY=y;
			int underlineRectWidth=fm.charWidth(text.charAt(underlinedIndex));
			int underlineRectHeight=1;
			g.fillRect(underlineRectX,underlineRectY+1,underlineRectWidth,underlineRectHeight);
		}
	}
}

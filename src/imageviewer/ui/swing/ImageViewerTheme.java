/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

// =======================================================================

/**
 * ImageViewerClient Theme for our Look and Feel.
 * 
 */
public class ImageViewerTheme extends DefaultMetalTheme {

	protected FontUIResource plainFont=new FontUIResource("Tahoma",Font.PLAIN,11);
	protected FontUIResource boldFont=new FontUIResource("Tahoma",Font.PLAIN,11);

	private final ColorUIResource primary1=new ColorUIResource(0,0,0);
	private final ColorUIResource primary2=new ColorUIResource(213,211,209); 
	private final ColorUIResource primary3=new ColorUIResource(213,211,209);

	private final ColorUIResource secondary1=new ColorUIResource(167,165,163); 
	private final ColorUIResource secondary2=new ColorUIResource(167,165,163);
	private final ColorUIResource secondary3=new ColorUIResource(236,233,216);

	private final ColorUIResource menuSelected=new ColorUIResource(255,255,255);

	private final ColorUIResource darkShadow=new ColorUIResource(0,0,0);

	// =======================================================================

	public ImageViewerTheme() {

		GraphicsEnvironment env=GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] fontNames=env.getAvailableFontFamilyNames();
		for (int loop=0; loop<fontNames.length; loop++) {
			if (fontNames[loop].compareToIgnoreCase("Segoe UI")==0) {
				plainFont=new FontUIResource("Segoe UI",Font.PLAIN,11);
				boldFont=new FontUIResource("Segoe UI",Font.PLAIN,11);
				return;
			}
		}
		plainFont=new FontUIResource("Tahoma",Font.PLAIN,11);
		boldFont=new FontUIResource("Tahoma",Font.PLAIN,11);
	}

	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getControlTextFont()
	 */
	public FontUIResource getControlTextFont() {return plainFont;}

	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getMenuTextFont()
	 */
	public FontUIResource getMenuTextFont() {return plainFont;}

	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getName()
	 */
	public String getName() {return new String("imageViewer theme");}

	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getPrimary1()
	 */
	protected ColorUIResource getPrimary1() {return primary1;}
	
	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getPrimary2()
	 */
	protected ColorUIResource getPrimary2() {return primary2;}
	
	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getPrimary3()
	 */
	protected ColorUIResource getPrimary3() {return primary3;}

	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getSecondary1()
	 */
	protected ColorUIResource getSecondary1() {return secondary1;}
	
	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getSecondary2()
	 */
	protected ColorUIResource getSecondary2() {return secondary2;}
	
	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getSecondary3()
	 */
	protected ColorUIResource getSecondary3() {return secondary3;}

	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getMenuSelectedForeground()
	 */
	public ColorUIResource getMenuSelectedForeground() {return menuSelected;}
	
	/**
	 * @see javax.swing.plaf.metal.MetalTheme#getControlDarkShadow()
	 */
	public ColorUIResource getControlDarkShadow() {return darkShadow;}
}

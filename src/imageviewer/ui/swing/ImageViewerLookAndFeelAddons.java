/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.jdesktop.swingx.plaf.metal.MetalLookAndFeelAddons;

// =======================================================================

public class ImageViewerLookAndFeelAddons extends MetalLookAndFeelAddons {

	public void initialize() {super.initialize(); loadDefaults(getDefaults());}
  public void uninitialize() {super.uninitialize(); unloadDefaults(getDefaults());}
	
  private Object[] getDefaults() {
   
		Object[] defaults=new Object[] {"JXDatePicker.arrowDown.image",new ImageIcon("resources/icons/swing/calendar.png"),
																		"JXMonthView.font",((ImageViewerLookAndFeel)UIManager.getLookAndFeel()).getControlTextFont(),
																		//"JXMonthView.monthUpFileName","resources/icons/swing/larowsvg.png",
																		//"JXMonthView.monthDownFileName","resources/icons/swing/rarowsvg.png",
																		"JXMonthView.boxPaddingX",3,
																		"JXMonthView.boxPaddingY",3
		};
    return defaults;
  }
}

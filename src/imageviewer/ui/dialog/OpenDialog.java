/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

// =======================================================================

public class OpenDialog extends JPanel {

	private static OpenDialog INSTANCE=null;

	public static OpenDialog getInstance() {if (INSTANCE==null) INSTANCE=new OpenDialog(); return INSTANCE;}

	// =======================================================================

	OpenDialogPanel odp=new OpenDialogPanel();
	OpenTrackingPanel otp=new OpenTrackingPanel();
	OpenOptionsPanel oop=new OpenOptionsPanel();

	private OpenDialog() {

		super();	
		JTabbedPane tp=new JTabbedPane();	
		tp.add("Image selection",odp);
		tp.add("Image processing",new JPanel());
		tp.add("Image tracking",otp);
		tp.add("Advanced options",oop);
		add(tp); 
	}

	// =======================================================================

	public OpenTrackingPanel getOpenTrackingPanel() {return otp;}
	public OpenDialogPanel getOpenDialogPanel() {return odp;}
} 

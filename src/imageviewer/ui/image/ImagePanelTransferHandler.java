/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image;

import java.awt.Graphics2D;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable; 
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

// =======================================================================

public class ImagePanelTransferHandler extends TransferHandler implements Transferable {

	public static DataFlavor IMAGE_PANEL_MIME_FLAVOR;
	public static final ImagePanelTransferHandler INSTANCE=new ImagePanelTransferHandler();

	static {try {IMAGE_PANEL_MIME_FLAVOR=new DataFlavor("image/x-image-panel;class="+ImagePanel.class.getCanonicalName());} catch (ClassNotFoundException cnfe) {}}

	public static ImagePanelTransferHandler getInstance() {return INSTANCE;}

	// =======================================================================

  ImagePanel ip=null;
	BufferedImage bi=null;

	private ImagePanelTransferHandler() {}

	// =======================================================================

  public boolean canImport(JComponent c, DataFlavor flavor[]) {return false;}
  public boolean importData(JComponent c, Transferable t) {return false;}

  public int getSourceActions(JComponent c) {return TransferHandler.COPY;}
  protected Transferable createTransferable(JComponent c) {if (c instanceof ImagePanel) {ip=(ImagePanel)c; return this;} else return null;}

	protected void exportDone(JComponent c, Transferable t, int action) {

		// Remember to cleanup after the export is done so that we don't
		// retain anything in memory otherwise things might pile up...

		super.exportDone(c,t,action);	
		if (bi!=null) {bi.flush(); bi=null;}
		ip=null;
		if (c instanceof ImagePanel) ((ImagePanel)c).setHighlight(false);
	}

	// =======================================================================
	// The transferable interface, in support of being able to drag 'n drop.

  public Object getTransferData(DataFlavor flavor) {
		
		if (flavor==DataFlavor.imageFlavor) {
			ip.setHighlight(false);
			bi=new BufferedImage(ip.getWidth(),ip.getHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2=bi.createGraphics();
			ip.paintComponent(g2);
			return bi;
		} else if (flavor.isMimeTypeEqual(IMAGE_PANEL_MIME_FLAVOR.getMimeType())) {
			return ip;
		} 
		return null;
  }

	public DataFlavor[] getTransferDataFlavors() {return new DataFlavor[] {DataFlavor.imageFlavor,IMAGE_PANEL_MIME_FLAVOR};}
	public boolean isDataFlavorSupported(DataFlavor flavor) {return ((flavor==DataFlavor.imageFlavor)||(flavor.isMimeTypeEqual(IMAGE_PANEL_MIME_FLAVOR.getMimeType())));}

}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog.ps;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class PSThumbnailTransferHandler extends TransferHandler implements Transferable {

	public static DataFlavor PSTHUMBNAIL_PANEL_MIME_FLAVOR;
	public static final PSThumbnailTransferHandler INSTANCE=new PSThumbnailTransferHandler();

	static {try {PSTHUMBNAIL_PANEL_MIME_FLAVOR=new DataFlavor("image/x-psthumbnail-panel;class="+PSThumbnailPanel.class.getCanonicalName());} catch (ClassNotFoundException cnfe) {}}

	public static PSThumbnailTransferHandler getInstance() {return INSTANCE;}

	// =======================================================================

	PSThumbnailPanel pstp=null;

	private PSThumbnailTransferHandler() {}

	// =======================================================================

  public boolean canImport(JComponent c, DataFlavor flavors[]) {return false;}
  public boolean importData(JComponent c, Transferable t) {return false;}

	public int getSourceActions(JComponent c) {return TransferHandler.COPY;}
  protected Transferable createTransferable(JComponent c) {if (c instanceof PSThumbnailPanel) {pstp=(PSThumbnailPanel)c; return this;} else return null;}

	protected void exportDone(JComponent c, Transferable t, int action) {pstp=null; super.exportDone(c,t,action);}	

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		
		return (flavor.isMimeTypeEqual(PSTHUMBNAIL_PANEL_MIME_FLAVOR.getMimeType())) ? pstp : null;
  }

	public DataFlavor[] getTransferDataFlavors() {return new DataFlavor[] {PSTHUMBNAIL_PANEL_MIME_FLAVOR};}
	public boolean isDataFlavorSupported(DataFlavor flavor) {return flavor.isMimeTypeEqual(PSTHUMBNAIL_PANEL_MIME_FLAVOR.getMimeType());}

}

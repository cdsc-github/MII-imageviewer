/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog.ps;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Point;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable; 

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import imageserver.model.AssociablePresentationState;
import imageserver.model.AssociatedData;
import imageserver.model.Series;

import imageviewer.model.Image;
import imageviewer.model.PresentationState;

import imageviewer.system.ImageViewerClientNode;
import imageviewer.system.Saveable;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.BasicImagePanel;
import imageviewer.ui.image.ImagePanel;

// =======================================================================

public class PSThumbnailPanel extends JPanel implements Saveable, DropTargetListener, DragSourceListener, DragGestureListener, MouseListener, MouseMotionListener {

	private static final AffineTransform IDENTITY=new AffineTransform();

	private static final Color SELECT_COLOR=UIManager.getColor("PSThumbnail.selectColor");
	private static final Color DRAG_HILITE_COLOR=UIManager.getColor("PSThumbnail.dragHighlightColor");

	private static final SimpleDateFormat DATE_FORMAT_SRC=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT_TRG=new SimpleDateFormat("MMMM d, yyyy h:mm aa");

	private static int COUNTER=0;

	// =======================================================================

	boolean thumbnailSelected=false, saved=true, reorderSelected=false, editable=false, markedForDeletion=false;
	BufferedImage bi=null;
	PresentationState ps=null;
	Image source=null;
	DragSource ds=null;
	DropTarget dt=null;
	AssociatedData ad=null;
	int id=COUNTER++;

	public PSThumbnailPanel(BasicImagePanel bip) {

		// Create a thumbnail image for the image panel, toggling the
		// header information off.  The target size for this image is 128
		// x 128 pixels, so take the current size of the panel and compute
		// a scale factor.

		super();
		bi=new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);
		Boolean showHeader=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.DISPLAY_IMAGE_INFORMATION);
		Boolean showGrid=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.DISPLAY_GRID);
		ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_IMAGE_INFORMATION,new Boolean(false));
		ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_GRID,new Boolean(false));
		Graphics2D g2=bi.createGraphics();
		g2.scale(128/(double)bip.getWidth(),128/(double)bip.getHeight());
		if (bip instanceof ImagePanel) {
			((ImagePanel)bip).paintComponent(g2,false);
		} else if (bip instanceof PSViewPanel) {
			((PSViewPanel)bip).paintComponent(g2);
		}
		ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_IMAGE_INFORMATION,showHeader);
		ApplicationContext.getContext().setProperty(ApplicationContext.DISPLAY_GRID,showGrid);

		setOpaque(true);
		setSize(128,128);
		setPreferredSize(new Dimension(128,128));
		setMaximumSize(new Dimension(128,128));

		addMouseListener(this);
		addMouseMotionListener(this);

		ds=new DragSource();
		ds.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE,this);
		setTransferHandler(PSThumbnailTransferHandler.getInstance());
		dt=new DropTarget(this,this);

	}

	// =======================================================================

	public String toString() {return new String("-"+id);}

	// =======================================================================

	public PresentationState getPresentationState() {return ps;}
	public AssociatedData getAssociatedData() {return ad;}
	public boolean isThumbnailSelected() {return thumbnailSelected;}
	public boolean isReorderSelected() {return reorderSelected;}
	public boolean isEditable() {return editable;}
	public boolean isSaved() {return saved;}
	public boolean isMarkedForDeletion() {return markedForDeletion;}
	public Image getSource() {return source;}
	
	public void setPresentationState(PresentationState x) {

		ps=x;
		try {																																
			String tooltip=new String("<html>");
			tooltip+=DATE_FORMAT_TRG.format(DATE_FORMAT_SRC.parse(ps.getPresentationCreationDate()+" "+ps.getPresentationCreationTime()));
			String author=ps.getPresentationCreatorName();
			if ((author!=null)&&(author.length()!=0)) tooltip+=new String("<br>Created by: "+author);
			String description=ps.getPresentationDescription();
			if ((description!=null)&&(description!="")) tooltip+=new String("<br>"+description);
			setToolTipText(tooltip+"</html>");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void setReorderSelected(boolean x) {reorderSelected=x;}
	public void setSaved(boolean x) {saved=x;}
	public void setEditable(boolean x) {editable=x;}
	public void setSource(Image x) {source=x;}
	public void setAssociatedData(AssociatedData x) {ad=x;}
	public void setMarkedForDeletion(boolean x) {markedForDeletion=x;}

	// =======================================================================
	// Try and make the selected thumbnail visible by scrolling the
	// containing panel.

	public void setThumbnailSelected(boolean x) {

		thumbnailSelected=x;
		if (x) {
			JViewport jv=PSDialogPanel.getInstance().getThumbnailScrollPane().getViewport();
			Component[] componentList=getParent().getComponents();
			for (int loop=0; loop<componentList.length; loop++) {
				Component c=componentList[loop];
				if (c==this) {
					int xCoord=129*loop;
					Point p=jv.getViewPosition();
					if (xCoord<(p.x+jv.getExtentSize().width)) return;
					PSDialogPanel.getInstance().getCollectionTable().scrollRectToVisible(new Rectangle(xCoord,p.y,129,129));
					return;
				}
			}
		}
	}

	// =======================================================================

	public void flush() {if (bi!=null) bi.flush(); bi=null; ps=null; if (source!=null); source.unload(); source=null; ad=null;}

	// =======================================================================

	protected void paintComponent(Graphics g) {

		g.setColor(Color.black);
		g.fillRect(0,0,127,127);
		super.paintComponent(g);
		Graphics2D g2=(Graphics2D)g;
		g2.drawRenderedImage(bi,IDENTITY);
		g2.setColor((thumbnailSelected) ? SELECT_COLOR : ((reorderSelected) ? DRAG_HILITE_COLOR : Color.darkGray));
		g2.drawRect(0,0,127,127);
	}

	// =======================================================================
	// Save the presentation state associated with this thumbnail panel.
	// We save at the series level

	public boolean save() {

		try {
			AssociablePresentationState aps=new AssociablePresentationState(ps);	
			aps.setAuthor(ps.getPresentationCreatorName());
			aps.setCreationTime(new Date());
			aps.setUpdatedTime(new Date());
			aps.setDescription(ps.getPresentationDescription());
			aps.setFormat(ps.getImageType());
			aps.setType(AssociatedData.AssocDataType.PRESENTATION_STATE.getTypeNumber());
			ImageViewerClientNode.getInstance().beginTransaction();
			Series s=ImageViewerClientNode.getInstance().getArchive().findInstance(ps.getReferencedImageKey()).getSeries();
			ad=ImageViewerClientNode.getInstance().getArchive().saveAssociatedData(s,aps);
			ImageViewerClientNode.getInstance().commitTransaction();
			saved=true;
			return true;
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return false;
	}

	// =======================================================================
	// Remove from the parent collection...

	public void delete() {

		PSCollection psc=(PSCollection)getParent();	
		psc.delete(this);
		if (saved) {

		}
	}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {

		PSDialogPanel psdp=PSDialogPanel.getInstance();
		PSThumbnailPanel currentSelection=psdp.getCurrentSelection();
		if (currentSelection==this) return;
		if (currentSelection!=null) currentSelection.setThumbnailSelected(false);
		psdp.setCurrentSelection(this);
		setThumbnailSelected(true); 

		// Update the information in the fields and set editablity based on save state.

		psdp.getViewPanel().setSource(source);
		psdp.getViewPanel().setPresentationState(ps);
		psdp.setViewPanelAuthor(ps.getPresentationCreatorName());
		psdp.setViewPanelTimestamp(ps.getPresentationCreationDate(),ps.getPresentationCreationTime());
		psdp.getInstance().setViewPanelDescription(ps.getPresentationDescription());
		psdp.setViewPanelEditable(!saved);
		psdp.getCollectionTable().repaint();
	}

	public void mouseDragged(MouseEvent e) {if (e.isControlDown()) getTransferHandler().exportAsDrag(this,e,TransferHandler.COPY);}
	public void mouseEntered(MouseEvent e) {requestFocusInWindow();}

	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}

	// =======================================================================
	// Drag and drop support for the panel...Absorb some weird exceptions
	// that can be thrown from the startDrag() method (drag-in-progress issues).

	public void dragGestureRecognized(DragGestureEvent dge) {

		if (!dge.getTriggerEvent().isControlDown()) return;
		try {
			ds.startDrag(dge,DragSource.DefaultLinkDrop,(PSThumbnailTransferHandler)getTransferHandler(),this);
		} catch (Exception exc) {}
	}

	public void dragEnter(DragSourceDragEvent dsde) {}
	public void dragOver(DragSourceDragEvent dsde) {}
  public void dropActionChanged(DragSourceDragEvent dsde) {}
  public void dragExit(DragSourceEvent dse) {}
	public void dragDropEnd(DragSourceDropEvent dsde) {}

	// =======================================================================
	// Drop target actions

  public void drop(DropTargetDropEvent dtde) {

		try {
			Transferable t=dtde.getTransferable();
			if (t.isDataFlavorSupported(PSThumbnailTransferHandler.PSTHUMBNAIL_PANEL_MIME_FLAVOR)) {
				PSThumbnailPanel pstp=(PSThumbnailPanel)t.getTransferData(PSThumbnailTransferHandler.PSTHUMBNAIL_PANEL_MIME_FLAVOR);
				PSCollection psc=(PSCollection)getParent();
				if ((pstp!=this)&&(pstp.getParent()==psc)) {
					reorderSelected=false;
					int index=psc.getComponentZOrder(this);
					psc.setComponentZOrder(pstp,index);
					psc.setReordered(true);
					psc.setSaved(false);
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					dtde.getDropTargetContext().dropComplete(true);
					PSDialogPanel.getInstance().getCollectionTable().repaint();
					return;
				} 
			} 
			dtde.rejectDrop();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

  public void dragEnter(DropTargetDragEvent dtde) {

		// Make sure that we aren't trying to drop onto the same
		// panel. Dummy!

		if (dtde.isDataFlavorSupported(PSThumbnailTransferHandler.PSTHUMBNAIL_PANEL_MIME_FLAVOR)) {
			try {
				Transferable t=dtde.getTransferable();
				PSThumbnailPanel pstp=(PSThumbnailPanel)t.getTransferData(PSThumbnailTransferHandler.PSTHUMBNAIL_PANEL_MIME_FLAVOR);
				if (pstp!=this) {
					reorderSelected=true;
					PSDialogPanel.getInstance().getCollectionTable().repaint();
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	public void dragExit(DropTargetEvent dte) {reorderSelected=false;	PSDialogPanel.getInstance().getCollectionTable().repaint();}

  public void dragOver(DropTargetDragEvent dtde) {}
  public void dropActionChanged(DropTargetDragEvent dtde) {}
}

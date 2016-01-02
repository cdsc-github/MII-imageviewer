/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.util.EventObject;

import javax.media.jai.InterpolationBicubic;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import javax.swing.JPanel;

import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.graphics.IndicatorBar;

import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.event.MenuActionEvent;
import imageviewer.ui.swing.event.MenuActionListener;

// =======================================================================

public class MagicLensTool extends ImagingTool implements Tool, MenuActionListener {

	protected static final int BORDER_EXTEND=100;
	protected static final InterpolationBicubic INTERP_BICUBIC=new InterpolationBicubic(8);
	protected static final AffineTransform IDENTITY=new AffineTransform();
	protected static final AlphaComposite HILITE_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);

	// =======================================================================

	RenderingProperties rp=null;
	RenderedImage ri=null; 
	PlanarImage borderedImage=null, magnifiedImage=null, lensImage=null;
	MagnifyLensPanel mlp=null;
	MagnifyHighlight mh=null;
	int lensWidth=75, lensHeight=75, px=0, py=0;
	double magnification=2, scale=2, translateX=0, translateY=0;
	boolean fixedLocation=false, showMagnifyHighlight=false;

	public MagicLensTool() {}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Magic lens");}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {execute(e);}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {prepare(e);}
	public void mouseReleased(MouseEvent e) {finish(e);}
	public void mouseWheelMoved(MouseWheelEvent e) {}

	// =======================================================================

	public void actionPerformed(MenuActionEvent mae) {

		ActionEvent ae=mae.getActionEvent();
		String actionCommand=ae.getActionCommand();
		if (actionCommand==null) {
			MenuAction ma=mae.getMenuAction();
			if (ma!=null) actionCommand=ma.getCommandName(); else return;
		}

		// Magnifying lens handling. Handles the specific options
		// associated with the magic lens.

		if (actionCommand==null) return;
		if (actionCommand.compareTo("Magnifier")==0) {
			ApplicationContext.getContext().setTool(this);
		} else if (actionCommand.compareTo("Small magnification region")==0) {
			lensWidth=50;
			lensHeight=50;
		} else if (actionCommand.compareTo("Medium magnification region")==0) {
			lensWidth=75;
			lensHeight=75;
		} else if (actionCommand.compareTo("Large magnification region")==0) {
			lensWidth=100;
			lensHeight=100;
		} else if (actionCommand.compareTo("2x magnification")==0) {
			magnification=2;
		} else if (actionCommand.compareTo("4x magnification")==0) {
			magnification=4;
		} else if (actionCommand.compareTo("6x magnification")==0) {
			magnification=6;
		} else if (actionCommand.compareTo("8x magnification")==0) {
			magnification=8;
		} else if (actionCommand.compareTo("Fixed lens location")==0) {
			fixedLocation=mae.getMenuAction().getMenuItem().isSelected();
		} else if (actionCommand.compareTo("Highlight magnified region")==0) {
			showMagnifyHighlight=mae.getMenuAction().getMenuItem().isSelected();
		}
	}

	// =======================================================================

	public void prepare(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			ri=ip.getPipelineRenderer().getRenderedImage();
			computeMagnifiedImage();
			computeLensImage(e.getPoint());
			if (fixedLocation) {
				mlp=new MagnifyLensPanel(5,5,lensWidth,lensHeight);
				if (showMagnifyHighlight) {
					Point p=e.getPoint();
					mh=new MagnifyHighlight(p.x-(int)((float)lensWidth/((float)2*magnification)),p.y-(int)((float)lensHeight/((float)2*magnification)),lensWidth,lensHeight);
					ip.add(mh);
				}
			} else {
				Point p=e.getPoint();
				mlp=new MagnifyLensPanel(p.x-(int)((float)lensWidth/(float)2),p.y-(int)((float)lensHeight/(float)2),lensWidth,lensHeight);
			}
			ip.add(mlp);

			rp=ip.getPipelineRenderer().getRenderingProperties();
			scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
			translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
			translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();

			Point2D.Double translatedPoint=translateToImage(rp,e.getPoint());
			px=(int)translatedPoint.x;
			py=(int)translatedPoint.y;
			addIndicatorBar(ip,new MagicLensIndicatorBar());
			ip.repaint();
		}
	}

	// =======================================================================

	protected void computeMagnifiedImage() {

		// Create a border around the core image so that if the magic lens
		// goes to the edge of the original image, you get a black region
		// extended outside and there are no errors in magnifying the
		// non-image area.  Create the scaled image based on the bordered
		// image.  Only create the scaled image once to reduce computation
		// time, and store it; the bordered image and scaled image are
		// discarded if there are any other changes to the image.

		ParameterBlock pb=new ParameterBlock();
		pb.addSource(ri);
		pb.add(BORDER_EXTEND);
		pb.add(BORDER_EXTEND);
		pb.add(BORDER_EXTEND);
		pb.add(BORDER_EXTEND);
		borderedImage=JAI.create("border",pb);
		pb=new ParameterBlock();
		pb.addSource(borderedImage);
		pb.add((float)magnification);
		pb.add((float)magnification);
		pb.add((float)0);
		pb.add((float)0);
		pb.add(INTERP_BICUBIC);
		if (magnifiedImage!=null) magnifiedImage.dispose();
		magnifiedImage=null;
		magnifiedImage=JAI.create("scale",pb,null);
		pb=null;
	}

	// =======================================================================

	protected void computeLensImage(Point p) {

		// Crop the scaled image to the region pointed to. Check to make
		// sure that the point is actually in the image area.

		int minX=ri.getMinX();
		int minY=ri.getMinY();

		if ((p.x<=minX)||(p.y<=minY)||(p.x>(minX+ri.getWidth()))||(p.y>(minY+ri.getHeight()))) {lensImage=null; return;}

		double x=p.x-(lensWidth/2.0);
		double y=p.y-(lensHeight/2.0);

		ParameterBlock pb=new ParameterBlock();
		pb.addSource(magnifiedImage);
		pb.add((float)(x*magnification));
		pb.add((float)(y*magnification));
		pb.add((float)(lensWidth*magnification));
		pb.add((float)(lensHeight*magnification));
		PlanarImage croppedImage=JAI.create("crop",pb);

		// Translate the cropped image to the correct region.  Discard the
		// cropped image.

		if (lensImage!=null) {lensImage.dispose(); lensImage=null;}
		pb=new ParameterBlock();
		pb.addSource(croppedImage);
		pb.add(-(float)(p.x*magnification-(lensWidth/2.0)));
		pb.add(-(float)(p.y*magnification-(lensHeight/2.0)));
		lensImage=JAI.create("translate",pb);

		croppedImage.dispose();
		croppedImage=null;
		pb=null;
	}

	public void execute(MouseEvent e) {

		Point p=e.getPoint();
		Point2D.Double translatedPoint=translateToImage(rp,p);
		px=(int)translatedPoint.x;
		py=(int)translatedPoint.y;
		computeLensImage(p);
		if (!fixedLocation) mlp.setLocation(p.x-(int)((float)lensWidth/(float)2),p.y-(int)((float)lensHeight/(float)2));
		if (mh!=null) mh.setLocation(p.x-(int)((float)lensWidth/((float)2*magnification)),p.y-(int)((float)lensHeight/((float)2*magnification)));
		mlp.repaint();
		ib.repaint();
	}

	// =======================================================================

	public void finish(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			ip.remove(mlp);
			if (mh!=null) ip.remove(mh);
			if (ri instanceof PlanarImage) ((PlanarImage)ri).dispose();
			ri=null;
			borderedImage.dispose(); borderedImage=null;
			magnifiedImage.dispose(); magnifiedImage=null;
			if (lensImage!=null) lensImage.dispose(); 
			lensImage=null;
			mlp=null;
			mh=null;
			removeIndicatorBar(ip);
			ip.repaint();
		}
	}

	// =======================================================================

	private class MagnifyHighlight extends JPanel {

		public MagnifyHighlight(int x, int y, int lensWidth, int lensHeight) {

			super();
			setOpaque(false);
			setBounds(x,y,lensWidth,lensHeight);
		}

		// =======================================================================

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2=(Graphics2D)g.create();
			g2.setComposite(HILITE_AC);
			g2.setColor(Color.RED);
			g2.fillRect(0,0,(int)((lensWidth/magnification)-1),(int)((lensHeight/magnification)-1));
			g2.dispose();
		}
	}

	// =======================================================================

	private class MagnifyLensPanel extends JPanel {

		public MagnifyLensPanel(int x, int y, int lensWidth, int lensHeight) {

			super();
			setOpaque(true);
			setBackground(Color.black);
			setBounds(x,y,lensWidth,lensHeight);
		}

		// =======================================================================

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2=(Graphics2D)g.create();
			if (lensImage!=null) {
				g2.drawRenderedImage(lensImage,IDENTITY);
				g2.setColor(Color.RED);
				g2.drawRect(0,0,lensWidth-1,lensHeight-1);
			}
			g2.dispose();
		}
	}

	// =======================================================================

	private class MagicLensIndicatorBar extends IndicatorBar {

		public MagicLensIndicatorBar() {super();}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2=(Graphics2D)g.create();
			String str=new String("("+px+","+py+"): "+String.format("%.2fx",scale*magnification));
			paintText(g2,str,0,1);
			g2.dispose();
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.geom.AffineTransform;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingworker.SwingWorker;

import imageviewer.model.Image;
import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;
import imageviewer.model.ImageSequenceProperties;

import imageviewer.rendering.ImagePipelineRenderer;
import imageviewer.rendering.RenderingOpPipeline;
import imageviewer.rendering.RenderingOpPipelineFactory;
import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.wl.DefaultWindowLevelManager;
import imageviewer.rendering.wl.WindowLevel;

import imageviewer.ui.DataPanel;

// =======================================================================

public class ImageMagPanel extends DataPanel {

	public static final int SEQUENCE_TOP=0;
	public static final int SEQUENCE_BOTTOM=1;

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,0.9f);
	private static final AlphaComposite AC=AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,0.9f);
	private static final SimpleDateFormat DATE_FORMAT=new SimpleDateFormat("M/dd/yy");
	private static final Rectangle VISIBLE_RECT=new Rectangle();

	private static final GradientPaint BUTTON_PAINT=(GradientPaint)UIManager.get("Button.gradientBackground");
	private static final Font DEFAULT_FONT=new Font("Segoe UI",Font.PLAIN,8);

	// =======================================================================

	int thumbnailSize=64, panelWidth=640, magSize=4;
	int lastRow=-1, offsetX=0, offsetY=0, gridWidth=0, gridHeight=0, currentX=0, verticalOffset=0, totalImages=0;
	BufferedImage backgroundImage=null, magImage=null;
	ArrayList<ImageSequence> isList=new ArrayList<ImageSequence>();
	Point lastPoint=null;

	public ImageMagPanel(ArrayList<? extends ImageSequenceGroup> isg, int thumbnailSize, int panelWidth) {

		super(); 
		this.thumbnailSize=thumbnailSize; 
		this.panelWidth=panelWidth; 
		initialize(isg);
	}

	// =======================================================================

	private void initialize(ArrayList<? extends ImageSequenceGroup> isg) {

		for (ImageSequenceGroup study : isg) {for (ImageSequence is : study.getGroups()) {isList.add(is); totalImages+=is.size();}}

		gridWidth=(int)Math.floor(panelWidth/thumbnailSize);
		gridHeight=(int)Math.ceil((double)totalImages/(double)gridWidth);
		// verticalOffset=17+(int)Math.ceil(thumbnailSize/2);
		verticalOffset=(int)Math.ceil(thumbnailSize/4);
		setSize(gridWidth*thumbnailSize,gridHeight*(thumbnailSize+1));

		backgroundImage=new BufferedImage(1+thumbnailSize*gridWidth,1+thumbnailSize*gridHeight,BufferedImage.TYPE_BYTE_GRAY);		
		final Graphics2D g2=backgroundImage.createGraphics();
		g2.setColor(Color.darkGray);
	
		SwingWorker sw=new SwingWorker<Boolean,Void>() {

			public Boolean doInBackground() {
				RenderingProperties rp=new RenderingProperties();
				rp.setProperties(new String[] {RenderingProperties.HORIZONTAL_FLIP,RenderingProperties.ROTATION,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
												 new Object[] {new Boolean(false),new Float(0),new Double(0),new Double(0)});
				ImagePipelineRenderer ipr=new ImagePipelineRenderer(RenderingOpPipelineFactory.create(),rp);
				for (int y=0; y<gridHeight; y++) {
					for (int x=0; (x<gridWidth && ((y*gridWidth)+x)<totalImages); x++) {
						Image i=getImage((y*gridWidth)+x);
						int imageWidth=i.getWidth();
						double scaleFactor=(double)thumbnailSize/(double)imageWidth;
						WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(i);				
						rp.setProperties(new String[] {RenderingProperties.SCALE,RenderingProperties.WINDOW_LEVEL,RenderingProperties.MAX_PIXEL,
																					 RenderingProperties.SOURCE_WIDTH,RenderingProperties.SOURCE_HEIGHT}, 
							               new Object[] {new Double(scaleFactor),wl,new Integer(i.getMaxPixelValue()),
																					 new Integer(i.getWidth()),new Integer(i.getHeight())});
						ipr.setSource(i.getRenderedImage());
						RenderedImage ri=ipr.getRenderedImage();
						AffineTransform at=new AffineTransform(1,0,0,1,x*thumbnailSize,y*thumbnailSize);
						g2.drawRenderedImage(ri,at);
						g2.drawRect(x*thumbnailSize,y*thumbnailSize,thumbnailSize,thumbnailSize);
					}
				}
				ipr.flush();
				ipr.doCleanup();
				ipr=null;
				return Boolean.TRUE;
			}

			protected void done() {repaint();}
		};
		sw.execute();

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	// =======================================================================

	private Image getImage(int index) {int n=0; for (ImageSequence is : isList) {if (n+is.size()>index) return is.get(index-n); n+=is.size();} return null;}
	private ImageSequence getImageSequence(int index) {int n=0; for (ImageSequence is : isList) {if (n+is.size()>index) return is; n+=is.size();} return null;}
	private boolean isSeriesStart(int index) {int n=0; for (ImageSequence is : isList) {if (n==index) return true; n+=is.size();} return false;}

	// =======================================================================

	public void paintLayers(Graphics g) {}
	public void groupPropertyChange(Object source, String[] propertyNames, Object[] values) {}

	// =======================================================================

	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		Graphics2D g2=(Graphics2D)g;
		g2.drawImage(backgroundImage,0,verticalOffset-offsetY,null);
		if (magImage!=null) {
			int y=(lastRow*thumbnailSize-offsetY+verticalOffset)-(int)(thumbnailSize/2);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);		

			float[] rgb=new float[] {0.1f,0.1f,0.1f};
			g2.setComposite(DEFAULT_AC);
			/*g2.setPaint(BUTTON_PAINT);
			g2.fillRect(0,y-16,panelWidth+1,16);
			g2.setPaint(new GradientPaint(0,y-16,new Color(0.5f,0.5f,0.7f,0.45f),0,y-8,new Color(rgb[0],rgb[1],rgb[2],0.3f)));
			g2.fillRect(0,y-16,panelWidth+1,8);
			g2.setPaint(new GradientPaint(0,y-8,new Color(rgb[0],rgb[1],rgb[2],0f).darker().darker(),0,y,new Color(rgb[0],rgb[1],rgb[2],0.27f).darker()));
			g2.fillRect(0,y-8,panelWidth+1,8);
			*/
			/* g2.setFont(DEFAULT_FONT)
				 final FontMetrics fm=g2.getFontMetrics(DEFAULT_FONT);
				 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);			

				 if (isSeriesStart((y*gridWidth)+x)) {
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.67f));
				GradientPaint gp=new GradientPaint(x*thumbnailSize,y*thumbnailSize,Color.gray,11+x*thumbnailSize,y*thumbnailSize,Color.darkGray);
				g2.setPaint(gp);
				g2.fillRect(x*thumbnailSize,y*thumbnailSize,11,thumbnailSize);
				ImageSequence is=getImageSequence((y*gridWidth)+x);
				String date=DATE_FORMAT.format((Date)is.getProperty(ImageSequenceProperties.TIMESTAMP));
				int stringWidth=3+fm.stringWidth(date);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));	
				g2.rotate(-Math.PI/2);
				g2.translate(-stringWidth,0);
				g2.setColor(Color.white);
				// g2.drawString(date,-y*thumbnailSize,x*thumbnailSize+9);
				g2.setColor(Color.darkGray);
				g2.translate(stringWidth,0);
				g2.rotate(Math.PI/2);
				} */

			g2.clip(new Rectangle(0,y,panelWidth,thumbnailSize*4));
			g2.setComposite(AC);
			g2.drawImage(magImage,Math.max(-currentX,-panelWidth),y,null);
			g2.setColor(Color.gray);
			g2.drawLine(0,y,panelWidth,y);
			g2.drawLine(0,y+(4*thumbnailSize)-1,panelWidth,y+(4*thumbnailSize)-1);
			int targetXCell=(int)Math.floor(currentX/thumbnailSize);
			if (((lastRow*gridWidth)+targetXCell)>totalImages-1) return;
			g2.setColor(Color.red);
			g2.drawRect(-currentX+(targetXCell*4*thumbnailSize),y,thumbnailSize*4,thumbnailSize*4-1);

		}
	}

	// =======================================================================

	public void jumpTo(int position) {

		if ((position==SEQUENCE_TOP)&&(offsetY!=0)) {offsetY=0; resetMagImage(); repaint();}
		if (position==SEQUENCE_BOTTOM) {
			computeVisibleRect(VISIBLE_RECT); 
			if ((thumbnailSize*(1+gridHeight))>VISIBLE_RECT.height) {offsetY=(thumbnailSize*(1+gridHeight))-VISIBLE_RECT.height; resetMagImage(); repaint();}
		}
	}

	// =======================================================================

	public void mouseMoved(MouseEvent e) {

		int x=e.getX();
		int targetRow=(int)Math.max(0,(e.getY()+offsetY-verticalOffset)/thumbnailSize);
		targetRow=Math.min(gridHeight-1,targetRow);
		if (targetRow==lastRow) {
			if (x==currentX) return;
			currentX=x;
			repaint();
			return;
		}
		currentX=x;
		int startIndex=(targetRow*gridWidth);
		int endIndex=(int)Math.min(((targetRow+1)*gridWidth),totalImages);

		resetMagImage();
		magImage=new BufferedImage(thumbnailSize*gridWidth*magSize,thumbnailSize*magSize,BufferedImage.TYPE_INT_RGB);	
		Graphics2D g2=magImage.createGraphics();
		setCursor(HOURGLASS_CURSOR); 
		RenderingProperties rp=new RenderingProperties();
		ImagePipelineRenderer ipr=new ImagePipelineRenderer(RenderingOpPipelineFactory.create(),rp);
		for (int loop=startIndex, index=0; loop<endIndex; loop++, index++) {
			Image i=getImage(loop);
			int imageWidth=i.getWidth();
			double scaleFactor=(double)(magSize*thumbnailSize)/(double)imageWidth;
			WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(i);				
			rp.setProperties(new String[] {RenderingProperties.SCALE,RenderingProperties.WINDOW_LEVEL,RenderingProperties.MAX_PIXEL,
																		 RenderingProperties.SOURCE_WIDTH,RenderingProperties.SOURCE_HEIGHT}, 
					                           new Object[] {new Double(scaleFactor),wl,new Integer(i.getMaxPixelValue()),
																									 new Integer(i.getWidth()),new Integer(i.getHeight())});
			ipr.setSource(i.getRenderedImage());
			RenderedImage ri=ipr.getRenderedImage();
			AffineTransform at=new AffineTransform(1,0,0,1,index*4*thumbnailSize,0);
			g2.drawRenderedImage(ri,at);
			g2.setColor(Color.darkGray);
			g2.drawLine(index*4*thumbnailSize-1,0,index*4*thumbnailSize-1,2*thumbnailSize);
			g2.setColor(Color.black);
			g2.drawLine(index*4*thumbnailSize,0,index*4*thumbnailSize,2*thumbnailSize);
		}
		ipr.flush();
		ipr.doCleanup();
		ipr=null;
		lastRow=targetRow;
		setCursor(DEFAULT_CURSOR); 
		repaint();
	}
	
	public void mousePressed(MouseEvent e) {lastPoint=e.getPoint();}
	public void mouseReleased(MouseEvent e) {mouseMoved(e);}
	public void mouseDragged(MouseEvent e) {int deltaY=e.getY()-lastPoint.y; moveImage(deltaY); lastPoint=e.getPoint();}
	public void mouseWheelMoved(MouseWheelEvent e) {int deltaY=thumbnailSize*e.getWheelRotation(); moveImage(deltaY);}
	public void mouseExited(MouseEvent e) {resetMagImage(); repaint();}

	private void moveImage(int deltaY) {

		computeVisibleRect(VISIBLE_RECT);
		if (thumbnailSize*(1+gridHeight)<VISIBLE_RECT.height) return;
		if ((offsetY+deltaY>=0)&&(offsetY+deltaY<(thumbnailSize*(1+gridHeight)-VISIBLE_RECT.height))) {resetMagImage(); offsetY+=deltaY; repaint(); return;}
		if (offsetY+deltaY<0) {resetMagImage(); offsetY=0; repaint(); return;}
		if (offsetY+deltaY>(thumbnailSize*(1+gridHeight)-VISIBLE_RECT.height)) {resetMagImage(); offsetY=(thumbnailSize*(1+gridHeight)-VISIBLE_RECT.height); repaint(); return;}
	} 

	private void resetMagImage() {if (magImage!=null) {magImage.flush(); magImage=null;} lastRow=-1;}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.plugins;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.util.ArrayList;
import java.util.EventObject;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.TiledImage;

import imageviewer.model.dl.ShapeDataLayer;
import imageviewer.rendering.RenderingProperties;

import imageviewer.tools.ImagingTool;
import imageviewer.tools.Tool;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.annotation.StylizedShape;
import imageviewer.ui.image.ImagePanel;

import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.event.MenuActionEvent;
import imageviewer.ui.swing.event.MenuActionListener;

// =======================================================================

public class MagicWandTool extends ImagingTool implements Tool, Plugin, MenuActionListener {

	private static final String[] DEFAULT_MENU_LOCATION=new String[] {"tools","plugins"};
	private static final String DEFAULT_PLUGIN_MENU=new String("config/plugins/magicWandPlugin.xml");

	private static final int UP=0;
	private static final int DOWN=1;
	private static final int UP_OR_DOWN=2;
	private static final int LEFT=3;
	private static final int RIGHT=4;
	private static final int LEFT_OR_RIGHT=5;
	private static final int NA=6;

	private static final int[] LOOKUP_TABLE={NA,RIGHT,DOWN,RIGHT,UP,UP,UP_OR_DOWN,UP,LEFT,LEFT_OR_RIGHT,DOWN,RIGHT,LEFT,LEFT,DOWN,NA};

	private static final Color FILL_COLOR=new Color(32,128,220);
	private static final Color STROKE_COLOR=new Color(50,50,200);

	// =======================================================================

	double tolerance=0.10, upperThreshold=0, lowerThreshold=0;
	float[] dataArray=null;
	RenderingProperties rp=null;
	int width=0, height=0, numBands=0;

	public MagicWandTool() {}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {dataArray=null;}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Magic wand");}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {execute(e);}

	// =======================================================================

	public String getMenuFilename() {

		String menuConfig=(String)ApplicationContext.getContext().getProperty("PLUGIN_CONFIG_MAGIC_WAND");
		if (menuConfig==null) menuConfig=DEFAULT_PLUGIN_MENU;
		return menuConfig;
	}

	public String[] getMenuLocation() {return DEFAULT_MENU_LOCATION;}

	// =======================================================================

	public void actionPerformed(MenuActionEvent mae) {

		ActionEvent ae=mae.getActionEvent();
		String actionCommand=ae.getActionCommand();
		if (actionCommand==null) {
			MenuAction ma=mae.getMenuAction();
			if (ma!=null) actionCommand=ma.getCommandName(); else return;
		}
		if (actionCommand==null) return;
		

		if (actionCommand.compareTo("Select region")==0) {
			ApplicationContext.getContext().setTool(this);
		} 

		/*else if (actionCommand.compareTo("Stroke region")==0) {
			isStroked=mae.getMenuAction().getMenuItem().isSelected();
		} else if (actionCommand.compareTo("Fill region")==0) {
			isFilled=mae.getMenuAction().getMenuItem().isSelected();
		} else if (actionCommand.compareTo("Draw final normals")==0) {
			drawNormals=mae.getMenuAction().getMenuItem().isSelected();
		} else if (actionCommand.compareTo("Draw iterations")==0) {
			drawIterations=mae.getMenuAction().getMenuItem().isSelected();
		} 
		*/
	}

	// =======================================================================

	public void execute(MouseEvent e) {

		ImagePanel ip=(ImagePanel)e.getSource();
		rp=ip.getPipelineRenderer().getRenderingProperties();
		ArrayList boundary=(ArrayList)run(new Object[] {translateToImage(rp,e.getPoint()),ip});
		Polygon contour=new Polygon();
		for (int loop=0, n=boundary.size(); loop<n; loop++) {
			Point p=(Point)boundary.get(loop);
			contour.addPoint(p.x,p.y);
		}
		StylizedShape ss=new StylizedShape(contour,null,1.0f,0.75f);
		ss.setStroked(true);
		ss.setFilled(false);
		ss.setStrokeColor(STROKE_COLOR);
		ss.setFillColor(FILL_COLOR);
		ss.setFillAlphaComposite(0.5f);
		ShapeDataLayer sdl=new ShapeDataLayer(ss); 
		ip.getSource().addDataLayer(sdl);
		ip.clearTemporaryShapes();
		ip.repaint();
	}

	// =======================================================================

	private boolean inRange(int x, int y) {

		float value=dataArray[(int)(Math.round(y*numBands*width))+(int)(Math.round(x*numBands))];
		return ((value>=lowerThreshold)&&(value<=upperThreshold));
	}

	// =======================================================================
	// Execute the plugin algorithm.  Adapted from ImageJ Wand plugin. 

	public Object run(Object[] arguments) {

		Point2D p2=(Point2D)arguments[0];
		Point p=new Point((int)p2.getX(),(int)p2.getY());
		ImagePanel ip=(ImagePanel)arguments[1];
		int x=p.x; int x0=p.x;
		int y=p.y; int y0=p.y;

		// Grab the the data array for the image.
		
		RenderedImage ri=ip.getSource().getRenderedImage();
		PlanarImage pi=(ri instanceof PlanarImage) ? (PlanarImage)ri : new TiledImage(ri,false);
		ParameterBlock pb=new ParameterBlock();
		pb.addSource(pi);
		pb.add(DataBuffer.TYPE_FLOAT);
		PlanarImage floatImage=JAI.create("format",pb);

		width=floatImage.getWidth();
		height=floatImage.getHeight();
		numBands=floatImage.getSampleModel().getNumBands();
		RenderedImage[] src={floatImage};
		RasterAccessor ra=new RasterAccessor(floatImage.getData(),new Rectangle(0,0,width,height),(RasterAccessor.findCompatibleTags(src,floatImage))[0],floatImage.getColorModel());
		dataArray=ra.getFloatDataArray(0);

		// Compute the threshold based on the start point.

		float seedPoint=dataArray[(int)(Math.round(y*numBands*width))+(int)(Math.round(x*numBands))];
		upperThreshold=seedPoint; //*(1+tolerance);
		lowerThreshold=seedPoint; //*(1-tolerance);

		// Compute the initial direction in which to start 

		int direction=UP;
		if (inRange(x,y)) {
			do {x++;} while (inRange(x,y));
			direction=(!inRange(x-1,y-1)) ? RIGHT : ((inRange(x,y-1)) ? LEFT : DOWN);
		} else {
			do {x++;} while (!inRange(x,y) && x<width);
			if (x>=width) return null;
		}

		boolean UL=inRange(x-1,y-1);
		boolean UR=inRange(x,y-1);	
		boolean LL=inRange(x-1,y);	  
		boolean LR=inRange(x,y);

		ArrayList boundary=new ArrayList();
		int startDirection=direction;
		do {
			int index=0;
			if (LR) index|=1;
			if (LL) index|=2;
			if (UR) index|=4;
			if (UL) index|=8;
			int newDirection=LOOKUP_TABLE[index];
			if (newDirection==UP_OR_DOWN) newDirection=(direction==RIGHT) ? UP : DOWN;
			if (newDirection==LEFT_OR_RIGHT) newDirection=(direction==UP) ? LEFT : RIGHT;
			if (newDirection!=direction) boundary.add(new Point(x,y));
			switch (newDirection) {
			     case UP: y--; LL=UL; LR=UR; UL=inRange(x-1,y-1); UR=inRange(x,y-1); break;
			   case LEFT: x--; UR=UL; LR=LL; UL=inRange(x-1,y-1); LL=inRange(x-1,y); break;
			   case DOWN: y++; UL=LL; UR=LR; LL=inRange(x-1,y); LR=inRange(x,y); break;
			  case RIGHT: x++; UL=UR; LL=LR; UR=inRange(x,y-1); LR=inRange(x,y); break;
			}
			direction=newDirection;
		} while ((x!=x0 || y!=y0 || direction!=startDirection));

		dataArray=null;
		floatImage.dispose(); floatImage=null;
		if (!(ri instanceof PlanarImage)) pi.dispose(); pi=null;
		return boundary;
	}
}

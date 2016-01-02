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
import java.awt.Polygon;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.EventObject;

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

public class ActiveContourTool extends ImagingTool implements Tool, Plugin, MenuActionListener {

	private static final String[] DEFAULT_MENU_LOCATION=new String[] {"tools","plugins"};
	private static final String DEFAULT_PLUGIN_MENU=new String("config/plugins/activeContourPlugin.xml");

	private static final Color FILL_COLOR=new Color(32,128,220);
	private static final Color STROKE_COLOR=new Color(50,50,200);

	// =======================================================================

	boolean isStroked=true, isFilled=false, drawNormals=false, drawIterations=false;
	ArrayList<Point2D.Double> initialContour=new ArrayList<Point2D.Double>();
	RenderingProperties rp=null;
	ImagePanel lastIP=null;

	public ActiveContourTool() {}

	// =======================================================================

	public void startTool(EventObject e) {}

	public void endTool(EventObject e) {

		if (lastIP!=null) {
			lastIP.clearTemporaryShapes();
			lastIP.repaint();
			lastIP=null;
		}
	}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Active contour segmentation");}

	// =======================================================================

	public void mousePressed(MouseEvent e) {
	
		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			rp=ip.getPipelineRenderer().getRenderingProperties();
			lastIP=ip;
		}
	}

	// =======================================================================

	public String getMenuFilename() {

		String menuConfig=(String)ApplicationContext.getContext().getProperty("PLUGIN_CONFIG_ACTIVE_CONTOUR");
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
		
		if (actionCommand.compareTo("Draw active contour")==0) {
			ApplicationContext.getContext().setTool(this);
		} else if (actionCommand.compareTo("Stroke region")==0) {
			isStroked=mae.getMenuAction().getMenuItem().isSelected();
		} else if (actionCommand.compareTo("Fill region")==0) {
			isFilled=mae.getMenuAction().getMenuItem().isSelected();
		} else if (actionCommand.compareTo("Draw final normals")==0) {
			drawNormals=mae.getMenuAction().getMenuItem().isSelected();
		} else if (actionCommand.compareTo("Draw iterations")==0) {
			drawIterations=mae.getMenuAction().getMenuItem().isSelected();
		} 
	}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {

		int clickCount=e.getClickCount();
		ImagePanel ip=(ImagePanel)e.getSource();
		lastIP=ip;

		if (clickCount==1) {

			// User is only putting down points for the initial contour

			Point p=e.getPoint();
			Point2D.Double translatedPoint=translateToImage(rp,p);
			initialContour.add(translatedPoint);
			StylizedShape ss=new StylizedShape(new Ellipse2D.Double(translatedPoint.x,translatedPoint.y,3,3));
			ss.setStroked(false);
			ss.setFilled(true);
			ip.addTemporaryShape(ss);
			ip.repaint();

		} else if (clickCount==2) {		

			// Last point entered, activate the algorithm

			ShapeDataLayer sdl=(ShapeDataLayer)run(new Object[]{ip});
			ip.getSource().addDataLayer(sdl);
			ip.clearTemporaryShapes();
			ip.repaint();
			initialContour.clear();
			lastIP=null;
		}
	}

	// =======================================================================
	// Execute the plugin algorithm.

	public Object run(Object[] arguments) {

		ActiveContour ac=new ActiveContour();
		ImagePanel ip=(ImagePanel)arguments[0];
		double[][] points=(double[][])ac.process(initialContour,ip.getSource().getRenderedImage(),drawIterations);

		ShapeDataLayer sdl=null;
		if (drawIterations) {
			ArrayList polygons=ac.getIterationPolygons();
			for (int loop=0, n=polygons.size(); loop<n; loop++) {
				Polygon p=(Polygon)polygons.get(loop);
				Color c=(loop==0) ? Color.red : Color.orange;
				StylizedShape ss=new StylizedShape(p,null,0.5f,0.5f);
				ss.setStrokeColor(c);
				ss.setStroked(true);
				ss.setFilled(false);
				if (loop==0) sdl=new ShapeDataLayer(ss); else sdl.addShape(ss);
			}
		}
		Shape s=(Shape)ac.getPolygon(points[0],points[1]);
		StylizedShape ss=new StylizedShape(s,null,1.0f,0.75f);
		ss.setStroked(isStroked);
		ss.setFilled(isFilled);
		ss.setStrokeColor(STROKE_COLOR);
		ss.setFillColor(FILL_COLOR);
		ss.setFillAlphaComposite(0.5f);
		if (sdl==null) sdl=new ShapeDataLayer(ss); else sdl.addShape(ss);
		if (drawNormals) {
			s=ac.generateNormalsDisplay(points[0],points[1]);
			ss=new StylizedShape(s,null,0.5f,0.75f);
			ss.setStrokeColor(Color.yellow);
			sdl.addShape(ss);
		}
		ac.flush();
		return sdl;
	}
}

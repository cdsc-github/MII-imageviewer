/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;

import imageviewer.model.DataLayer;
import imageviewer.rendering.RenderingProperties;
import imageviewer.ui.annotation.ControlPoint.Location;

// =======================================================================
// Basic shape object, augmented to hold information on color,
// line style, opacity, and other potential rendering properties.

public class StylizedShape implements Selectable {

	protected static final Color DEFAULT_COLOR=Color.red;
	protected static final Color DEFAULT_OUTLINE_COLOR=Color.black;
	protected static final BasicStroke DEFAULT_STROKE=new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	protected static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
	protected static final AlphaComposite DEFAULT_OUTLINE_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);

	// =======================================================================

	protected Shape baseShape=null;
	protected DataLayer parentLayer=null;
	protected boolean paintFlag=false;

	AlphaComposite strokeAC=DEFAULT_AC, outlineAC=DEFAULT_OUTLINE_AC, fillAC=DEFAULT_AC;
	Color strokeColor=DEFAULT_COLOR, fillColor=DEFAULT_COLOR, outlineColor=DEFAULT_OUTLINE_COLOR;
	BasicStroke bs=DEFAULT_STROKE; 
	boolean filled=false, stroked=true, outlined=true, selected=false;
	int ssID = 0;
	
	public StylizedShape() {}
	public StylizedShape(Shape baseShape) {this.baseShape=baseShape;}
	public StylizedShape(Shape baseShape, Color c, AlphaComposite ac) {initialize(baseShape,c,c,DEFAULT_STROKE,ac,ac,true,false);}
	public StylizedShape(Shape baseShape, Color c, BasicStroke bs) {initialize(baseShape,c,c,bs,DEFAULT_AC,DEFAULT_AC,true,false);}
	public StylizedShape(Shape baseShape, Color c, BasicStroke bs, AlphaComposite ac) {initialize(baseShape,c,c,bs,ac,ac,true,false);}
	public StylizedShape(Shape baseShape, Color c, BasicStroke bs, AlphaComposite strokeAC, AlphaComposite fillAC) {initialize(baseShape,c,c,bs,strokeAC,fillAC,true,false);}

	public StylizedShape(Shape baseShape, Color c, float strokeSize, float alpha) {

		AlphaComposite ac=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha); 
		initialize(baseShape,c,c,new BasicStroke(strokeSize),ac,ac,true,false);
	}
	
	private void initialize(Shape baseShape, Color strokeColor, Color fillColor, BasicStroke bs, AlphaComposite strokeAC, AlphaComposite fillAC, boolean stroked, boolean filled) {

		this.baseShape=baseShape;
		this.strokeColor=strokeColor;
		this.fillColor=fillColor;
		this.bs=bs;
		this.strokeAC=strokeAC;
		this.fillAC=fillAC;
		this.filled=filled;
		this.stroked=stroked;
	}

	// =======================================================================

	public AlphaComposite getStrokeAlphaComposite() {return strokeAC;}
	public AlphaComposite getFillAlphaComposite() {return fillAC;}
	public AlphaComposite getOutlineAlphaComposite() {return outlineAC;}
	public Shape getBaseShape() {return baseShape;}
	public Color getStrokeColor() {return strokeColor;}
	public Color getFillColor() {return fillColor;}
	public Color getOutlineColor() {return outlineColor;}
	public BasicStroke getStroke() {return bs;}
	public DataLayer getParentLayer() {return parentLayer;}

	public boolean isFilled() {return filled;}
	public boolean isStroked() {return stroked;}
	public boolean isOutlined() {return outlined;}
	public int getID() {return ssID;}

	public void setBaseShape(Shape x) {baseShape=x;}
	public void setStroke(BasicStroke x) {bs=x;}
	public void setColor(Color x) {strokeColor=x; fillColor=x;}
	public void setStrokeColor(Color x) {strokeColor=x;}
	public void setFillColor(Color x) {fillColor=x;}
	public void setOutlineColor(Color x) {outlineColor=x;}
	public void setAlphaComposite(AlphaComposite x) {strokeAC=x; fillAC=x;}
	public void setStrokeAlphaComposite(AlphaComposite x) {strokeAC=x;}
	public void setStrokeAlphaComposite(float x) {AlphaComposite ac=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,x); strokeAC=ac;}
	public void setFillAlphaComposite(AlphaComposite x) {fillAC=x;}
	public void setFillAlphaComposite(float x) {AlphaComposite ac=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,x); fillAC=ac;}
	public void setOutlineAlphaComposite(AlphaComposite x) {outlineAC=x;}
	public void setOutlineAlphaComposite(float x) {AlphaComposite ac=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,x); outlineAC=ac;}
	public void setFilled(boolean x) {filled=x;}
	public void setStroked(boolean x) {stroked=x;}
	public void setOutlined(boolean x) {outlined=x;}
	public void setParentLayer(DataLayer x) {parentLayer=x;}
	public void setID(int ID) {ssID = ID;}
	
	//=======================================================================

	public boolean contains(Point x) {Shape s=(baseShape instanceof Line2D) ? bs.createStrokedShape(baseShape) : baseShape; return s.contains(x);}

	public boolean intersects(Shape x) {

		// A somewhat slow way of doing polygon intersection, but it's
		// okay for now.  The problem is that we have arbitrary shapes
		// against a potentially rotated selection box...so the easiest
		// way to do this in Java2D is to create two Areas and see if they
		// intersect.  Dumb dumb dumb...
		//
		// Note that we also used a strokedShape mechanism if the object
		// is stroked and not filled; this gives more natural selection
		// behavior.

		Shape s=((baseShape instanceof Line2D)||(stroked && !filled)) ? bs.createStrokedShape(baseShape) : baseShape; 
		Area targetArea=new Area(x);
		Area shapeArea=new Area(s);
		targetArea.intersect(shapeArea);
		return (!targetArea.isEmpty());
	}

	// =======================================================================

	public boolean paints() {return paintFlag;}

	public void draw(Graphics2D g2) {}
	public void fill(Graphics2D g2) {}

	// =======================================================================
	// Selectable interface...At this level, only permit that it be selected, but
	// otherewise, everything else should be done at a lower level.

	public boolean isSelected() {return selected;}
	public boolean isMovable() {return false;}
	public boolean hasControlPoints() {return false;}
	public boolean hasRotationAxis() {return false;}

	public void select() {selected=true;}
	public void deselect() {selected=false;}
	public void moveStart(int x, int y) {}
	public void moveSDrag(int x, int y) {}
	public void moveEnd(int x, int y) {}

	public void cut() {}
	public void copy() {}
	public void paste() {}

	public void delete() {parentLayer.remove(this);}

	public void controlPointMoveStart(ControlPoint cp, Point2D.Double p1) {}
	public void controlPointMoveDrag(ControlPoint cp, RenderingProperties rp, Point2D.Double[] delta, Point2D.Double p) {}
	public void controlPointMoveEnd(ControlPoint cp, Point2D.Double p1, Point2D.Double p2) {}
	public void setControlPoints(ArrayList<ControlPoint> x) {}

	public double getRotationAxis() {return 0;}

	public ArrayList<ControlPoint> getControlPoints() {return null;}
	public ControlPoint getControlPoint(Location x) {return null;}   

}

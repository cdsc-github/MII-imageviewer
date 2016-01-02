/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dl;

import java.awt.Point;
import java.awt.Shape;

import java.util.ArrayList;

import imageviewer.model.DataLayer;
import imageviewer.ui.VisualLayerRenderer;
import imageviewer.ui.annotation.StylizedShape;
import imageviewer.ui.image.vl.ShapeVisualLayer;

// =======================================================================

public class ShapeDataLayer implements DataLayer {

	private static final VisualLayerRenderer SHAPE_RENDERER=new ShapeVisualLayer();
	private static final String LAYER_NAME=new String("SHAPE");

	ArrayList<StylizedShape> shapes=new ArrayList<StylizedShape>();

	public ShapeDataLayer() {}
	public ShapeDataLayer(StylizedShape ss) {shapes.add(ss);}

	// =======================================================================

	public VisualLayerRenderer getRenderer() {return SHAPE_RENDERER;}
	public String getName() {return LAYER_NAME;}

	// =======================================================================

	public ArrayList<StylizedShape> getShapes() {return shapes;}

	public void add(Object x) {if (x instanceof Shape) addShape((Shape)x); else if (x instanceof StylizedShape) addShape((StylizedShape)x);}
	public void remove(Object x) {if (x instanceof StylizedShape) removeShape((StylizedShape)x);}

	public void addShape(Shape x) {StylizedShape ss=new StylizedShape(x); ss.setParentLayer(this); shapes.add(ss);}
	public void addShape(StylizedShape x) {shapes.add(x); x.setParentLayer(this);}
	public void removeShape(StylizedShape x) {shapes.remove(x);}
	public void clearShapes() {shapes.clear();}

	// =======================================================================

	public boolean canSelect() {return true;}
	
	public ArrayList getSelections(Point x) {

		ArrayList selections=new ArrayList();
		for (int loop=0, n=shapes.size(); loop<n; loop++) {
			StylizedShape ss=shapes.get(loop);
			if (ss.contains(x)) selections.add(ss);
		}
		return selections;
	}

	public ArrayList getSelections(Shape x) {

		ArrayList selections=new ArrayList();
		for (int loop=0, n=shapes.size(); loop<n; loop++) {
			StylizedShape ss=shapes.get(loop);
			if (ss.intersects(x)) selections.add(ss);
		}
		return selections;
	}
}

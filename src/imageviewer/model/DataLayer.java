/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.awt.Point;
import java.awt.Shape;

import java.util.ArrayList;

import imageviewer.ui.VisualLayerRenderer;

public interface DataLayer {

	public static final String ANNOTATION=new String("ANNOTATION");
	public static final String HEADER=new String("HEADER");
	public static final String SHAPE=new String("SHAPE");

	public VisualLayerRenderer getRenderer();
	public String getName();

	public void add(Object x);
	public void remove(Object x);

	public boolean canSelect();
	public ArrayList getSelections(Point x);
	public ArrayList getSelections(Shape x);
}

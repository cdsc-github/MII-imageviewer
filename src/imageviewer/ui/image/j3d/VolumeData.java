/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import javax.vecmath.Point3d;

import imageviewer.model.processing.ColorMap;

// =======================================================================

public interface VolumeData {

	public static final int PLUS_X=0;
	public static final int PLUS_Y=1;
	public static final int PLUS_Z=2;
	public static final int MINUS_X=3;
	public static final int MINUS_Y=4;
	public static final int MINUS_Z=5;

	public int getWidth();
	public int getHeight();
	public int getDepth();
	public int getMinX();
	public int getMinY();
	public int getMinZ();
	public int getMaxX();
	public int getMaxY();
	public int getMaxZ();

	public double getXSpacing();
	public double getYSpacing();
	public double getZSpacing();
	public double getScaledXSpace();
	public double getScaledYSpace();
	public double getScaledZSpace();

	public Point3d getMinCoord();
	public Point3d getMaxCoord();
	public Point3d getReferencePoint();

	public void update();

}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture3D;

// =======================================================================

public abstract class Volume {

	private static final int RED=0;
	private static final int GREEN=1;
	private static final int BLUE=2;
	private static final int ALPHA=3;

	// =======================================================================

	VolumeData vd=null;
	boolean colorMapEnable=false;
	int xSize=0, ySize=0, zSize=0;
	float xSpacing=0.0f, ySpacing=0.0f, zSpacing=0.0f;
	float	xTexGenScale=0.0f, yTexGenScale=0.0f, zTexGenScale=0.0f;

	TexCoordGeneration tcg=new TexCoordGeneration();
	Texture3D	texture=null;

	public Volume(VolumeData vd) {this.vd=vd;}

	// =======================================================================

	public VolumeData getVolumeData() {return vd;}
	public Texture3D getTexture() {return texture;}

	public float getXSpacing() {return xSpacing;}
	public float getYSpacing() {return ySpacing;}
	public float getZSpacing() {return zSpacing;}
	public float getXTenGenScale() {return xTexGenScale;}
	public float getYTenGenScale() {return yTexGenScale;}
	public float getZTenGenScale() {return zTexGenScale;}

	public int getXSize() {return xSize;}
	public int getYSize() {return ySize;}
	public int getZSize() {return zSize;}

	public void setTexture(Texture3D x) {texture=x;}
	public void setVolumeData(VolumeData x) {vd=x;}

	public TexCoordGeneration getTextureCoordinates() {return tcg;}

	// =======================================================================

	protected int textureSize(int value) {

		int retval=4;
		while (retval<value) retval*=2;
		return retval;
	}

	// =======================================================================

	public abstract void generateTexture();
}

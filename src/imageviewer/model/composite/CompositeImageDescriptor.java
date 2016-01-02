/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.composite;

import java.awt.AlphaComposite;

import imageviewer.model.Image;

public class CompositeImageDescriptor {

	AlphaComposite ac=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.75f);
	Image source=null;
	String name=new String("PRIMARY");

	public CompositeImageDescriptor() {}
	public CompositeImageDescriptor(Image source) {this.source=source;}
	public CompositeImageDescriptor(Image source, AlphaComposite ac) {this.source=source; this.ac=ac;}
	public CompositeImageDescriptor(Image source, float alpha, int compositionRule) {this.source=source; ac=AlphaComposite.getInstance(compositionRule,alpha);}

	// =======================================================================

	public Image getSource() {return source;}
	public AlphaComposite getAlphaComposite() {return ac;}
	public float getAlpha() {return ac.getAlpha();}
	public String getName() {return name;}

	public void setSource(Image x) {source=x;}
	public void setAlphaComposite(AlphaComposite x) {ac=x;}
	public void setAlpha(float x) {ac=AlphaComposite.getInstance(ac.getRule(),x);}
	public void setCompositionRule(int x) {ac=AlphaComposite.getInstance(x,ac.getAlpha());}
	public void setName(String x) {name=x;}
}

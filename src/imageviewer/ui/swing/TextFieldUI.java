/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalTextFieldUI;

import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.painter.ShapePainter;
import org.jdesktop.swingx.painter.effects.InnerShadowPathEffect;

// =======================================================================

public class TextFieldUI extends MetalTextFieldUI {

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
	private static InnerShadowPathEffect IPSE=new InnerShadowPathEffect();

	static {
		IPSE.setOffset(new Point2D.Double(1,1));
		IPSE.setEffectWidth(5);
	}

	JTextField textField=null;

	public static ComponentUI createUI(JComponent c) {return new TextFieldUI();}

	// =======================================================================

	/**
	 * @see javax.swing.plaf.basic.BasicTextFieldUI#installUI(javax.swing.JComponent)
	 */

	public void installUI(JComponent c) {super.installUI(c); textField=(JTextField)c;}

	// =======================================================================

	public void paintSafely(Graphics g) {

		Graphics2D g2=(Graphics2D)g;
		g2.setComposite(DEFAULT_AC);	
		super.paintSafely(g2);
	}

	protected void paintBackground(Graphics g) {			
	 
		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);			
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);			
		JTextComponent editor=getComponent();
		RoundRectangle2D.Double r=new RoundRectangle2D.Double(0,0,editor.getWidth(),editor.getHeight(),6,6);
		ShapePainter sp=new ShapePainter(r,(editor.isEnabled()) ? editor.getBackground() : UIManager.getDefaults().getColor("TextField.disabledBackground"));
		sp.setAntialiasing(true);
		sp.setStyle(ShapePainter.Style.FILLED);
		if (!(editor.getParent() instanceof JSpinner.DefaultEditor)) sp.setAreaEffects(IPSE);
		sp.paint(g2,null,editor.getWidth(),editor.getHeight());
		g2.dispose();
	}
}

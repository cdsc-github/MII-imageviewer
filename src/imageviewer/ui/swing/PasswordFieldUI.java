/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;

import org.jdesktop.swingx.painter.ShapePainter;
import org.jdesktop.swingx.painter.effects.InnerShadowPathEffect;

// =======================================================================

public class PasswordFieldUI extends BasicPasswordFieldUI {

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);

	public static ComponentUI createUI(JComponent c) {return new PasswordFieldUI();}

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
		JComponent editor=getComponent();
		RoundRectangle2D.Double r=new RoundRectangle2D.Double(0,0,editor.getWidth(),editor.getHeight(),6,6);
		ShapePainter sp=new ShapePainter(r,(editor.isEnabled()) ? editor.getBackground() : UIManager.getDefaults().getColor("TextField.disabledBackground"));
		sp.setAntialiasing(true);
		sp.setStyle(ShapePainter.Style.FILLED);
		sp.setAreaEffects(new InnerShadowPathEffect());
		sp.paint(g2,null,editor.getWidth(),editor.getHeight());
		g2.dispose();
	}
}

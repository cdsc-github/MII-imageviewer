/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalButtonUI;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.ShapePainter;

// =======================================================================
// Loosely adapted from JGoodies. These buttons render true rounded
// rectangles, as opposed to the "faked" rounded rectangles that the
// original code used (based on determining, for example, the
// background color of the component, instead of the background of the
// containing object, opaqueness, etc.). Also, overrode the
// buttonPressed painting, and now use Graphics2D anti-aliased
// rendering when possible.

public class ButtonUI extends MetalButtonUI {

	private static final ButtonUI INSTANCE=new ButtonUI();
	private static final Paint BUTTON_PAINT=(Paint)UIManager.get("Button.gradientBackground");
	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
	private static final Painter BP=(Painter)UIManager.get("Button.backgroundPainter");

	private static BufferedImage HALO=null;

	// =======================================================================

	static {try {HALO=ImageIO.read(new File("resources/icons/swing/halo.png"));} catch (Exception exc) {exc.printStackTrace();}}

	protected boolean borderPaintsFocus=false;

	public static ComponentUI createUI(JComponent b) {return INSTANCE;}

	public void installDefaults(AbstractButton b) {

		super.installDefaults(b);
		borderPaintsFocus=Boolean.TRUE.equals(UIManager.get("Button.borderPaintsFocus"));
		b.putClientProperty("Button.ghostValue",new Float(0.0f));
		b.putClientProperty("Button.glow",Boolean.TRUE);
	}

	protected void installListeners(AbstractButton b) {

		super.installListeners(b);
		HighlightHandler hh=new HighlightHandler(b);
		b.putClientProperty("Button.highlightHandler",hh);
		b.addMouseListener(hh);
	}

	protected void uninstallListeners(AbstractButton b) {

		HighlightHandler hh=(HighlightHandler)b.getClientProperty("Button.highlightHandler");
		if (hh!=null) {hh.release(); b.removeMouseListener(hh);}
		super.uninstallListeners(b);
	}

	// =======================================================================

	protected void updateButton(Graphics g, JComponent c) {
		
		if (c.isOpaque()) {
			AbstractButton b=(AbstractButton)c;
			Container parent=b.getParent();
			if (parent!=null && (parent instanceof JToolBar || parent.getParent() instanceof JToolBar))  {
				c.setOpaque(false);
			} else if (b.isContentAreaFilled()) {
				Graphics2D g2=(Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
				Integer roundedCorner=(Integer)c.getClientProperty("Button.rounded");
				RoundRectangle2D.Double r=new RoundRectangle2D.Double(0,0,c.getWidth(),c.getHeight(),(roundedCorner==null) ? 6 : roundedCorner.intValue(),(roundedCorner==null) ? 6 : roundedCorner.intValue());
				ShapePainter sp=new ShapePainter(r,BUTTON_PAINT);
				sp.setAntialiasing(true);
				sp.setPaintStretched(true);
				// sp.setStyle(ShapePainter.Style.FILLED);
				// sp.setClipPreserved(true);
				// GlossPainter gp1=new GlossPainter();
				// gp1.setPaint(new Color(1.0f,1.0f,1.0f,0.15f));
				// gp1.setPosition(GlossPainter.GlossPosition.TOP);
				// gp1.setAntialiasing(true);
				// CompoundPainter cp=new CompoundPainter(sp,gp1);
				// cp.setAntialiasing(true);
				// cp.setClipPreserved(true);
				Shape currentClip=g2.getClip();
				g2.setClip(r);
				// cp.paint(g2,c,c.getWidth(),c.getHeight());
				sp.paint(g2,c,c.getWidth(),c.getHeight());
				g2.setClip(currentClip);
				if (b.isEnabled()) {
					Boolean paintGlow=(Boolean)b.getClientProperty("Button.glow");
					Float f=(Float)b.getClientProperty("Button.ghostValue");
					if ((f!=null)&&(Boolean.TRUE.equals(paintGlow))) {
						float ghostValue=f.floatValue();
						g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,ghostValue));
						g2.drawImage(HALO,1,1,c.getWidth()-2,c.getHeight()-2,null);
						g2.setComposite(DEFAULT_AC);
					}
				} 
			}
		} 
	}

	public void update(Graphics g, JComponent c) {updateButton(g,c); paint(g,c);}

	// =======================================================================

	protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {

		Graphics2D g2=(Graphics2D)g;
		Composite oldComposite=g2.getComposite();
		g2.setComposite(DEFAULT_AC);	
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintText(g2,c,textRect,text);
		g2.setComposite(oldComposite);
	}

	protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {

	 if (borderPaintsFocus) return;
	 if (b.isSelected()) return;
	 if (b.getModel().isRollover()) return;
	 int width=b.getWidth()-1;
	 int height=b.getHeight()-1;
	 g.setColor(getFocusColor());
	 Graphics2D g2=(Graphics2D)g;
	 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	 g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
	 int offset=(b instanceof JButton) ? 0 : 1; 
	 g2.drawRoundRect(offset,offset,width-(2*offset),height-(2*offset),4,4);
	}

	// =======================================================================

	protected void paintButtonPressed(Graphics g, AbstractButton b) {

		if (b.isContentAreaFilled()) {
			Graphics2D g2=(Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
	    g2.setColor(getSelectColor());
			BP.paint(g2,b,b.getWidth(),b.getHeight());
		}
	}

	// =======================================================================
	
	private final class HighlightHandler extends MouseAdapter {

		Animator a=null;
		AbstractButton b=null;

		public HighlightHandler(AbstractButton b) {this.b=b;}

		public void mouseEntered(MouseEvent e) {if (a!=null && a.isRunning()) a.stop(); a=new Animator(175,new AnimateGhost(true,b)); a.start();}
		public void mouseExited(MouseEvent e) {if (a!=null && a.isRunning()) a.stop(); a=new Animator(175,new AnimateGhost(false,b)); a.start();}
		public void mouseReleased(MouseEvent e) {if (a!=null && a.isRunning()) a.stop(); a=new Animator(175,new AnimateGhost(false,b)); a.start();}
		
		public void release() {b=null; a=null;}
	}

	// =======================================================================

	private final class AnimateGhost implements TimingTarget {

		AbstractButton b=null;
		boolean forward;
    float oldValue;

		public AnimateGhost(boolean forward, AbstractButton b) {this.forward=forward; Float f=(Float)b.getClientProperty("Button.ghostValue"); oldValue=f.floatValue(); this.b=b;}

		public void timingEvent(float fraction) {

			float ghostValue=oldValue+fraction*(forward ? 0.67f : -0.67f);
			if (ghostValue>0.67f) {
				ghostValue=0.67f;
			} else if (ghostValue<0.0f) {
				ghostValue=0.0f;
			}
			b.putClientProperty("Button.ghostValue",new Float(ghostValue));
			b.repaint();
		}

		public void begin() {}
		public void end() {b=null;}
		public void repeat() {}
	}
}



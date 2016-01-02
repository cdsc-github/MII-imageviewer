/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import javax.swing.text.View;

import org.jdesktop.swingx.painter.Painter;

// =======================================================================

public class ToggleButtonUI extends MetalToggleButtonUI {

	private static final Painter BP=(Painter)UIManager.get("Button.backgroundPainter");
	private static ToggleButtonUI INSTANCE=new ToggleButtonUI();

	protected boolean borderPaintsFocus=false;

	public static ComponentUI createUI(JComponent b) {return INSTANCE;}

	public void installDefaults(AbstractButton b) {
        
		super.installDefaults(b);
		b.putClientProperty("ToggleButton.buttonPaintsRollover",Boolean.TRUE);
		b.putClientProperty("ToggleButton.buttonPaintsPress",Boolean.TRUE);
		borderPaintsFocus=Boolean.TRUE.equals(UIManager.get("ToggleButton.borderPaintsFocus"));
	}

	// =======================================================================

	protected void updateButton(Graphics g, JComponent c) {
		
		AbstractButton b=(AbstractButton)c;
		if (c.isOpaque()) {
			Container parent=b.getParent();
			ButtonModel model=b.getModel();
			if ((parent!=null)&&(parent instanceof JToolBar || parent.getParent() instanceof JToolBar)) {
				c.setOpaque(false);
			} else if (b.isContentAreaFilled()) {
				paintButton(g,c);
			} 
		}
	}

	public void update(Graphics g, JComponent c) {updateButton(g,c); paint(g,c);}

	// =======================================================================

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
		// buip.setFocusColor(getFocusColor());
		// buip.paintFocus(g,b,viewRect,textRect,iconRect);
	}

	// =======================================================================
	
	protected void paintButtonPressed(Graphics g, AbstractButton b) {

		Boolean cp=(Boolean)(b.getClientProperty("ToggleButton.buttonPaintsPress"));
		if (Boolean.TRUE.equals(cp)) {
			Graphics2D g2=(Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			g2.setColor(getSelectColor());
			BP.paint(g2,b,b.getWidth(),b.getHeight());
			}
		// buip.setSelectColor(getSelectColor());
		// buip.paintButtonPressed(g,b);
	}

	// =======================================================================

	public void paint(Graphics g, JComponent c) {

		AbstractButton b=(AbstractButton)c;
		ButtonModel model=b.getModel();
		Dimension size=b.getSize();
		FontMetrics fm=g.getFontMetrics();
		Insets i=c.getInsets();

		Boolean cp=(Boolean)(c.getClientProperty("ToggleButton.buttonPaintsRollover"));
		if ((Boolean.TRUE.equals(cp))&&(model.isRollover())) {
			paintButton(g,c); 
		}

		Rectangle viewRect=new Rectangle(size);
		viewRect.x+=i.left;
		viewRect.y+=i.top;
		viewRect.width-=(i.right+viewRect.x);
		viewRect.height-=(i.bottom+viewRect.y);

		Rectangle iconRect=new Rectangle();
		Rectangle textRect=new Rectangle();
		Font f=c.getFont();
		g.setFont(f);

		String text=SwingUtilities.layoutCompoundLabel(c,fm,b.getText(),b.getIcon(),b.getVerticalAlignment(),b.getHorizontalAlignment(),
																									 b.getVerticalTextPosition(),b.getHorizontalTextPosition(),viewRect,iconRect,
																									 textRect,(b.getText()==null) ? 0 : b.getIconTextGap());
		
		g.setColor(b.getBackground());
		if (model.isArmed() && model.isPressed() || model.isSelected()) paintButtonPressed(g,b);
		if (b.getIcon()!=null) paintIcon(g,b,iconRect);
		if (text!=null && !text.equals("")) {
			View v=(View)c.getClientProperty(BasicHTML.propertyKey);
			if (v!=null) v.paint(g,textRect); else paintText(g,c,textRect,text);
		}
		if (b.isFocusPainted() && b.hasFocus()) paintFocus(g,b,viewRect,textRect,iconRect);
	}

	// =======================================================================

	protected void paintButton(Graphics g, JComponent c) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		BP.paint(g2,c,c.getWidth(),c.getHeight());
		g2.dispose();
	}

	// =======================================================================

	/*		
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new ImageViewerLookAndFeel());
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		} 
		javax.swing.JFrame frame=new javax.swing.JFrame("Test");
		javax.swing.JPanel panel=new javax.swing.JPanel();
		javax.swing.JToggleButton button=new javax.swing.JToggleButton("testing");
		button.setForeground(Color.WHITE);
		button.setBackground(Color.BLACK);
		panel.add(button);
		frame.setContentPane(panel);
		frame.pack();
		frame.setSize(512,512);
		frame.setVisible(true);
	}
	*/
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;

import javax.swing.border.EtchedBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;

import com.l2fprod.common.swing.plaf.ButtonBarButtonUI;
import com.l2fprod.common.swing.plaf.basic.BasicButtonBarUI;

// =======================================================================
// Adaptation for l2fprod's ButtonBar swing component.  Turned on
// anti-aliasing and better rendering for graphics2d components.

public class ImageViewerButtonBarUI extends BasicButtonBarUI {

  public static ComponentUI createUI(JComponent c) {return new ImageViewerButtonBarUI();}

	// =======================================================================

  protected void installDefaults() {

    Border b=bar.getBorder();
    if (b==null || b instanceof UIResource) bar.setBorder(new BorderUIResource(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
																																																	BorderFactory.createEmptyBorder(2,2,2,2))));
    if (bar.getBackground()==null || bar.getBackground() instanceof UIResource) {
      bar.setBackground(new ColorUIResource(128,128,128));
      bar.setOpaque(true);
    }
  }

	// =======================================================================

  public void installButtonBarUI(AbstractButton button) {

    button.setUI(new ButtonUI());
    button.setHorizontalTextPosition(JButton.RIGHT);
    button.setVerticalTextPosition(JButton.CENTER);
  }

	// =======================================================================

  static class ButtonUI extends BasicButtonUI implements ButtonBarButtonUI {

    private static Color selectedBackground=new Color(200,200,200);
    private static Color selectedBorder=Color.darkGray;
    private static Color selectedForeground=Color.black;
    private static Color unselectedForeground=Color.white;

    public void installUI(JComponent c) {

      super.installUI(c);
      AbstractButton button=(AbstractButton)c;
      button.setOpaque(false);
      button.setRolloverEnabled(true);
      button.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
    }

    public void paint(Graphics g, JComponent c) {

      AbstractButton button=(AbstractButton)c;
			Graphics2D g2=(Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
      if (button.getModel().isSelected()) {
        Color oldColor=g.getColor();
        g2.setColor(selectedBackground);
        g2.fillRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,5,5);
        g2.setColor(selectedBorder);
        g2.drawRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,5,5);
        g2.setColor(oldColor);
      }

      // This is a tweak to get the View with the color we expect it to be. We
      // change directly the color of the button

      if (c.getClientProperty(BasicHTML.propertyKey) != null) {
        ButtonModel model=button.getModel();
        if (model.isEnabled()) {
          if (model.isSelected()) {
            button.setForeground(selectedForeground);
          } else {
            button.setForeground(unselectedForeground);
          }
        } else {
          button.setForeground(unselectedForeground.darker());
        }
      }
      super.paint(g2,c);
			g2.dispose();
    }

    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {

      ButtonModel model=b.getModel();
      FontMetrics fm=g.getFontMetrics();
      int mnemonicIndex=b.getDisplayedMnemonicIndex();
      Color oldColor=g.getColor();
      if (model.isEnabled()) {
        if (model.isSelected()) {
          g.setColor(selectedForeground);
        } else {
          g.setColor(unselectedForeground);
        }
      } else {
        g.setColor(unselectedForeground.darker());
      }
			Graphics2D g2=(Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
      BasicGraphicsUtils.drawStringUnderlineCharAt(g2,text,mnemonicIndex,textRect.x+getTextShiftOffset(),textRect.y+fm.getAscent()+getTextShiftOffset());
			g2.dispose();
      g.setColor(oldColor);
    }
  }
}

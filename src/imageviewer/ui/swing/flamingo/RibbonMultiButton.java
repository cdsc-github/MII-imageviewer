/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import javax.swing.text.View;

import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.JCommandButtonPanel;
import org.jvnet.flamingo.common.JIconPopupPanel;
import org.jvnet.flamingo.common.JPopupPanel;
import org.jvnet.flamingo.common.PopupPanelManager;

// =======================================================================

public class RibbonMultiButton extends AbstractMultiButton {

	private static final Color BUTTON_TOP_HIGHLIGHT_LIGHT=UIManager.getColor("RibbonBandButton.topHighlightLight");
	private static final Color BUTTON_TOP_HIGHLIGHT_DARK=UIManager.getColor("RibbonBandButton.topHighlightDark");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_LIGHT=UIManager.getColor("RibbonBandButton.bottomHighlightLight");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_DARK=UIManager.getColor("RibbonBandButton.bottomHighlightDark");
	private static final Color BUTTON_BORDER=UIManager.getColor("RibbonBandButton.borderColor");

	private static final Font BUTTON_FONT=UIManager.getFont("Button.font");

	// =======================================================================
	
	boolean largePopup=false, useExtraText=true;
	int maxPopupWidth=300;

	public RibbonMultiButton(boolean largePopup, int maxPopupWidth, boolean useExtraText) {

		super();
		this.maxPopupWidth=maxPopupWidth;
		this.largePopup=largePopup;
		this.useExtraText=useExtraText;
	}

	// =======================================================================

	public void generatePopupPanel() {

		ArrayList<JComponent> buttons=new ArrayList<JComponent>();
		Insets i=new Insets(1,1,1,1);
		int index=0;
		for (final JCommandButton jcb : buttonList) {
			MenuButton mb=(!largePopup) ? createSmallMenuButton(jcb,i) : createLargeMenuButton(jcb,i,useExtraText);
			if (index==0) mb.setSelected(true);
			jcb.getModel().addItemListener(new ClonedButtonSyncListener(mb));
			mb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					PopupPanelManager.defaultManager().hideLastPopup();
					setCurrentButton(jcb);
					jcb.doClick();
				}
			});				
			buttons.add(mb);
			index++;
		}
		RibbonMenuPanel rmp=new RibbonMenuPanel(largePopup,(largePopup) ? LARGE_BUTTON_SIZE : SMALL_BUTTON_SIZE,maxPopupWidth,buttons);
		JPopupPanel jpp=new JPopupPanel(rmp,new Dimension((int)Math.min(maxPopupWidth,rmp.getComputedWidth()),rmp.getComputedHeight()));
		jpp.putClientProperty("paintBandTitle",Boolean.FALSE);
		for (JCommandButton jcb : buttonList) jcb.setPopupPanel(jpp);
	}

	// =======================================================================

	public MenuButton createSmallMenuButton(final JCommandButton sourceButton, Insets i) {

		MenuButton mb=new MenuButton(sourceButton,i) {
			public void paintComponent(Graphics g) {super.paintComponent(g); paintSmallMenuButton(g,SMALL_BUTTON_SIZE,SMALL_BUTTON_ICON_SIZE);}
		};
		mb.setBorder(null);
		mb.setOpaque(false);
		mb.setTitle(sourceButton.getTitle());
		return mb;
	}

	// =======================================================================

	public MenuButton createLargeMenuButton(JCommandButton sourceButton, Insets i, boolean showExtraText) {

		String text=sourceButton.getTitle();
		String extraText=sourceButton.getExtraText();

		MenuButton mb=new MenuButton(sourceButton,i) {

			public void paintComponent(Graphics g) {
				Graphics2D g2=(Graphics2D)g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
				boolean isMouseOver=(getMousePosition(true)!=null);
				int height=getHeight()-(i.top+i.bottom);
				super.paintComponent(g);
				if ((!isMouseOver)&&(isSelected())) {
					g2.setColor(BUTTON_TOP_HIGHLIGHT_LIGHT);
					g2.fillRoundRect(i.left+4,i.top+3,40,40,4,4);
					g2.setColor(BUTTON_BORDER);
					g2.setStroke(new BasicStroke(2.0f));
					g2.drawRoundRect(i.left+4,i.top+3,40,40,4,4);
				}
				bsi.setDimension(new Dimension(LARGE_BUTTON_ICON_SIZE,LARGE_BUTTON_ICON_SIZE));
				bsi.paintIcon(this,g2,i.left+8,i.top+((height-LARGE_BUTTON_ICON_SIZE)/2));
				g2.setFont(BUTTON_FONT);
				View v=(View)getClientProperty("html");
				int textWidth=(int)Math.min(v.getPreferredSpan(View.X_AXIS),maxPopupWidth-LARGE_BUTTON_SIZE-10);
				int textHeight=(int)v.getPreferredSpan(View.Y_AXIS);
				v.paint(g2,new Rectangle(50,1+(int)((height-textHeight)/2),textWidth,textHeight));
				g2.dispose();
			}
		};
		mb.setBorder(null);
		mb.setOpaque(false);
		mb.setForeground(Color.darkGray);
		if (showExtraText) {
			mb.setText((extraText!=null) ? ("<html><body><b>"+text+"</b><br>"+extraText+"</body></html>") : ("<html><body><b>"+text+"</b><br></body></html>"));
		} else {
			mb.setText("<html><body>"+text+"</body></html>");
		}
		mb.setTitle(sourceButton.getTitle());
		return mb;
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

// =======================================================================

public class ScrollablePanel extends JPanel implements Scrollable {

	public ScrollablePanel() {this(FlowLayout.LEFT);}
	public ScrollablePanel(int flowLayoutType) {super(new FlowLayout(flowLayoutType));}
	public ScrollablePanel(int flowLayoutType, int hgap, int vgap) {super(new FlowLayout(flowLayoutType,hgap,vgap));}

	// =======================================================================
	
	public Dimension getPreferredSize() {

		if (getParent()==null) return getPreferredSize();
		FlowLayout flow=(FlowLayout)getLayout();
		Component[] comps=getComponents();
		int w=getParent().getWidth();
		int rv=0;
		for (int loop=0, count=comps.length; loop<count; loop++) {
			Component c=comps[loop];
			Rectangle r=c.getBounds();
			int h=r.y+r.height;
			if (h>rv) rv=h;
		}
		rv+=flow.getVgap();
		return new Dimension(w,rv);
	}

	// =======================================================================
	
	public Dimension getPreferredScrollableViewportSize() {return getPreferredSize();}
	
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {return (orientation==SwingConstants.HORIZONTAL) ? visibleRect.width : visibleRect.height;}
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {int hundredth=(orientation==SwingConstants.VERTICAL	? getParent().getHeight() : getParent().getWidth())/100; return (hundredth==0 ? 1 : hundredth);}
	
	public boolean getScrollableTracksViewportHeight() {return (getPreferredSize().height<getParent().getHeight());}
	public boolean getScrollableTracksViewportWidth() {return true;}
}

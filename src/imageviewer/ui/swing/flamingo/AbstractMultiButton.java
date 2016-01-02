/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.CardLayout;
import java.awt.Dimension;

import java.util.ArrayList;
import javax.swing.JPanel;

import org.jvnet.flamingo.common.ElementState;
import org.jvnet.flamingo.common.JCommandButton;

// =======================================================================

public abstract class AbstractMultiButton extends JCommandButton {

	protected static final int LARGE_BUTTON_SIZE=48;
	protected static final int LARGE_BUTTON_ICON_SIZE=32;
	protected static final int SMALL_BUTTON_SIZE=24;
	protected static final int SMALL_BUTTON_ICON_SIZE=16;

	// ======================================================================= 

	protected ArrayList<JCommandButton> buttonList=new ArrayList<JCommandButton>();
	protected JCommandButton currentButton=null;
	protected JPanel containingPanel=null;
	protected CardLayout cl=new CardLayout();

	public AbstractMultiButton() {

		super("MultiButton",new BasicResizableIcon("resources/icons/ribbon/32x32/empty.png")); 
		containingPanel=new JPanel(cl,true);
		containingPanel.setOpaque(false);
		add(containingPanel);
	}

	// ======================================================================= 

	public String getUIClassID() {return "RibbonMultiButtonUI";}

	// =======================================================================

	public void addButton(JCommandButton jcb) {if (buttonList.isEmpty()) setCurrentButton(jcb); buttonList.add(jcb); containingPanel.add(jcb,jcb.getTitle());}
	public void setCurrentButton(JCommandButton jcb) {currentButton=jcb; cl.show(containingPanel,jcb.getTitle()); repaint();}

	// =======================================================================
	
	public void setBounds(int x, int y, int width, int height) {super.setBounds(x,y,width,height); for (JCommandButton jcb : buttonList) jcb.setBounds(x,y,width,height);}
	public void setState(ElementState state, boolean toUpdateIcon) {super.setState(state,toUpdateIcon); for (JCommandButton jcb : buttonList) jcb.setState(state,toUpdateIcon);}
	public void setEnabled(boolean x) {super.setEnabled(x); for (JCommandButton jcb : buttonList) jcb.setEnabled(x);}

	// =======================================================================

	public Dimension getPreferredSize() {

		int maxWidth=0, maxHeight=0;
		for (JCommandButton jcb : buttonList) {
			Dimension d=jcb.getPreferredSize();
			if (d.width>maxWidth) maxWidth=d.width;
			if (d.height>maxHeight) maxHeight=d.height;
		}
		return new Dimension(maxWidth,maxHeight);
	}
}

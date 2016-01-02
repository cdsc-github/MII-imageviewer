/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;

// =======================================================================

public class OptionPaneUI extends BasicOptionPaneUI {

	public static ComponentUI createUI(JComponent b) {return new OptionPaneUI();}

	// =======================================================================

	protected void addIcon(Container top) {
		
		Icon sideIcon=getIcon();
		if (sideIcon!=null){
			JLabel iconLabel=new JLabel(sideIcon);
			iconLabel.setOpaque(false);
			iconLabel.setVerticalAlignment(SwingConstants.TOP);
			iconLabel.setBorder(new EmptyBorder(5,0,0,0));
			top.add(iconLabel,BorderLayout.BEFORE_LINE_BEGINS);
		}
	}

	// =======================================================================

	protected Container createButtonArea() {

		JPanel bottom=new JPanel(new OptionPaneButtonAreaLayout(true,6));
		bottom.setBorder(UIManager.getBorder("OptionPane.buttonAreaBorder"));
		addButtonComponents(bottom,getButtons(),getInitialValueIndex());
		bottom.setOpaque(false);
		return bottom;
	}

	// =======================================================================

	protected Container createSeparator() {

		JPanel separator=new JPanel();
		separator.setBorder(new LineBorder(Color.gray));
		separator.setOpaque(false);
		separator.setPreferredSize(new Dimension(8,1));
		return separator;
	}

	// =======================================================================
	// Set each of the components created in the message area to be
	// non-opaque.

	private void setComponentOpacity(Container c) {

		Component[] comp=c.getComponents();
		for (int i=0; i<comp.length; i++) {
			Component child=comp[i];
			if (child instanceof JComponent) ((JComponent)child).setOpaque(false);
			if (child instanceof Container) setComponentOpacity((Container)child);
		}
		if (c instanceof JComponent) ((JComponent)c).setOpaque(false);
	}

	// =======================================================================
	// Adapt the code from the BasicOptionPaneUI to handle layouts better.
    
	protected Container createMessageArea() {

		JPanel top=new JPanel();
		Border topBorder=(Border)sun.swing.DefaultLookup.get(optionPane,this,"OptionPane.messageAreaBorder");
		if (topBorder!=null) top.setBorder(topBorder);
		top.setLayout(new BorderLayout());

		Container body=new JPanel(new GridBagLayout());
		Container realBody=new JPanel(new BorderLayout());

		body.setName("OptionPane.body");
		realBody.setName("OptionPane.realBody");

		if (getIcon()!=null) {
			JPanel sep=new JPanel();
			sep.setName("OptionPane.separator");
			sep.setPreferredSize(new Dimension(5,1));
	    realBody.add(sep,BorderLayout.BEFORE_LINE_BEGINS);
		}
		realBody.add(body,BorderLayout.CENTER);

		GridBagConstraints cons=new GridBagConstraints();
		cons.gridx=cons.gridy=0;
		cons.gridwidth=GridBagConstraints.REMAINDER;
		cons.gridheight=1;
		cons.anchor=sun.swing.DefaultLookup.getInt(optionPane,this,"OptionPane.messageAnchor",GridBagConstraints.CENTER);
		cons.insets=new Insets(5,0,3,0);

		addMessageComponents(body,cons,getMessage(),getMaxCharactersPerLineCount(),false);
		top.add(realBody,BorderLayout.CENTER);
		addIcon(top);
		setComponentOpacity(top);
		return top;
	}
}

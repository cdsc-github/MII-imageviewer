/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;

import org.jdesktop.swingx.JXPanel;

import com.l2fprod.common.swing.JButtonBar;

// =======================================================================

public class TranslucentPanel extends JXPanel {

	public TranslucentPanel() {super();}
	public TranslucentPanel(LayoutManager layout) {super(layout);}

	// =======================================================================
	// Set each of the components created in the message area to be
	// non-opaque.

	public void setComponentOpacity() {setComponentOpacity(this);}

	private void setComponentOpacity(Container c) {
		
		Component[] comp=c.getComponents();
		for (int i=0; i<comp.length; i++) {
			Component child=comp[i];
			if (child instanceof JComponent) {
				if (child instanceof JTextField) {
					JTextField textField=(JTextField)child;
					textField.setOpaque(true);
					Color bgColor=textField.getBackground();
					textField.setBackground(new Color(bgColor.getRed(),bgColor.getGreen(),bgColor.getBlue(),255));
				} else {
					((JComponent)child).setOpaque(false);
				}
			}
			if (child instanceof Container) setComponentOpacity((Container)child);
		}

		if (c instanceof JScrollPane) {
			JViewport jvp=((JScrollPane)c).getViewport();
			Component view=jvp.getView();
			if (view instanceof Container) setComponentOpacity((Container)view);
			jvp.setOpaque(true);
		}
				
		if (c instanceof JComponent) {
			if (c instanceof JTextField) {
				JTextField textField=(JTextField)c;
				textField.setOpaque(true);
				Color bgColor=textField.getBackground();
				textField.setBackground(new Color(bgColor.getRed(),bgColor.getGreen(),bgColor.getBlue(),255));
			} else if ((c instanceof JButton)||(c instanceof JList)||(c instanceof JButtonBar)) {
				JComponent component=(JComponent)c;
				component.setOpaque(true);
			} else {
				((JComponent)c).setOpaque(false);
			}
		}
	}
}

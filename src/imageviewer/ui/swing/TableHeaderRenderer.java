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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.Painter;

// =======================================================================

public class TableHeaderRenderer extends JXPanel implements TableCellRenderer {

	private static final Painter HEADER_PAINTER=(Painter)UIManager.get("TableHeader.backgroundPainter");

	JLabel textLabel=null;
	Color headerColor=Color.black;

	// =======================================================================

	public TableHeaderRenderer() {this(Color.black);}

	public TableHeaderRenderer(Color headerColor) {

		super(new FlowLayout(FlowLayout.CENTER,0,0)); 
		this.headerColor=headerColor;
		textLabel=new JLabel(); 
		textLabel.setOpaque(false); 
		add(textLabel);
		if (HEADER_PAINTER!=null) setBackgroundPainter(HEADER_PAINTER);
	}

	// =======================================================================

	public void paintComponent(Graphics g) {

		g.setColor(headerColor);
		g.fillRect(0,0,getWidth()-1,getHeight()-1);
		super.paintComponent(g);
		g.setColor(Color.gray);
		g.drawLine(getWidth()-1,0,getWidth()-1,getHeight()-1);
		g.setColor(Color.black);
		g.drawLine(getWidth()-2,0,getWidth()-2,getHeight()-1);
	}

	// =======================================================================
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {

		textLabel.setText(value.toString());
		return this;
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTable;

import imageviewer.model.Image;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.FloatingPanelActionListener;
import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.TableHeaderRenderer;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.Painter;

// =======================================================================

public class ImageInfoPanel extends JXPanel implements FloatingPanelActionListener {
	
	protected static final Color PROP_COLUMN_COLOR=new Color(182,206,251);
	private static final int[] COLUMN_WIDTH=new int[] {135,75,200};

	// =======================================================================

	ImageInfoTableModel model=new ImageInfoTableModel();
	JXTable propertyTable=null;

	public ImageInfoPanel() {

		super(new BorderLayout());
		setBorder(new EmptyBorder(10,5,5,5));
		setSize(250,100);
		setLocation(200,200);

		propertyTable=new JXTable(model) {
			final TableCellRenderer tcr=new CellRenderer();
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
				Component c=super.prepareRenderer(renderer,rowIndex,vColIndex);
				if ((vColIndex==0) && !isCellSelected(rowIndex,vColIndex)) {
					c.setBackground(PROP_COLUMN_COLOR);
				} 
				return c;
			}
			public TableCellRenderer getCellRenderer(int row, int column) {return tcr;}
		};

		propertyTable.setHorizontalScrollEnabled(false);
		propertyTable.setShowHorizontalLines(true);
		propertyTable.setShowVerticalLines(true);
		propertyTable.setColumnMargin(1);
		propertyTable.setColumnControlVisible(false);
		propertyTable.setRolloverEnabled(true);
		propertyTable.getTableHeader().setReorderingAllowed(false); 
		propertyTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		propertyTable.packAll();

		for (int i=0; i<COLUMN_WIDTH.length; i++) {
			TableColumn column=propertyTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(COLUMN_WIDTH[i]);
			column.setHeaderRenderer(new TableHeaderRenderer(Color.black));
    }

		JScrollPane sp=new JScrollPane(propertyTable,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(sp,BorderLayout.CENTER);
		setBackgroundPainter((Painter)UIManager.get("FloatingPanel.backgroundPainter"));
	}

	// =======================================================================

	public Image getImage() {return model.getImage();}
	public void updatePanel(Image i) {model.setImage(i);}

	// =======================================================================

	private class CellRenderer extends DefaultTableCellRenderer {
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)	{

			super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			setHorizontalAlignment(LEFT);
			return this;
		}
	}

	// =======================================================================

	public void closeAction() {MenuAction ma=MenuAction.ACTIONS.get(ApplicationContext.DISPLAY_IMAGE_PROP_WINDOW_COMMAND); ma.getMenuItem().doClick();}
}

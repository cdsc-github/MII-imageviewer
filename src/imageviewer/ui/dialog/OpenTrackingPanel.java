/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import imageviewer.ui.FloatingPanel;
import imageviewer.ui.swing.TableHeaderRenderer;

// =======================================================================

public class OpenTrackingPanel extends JPanel implements ActionListener {

	private static final int[] COLUMN_WIDTH=new int[] {115,70,193,100,100,75,130,50,130,130,200,115,200};
	private static final Color ROW_COLOR1=new Color(20,30,45);
	private static final Color ROW_COLOR2=new Color(0,10,25);
	private static final Color DARK_GREEN=new Color(0,192,0);
	
	// =======================================================================

	TrackingTableModel model=new TrackingTableModel();
	JXTable trackingTable=null;
	JButton closeButton=new JButton("Close");
	JButton clearCompletedButton=new JButton("Clear completed transfers");
	JButton clearErrorButton=new JButton("Clear failed transfers");
	JCheckBox clearCompletedTrackCheckBox=new JCheckBox("Clear entries once original study/series transfer is completed");
	
	public OpenTrackingPanel() {

		super();

		JTextArea trackerDescription=DialogUtil.createTextArea("The image tracker provides detailed information on the status of requested studies/series.  "+
																													 "Approximate statistics on the data transfer rate are shown below and are dependent on your network speed.  "+
																													 "Transfers over slower networks may benefit from optional stream compression (see Advanced options tab).");

		FormLayout fl=new FormLayout("670px,5px,pref:grow,2dlu,pref,2dlu,right:pref","pref,5px,pref,5px,pref,5px,pref,5px,pref:grow,5px,pref");
		setLayout(fl);
		setBorder(new EmptyBorder(5,5,5,5));
		
		trackingTable=new JXTable(model) {
			final TableCellRenderer tcr=new BarCellRenderer();
			public TableCellRenderer getCellRenderer(int row, int column) {if (column==4) return tcr; else return super.getCellRenderer(row,column);}
		};

		trackingTable.setHorizontalScrollEnabled(true);
		trackingTable.setShowHorizontalLines(true);
		trackingTable.setShowVerticalLines(true);
		trackingTable.setColumnMargin(1);
		trackingTable.setColumnControlVisible(false);
		trackingTable.setRolloverEnabled(true);
		trackingTable.setRowSelectionAllowed(false);

		CompoundHighlighter cp=new CompoundHighlighter();
		cp.addHighlighter(new AlternateRowHighlighter(ROW_COLOR1,ROW_COLOR2,Color.white));
		trackingTable.setHighlighters(cp);
		for (int i=0; i<COLUMN_WIDTH.length; i++) {
			TableColumn column=trackingTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(COLUMN_WIDTH[i]);
			column.setHeaderRenderer(new TableHeaderRenderer(Color.black));
    }

		JScrollPane sp=new JScrollPane(trackingTable,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		sp.setPreferredSize(new Dimension(785,450));

		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);
		clearCompletedButton.setActionCommand("clearCompleted");
		clearCompletedButton.addActionListener(this);
		clearErrorButton.setActionCommand("clearFailed");
		clearErrorButton.addActionListener(this);		
		clearCompletedTrackCheckBox.setSelected(true);

		CellConstraints cc=new CellConstraints();
		add(sp,cc.xywh(1,1,1,9));
		add(trackerDescription,cc.xywh(3,1,5,1));
		add(clearCompletedTrackCheckBox,cc.xywh(3,3,5,1));
		add(clearCompletedButton,cc.xy(5,5));
		add(clearErrorButton,cc.xy(7,5));
		add(new JSeparator(),cc.xywh(3,9,5,1));
		add(closeButton,cc.xy(7,11));
	}

	// =======================================================================

	public boolean shouldRemoveOnComplete() {return clearCompletedTrackCheckBox.isSelected();}

	public void addItem(TrackingItem ti) {model.add(ti);}
	public void removeItem(TrackingItem ti) {model.remove(ti);}
	public void updated(TrackingItem ti) {model.updated(ti);}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		String command=e.getActionCommand();
		if ("close".equals(command)) {
			FloatingPanel fp=(FloatingPanel)SwingUtilities.getAncestorOfClass(FloatingPanel.class,this);
			if (fp!=null) fp.actionPerformed(new ActionEvent(this,1,"close"));
		} else if ("clearFailed".equals(command)) {
			model.clearFailed();
		} else if ("clearCompleted".equals(command)) {
			model.clearCompleted();
		}
	}

	// =======================================================================

	private class BarCellRenderer extends JLabel implements TableCellRenderer {

		ProgressBarIcon pbi=new ProgressBarIcon();

		public BarCellRenderer() {
			super();
			setOpaque(true);
			setIcon(pbi);
			setVerticalAlignment(JLabel.CENTER);
			setHorizontalAlignment(JLabel.CENTER);
		}

    public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {

			Object o=table.getValueAt(row,column);
			if (o instanceof Double) {
				double percentComplete=((Double)o).doubleValue();
				pbi.setPercentage(percentComplete);
				setToolTipText(String.format("%.2f",percentComplete)+"% downloaded");
			} 
			setBackground(((row % 2)==0) ? ROW_COLOR2 : ROW_COLOR1);
			return this;
		}
	}

	// =======================================================================

	private class ProgressBarIcon implements Icon {

		double percentage=0;
		float[] rgb=new float[] {0f,0.7f,0f};

    public int getIconHeight() {return 10;}
    public int getIconWidth() {return 90;}

		public void setPercentage(double x) {percentage=x;}

    public void paintIcon(Component c, Graphics g, int x, int y) {

			Graphics2D g2=(Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			g2.setColor(Color.gray);
			g2.fillRect(x,y,90,9);

			g2.setPaint(new GradientPaint(x,y,new Color(0.8f,0.8f,0.8f,0.6f),x,y+5,new Color(rgb[0],rgb[1],rgb[2],0.3f)));
			g2.fillRect(x,y,(int)(90*(percentage/100)),5);        
			g2.setPaint(new GradientPaint(x,y+5,new Color(rgb[0],rgb[1],rgb[2],0f).darker().darker(),x,y+10,new Color(rgb[0],rgb[1],rgb[2],0.27f).darker()));
			g2.fillRect(x,y+5,(int)(90*(percentage/100)),5);
			g2.setColor(Color.darkGray);
			g2.drawRect(x,y,89,9);
    }
	}
}

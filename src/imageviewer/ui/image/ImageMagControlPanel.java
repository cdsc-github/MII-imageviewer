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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;

import imageviewer.system.GarbageCollectionTimer;
import imageviewer.system.ImageReaderManager;

import imageviewer.ui.DataPanel;
import imageviewer.ui.swing.ImageViewerLookAndFeel;
import imageviewer.ui.swing.ImageViewerLookAndFeelAddons;

// =======================================================================

public class ImageMagControlPanel extends DataPanel implements ActionListener {

	private static final int NORTH_ARROW=0;
	private static final int SOUTH_ARROW=1;

	// =======================================================================

	ImageMagPanel imp=null;

	public ImageMagControlPanel(ArrayList<? extends ImageSequenceGroup> isg, int thumbnailSize, int panelWidth) {

		super();
		setLayout(new BorderLayout(0,1));
		JButton topButton=new ArrowButton(NORTH_ARROW,11); 
		JButton bottomButton=new ArrowButton(SOUTH_ARROW,11); 
		topButton.setActionCommand("top"); 
		topButton.addActionListener(this);
		topButton.putClientProperty("Button.rounded",0);
		bottomButton.setActionCommand("bottom");
		bottomButton.addActionListener(this);
		bottomButton.putClientProperty("Button.rounded",0);
		imp=new ImageMagPanel(isg,thumbnailSize,panelWidth);

		add(topButton,BorderLayout.NORTH);
		add(imp,BorderLayout.CENTER);
		add(bottomButton,BorderLayout.SOUTH);
		setSize(panelWidth,imp.getHeight()+32);
	}

	// =======================================================================

	public void paintLayers(Graphics g) {}
	public void groupPropertyChange(Object source, String[] propertyNames, Object[] values) {}

	// =======================================================================

	private static class ArrowButton extends JButton implements UIResource {

		private final int buttonWidth, direction;

		public ArrowButton(int direction, int buttonWidth) {

			this.direction=direction; 
			this.buttonWidth=buttonWidth; 
			setOpaque(true);
			setRequestFocusEnabled(false);  
			setRolloverEnabled(false); 
			setBorderPainted(false);
			setSelected(false);
			setFocusPainted(false);
			setBackground(Color.black);
		}

		protected void paintComponent(Graphics g) {super.paintComponent(g); paintArrow(g);}

		private void paintArrow(Graphics g) {

			Graphics2D g2=(Graphics2D)g.create();
			int arrowWidth=9, arrowHeight=5;
			int x=1+(getWidth()-arrowWidth)/2;
			int y=(getHeight()-arrowHeight)/2;
			boolean isEnabled=isEnabled();
			g2.translate(x,y);
			g2.setColor(isEnabled ? ImageViewerLookAndFeel.getControlInfo().brighter() : ImageViewerLookAndFeel.getControlDisabled());
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);		
			Color shadow=isEnabled ? ImageViewerLookAndFeel.getControlShadow() : UIManager.getColor("ScrollBar.highlight");

			switch (direction) {

			  case NORTH_ARROW: g2.fillRect(0,4,9,1);
					                g2.fillRect(1,3,7,1);
													g2.fillRect(2,2,5,1);
													g2.fillRect(3,1,3,1);
													g2.fillRect(4,0,1,1);
													g2.setColor(shadow);	
													g2.fillRect(1,5,9,1);
													break;
			  case SOUTH_ARROW:	g2.fillRect(0,0,9,1);
					                g2.fillRect(1,1,7,1);
													g2.fillRect(2,2,5,1);
													g2.fillRect(3,3,3,1);
													g2.fillRect(4,4,1,1);
													g2.setColor(shadow); 
													g2.drawLine(5,4,8,1); 
													g2.drawLine(5,5,9,1);
													break;
			}
			g2.dispose();
		}

		public Dimension getPreferredSize() {return new Dimension(buttonWidth,buttonWidth);}
		public Dimension getMinimumSize() {return getPreferredSize();}
		public Dimension getMaximumSize() {return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);}
	}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		if ("top".equals(e.getActionCommand())) {imp.jumpTo(ImageMagPanel.SEQUENCE_TOP); return;}
		else if ("bottom".equals(e.getActionCommand())) {imp.jumpTo(ImageMagPanel.SEQUENCE_BOTTOM); return;}
	}

	// =======================================================================

	 public static void main(String[] args) {       
		 
		 if (args.length==1) {

			 try {
				 UIManager.setLookAndFeel(new ImageViewerLookAndFeel());
				 LookAndFeelAddons.setAddon(ImageViewerLookAndFeelAddons.class);
			 } catch (Exception exc) {
				 exc.printStackTrace();
			 }

			 Timer timer=new Timer();
			 timer.schedule(new GarbageCollectionTimer(),5000,2500);
			 ImageReader ir=ImageReaderManager.getInstance().getImageReader("DICOM");
			 JFrame frame=new JFrame("imageViewer | magPanel demo");
			 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			 ImageSequence is=ir.readDirectory(args[0],true);
			 ImageMagControlPanel imcp=new ImageMagControlPanel(ir.organizeByStudy(ir.readImages(args[0],true)),64,640);
			 // ImageMagDetailPanel imdp=new ImageMagDetailPanel();
			 // imdp.setImage(is.getImage(0));
			 JPanel jp=new JPanel(new BorderLayout());
			 jp.add(imcp,BorderLayout.CENTER);
			 // jp.add(imdp,BorderLayout.EAST);
			 frame.getContentPane().add(jp);
			 frame.pack();
			 frame.setSize(512,768);
			 frame.setVisible(true);
		 }
	 }
}

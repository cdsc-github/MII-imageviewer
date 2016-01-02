/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.animation;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

import javax.media.opengl.GL;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;

import javax.swing.JComponent;
import javax.swing.JFrame;

// =======================================================================

public class TransitionPanel extends GLJPanel implements AnimationListener {

	BufferedImage img1=null, img2=null;
	TransitionRenderer tr=null;

	private TransitionPanel() {

		super(OpenGLInfo.getCapabilities(true),null,null); 
		setBackground(Color.black); 
		setOpaque(true);
		tr=new TransitionRenderer(false);
		tr.addAnimationListener(this);
		addGLEventListener(tr);
	}

	// =======================================================================

	public TransitionRenderer getTransitionRenderer() {return tr;}

	private BufferedImage generateComponentImage(JComponent c) {BufferedImage bi=new BufferedImage(c.getWidth(),c.getHeight(),BufferedImage.TYPE_INT_ARGB); c.paint(bi.getGraphics()); return bi;}

	public void execute(JComponent c1, JComponent c2) {img1=generateComponentImage(c1);	img2=generateComponentImage(c2); tr.execute(img1,img2);}
	public void execute(BufferedImage startImage, BufferedImage endImage) {tr.execute(startImage,endImage);}

	// =======================================================================

	public void animationStarted() {}
	public void animationCompleted() {if (img1!=null) img1.flush(); if (img2!=null) img2.flush(); img1=null; img2=null;}

	// =======================================================================

	public static void main(String args[]) {

		if (args.length==2) {
			JFrame frame=new JFrame("3D JOGL Transition Panel Demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			TransitionPanel tp=new TransitionPanel();
			frame.setSize(512,512);
			frame.setContentPane(tp);
			frame.setVisible(true);
			try {
				BufferedImage bi1=ImageIO.read(new File(args[0]));
				BufferedImage bi2=ImageIO.read(new File(args[1]));
				tp.execute(bi1,bi2);
				bi1.flush();
				bi2.flush();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
}

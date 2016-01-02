/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.jogl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.util.Timer;

import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.TileScheduler;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import imageviewer.model.ImageSequence;
import imageviewer.system.GarbageCollectionTimer;

import imageviewer.ui.image.jogl.gl.CompositeGLJPanel;

// =======================================================================

public class TextureModel3D extends CompositeGLJPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

	TextureVolume volume=null;
	float rotationX=0.0f, rotationY=0.0f, rotationZ=0f, scale=1.0f;
	int lastX=0, lastY=0;

	public TextureModel3D(String directory, String imageType, int threshold, int seriesNumber) {super(false,true); initialize(new TextureVolume(directory,imageType,threshold,seriesNumber));}
	public TextureModel3D(String directory, String imageType, int threshold) {super(false,true); initialize(new TextureVolume(directory,imageType,threshold));}
	public TextureModel3D(ImageSequence is, int threshold) {super(false,true); initialize(new TextureVolume(is,threshold));}
 
	// =======================================================================
    
	private void initialize(TextureVolume volume) {
		
		this.volume=volume;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	protected void init3DResources(GL gl, GLU glu) {super.init3DResources(gl,glu); volume.setTextureInitialized(false);}

	// =======================================================================
	// Rendering methods for the Swing components in the background and
	// foreground, and the middle 3D scene.

	protected void render2DBackground(Graphics2D g2d) {g2d.setColor(Color.BLACK);	g2d.fillRect(0,0,getWidth(),getHeight());}
    
	protected void render3DScene(GL gl, GLU glu) {

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
		gl.glPushMatrix();
		volume.render(gl,glu,rotationX,rotationY,rotationZ);
		gl.glPopMatrix();
	}
    
	protected void render2DForeground(Graphics2D g2d) {}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		
		if (e.isShiftDown()) {
			volume.setPicking(true);
			volume.setSelectionPoint(e.getX(),e.getY());
			repaint();
		} else {
			lastX=e.getX(); 
			lastY=e.getY();
		}
	}
			
	public void mouseDragged(MouseEvent e) {

		if (e.isShiftDown()) {

		} else {
			int x=e.getX();
			int y=e.getY();
			Dimension size=e.getComponent().getSize();
			float thetaY=360.0f*((float)(lastX-x)/(float)size.width);
			float thetaX=360.0f*((float)(lastY-y)/(float)size.height);
			lastX=x;
			lastY=y;
			if (SwingUtilities.isLeftMouseButton(e)) {
				rotationX+=thetaX; if (rotationX>360) rotationX-=360; if (rotationX<0) rotationX+=360;
				rotationY+=thetaY; if (rotationY>360) rotationY-=360; if (rotationY<0) rotationY+=360;
			} else {
				rotationZ+=thetaY; if (rotationZ>360) rotationZ-=360; if (rotationZ<0) rotationZ+=360;
			}
			repaint();
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {

		int advanceAmount=e.getWheelRotation();
		scale-=(float)(advanceAmount*0.02f);
		if (scale<0.01f) scale=0.01f;
		if (scale>5.0f) scale=5.0f;
		volume.setScale(scale);
		repaint();
	}

	// =======================================================================
	// Testing method to render a given directory with DICOM images into
	// a 3D visualization.

	public static void main(String[] args) {

		if (args.length>=1) {
			JFrame frame=new JFrame("DICOM 3D JOGL Volume Renderer");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			TileCache tc=JAI.getDefaultInstance().getTileCache();
			tc.setMemoryCapacity(32*1024*1024);
			TileScheduler ts=JAI.createTileScheduler();
			ts.setPriority(Thread.MAX_PRIORITY);
			JAI.getDefaultInstance().setTileScheduler(ts);
			JAI.getDefaultInstance().setRenderingHint(JAI.KEY_CACHED_TILE_RECYCLING_ENABLED,Boolean.TRUE);
			Timer timer=new Timer();
			timer.schedule(new GarbageCollectionTimer(),5000,2500);
			int threshold=(args.length>=2) ? Integer.parseInt(args[1]) : 0;
			TextureModel3D panel=(args.length==3) ? new TextureModel3D(args[0],"DICOM",threshold,Integer.parseInt(args[2])) : 
				new TextureModel3D(args[0],"DICOM",threshold);
			frame.setSize(512,512);
			frame.setContentPane(panel);
			frame.setVisible(true);
		}
	}
}

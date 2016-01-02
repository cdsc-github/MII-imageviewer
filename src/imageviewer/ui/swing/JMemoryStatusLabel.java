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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.JLabel;

// =======================================================================

public final class JMemoryStatusLabel extends JLabel {

	private static final Runtime RUNTIME=Runtime.getRuntime();
	private static final float MEGABYTES=1024*1024;

	// =======================================================================

	MemoryBarIcon mbi=null;
	Timer timer=null;

	public JMemoryStatusLabel() {

		super();
		setOpaque(false);
		setForeground(Color.white);
		update();
		mbi=new MemoryBarIcon();
		setIcon(mbi);
		setVerticalAlignment(JLabel.CENTER);
    setHorizontalTextPosition(JLabel.LEFT);
		setVerticalTextPosition(JLabel.CENTER);
		timer=new Timer();
		timer.schedule(new UpdateMemoryStatusTimer(),10000,5000);
	}

	// =======================================================================

	public void update() {

		setText("Memory usage ("+Math.round((RUNTIME.totalMemory()-RUNTIME.freeMemory())/MEGABYTES)+"/"+Math.round(RUNTIME.maxMemory()/MEGABYTES)+" MB)");
	}

	// =======================================================================

	public class UpdateMemoryStatusTimer extends TimerTask {

		public void run() {
			if (System.currentTimeMillis()-scheduledExecutionTime()>=5000) return; 
			update();
			repaint();
		}
	}

	// =======================================================================

	public class MemoryBarIcon implements Icon {

		float[] rgb=new float[3];

    public int getIconHeight() {return 10;}
    public int getIconWidth() {return 75;}

    public void paintIcon(Component c, Graphics g, int x, int y) {

			Graphics2D g2=(Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			g2.setColor(Color.gray);
			g2.fill3DRect(x,y,75,9,true);
			double totalMemory=RUNTIME.totalMemory();
			double maxMemory=RUNTIME.maxMemory();
			double freeMemory=RUNTIME.freeMemory();
			double memoryUsage=(totalMemory-freeMemory)/maxMemory;
			
			if (memoryUsage<0.35) {
				rgb[0]=0f; rgb[1]=0.75f; rgb[2]=0f;
			} else if ((memoryUsage>=0.35)&&(memoryUsage<0.7)) {
				rgb[0]=(float)((memoryUsage-0.35)/0.35); rgb[1]=0.75f; rgb[2]=0f;
			} else {
				rgb[0]=1f; rgb[1]=(float)((1.0-memoryUsage)/0.4); rgb[2]=0f;
			}

			g2.setPaint(new GradientPaint(x,y,new Color(0.8f,0.8f,0.8f,0.6f),x,y+5,new Color(rgb[0],rgb[1],rgb[2],0.3f)));
			g2.fillRect(x,y,(int)(memoryUsage*75),5);
			g2.setPaint(new GradientPaint(x,y+5,new Color(rgb[0],rgb[1],rgb[2],0f).darker().darker(),x,y+10,new Color(rgb[0],rgb[1],rgb[2],0.27f).darker()));
			g2.fillRect(x,y+5,(int)(memoryUsage*75),5);
			g2.setColor(Color.darkGray);
			g2.drawRect(x,y,74,9);
    }
	}
}

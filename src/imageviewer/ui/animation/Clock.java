/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package imageviewer.ui.animation;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

// =======================================================================
/* An indeterminate animated progress indicator
 *
 * @author Jasper Potts (new implementation)
 */

public class Clock extends JComponent {

	private static int MAX_SIZE=250;
	private static int NUMBER_OF_SEGMENTS=6;
	private static int cachedShapeMaxSize=0;

	private static double ANGLE_OF_SEGMENT=360d/NUMBER_OF_SEGMENTS;
	private static double RING_THICKNESS=0.17f;
	private static double lastRotation=Double.MIN_VALUE;

	private static BufferedImage gradientImage=null;
	private static BufferedImage cachedShapeImg=new BufferedImage(MAX_SIZE,MAX_SIZE,BufferedImage.TYPE_INT_ARGB);
	private static BufferedImage workingImg=new BufferedImage(MAX_SIZE,MAX_SIZE,BufferedImage.TYPE_INT_ARGB);

	// =======================================================================

	int minsPastMidnight=0;
	Animator animator;

	public Clock() {

		PropertySetter propertySetter=new PropertySetter(this,"minsPastMidnight",0,60);
		animator=new Animator(4000, propertySetter);
		animator.setRepeatBehavior(Animator.RepeatBehavior.LOOP);
		animator.setRepeatCount(Animator.INFINITE);
		animator.setResolution(4000/60);
	}

	// =======================================================================

	public void startAnimation() {animator.start();}
	public void stopAnimation() {animator.stop();}

	public int getMinsPastMidnight() {return minsPastMidnight;}

	public void setMinsPastMidnight(int minsPastMidnight) {if (minsPastMidnight!=this.minsPastMidnight) {this.minsPastMidnight=minsPastMidnight;	repaint();}}

	// =======================================================================

	private static void updateCachedShapeImg(int size){

		Graphics2D g2=cachedShapeImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setComposite(AlphaComposite.Clear);
		g2.fillRect(0,0,cachedShapeImg.getWidth(),cachedShapeImg.getHeight());
		int wcx=MAX_SIZE/2, wcy=MAX_SIZE/2;
		int outerRadius=size/2;
		int innerRadius=(int)(outerRadius*(1-RING_THICKNESS));
		Area segments=new Area();
		for (int i=0; i<NUMBER_OF_SEGMENTS; i++) {
			double angle=i*ANGLE_OF_SEGMENT;
			segments.add(new Area(new Arc2D.Double(wcx-outerRadius,wcy-outerRadius,size,size,angle,ANGLE_OF_SEGMENT-10,Arc2D.PIE)));
		}
		segments.subtract(new Area(new Ellipse2D.Double(wcx-innerRadius,wcy-innerRadius,innerRadius*2,innerRadius*2)));
		g2.setComposite(AlphaComposite.SrcOver);
		g2.setColor(Color.WHITE);
		g2.fill(segments);
		g2.dispose();
	}

	// =======================================================================

	public static void draw(Graphics2D g, Color backgroundColor, Color foregroundColor, int minsPastMidnight, int x, int y, int width, int height) {

		if (gradientImage==null) {
			try {gradientImage=ImageIO.read(new File("resources/images/clockProgressGradient.png"));} catch (Exception exc) {exc.printStackTrace();}
		}

		int size=Math.min(MAX_SIZE,Math.min(width, height));
		int cx=x+(width/2), cy=y+(height/2);
		int wcx=MAX_SIZE/2, wcy=MAX_SIZE/2;
		int outerRadius=size/2;
		int min=minsPastMidnight % 60;
		double rotation=(360d/60d)*min;
		double gradientRotation=rotation*2;
		if ((rotation!=lastRotation) || (cachedShapeMaxSize!=size)) {
			lastRotation=rotation;
			if (cachedShapeMaxSize!=size) updateCachedShapeImg(size);
			Graphics2D g2=workingImg.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			g2.setComposite(AlphaComposite.Clear);
			g2.fillRect(0,0,workingImg.getWidth(),workingImg.getHeight());
			g2.setComposite(AlphaComposite.SrcOver);
			g2.rotate(Math.toRadians(-rotation),wcx,wcy);
			g2.drawImage(cachedShapeImg,0,0,null);
			g2.rotate(Math.toRadians(rotation),wcx,wcy);
			g2.setComposite(AlphaComposite.SrcAtop);
			g2.rotate(Math.toRadians(gradientRotation),wcx,wcy);
			g2.drawImage(gradientImage,wcx-outerRadius,wcy-outerRadius,wcx+outerRadius,wcy+outerRadius,0,0,gradientImage.getWidth(),gradientImage.getHeight(),null);
			g2.dispose();
		}
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		if (backgroundColor!=null) {
			g.setPaint(new GradientPaint(x,y,backgroundColor,x,y+height,Color.black));
			int sz=(int)(0.8f*Math.min(width,height));
			int arcSz=(int)(0.4f*sz);
			g.fillRoundRect(x,y,width,height,arcSz,arcSz);
		}
		g.drawImage(workingImg,cx-wcx,cy-wcy,null);
	}

  public static void drawOldClock(Graphics2D g, Color backgroundColor, Color foregroundColor, int minsPastMidnight, int x, int y, int width, int height) {

		int midx=(int)(x+(width/2.0f));
		int midy=(int)(y+(height/2.0f));
		int sz=(int)(0.8f*Math.min(width,height));
		int arcSz=(int)(0.4f*sz);
		int smallHandSz=(int)(0.3f*sz);
		int bigHandSz=(int)(0.4f*sz);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(backgroundColor);
		g.fillRoundRect(x,y,width,height,arcSz,arcSz);
		g.setColor(foregroundColor);
		g.setStroke(new BasicStroke(sz/20.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER));
		g.drawRoundRect(midx-(sz/2),midy-(sz/2),sz,sz,arcSz,arcSz);
		float hour=minsPastMidnight/60.0f;
		int min=minsPastMidnight % 60;
		float hourAngle=hour*2.0f*(float)Math.PI/12;
		float minAngle=min*2.0f*(float)Math.PI/60;
		g.drawLine(midx,midy,midx+(int)(smallHandSz*Math.cos(hourAngle)),midy+(int)(smallHandSz*Math.sin(hourAngle)));
		g.drawLine(midx,midy,midx+(int)(bigHandSz*Math.cos(minAngle)),midy+(int)(bigHandSz*Math.sin(minAngle)));
	}

	// =======================================================================

	protected void paintComponent(Graphics g) {

		Color background=(isOpaque()) ? null : getBackground();
		draw((Graphics2D)g,background,getForeground(),minsPastMidnight,0,0,getWidth(),getHeight());
	}
}

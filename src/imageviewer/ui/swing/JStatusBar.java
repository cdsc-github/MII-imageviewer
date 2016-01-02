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
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.Painter;

// =======================================================================
// Adapated from the example in the Swing Hacks book. Significant
// changes to make it fit the L&F we have, and also, new component
// mechanisms for updates, etc.

public class JStatusBar extends JXPanel implements SwingConstants {

	private static final Color SQUARE_COLOR_LEFT=new Color(184,180,163);
	private static final Color SQUARE_COLOR_TOP_RIGHT=new Color(184,180,161);
	private static final Color SQUARE_COLOR_BOTTOM_RIGHT=new Color(184,181,161);
	
	// =======================================================================

	JXPanel containerBar=null, leftPanel=null, rightPanel=null;
	JLabel currentMessage=new JLabel("");
	ConcurrentLinkedQueue<MessageDelayPair> messageQueue=new ConcurrentLinkedQueue<MessageDelayPair>();
	Timer timer=null;

	public JStatusBar() {

		setLayout(new BorderLayout(0,0));
		setPreferredSize(new Dimension(10,20));
		Painter p=(Painter)UIManager.get("StatusBar.backgroundPainter");
		if (p!=null) setBackgroundPainter(p); 
		currentMessage.setOpaque(false);
		currentMessage.setForeground(UIManager.getColor("StatusBar.foreground"));
		JPanel iconPanel=new JPanel(new BorderLayout());
		iconPanel.add(new JLabel(new CornerIcon()),BorderLayout.SOUTH);
		iconPanel.setOpaque(false);
		add(iconPanel,BorderLayout.EAST);
		containerBar=new JXPanel();
		containerBar.setOpaque(false);
		containerBar.setLayout(new BoxLayout(containerBar,BoxLayout.X_AXIS));
		add(containerBar,BorderLayout.CENTER);
		leftPanel=new JXPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.X_AXIS));
		leftPanel.add(Box.createHorizontalStrut(3));
		leftPanel.add(currentMessage);
		leftPanel.setOpaque(false);
		rightPanel=new JXPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.X_AXIS));
		rightPanel.setOpaque(false); 
		containerBar.add(leftPanel);
		containerBar.add(Box.createHorizontalGlue());
		containerBar.add(rightPanel);
		timer=new Timer();
		timer.schedule(new MessageTimer(),1500,500);
	}

	// =======================================================================

	public void addMessage(String messageText) {addMessage(messageText,0);}
	public void addMessage(String messageText, long removeDelay) {messageQueue.add(new MessageDelayPair(messageText,removeDelay));}

	// =======================================================================

	public void addMessagePanel(JComponent c, int orientation) {

		JPanel p=(orientation==LEFT) ? leftPanel : rightPanel;
		if ((p.getComponentCount()!=0)||(orientation==RIGHT)) {
			JSeparator js=new JSeparator(JSeparator.VERTICAL);
			js.setMaximumSize(new Dimension(2,22));
			js.setBackground(Color.black);
			js.setForeground(Color.gray);
			p.add(js);
		}
		if (orientation==LEFT) c.setBorder(new EmptyBorder(0,4,0,8)); else c.setBorder(new EmptyBorder(0,8,0,4));
		p.add(c);
	}

	// =======================================================================

	protected void paintComponent(Graphics g) {

		super.paintComponent(g);
		int width=getWidth();
	  int height=getHeight();
		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		g2.setColor(Color.darkGray);
		g2.drawLine(0,0,width,0);	
		g2.dispose();
	}

	// =======================================================================

	public class MessageTimer extends TimerTask {

		public void run() {
			if (messageQueue.isEmpty()) return;
			MessageDelayPair mdp=messageQueue.poll();
			currentMessage.setText(mdp.getTextMessage());
			long delay=mdp.getDelayTime();
			if (delay!=0) {
				try {Thread.sleep(delay);} catch (Exception exc) {}
				if (messageQueue.isEmpty()) currentMessage.setText("");
			}
		}
	}

	// =======================================================================

	public class MessageDelayPair {

		final String textMessage;
		final long delayTime;

		public MessageDelayPair(String textMessage, long delayTime) {this.textMessage=textMessage; this.delayTime=delayTime;}

		public String getTextMessage() {return textMessage;}
		public long getDelayTime() {return delayTime;}
	}

	// =======================================================================

	private class CornerIcon implements Icon {

    public int getIconHeight() {return 12;}
    public int getIconWidth() {return 12;}

		public void paintIcon(Component c, Graphics g, int x, int y) {

			// Layout a row and column "grid"
			
			int firstRow=0, firstColumn=0, rowDiff=4, columnDiff=4;
			
			int secondRow=firstRow+rowDiff;
			int secondColumn=firstColumn+columnDiff;
			int thirdRow=secondRow+rowDiff;
			int thirdColumn=secondColumn+columnDiff;

			Color oldColor=g.getColor();
			g.setColor(Color.white);
			g.fillRect(firstColumn+1,thirdRow+1,2,2);
			g.fillRect(secondColumn+1,secondRow+1,2,2);
			g.fillRect(secondColumn+1,thirdRow+1,2,2);
			g.fillRect(thirdColumn+1,firstRow+1,2,2);
			g.fillRect(thirdColumn+1,secondRow+1,2,2);
			g.fillRect(thirdColumn+1,thirdRow+1,2,2);
			drawSquare(g,firstColumn,thirdRow);
			drawSquare(g,secondColumn,secondRow);
			drawSquare(g,secondColumn,thirdRow);
			drawSquare(g,thirdColumn,firstRow);
			drawSquare(g,thirdColumn,secondRow);
			drawSquare(g,thirdColumn,thirdRow);
			g.setColor(oldColor); 
    }

    private void drawSquare(Graphics g, int x, int y){

			g.setColor(SQUARE_COLOR_LEFT);
			g.drawLine(x,y,x,y+1);
			g.setColor(SQUARE_COLOR_TOP_RIGHT);
			g.drawLine(x+1,y,x+1,y);
			g.setColor(SQUARE_COLOR_BOTTOM_RIGHT);
			g.drawLine(x+1,y+1,x+1,y+1);
    }
	}
}

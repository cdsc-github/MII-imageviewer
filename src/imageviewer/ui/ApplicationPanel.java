/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.TimerTask;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;

import imageviewer.model.ImageSequenceGroup;
import imageviewer.system.ImageReaderManager;
import imageviewer.tools.ToolManager;
import imageviewer.tools.SelectTool;

import imageviewer.ui.graphics.EllipticalGradientPaint;

import imageviewer.ui.layout.Layout;
import imageviewer.ui.layout.LayoutDescription;

import imageviewer.ui.swing.JMemoryStatusLabel;
import imageviewer.ui.swing.JStatusBar;
import imageviewer.ui.swing.MultiSplitPane;
import imageviewer.ui.swing.ToolbarReader;
import imageviewer.ui.swing.event.MenuActionListenerFactory;

// =======================================================================
/* ApplicationPanel is the main UI interface for imageviewer.  Only
 * one instance of the ApplicationPanel should be created (for this
 * reason, the constructor is made private), and the static
 * getInstance() should be used.  ApplicationPanel coordinates the
 * multisplitpane (which in turn contains tabbed panels for images),
 * the messages that are displayed in the bottom status bar, the
 * toolbar, and presentation of floating panels (e.g., dialog boxes).
 *
 * @author "Alex Bui"
 * @version 1.0
 * @since 1.0
 * @see JPanel
 */

public class ApplicationPanel extends JPanel {

	private static final long serialVersionUID=-882918996477680290L;

	private static ApplicationPanel AP=null;
	
	MultiSplitPane msp=null;
	GlassPaneManager gpm=null;
	Layout activeLayout=null;
	JPanel toolPanel=null;
	JToolBar tb=null;
	JStatusBar sb=null;
	JLayeredPane lp=null;
	LoginLabel ll=new LoginLabel("Not logged in");

	// =======================================================================

	public static ApplicationPanel getInstance() {if (AP==null) AP=new ApplicationPanel(); return AP;}

	// =======================================================================

	private ApplicationPanel() {

		super(new BorderLayout(),true); 		
		setBackground(Color.black);
		setOpaque(true);
		String toolbarFile=(String)ApplicationContext.getContext().getProperty(ApplicationContext.CONFIG_TOOLBAR);
		if (toolbarFile!=null) {
			tb=ToolbarReader.parseFile(toolbarFile);
			toolPanel=new JPanel(new BorderLayout());
			toolPanel.add(tb,BorderLayout.PAGE_START);
			add(toolPanel,BorderLayout.PAGE_START);
		}
		ApplicationContext.setCurrentTool(ToolManager.getTool(SelectTool.class));
		sb=new JStatusBar();
		add(sb,BorderLayout.PAGE_END);
		sb.addMessagePanel(ll,JStatusBar.RIGHT);
		sb.addMessagePanel(new JMemoryStatusLabel(),JStatusBar.RIGHT);
		TabbedDataPanel tdp=new TabbedDataPanel(this);
		msp=new MultiSplitPane();
		msp.add(tdp);
		add(msp,BorderLayout.CENTER);
		ll.setBorder(new CompoundBorder(ll.getBorder(),new EmptyBorder(0,0,2,0)));
	}
	
	// =======================================================================

	public void addToMultiSplitPane(JComponent c) {msp.add(c);}
	public void removeFromMultiSplitPane(JComponent c) {msp.remove(c);}
	
	// =======================================================================

	public void load(String dir, String imageType) {

		ImageReader ir=ImageReaderManager.getInstance().getImageReader(imageType);
		ArrayList imageStudies=ir.organizeByStudy(ir.readImages(dir,true));
		addImages(imageStudies);
	}

	public void addImages(ArrayList<? extends ImageSequenceGroup> imageStudies) {

		TabbedDataPanel tdp=null;
		if (activeLayout==null) {
	    int childCount=msp.getComponentCount();
	    if (childCount==0) {
				tdp=new TabbedDataPanel(this);
				msp.add(tdp);
	    } else {
				tdp=(TabbedDataPanel)msp.getComponent(0);
	    }
		} else {
	    tdp=(TabbedDataPanel)activeLayout.getParent().getParent();
		}
		tdp.addImages(imageStudies); 
		tdp.validate();
	}

	// =======================================================================

	public Component getRightComponent(JComponent c) {

		Component[] mspComponents=msp.getComponents();
		for (int loop=0; loop<(mspComponents.length-1); loop++) {
	    Component comp=mspComponents[loop];
	    if (c==comp) return mspComponents[loop+1];
		}
		return null;
	}

	public Component getLeftComponent(JComponent c) {
		
		Component[] mspComponents=msp.getComponents();
		for (int loop=0; loop<(mspComponents.length); loop++) {
	    Component comp=mspComponents[loop];
	    if ((c==comp)&&(loop!=0)) return mspComponents[loop-1];
		}
		return null;
	}

	// =======================================================================

	private void repaintSelectedTab(TabbedDataPanel tdp) {
		
		int selectedIndex=tdp.getSelectedIndex();
		if (selectedIndex<0) return;
		Rectangle selectedTabBounds=tdp.getBoundsAt(selectedIndex);
		if (selectedTabBounds!=null) tdp.repaint(selectedTabBounds);
	}

	public Layout getActiveLayout() {return activeLayout;}

	public void setActiveLayout(Layout x) {

		if (activeLayout==x) return;
		if (activeLayout!=null) {
	    activeLayout.putClientProperty(ApplicationContext.ACTIVE_LAYOUT,null); 
			Object o=activeLayout.getParent().getParent();
			if (o instanceof TabbedDataPanel) {
				TabbedDataPanel tdp=(TabbedDataPanel)o; //activeLayout.getParent().getParent();
				if (tdp!=null) repaintSelectedTab(tdp);
			}
		}
		activeLayout=x;
		if (x!=null) {
	    activeLayout.putClientProperty(ApplicationContext.ACTIVE_LAYOUT,true);
			Object o=activeLayout.getParent().getParent();
			if (o instanceof TabbedDataPanel) {
				TabbedDataPanel tdp=(TabbedDataPanel)o;
				if (tdp!=null) repaintSelectedTab(tdp);
			}
		}
	}

	// =======================================================================

	public String getActiveAssocKey() {return (activeLayout!=null) ? (activeLayout.getImageSequences())[0].getImage(0).getAssocKey() : null;}

	// =======================================================================

	public void changeCurrentLayout(LayoutDescription ld) {if (activeLayout!=null) activeLayout.changeLayout(ld,true);}

	// =======================================================================

	public JLayeredPane getLayeredPane() {return lp;}

	public void setGlassPane(JPanel x) {gpm=new GlassPaneManager(x);}
	public void setLayeredPane(JLayeredPane x) {lp=x;}

	// =======================================================================

	public int showDialog(JOptionPane op) {int status=gpm.showModalDialog(op); ApplicationContext.getContext().setLastActionStatus(status); return status;}
	public int showDialog(JFileChooser fc, String dialogTitle) {int status=gpm.showModalDialog(fc,dialogTitle); ApplicationContext.getContext().setLastActionStatus(status); return status;}

	public int showDialog(String textMessage, JComponent[] components, int optionPaneType, int optionPaneButtons) {
		return showDialog(textMessage,components,optionPaneType,optionPaneButtons,null);
	}

	public int showDialog(String textMessage, JComponent[] components, int optionPaneType, int optionPaneButtons, Icon i) {

		int objectCount=0, index=0;
		if (textMessage!=null) objectCount++;
		if (components!=null)	objectCount+=components.length;

		Object[] msg=new Object[objectCount];
	
		if (textMessage!=null) {
			if (textMessage.length()<50) {
				JLabel jl=new JLabel(textMessage,JLabel.CENTER);
				jl.setBorder(new EmptyBorder(0,0,12,0));
				msg[index++]=jl;
			} else {
				JTextArea ta=new JTextArea(textMessage);
				ta.setColumns(35);
				ta.setLineWrap(true);
				ta.setWrapStyleWord(true);
				ta.setRows(ta.getLineCount());
				ta.setEditable(false);
				msg[index++]=ta;
			}
		}
		if (components!=null) for (int loop=0; loop<components.length; loop++) msg[index++]=components[loop];
		JOptionPane op=(i==null) ? (new JOptionPane(msg,optionPaneType,optionPaneButtons)) : (new JOptionPane(msg,optionPaneType,optionPaneButtons,i));
		int status=gpm.showModalDialog(op);
		ApplicationContext.getContext().setLastActionStatus(status); 
		return status;
	}

	// =======================================================================
	// Handle some weird issue in Java with a missing repaint whenever
	// something is added to a layeredPanel.

	public void centerFloatingPanel(JPanel panel) {

		int w=getWidth();
		int h=getHeight();
		int x=(w-panel.getWidth())/2;
		int y=(h-panel.getHeight())/2;
		panel.setLocation((x>=0) ? x : 0,(y>=0) ? y : 0);
	}

	public void addFloatingPanel(JPanel panel) {lp.add(panel,JLayeredPane.PALETTE_LAYER); lp.moveToFront(panel); lp.repaint();}
	public void addFloatingPanel(JPanel panel, Integer layer) {lp.add(panel,layer); lp.moveToFront(panel); lp.repaint();}
	public void removeFloatingPanel(JPanel panel) {lp.remove(panel); lp.repaint();}

	// =======================================================================

	public void updateLogin(Subject s, LoginContext lc) {UserManager.setCurrentUser(s,lc); ll.setText(UserManager.getCurrentUserName());}

	// =======================================================================

	public void addStatusMessage(String textMessage) {sb.addMessage(textMessage);}
	public void addStatusMessage(String textMessage, long delay) {sb.addMessage(textMessage,delay);}

	public StatusTimerTask getNewTimerTask(String msg) { return new StatusTimerTask(msg); }

	private class StatusTimerTask extends TimerTask {
		String msg=new String();
		public StatusTimerTask(String msg) {if (this.msg!=null) this.msg=msg;}	
		public void run() {getInstance().addStatusMessage(msg);}
	}

	// =======================================================================
	// Make sure that the height dimension is maxed out so that the
	// highlight covers the entire needed area within the status bar.

	private class LoginLabel extends JLabel implements MouseListener {

		Color highlight=UIManager.getColor("LoginLabel.highlightColor");
		boolean isMouseOver=false;

		public LoginLabel(String s) {super(s); addMouseListener(LoginLabel.this); setToolTipText("Click to login/logout of imageViewer"); setOpaque(false);}

		// =======================================================================

		public Dimension getMaximumSize() {Dimension d=getPreferredSize(); d.height=Short.MAX_VALUE; return d;}

		public void paintComponent(Graphics g) {
			
			if (isMouseOver) {
				int w=getWidth();
				int h=getHeight();
				EllipticalGradientPaint egp=new EllipticalGradientPaint(w/2,h,highlight,new Point2D.Double(w,h),new Color(0,0,0,32));
				Graphics2D g2=(Graphics2D)g;
				g2.setPaint(egp);
				g2.fillRect(0,h/2,getWidth(),h/2);
			}
			super.paintComponent(g);
		}

		public void mouseClicked(MouseEvent e) {UserManager um=(UserManager)MenuActionListenerFactory.getListener(UserManager.class); um.toggleLogin();}
		public void mouseEntered(MouseEvent e) {isMouseOver=true; repaint();}
		public void mouseExited(MouseEvent e) {isMouseOver=false; repaint();}

		public void mousePressed(MouseEvent e) {} 
		public void mouseReleased(MouseEvent e) {}
	}
}




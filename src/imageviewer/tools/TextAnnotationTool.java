/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import imageviewer.model.DataLayer;
import imageviewer.model.dl.ShapeDataLayer;
import imageviewer.ui.ApplicationContext;
import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.Polygon2D;
import imageviewer.ui.annotation.StylizedShape;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.undo.AddAnnotationEdit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.EventObject;

public class TextAnnotationTool extends BasicAnnotationTool implements KeyListener{
	
	private static final Color LAST_POINT_COLOR=new Color(0,113,188);
	private static final Color POINT_COLOR=Color.white;

	// =======================================================================

	StylizedShape lastPoint=null;
	Point textStart = null;
	String currentString = "";

	public TextAnnotationTool() {super();}
	
	// =======================================================================

	public void startTool(EventObject e) {a=null;}
	public void endTool(EventObject e) {
		
		if ((a!=null)&&(ip!=null)) {
			if (currentString.equals("Type to add text") || currentString.equals("")){
				if (a!=null){
					ip.removeTemporaryShape(a);
					a=null;
				}
				ip.repaint();
			}
			else 
				createTextAnnotation();
		}
		
	}

	private void createTextAnnotation() {
		
		if (a!=null) {
			endPoint=startPoint; 
			ip.removeTemporaryShape(a);
			if (((startPoint!=null)&&(endPoint!=null))) {
				ShapeDataLayer sdl=(ShapeDataLayer)ip.getSource().findDataLayer(DataLayer.SHAPE);
				if (sdl==null) {
					sdl=new ShapeDataLayer();
					ip.getSource().addDataLayer(sdl);
				}
				sdl.addShape(a);
				AddAnnotationEdit aae=new AddAnnotationEdit(ip,sdl,a);
				ApplicationContext.postEdit(aae);
			}
//			ip.setCursor(Cursor.getDefaultCursor());
//			ip.clearPanelOverlayShapes();
			ip.repaint();
//			ip.setCursor(Cursor.getDefaultCursor());
			a=null;
			currentString = "";
		}
		reset();
	}

	// =======================================================================
 
	public String getToolName() {return new String("Text");}

	// =======================================================================

	protected Annotation createAnnotation(Point2D.Double p) {return Annotation.createText(p.x, p.y, currentString);}
	protected Annotation createAnnotation(Point p) {return Annotation.createText(p.x,p.y,currentString);}

	protected void setShapeCoordinate() {}

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {
		
		if (e.getSource() instanceof ImagePanel) {
			ImagePanel currentPanel=(ImagePanel)e.getSource();
			if ((currentPanel!=ip)&&(ip!=null)) {
				if (currentString.equals("Type to add text") || currentString.equals("")){
					if (a!=null){
						ip.removeTemporaryShape(a);
						a=null;
					}
					ip.repaint();
				}
				else 
					createTextAnnotation();
			}
			else if (a!=null){
				ip=currentPanel;
				String typedChar = Character.toString(e.getKeyChar());
				if ((e.getKeyCode() == KeyEvent.VK_ENTER || typedChar.equals("\n"))){
					if (currentString.equals("Type to add text") || currentString.equals("")){
						if (a!=null){
							ip.removeTemporaryShape(a);
							a=null;
						}
						ip.repaint();
					}
					else 
						createTextAnnotation();
				} else {
					if (currentString.equals("Type to add text")){
						currentString = "";
					}
					if (typedChar.equals("\b")){
						if (currentString.length() > 0)
							currentString = currentString.substring(0,currentString.length()-1);
						if (currentString.equals("")){
							currentString = "Type to add text";
						}
					} else {
						currentString = currentString + typedChar;
					}
					if (e.getSource() instanceof ImagePanel) {
						ip=(ImagePanel)e.getSource();
						rp=ip.getPipelineRenderer().getRenderingProperties();
						startPoint=textStart;
						startPointTranslated=translateToImage(rp,startPoint);
						if (a!=null){
							ip.removeTemporaryShape(a);
						}
						a=createAnnotation(startPointTranslated); 
						ip.addTemporaryShape(a);
						ip.repaint();
					}
				}
			}
		}
		
	}

	// =======================================================================

	public void mousePressed(MouseEvent e) {}
	
	public void mouseClicked(MouseEvent e){
		
		if (e.getSource() instanceof ImagePanel) {
			ImagePanel currentPanel=(ImagePanel)e.getSource();
			if ((currentPanel!=ip)&&(ip!=null)) {
				if (currentString.equals("Type to add text") || currentString.equals("")){
					if (a!=null){
						ip.removeTemporaryShape(a);
						a=null;
					}
					ip.repaint();
				}
				else 
					createTextAnnotation();
			}
			if (currentString.equals("")){
				currentString = "Type to add text";
			}
			textStart=e.getPoint();
			ip=currentPanel;
			rp=ip.getPipelineRenderer().getRenderingProperties();
			startPoint=textStart;
			startPointTranslated=translateToImage(rp,startPoint);
			if (a!=null){
				ip.removeTemporaryShape(a);
			}
			a=createAnnotation(startPointTranslated); 
			ip.addTemporaryShape(a);
			ip.repaint();
		}
	}
	
	public void mouseDragged(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	
	
}

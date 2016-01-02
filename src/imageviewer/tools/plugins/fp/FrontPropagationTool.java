/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.plugins.fp;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import java.awt.geom.Area;
import java.awt.geom.Point2D;

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.util.ArrayList;
import java.util.EventObject;

import javax.imageio.ImageIO;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.TiledImage;

import javax.swing.JPanel;

import java.text.NumberFormat;

import imageviewer.model.Image;
import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;
import imageviewer.model.dl.ShapeDataLayer;

import imageviewer.rendering.RenderingProperties;
import imageviewer.system.ImageReaderManager;

import imageviewer.tools.ImagingTool;
import imageviewer.tools.Tool;
import imageviewer.tools.ToolSliderWidget;
import imageviewer.tools.plugins.Plugin;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.FloatingPanel;
import imageviewer.ui.annotation.StylizedShape;
import imageviewer.ui.image.ImagePanel;

import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.event.MenuActionEvent;
import imageviewer.ui.swing.event.MenuActionListener;

// =======================================================================
// Code adapated from: "Shape modeling with front propagation: A level
// set approach" (Malladi, Sethian, Vemuri; IEEE Trans PAMI,
// 17(2):158-175, 1995).  Given a seed point, the algorithm attempts
// to find the associated shape based on boundary conditions.  The
// problem is considered in terms of a front propagation problem,
// where for a given time, t, we want to know the shape/contour as it
// expands/contracts. This class encode Algorithm #2 from the paper.
// Presently handles only the 2D case; 3D should be a simple
// extension.  Also handles colored images correctly (RGB).
//
// Note that two parameters currently control the algorithm. cellSize
// is used to discretize the image to a grid/mesh over which the
// algorithm runs (i.e., considers those grid points for the front
// propagation); and the tolerance, which is used to gauge sensitivity
// in the change in pixel values.

public class FrontPropagationTool extends ImagingTool implements Tool, Plugin, MenuActionListener {

	private static final String[] DEFAULT_MENU_LOCATION=new String[] {"tools","plugins"};
	private static final String DEFAULT_PLUGIN_MENU=new String("config/plugins/frontPropagation.xml");

	private static final int DEFAULT_CELL_SIZE=1;
	private static final int DEFAULT_TOLERANCE=35;

	private static final Color[] COLORS=new Color[] {Color.red, Color.orange, Color.yellow, Color.green, Color.blue};

	// =======================================================================

	int red=0, green=0, blue=0, threshold=0, width=0, height=0, numBands=0;
	int cellSize=DEFAULT_CELL_SIZE, tolerance=DEFAULT_TOLERANCE;
	ToolSliderWidget toleranceSlider=null;
	Cell[] map=null;

	public FrontPropagationTool() {

		toleranceSlider=new ToolSliderWidget("Tolerance parameter",0,100,35,NumberFormat.getIntegerInstance());
	}

	// =======================================================================

	public void startTool(EventObject e) {

		if (toolDialog==null) {
			JPanel panel=new JPanel();
			panel.add(toleranceSlider);
			toolDialog=new FloatingPanel(panel,getToolName());
		}
		showToolDialog();
	}

	public void endTool(EventObject e) {hideToolDialog();}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Front propagation selector");}

	public Cell[] getMap() {return map;}

	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public int getThreshold() {return threshold;}
	public int getCellSize() {return cellSize;}
	public int getTolerance() {return tolerance;}

	// =======================================================================

	public String getMenuFilename() {

		String menuConfig=(String)ApplicationContext.getContext().getProperty("PLUGIN_CONFIG_FRONT_PROPAGATION");
		if (menuConfig==null) menuConfig=DEFAULT_PLUGIN_MENU;
		return menuConfig;
	}

	public String[] getMenuLocation() {return DEFAULT_MENU_LOCATION;}

	// =======================================================================

	public void actionPerformed(MenuActionEvent mae) {

		ActionEvent ae=mae.getActionEvent();
		String actionCommand=ae.getActionCommand();
		if (actionCommand==null) {
			MenuAction ma=mae.getMenuAction();
			if (ma!=null) actionCommand=ma.getCommandName(); else return;
		}
		if (actionCommand==null) return;
		
		if (actionCommand.compareTo("Select region")==0) {
			Tool currentTool=ApplicationContext.getContext().getTool();
			EventObject eo=new EventObject(this);
			if (currentTool!=null) currentTool.endTool(eo);
			ApplicationContext.getContext().setTool(this);
			startTool(eo);
		} 
	}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {

		ImagePanel ip=(ImagePanel)e.getSource();
		RenderingProperties rp=ip.getPipelineRenderer().getRenderingProperties();
		run(new Object[] {ip,translateToImage(rp,e.getPoint())});
	}

	// =======================================================================
	// Create the map based on the number of bands used.

	private void convertMap(RasterAccessor ra) {

		if ((map!=null)&&(map.length==(width*height))) {
			for (int loop=0, n=width*height; loop<n; loop++) map[loop].reset();
		} else {
			map=new Cell[width*height];
		}
			
		ColorModel cm=ColorModel.getRGBdefault();
		if (numBands==1) {
			int[] dataArray=ra.getIntDataArray(0);
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pixelRGB=(int)dataArray[(y*width)+x];
					map[(y*width)+x]=new Cell(x,y,pixelRGB,pixelRGB,pixelRGB);
				}
			}
			dataArray=null;
		} else {
			int[][] dataArray=ra.getIntDataArrays();
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int[] pixelRGB=dataArray[(y*width)+x];
					map[(y*width)+x]=new Cell(x,y,cm.getRed(pixelRGB[0]),cm.getGreen(pixelRGB[1]),cm.getBlue(pixelRGB[2]));
				}
			}
			dataArray=null;
		}
	}

	// =======================================================================

	public Object run(Object[] args) {

		ImagePanel ip=(ImagePanel)args[0];
		Point2D.Double p=(Point2D.Double)args[1];
		RenderedImage ri=ip.getSource().getRenderedImage();
		Contour c=run(ri,p);

		// Create a shape data layer to store the results.

		ShapeDataLayer sdl=new ShapeDataLayer();
		int index=0;
		Area a=null;

		// Loop through the first contour that's given back from the
		// plotter.  If a subsequent contour is contained within the
		// current area, then we subtract it as a region; otherwise we
		// create a new area.  This handles the problem of generating
		// regions that have holes in them...

		while (c!=null) {
			Point firstPoint=c.getFirstPoint();
			if ((firstPoint!=null)&&(a!=null)) {
				if (a.contains(firstPoint.x,firstPoint.y)) a.subtract(new Area(c)); else a=null;
			}
			if (a==null) {
				a=new Area(c);
				StylizedShape ss=new StylizedShape(a,null,0.5f,0.75f);
				ss.setStrokeColor(COLORS[index % 5]);
				ss.setFillColor(COLORS[index % 5]);
				ss.setStroked(true);
				ss.setFilled(true);
				sdl.addShape(ss);
				index++;
			}
			c=c.next;
		}

		ip.getSource().addDataLayer(sdl);
		ip.repaint();
		return null;
	}

	// =======================================================================

	public Contour run(RenderedImage ri, Point2D.Double p) {

		width=ri.getWidth()/cellSize;
		height=ri.getHeight()/cellSize;
		threshold=(2*width*height);		
		tolerance=toleranceSlider.getValue();

		// Transform the image into a planar image and cast the values as
		// floats. Then scan the float array to create the 1D
		// representation for the cell array.

		PlanarImage pi=(ri instanceof PlanarImage) ? (PlanarImage)ri : new TiledImage(ri,false);
		ParameterBlock pb=new ParameterBlock();
		pb.addSource(pi);
		pb.add(DataBuffer.TYPE_INT);
		PlanarImage image=JAI.create("format",pb);
		RenderedImage[] src={image};
		RasterAccessor ra=new RasterAccessor(image.getData(),new Rectangle(0,0,ri.getWidth(),ri.getHeight()),
																				 (RasterAccessor.findCompatibleTags(src,image))[0],image.getColorModel());
		numBands=image.getSampleModel().getNumBands();
		convertMap(ra);

		// Run the core algorithm from the given coordinate point.  The
		// results will be stored in the cell map, from which the contours
		// can then be generated to represent the shape boundaries.

		findRegion((int)Math.round(p.x),(int)Math.round(p.y));

		// Compute the polygons associated with the detected region.

		Plotter plotter=new Plotter(map,width,height,cellSize);
		Contour c=plotter.computeContour((double)threshold);

		// Do cleanup to make sure things are garbage collected promptly.

		image.dispose(); image=null;
		if (!(ri instanceof PlanarImage)) pi.dispose(); pi=null;
		return c;
	}

	// =======================================================================

	private int pixelDelta(Cell c)	{

		int[] rgb=c.getRGB(); 
		int i=(numBands==1) ? Math.abs(rgb[0]-red) : (Math.abs(rgb[0]-red)+Math.abs(rgb[1]-green)+Math.abs(rgb[2]-blue));
		return (i>tolerance) ? threshold : 0;
	}

	private double quadratic(double a, double b, double c) {double d=(b*b)-(4*a*c);	return (d<0) ? (Double.POSITIVE_INFINITY) : (Math.sqrt(d)-b)/(2*a);}

	// =======================================================================
	// Update for 3D will require adding a third z-dimension to the
	// quadratic distance computation and comparisons in updateCell.

	private void updateCell(Cell c) {

		int x=c.x, y=c.y;
		double l=(double)(pixelDelta(c)+1);

		double d0=(x-1>=0) ? map[(y*width)+x-1].distance : (Double.POSITIVE_INFINITY);
		double d1=(y-1>=0) ? map[((y-1)*width)+x].distance : (Double.POSITIVE_INFINITY);
		if ((x+1<width)&&(d0>map[(y*width)+x+1].distance)) d0=map[(y*width)+x+1].distance;
		if ((y+1<height)&&(d1>map[((y+1)*width)+x].distance)) d1=map[((y+1)*width)+x].distance;

		double d2=(d0<=d1) ? d0 : d1;
		double d3=(d0<=d1) ? d1 : d0;
		double d4=(d3!=(Double.POSITIVE_INFINITY)) ? quadratic(2,-2*(d3+d2),(d3*d3+d2*d2)-(l*l)) : (Double.POSITIVE_INFINITY);
		if ((d4==(Double.POSITIVE_INFINITY))||(d4<d3)) d4=quadratic(1,-2*d2,(d2*d2)-(l*l));
		c.distance=(d4>(d2+l))? (d2+l) : d4;
	}

	// =======================================================================

	private void findRegion(int x0, int y0)	{

		// Allocate a heap to keep track of the values on the front
		// propagation for the shape.  Initialize the starting point based
		// on the specified cell location and insert into the heap;
		// remember to convert the start point based on the cellSize and
		// the imposed discretizing grid.

		CellHeap heap=new CellHeap(2*width+height);
		int x=(x0/cellSize), y=(y0/cellSize);
				
		Cell c0=map[(y*width)+x];
		c0.distance=0;
		c0.setMarked(true);
		heap.add(c0);

		int[] initialRGB=c0.getRGB();
		red=initialRGB[0];
		green=initialRGB[1];
		blue=initialRGB[2];

		Cell c1=heap.poll();
		int k=1, index=0;

		while (true) {

			c1.setMarked(true);
			x=c1.x;
			y=c1.y;

			// Examine each of the four directions around the current cell
			// (north, south, east, west) and check to see if we need to add
			// the point to the heap.  Update the current cell if it's not
			// marked.

			if ((x-1)>=0) {
				Cell c=map[(y*width)+x-1];
				if (!c.isMarked()) {updateCell(c); if (c.position<0) heap.add(c); else heap.balance(c);}
			}
			if ((x+1)<width) {
				Cell c=map[(y*width)+x+1];
				if (!c.isMarked()) {updateCell(c); if (c.position<0) heap.add(c); else heap.balance(c);}
			}
			if ((y-1)>=0)	{
				Cell c=map[((y-1)*width)+x];
				if (!c.isMarked()) {updateCell(c); if (c.position<0) heap.add(c); else heap.balance(c);}
			}
			if ((y+1)<height) {
				Cell c=map[((y+1)*width)+x];
				if (!c.isMarked()) {updateCell(c); if (c.position<0) heap.add(c); else heap.balance(c);}
			}

			// To extend to 3D, create a z-axis check here, too, to add to
			// the heap by checking z-1, z+1 coordinates.

			c1=heap.poll();
			if (c1==null) break;
			if (c1.distance>threshold) return;
			if (pixelDelta(c1)<tolerance) {
				int l=k+1;
				int[] newRGB=c1.getRGB();
				red=Math.round((float)(red*k+newRGB[0])/(float)l);
				green=Math.round((float)(green*k+newRGB[1])/(float)l);
				blue=Math.round((float)(blue*k+newRGB[2])/(float)l);
				k=l;
			}
		}
	}

	// =======================================================================
	// Basic heap implementation that uses the cells' distances to
	// compute the minimum (top) of the heap.  For some reason, this
	// particular implementation is needed over the PriorityQueue in
	// Java because the latter has a weird arbitrary decision process
	// for selecting ties.

	private class CellHeap {

		int size=0, index=-1;
		Cell heap[];
	
		public CellHeap(int size) {this.size=size; heap=new Cell[size];}

		// =======================================================================

		public void add(Cell c) {

			if (++index==size) {
				Cell[] newHeap=new Cell[2*size];
				System.arraycopy(heap,0,newHeap,0,size);
				heap=newHeap;
				size*=2;
			}
			heap[index]=c;
			c.position=index;
			balance(c);
		}

		// =======================================================================

		public void balance(Cell c0) {

			if (c0.position==0) return;
			while (true) {
				int i=(c0.position-1)/2;
				Cell c1=heap[i];
				if (c1.distance<=c0.distance) return;
				heap[c0.position]=c1;
				c1.position=c0.position;
				heap[i]=c0;
				c0.position=i;
			} 
		}

		// =======================================================================

		public Cell poll() {

			if (index==-1) return null;
			Cell c0=heap[0], c1=heap[index--];
			heap[0]=c1;
			c1.position=0;
			while (true) {
				int i=(c1.position*2)+1;
				if (i>index) return c0;
				Cell c2=heap[i];
				int j=i+1;
				Cell c3=(j>index) ? null : heap[j];
				if ((c3==null)||(c2.distance<=c3.distance)) {
					if (c1.distance<=c2.distance) return c0;
					heap[c1.position]=c2;
					c2.position=c1.position;
					heap[i]=c1;
					c1.position=i;
				} else {
					if (c1.distance<=c3.distance) return c0;
					heap[c1.position]=c3;
					c3.position=c1.position;
					heap[j]=c1;
					c1.position=j;
				}
			} 
		}
	}

	// =======================================================================

	public static void main(String[] args) {

		if (args.length==3) {
			try {

				// ImageReader ir=ImageReaderManager.getInstance().getImageReader("DICOM");
				// ImageSequence files=ir.readDirectory(args[0],true);
				// Image i=files.getImage(Integer.parseInt(args[1]));
				RenderedImage ri=ImageIO.read(FrontPropagationTool.class.getResourceAsStream(args[0]));
				FrontPropagationTool fpt=new FrontPropagationTool();
				Point2D.Double p=new Point2D.Double(Double.parseDouble(args[1]),Double.parseDouble(args[2]));
				Contour c=fpt.run(ri,p);
				System.out.println(c);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}		
	}
}

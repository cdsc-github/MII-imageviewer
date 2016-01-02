/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.plugins.fp;

// =======================================================================

public class Plotter {

	ContourPoint end=null, point=null, localPoint=null;
	Contour contour=null;

	int height=0, width=0, maxArea=0, cellSize=0;
	Cell[] map=null;

	public Plotter(Cell[] map, int width, int height, int cellSize) {

		this.width=width;
		this.height=height;
		this.cellSize=cellSize;
		this.map=map;
	}

	// =======================================================================

	public Contour computeContour(double threshold) {

		end=new ContourPoint();
		point=new ContourPoint();
		localPoint=new ContourPoint();

		Contour initialContour=contour=new Contour();
		maxArea=1;

		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++)	{
				double d=map[(y*width)+x].distance-threshold;
				map[(y*width)+x].distance=(d!=0) ? d : 1;
			}
		}

		for (int x=0; x<(width-1); x++) {for (int y=0; y<(height-1); y++) clean(x,y);}
		for (int x=0; x<(width-1); x++) {for (int y=0; y<(height-1); y++) if (!map[(y*width)+x].isProcessed()) recordContour(map[(y*width)+x]);}
		return initialContour;
	}

	// =======================================================================

	private void clean(int x, int y) {

		if (((x+1)>width)||((y+1)>width)) return;

		if ((map[(y*width)+x].distance*map[(y*width)+x+1].distance<0)&&
				(map[(y*width)+x].distance*map[((y+1)*width)+x].distance<0)&& 
				(map[((y+1)*width)+x+1].distance*map[(y*width)+x+1].distance<0)&&
				(map[((y+1)*width)+x+1].distance*map[((y+1)*width)+x].distance<0)) {

			if (map[(y*width)+x].distance<0)	{
				map[(y*width)+x].distance=1;
				map[((y+1)*width)+x+1].distance=1;
				clean(x+1,y);
				clean(x+1,y+1);
				clean(x,y+1);
				if (x-1>=0)	clean(x-1,y);
				if (y-1>=0)	clean(x,y-1);
				if (((x-1)>=0)&&((y-1)>=0))	{clean(x-1,y-1); return;}
			} else {
				map[(y*width)+x+1].distance=1;
				map[((y+1)*width)+x].distance=1;
				clean(x+1,y);
				if (y-1>=0)	{
					clean(x,y-1);
					clean(x+1,y-1);
				}
				clean(x,y+1);
				if (x-1>=0)	{
					clean(x-1,y);
					clean(x-1,y+1);
				}
			}
		}
	}

	// =======================================================================

	private void recordContour(Cell c0) {
		
		if (!getFirstPoints(c0)) return;

		contour.addPoint(end.x,end.y);
		contour.addPoint(point.x,point.y);

		int i=height-1, j=width-1;
		boolean flag=false;
		Cell c=c0;

		while (true) {
			switch (point.direction)	{
			  case 0: if ((c.x-1)<0) flag=true;	else c=map[(c.y*width)+c.x-1]; break;
			  case 1: if ((c.x+1)>=j) flag=true; else c=map[(c.y*width)+c.x+1]; break;
			  case 2: if ((c.y-1)<0) flag=true; else c=map[((c.y-1)*width)+c.x]; break;
			  case 3:	if ((c.y+1)>=i)	flag=true; else	c=map[((c.y+1)*width)+c.x]; break;
			}

			// If the traversal comes back to the start point, finish the
			// contour.  If we hit the edge of the region, end.

			if (flag)	break;
			if ((c.x==c0.x)&&(c.y==c0.y)) {
				if (contour.computeArea()>maxArea) maxArea=contour.getArea();
				contour.next=new Contour();
				contour=contour.next;
				return;
			}
			getNextPoint(c);
			contour.addPoint(point.x,point.y);
		}

		point.x=end.x;
		point.y=end.y;
		point.direction=end.direction;
		c=c0;

		contour.setArea(0x7fffffff);
		contour.next=new Contour();
		contour=contour.next;
		contour.setArea(0x7fffffff);
		contour.addPoint(point.x,point.y);

		while (true) {

			switch (point.direction) {

			  case 0: if ((c.x-1)<0) {contour.next=new Contour();	contour=contour.next;	return;}
					      c=map[(c.y*width)+c.x-1];
						    break;
				case 1: if ((c.x+1)>=j)	{contour.next=new Contour(); contour=contour.next; return;}
					      c=map[(c.y*width)+c.x+1];
								break;
				case 2:	if ((c.y-1)<0) {contour.next=new Contour();	contour=contour.next;	return;}
					      c=map[((c.y-1)*width)+c.x];
								break;
				case 3: if ((c.y+1)>=i)	{contour.next=new Contour();	contour=contour.next;	return;}
					      c=map[((c.y+1)*width)+c.x];
								break;
			 default: break;
			}
			getNextPoint(c);
			contour.addPoint(point.x,point.y);
		} 
	}

	// =======================================================================

	private boolean getFirstPoints(Cell c0) {

		int x=c0.x, y=c0.y;
		c0.setProcessed(true);
		boolean flag=false;

		if (map[(y*width)+x].distance*map[(y*width)+x+1].distance<0) {
			flag=true;
			end.direction=2;
			processEdge(map[(y*width)+x],map[(y*width)+x+1],true,end);
		}
		if (map[(y*width)+x+1].distance*map[((y+1)*width)+x+1].distance<0) {
			if (flag)	{
				point.direction=1;
				processEdge(map[(y*width)+x+1],map[((y+1)*width)+x+1],false,point);
				return true;
			}
			flag=true;
			end.direction=1;
			processEdge(map[(y*width)+x+1],map[((y+1)*width)+x+1],false,end);
		}
		if (map[((y+1)*width)+x].distance*map[((y+1)*width)+x+1].distance<0) {
			if (flag) {
				point.direction=3;
				processEdge(map[((y+1)*width)+x],map[((y+1)*width)+x+1],true,point);
				return true;
			}
			flag=true;
			end.direction=3;
			processEdge(map[((y+1)*width)+x],map[((y+1)*width)+x+1],true,end);
		}
		if ((map[(y*width)+x].distance*map[((y+1)*width)+x].distance<0)&&(flag)) {
			point.direction=0;
			processEdge(map[(y*width)+x],map[((y+1)*width)+x],false,point);
			return true;
		} else {
			return false;
		}
	}

	// =======================================================================

	private void getNextPoint(Cell c0) {

		int x=c0.x, y=c0.y;
		c0.setProcessed(true);
		boolean flag=false;

		if (map[(y*width)+x].distance*map[(y*width)+x+1].distance<0) {
			flag=true;
			localPoint.direction=2;
			processEdge(map[(y*width)+x],map[(y*width)+x+1],true,localPoint);
		}

		if (map[(y*width)+x+1].distance*map[((y+1)*width)+x+1].distance<0) {
			if (flag) {
				if ((point.direction!=0)&&(localPoint.x==point.x)&&(localPoint.y==point.y)) {
					point.direction=1;
					processEdge(map[(y*width)+x+1],map[((y+1)*width)+x+1],false,point);
					return;
				} else {
					point.x=localPoint.x;
					point.y=localPoint.y;
					point.direction=localPoint.direction;
					return;
				}
			}
			flag=true;
			localPoint.direction=1;
			processEdge(map[(y*width)+x+1],map[((y+1)*width)+x+1],false,localPoint);
		}

		if (map[((y+1)*width)+x].distance*map[((y+1)*width)+x+1].distance<0) {
			if (flag) {
				if ((point.direction!=2)&&(localPoint.x==point.x)&&(localPoint.y==point.y))	{
					point.direction=3;
					processEdge(map[((y+1)*width)+x],map[((y+1)*width)+x+1],true,point);
					return;
				} else {
					point.x=localPoint.x;
					point.y=localPoint.y;
					point.direction=localPoint.direction;
					return;
				}
			}
			flag=true;
			localPoint.direction=3;
			processEdge(map[((y+1)*width)+x],map[((y+1)*width)+x+1],true,localPoint);
		}

		if ((map[(y*width)+x].distance*map[((y+1)*width)+x].distance<0.0)&&(flag)) {
			if ((point.direction!=1)&&(localPoint.x==point.x)&&(localPoint.y==point.y)) {
				point.direction=0;
				processEdge(map[(y*width)+x],map[((y+1)*width)+x],false,point);
				return;
			} else {
				point.x=localPoint.x;
				point.y=localPoint.y;
				point.direction=localPoint.direction;
			}
		} else {
			System.err.println("Unexpected situation in getNextPoint() cell: ("+c0.x+","+c0.y+")");
		}
	}

	// =======================================================================

	void processEdge(Cell c0, Cell c1, boolean flag, ContourPoint cp) {

		if (flag) {
			cp.x=(int)Math.round((double)cellSize*((double)c0.x+c0.distance/(c0.distance-c1.distance)));
			cp.y=(cellSize*c0.y);
		} else {
			cp.y=(int)Math.round((double)cellSize*((double)c0.y+c0.distance/(c0.distance-c1.distance)));
			cp.x=(cellSize*c0.x);
		}
	}

	// =======================================================================

	private class ContourPoint {public int x=0, y=0, direction=0;}
}

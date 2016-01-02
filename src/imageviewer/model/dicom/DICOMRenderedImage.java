/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import java.io.FileInputStream;
import java.io.IOException;

import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;

import com.sun.media.imageio.stream.FileChannelImageInputStream;

// =======================================================================

public class DICOMRenderedImage implements RenderedImage {

	int minX=0, minY=0;
	int tileWidth=0, tileHeight=0;
	int height=0, width=0, numBands=1;
	int tileGridXOffset=0, tileGridYOffset=0;
	int minBitsAllocated=0, bitsAllocated=0, bitsStored=0;
	int maxPixelValue=0, maxTileX=0, maxTileY=0;

	long fileOffset=0;
	SampleModel sampleModel=null;
	ColorModel colorModel=null;
	Hashtable properties=new Hashtable();

	FileChannelImageInputStream fciis=null;
	String filename=null;

	// =======================================================================

	public DICOMRenderedImage(String filename, long fileOffset, int bitsAllocated, int bitsStored, int width, int height, int tileWidth, int tileHeight, int numBands) {

		this.filename=filename;
		initialize(fileOffset,bitsAllocated,bitsStored,width,height,tileWidth,tileHeight,numBands);
	}

	// =======================================================================

	private void initialize(long fileOffset, int bitsAllocated, int bitsStored, int width, int height, int tileWidth, int tileHeight, int numBands) {

		this.width=width;
		this.height=height;
		this.tileWidth=tileWidth;
		this.tileHeight=tileHeight;
		this.bitsAllocated=bitsAllocated;
		this.bitsStored=bitsStored;
		this.fileOffset=fileOffset;
		this.numBands=numBands;

		// Create the appropriate sample model based on the bits stored.
		// The default is to create a sample model for 16-bit.

		switch (bitsStored) {
			case 8:	sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,width,height,numBands); break;
		 case 16: sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_USHORT,width,height,numBands); break;
		 case 32:	sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_INT,width,height,numBands);	break;
		 default:	sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_USHORT,width,height,numBands);
		}

		colorModel=PlanarImage.createColorModel(sampleModel);
		minBitsAllocated=Math.min(bitsStored,15);
		maxPixelValue=(int)Math.pow(2,bitsStored)-1;
		maxTileX=XToTileX(getMaxX()-1);
		maxTileY=YToTileY(getMaxY()-1);
	}

	// =======================================================================

	public static int XToTileX(int x, int tileGridXOffset, int tileWidth) {

		x-=tileGridXOffset;
		if (x<0) x+=(1-tileWidth); // Force round to -infinity
		return x/tileWidth;
	}

	public static int YToTileY(int y, int tileGridYOffset, int tileHeight) {

		y-=tileGridYOffset;
		if (y<0) y+=(1-tileHeight); // Force round to -infinity
		return y/tileHeight;
	}

	public static int tileXToX(int tx,int tileGridXOffset,int tileWidth) {return (tx*tileWidth)+tileGridXOffset;}
	public static int tileYToY(int ty,int tileGridYOffset,int tileHeight) {return (ty*tileHeight)+tileGridYOffset;}

	// =======================================================================

	public int getMinX() {return minX;}
	public int getMaxX() {return (minX+width);}
	public int getMinY() {return minY;}
	public int getMaxY() {return minY+height;}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public int getTileWidth() {return tileWidth;}
	public int getTileHeight() {return tileHeight;}
	public int getTileGridXOffset() {return tileGridXOffset;}
	public int getTileGridYOffset() {return tileGridYOffset;}
	public int getMinTileX() {return 0;}
	public int getMaxTileX() {return maxTileX;}
	public int getNumXTiles() {return maxTileX+1;}
	public int getMinTileY() {return 0;}
	public int getMaxTileY() {return maxTileY;}
	public int getNumYTiles() {return maxTileY+1;}

	public Rectangle getBounds() {return new Rectangle(minX,minY,width,height);}
	public SampleModel getSampleModel() {return sampleModel;}
	public ColorModel getColorModel() {return colorModel;}
	public Vector getSources() {return null;}

	// =======================================================================

	public Object getProperty(String name) {return properties.get(name.toLowerCase());}

	public String[] getPropertyNames() {

		String[] names=new String[properties.size()];
		int index=0;
		Enumeration e=properties.keys();
		while (e.hasMoreElements()) {
			String name=(String) e.nextElement();
			names[index++]=name;
		}
		return names;
	}

	public String[] getPropertyNames(String prefix) {

		String[] propertyNames=getPropertyNames();
		if (propertyNames==null) return null;
		prefix=prefix.toLowerCase();
		ArrayList names=new ArrayList();
		for (int i=0; i<propertyNames.length; i++) {
			if (propertyNames[i].startsWith(prefix)) names.add(propertyNames[i]);
		}

		if (names.size()==0) return null;

		// Copy the strings from the ArrayList over to a String array.

		String[] prefixNames=new String[names.size()];
		int count=0;
		for (Iterator it=names.iterator(); it.hasNext();) prefixNames[count++]=(String) it.next();
		return prefixNames;
	}

	// =======================================================================
	
	public int XToTileX(int x) {return XToTileX(x,getTileGridXOffset(),getTileWidth());}
	public int YToTileY(int y) {return YToTileY(y,getTileGridYOffset(),getTileHeight());}

	public int tileXToX(int tx) {return (tx*tileWidth)+tileGridXOffset;}
	public int tileYToY(int ty) {return (ty*tileHeight)+tileGridYOffset;}

	// =======================================================================

	public Raster getData() {

		Rectangle rect=new Rectangle(minX,minY,width,height);		
		return getData(rect);
	}

	// =======================================================================
	/**
	 * Returns an arbitrary rectangular region of the RenderedImage in
	 * a raster.
	 * <p> The rectangle of interest will be clipped against the image
	 * bounds.
	 *
	 * @param bounds
	 * @return bitmap image
	 * @see java.awt.image.RenderedImage#getData(Rectangle)
	 */

	public Raster getData(Rectangle bounds) {

		// The returned Raster is semantically a copy.  This means that
		// updates to the source image will not be reflected in the
		// returned Raster.  For non-writable (immutable) source images,
		// the returned value may be a reference to the image's internal
		// data.  The returned Raster should be considered non-writable;
		// any attempt to alter its pixel data (such as by casting it to
		// WritableRaster or obtaining and modifying its DataBuffer) may
		// result in undefined behavior.  The copyData method should be
		// used if the returned Raster is to be modified.

		int startX=XToTileX(bounds.x);
		int startY=YToTileY(bounds.y);
		int endX=XToTileX((bounds.x+bounds.width)-1);
		int endY=YToTileY((bounds.y+bounds.height)-1);

		if ((startX==endX)&&(startY==endY)) {

			Raster tile=getTile(startX,startY);
			Raster r=tile.createChild(bounds.x,bounds.y,bounds.width,bounds.height,bounds.x,bounds.y,null);
			return r;

		} else {

			// Create a WritableRaster of the desired size and translate

			SampleModel sm=sampleModel.createCompatibleSampleModel(bounds.width,bounds.height);
			WritableRaster dest=Raster.createWritableRaster(sm,bounds.getLocation());

			for (int j=startY; j<=endY; j++) {
				for (int i=startX; i<=endX; i++) {
					Raster tile=getTile(i,j);
					if (tile!=null) {
						Rectangle tileRect=tile.getBounds();
						Rectangle intersectRect=bounds.intersection(tileRect);
						Raster liveRaster=tile.createChild(intersectRect.x,intersectRect.y,intersectRect.width,intersectRect.height,intersectRect.x,intersectRect.y,null);
						dest.setDataElements(0,0,liveRaster);
					}
				}
			}
			return dest;
		}
	}

	// =======================================================================
	/**
	 * Copies an arbitrary rectangular region of the RenderedImage into
	 * a caller-supplied WritableRaster.
	 * <p>
	 * The region to be computed is determined by clipping the bounds of
	 * the supplied WritableRaster against the bounds of the image. The
	 * supplied WritableRaster must have a SampleModel that is compatible
	 * with that of the image.
	 * <p>
	 * If the raster argument is null, the entire image will be copied into
	 * a newly-created WritableRaster with a SampleModel that is compatible
	 * with that of the image.
	 *
	 * @param dest WritableRaster
	 * @return  raster with arbitrary rectangular region of rendered image
	 * @see java.awt.image.RenderedImage#copyData(WritableRaster)
	 */

	public WritableRaster copyData(WritableRaster dest) {

		Rectangle bounds=null;

		if (dest==null) {
			bounds=getBounds();
			Point p=new Point(minX,minY);
			SampleModel sm=sampleModel.createCompatibleSampleModel(width,height); 			// A SampleModel to hold the entire image
			dest=Raster.createWritableRaster(sm,p);
		} else {
			bounds=dest.getBounds();
		}

		int startX=XToTileX(bounds.x);
		int startY=YToTileY(bounds.y);
		int endX=XToTileX((bounds.x+bounds.width)-1);
		int endY=YToTileY((bounds.y+bounds.height)-1);

		for (int j=startY; j<=endY; j++) {
			for (int i=startX; i<=endX; i++) {
				Raster tile=getTile(i,j);
				if (tile!=null) {
					Rectangle tileRect=tile.getBounds();
					Rectangle intersectRect=bounds.intersection(tileRect);
					Raster liveRaster=tile.createChild(intersectRect.x,intersectRect.y,intersectRect.width,intersectRect.height,intersectRect.x,intersectRect.y,null);

					// WritableRaster.setDataElements takes into account of
					// inRaster's minX and minY and add these to x and y. Since
					// liveRaster has the origin at the correct location, the
					// following call should not again give these coordinates in
					// places of x and y.
					
					dest.setDataElements(0,0,liveRaster);
				}
			}
		}
		return dest;
	}

	// =======================================================================
	/**
	 * Returns tile given x,y location.
	 *
	 * @param tileX  the location of requested tile on x-axis
	 * @param tileY  the location of requested tile on y-axis
	 * @return raster of tile
	 * @see java.awt.image.RenderedImage#getTile(int, int)
	 */

	public Raster getTile(int tileX, int tileY) {

		if ((fciis==null)&&(filename!=null)) {
			try {
				FileChannel fc=new FileInputStream(filename).getChannel();
				fciis=new FileChannelImageInputStream(fc);
				fciis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
			} catch (Exception exc) {
				exc.printStackTrace();
				return null;
			}
		} 

		// Determine the correct pixel location for the requested tile.
		// Allocate the correct type of dataBuffer to handle the data;
		// this will be dependent on the bitsAllocated in the DICOM file.
		// Read the data from the ImageInputStream, and create the
		// corresponding raster to be returned.

		if ((tileX>maxTileX)||(tileY>maxTileY)||(tileX<0)||(tileY<0)) {
			String errorString=new String("Error in DICOMRenderedImage - Illegal tile call ("+tileX+","+tileY+")");
			throw new IllegalArgumentException(errorString);
		}

		try {
			Raster tile=getTile(bitsAllocated,tileX,tileY);
			if ((tileX==maxTileX)&&(tileY==maxTileY)) {
				fciis.close();
				fciis=null;
			}
			return tile;
		} catch (Exception exc) {
			System.err.println("Error attempting to create tile ("+tileX+","+tileY+") for: "+filename);
			exc.printStackTrace();
		}
		return null;
	}

	// =======================================================================
	/**
	 * Returns tile given x, y location.
	 *
	 * @param bitsAllocated
	 * @param tileX  the location of requested tile on x-axis
	 * @param tileY  the location of requested tile on y-axis
	 * @return raster of tile
	 * @throws IOException
	 */

	private Raster getTile(int bitsAllocated, int tileX, int tileY) throws IOException {

		// Basically, identify the first and last row that we are
		// interested in, and read the *entire* block of data in through
		// the file channel, as opposed to doing continuous seeks per row;
		// this block will minimize the amount of file i/o that has to
		// occur.  Once the entire block is read in, then simply take the
		// portion out that is in memory and convert it to the desired
		// data buffer.

		int xCoord=tileXToX(tileX);
		int yCoord=tileYToY(tileY);
		int bytesAllocated=bitsAllocated/8;

		DataBuffer db=null;

		// Find the first byte coordinate, based on x=0, y=yCoord.  Then
		// the amount of data to be read in through the file channel is
		// dependent on the number of bytes allocated per pixel.  The goal
		// then is to create the appropriate data buffer.
		
		long targetBytePosition=fileOffset+(yCoord*numBands*width*bytesAllocated);
		if (targetBytePosition!=fciis.length()) fciis.seek(targetBytePosition);
		int size=(width*tileHeight*numBands);

		switch (bytesAllocated) {

			// Note that we need to reverse the RGB (specifically, R and B)
			// in color images for this to work correctly...

		  case 1: byte[] byteArray=new byte[size];
			        fciis.readFully(byteArray,0,size);
							if (tileWidth!=width) {
								size=tileWidth*tileHeight*numBands;
								byte[] blockArray=new byte[size];
								for (int y=0; y<(tileHeight*numBands); y+=numBands) {
									for (int x=0; x<(tileWidth*numBands); x+=numBands) {
										if (numBands==1) {
											blockArray[(tileWidth*y)+x]=byteArray[(width*y)+(xCoord*numBands)+x];
										} else {
											for (int z=0; z<numBands; z++) blockArray[(tileWidth*y)+x+(numBands-z-1)]=byteArray[(width*y)+(xCoord*numBands)+x+z];
										}
									}
								}
								byteArray=blockArray;
							}
							db=new DataBufferByte(byteArray,size);
							break;
							

		  case 2: byte[] bArray=new byte[size*2];
				      try {
							  fciis.readFully(bArray,0,size*2);
							} catch (Exception exc) {
								System.err.println("Unexpected EOF for: "+filename+"("+fileOffset+","+targetBytePosition+")");
							}
							short[] tileArray=new short[size];
							for (int i=0; i<size; i+=numBands) {
								for (int j=0; j<numBands; j++) {
									short s=(short)((char)((bArray[(i*2)+1+j] & 0xffff) << 8) | (char)(bArray[(i*2)+j] & 0x00ff)); // Convert to unsigned shorts 
									tileArray[i+j]=(s>=maxPixelValue) ? 0 : ((s<0) ? 0 : s); 
								}
							}

							if (tileWidth!=width) {
								size=tileWidth*tileHeight*numBands;
								short[] blockArray=new short[size];
								for (int y=0; y<(tileHeight*numBands); y+=numBands) {
									for (int x=0; x<(tileWidth*numBands); x+=numBands) {
										for (int z=0; z<numBands; z++) {
											try {
												blockArray[(tileWidth*y)+x+z]=tileArray[(width*y)+(xCoord*numBands)+x+z];
											} catch (Exception exc) {
												blockArray[(tileWidth*y)+x+z]=0;
											}
										}
									}
								}
								tileArray=blockArray;
							} 
							db=new DataBufferUShort(tileArray,size);
							break;
	
		  case 4: int[] intArray=new int[size];
							fciis.readFully(intArray,0,size);
							if (tileWidth!=width) {
								size=tileWidth*tileHeight;
								int[] blockArray=new int[size];
								for (int y=0; y<tileHeight; y++) {
									for (int x=0; x<tileWidth; x++) {
										blockArray[(tileWidth*y)+x]=intArray[(width*y)+xCoord+x];
									}
								}
								intArray=blockArray;
							}
							db=new DataBufferInt(intArray,size,0);
							break;
		}

		if (numBands==1) {
			Raster tile=Raster.createRaster(sampleModel.createCompatibleSampleModel(tileWidth,tileHeight),db,new Point(xCoord,yCoord));
			return tile;
		} else {
			Raster tile=Raster.createWritableRaster(sampleModel.createCompatibleSampleModel(tileWidth,tileHeight),db,new Point(xCoord,yCoord));
			return tile;
		}
	}
}

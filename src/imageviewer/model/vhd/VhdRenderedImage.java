/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.vhd;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;

import java.awt.color.ColorSpace;

import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
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

// ================================================================================================

/**
 * Constructs rasterized image tiles from image data. Creates color models based on image type. Allows 
 *retrieval of image tiles using pixel-based xy-coordinates or tile-based xy-corrdinates.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 * @author Brian Burns, Jean Garcia, Kyle Singleton, Jamal Madni, Agatha Lee
 * @version $Revision: 1.1 $ $Date: 2008/12/05 10:23:00 $
 */
public class VhdRenderedImage implements RenderedImage {

	int minX=0, minY=0;
	int tileWidth=0, tileHeight=0;
	int height=0, width=0, numBands=1;
	int tileGridXOffset=0, tileGridYOffset=0;
	int bitsAllocated=0, fileType=0;
	int maxPixelValue=0, maxTileX=0, maxTileY=0;

	long fileOffset=0;
	SampleModel sampleModel=null;
	ColorModel colorModel=null;
	Hashtable properties=new Hashtable();
	//KYLE - Switched from Little to Big
	ByteOrder bo=ByteOrder.BIG_ENDIAN;

	FileChannelImageInputStream fciis=null;
	String filename=null;

	// ===============================================================================
	/** VhdRenderedImage constructor. Takes numerous image properties as parameters and
	 * initializes rendered image based on those properties.  
	 * 
	 * @param filename - image file name
	 * @param fileOffset - the size, in bytes, of the image file header
	 * @param bitsAllocated - the size, in bits, per pixel
	 * @param fileType - the image file type, see imageviewer.model.vhd.vhdheader for possible values
	 * @param maxPixelValue - the maximum per pixel value for this image
	 * @param width - the total tile width, in pixels
	 * @param height - the total tile height, in pixels
	 * @param tileWidth - the tile width, in pixels
	 * @param tileHeight - the tile height, in pixels
	 * @param bo - the image byte order encoding. can be ByteOrder.BIG_ENDIAN or ByteOrder.LITTLE_ENDIAN
	 * @see ImageViewer.model.vhd.vhRenderedImage#initialize(fileOffset, bitsAllocated, fileType, maxPixelValue, width, height, tileWidth, tileHeight, bo)
	 */
	public VhdRenderedImage(String filename, long fileOffset, int bitsAllocated, int fileType, int maxPixelValue,
															int width, int height, int tileWidth, int tileHeight, ByteOrder bo) {

		this.filename=filename;
		initialize(fileOffset,bitsAllocated,fileType,maxPixelValue,width,height,tileWidth,tileHeight,bo);
	}

	// =======================================================================
	/** VhdRenderedImage constructor helper function. Takes numerous image properties as parameters and
	 * initializes rendered image based on those properties. 
	 * <p>
	 * Creates the image sample and color models based on image file type. 
	 * 
	 * @param fileOffset - the size, in bytes, of the image file header
	 * @param bitsAllocated - the size, in bits, per pixel
	 * @param fileType - the image file type, see imageviewer.model.vhd.vhdheader for possible values
	 * @param maxPixelValue - the maximum per pixel value for this image
	 * @param width - the total tile width, in pixels
	 * @param height - the total tile height, in pixels
	 * @param tileWidth - the tile width, in pixels
	 * @param tileHeight - the tile height, in pixels
	 * @param bo - the image byte order encoding. can be ByteOrder.BIG_ENDIAN or ByteOrder.LITTLE_ENDIAN
	 */
	private void initialize(long fileOffset, int bitsAllocated, int fileType, int maxPixelValue, int width, int height, int tileWidth, int tileHeight, ByteOrder bo) {

		this.width=width;
		this.height=height;
		this.tileWidth=tileWidth;
		this.tileHeight=tileHeight;
		this.bitsAllocated=bitsAllocated;
		this.fileType=fileType;
		this.fileOffset=fileOffset;
		this.maxPixelValue=maxPixelValue;
		this.bo=bo;

		// Create the appropriate sample model based on the bits stored.
		// The default is to create a sample model for 16-bit.

		switch (fileType) {

		     case VhdHeader.GRAY8: sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,width,height,numBands); break;
		     case VhdHeader.GRAY16_SIGNED: sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_USHORT,width,height,numBands); break;
		     case VhdHeader.GRAY16_UNSIGNED: sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_USHORT,width,height,numBands); break;
		     case VhdHeader.GRAY32_INT:
		     case VhdHeader.GRAY32_FLOAT: sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_INT,width,height,numBands); break;
		     case VhdHeader.RGB:
			 numBands = 3;
			 sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,width,height,numBands); 
			 break;
		     case VhdHeader.RGB_PLANAR:
			 numBands = 3;
			 sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_BYTE,width,height,numBands); 
			 break;
		     default: sampleModel=RasterFactory.createPixelInterleavedSampleModel(DataBuffer.TYPE_SHORT,width,height,numBands);
		}

		// JAI doesn't do a DataBuffer.TYPE_SHORT automatically through
		// the planar image mechanism. Use a specific componentColorModel
		// instead, otherwise you'll get a null later on that appears out
		// of nowhere.

		colorModel=(sampleModel.getDataType()!=DataBuffer.TYPE_SHORT) ? PlanarImage.createColorModel(sampleModel) :
			new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),false,false,Transparency.OPAQUE,DataBuffer.TYPE_SHORT);

		maxTileX=XToTileX(getMaxX()-1);
		maxTileY=YToTileY(getMaxY()-1);
	}
	
	// =======================================================================
	/** Calculates the x-coordinate, in pixels, of a pixel within an image tile. 
	 * This function transforms pixel coordinates from absolute coordinates within 
	 * the image to pixel coordinates within an image tile.
	 * 
	 * @param x - the x-coordinate, in pixels, of the pixel within the image
	 * @param tileGridXOffset - the x-coordinate, in tiles,  of the image tile
	 * @param tileWidth - the tile width, in pixels
	 * @return a pixel's x-coordinate within an image tile
	 */
	public static int XToTileX(int x, int tileGridXOffset, int tileWidth) {

		x-=tileGridXOffset;
		if (x<0) x+=(1-tileWidth); // Force round to -infinity
		return x/tileWidth;
	}
	
	// =======================================================================
	/** Calculates the y-coordinate, in pixels, of a pixel within an image tile. 
	 * This function transforms pixel coordinates from absolute coordinates within 
	 * the image to pixel coordinates within an image tile. 
	 * 
	 * @param y - the y-coordinate, in pixels, of the pixel within the image
	 * @param tileGridYOffset - the y-coordinate, in tiles,  of the image tile
	 * @param tileHeight - the tile height, in pixels
	 * @return a pixel's y-coordinate within an image tile
	 */
	public static int YToTileY(int y, int tileGridYOffset, int tileHeight) {

		y-=tileGridYOffset;
		if (y<0) y+=(1-tileHeight); // Force round to -infinity
		return y/tileHeight;
	}
	
	// =======================================================================
	/** Calculates the x-coordinate, in pixels, of a pixel within an image. 
	 * This function transforms pixel coordinates from within an image tile to 
	 * absolute coordinates within an image.
	 * 
	 * @param tx - the x-coordinate, in pixels,  of the pixel within an image tile
	 * @param tileGridXOffset - the x-coordinate, in tiles,  of the image tile
	 * @param tileWidth - the tile width, in pixels
	 * @return a pixel's x-coordinate within an image
	 */
	public static int tileXToX(int tx,int tileGridXOffset,int tileWidth) {return (tx*tileWidth)+tileGridXOffset;}
	
	// =======================================================================
	/** Calculates the y-coordinate, in pixels, of a pixel within an image. 
	 * This function transforms pixel coordinates from within an image tile to 
	 * absolute coordinates within an image.
	 * 
	 * @param ty - the y-coordinate, in pixels,  of the pixel within an image tile
	 * @param tileGridYOffset - the y-coordinate, in tiles,  of the image tile
	 * @param tileHeight - the tile height, in pixels
	 * @return a pixel's y-coordinate within an image
	 */
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
	/** Returns an image property value based on its string representation
	 * 
	 * @param name - the property string name
	 * @return The property object if found, null otherwise
	 */
	public Object getProperty(String name) {return properties.get(name.toLowerCase());}
	
	// =======================================================================
	/** Returns an array of property string names for VhdRenderedImage 
	 * 
	 * @return a string array of property names
	 * @see imageviewer.model.vhd.vhdrenderedimage#getProperty(name) 
	 */
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
	
	// =======================================================================
	/** Returns an array of image property values whose string representations start
	 * with the prefix string
	 * 
	 * @param prefix - the property string prefix
	 * @return An array of matching property string names if found, null otherwise
	 */
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
	/** Calculates the x-coordinate, in pixels, of a pixel within an image tile. 
	 * This function transforms pixel coordinates from absolute coordinates within 
	 * the image to pixel coordinates within an image tile.
	 * 
	 * @param x - the x-coordinate, in pixels, of the pixel within the image
	 * @return a pixel's x-coordinate within an image tile
	 * @see imageviewer.model.vhd.VhdRenderedImage#XToTileX(int, int, int)
	 */
	public int XToTileX(int x) {return XToTileX(x,getTileGridXOffset(),getTileWidth());}

	// =======================================================================
	/** Calculates the y-coordinate, in pixels, of a pixel within an image tile. 
	 * This function transforms pixel coordinates from absolute coordinates within 
	 * the image to pixel coordinates within an image tile.
	 * 
	 * @param y - the y-coordinate, in pixels, of the pixel within the image
	 * @return a pixel's y-coordinate within an image tile
	 * @see imageviewer.model.vhd.VhdRenderedImage#YToTileY(int, int, int)
	 */
	public int YToTileY(int y) {return YToTileY(y,getTileGridYOffset(),getTileHeight());}

	// =======================================================================
	/** Calculates the x-coordinate, in pixels, of a pixel within an image. 
	 * This function transforms pixel coordinates from within an image tile to 
	 * absolute coordinates within an image.
	 * 
	 * @param tx - the x-coordinate, in pixels, of the pixel within an image tile
	 * @return a pixel's x-coordinate within an image
	 * @see imageviewer.model.vhd.VhdRenderedImage#tileXToX(int, int, int)
	 */
	public int tileXToX(int tx) {return (tx*tileWidth)+tileGridXOffset;}
	
	// =======================================================================
	/** Calculates the y-coordinate, in pixels, of a pixel within an image. 
	 * This function transforms pixel coordinates from within an image tile to 
	 * absolute coordinates within an image.
	 * 
	 * @param ty - the y-coordinate, in pixels, of the pixel within an image tile
	 * @return a pixel's y-coordinate within an image
	 * @see imageviewer.model.vhd.VhdRenderedImage#tileYToY(int, int, int)
	 */
	public int tileYToY(int ty) {return (ty*tileHeight)+tileGridYOffset;}

	// =======================================================================
	/** Returns the full Rasterized image data
	 * 
	 * @return A raster of the entire image 
	 */
	public Raster getData() {

		Rectangle rect=new Rectangle(minX,minY,width,height);
		return getData(rect);
	}

	// =======================================================================
	/** Returns an arbitrary rectangular region of the RenderedImage in
	 * a raster.
	 * <p> The rectangle of interest will be clipped against the image
	 * bounds.
	 *
	 * @param bounds - the xy-coordinate boundaries, in pixels, of the rectangular region of interest
	 * @return bitmap - the rasterized region of interest in the image
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
	 * Returns a tile given an x,y location, in tiles.
	 *
	 * @param tileX - the x-coordinate, in tiles, of the tile
	 * @param tileY - the y-coordinate, in tiles, of the tile
	 * @return a raster of the image tile data
	 * @see java.awt.image.RenderedImage#getTile(int, int)
	 */

	public Raster getTile(int tileX, int tileY) {

		if ((fciis==null)&&(filename!=null)) {
			try {
				FileChannel fc=new FileInputStream(filename).getChannel();
				fciis=new FileChannelImageInputStream(fc);
				fciis.setByteOrder(bo);
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
			System.err.println("Error attempting to create tile ("+tileX+","+tileY+").");
			exc.printStackTrace();
		}
		return null;
	}

	// =======================================================================
	/**
	 * Returns a rasterized image tile given its xy-coordinates
	 *
	 * @param bitsAllocated - the size, in bits, per pixel
	 * @param tileX - the x-coordinate, in tiles, of the tile
	 * @param tileY - the y-coordinate, in tiles, of the tile
	 * @return a raster of the image tile data specified by the xy-coordinates
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
		
		//fixed size calulation bug #1 from Analyze here
		long targetBytePosition=fileOffset+(yCoord*width*bytesAllocated);
			
		// Move the fciis file cursor to the first pixel of the region of interest in the image
		if (targetBytePosition<fciis.length()) fciis.seek(targetBytePosition);	 
		int size=(width*tileHeight*numBands);
		
		// fixed size calculation bug #2 from Analyze here
		int sizeColor=(width*tileHeight*bytesAllocated); //size of 1 row of tiles
		int fileSize=width*height*bytesAllocated;   //size of entire file (all tiles)

		switch (bytesAllocated) {

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

		  // KYLE - Replaced previous case 2 because Analyze uses a different read type
		  case 2:	short[] tileArray=new short[size];
		      try {
				fciis.readFully(tileArray,0,size);
			} catch (Exception exc) {
			        System.err.println("Unexpected EOF for: "+filename+"");
				//System.err.println("Unexpected EOF for: "+filename+"("+fileOffset+","+targetBytePosition+")");
			}

			   if (tileWidth!=width) {
				size=tileWidth*tileHeight*numBands;
				short[] blockArray=new short[size];
				for (int y=0; y<(tileHeight*numBands); y+=numBands) {
					for (int x=0; x<(tileWidth*numBands); x+=numBands) {
						for (int z=0; z<numBands; z++) {
							try {
								blockArray[(tileWidth*y)+x+z]=(short)tileArray[(width*y)+(xCoord*numBands)+x+z];
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

		  case 3: byte [] fileArray=null;
			  
			  //temporary tile pixel data buffer
			  byte[] blockArray;
			  
			  // file type is interleaved 24-bit RGB (.RGB)
			  if(filename.contains(".rgb")){
				  
				  //if file has not been read, read it in fully. Otherwise, read next tile
				  if(fileArray == null){
					  fileArray=new byte[fileSize];
					  try{
						  fciis.seek(0);
						  fciis.readFully(fileArray,0,fileSize);
					  } catch (Exception exc) {
						  System.err.println("Unexpected Read Error for: "+filename+"");
					  }
				  }
				  
				  //if more than 1 image tile
				  if (tileWidth!=width) {
					  	
					    //initialize tile pixel data buffer
					  	blockArray=new byte[sizeColor];
					  	
					  	//iterate over each pixel in the tile and pull corresponding 
						//RGB out of file array
					  	for (int y=0; y<(tileHeight*numBands); y+=numBands) {
					  		for (int x=0; x<(tileWidth*numBands); x+=numBands) {
					  			for (int z=0; z<numBands; z++) {
					  				try {
					  					blockArray[(tileWidth*y)+x+(numBands-z-1)]=fileArray[(width*y)+x+z+xCoord*numBands+yCoord*width*numBands];
					  				} catch (Exception exc) {
					  					blockArray[(tileWidth*y)+x+z]=0;
					  				}
					  			}
					  		}
					  	}
					}else{
						//only 1 image tile
						blockArray=fileArray;
					}
				  	
				  	//wrap tile pixel data in DataBufferByte class
				    db=new DataBufferByte(blockArray,size,0);
				  
			  // file type is planar 24-bit RGB (.RAW)
			  }else{	
				  
				  //if file has not been read, read it in fully. Otherwise, read next tile
				  if(fileArray == null){
					  fileArray=new byte[fileSize];
					  try{
						  fciis.seek(0);
						  fciis.readFully(fileArray,0,fileSize);
					  } catch (Exception exc) {
						  System.err.println("Unexpected Read Error for: "+filename+"");
					  }
				  }  
				  
				  //initialize tile pixel data buffer
				  blockArray=new byte[sizeColor];
				  
				  // byte offsets for red, green, and blue pixel data into file array 
				  int redOffset = 0;
				  int greenOffset = bytesAllocated*width*height/numBands;
				  int blueOffset = 2*bytesAllocated*width*height/numBands;
				
				  //iterate over each pixel in the tile and pull corresponding 
				  //RGB out of file array 
				  for (int y=0; y<tileHeight*numBands; y+=numBands) {
					  for (int x=0; x<tileWidth*numBands; x+=numBands) {
						  //blue
						  try {
							  blockArray[x+(tileWidth*y)+0]=fileArray[blueOffset+(x+y*width)/numBands+xCoord+yCoord*width];
						  } catch (Exception exc) {
							  blockArray[x+(tileWidth*y)+0]=0;
						  }
						  //green
						  try {
							  blockArray[x+(tileWidth*y)+1]=fileArray[greenOffset+(x+y*width)/numBands+xCoord+yCoord*width];
						  } catch (Exception exc) {
							  blockArray[x+(tileWidth*y)+1]=0;
						  }
						  //red
						  try {
							  blockArray[x+(tileWidth*y+2)]=fileArray[redOffset+(x+y*width)/numBands+xCoord+yCoord*width];
						  } catch (Exception exc) {
							  blockArray[x+(tileWidth*y)+2]=0;
						  }

					  }
				  }
			  	  //wrap tile pixel data in DataBufferByte class
			      db=new DataBufferByte(blockArray,size,0);
			  }  
			  break;

		  case 4: int[] intArray=new int[size];
							fciis.readFully(intArray,0,size);
							if (tileWidth!=width) {
								size=tileWidth*tileHeight;
								int[] blockArrayB=new int[size];
								for (int y=0; y<tileHeight; y++) {
									for (int x=0; x<tileWidth; x++) {
										blockArrayB[(tileWidth*y)+x]=intArray[(width*y)+xCoord+x];
									}
								}
								intArray=blockArrayB;
							}
							db=new DataBufferInt(intArray,size,0);
							break;
		}

		return (numBands==1) ? RasterFactory.createRaster(sampleModel.createCompatibleSampleModel(tileWidth,tileHeight),db,new Point(xCoord,yCoord)) :
			                     RasterFactory.createWritableRaster(sampleModel.createCompatibleSampleModel(tileWidth,tileHeight),db,new Point(xCoord,yCoord));
	}
}

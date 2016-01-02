/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.raw;

import imageviewer.model.Image;
import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;

import java.io.FileInputStream;
import java.io.File;
import javax.imageio.stream.FileImageOutputStream;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.media.jai.TiledImage;

import com.sun.media.imageio.stream.FileChannelImageInputStream;

// =======================================================================

public class RawImageReader extends ImageReader {

	public static final int DEFAULT_TILE_WIDTH=256;
	public static final int DEFAULT_TILE_HEIGHT=256;

	// =======================================================================

	int imageWidth=DEFAULT_TILE_WIDTH, imageHeight=DEFAULT_TILE_HEIGHT;
	int bitsAllocated=16, bitsStored=16, numImages=1;
	long fileOffset=0;

	public RawImageReader() {super();}
	public RawImageReader(int imageWidth, int imageHeight, int bitsAllocated, int bitsStored, long fileOffset, int numImages) {

		super();
		this.imageWidth=imageWidth;
		this.imageHeight=imageHeight;
		this.bitsAllocated=bitsAllocated;
		this.bitsStored=bitsStored;
		this.fileOffset=fileOffset;
		this.numImages=numImages;
	}

	// =======================================================================

	public ImageSequence readDirectory(String dir, boolean recurse) {

		OpenImageSetTask oist=new OpenImageSetTask(dir,recurse);
		ArrayList<Image> images=oist.openImages();
		return new RawImageSeries(images);
	}

	public ArrayList<Image> readImages(String[] files) {

		OpenImageSetTask oist=new OpenImageSetTask(files);
		return oist.openImages();
	}

	public ArrayList<Image> readImages(String dir, boolean recurse) {

		OpenImageSetTask oist=new OpenImageSetTask(dir,recurse);
		return oist.openImages();
	}

	// =======================================================================
	// Really a dummy method to satisfy the interface...

	public ArrayList<RawImageStudy> organizeByStudy(ArrayList<? extends Image> unsortedImages) {return organizeByStudy(new RawImageSeries(unsortedImages));}

	public ArrayList<RawImageStudy> organizeByStudy(ImageSequence unsortedImageSequence) {

		ArrayList<RawImageStudy> imageStudyList=new ArrayList<RawImageStudy>();
		ArrayList<RawImageSeries> seriesList=new ArrayList<RawImageSeries>();
		RawImageSeries series=new RawImageSeries(unsortedImageSequence.getSequence());
		seriesList.add(series);
		RawImageStudy ris=new RawImageStudy(seriesList);
		imageStudyList.add(ris);
		return imageStudyList;
	}

	// =======================================================================

	public int getBitsAllocated() {return bitsAllocated;}
	public int getBitsStored() {return bitsStored;}
	public int getWidth() {return imageWidth;}
	public int getHeight() {return imageHeight;}
	public long getFileOffset() {return fileOffset;}

	public void setBitsAllocated(int x) {bitsAllocated=x;}
	public void setBitsStored(int x) {bitsStored=x;}
	public void setFileOffset(long x) {fileOffset=x;}
	public void setNumImages(int x) {numImages=x;}
	public void setWidth(int x) {imageWidth=x;}
	public void setHeight(int x) {imageHeight=x;}

	// =======================================================================

	public void setParameters(ArrayList<Integer> paramList) {

		Integer i=paramList.get(0); if (i!=null) imageWidth=i.intValue();
		i=paramList.get(1); if (i!=null) imageHeight=i.intValue();
		i=paramList.get(2); if (i!=null) bitsAllocated=i.intValue();
		i=paramList.get(3); if (i!=null) bitsStored=i.intValue();
		i=paramList.get(4); if (i!=null) fileOffset=i.intValue();
		i=paramList.get(5); if (i!=null) numImages=i.intValue();
	}

	// =======================================================================

	public ArrayList<RawImage> readFile(String filename) {

		try {

			ArrayList<RawImage> al=new ArrayList<RawImage>();
			FileChannel fc=new FileInputStream(filename).getChannel();
			FileChannelImageInputStream fciis=new FileChannelImageInputStream(fc);
			fciis.setByteOrder(ByteOrder.LITTLE_ENDIAN);

			int maxPixelValue=(int) Math.pow(2,bitsAllocated)-1;
			int tileWidth=Math.min(((imageWidth>512) ? 512 : DEFAULT_TILE_WIDTH),imageWidth);
			int tileHeight=Math.min(((imageHeight>512) ? 512 : DEFAULT_TILE_HEIGHT),imageHeight);
			int minBitsAllocated=Math.min(bitsStored,15);

			fciis.seek(fileOffset);
			
			try {
				for (int loop=0; loop<numImages; loop++) {
					long filePosition=fciis.getStreamPosition();
					RawRenderedImage rri=new RawRenderedImage(filename,filePosition,bitsAllocated,bitsStored,imageWidth,imageHeight,tileWidth,tileHeight);
					TiledImage ti=new TiledImage(rri,tileWidth,tileHeight);					
					RawImage ri=new RawImage(ti,filename,filePosition);
					ri.setMaxPixelValue(maxPixelValue);
					ri.setFileOffset(filePosition);
					ri.setBitsAllocated(bitsAllocated);
					ri.setBitsStored(bitsStored);
					ri.setBitDepth(minBitsAllocated); 
					ri.setWidth(imageWidth);
					ri.setHeight(imageHeight);
					al.add(ri);
					fciis.seek(fciis.getStreamPosition()+(imageWidth*imageHeight*bitsAllocated/8));
					ri=null;
					ti=null;
					rri=null;
				}
			} catch (Exception exc) {
				LOG.error("Unable to process raw image file: "+filename);
				exc.printStackTrace();
			}
			fciis.close();
			return al;
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}
	
	/*public String DecompressFile(String filename){
		
		byte buf[]=new byte[2048];
		//int length = 0; 
		int offset = 0;
		
        try{
			//open file input and output streams
			FileInputStream fis = new FileInputStream(filename);
			//remove the .gz from filename extension
			filename = filename.substring(0, filename.length()-3);
			FileImageOutputStream fos = new FileImageOutputStream(new File(filename));
			fos.setByteOrder(ByteOrder.LITTLE_ENDIAN);
			
			// decompress here
			GZIPInputStream gzipfis = new GZIPInputStream (fis);
			
			for( int length=gzipfis.read(buf,offset,2048); length >0; length=gzipfis.read(buf,offset,2048)){
				fos.write(buf, 0, length);
			}
			
			//close input and output streams
			fis.close();
			fos.close();
	
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		return filename;
	}*/
	// =======================================================================
	// Convenience method to handle the issue of loading/unloading a given
	// image.

	public static TiledImage readFile(int imageWidth, int imageHeight, int bitsAllocated, int bitsStored, String filename, long filePosition) {

		int tileWidth=Math.min(((imageWidth>512) ? 512 : DEFAULT_TILE_WIDTH),imageWidth);
		int tileHeight=Math.min(((imageHeight>512) ? 512 : DEFAULT_TILE_HEIGHT),imageHeight);
		RawRenderedImage rri=new RawRenderedImage(filename,filePosition,bitsAllocated,bitsStored,imageWidth,imageHeight,tileWidth,tileHeight);
		TiledImage ti=new TiledImage(rri,tileWidth,tileHeight);
		rri=null;
		return ti;
	}
}

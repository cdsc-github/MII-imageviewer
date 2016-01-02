/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.analyze;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.sun.media.imageio.stream.FileChannelImageInputStream;

import imageviewer.util.StringUtilities;

// =======================================================================

public class AnalyzeHeader {

	public static final int GRAY8=0;
	public static final int GRAY16_SIGNED=1; 	  // 16-bit signed integer (-32768-32767). Imported signed images are converted to unsigned by adding 32768. 
	public static final int GRAY16_UNSIGNED=2; 	// 16-bit unsigned integer (0-65535). 
	public static final int GRAY32_INT=3;       // 32-bit signed integer. Imported 32-bit integer images are converted to floating-point. 
	public static final int GRAY32_FLOAT=4; 	  // 32-bit floating-point. 

	public static final int COLOR8=5; 	        // 8-bit unsigned integer with color lookup table. 
	public static final int RGB=6; 	            // 24-bit interleaved RGB. Import/export only. 
	public static final int RGB_PLANAR=7; 	    // 24-bit planer RGB. Import only. 
	public static final int BITMAP=8; 	        // 1-bit black and white. Import only.
	public static final int ARGB=9;             // 32-bit interleaved ARGB. Import only. 
	public static final int BGR=10; 	          // 24-bit interleaved BGR. Import only. 
	public static final int GRAY32_UNSIGNED=11; // 32-bit unsigned integer. Imported 32-bit integer images are converted to floating-point. 

	// =======================================================================

	int width=0, height=0, nImages=1, gapBetweenImages=0, lutSize=256, fileType=GRAY8, extents=0;
	int sessionError=0, dataType=0, views=0, volumesAdded=0, startField=0, fieldSkip=0, oMax=0, oMin=0, sMax=0, sMin=0, bitsAllocated=0;
	float pixelWidth=1.0f, pixelHeight=1.0f, sliceThickness=1.0f, calibrationMin=0.0f, calibrationMax=0.0f, roiScale=1.0f;
	double[] pixelDimensions=new double[8];
	int[] dimensions=new int[8];
	char regular, orientation;
	long offset=0;

	boolean whiteIsZero=false;
	ByteOrder bo=ByteOrder.LITTLE_ENDIAN;
	String filename=null, description=null, dt=null, databaseName=null, auxilliaryFile=null, originator=null, generated=null;
	String scanNumber=null, patientID=null, expirationDate=null, expirationTime=null, unit=null, calibration=null;
	
	// =======================================================================

	public AnalyzeHeader() {}

	// =======================================================================

	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public int getNumSlices() {return nImages;}
	public int getBitsAllocated() {return bitsAllocated;}
	public int getFileType() {return fileType;}

	public float getCalibrationMin() {return calibrationMin;}
	public float getCalibrationMax() {return calibrationMax;}

	public ByteOrder getByteOrder() {return bo;}

	public String getFilename() {return filename;}
	public String getDescription() {return description;}
	public String getPatientID() {return patientID;}
	public String getExpirationDate() {return expirationDate;}
	public String getExpirationTime() {return expirationTime;}

	public double[] getPixelDimensions() {return pixelDimensions;}

	// =======================================================================

	public boolean readAnalyzeHeader(String filename) {

		try {
			if (filename==null) return false;
			FileChannel fc=new FileInputStream(filename).getChannel();
			if (fc.size()==0) return false;
			this.filename=filename;
			FileChannelImageInputStream fciis=new FileChannelImageInputStream(fc);
			fciis.setByteOrder(ByteOrder.BIG_ENDIAN);
			boolean b=readAnalyzeHeader(fciis);
			fciis.close();
			return b;
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return false;
	}

	// =======================================================================

	private boolean readAnalyzeHeader(FileChannelImageInputStream fciis) {

		try {

			// Start of the Analyze header_key struct.
	
			fciis.readInt();                     // Header size
			dt=readString(fciis,10);             // Data type
			databaseName=readString(fciis,18);   // DB name
			extents=fciis.readInt();             // Extents (should be 16384)
			sessionError=fciis.readShort();      // Session error?
			regular=(char)fciis.readByte();      // Should be "r" so that all images in volume are same dimension
			fciis.readByte();                    // Unused

			// Store the image dimensions. Can have up to 8 different ones.
			// Start of the Analyze image_dimension struct.

			short endian=fciis.readShort();
			if ((endian<0)||(endian>15)) {
				fciis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				bo=ByteOrder.LITTLE_ENDIAN;
			} else {
				bo=ByteOrder.BIG_ENDIAN;
			}

			dimensions[0]=endian;
			width=fciis.readShort(); dimensions[1]=width;
			height=fciis.readShort(); dimensions[2]=height;
			nImages=fciis.readShort(); dimensions[3]=nImages;
			for (int loop=0; loop<4; loop++) dimensions[loop+4]=fciis.readShort();

			unit=readString(fciis,4);            // Voxel units
			calibration=readString(fciis,8);     // Name of calibration units
			fciis.readShort();                   // Unused
			
			int dataType=(int)fciis.readShort();
			switch (dataType) {
			    case 2: fileType=GRAY8; bitsAllocated=8; break;
			    case 4: fileType=GRAY16_SIGNED; bitsAllocated=16; break;
			    case 8: fileType=GRAY32_INT; bitsAllocated=32; break;
			   case 16: fileType=GRAY32_FLOAT; bitsAllocated=32; break;
			  case 128: fileType=RGB_PLANAR; bitsAllocated=24; break;
			   default: fileType=0;
			}

			fciis.readShort();                   // Bits per pixel; but we've already computed it, so skip...
			fciis.readShort();                   // Unused
	
			// Store the pixel dimensions. Can have up to 8 different ones.
	
			pixelDimensions[0]=(double)fciis.readFloat();
			pixelWidth=fciis.readFloat(); pixelDimensions[1]=(double)pixelWidth; 
			pixelHeight=fciis.readFloat(); pixelDimensions[2]=(double)pixelHeight; 
			sliceThickness=fciis.readFloat(); pixelDimensions[3]=(double)sliceThickness; 
			for (int loop=0; loop<4; loop++) pixelDimensions[loop+4]=(double)fciis.readFloat();	

			offset=(long)fciis.readFloat();      // Voxel offset
			roiScale=fciis.readFloat();          // ROI scaling factor
			fciis.readFloat();                   // Unused
			fciis.readFloat();                   // Unused
			calibrationMax=fciis.readFloat();    // Calibration range values
			calibrationMin=fciis.readFloat();

			int compressed=fciis.readInt();
			int verified=fciis.readInt();
			int globalMax=fciis.readInt(); 
			int globalMin=fciis.readInt(); 

			// Start of the Analyze data_history struct

			description=readString(fciis,80);
			auxilliaryFile=readString(fciis,24);
			orientation=(char)fciis.readByte();
			originator=readString(fciis,10);
			generated=readString(fciis,10);
			scanNumber=readString(fciis,10);
			patientID=readString(fciis,10);
			expirationDate=readString(fciis,10);
			expirationTime=readString(fciis,10);
						
			fciis.readByte();                    // Unused
			fciis.readByte();                    // Unused
			fciis.readByte();                    // Unused
	
			views=fciis.readInt();               // Views
			volumesAdded=fciis.readInt();        // vols_added
			startField=fciis.readInt(); 
			fieldSkip=fciis.readInt(); 
			oMax=fciis.readInt(); 
			oMin=fciis.readInt();
			sMax=fciis.readInt();
			sMin=fciis.readInt();
			return true;
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return false;
	}

	// =======================================================================

	private String readString(FileChannelImageInputStream fciis, int length) {

		try {
			byte[] b=new byte[length]; 
			fciis.readFully(b); 
			String s=new String(b);
			return StringUtilities.clean(s).trim();
		} catch (Exception exc) {
			exc.printStackTrace();
		} 
		return null;
	}

	// =======================================================================
	// Returns the number of bytes used per pixel. 

	public int getBytesPerPixel() {

		switch (fileType) {

		  case GRAY8: case COLOR8: case BITMAP: return 1;
		  case GRAY16_SIGNED: case GRAY16_UNSIGNED: return 2;
		  case GRAY32_INT: case GRAY32_UNSIGNED: case GRAY32_FLOAT: case ARGB: return 4;
		  case RGB: case RGB_PLANAR: case BGR: return 3;
		  default: return 0;
		}
	}

	// =======================================================================

	private String getType() {

		switch (fileType) {

		  case GRAY8: return new String("byte");
		  case GRAY16_SIGNED: return new String("short");
		  case GRAY16_UNSIGNED: return new String("ushort");
		  case GRAY32_INT: return new String("int");
		  case GRAY32_UNSIGNED: return new String("uint");
		  case GRAY32_FLOAT: return new String("float");
		  case COLOR8: return new String("byte+lut");
		  case RGB: return new String("RGB");
		  case RGB_PLANAR: return new String("RGB(p)");
		  case BITMAP: return new String("bitmap");
		  case ARGB: return new String("ARGB");
		  case BGR: return new String("BGR");
		  default: return new String("");
		}
	}

	// =======================================================================

	public String toString() {return new String("name="+filename+", width="+width+", height="+height+", nImages="+nImages+
																							",type="+getType()+", offset="+offset+", whiteZero="+(whiteIsZero ? "t" : "f")+", byteOrder="+bo+
																							",lutSize="+lutSize);}

	// =======================================================================

	public static void main(String args[]) {
		
		if (args.length==1) {
			AnalyzeHeader ah=new AnalyzeHeader();
			ah.readAnalyzeHeader(args[0]);
			System.err.println(ah);
		}
	}
}

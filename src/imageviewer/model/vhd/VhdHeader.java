/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.vhd;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.sun.media.imageio.stream.FileChannelImageInputStream;

import imageviewer.util.StringUtilities;

// =======================================================================
/**
 * Contructs variables to hold image specific data and reads values derived
 * from header information of the current Visible Human Dataset file being 
 * opened. Provides accessor functions to information which is important
 * for image display and sorting.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 * @author Brian Burns, Jean Garcia, Kyle Singleton, Jamal Madni, Agatha Lee
 * @version $Revision: 1.1 $ $Date: 2008/12/05 10:23:00 $
 */

public class VhdHeader {

	public static final int GRAY8=0;
	public static final int GRAY16_SIGNED=1; 	  // 16-bit signed integer (-32768-32767). Imported signed images are converted to unsigned by adding 32768.
	public static final int GRAY16_UNSIGNED=2; 	  // 16-bit unsigned integer (0-65535).
	public static final int GRAY32_INT=3;             // 32-bit signed integer. Imported 32-bit integer images are converted to floating-point.
	public static final int GRAY32_FLOAT=4; 	  // 32-bit floating-point.

	public static final int COLOR8=5; 	          // 8-bit unsigned integer with color lookup table.
	public static final int RGB=6; 	                  // 24-bit interleaved RGB. Import/export only.
	public static final int RGB_PLANAR=7; 	          // 24-bit planer RGB. Import only.
	public static final int BITMAP=8; 	          // 1-bit black and white. Import only.
	public static final int ARGB=9;                   // 32-bit interleaved ARGB. Import only.
	public static final int BGR=10; 	          // 24-bit interleaved BGR. Import only.
	public static final int GRAY32_UNSIGNED=11;       // 32-bit unsigned integer. Imported 32-bit integer images are converted to floating-point.

	// =======================================================================

	// Added magic, compression, headerSize
        // Some variables are left over from ANALYZE and may not be part of a Vhd header
	int width=0, height=0, nImages=1, gapBetweenImages=0, lutSize=256, fileType=GRAY8, extents=0, magic=0, compression=0, headerSize=0;
	int sessionError=0, dataType=0, views=0, volumesAdded=0, startField=0, fieldSkip=0, oMax=0, oMin=0, sMax=0, sMin=0, bitsAllocated=0;
	float pixelWidth=1.0f, pixelHeight=1.0f, sliceThickness=1.0f, calibrationMin=0.0f, calibrationMax=0.0f, roiScale=1.0f;
	double[] pixelDimensions=new double[8];
	int[] dimensions=new int[8];
	char regular, orientation;
	long offset=0;
        int echoTotal=0, echoNumber=0;

        // variables for organizing studies
        int examNumber = 0;
        int seriesNumber = 0;

	boolean whiteIsZero=false;
	// Changed Little to Big endian for Visible Human Data
	ByteOrder bo=ByteOrder.BIG_ENDIAN;
	String filename=null, description=null, dt=null, databaseName=null, auxilliaryFile=null, originator=null, generated=null;
	String scanNumber=null, patientID=null, expirationDate=null, expirationTime=null, unit=null, calibration=null;

	// =======================================================================

	public VhdHeader() {}

	// =======================================================================

    	/**
	 * Returns the width of the image found in the header.
	 *
	 * @return int - pixel width
	 */
	public int getWidth() {return width;}
    	/**
	 * Returns the height of the image found in the header.
	 *
	 * @return int - pixel height
	 */
	public int getHeight() {return height;}
    	/**
	 * Returns the number of images expected in the dataset.
	 *
	 * @return int - number of images
	 */
	public int getNumSlices() {return nImages;}
    	/**
	 * Returns the bit depth of the current image.
	 *
	 * @return int - bit depth per pixel 
	 */
	public int getBitsAllocated() {return bitsAllocated;}
	/**
	 * Returns the file type of the current image.
	 *
	 * @return int - file type as defined in imageviewer.model.vhd.vhdheader
	 */
	public int getFileType() {return fileType;}
	/**
	 * Returns the minimum grayscale intensity of the current image.
	 *
	 * @return int - minimum grayscale intensity
	 */
	public float getCalibrationMin() {return calibrationMin;}
	/**
	 * Returns the maximum grayscale intensity of the current image.
	 *
	 * @return int - maximum grayscale intensity 
	 */
	public float getCalibrationMax() {return calibrationMax;}
	/**
	 * Returns the byte order encoding of the current image.
	 *
	 * @return ByteOrder - byte encoding of the image
	 */
	public ByteOrder getByteOrder() {return bo;}
	/**
	 * Returns the filename of the current image.
	 *
	 * @return String - image filename 
	 */
	public String getFilename() {return filename;}
	/**
	 * Returns the series description of the current image.
	 *
	 * @return String - series description 
	 */
	public String getDescription() {return description;}
	/**
	 * Returns the patient identifier of the current image.
	 *
	 * @return String - patient identifier
	 */
	public String getPatientID() {return patientID;}
	/**
	 * Returns the expiration date of the current image
	 *
	 * @return String - image expiration date 
	 */
	public String getExpirationDate() {return expirationDate;}
	/**
	 * Returns the expiration time of the current image.
	 *
	 * @return String - image expiration time
	 */
	public String getExpirationTime() {return expirationTime;}
	/**
	 * Returns the spatial pixel size of the current image.
	 *
	 * @return double - spatial pixel dimensions
	 */
	public double[] getPixelDimensions() {return pixelDimensions;}
    
	// Added accessor getHeaderSize
	/**
	 * Returns the total size, in bytes, of the image header.
	 *
	 * @return int - image header size, in bytes 
	 */
	public int getHeaderSize() {return headerSize;}
        // accessor functions for organizing studies
    	/**
	 * Returns the Exam Number of the current image for use in sorting.
	 *
	 * @return int - exam number
	 */
        public int getExamNumber() {return examNumber;}
    	/**
	 * Returns the Series Number of the current image for use in sorting.
	 *
	 * @return int - series number
	 */
        public int getSeriesNumber() {return seriesNumber;}
    	/**
	 * Returns the expected number of series taken consecutively. The value
	 * is an indicator for MR scans when two related readings did not have their
	 * series number changed (i.e. T2 and PD scans)
	 *
	 * @return int - number of scan types within a series
	 */
        public int getEchoTotal() {return echoTotal;}
    	/**
	 * Returns the Echo Number of the current image for use in sorting.
	 *
	 * @return int - echo number of image
	 */
	public int getEchoNumber() {return echoNumber;}

	// =======================================================================

	/**
	 * Opens file channel for current file.  Calls secondary method to read specific information
	 * from the header of the file.  Returns a boolean to indicate success of the read.
	 *
	 * @param filename - filesystem path to file
	 * @return boolean value describing if the header was able to be read
	 */

	public boolean readVhdHeader(String filename) {

		try {
			if (filename==null) return false;
			FileChannel fc=new FileInputStream(filename).getChannel();
			if (fc.size()==0) return false;
			this.filename=filename;
			FileChannelImageInputStream fciis=new FileChannelImageInputStream(fc);
			fciis.setByteOrder(ByteOrder.BIG_ENDIAN);

			boolean b = false;
			if (filename.contains(".rgb")){
			     b=readVhdHeader(fciis,".rgb");
			}else if(filename.contains(".raw")){
			     b=readVhdHeader(fciis,".raw");
			}else {
			     b=readVhdHeader(fciis,null);
			}
			fciis.close();
			return b;
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return false;
	}

	// =======================================================================

	/**
	 * Main method for reading Vhd Header information.  Sets image values depending
	 * on the extension of file selected for opening.  RAW and RGB files have no headers, requiring
	 * hard coded values for the image data.  CT and MR images read their values directly from the
	 * header information in the file. The code handles the varying header sizes found in CT and MR 
	 * data.
	 *
	 * @param fciis  input stream for the current file being read
	 * @param imageExtension  extension of current file if available
	 * @return boolean  value describing if the header was able to be read
	 */

         private boolean readVhdHeader(FileChannelImageInputStream fciis, String imageExtension) {

		try {

		        /* Visible Human Datasets of type RGB and RAW have no header.  So, their "header" information
		           is hard coded and returned.
			 */
		    	if (imageExtension == ".rgb"){
			    fileType = RGB;				// file type is interleaved RGB
			    width = 4096;				// image pixel width
			    height = 2700;				// image pixel height
			    bitsAllocated = 24; 			// bits per pixel	
			    description="Full-Body Unaligned 70mm RGB";	
			    patientID="Joe the Plummer";
			    expirationDate="Today 1/1/1";
			    examNumber=0;
			    fciis.seek(0);				// reset file position to beginning
			    return true;
			}else if(imageExtension == ".raw"){
			    fileType = RGB_PLANAR;			// file type is non-interleaved RGB (Planar RGB)
			    width = 2048;				// image pixel width
			    height = 1216;				// image pixel height
			    bitsAllocated = 24; 			// bits per pixel
			    description="Full-Body Raw Image";
			    patientID="Joe the Plummer";
			    expirationDate="Today 1/1/1";
			    examNumber=1;
			    fciis.seek(0);				// reset file position to beginning
			    return true;
			}else {

			// Start of the Vhd header_key struct for MRI and CT Vhd data

			// KYLE - Reading order for Vhd matches GE Genesis structure
			magic = fciis.readInt();			 // Magic Number
			headerSize = fciis.readInt();		         // Header Size
			width = fciis.readInt();			 // Image Width
			height = fciis.readInt();			 // Image Height
			int dataType=(int)fciis.readInt();               // Bits Allocated			

			bo=ByteOrder.BIG_ENDIAN;

			// KYLE - Switch statement has different structure from analyze
			switch (dataType) {
			    case 8: fileType=GRAY8; bitsAllocated=8; break;
			    case 16: fileType=GRAY16_SIGNED; bitsAllocated=16; break;
			    case 32: fileType=GRAY32_INT; bitsAllocated=32; break;
			    //case 16: fileType=GRAY32_FLOAT; bitsAllocated=32; break;
			    //case 128: fileType=RGB_PLANAR; bitsAllocated=24; break;
			    default: fileType=0;
			}

			int suiteHeader = 0;
			int examHeader = 0;
			int seriesHeader = 0;
			int imageHeader = 0;
			int userData = 0;
			int histogramHeader = 0;

			switch (headerSize) {
			    // Define MRI Header Section Starts
			    case 7900: suiteHeader = 2304; examHeader = 2418; seriesHeader = 3442; imageHeader = 4462; userData = 5484; histogramHeader=236; break;
			    // Define CT Header Section Starts
			    case 3416: suiteHeader = 236; examHeader = 350; seriesHeader = 1375; imageHeader = 2394; userData = 3416; break;
			    default: System.err.println("Invalid VHD Header Size");
			}

			// KYLE - Pull histogram related data (only MR has valid values)
			switch (headerSize) {
			    case 7900: fciis.seek(histogramHeader); fciis.readInt(); fciis.readFloat(); fciis.readShort(); calibrationMin = fciis.readShort(); calibrationMax = fciis.readShort(); break;
			    case 3416: calibrationMin = 0; calibrationMax = 0; break;
			    default: System.err.println("Invalid VHD Header Size");
			}

		        // KYLE - Find Specific header data
			fciis.seek(examHeader);                           // Start at exam header section
			String suiteID = readString(fciis,4);             // Suite ID
			fciis.readShort();                                // Skip Field
			fciis.readShort();                                // SkipField
			examNumber=fciis.readShort();                     // Exam Number
			fciis.seek(examHeader + 305);                     // Move ahead to known location
			String examType = readString(fciis,3);            // Exam Type
			fciis.seek(examHeader+84);                        // Move ahead to known location
			patientID = readString(fciis,13);                 // Patient ID
			String patientName = readString(fciis,25);        // Patient Name
			fciis.seek(examHeader+126);                       // Move ahead to known location
			int patientSexNum = fciis.readShort();            // Patient Sex
			String patientSex = "";
			switch(patientSexNum){                            // Convert patient sex from number to text
			    case 1: patientSex = "Male"; break;
			    case 2: patientSex = "Female"; break;
			    default: patientSex = "Unknown";
			}
			
			fciis.seek(seriesHeader+16);                      // Jump to series header section
			int dateTime=fciis.readInt();                     // Study Date
			String seriesDescription=readString(fciis,10);    // Series Descrtiption

			int lastNumImage = 0;
			switch (headerSize) {                             // Specific seek position based on MR or CT file
			    case 7900: fciis.seek(seriesHeader+200); lastNumImage = fciis.readInt(); break;
			    case 3416: fciis.seek(seriesHeader+199); lastNumImage = fciis.readInt(); break;
			    default: System.err.println("Invalid VHD Header Size");
			}

			// KYLE - MR specific values (CT does not have valid entries for these fields)
			int sliceGroupSize = 0;
		        switch (headerSize) {
			    case 7900: fciis.seek(imageHeader+398);sliceGroupSize=fciis.readShort(); fciis.seek(imageHeader+210); echoTotal=fciis.readShort(); echoNumber=fciis.readShort(); break;
			    case 3416: sliceGroupSize=0; echoTotal=0; echoNumber=0; break;
			    default: System.err.println("Invalid VHD Header Size");
			}

			fciis.seek(imageHeader+10);
			seriesNumber = fciis.readShort();
			int imageNumber = fciis.readShort();
			
			// KYLE - Description, PatientID, and expirationDate are required to open
			// a file currently.  Expiration date is still not set by header data and
			// is a nonsense string.
			switch (headerSize) {
			    case 7900: description="VHD-" + patientSex + "-" + examType + "-" + seriesDescription; break;
			    case 3416: description="VHD-" + patientSex + "-" + examType; break;
			    default: System.err.println("Invalid VHD Header Size");
			}

			expirationDate="Today 01/01/01";
			fciis.seek(headerSize);
			return true;
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return false;
	}

	// ======================================================================================
    	/**
	 * Reads in a specified byte length of a FileChannelImageInputStream and returns it as a String
	 *
	 * @param fciis - FileChannelImageInputStream to be read
	 * @param length - the length, in bytes, to read from fciis
	 * @return String - a length sized section of the FileChannelImageInputStream
	 */
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

	// ======================================================================================
    	/**
	 * Returns the number of bytes used per pixel based on current image filetype
	 *
	 * @return int - bytes per pixel
	 */
	public int getBytesPerPixel() {

		switch (fileType) {

		  case GRAY8: case COLOR8: case BITMAP: return 1;
		  case GRAY16_SIGNED: case GRAY16_UNSIGNED: return 2;
		  case GRAY32_INT: case GRAY32_UNSIGNED: case GRAY32_FLOAT: case ARGB: return 4;
		  case RGB: case RGB_PLANAR: case BGR: return 3;
		  default: return 0;
		}
	}

	// ======================================================================================
    	/**
	 * Returns a string based representation of the current image filetype
	 *
	 * @return String - image filetype
	 */
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

	// ======================================================================================
    	/**
	 * Returns a string based representation of the current instance of VhdHeader
	 *
	 * @return String - string description of VhdHeader
	 */
	public String toString() {return new String("name="+filename+", width="+width+", height="+height+", nImages="+nImages+
																							",type="+getType()+", offset="+offset+", whiteZero="+(whiteIsZero ? "t" : "f")+", byteOrder="+bo+
																							",lutSize="+lutSize);}

	// =======================================================================

	public static void main(String args[]) {

		if (args.length==1) {
			VhdHeader ah=new VhdHeader();
			ah.readVhdHeader(args[0]);
			System.err.println(ah);
		}
	}
}

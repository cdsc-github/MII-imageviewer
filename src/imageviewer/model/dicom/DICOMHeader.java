/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.sun.media.imageio.stream.FileChannelImageInputStream;
import com.sun.media.imageio.stream.FileChannelImageOutputStream;

// =======================================================================
/**
 * Object responsible for reading a DICOM header and storing that data in memory.  
 */

public class DICOMHeader implements DICOMTags {

	private static final int DICOM_FILE_OFFSET=128;
	private static final int DICOM_PIXEL_TAG=0x7fe00010;
	private static final int ITEM_DELIMETER=0xFFFEE00D;
	private static final int SEQUENCE_DELIMETER=0xFFFEE0DD;

	//private static final int ITEM=0xFFFEE000;
	//private static final int THUMBNAIL_IMAGE_SEQUENCE=0x00880200;

	private static final String DICOM_FILE_IDENTIFIER=new String("DICM");
	private static final NumberFormat NUM_FORMAT=NumberFormat.getInstance();

	private static final Logger LOG=Logger.getLogger("imageviewer.dicom");
	
	// =======================================================================

	private static final double MR_ECHO_MIN=1;
	private static final double MR_ECHO_MAX=60;
	private static final double MR_REP=1000;
	private static final double MR_INV=500;

	// =============== Values for DICOM header fields ======================
	
	String studyTime=new String("000000");    // Default values for 12:00 AM, if time not set
	String studyDate=new String("19700101");  // Default values for january 1, 1970, if date not set

	String patientName=null, patientID=null, patientSex=null, patientBirthdate=null, patientBirthTime=null;
	String patientPosition=null, patientOrientation=null;
	String SOPClassUID=null, SOPInstanceUID=null;
	String studyImageType=null, studyReferringPhysician=null;
	String studyAccessionNumber=null, studyInsuranceID=null, studyInstitution=null;
	String studyDescription=null;
	String seriesImageModality=null, seriesFormUID=null, seriesPositionID=null;
	String seriesManufacturer=null, seriesManufacturerModel=null, seriesStationName=null;
	String seriesDescription=null, seriesDate=null, seriesTime=null;
	String seriesContrast=null, seriesScanningSequence=null, seriesSequenceVariant=null;
	String seriesBodyPartExamined=null, seriesMRAcquisitionType=null;
	String seriesSequenceName=null, seriesAngioFlag=null;
	String imageInstance=null, acquisitionDate=null, acquisitionTime=null;
	String contentDate=null, contentTime=null;
	String transferSyntaxUID=null, studyInstanceUID=null, seriesInstanceUID=null;
	String photoInterpretation=null, correctedImage=null;
	String additionalPatientHistory=null;
	
	double[] imagePosition=null, imageOrientation=null, imagePixelSpacing=null;
	double[] windowCenter=null, windowWidth=null;

	byte[] redPalette=null, greenPalette=null, bluePalette=null;
	
	double imagePixelAspectRatio=0, imageSlicePosition=0, imageSliceThickness=0;
	double imageSliceSpacing=0, imageGantryTilt=0, imageTableHeight=0;
	double imageSliceLocation=0, imageRescaleIntercept=0, imageKVP=0;
	double imageRepetitionTime=0, imageEchoTime=0, imageFlipAngle=0;
	double imageRescaleSlope=1, imageEchoNumber=0, imageInversionTime=0;
	
	int imageBitsAllocated=0, imageBitsStored=0, imagePlanarConfiguration=0;
	int imageHighBit=0, imageSamplePerPixel=0, imageRows=0, imageColumns=0;
	int imageMaxPixel=0, imageMinPixel=0, seriesNumber=0, studyID=0;
	int acquisitionNumber=0, numberFrames=1, pixelPaddingValue=0;
	
	short imagePixelRepresentation=0;

	// =======================================================================

	boolean validDicomImageFlag=false;
	Hashtable<String,Object> lookupTable=new Hashtable<String,Object>();
	int pixelTagPosition=-1;

	/**
	 * Default constructor. 
	 */
	public DICOMHeader() {}

	// =======================================================================
		
	public String getAcquisitionDate() {return acquisitionDate;} 
	public String getAcquisitionTime() {return acquisitionTime;}
	public String getAdditionalPatientHistory() {return additionalPatientHistory;}
	public String getBodyPartExamined() {return seriesBodyPartExamined;}
	public String getContentDate() {return contentDate;}
	public String getContentTime() {return contentTime;}
	public String getCorrectedImage() {return correctedImage;}
	public String getImageInstance() {return imageInstance;}
	public String getPatientBirthdate() {return patientBirthdate;}
	public String getPatientBirthTime() {return patientBirthTime;}
	public String getPatientID() {return patientID;}
	public String getPatientName() {return patientName;}
	public String getPatientOrientation() {return patientOrientation;}
	public String getPatientPosition() {return patientPosition;}
	public String getPatientSex() {return patientSex;}
	public String getPhotoInterpretation() {return photoInterpretation;}
	public String getSOPClassUID() {return SOPClassUID;}
	public String getSOPInstanceUID() {return SOPInstanceUID;}
	public String getSeriesAngioFlag() {return seriesAngioFlag;}
	public String getSeriesContrast() {return seriesContrast;}
	public String getSeriesDate() {return seriesDate;}
	public String getSeriesDescription() {return seriesDescription;}
	public String getSeriesFormUID() {return seriesFormUID;}
	public String getSeriesImageModality() {return seriesImageModality;}
	public String getSeriesInstanceUID() {return seriesInstanceUID;}
	public String getSeriesMRAcquisitionType() {return seriesMRAcquisitionType;}
	public String getSeriesManufacturer() {return seriesManufacturer;}
	public String getSeriesManufacturerModel() {return seriesManufacturerModel;}
	public String getSeriesPositionID() {return seriesPositionID;}
	public String getSeriesScanningSequence() {return seriesScanningSequence;}
	public String getSeriesSequenceName() {return seriesSequenceName;}
	public String getSeriesSequenceVariant() {return seriesSequenceVariant;}
	public String getSeriesStationName() {return seriesStationName;}
	public String getSeriesTime() {return seriesTime;}
	public String getStudyAccessionNumber() {return studyAccessionNumber;}
	public String getStudyDate() {return studyDate;}
	public String getStudyDescription() {return studyDescription;}
	public String getStudyImageType() {return studyImageType;}
	public String getStudyInstanceUID() {return studyInstanceUID;}
	public String getStudyInstitution() {return studyInstitution;}
	public String getStudyInsuranceID() {return studyInsuranceID;}
	public String getStudyReferringPhysician() {return studyReferringPhysician;}
	public String getStudyTime() {return studyTime;}
	public String getTransferSyntaxUID() {return transferSyntaxUID;}

	public double[] getImageLevel() {return windowCenter;}
	public double[] getImageOrientation() {return imageOrientation;}
	public double[] getImagePixelSpacingArray() {return imagePixelSpacing;}
	public double[] getImagePosition() {return imagePosition;}
	public double[] getImageWindow() {return windowWidth;}
	public double[] getWindowCenter() {return windowCenter;}
	public double[] getWindowWidth() {return windowWidth;}
	
	/**
	 * Provides pixel spacing in string representation e.g. (x=1.0 mm, y=1.0 mm)
	 * 
	 * @return
	 */
	public String getImagePixelSpacing() {	
		if (imagePixelSpacing!=null) {
			if (imagePixelSpacing.length==2) {
				String pixelSpacing=new String("(x="+NUM_FORMAT.format(imagePixelSpacing[0])+" mm, y="+NUM_FORMAT.format(imagePixelSpacing[1])+" mm)");	
				return pixelSpacing;
			}
		}
		return new String();
	}
			
	public double getImageEchoNumber() {return imageEchoNumber;}
	public double getImageEchoTime() {return imageEchoTime;}
	public double getImageFlipAngle() {return imageFlipAngle;}
	public double getImageGantryTilt() {return imageGantryTilt;}
	public double getImageInversionTime() {return imageInversionTime;}
	public double getImageKVP() {return imageKVP;}
	public double getImageRepetitionTime() {return imageRepetitionTime;}
	public double getImageRescaleIntercept() {return imageRescaleIntercept;}
	public double getImageRescaleSlope() {return imageRescaleSlope;}
	public double getImageSlicePosition() {return imageSlicePosition;}
	public double getImageSliceSpacing() {return imageSliceSpacing;}
	public double getImageSliceThickness() {return imageSliceThickness;}
	public double getImageTableHeight() {return imageTableHeight;}
	public double getImagePixelAspectRatio() {return imagePixelAspectRatio;}

	public int getAcquisitionNumber() {return acquisitionNumber;}
	public int getImageBitsAllocated() {return imageBitsAllocated;}
	public int getImageBitsStored() {return imageBitsStored;}
	public int getImageColumns() {return imageColumns;}
	public int getImageHighBit() {return imageHighBit;}
	public int getImageRows() {return imageRows;}
	public int getImageSamplePerPixel() {return imageSamplePerPixel;}
	public int getNumberFrames() {return numberFrames;}
	public int getPixelPaddingValue() {return pixelPaddingValue;}
	public int getSeriesNumber() {return seriesNumber;}
	public int getStudyID() {return studyID;}

	public short getImagePixelRepresentation() {return imagePixelRepresentation;}
	
	public void setAcquisitionDate(String x) {acquisitionDate=x;} 
	public void setAcquisitionTime(String x) {acquisitionTime=x;}
	public void setAdditionalPatientHistory(String x) {additionalPatientHistory=x;}
	public void setBodyPartExamined(String x) {seriesBodyPartExamined=x;}
	public void setContentDate(String x) {contentDate=x;}
	public void setContentTime(String x) {contentTime=x;}
	public void setImageBitsAllocated(int x) {imageBitsAllocated=x;}
	public void setImageBitsStored(int x) {imageBitsStored=x;}
	public void setImageColumns(int x) {imageColumns=x;}
	public void setImageGantryTilt(double x) {imageGantryTilt=x;}
	public void setImageHighBit(int x) {imageHighBit=x;}
	public void setImageInstance(String x) {imageInstance=x;}
	public void setImageOrientation(double[] x) {imageOrientation=x;}
	public void setImagePixelAspectRatio(double x) {imagePixelAspectRatio=x;}
	public void setImagePixelRepresentation(short x) {imagePixelRepresentation=x;}
	public void setImagePixelSpacing(double[] x) {imagePixelSpacing=x;}
	public void setImagePosition(double[] x) {imagePosition=x;}
	public void setImageRescaleSlope(double x) {imageRescaleSlope=x;}
	public void setImageRows(int x) {imageRows=x;}
	public void setImageSamplePerPixel(int x) {imageSamplePerPixel=x;}
	public void setImageSlicePosition(double x) {imageSlicePosition=x;}
	public void setImageSliceSpacing(double x) {imageSliceSpacing=x;}
	public void setImageSliceThickness(double x) {imageSliceThickness=x;}
	public void setImageTableHeight(double x) {imageTableHeight=x;}
	public void setPatientBirthdate(String x) {patientBirthdate=x;}
	public void setPatientBirthTime(String x) {patientBirthTime=x;}
	public void setPatientID(String x) {patientID=x;}
	public void setPatientName(String x) {patientName=x;}
	public void setPatientOrientation(String x) {patientOrientation=x;}
	public void setPatientPosition(String x) {patientPosition=x;}
	public void setPatientSex(String x) {patientSex=x;}
	public void setPhotoInterpretation(String x) {photoInterpretation=x;}
	public void setPixelPaddingValue(int x) {pixelPaddingValue=x;}
	public void setSOPClassUID(String x) {SOPClassUID=x;}
	public void setSOPInstanceUID(String x) {SOPInstanceUID=x;}
	public void setSeriesAngioFlag(String x) {seriesAngioFlag=x;}
	public void setSeriesDate(String x) {seriesDate=x;}
	public void setSeriesDescription(String x) {seriesDescription=x;}
	public void setSeriesFormUID(String x) {seriesFormUID=x;}
	public void setSeriesImageModality(String x) {seriesImageModality=x;}
	public void setSeriesManufacturer(String x) {seriesManufacturer=x;}
	public void setSeriesManufacturerModel(String x) {seriesManufacturerModel=x;}
	public void setSeriesPositionID(String x) {seriesPositionID=x;}
	public void setSeriesStationName(String x) {seriesStationName=x;}
	public void setSeriesTime(String x) {seriesTime=x;}
	public void setStudyAccessionNumber(String x) {studyAccessionNumber=x;}
	public void setStudyDate(String x) {studyDate=x;}
	public void setStudyDescription(String x) {studyDescription=x;}
	public void setStudyImagetype(String x) {studyImageType=x;}
	public void setStudyInstitution(String x) {studyInstitution=x;}
	public void setStudyInsuranceID(String x) {studyInsuranceID=x;}
	public void setStudyReferringPhysician(String x) {studyReferringPhysician=x;}
	public void setStudyTime(String x) {studyTime=x;}

	// =======================================================================
	/* Lookup an object from the lookup table.  In most cases the result
	 * will be a string, but not necessarily, depending on the object
	 * type retrieved from the header.
	 *  
	 * @param key
	 * @return
	 */

	public Object doLookup(String key) {return lookupTable.get(key);}

	public Hashtable<String,Object> getProperties() {return lookupTable;}
	public void setProperty(String key, Object o) {lookupTable.put(key,o);}

	public int getPixelTagPosition() {return pixelTagPosition;}

	// =======================================================================
	// Strip a DICOM header of specified fields. Takes the input stream
	// and writes to the output stream the exact contents minus anything
	// that has been specified for removal.

	public boolean stripDICOMHeader(FileChannelImageInputStream fciis, FileChannelImageOutputStream fcios, ArrayList<Integer> tags, String hint) {

		try {
			byte[] skip=new byte[DICOM_FILE_OFFSET];
			fciis.readFully(skip); 
			fcios.write(skip);

			byte[] buf=new byte[DICOM_FILE_IDENTIFIER.length()];
			fciis.readFully(buf);
			String headerID=new String(buf);
			if (!headerID.equals(DICOM_FILE_IDENTIFIER)) return false;
			fcios.write(buf);

			ByteOrder bo=fciis.getByteOrder();
			byte[] b=new byte[4];
			boolean oddLocation=false;
			boolean sequenceFlag=false;
			int length=0;

			while (true) {
	
				int group=fciis.readUnsignedShort();	
				int element=fciis.readUnsignedShort();
				int tag=(group << 16) | element;
				fcios.writeChar(group);
				fcios.writeChar(element);

				// We cannot know whether the VR is implicit or explicit
				// without the full DICOM Data Dictionary for public and
				// private groups.  Assume the VR is explicit if the two bytes
				// match the known codes. It is possible that these two bytes
				// are part of a 32-bit length for an implicit VR.

				if ((fciis.getStreamPosition() & 1)!=0) oddLocation=true; 				
				fciis.readFully(b);
				fcios.write(b);

				int vr=(b[0]<<8)+b[1];
				length=0;
				
				switch (vr) {
				
				  case OB: case OW: case SQ: case UN: 

						if ((b[2]==0)||(b[3]==0)) {                                                                                            // Explicit VR with 32-bit length 
							length=(int)fciis.readUnsignedInt();                                                                                 // but if other two bytes are zero
							fcios.writeInt(length);
						} else {                                                                                                               // Implicit VR with 32-bit length
							length=(bo==ByteOrder.LITTLE_ENDIAN) ? (((b[3] & 0xff)<<24)+((b[2] & 0xff)<<16)+((b[1] & 0xff)<<8)+(b[0] & 0xff)) :
								                                     (((b[0] & 0xff)<<24)+((b[1] & 0xff)<<16)+((b[2] & 0xff)<<8)+(b[3] & 0xff));
						}
						break;
					
				  case AE: case AS: case AT: case CS: 
				  case DA: case DS: case DT: case FD:
				  case FL: case IS: case LO: case LT: 
			 	  case PN: case SH: case SL: case SS:
				  case ST: case TM: case UI: case UL: 
				  case US: case UT: case QQ: 

						length=(bo==ByteOrder.LITTLE_ENDIAN) ? (((b[3] & 0xff)<<8)+(b[2] & 0xff)) : (((b[2] & 0xff)<<8)+(b[3] & 0xff));        // Explicit vr with 16-bit length
						break;
					
				  default: 

						length=(bo==ByteOrder.LITTLE_ENDIAN) ? (((b[3] & 0xff)<<24)+((b[2] & 0xff)<<16)+((b[1] & 0xff)<<8)+(b[0] & 0xff)) :    // Implicit vr with 32-bit length
							                                     (((b[0] & 0xff)<<24)+((b[1] & 0xff)<<16)+((b[2] & 0xff)<<8)+(b[3] & 0xff)); 
				}
				
				// Hack needed to read some GE files.  The element length must
				// be even! Also, handle undefined element lengths (-1) by
				// setting the length to be 0.  Also handle images (e.g.,
				// GEIIS format) that embed thumbnails and re-use other DICOM
				// header tags; a bit of a hack for now by detecting a
				// sequence; check to see if the tag marks the end of a
				// sequence.  When the pixel data is encountered, stop.
			
				if ((length==13) && (!oddLocation)) length=10; 
				if (length==-1) {length=0; sequenceFlag=true;}
				if (tag==ITEM_DELIMETER || tag==SEQUENCE_DELIMETER) sequenceFlag=false;
				if (sequenceFlag) {
					byte[] tmpBuffer=new byte[length];
					fciis.readFully(tmpBuffer); 
					fcios.write(tmpBuffer);
					continue;
				}
				if (tag==DICOM_PIXEL_TAG) break;
				if (length<0) return false;

				byte[] dataBuffer=new byte[length];
				fciis.readFully(dataBuffer);
				if (tags.contains(tag)) {
					String s1=Integer.toHexString(tag);
					while (s1.length()<8) s1="0"+s1;
					LOG.info("Found tag 0x"+s1+" ["+DICOMTagMap.doLabelLookup(tag)+"]");
					dataBuffer=new byte[length];
				}
				if (hint!=null) {
					String tmpBuffer=new String(dataBuffer);
					tmpBuffer=tmpBuffer.toLowerCase();
					if (tmpBuffer.contains(hint.toLowerCase())) {
						String s1=Integer.toHexString(tag);
						while (s1.length()<8) s1="0"+s1;
						LOG.info("Found hint in tag 0x"+s1+" ["+DICOMTagMap.doLabelLookup(tag)+"]");
						dataBuffer=new byte[length];
					}
				}
				fcios.write(dataBuffer);
			}

		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
		return true;
	}

	// =======================================================================
	/* Reads our DICOMHeader from a FileChannelImageInputStream.  
	 * 
	 * @param fciis - file channel stream
	 * @return - true if file is valid DICOM 
	 */

	public boolean readDICOMHeader(FileChannelImageInputStream fciis) {

		try {

			byte[] buf=new byte[DICOM_FILE_IDENTIFIER.length()];
			fciis.skipBytes(DICOM_FILE_OFFSET);
			fciis.readFully(buf);
			String headerID=new String(buf);
			if (!headerID.equals(DICOM_FILE_IDENTIFIER)) return false;

			byte[] b=new byte[4];
			boolean oddLocation=false;
			//boolean checkByteOrderTag=true;
			boolean sequenceFlag=false;

			int length=0;
			ByteOrder bo=fciis.getByteOrder();

			while (true) {

				int group=fciis.readUnsignedShort();	
				int element=fciis.readUnsignedShort();
				int tag=(group << 16) | element;

				if ((fciis.getStreamPosition() & 1)!=0) oddLocation=true; 
				fciis.readFully(b);

				// We cannot know whether the VR is implicit or explicit
				// without the full DICOM Data Dictionary for public and
				// private groups.  Assume the VR is explicit if the two bytes
				// match the known codes. It is possible that these two bytes
				// are part of a 32-bit length for an implicit VR.
				
				int vr=(b[0]<<8)+b[1];
				length=0;
				
				switch (vr) {
				
				  case OB: case OW: case SQ: case UN: 

						if ((b[2]==0)||(b[3]==0)) {                                                                                            // Explicit VR with 32-bit length 
							length=(int)fciis.readUnsignedInt();                                                                                 // but if other two bytes are zero
						} else {                                                                                                               // Implicit VR with 32-bit length
							length=(bo==ByteOrder.LITTLE_ENDIAN) ? (((b[3] & 0xff)<<24)+((b[2] & 0xff)<<16)+((b[1] & 0xff)<<8)+(b[0] & 0xff)) :
								                                     (((b[0] & 0xff)<<24)+((b[1] & 0xff)<<16)+((b[2] & 0xff)<<8)+(b[3] & 0xff));
						}
						break;
					
				  case AE: case AS: case AT: case CS: 
				  case DA: case DS: case DT: case FD:
				  case FL: case IS: case LO: case LT: 
			 	  case PN: case SH: case SL: case SS:
				  case ST: case TM: case UI: case UL: 
				  case US: case UT: case QQ: 

						length=(bo==ByteOrder.LITTLE_ENDIAN) ? (((b[3] & 0xff)<<8)+(b[2] & 0xff)) : (((b[2] & 0xff)<<8)+(b[3] & 0xff));        // Explicit vr with 16-bit length
						break;
					
				  default: 

						length=(bo==ByteOrder.LITTLE_ENDIAN) ? (((b[3] & 0xff)<<24)+((b[2] & 0xff)<<16)+((b[1] & 0xff)<<8)+(b[0] & 0xff)) :    // Implicit vr with 32-bit length
							                                     (((b[0] & 0xff)<<24)+((b[1] & 0xff)<<16)+((b[2] & 0xff)<<8)+(b[3] & 0xff)); 
				}
				
				// Hack needed to read some GE files.  The element length must
				// be even! Also, handle undefined element lengths (-1) by
				// setting the length to be 0.  Also handle images (e.g.,
				// GEIIS format) that embed thumbnails and re-use other DICOM
				// header tags; a bit of a hack for now by detecting a
				// sequence; check to see if the tag marks the end of a
				// sequence.  When the pixel data is encountered, stop.
			
				if ((length==13) && (!oddLocation)) length=10; 
				if (length==-1) {length=0; sequenceFlag=true;}
				if (tag==ITEM_DELIMETER || tag==SEQUENCE_DELIMETER) sequenceFlag=false;
				if (sequenceFlag) {fciis.skipBytes(length); continue;}
				if (tag==DICOM_PIXEL_TAG) {validDicomImageFlag=true; pixelTagPosition=length; break;}
				if (length<0) {validDicomImageFlag=false; return false;}

				byte[] dataBuffer=new byte[length];
				fciis.readFully(dataBuffer);
				retrieveHeaderData(fciis,tag,dataBuffer);
			}
			
		} catch (EOFException eof) {
			LOG.warn("Invalid DICOM header, EOF reached unexpectedly.");
			return false;
		} catch (Exception exc) {
			LOG.warn("Error trying to parse DICOM header.");
			return false;
		}
		return validDicomImageFlag;
	}

	// =======================================================================
	
	private short parseShort(byte[] buf) {return (short)((buf[0] & 0xff) | ((buf[1] & 0xff) << 8));}

	private byte[] parseLUTArray(byte[] buf) {

		int length=buf.length/2;
		byte[] lut=new byte[length];
		for (int i=0; i<length; i+=2) {
			byte b0=buf[i];
			byte b1=buf[i+1];
			lut[i]=(byte)((b1<<8)+b0);
		}
		return lut;
	}
	
	private double parseDouble(byte[] buf) {String s=new String(buf); return (new Double(s)).doubleValue();}
	
	private double[] parseDoubleArray(byte[] buf) {
		
		String s=new String(buf);
		
		// Count the number of "\" that occur in the given string.  Each
		// double number is delineated by a slash.  The number of objects
		// will be used to allocate the double array size.
		
		int count=1, pos, index=0;
		for (int loop=0; loop<s.length(); loop++) if (s.charAt(loop)=='\\') count++;
		
		double[] darray=new double[count];
		
		while ((pos=s.indexOf("\\"))>=0) {
			String sub=s.substring(0,pos);
			darray[index++]=(new Double(sub)).doubleValue();
			s=s.substring(pos+1);
		}
		
		if (s.length()>0) darray[index++]=(new Double(s)).doubleValue();
		
		return darray;
	}
	
	// =======================================================================

	/**
	 * Determines from image orientation data if the image is OBLIQUE, SAGITTAL, AXIAL or CORONAL.
	 * 
	 * @return
	 */
	public int getSliceOrientation() {

		double[] direction=imageOrientation;
		if (direction.length<6) return OBLIQUE;
		if (((direction[0]>=-0.34)&&(direction[0]<=0.34))&&(direction[1]>=0.94)&&((direction[2]>=-0.34)&&(direction[2]<=0.34))&&
				((direction[3]>=-0.34)&&(direction[3]<=0.34))&&((direction[4]>=-0.34)&&(direction[4]<=0.34))&&(direction[5]<=-0.94)) return SAGITTAL;
		if ((direction[0]>=+0.94)&&((direction[1]>=-0.34)&&(direction[1]<=0.34))&&((direction[2]>=-0.34)&&(direction[2]<=0.34))&&
				((direction[3]>=-0.34)&&(direction[3]<=0.34))&&(direction[4]>=0.94)&&((direction[5]>=-0.34)&&(direction[5]<=0.34))) return AXIAL;
		if ((direction[0]>=+0.94)&&((direction[1]>=-0.34)&&(direction[1]<=0.34))&&((direction[2]>=-0.34)&&(direction[2]<=0.34))&&
				((direction[3]>=-0.34)&&(direction[3]<=0.34))&&((direction[4]>=-0.34)&&(direction[4]<=0.34))&&(direction[5]<=-0.94)) return CORONAL;
		return OBLIQUE;
	}

	// =======================================================================
	/**
	 * Tries to determine the imaging plane for the series.  The
	 * imageOrientation parameter in a DICOM header should give us this
	 * information; six numbers are given, representing an X/Y/Z axis
	 * coordinate system; each three-tuple specifies a single plane (x,
	 * y, or z).  Typically, the z-plane represents the length of the
	 * patient, the y-plane the depth of the patient (front to back),
	 * and the x-plane the width of the patient (left to right).  For
	 * instance, axial is therefore in the x/y plane.
	 *
	 * Note that some of the values may not be exactly 1 (or -1).
	 *
	 * @return computed imaging plane for series
	 */
	public int computePlane() {

		double[] imageOrientation=getImageOrientation();
		if (imageOrientation!=null) {
			if (imageOrientation.length==6) {
				if ((Math.abs(Math.round(imageOrientation[0]))==1)&&(Math.abs(Math.round(imageOrientation[4]))==1)) return (AXIAL);
				if ((Math.abs(Math.round(imageOrientation[1]))==1)&&(Math.abs(Math.round(imageOrientation[3]))==1)) return (AXIAL);
				if ((Math.abs(Math.round(imageOrientation[0]))==1)&&(Math.abs(Math.round(imageOrientation[5]))==1)) return (CORONAL);
				if ((Math.abs(Math.round(imageOrientation[2]))==1)&&(Math.abs(Math.round(imageOrientation[3]))==1)) return (CORONAL);
				return (SAGITTAL);
			}
		}
		return (AXIAL);
	}

	// =======================================================================

	/**
	 * MR weighting is returned on the basis of if the scanning sequence is SE, IR or RM.  
	 * SE weighting may be T1, T2, PD or UKNOWN.  IR or RM weighting may be STIR or FLAIR.
	 * You can test these via the public static final constants.  
	 * 
	 * @return
	 */
	public int getMRWeighting() {

		if (seriesScanningSequence.compareToIgnoreCase("SE")==0) return getSEWeighting(imageEchoTime,imageRepetitionTime);
		if (seriesScanningSequence.compareToIgnoreCase("IR")==0) return getIRWeighting(imageInversionTime);
		if (seriesScanningSequence.compareToIgnoreCase("RM")==0) return getRMWeighting(imageEchoTime,imageRepetitionTime,imageInversionTime);
		return UNKNOWN_WEIGHTING;
	}

	private int getSEWeighting(double echoTime, double repTime) {

		if ((echoTime>MR_ECHO_MIN)&&(echoTime<MR_ECHO_MAX)&&(repTime<=MR_REP)) return T1;
		if ((echoTime>MR_ECHO_MIN)&&(echoTime<MR_ECHO_MAX)&&(repTime>MR_REP)) return PD;
		if ((echoTime>MR_ECHO_MAX)&&(repTime>MR_REP)) return T2;
		return UNKNOWN_WEIGHTING;
	}

	private int getIRWeighting(double inversionTime) {return (inversionTime>MR_INV) ? STIR : FLAIR;}
	private int getRMWeighting(double echoTime, double repTime, double invTime) {return (invTime>0) ? getSEWeighting(echoTime,repTime) : getIRWeighting(invTime);}

	// =======================================================================
	
	private boolean retrieveHeaderData(FileChannelImageInputStream fciis, int tag, byte[] buf) throws IOException {
		
		// Given the information in the buffer and the group tag, set the
		// corresponding DICOM header information.  Group tags are set out
		// by the DICOM ACR NEMA standard, and can be found in the data
		// dictionary (Part 6).  Extend accordingly.
		
		if (buf.length==0) return false;
		String bufString=new String(buf);
		bufString=bufString.trim();
		switch (tag) {
		
		  case 0x00080008: studyImageType=bufString; lookupTable.put(STUDY_IMAGE_TYPE,studyImageType); break;                               // Image type
		  case 0x00080016: SOPClassUID=bufString; lookupTable.put(SOP_CLASS_UID,SOPClassUID); break;                                        // SOP Class
		  case 0x00080018: SOPInstanceUID=bufString; lookupTable.put(SOP_INSTANCE_UID,SOPInstanceUID); break;                               // SOP ID
		  case 0x00080020: studyDate=bufString; lookupTable.put(STUDY_DATE,studyDate); break;                                               // Study date
		  case 0x00080021: seriesDate=bufString; lookupTable.put(SERIES_DATE,seriesDate); break;                                            // Series date
		  case 0x00080022: acquisitionDate=bufString; lookupTable.put(ACQUISITION_DATE,acquisitionDate); break;
		  case 0x00080023: contentDate=bufString; lookupTable.put(CONTENT_DATE,contentDate); break;   
		  case 0x00080030: studyTime=bufString; lookupTable.put(STUDY_TIME,studyTime); break;                                               // Study time
		  case 0x00080032: acquisitionTime=bufString; lookupTable.put(ACQUISITION_TIME,acquisitionTime); break; 
		  case 0x00080033: contentTime=bufString; lookupTable.put(CONTENT_TIME,contentTime); break; 
		  case 0x00080050: studyAccessionNumber=bufString.trim(); 
				               lookupTable.put(STUDY_ACCESSION_NUMBER,studyAccessionNumber); break; 																	          // Accession Number		  
		  case 0x00080060: seriesImageModality=bufString; lookupTable.put(SERIES_IMAGE_MODALITY,seriesImageModality); break;                // Imaging modality
		  case 0x00080070: seriesManufacturer=bufString; lookupTable.put(SERIES_MANUFACTURER,seriesManufacturer); break;                    // Manufacturer
		  case 0x00080080: studyInstitution=bufString; lookupTable.put(STUDY_INSTITUTION,studyInstitution); break;                          // Institution
		  case 0x00080090: studyReferringPhysician=bufString;  lookupTable.put(STUDY_REFERRING_PHYSICIAN,studyReferringPhysician);  break; 	// Referring Physician
		  case 0x00081010: seriesStationName=bufString; lookupTable.put(SERIES_STATION_NAME,seriesStationName); break;                      // Series station name
		  case 0x00081030: studyDescription=bufString; lookupTable.put(STUDY_DESCRIPTION,studyDescription); break;                          // Study station name
		  case 0x0008103E: seriesDescription=bufString; lookupTable.put(SERIES_DESCRIPTION,seriesDescription); break;                       // Series description
		  case 0x00081090: seriesManufacturerModel=bufString; lookupTable.put(SERIES_MANUFACTURER_MODEL,seriesManufacturerModel); break;    // Manufacturer model
		  case 0x00100010: patientName=bufString; lookupTable.put(PATIENT_NAME,patientName); break;                                         // Patient name
		  case 0x00100020: patientID=bufString; lookupTable.put(PATIENT_ID,patientID); break;                                               // Patient ID
		  case 0x00100030: patientBirthdate=bufString; lookupTable.put(BIRTH_DATE,patientBirthdate); break;                                 // Patient b-day
		  case 0x00100040: patientSex=bufString; lookupTable.put(PATIENT_SEX,patientSex); break;                                            // Patient gender
		  case 0x00180010: seriesContrast=bufString; lookupTable.put(SERIES_CONTRAST,seriesContrast); break;                                // Series contrast
		  case 0x00180015: seriesBodyPartExamined=bufString; lookupTable.put(SERIES_BODY_PART_EXAMINED,seriesBodyPartExamined); break;      // Body part
		  case 0x00180020: seriesScanningSequence=bufString; lookupTable.put(SERIES_SCANNING_SEQUENCE,seriesScanningSequence);break;        // Scanning sequence
		  case 0x00180021: seriesSequenceVariant=bufString; lookupTable.put(SERIES_SEQUENCE_VARIANT,seriesSequenceVariant); break;          // Sequence variant
		  case 0x00180023: seriesMRAcquisitionType=bufString; lookupTable.put(SERIES_MR_ACQUISITION_TYPE, seriesMRAcquisitionType); break;  // MR acquisition type
		  case 0x00180024: seriesSequenceName=bufString; lookupTable.put(SERIES_SEQUENCE_NAME,seriesSequenceName); break;                   // Sequence name
		  case 0x00180025: seriesAngioFlag=bufString; lookupTable.put(SERIES_ANGIO_FLAG,seriesAngioFlag); break;                            // Angio flag
		  case 0x00180050: imageSliceThickness=parseDouble(buf); lookupTable.put(IMAGE_SLICE_THICKNESS,imageSliceThickness); break;         // Slice thickness
		  case 0x00180060: imageKVP=parseDouble(buf); lookupTable.put(IMAGE_KVP,imageKVP); break;                                           // KVP
		  case 0x00180080: imageRepetitionTime=parseDouble(buf);lookupTable.put(IMAGE_REPITITION_TIME,imageRepetitionTime); break;          // Repetition time
		  case 0x00180081: imageEchoTime=parseDouble(buf); lookupTable.put(IMAGE_ECHO_TIME,imageEchoTime); break;                           // Echo time
		  case 0x00180082: imageInversionTime=parseDouble(buf); lookupTable.put(IMAGE_INVERSION_TIME,imageInversionTime); break;            // Inversion time
		  case 0x00180086: imageEchoNumber=parseDouble(buf); lookupTable.put(IMAGE_ECHO_NUMBER,imageEchoNumber); break;                     // Echo number
		  case 0x00180088: imageSliceSpacing=parseDouble(buf); lookupTable.put(IMAGE_SLICE_SPACING,imageSliceSpacing); break;               // Slice spacing
		  case 0x00181120: imageGantryTilt=parseDouble(buf);  lookupTable.put(IMAGE_GANTRY_TILT,imageGantryTilt); break;                    // Gantry tilt angle
		  case 0x00181130: imageTableHeight=parseDouble(buf);  lookupTable.put(IMAGE_TABLE_HEIGHT,imageTableHeight); break;                 // Table height
		  case 0x00181314: imageFlipAngle=parseDouble(buf);  lookupTable.put(IMAGE_FLIP_ANGLE,imageTableHeight); break;                     // Flip angle
		  case 0x00185100: patientPosition=bufString; lookupTable.put(PATIENT_POSITION,patientPosition); break;                             // Patient position
		  case 0x0020000D: studyInstanceUID=bufString; lookupTable.put(STUDY_INSTANCE_UID,studyInstanceUID); break;                         // SOP ID (study)
		  case 0x0020000E: seriesInstanceUID=bufString; lookupTable.put(SERIES_INSTANCE_UID,seriesInstanceUID); break;                      // SOP ID (series)
		  case 0x00200010: studyID=(int)parseShort(buf); lookupTable.put(STUDY_ID,Integer.toString(studyID));break;                         // Study ID
		  case 0x00200011: seriesNumber=(int)parseShort(buf); lookupTable.put(SERIES_NUMBER,seriesNumber); break;                           // Series number
		  case 0x00200012: acquisitionNumber=(int)parseShort(buf); lookupTable.put(ACQUISITION_NUMBER,acquisitionNumber); break;            // Acquisition number
		  case 0x00200013: imageInstance=bufString; lookupTable.put(IMAGE_INSTANCE_NUMBER,imageInstance); break;                            // Image instance number
		  case 0x00200020: patientOrientation=bufString; lookupTable.put(PATIENT_ORIENTATION,patientOrientation); break;                    // Patient orientation
		  case 0x00200032: imagePosition=parseDoubleArray(buf); lookupTable.put(IMAGE_POSITION,imagePosition); break;                       // Image position
		  case 0x00200034: imagePixelAspectRatio=parseDouble(buf); lookupTable.put(IMAGE_PIXEL_ASPECT_RATIO,imagePixelAspectRatio); break;  // Pixel aspect ratios
		  case 0x00200037: imageOrientation=parseDoubleArray(buf); lookupTable.put(IMAGE_ORIENTATION,imageOrientation); break;              // Image orientation cosine
		  case 0x00201041: imageSlicePosition=parseDouble(buf); lookupTable.put(IMAGE_SLICE_POSITION,imageSlicePosition); break;            // Slice position
		  case 0x00280002: imageSamplePerPixel=(int)parseShort(buf); lookupTable.put(IMAGE_SAMPLE_PER_PIXEL,imageSamplePerPixel); break;    // Samples per pixel
		  case 0x00280004: photoInterpretation=bufString; lookupTable.put(PHOTO_INTERPRETATION, photoInterpretation); break;                // Photometric interpretation
		  case 0x00280006: imagePlanarConfiguration=parseShort(buf); lookupTable.put(PLANAR_CONFIGURATION,imagePlanarConfiguration); break; // Planar configuration
		  case 0x00280008: numberFrames=(int)parseDouble(buf); lookupTable.put(NUMBER_FRAMES, numberFrames); break;                         // Number of images in file
		  case 0x00280010: imageRows=(int)parseShort(buf); lookupTable.put(IMAGE_ROWS,imageRows); break;                                    // Image rows
		  case 0x00280011: imageColumns=(int)parseShort(buf); lookupTable.put(IMAGE_COLUMNS,imageColumns); break;                           // Image columns
		  case 0x00280030: imagePixelSpacing=parseDoubleArray(buf); lookupTable.put(IMAGE_PIXEL_SPACING,imagePixelSpacing); break;          // Pixel spacings
		  case 0x00280051: correctedImage=bufString; lookupTable.put(CORRECTED_IMAGE, correctedImage); break;                               // Image correction values
		  case 0x00280100: imageBitsAllocated=(int)parseShort(buf); lookupTable.put(IMAGE_BITS_ALLOCATED,imageBitsAllocated); break;        // Bits allocated
		  case 0x00280101: imageBitsStored=(int)parseShort(buf); lookupTable.put(IMAGE_BITS_STORED,imageBitsStored); break;                 // Bits stored
		  case 0x00280102: imageHighBit=(int)parseShort(buf); lookupTable.put(IMAGE_HIGH_BIT,imageHighBit); break;                          // High bit
		  case 0x00280103: imagePixelRepresentation=parseShort(buf); 
				               lookupTable.put(IMAGE_PIXEL_REPRESENTATION,imagePixelRepresentation); break;                                     // Pixel representation
		  case 0x00280106: imageMinPixel=(int)parseShort(buf); lookupTable.put(IMAGE_MIN_PIXEL,imageMinPixel); break;                       // Smallest pixel value
		  case 0x00280107: imageMaxPixel=(int)parseShort(buf); lookupTable.put(IMAGE_MAX_PIXEL, imageMaxPixel); break;                      // Maximum pixel value
		  case 0x00281050: windowCenter=parseDoubleArray(buf); lookupTable.put(WINDOW_CENTER, windowCenter); break;                         // Window center=level 
		  case 0x00281051: windowWidth=parseDoubleArray(buf); lookupTable.put(WINDOW_WIDTH, windowWidth); break;                            // Window width=window
		  case 0x00281052: imageRescaleIntercept=parseDouble(buf); lookupTable.put(IMAGE_RESCALE_INTERCEPT, imageRescaleIntercept); break;  // Rescale intercept 
		  case 0x00281053: imageRescaleSlope=parseDouble(buf); lookupTable.put(IMAGE_RESCALE_SLOPE, imageRescaleSlope); break;
		  case 0x00281201: redPalette=parseLUTArray(buf); lookupTable.put(RED_PALETTE, redPalette); break;                                  // Red palette color descriptor
		  case 0x00281202: greenPalette=parseLUTArray(buf); lookupTable.put(GREEN_PALETTE, greenPalette); break;                            // Green palette color descriptor
		  case 0x00281203: bluePalette=parseLUTArray(buf); lookupTable.put(BLUE_PALETTE, bluePalette); break;                               // Blue palette color descriptor
	  	case 0x00080031: seriesTime=bufString; lookupTable.put(SERIES_TIME,seriesTime); break;                                            // Series time

		  case 0x001021B0: additionalPatientHistory=bufString; lookupTable.put(ADDITIONAL_PATIENT_HISTORY,additionalPatientHistory); break;
		  case 0x00100032: patientBirthTime=bufString; lookupTable.put(PATIENT_BIRTH_TIME,patientBirthTime); break;
		  case 0x00280120: pixelPaddingValue=(int)parseShort(buf); lookupTable.put(PIXEL_PADDING_VALUE,pixelPaddingValue); break;      

		  case 0x00020010: transferSyntaxUID=bufString; lookupTable.put(STUDY_TRANSFER_SYNTAX,transferSyntaxUID);                           // Transfer syntax UID
				               if ((transferSyntaxUID.indexOf("1.2.4")>-1)||
													 (transferSyntaxUID.indexOf("1.2.5")>-1)) {
												 throw new IOException("Cannot opened compressed DICOM image: "+transferSyntaxUID);
											 } else if (transferSyntaxUID.trim().equals("1.2.840.10008.1.2")) {                                               // Little endian implicit
												 fciis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
											 } else if (transferSyntaxUID.trim().equals("1.2.840.10008.1.2.1")) {                                             // Little endian explicit
												 fciis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
											 } else if (transferSyntaxUID.trim().equals("1.2.840.10008.1.2.2")) {                                             // Big endian explicit
												 fciis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
											 }
											 break;
		          default: String label=DICOMTagMap.doLabelLookup(new Integer(tag));
								       if (label==null) return false;
											 lookupTable.put(label,new String(bufString));
		}
		return true;
	}
}


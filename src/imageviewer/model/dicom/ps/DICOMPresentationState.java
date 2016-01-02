/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
 package imageviewer.model.dicom.ps;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sun.media.imageio.stream.FileChannelImageInputStream;

import imageviewer.model.PresentationState;
import imageviewer.model.dicom.DICOMTags;
import imageviewer.model.dicom.parser.DcmHandler;
import imageviewer.model.dicom.parser.DcmParser;
import imageviewer.model.dicom.parser.DcmParserImpl;
import imageviewer.model.dicom.parser.DefaultDcmHandler;
import imageviewer.model.dicom.parser.FileFormat;
import imageviewer.model.dicom.parser.Tags;
import imageviewer.model.dicom.ps.GraphicObject.AnnotationUnitType;
import imageviewer.model.dicom.ps.GraphicObject.GraphicType;

import imageviewer.ui.annotation.Annotation;
import imageviewer.util.StringUtilities;

// =======================================================================

public class DICOMPresentationState extends DefaultDcmHandler implements DcmHandler, PresentationState, DICOMTags {

	private static final char[] HEX_DIGIT={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("yyyyMMdd HHmmss");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	private static final SimpleDateFormat DATE_FORMAT3=new SimpleDateFormat("M/dd/yy");
	private static final SimpleDateFormat DATE_FORMAT4=new SimpleDateFormat("MMMM d, yyyy h:mm a");
	private static final SimpleDateFormat DATE_FORMAT5=new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat DATE_FORMAT6=new SimpleDateFormat("M/dd/yy h:mm a");

	// =======================================================================
	// Internal data fields

	private transient Logger LOG=Logger.getLogger(DICOMPresentationState.class);

	private DcmParser parser=null;
	private HashMap<Integer,Object> lookup=null;
	private HashMap<Long,String> sequenceLoc=null;

	private ArrayList<GraphicObject> graphicobjects=null;
	private ArrayList<TextObject> textobjects=null;
	private Set<GraphicLayer> graphicLayers=null;

	private int currentTag, currentVr;
	private long currentPos;

	private boolean inGraphicObjectSequence=false;       
	private boolean inTextObjectSequence=false;

	private transient ArrayList<Annotation> cachedAnnotations=null;

	// =======================================================================
	// Publicly available data fields

	private GraphicObject currentGraphicObject=null;
	private TextObject currentTextObject=null;
	private GraphicLayer currentGraphicLayer=null;

	private String accessionNumber=null;
	private String additionalPatientHistory=null;
	private String admittingDiagnosesCodeSeq=null;
	private String admittingDiagnosesDescription=null;
	private String deviceSerialNumber=null;
	private String ethnicGroup=null;
	private String imageHorizontalFlip=null; 
	private String instanceNumber=null;                 // Specific to the DICOM PS
	private String institutionAddress=null;
	private String institutionDepartmentName=null;
	private String institutionName=null;
	private String lastCalibrationDate=null;
	private String lastCalibrationTime=null;
	private String manufacturer=null;
	private String manufacturerModel=null;
	private String occupation=null;
	private String otherPatientIDs=null;
	private String otherPatientNames=null;
	private String patientAge=null;
	private String patientBirthDate=null;
	private String patientBirthTime=null;
	private String patientComments=null;
	private String patientID=null;
	private String patientName=null;
	private String patientSex=null;
	private String patientSize=null;
	private String patientWeight=null;
	private String physicianOfRecord=null;
	private String presentationCreationDate=null; 
	private String presentationCreationTime=null; 
	private String presentationCreatorName=null;
	private String presentationDescription=null;
	private String presentationLUTShape=null;            // enum IDENTITY, INVERSE   id - no further xform necessary, inverse - output vals after inversion are p-values displayed area module 
	private String presentationLabel=null;
	private String presentationSizeMode=null;            // enum SCALE TO FIT, TRUE SIZE, MAGNIFY 
	private String procedureCodeSequence=null;
	private String referencedFrameNumber=null;           // frame numbers within ref sop instance, if ref image is multi-frame img (delimiter?)
	private String referencedSOPClassUID=null; 
	private String referencedSOPInstanceUID=null;
	private String referencedStudySequence=null;
	private String referringPhysicianName=null;
	private String rescaleType=null;                     // enum OD, HU, US = optical density, hounsfield units, unspecified
	private String seriesDate=null;
	private String seriesDescription=null;
	private String seriesInstanceUID=null;  
	private String seriesModality=null;
	private String seriesTime=null;
	private String softwareVersion=null;
	private String spatialResolution=null;
	private String stationName=null;
	private String studyDate=null;
	private String studyDescription=null;
	private String studyID=null;
	private String studyInstanceUID=null; 
	private String studyTime=null;

	private String[] presentationPixelAspectRatio=null;
	private String[] presentationPixelSpacing=null;      // (DS) - numeric pair - adjacent row spacing (delimiter) adjacent column spacing in mm

	private double rescaleIntercept; 
	private double rescaleSlope;  
	private double windowCenter;  
	private double windowLevel; 

	private short[] displayedAreaBottomRightHandCorner;  // DICOM (SL) column/row offset after spatial xform
	private short[] displayedAreaTopLeftHandCorner;      // DICOM (SL) column/row offset after spatial xform

	private float presentationPixelMagnificationRatio;
	private short pixelPaddingValue=0;

	private short imageRotation;                         // Enumerated values 0, 90, 180, 270
	private short seriesNumber;

	// =======================================================================
			
	public DICOMPresentationState() {
		lookup=new HashMap<Integer,Object>();
		sequenceLoc=new HashMap<Long,String>();
		textobjects=new ArrayList<TextObject>();
		graphicobjects=new ArrayList<GraphicObject>();
		graphicLayers=new HashSet<GraphicLayer>();
	}

	// =======================================================================
	
	public boolean readPresentationState(FileChannelImageInputStream fciis) {
		parser=new DcmParserImpl(fciis);
		parser.setDcmHandler(this);
		try {
			FileFormat fmat=null;
			parser.parseDcmFile(fmat,0);
			return parser.hasSeenEOF();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return false;
	}

	// =======================================================================
	
	public ArrayList<TextObject> getTextObjects() {return textobjects;}
	public ArrayList<GraphicObject> getGraphicObjects() {return graphicobjects;}
	public Set<GraphicLayer> getGraphicLayers() {return graphicLayers;}

	public void setCurrentGraphicLayer(GraphicLayer x) {currentGraphicLayer=x;}
	public void setCurrentGraphicObject(GraphicObject x) {currentGraphicObject=x;}
	public void setCurrentTextObject(TextObject x) {currentTextObject=x;}
	public void setTextObjects(ArrayList<TextObject> to) {this.textobjects=to;}
	public void setGraphicObjects(ArrayList<GraphicObject> go) {this.graphicobjects=go;}
	public void setGraphicLayers(Set<GraphicLayer> gl) {this.graphicLayers=gl;}

	// =======================================================================

	public void addGraphicLayer(GraphicLayer gl) {graphicLayers.add(gl);}
	public void addGraphicObject(GraphicObject go) {graphicobjects.add(go);}
	public void addTextObject(TextObject to) {textobjects.add(to);}

	// =======================================================================
	// DICOM HANDLER IMPLEMENTATIONS
	
	public void value(byte[] data, int start, int length) throws IOException {retrieveHeaderData(currentTag,data);}
	public void startElement(int tag, int vr, long pos) throws IOException {currentTag=tag; currentVr=vr; currentPos=pos;}

	// =======================================================================

	public void startSequence(int length) throws IOException {
		
		currentPos=parser.getStreamPosition();
		long until=(currentPos+length);
		String seqName="Unknown";
		switch(currentTag) {		
		  case 0x00081115: seqName="ReferencedSeriesSequence"; break;
		  case 0x00081140: seqName=("ReferencedImageSequence"); break;
		  case 0x00283110: seqName=("Softcopyvoilut sq"); break;
		  case 0x00700001: seqName=("GraphicAnnotationSequence "); break;    
		  case 0x0070005a: seqName=("DisplayedAreaSelectionSequence"); break;
		  case 0x00700060: seqName=("GraphicLayerSequence"); break;
		  case 0x00700008: seqName=("TextObjectSequence");
				               inTextObjectSequence=true;
											 break;
		  case 0x00700009: seqName=("GraphicObjectSequence");
			                 inGraphicObjectSequence=true;
											 break;
					    default: break;
		}
		LOG.debug("SEQUENCE Start "+seqName+" until: "+until);
		sequenceLoc.put(until,seqName);
	}

	public void endSequence(int length) throws IOException {
		
		currentPos=parser.getStreamPosition();
		String seqname=sequenceLoc.get(currentPos);
		if (seqname.equals("TextObjectSequence")) inTextObjectSequence=false;
		else if (seqname.equals("GraphicObjectSequence")) inGraphicObjectSequence=false;
		LOG.debug("SEQUENCE End "+seqname);
	}

	// =======================================================================

	public void startItem(int id, long pos, int length) throws IOException {
	
		if (inTextObjectSequence) { 
			currentTextObject=new TextObject();
			currentTextObject.setGraphicLayer(currentGraphicLayer.getGraphicLayer());
		} else if (inGraphicObjectSequence) { 
			currentGraphicObject=new GraphicObject();
			currentGraphicObject.setGraphicLayer(currentGraphicLayer.getGraphicLayer());
		}		
	}

	public void endItem(int len) throws IOException {
	
		if (inTextObjectSequence) { 
			textobjects.add(currentTextObject);
			currentTextObject=null;
		} else if (inGraphicObjectSequence) {
			graphicobjects.add(currentGraphicObject);
			currentGraphicObject=null;
		}		
	}

	// =======================================================================

	private boolean retrieveHeaderData(int tag, byte[] buf) {

		if (buf.length==0) return false;
		String bufString=new String(buf);
		bufString=bufString.trim();

		switch (tag) {

		  case 0x00020002: lookup.put(tag,bufString); break;                                                 // MediaStorageSOPClassUID UI(0002,0002)
		  case 0x00020003: lookup.put(tag,bufString); break;                                                 // MediaStorageSOPInstanceUID UI(0002,0003)
		  case 0x00080005: lookup.put(tag,bufString); break;                                                 // SpecificCharacterSet CS(0008,0005)		
		  case 0x00020010: lookup.put(tag,bufString); break;                                                 // TransferSyntaxUID UI(0002,0010)
		  case 0x00020012: lookup.put(tag,bufString); break;                                                 // ImplementationClassUID UI(0002,0012)
		  case 0x00020013: lookup.put(tag,bufString); break;                                                 // ImplementationVersionName SH(0002,0013)
		  case 0x00080012: lookup.put(tag,bufString); break;                                                 // InstanceCreationDate DA(0008,0012)
		  case 0x00080013: lookup.put(tag,parseDouble(buf)); break;                                          // InstanceCreationTime TM(0008,0013)
		  case 0x00080016: lookup.put(tag,bufString); break;                                                 // SOPClassUID UI(0008,0016)
		  case 0x00080018: lookup.put(tag,bufString); break;                                                 // SOPInstanceUID UI(0008,0018)
		  case 0x00080020: lookup.put(tag,bufString); break;                                                 // StudyDate DA(0008,0020)
		  case 0x00080030: lookup.put(tag,parseDouble(buf)); break;                                          // StudyTime TM(0008,0030)
		  case 0x00080050: lookup.put(tag,bufString); break;                                                 // AccessionNumber SH(0008,0050)
		  case 0x00080060: lookup.put(tag,bufString); break;                                                 // Modality CS(0008,0060)
		  case 0x00080070: lookup.put(tag,bufString); break;                                                 // Manufacturer LO(0008,0070)
		  case 0x00080090: lookup.put(tag,bufString); break;                                                 // ReferringPhysiciansName PN(0008,0090)
		  case 0x00081030: lookup.put(tag,bufString); break;                                                 // StudyDescription LO(0008,1030)		
		  case 0x00081080: this.admittingDiagnosesDescription=bufString; 
				               lookup.put(tag,bufString); 
											 break;                                                                            // AdmittingDiagnosesDescription(0008,1080)		
		  case 0x00081150: lookup.put(tag,bufString);  
				               this.referencedSOPClassUID=bufString;
											 break; 			                                                                     // ReferencedSOPClassUID UI(0008,1150)
		  case 0x00081155: lookup.put(tag,bufString);
				               this.referencedSOPInstanceUID=bufString; 
											 break; 			                                                                     // ReferencedSOPInstanceUID UI(0008,1155)
		  case 0x00081160: lookup.put(tag,bufString); 
				               this.referencedFrameNumber=bufString; 
											 break; 			                                                                     // ReferencedFrameNumber IS(0008,1160)
		  case 0x00100010: lookup.put(tag,bufString);
				               this.patientName=bufString;  
											 break;                                                                            // PatientsName PN(0010,0010)
		  case 0x00100020: lookup.put(tag,bufString); 
				               this.patientID=bufString;
											 break;                                                                            // PatientID  LO(0010,0020)
		  case 0x00100030: lookup.put(tag,bufString); 
				               this.patientBirthDate=bufString;
											 break;                                                                            // PatientsBirthDate DA(0010,0030)
		  case 0x00100040: lookup.put(tag,bufString); 
				               this.patientSex=bufString;    
											 break;                                                                            // PatientsSex CS(0010,0040)
		  case 0x00101010: lookup.put(tag,bufString); 
				               this.patientAge=bufString;   
											 break;                                                                            // PatientsAge  AS(0010,1010)
		  case 0x001021B0: lookup.put(tag,bufString); 
				               this.additionalPatientHistory=bufString;  
											 break;                                                                            // AdditionalPatientHistory (0010,21b0)
		  case 0x0020000d: lookup.put(tag,bufString); 
				               this.studyInstanceUID=bufString;
											 break;                                                                            // StudyInstanceUID (0020,000d)
		  case 0x0020000e: lookup.put(tag,bufString); 
				               this.seriesInstanceUID=bufString;  
											 break;                                                                            // SeriesInstanceUID (0020,000e)
		  case 0x00200010: lookup.put(tag,bufString); 
				               this.studyID=bufString;  
											 break;                                                                            // StudyID (0020,0010)
		  case 0x00200011: this.seriesNumber=parseShort(buf); 
				               lookup.put(tag,this.seriesNumber); 
											 break;                                                                            // SeriesNumber (0020,0011)
		  case 0x00200013: lookup.put(tag,bufString); 
				               this.instanceNumber=bufString; 
											 break;                                                                            // InstanceNumber (0020,0013)
		  case 0x00281052: this.rescaleIntercept=parseDouble(buf);
				               lookup.put(tag,rescaleIntercept);
											 break;                                                                            // RescaleIntercept (0028,1052)
		  case 0x00281053: this.rescaleSlope=parseDouble(buf); 
				               lookup.put(tag,rescaleSlope); 
											 break;                                                                            // RescaleSlope (0028,1053)
		  case 0x00281054: lookup.put(tag,bufString); 
				               this.rescaleType=bufString;  
											 break;                                                                            // RescaleType (0028,1054)
		  case 0x00281050: this.windowCenter=parseDouble(buf);  
				               lookup.put(tag,this.windowCenter); 
											 break; 			                                                                     // WindowCenter (0008,1050)
		  case 0x00281051: this.windowLevel=parseDouble(buf);   
				               lookup.put(tag,this.windowLevel); 
											 break; 			                                                                     // WindowLevel (0008,1051)
		  case 0x00700002: currentGraphicLayer=new GraphicLayer();
											 currentGraphicLayer.setGraphicLayer(bufString); 
											 graphicLayers.add(currentGraphicLayer);
				               break;                                                                            // GraphicLayer (0070,0002)
		  case 0x00700003: currentTextObject.setAnnotationUnits(bufString);                                  // BoundingBoxAnnotationUnits (0070,0003)
			                 break;                                                                            
		  case 0x00700005: currentGraphicObject.setAnnotationUnits(AnnotationUnitType.getAnnotationUnitType(bufString)); 
				               lookup.put(tag,bufString); 
				               break;                                                                            // GraphicAnnotationUnits (0070,0005)
		  case 0x00700006: currentTextObject.setTextValue(bufString); break;                                 // UnformattedTextValue (0070,0006)
		  case 0x00700010: currentTextObject.setTlhc(parseFloats(buf)); break;                               // BoundingBoxTLHC (0070,0010)
		  case 0x00700011: currentTextObject.setBrhc(parseFloats(buf)); break;                               // BoundingBoxBRHC (0070,0011)
		  case 0x00700012: currentTextObject.setThj(bufString); break;                                       // BoundingBoxTHJ (0070,0012)
		  case 0x00700020: currentGraphicObject.setDimensions(parseShort(buf));  break;                      // GraphicDimensions (0070,0020)
		  case 0x00700021: currentGraphicObject.setNumberOfPoints(parseShort(buf)); break;                   // NumberOfGraphicPoints (0070,0021)
		  case 0x00700022: currentGraphicObject.setData(parseFloats(buf)); break;                            // GraphicData (0070,0022)
		  case 0x00700023: currentGraphicObject.setType(GraphicType.getGraphicType(bufString)); 
			                 break;                                                                            // GraphicType (0070,0023)
		  case 0x00700024: currentGraphicObject.setFilled(parseBoolean(bufString)); break;                   // GraphicFilled (0070,0024)
		  case 0x00700041: this.imageHorizontalFlip=bufString; lookup.put(tag,bufString); break;             // ImageHorizontalFlip (0070,0041)
		  case 0x00700042: this.imageRotation=parseShort(buf); lookup.put(tag,imageRotation); break;         // ImageRotation (0070,0042)
		  case 0x00700052: this.displayedAreaTopLeftHandCorner=parseShorts(buf); 
				               lookup.put(tag,displayedAreaTopLeftHandCorner); break; 				                   // DisplayedAreaTopLeftHandCorner (0070,0052)
		  case 0x00700053: this.displayedAreaBottomRightHandCorner=parseShorts(buf); 
				               lookup.put(tag,displayedAreaBottomRightHandCorner); break; 				               // DisplayedAreaBottomRightHandCorner (0070,0053)
		  case 0x00700100: this.presentationSizeMode=bufString; lookup.put(tag,bufString); break; 				   // PresentationSizeMode (0070,0100)
	 // case 0x00700101: this.presentationPixelSpacing=bufString; lookup.put(tag,bufString); break; 			 // PresentationPixelSpacing (0070,0101)
		  case 0x00700062: currentGraphicLayer.setGraphicLayerOrder(parseShort(buf)); break;                 // GraphicLayerOrder (0070,0062)
		  case 0x00700068: currentGraphicLayer.setGraphicLayerDescription(bufString); break;                 // GraphicLayerDescription (0070,0068)
		  case 0x00700080: this.presentationLabel=bufString; lookup.put(tag,bufString); break;               // PresentationLabel (0070,0080)
		  case 0x00700081: this.presentationDescription=bufString; lookup.put(tag,bufString); break;         // PresentationDescription (0070,0081)
		  case 0x00700082: this.presentationCreationDate=bufString; lookup.put(tag,bufString); break;        // PresentationCreationDate (0070,0082)
		  case 0x00700083: this.presentationCreationTime=bufString; lookup.put(tag,presentationCreationTime); 
				               break;                                                                            // PresentationCreationTime (0070,0083)
		  case 0x00700084: this.presentationCreatorName=bufString; lookup.put(tag,bufString); break;         // PresentationCreatorName (0070,0084)
		  case 0x20500020: this.presentationLUTShape=bufString; lookup.put(tag,bufString); break;            // PresentationLUTShapee (2050,0020)
				      default: LOG.debug("====> UNKNOWN TAG: "+Tags.toString(tag)+", value:"+bufString); 
								       break;
		}
		return false;
	}

	// =======================================================================

	public static StringBuffer toHexString(StringBuffer sb, int v, int l) {for (int i=l; --i>=0;)	sb.append(HEX_DIGIT[(v >>> (i << 2)) & 0xf]);	return sb;}
	
	private boolean parseBoolean(String bufString) {if (bufString==null) return false; if (bufString.trim().equalsIgnoreCase("N")) return false; return true;}

	private short parseShort(byte[] buf) {return (short)((buf[0] & 0xff) | ((buf[1] & 0xff) << 8));}

	private short[] parseShorts(byte[] buf) {

		ByteBuffer bb=ByteBuffer.wrap(buf,0,buf.length).order(ByteOrder.LITTLE_ENDIAN);		
		int maxlen=Integer.MAX_VALUE;
		if (bb.limit()<4)	return null;
		short[] shorts=new short[bb.limit()];
		bb.rewind();
		shorts[0]=bb.getShort();
		for (int i=1; bb.remaining() >= 4 && shorts.length<maxlen; i++) shorts[i]=bb.getShort();
		return shorts;        
	}

	private double parseDouble(byte[] buf) {String s=new String(buf); return (new Double(s)).doubleValue();}	

	private float[] parseFloats(byte[] buf) {

		ByteBuffer bb=ByteBuffer.wrap(buf,0,buf.length).order(ByteOrder.LITTLE_ENDIAN);		
		int maxlen=Integer.MAX_VALUE;
		if (bb.limit()<4)	return null;
		float[] floats=new float[bb.limit()];
		bb.rewind();
		floats[0]=bb.getFloat();
		for (int i=1; bb.remaining() >= 4 && floats.length<maxlen; i++) floats[i]=bb.getFloat();
		return floats;        
	}
			
	public static boolean isStringValue(int vr) {

		switch (vr) {
		  case AE: case AS: case CS: case DA: case DS: case DT: case IS: case LO: 
		  case LT: case PN: case SH: case ST: case TM: case UI:	case UT: return true;
		}
		return false;
	}

	public static boolean isLengthField16Bit(int vr) {

		switch (vr) {
		  case AE: case AS: case AT: case CS: case DA: case DS: case DT:
		  case FL: case FD: case IS: case LO: case LT: case PN: case SH:
		  case SL: case SS: case ST: case TM: case UI: case UL: case US: return true;
		}
		return false;
	}
    
	public String vrToString(int vr) {return (vr==0x0000 ? "NONE"	: new String(new byte[]{(byte)(vr>>8),(byte)(vr)}));}

	public int vrValueOf(String str) {

		if ("NONE".equals(str))	return 0x0000;      
		if (str.length()!=2) throw new IllegalArgumentException(str);
		return ((str.charAt(0) & 0xff) << 8) | (str.charAt(1) & 0xff);
	}    
	
	// =======================================================================
	// Setters/getters for DICOM PS stuff

	public HashMap<Long,String> getSequenceLoc() {return sequenceLoc;}

	public String[] getPresentationPixelSpacing() {return presentationPixelSpacing;}
	public String[] getPresentationPixelAspectRatio() {return presentationPixelAspectRatio;}

	public String getAccessionNumber() {return accessionNumber;}
	public String getAdditionalPatientHistory() {return additionalPatientHistory;}
	public String getAdmittingDiagnosesCodeSeq() {return admittingDiagnosesCodeSeq;}
	public String getAdmittingDiagnosesDescription() {return admittingDiagnosesDescription;}
	public String getDeviceSerialNumber() {return deviceSerialNumber;}
	public String getEthnicGroup() {return ethnicGroup;}
	public String getImageHorizontalFlip() {return imageHorizontalFlip;}
	public String getInstanceNumber() {return instanceNumber;}
	public String getInstitutionAddress() {return institutionAddress;}
	public String getInstitutionDepartmentName() {return institutionDepartmentName;}
	public String getInstitutionName() {return institutionName;}
	public String getLastCalibrationDate() {return lastCalibrationDate;}
	public String getLastCalibrationTime() {return lastCalibrationTime;}
	public String getManufacturer() {return manufacturer;}
	public String getManufacturerModel() {return manufacturerModel;}
	public String getOccupation() {return occupation;}
	public String getOtherPatientIDs() {return otherPatientIDs;}
	public String getOtherPatientNames() {return otherPatientNames;}
	public String getPatientAge() {return patientAge;}
	public String getPatientBirthDate() {return patientBirthDate;}
	public String getPatientBirthTime() {return patientBirthTime;}
	public String getPatientComments() {return patientComments;}
	public String getPatientID() {return patientID;}
	public String getPatientName() {return patientName;}
	public String getPatientSex() {return patientSex;}
	public String getPatientSize() {return patientSize;}
	public String getPatientWeight() {return patientWeight;}
	public String getPhysicianOfRecord() {return physicianOfRecord;}
	public String getPresentationCreationDate() {return presentationCreationDate;}
	public String getPresentationCreationTime() {return presentationCreationTime;}
	public String getPresentationCreatorName() {return presentationCreatorName;}
	public String getPresentationDescription() {return presentationDescription;}
	public String getPresentationLUTShape() {return presentationLUTShape;}
	public String getPresentationLabel() {return presentationLabel;}
	public String getPresentationSizeMode() {return presentationSizeMode;}
	public String getProcedureCodeSequence() {return procedureCodeSequence;}
	public String getReferencedFrameNumber() {return referencedFrameNumber;}
	public String getReferencedSOPClassUID() {return referencedSOPClassUID;}
	public String getReferencedSOPInstanceUID() {return referencedSOPInstanceUID;}
	public String getReferencedStudySequence() {return referencedStudySequence;}
	public String getReferringPhysicianName() {return referringPhysicianName;}
	public String getRescaleType() {return rescaleType;}
	public String getSeriesDate() {return seriesDate;}
	public String getSeriesDescription() {return seriesDescription;}
	public String getSeriesInstanceUID() {return seriesInstanceUID;}
	public String getSeriesModality() {return seriesModality;}
	public String getSeriesTime() {return seriesTime;}
	public String getSoftwareVersion() {return softwareVersion;}
	public String getSpatialResolution() {return spatialResolution;}
	public String getStationName() {return stationName;}
	public String getStudyDate() {return studyDate;}
	public String getStudyDescription() {return studyDescription;}
	public String getStudyID() {return studyID;}
	public String getStudyInstanceUID() {return studyInstanceUID;}
	public String getStudyTime() {return studyTime;}
	public double getRescaleIntercept() {return rescaleIntercept;}
	public double getRescaleSlope() {return rescaleSlope;}
	public double getWindowCenter() {return windowCenter;}
	public double getWindowLevel() {return windowLevel;}

	public short[] getDisplayedAreaBottomRightHandCorner() {return displayedAreaBottomRightHandCorner;}
	public short[] getDisplayedAreaTopLeftHandCorner() {return displayedAreaTopLeftHandCorner;}

	public short getImageRotation() {return imageRotation;}
	public short getPixelPaddingValue() {return pixelPaddingValue;}
	public short getSeriesNumber() {return seriesNumber;}

	public float getPresentationPixelMagnificationRatio() {return presentationPixelMagnificationRatio;}

	public void setAccessionNumber(String x) {accessionNumber=x;}
	public void setAdditionalPatientHistory(String additionalPatientHistory) {this.additionalPatientHistory=additionalPatientHistory;}
	public void setAdmittingDiagnosesCodeSeq(String x) {admittingDiagnosesCodeSeq=x;}
	public void setAdmittingDiagnosesDescription(String admittingDiagnosesDescription) {this.admittingDiagnosesDescription=admittingDiagnosesDescription;}
	public void setDeviceSerialNumber(String x) {deviceSerialNumber=x;}
	public void setDisplayedAreaBottomRightHandCorner(short[] displayedAreaBottomRightHandCorner) {this.displayedAreaBottomRightHandCorner=displayedAreaBottomRightHandCorner;}
	public void setDisplayedAreaTopLeftHandCorner(short[] displayedAreaTopLeftHandCorner) {this.displayedAreaTopLeftHandCorner=displayedAreaTopLeftHandCorner;}
	public void setEthnicGroup(String x) {ethnicGroup=x;}
	public void setImageHorizontalFlip(String imageHorizontalFlip) {this.imageHorizontalFlip=imageHorizontalFlip;}
	public void setImageRotation(short imageRotation) {this.imageRotation=imageRotation;}
	public void setInstanceNumber(String instanceNumber) {this.instanceNumber=instanceNumber;}
	public void setInstitutionAddress(String x) {institutionAddress=x;}
	public void setInstitutionDepartmentName(String x) {institutionDepartmentName=x;}
	public void setInstitutionName(String x) {institutionName=x;}
	public void setLastCalibrationDate(String x) {lastCalibrationDate=x;}
	public void setLastCalibrationTime(String x) {lastCalibrationTime=x;}
	public void setManufacturer(String x) {manufacturer=x;}
	public void setManufacturerModel(String x) {manufacturerModel=x;}
	public void setOccupation(String x) {occupation=x;}
	public void setOtherPatientIDs(String x) {otherPatientIDs=x;}
	public void setOtherPatientNames(String x) {otherPatientNames=x;}
	public void setPatientAge(String patientAge) {this.patientAge=patientAge;}
	public void setPatientBirthDate(String patientBirthDate) {this.patientBirthDate=patientBirthDate;}
	public void setPatientBirthTime(String patientBirthTime) {this.patientBirthTime=patientBirthTime;}
	public void setPatientComments(String x) {patientComments=x;}
	public void setPatientID(String patientID) {this.patientID=patientID;}
	public void setPatientName(String patientName) {this.patientName=patientName;}
	public void setPatientSex(String patientSex) {this.patientSex=patientSex;}
	public void setPatientSize(String x) {patientSize=x;}
	public void setPatientWeight(String x) {patientWeight=x;}
	public void setPhysicianOfRecord(String x) {physicianOfRecord=x;}
	public void setPixelPaddingValue(short x) {pixelPaddingValue=x;}
	public void setPresentationCreationDate(String presentationCreationDate) {this.presentationCreationDate=presentationCreationDate;}
	public void setPresentationCreationTime(String presentationCreationTime) {this.presentationCreationTime=presentationCreationTime;}
	public void setPresentationCreatorName(String presentationCreatorName) {this.presentationCreatorName=presentationCreatorName;}
	public void setPresentationDescription(String presentationDescription) {this.presentationDescription=presentationDescription;}
	public void setPresentationLUTShape(String presentationLUTShape) {this.presentationLUTShape=presentationLUTShape;}
	public void setPresentationLabel(String presentationLabel) {this.presentationLabel=presentationLabel;}
	public void setPresentationPixelAspectRatio(String[] x) {presentationPixelAspectRatio=x;}
	public void setPresentationPixelMagnificationRatio(float x) {presentationPixelMagnificationRatio=x;}
	public void setPresentationPixelSpacing(String[] presentationPixelSpacing) {this.presentationPixelSpacing=presentationPixelSpacing;}
	public void setPresentationSizeMode(String presentationSizeMode) {this.presentationSizeMode=presentationSizeMode;}
	public void setProcedureCodeSequence(String x) {procedureCodeSequence=x;}
	public void setReferencedFrameNumber(String referencedFrameNumber) {this.referencedFrameNumber=referencedFrameNumber;}
	public void setReferencedSOPClassUID(String referencedSOPClassUID) {this.referencedSOPClassUID=referencedSOPClassUID;}
	public void setReferencedSOPInstanceUID(String referencedSOPInstanceUID) {this.referencedSOPInstanceUID=referencedSOPInstanceUID;}
	public void setReferencedStudySequence(String x) {referencedStudySequence=x;}
	public void setReferringPhysicianName(String x) {referringPhysicianName=x;}
	public void setRescaleIntercept(double rescaleIntercept) {this.rescaleIntercept=rescaleIntercept;}
	public void setRescaleSlope(double rescaleSlope) {this.rescaleSlope=rescaleSlope;}
	public void setRescaleType(String rescaleType) {this.rescaleType=rescaleType;}
	public void setSequenceLoc(HashMap<Long, String> sequenceLoc) {this.sequenceLoc=sequenceLoc;}
	public void setSeriesDate(String x) {seriesDate=x;}
	public void setSeriesDescription(String x) {seriesDescription=x;}
	public void setSeriesInstanceUID(String seriesInstanceUID) {this.seriesInstanceUID=seriesInstanceUID;}
	public void setSeriesModality(String x) {seriesModality=x;}
	public void setSeriesNumber(short x) {this.seriesNumber=x;}
	public void setSeriesTime(String x) {seriesTime=x;}
	public void setSoftwareVersion(String x) {softwareVersion=x;}
	public void setSpatialResolution(String x) {spatialResolution=x;}
	public void setStationName(String x) {stationName=x;}
	public void setStudyDate(String studyDate) {this.studyDate=studyDate;}
	public void setStudyDescription(String x) {studyDescription=x;}
	public void setStudyID(String studyID) {this.studyID=studyID;}
	public void setStudyInstanceUID(String studyInstanceUID) {this.studyInstanceUID=studyInstanceUID;}
	public void setStudyTime(String studyTime) {this.studyTime=studyTime;}
	public void setWindowCenter(double windowCenter) {this.windowCenter=windowCenter;}
	public void setWindowLevel(double windowLevel) {this.windowLevel=windowLevel;}

	// =======================================================================
	// PresentationState interface stuff

	public String[] getGroupingKeys() {return new String[] {seriesInstanceUID};}
	public String[] getSortingKeys() {return new String[] {studyDate,seriesTime,instanceNumber};}

	public String[] getPSStudyDescription() {return new String[] {getInstitutionName(),StringUtilities.replaceAndCapitalize(studyDescription,true)};}
	public String[] getPSSeriesDescription() {return new String[] {getSeriesModality()+ " ("+StringUtilities.replaceAndCapitalize(manufacturer+" "+manufacturerModel,true)+")",
																																 StringUtilities.replaceAndCapitalize(seriesDescription,true)};}
	
	public String getSeriesTimestamp() {

		try {
			String str=((seriesDate==null) ? ((studyDate==null) ? "" : studyDate) : seriesDate)+" "+((seriesTime==null) ? ((studyTime==null) ? "" : studyTime) : seriesTime);
			if (str!=null) str=str.trim(); 
			return DATE_FORMAT6.format((str!=null) ? (((str.indexOf(":")>0) ? DATE_FORMAT2.parse(str) : ((str.indexOf(" ")>0) ? DATE_FORMAT1.parse(str) : DATE_FORMAT5.parse(str)))) : new Date(0));
		} catch (Exception exc) {}
		return null;
	}

	public String getStudyTimestamp() {

		try {
			String str=getStudyDate()+" "+getStudyTime();
			if (str!=null) str=str.trim();
			return DATE_FORMAT6.format((str.indexOf(":")>0) ? DATE_FORMAT2.parse(str) : DATE_FORMAT1.parse(str));
		} catch (Exception exc) {}
		return null;
	}

	public String getReferencedImageKey() {return getReferencedSOPInstanceUID();}
	public String getReferencedSeriesKey() {return getSeriesInstanceUID();}
	public String getReferencedStudyKey() {return getStudyInstanceUID();}
	public String getModality() {return getSeriesModality();}
	public String getInstitution() {return (institutionName==null) ? "" : StringUtilities.replaceAndCapitalize(institutionName,true);}
	public String getImageType() {return "DICOM";}

	// =======================================================================

	public ArrayList<Annotation> getAnnotations() {if (cachedAnnotations==null) cachedAnnotations=DICOMPSFactory.createAnnotations(this); return cachedAnnotations;}

}

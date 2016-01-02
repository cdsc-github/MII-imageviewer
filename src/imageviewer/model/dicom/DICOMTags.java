/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

// =======================================================================
// String contstants for DICOM headers.  Aligned with David Clunie's
// DataDictionary from pixelmed.  Any new constants should use the
// same string reference that is in the DICOMTagMap class.

public interface DICOMTags {

	public static final int AE=0x4145, AS=0x4153, AT=0x4154, CS=0x4353, DA=0x4441, DS=0x4453, DT=0x4454,
		                      FD=0x4644, FL=0x464C, IS=0x4953, LO=0x4C4F, LT=0x4C54, PN=0x504E, SH=0x5348, 
		                      SL=0x534C, SS=0x5353, ST=0x5354, TM=0x544D, UI=0x5549, UL=0x554C, US=0x5553, 
		                      UT=0x5554, OB=0x4F42, OW=0x4F57, SQ=0x5351, UN=0x554E, QQ=0x3F3F;

	public static final String ACQUISITION_DATE="AcquisitionDate";	
	public static final String ACQUISITION_NUMBER="AcquisitionNumber";
	public static final String ACQUISITION_TIME="AcquisitionTime";
	public static final String ADDITIONAL_PATIENT_HISTORY="AdditionalPatientHistory";
	public static final String BIRTH_DATE="PatientBirthDate";
	public static final String BLUE_PALETTE="BluePaletteColorLookupTableData";	
	public static final String CONTENT_DATE="ContentDate";
	public static final String CONTENT_TIME="ContentTime";
	public static final String CORRECTED_IMAGE="CorrectedImage";
	public static final String GREEN_PALETTE="GreenPaletteColorLookupTableData";
	public static final String IMAGE_BITS_ALLOCATED="BitsAllocated";
	public static final String IMAGE_BITS_STORED="BitsStored";
	public static final String IMAGE_COLUMNS="Columns";
	public static final String IMAGE_ECHO_NUMBER="EchoNumber";
	public static final String IMAGE_ECHO_TIME="EchoTime";
	public static final String IMAGE_FLIP_ANGLE="FlipAngle";
	public static final String IMAGE_GANTRY_TILT="GantryDetectorTilt";
	public static final String IMAGE_HIGH_BIT="HighBit";
	public static final String IMAGE_INSTANCE_NUMBER="InstanceNumber";
	public static final String IMAGE_INVERSION_TIME="InversionTime";
	public static final String IMAGE_KVP="KVP";
	public static final String IMAGE_MAX_PIXEL="LargestImagePixelValue";
	public static final String IMAGE_MIN_PIXEL="SmallestImagePixelValue";
	public static final String IMAGE_ORIENTATION="ImageOrientation";
	public static final String IMAGE_PIXEL_ASPECT_RATIO="PixelAspectRatio";
	public static final String IMAGE_PIXEL_REPRESENTATION="PixelRepresentation";
	public static final String IMAGE_PIXEL_SPACING="PixelSpacing";
	public static final String IMAGE_POSITION="ImagePosition";
	public static final String IMAGE_REPITITION_TIME="RepetitionTime";
	public static final String IMAGE_RESCALE_INTERCEPT="RescaleIntercept";
	public static final String IMAGE_RESCALE_SLOPE="RescaleSlope";
	public static final String IMAGE_ROWS="Rows";
	public static final String IMAGE_SAMPLE_PER_PIXEL="SamplesPerPixel";
	public static final String IMAGE_SLICE_POSITION="SliceLocation";
	public static final String IMAGE_SLICE_SPACING="SpacingBetweenSlices";
	public static final String IMAGE_SLICE_THICKNESS="SliceThickness";
	public static final String IMAGE_TABLE_HEIGHT="TableHeight";
	public static final String NUMBER_FRAMES="NumberOfFrames";
	public static final String PATIENT_BIRTH_TIME="PatientBirthTime";
	public static final String PATIENT_ID="PatientID";
	public static final String PATIENT_NAME="PatientName";
	public static final String PATIENT_ORIENTATION="PatientOrientation";
	public static final String PATIENT_POSITION="PatientPosition";
	public static final String PATIENT_SEX="PatientSex";
	public static final String PHOTO_INTERPRETATION="PhotometricInterpretation";
	public static final String PIXEL_PADDING_VALUE="PixelPaddingValue";
	public static final String PLANAR_CONFIGURATION="PlanarConfiguration";
	public static final String RED_PALETTE="RedPaletteColorLookupTableData";
	public static final String SERIES_ANGIO_FLAG="AngioFlag";
	public static final String SERIES_BODY_PART_EXAMINED="BodyPartExamined";
	public static final String SERIES_CONTRAST="ContrastBolusAgent";
	public static final String SERIES_DATE="SeriesDate";
	public static final String SERIES_DESCRIPTION="SeriesDescription";
	public static final String SERIES_IMAGE_MODALITY="Modality";
	public static final String SERIES_INSTANCE_UID="SeriesInstanceUID";
	public static final String SERIES_MANUFACTURER="Manufacturer";
	public static final String SERIES_MANUFACTURER_MODEL="ManufacturerModelName";
	public static final String SERIES_MR_ACQUISITION_TYPE="MRAcquisitionType";
	public static final String SERIES_NUMBER="SeriesNumber";
	public static final String SERIES_SCANNING_SEQUENCE="ScanningSequence";
	public static final String SERIES_SEQUENCE_NAME="SequenceName";
	public static final String SERIES_SEQUENCE_VARIANT="SequenceVariant";
	public static final String SERIES_STATION_NAME="StationName";
	public static final String SERIES_TIME="SeriesTime";
	public static final String SOP_CLASS_UID="SOPClassUID";
	public static final String SOP_INSTANCE_UID="SOPInstanceUID";
	public static final String STUDY_ACCESSION_NUMBER = "StudyAccessionNumber";	
	public static final String STUDY_DATE="StudyDate";
	public static final String STUDY_DESCRIPTION="StudyDescription";
	public static final String STUDY_ID="StudyID";
	public static final String STUDY_IMAGE_TYPE="ImageType";
	public static final String STUDY_INSTANCE_UID="StudyInstanceUID";
	public static final String STUDY_INSTITUTION="InstitutionName";
	public static final String STUDY_REFERRING_PHYSICIAN = "StudyReferringPhysician";
	public static final String STUDY_TIME="StudyTime";
	public static final String STUDY_TRANSFER_SYNTAX="TransferSyntaxUID";
	public static final String WINDOW_CENTER="WindowCenter";
	public static final String WINDOW_WIDTH="WindowWidth";

	public static final int AXIAL=0;
	public static final int CORONAL=1;
	public static final int SAGITTAL=2;
	public static final int OBLIQUE=3;

	public static final int T1=0;
	public static final int T2=1;
	public static final int PD=2;
	public static final int STIR=3;
	public static final int FLAIR=4;
	public static final int UNKNOWN_WEIGHTING=5;

}

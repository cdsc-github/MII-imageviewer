/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.sun.media.imageio.stream.FileChannelImageInputStream;
import com.sun.media.imageio.stream.FileChannelImageOutputStream;

public class DICOMStrip {

	private static final Logger LOG=Logger.getLogger("imageviewer.dicom");

	private static final int[] DEFAULT_TAGS={0x00080080,0x00080081,0x00080090,0x00080092,0x00080094,0x00081048,0x00081070,0x00081072,
																					 0x00100010,0x00100020,0x00100030,0x00100032,0x00100050,0x00101000,0x00101001,0x00101005,
																					 0x00101040,0x00101060,0x00101080,0x00101081,0x00101090,0x00102000,0x00102150,0x00102152,
																					 0x00102154,0x00102160,0x00102180,0x001021B0,0x001021C0,0x001021D0,0x001021F0,0x00104000,
																					 0x00200010,0x00400006,0x40080114,0x00321032};
	// 0x00080020 Study date
	// 0x00080021 Series date
	// 0x00080022 Series date
	// 0x00080023 Series date

	// =======================================================================

	ArrayList<Integer> tags=new ArrayList<Integer>();

	public DICOMStrip() {for (int loop=0, n=DEFAULT_TAGS.length; loop<n; loop++) tags.add(DEFAULT_TAGS[loop]);}

	// =======================================================================

	public boolean stripFile(File rootInputDir, File inputFile, File outputDir, String hint) {

		try {
			FileChannel fcIn=new FileInputStream(inputFile).getChannel();
			if (fcIn.size()==0) return false;
			String inputPath=inputFile.getParent();
			String inputFilename=inputFile.getName();
			LOG.info("["+inputFilename+"]");
			String targetOutputName=outputDir.getPath()+"\\"+inputFile.toString().substring(rootInputDir.toString().length()+1);
			File outputFile=new File(targetOutputName);
			File outputPath=new File(outputFile.getParent());
			outputPath.mkdirs();
			FileChannel fcOut=new FileOutputStream(outputFile).getChannel();
			FileChannelImageInputStream fciis=new FileChannelImageInputStream(fcIn);
			FileChannelImageOutputStream fcios=new FileChannelImageOutputStream(new RandomAccessFile(outputFile,"rws").getChannel());
			fciis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
			fcios.setByteOrder(ByteOrder.LITTLE_ENDIAN);
			DICOMHeader dh=new DICOMHeader();
			boolean b=dh.stripDICOMHeader(fciis,fcios,tags,hint);
			if (b) {
				byte[] pixels=new byte[1024];
				while (true) {
					int readTotal=fciis.read(pixels);
					if (readTotal<0) break; else fcios.write(pixels,0,readTotal);
				}
			} else {
				LOG.warn("Unable to properly read and strip file: "+inputFilename);
			}
			fciis.close();
			fcios.flush();
			fcios.close();
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
		return true;
	}

	// =======================================================================

	private void getFiles(File f, ArrayList<File> holder) {

		// Handle the case difference between a single file, or a
		// directory containing multiple files. The problem is handled
		// recursively, as a sub-directory may be contained wihtin.
		
		if (f.isFile()) {
			holder.add(f);		
		} else {
			File[] filenames=f.listFiles();
			for (int loop=0; loop<filenames.length; loop++) {
				File aFile=filenames[loop];
				getFiles(aFile,holder);
			}
		}
	}

	// =======================================================================
	
	public static void main(String[] args) {
		
		if (args.length>=2) {
			try {
				File f=new File(args[0]);
				if (!f.exists()) {
					LOG.error("Targeted input file/directory does not exist, exiting...");
					System.exit(1);
				}
				File outputDir=new File(args[1]);
				if (outputDir.exists()) {
					if (outputDir.isDirectory()) LOG.warn("Output directory exists, files will be overwritten...");
					else if (outputDir.isFile()) {
						LOG.error("A file with the same output name exists; unable to proceed.");
						System.exit(1);
					}
				} else outputDir.mkdir();
				ArrayList<File> files=new ArrayList<File>();
				DICOMStrip ds=new DICOMStrip();
				ds.getFiles(f,files);
				String hint=(args.length==3) ? args[2] : null;
				for (File targetFile: files) {
					ds.stripFile(f,targetFile,outputDir,hint);
				}
				LOG.info("Finished processing "+files.size()+" file(s).");
			} catch (Exception exc) {
				LOG.error("Unable to execute DICOMStrip...");
				exc.printStackTrace();
				System.exit(1);
			}
		}
	}
}

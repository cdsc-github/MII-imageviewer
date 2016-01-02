/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.net.URL;

import javax.imageio.ImageIO;

import imageviewer.model.wrapped.*;
import imageviewer.ui.ApplicationPanel;
import java.awt.image.RenderedImage;


// =======================================================================

/**
 * Gets information from URL selected from a query result list during a MIRC
 * query.
 *
 * @author Brian Burns, Jean Garcia, Kyle Singleton, Jamal Madni, Agatha Lee
 * @version $Revision: 1.1 $ $Date: 2008/12/05 10:23:00 $
 */

public class OpenDialogURLThread extends Thread {

	URL databaseURL = null;
	ArrayList<WrappedImage> bufferedURLImages = null;
//	ArrayList<File> URLImageFiles = null;

       /**
        * Setup variables for new URL thread
        * 
        * @param newURL - URL to search for image information
        */
	public OpenDialogURLThread(URL newURL) {
		super();
		this.databaseURL = newURL;
		this.bufferedURLImages = new ArrayList<WrappedImage>();
	//	this.URLImageFiles = new ArrayList<File>();
	}
	
	// =======================================================================

       /**
        * Return the number of images containted int he buffered set
        * 
        * @return the number of images buffered for display
        */	
	public int getNumImages(){
		return bufferedURLImages.size();
	}
	
	// =======================================================================
	
       /**
        * Return a specified image from the buffered set
        * 
	* @param index - the index of the requested image
        * @return the wrapped image found at the given index
        */
	public WrappedImage getImage(int index){
		return bufferedURLImages.get(index);
	}
	
	// =======================================================================
	
       /**
        * Return the URL being used by this thread
        * 
        * @return the thread URL
        */
	public URL getURL(){
		return databaseURL;
	}
	
	// =======================================================================

       /**
        * Finds all references to images within the URL of interest.  Downloads
	* the images from each reference and places them into a bufferedImage
	* array for use in display.
        * 
        */
	public void run() {
		
		// pull all image URLs out of the database URL and then download each image 
		// into the bufferedImage array
		try  {  
			InputStream in = databaseURL.openStream();
			InputStreamReader inR = new InputStreamReader(in); 
			BufferedReader buf = new BufferedReader(inR); 
			String line;
			
			//parse the xml to find images
			while  ((line = buf.readLine ()) != null)   {  
				StringTokenizer st = new StringTokenizer (line,"\"");
				while (st.hasMoreTokens())  {
					String string = st.nextToken ();
					String stringLowerCase = string.toLowerCase ();
					//this is pulling out ALL images on the web page, even the display icons we don't need
					if (stringLowerCase.endsWith("jpg") || stringLowerCase.endsWith("jpeg")){  
						// create path of image
						String urlName = databaseURL.getPath();
						StringTokenizer urlTokenizer = new StringTokenizer (urlName, "/");
						String imageUrlString = databaseURL.getProtocol()+"://"+databaseURL.getHost()+"/";
						while (urlTokenizer.hasMoreTokens())  {
							String tempString = urlTokenizer.nextToken();
							if (urlTokenizer.hasMoreTokens())
								imageUrlString = imageUrlString + tempString + "/";
						}

						imageUrlString = imageUrlString + string.replace (" ", "%20");
						System.out.println (imageUrlString);
						URL imageURL = new URL (imageUrlString);
						
						//Retrieve the image and add it to list of buffered images
						bufferedURLImages.add(new WrappedImage ((RenderedImage)ImageIO.read(imageURL)));
						
						//write out image file
				//		ImageIO.write (bufferedURLImages.get(i), "raw",URLImageFiles.get(i));
				//		URLImageFiles.add(new File(string));
					}
				}
			}
			in.close ();
		}catch (Exception exp) {
			System.out.println ("Error reading images from URL: "+databaseURL);
		}
		if(bufferedURLImages.size()==0){
			System.out.println ("No images to read from URL: "+databaseURL);
		}

		//display images
		WrappedImageSequence wrappedImageSeq = new WrappedImageSequence (bufferedURLImages);
		ArrayList <WrappedImageSequence> wrappedImageSeqArray = new ArrayList <WrappedImageSequence> ();
		wrappedImageSeqArray.add (wrappedImageSeq);

		WrappedImageStudy wrappedImageStudy = new WrappedImageStudy (wrappedImageSeqArray);

		ArrayList <WrappedImageStudy> wrappedImageStudyArray = new ArrayList <WrappedImageStudy> ();
		wrappedImageStudyArray.add (wrappedImageStudy);
                ApplicationPanel.getInstance().addImages(wrappedImageStudyArray);

	}
	
	// =======================================================================
	
       /**
        * Provides image information (i.e. width, height, bit depth) for the
	* image at the specified index.
        * 
        * @param index - index of the requested image
	* @return the width, height, and bit depth as an array list of integers
        */
	public ArrayList<Integer> getImageParams(int index) {
		
		// if there are no images or bad index used, return null
		if(bufferedURLImages.size()==0 || bufferedURLImages.get(index-1) == null)  return null;
		
		//pull out the width, height, and figure out the bit depth of each image
		ArrayList<Integer> paramList=new ArrayList<Integer>();
			
		paramList.add(bufferedURLImages.get(index).getWidth());//width
		paramList.add(bufferedURLImages.get(index).getHeight());//height
			
		//Jean: doesn't compile, and I don't think we're using it anymore.
		int type = 0;//bufferedURLImages.get(index).getType();
		int bitsStored=32;
		int bitsAllocated=32;
			
		switch(type){
			case(BufferedImage.TYPE_3BYTE_BGR):
				bitsAllocated=24;
				bitsStored=24;
				break;
			case(BufferedImage.TYPE_4BYTE_ABGR):
			case(BufferedImage.TYPE_4BYTE_ABGR_PRE):
				bitsAllocated=24;
				bitsStored=32;
				break;
			case(BufferedImage.TYPE_BYTE_GRAY):
				bitsAllocated=8;
				bitsStored=8;
				break;
			case(BufferedImage.TYPE_INT_ARGB):
			case(BufferedImage.TYPE_INT_ARGB_PRE):
			case(BufferedImage.TYPE_INT_BGR):
			case(BufferedImage.TYPE_INT_RGB):
				bitsAllocated=bitsStored=32;
				break;		
			case(BufferedImage.TYPE_USHORT_555_RGB):
			case(BufferedImage.TYPE_USHORT_565_RGB):
			case(BufferedImage.TYPE_USHORT_GRAY):
				bitsAllocated=bitsStored=16;
				break;
					
			case(BufferedImage.TYPE_BYTE_INDEXED):
			case(BufferedImage.TYPE_BYTE_BINARY):
				bitsAllocated=32;
				bitsStored=32;
				break;
					
			case(BufferedImage.TYPE_CUSTOM):
			default:
					
		}
		paramList.add(bitsAllocated);//bits allocated
		paramList.add(bitsStored);	//bits stored
		paramList.add(0);			//file offset
		paramList.add(1);			//number of images to read
		
		return (paramList);
	}
}

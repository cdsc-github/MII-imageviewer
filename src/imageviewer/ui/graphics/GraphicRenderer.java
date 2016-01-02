/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.graphics;

import java.awt.image.Kernel;

public abstract class GraphicRenderer {

	// ============================================================================	

	protected static Kernel computeGaussianKernel(int radius) {

		int kernelSize=(radius*2)+1;
		float kernel[]=new float[kernelSize*kernelSize];

    double sum=0.0;
    double deviation=radius/3.0;
    double devSqr2=2*Math.pow(deviation,2);
    double piDevSqr2=Math.PI*devSqr2;
		
    for (int i=0; i<kernelSize; i++) {
      for (int j=0; j<kernelSize; j++) {
				kernel[i*kernelSize+j]=(float)(Math.pow(Math.E,-((j-radius)*(j-radius)+(i-radius)*(i-radius))/devSqr2)/piDevSqr2);
				sum+=kernel[i*kernelSize+j];			    
      }
    }

    for (int i=0; i<kernelSize; i++){
      for (int j=0; j<kernelSize; j++){
				kernel[i*kernelSize+j]/=sum;
      }
    }
		Kernel k=new Kernel(2*radius+1,2*radius+1,kernel);
		return k;
	}
}

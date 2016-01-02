/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.util;

import java.io.File;
import java.net.URLDecoder;

public class TestUtil {

	/**
	 * This method allows you to easily find Unit Test configuration files and resources
	 * located in the same directory as your Unit Test java file itself.  This method uses
	 * the classpath to locate the file.  The standard usage would be:
	 * 
	 *   TestUtil.getFilePath(this,"file.txt");
	 * 
	 * @param unitTestObject
	 * @param testFileName
	 * @return
	 */
	public static String getFilePath(Object unitTestObject, String testFileName) {
		
		Class c = unitTestObject.getClass();
		Package p = c.getPackage();
		String packageName = p.getName();
		String packagePath = packageName.replace('.','/');

		packagePath = "/"+packagePath;		
		String currentDir = unitTestObject.getClass().getResource(packagePath).getFile();

		// we have to clean things up because our getResource() method above
		// returns a URL, which likes to urlencode things
		currentDir = URLDecoder.decode(currentDir);

		File testFile = new File(currentDir,testFileName);

		if (!testFile.exists()) throw new RuntimeException("Path doesn't exist: "+testFile.getAbsolutePath());
		String testFilePath = testFile.getAbsolutePath();
		return testFilePath;
	}

}

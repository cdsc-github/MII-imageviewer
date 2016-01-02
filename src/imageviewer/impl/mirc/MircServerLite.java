/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/


//package org.rsna.mircsite.queryservice;

package imageviewer.impl.mirc;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
//import org.apache.log4j.Logger;
//import org.rsna.mircsite.util.*;

/**
 * Lightweight implementation of MIRC server code.
 * This is the thread that queries one storage service. One instance of this thread is
 * instantiated by the query service for each storage service (server) to be queried.
 */
public class MircServerLite extends Thread {

	private String urlString;
    //private Visa visa;
	private String serverName;
	private String mircQuery;
	private File xslProgram;
	private String firstResult;
	private String proxyCredentials;

    //static final Logger logger = Logger.getLogger(MircServerLite.class);

	/** the query result; null until the result has been received. */
	public String result = null;
	/** the result ready flag; false until the result is ready; then true. */
	public boolean ready = false;

	/**
	 * Class constructor.
	 * @param urlString the URL of the MIRC storage service to be queried.
	 * @param serverName the name of the MIRC storage service to be queried
	 * (used as a heading in the results list).
	 * @param firstResult the item number of the first result in the list of query results
	 * (used to set the first item number in the ordered list of results).
	 */
	public MircServerLite(
			String urlString,
			//Visa visa,
			String serverName,
			String mircQuery,
			//File xslProgram,
			String firstResult,
			String proxyCredentials) {
		this.urlString = urlString;
		//this.visa = visa;
		this.serverName = serverName;
		this.mircQuery = mircQuery;
		//this.xslProgram = xslProgram;
		this.firstResult = firstResult;
		this.proxyCredentials = proxyCredentials;
	}

	/**
	 * The thread's run code that sends a query to the server,
	 * waits for a response, processes the response, stores
	 * it in a public variable, and sets a flag to indicate
	 * that the result is ready.
	 */
	public void run() {
		String serverResponse = "";
		try {
			URL url = new URL(urlString);
			//if (url.getUserInfo() != null) Authenticator.setDefault(new QueryAuthenticator(url));
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type","text/xml; charset=\"UTF-8\"");
			//if (!proxyCredentials.equals("")){
			//conn.setRequestProperty("Proxy-Authorization", "Basic "+proxyCredentials);
			//}
			//if (visa != null) {
			//	String auth = visa.getBasicAuthorization();
			//	conn.setRequestProperty("Authorization",auth);
			//	}
			conn.setDoOutput(true);
			conn.connect();

			Charset utf8 = Charset.forName("UTF-8");

			//Send the query to the server
			BufferedWriter svrbw =
				new BufferedWriter(
					new OutputStreamWriter(
							       //conn.getOutputStream(),FileUtil.utf8));
							       conn.getOutputStream(),utf8));
			svrbw.write(mircQuery);
			svrbw.flush();
			svrbw.close();

			//Get the response
			BufferedReader svrrdr =
				new BufferedReader(
					new InputStreamReader(
							      conn.getInputStream(),/*FileUtil.*/utf8));
			StringWriter svrsw = new StringWriter();
			char[] cbuf = new char[1024];
			int n;
			boolean hcf = false;
			while (((n = svrrdr.read(cbuf,0,1024)) != -1) && !(hcf = interrupted())) svrsw.write(cbuf,0,n);
			svrrdr.close();
			if (hcf) serverResponse = makeExceptionResponse("No response from the server.");
			else serverResponse = svrsw.toString();
		}
		catch (MalformedURLException e) {
			serverResponse = makeExceptionResponse("Malformed URL: "+urlString);
		}
		catch (IOException e) {
			serverResponse =
				makeExceptionResponse(
					"IO Error during connection: " + urlString + "<br/>" + e.getMessage() );
		}
		catch (IllegalStateException e) {
			serverResponse = makeExceptionResponse("Programming error in the MircServerLite class.");
		}
		catch (NullPointerException e) {
			serverResponse = makeExceptionResponse("Programming error in the MircServerLite class.");
		}

		result = serverResponse;

		ready = true;
		//At this point, the thread stops, but it remains instantiated, and the
		//parent can poll the ready and result fields to see what happened.
		//There is an opportunity for improvement here by implementing an event/listener
		//interface to achieve a slight response improvement when all the queried
		//servers are present.
	}

	//Make an error response as a MIRCqueryresult.
	//This function is called when there has been a non-catastrophic error
	//so the transformer can still be used to process the result.
	private String makeExceptionResponse(String s) {
		return "<MIRCqueryresult><preamble><font color=\"red\"><b>"
						+ s + "</b></font></preamble></MIRCqueryresult>";
	}

	//Make an error response to return without transformation.
	//This function is called when there has been a catastrophic error
	//in processing the server's response, so we don't use the transformer
	//to process the error message; we just generate the HTML directly.
	//If you want to change the error reporting format, you have to do it
	//here, not in MIRCqueryresult.xsl.
	private String makeErrorResponse(String s) {
		return "<font color=\"red\"><h3>" + serverName + "</h3>" +
						"<p><b>" + s + "</b></p></font>";
	}

}


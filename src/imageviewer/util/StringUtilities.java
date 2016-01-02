/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ===========================================================
// Simple class that does some string management, such as
// capitalization of a string, and a truncation mapping utility.

public class StringUtilities {

	private static final String[] ANATOMY_TERMS={"BRAIN","CHEST","LUNG","ABDOMEN","HEAD","NECK","FOOT","CARDIAC","CARDIO","BREAST","THORAX","PELVIS",
																							 "HAND","SKULL","SPINE","LOWER EXTREMITY","UPPER EXTREMITY","EXTREMITY","HIP","FEMUR","KNEE","WRIST"};

	private static final HashMap capitalizations=new HashMap();

	private static final ArrayList<Pattern> replacePattern=new ArrayList<Pattern>();
	private static final ArrayList<String> replaceString=new ArrayList<String>();

	// ===========================================================
	// Initialize the mapping data into a hashmap and arraylists.

	static {
		
		try {
			FileInputStream fis=new FileInputStream("resources/text/capitalizationMapping.txt");
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));
			String mapping=null;
			while ((mapping=br.readLine())!=null) {
				if (mapping==null) break;
				mapping=mapping.trim();
				StringTokenizer entrytokenizer=new StringTokenizer(mapping,"=");
				while (entrytokenizer.hasMoreTokens()) {
					String word=entrytokenizer.nextToken().toLowerCase();
					String capWord=entrytokenizer.nextToken();
					capitalizations.put(word,capWord);
				}
			}
		} catch (EOFException eofex) {
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		try {
			FileInputStream fis=new FileInputStream("resources/text/replacementMapping.txt");
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));
			String mapping=null;
			while ((mapping=br.readLine())!=null) {
				if (mapping==null) break;
				StringTokenizer entryTokenizer=new StringTokenizer(mapping,"=");
				if (entryTokenizer.countTokens()==2) {
					String word=entryTokenizer.nextToken().toLowerCase();
					String replacement=entryTokenizer.nextToken().toLowerCase();
					if (replacement.compareTo("*")==0) replacement=new String(" ");
					replacePattern.add(Pattern.compile(word));
					replaceString.add(replacement);
				}
			}

		} catch (EOFException eofex) {
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	// ===========================================================

	public static String capitalize(String s) {

		// Capitalize only the first character of the string...

		if (s==null) return new String();

		switch (s.length()) {

		   case 0: return new String();
		   case 1: return s.toUpperCase();
		  default: String lowerSubstring=s.substring(1).toLowerCase();
			         String upperSubstring=s.substring(0,1).toUpperCase();
		           return new String(upperSubstring+lowerSubstring);
		}
	}

	// ===========================================================

	public static String multiCapitalize(String s) {

		// Tokenize the entire string based on spaces and capitalize each
		// token.  In theory, we should also address possible puncutation
		// problems, but this should do for now...

		if (s==null) return new String();
		StringBuffer sb=new StringBuffer(s.length());
		StringTokenizer st=new StringTokenizer(s," ");
		
		while (st.hasMoreTokens()) {
			String token=st.nextToken();
			if (token.startsWith("(")){
				token=capitalize(token.substring(token.indexOf("(")+1,token.indexOf(")")-1));
				token="("+token+")";
				sb.append(" " + token);		
			}
			else sb.append(" "+capitalize(token));
		}
		return sb.toString().trim();
	}

	// ===========================================================

	public static String capitalizationLookup(String s) {return capitalizationLookup(s,false,false);}

	public static String capitalizationLookup(String s, boolean isSentence, boolean isName) {

		// Tokenize the entire string based on spaces and capitalize each
		// token.  In theory, we should also address possible puncutation
		// problems, but this should do for now...

		if (s==null) return new String();
		if (s=="") return s;

		StringBuffer sb=new StringBuffer(s.length());
		s=s.replaceAll("\\^\\d*|_|\\s+"," ");
		StringTokenizer st=new StringTokenizer(s," ()-,:;/",true);
		
		int counter=0;
		while (st.hasMoreTokens()) {
			String token=st.nextToken();
			String newToken=(String)capitalizations.get(token.toLowerCase());
			if (newToken!=null) {
				sb.append(newToken);
			} else if (isName) {
				if (token.length()<2) {
					sb.append(token.toUpperCase());
				} else {
					char c=Character.toUpperCase(token.charAt(0));
					sb.append(c+token.toLowerCase().substring(1));
				}
			} else {
				sb.append(token.toLowerCase());
			}
		}
		String newString=sb.toString().trim();
		if ((newString.length()!=0)&&(isSentence)) {
			char c=Character.toUpperCase(newString.charAt(0));
			return (c+newString.substring(1));
		} else return newString;
	}

	// ===========================================================

	public static String applyReplacements(String s) {

		s=s.replaceAll("\\^\\d*|_|\\s+"," ");
		for (int loop=0, n=replacePattern.size(); loop<n; loop++) {
			Pattern p=replacePattern.get(loop);
			Matcher m=p.matcher(s);
			s=m.replaceAll(replaceString.get(loop));
		}
		return s;
	}

	// ===========================================================

	public static String replaceAndCapitalize(String s, boolean isSentence) {return (s == null)? "": capitalizationLookup(applyReplacements(s.toLowerCase()),isSentence,false);}

	// ===========================================================

	public static String formatName(String s, boolean hasLastNameFirst) {

		String formatted=capitalizationLookup(applyReplacements(s.toLowerCase()),false,true);
		if (hasLastNameFirst) formatted=formatted.replaceFirst(" ",", ");
		return formatted;
	}

	// ===========================================================

	public static String clean(String s) {

		if (s==null) return null;
		StringBuffer sb=new StringBuffer();
		for (int i=0, n=s.length(); i<n; i++) {
			char c=s.charAt(i);
			if (c>=0x20 && c<=0x7e) sb.append(c);
		}
		return sb.toString();
  }

	// ===========================================================

	public static String findAnatomyPhrase(String phrase) {

		if (phrase==null) return null;
		phrase=applyReplacements(phrase.toLowerCase()).toUpperCase();
		for (int loop=0; loop<ANATOMY_TERMS.length; loop++) {
			int stringIndex=phrase.indexOf(ANATOMY_TERMS[loop]);
			if (stringIndex!=-1) return ANATOMY_TERMS[loop];
		}
		return null;
	}

	// ===========================================================

	public static String findLongestPattern(String text, Pattern p) {

		String longestMatch=new String();
		Matcher m=p.matcher(text);
		while (m.find()) {
			String nextPattern=m.group();
			if (nextPattern.length()>longestMatch.length()) longestMatch=nextPattern;
		}
		return longestMatch;
	}

	// ===========================================================

	public static String formatPatientID(String patientID) {

		Pattern p=Pattern.compile("[\\d]+");
		String id=patientID.replaceAll("\\^\\d*|_|\\s+"," ");
		return findLongestPattern(id.replaceAll("-",""),p);
	}
}

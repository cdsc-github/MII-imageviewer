/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackInputStream;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;

import com.tagish.auth.Utils;

// =======================================================================

public final class PasswordGenerator {

	String filename=null, targetUser=null;
	Hashtable<String,char[]> userTable=new Hashtable<String,char[]>();
	ArrayList<String> userList=new ArrayList<String>();
	boolean addCommand=false, removeCommand=false, updateCommand=false;

	private PasswordGenerator(CommandLine args) {

		filename=(args.hasOption("file")) ? args.getOptionValue("file") : null;
		if (args.hasOption("add")) {targetUser=args.getOptionValue("add"); addCommand=true;}
		if (args.hasOption("remove")) {targetUser=args.getOptionValue("remove"); removeCommand=true;}
		if (args.hasOption("update")) {targetUser=args.getOptionValue("update"); updateCommand=true;}
	}

	// =======================================================================
	// If we switch to JDK 1.6, we can use the Console class to do all of this...

	private final char[] getPassword(InputStream in, String prompt) throws IOException {

		MaskingThread mt=new MaskingThread(prompt);
		Thread thread=new Thread(mt);
		thread.start();
	
		char[] lineBuffer;
		char[] buf=lineBuffer=new char[128];

		int room=buf.length;
		int offset=0;
		int c;

		loop: while (true) {
			switch (c=in.read()) {
			    case -1: 
			  case '\n': break loop;
			  case '\r': int c2=in.read();
					         if ((c2!='\n') && (c2!=-1)) {
										 if (!(in instanceof PushbackInputStream)) in=new PushbackInputStream(in);
										 ((PushbackInputStream)in).unread(c2);
									 } else {
										 break loop;
									 }
			    default: if (--room<0) {
										 buf=new char[offset+128];
										 room=buf.length-offset-1;
										 System.arraycopy(lineBuffer,0,buf,0,offset);
										 Arrays.fill(lineBuffer,' ');
										 lineBuffer=buf;
									 }
						       buf[offset++]=(char)c;
									 break;
			}
		}

		mt.stopMasking();
		if (offset==0) return null;
		char[] ret=new char[offset];
		System.arraycopy(buf,0,ret,0,offset);
		Arrays.fill(buf,' ');
		return ret;
	}

	// =======================================================================

	private void parse(File f) throws IOException {

		BufferedReader br=null;
		try {
			FileReader fr=new FileReader(f);
			br=new BufferedReader(fr);
			String entry=null;
			while ((entry=br.readLine())!=null) {
				int index=entry.indexOf(":");
				String userName=entry.substring(0,index);
				String password=entry.substring(index+1);
				userTable.put(userName,password.toCharArray());
				userList.add(userName);
			}
		} finally {
			if (br!=null) br.close();
		}
	}

	private void write(File f) throws IOException {
		
		PrintWriter pw=new PrintWriter(f);
		for (String userName : userList) {
			pw.println(userName+":"+new String(userTable.get(userName)));
			pw.flush();
		}
		pw.close();
	}

	// =======================================================================

	private void execute() {

		if ((filename!=null)&&(targetUser!=null)) {
			try {
				System.out.println("");
				File f=new File(filename);
				if (f.exists()) parse(f); else f.createNewFile();
				if (addCommand) {
					String password1=new String(getPassword(System.in,"Password for user "+targetUser+": "));
					String password2=new String(getPassword(System.in,"Re-enter password: "));
					if (password1.equals(password2)) {
						System.out.println("New password confirmed. Adding entry to file.");
						userList.add(targetUser);
						userTable.put(targetUser,Utils.cryptPassword(password1.toCharArray()));
						write(f);
					}
				} else if (removeCommand) {
					userTable.remove(targetUser);
					userList.remove(targetUser);
					write(f);
					System.out.println("User has been removed from file.");
				} else if (updateCommand) {
					String password1=new String(Utils.cryptPassword(getPassword(System.in,"Current password for user "+targetUser+": ")));
					String password2=new String(getPassword(System.in,"New password for user "+targetUser+": "));
					String password3=new String(getPassword(System.in,"Re-enter new password: "));
					if (userList.contains(targetUser)) {
						String targetOriginalPassword=new String(userTable.get(targetUser));
						if (targetOriginalPassword.equals(password1)) {
							if (password2.equals(password3)) {
								userTable.put(targetUser,Utils.cryptPassword(password2.toCharArray()));
								System.out.println("New password accepted, updating file.");
								write(f);
							} else {
								System.out.println("The new passwords do not match. Aborting.");
							}
						} else {
							System.out.println("The entered password does not match the password on file. Aborting.");
						}
					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	// =======================================================================

	private class MaskingThread extends Thread {

		volatile boolean stop=false;
		String prompt=null;

		public MaskingThread(String prompt) {System.out.print(prompt+" "); this.prompt=prompt;}
		
		public void run() {

      int priority=Thread.currentThread().getPriority();
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
      try {
				while (!stop) {
					try {
						Thread.currentThread().sleep(1);
					} catch (InterruptedException iex) {
						Thread.currentThread().interrupt();
						return;
					}
					if (!stop) System.out.print("\r"+prompt+"\r");
					System.out.flush();
				}
      } finally {
				System.out.print("\b");
				Thread.currentThread().setPriority(priority);
      }
		}

		public void stopMasking() {this.stop=true;}
	}

	// =======================================================================

	public static void main(String[] args) {

		Option help=new Option("help","Print this message");
		Option file=OptionBuilder.withArgName("file").hasArg().withDescription("Password filename").create("file");
		Option addUser=OptionBuilder.withArgName("add").hasArg().withDescription("Add user profile").create("add");
		Option removeUser=OptionBuilder.withArgName("remove").hasArg().withDescription("Remove user profile").create("remove");
		Option updateUser=OptionBuilder.withArgName("update").hasArg().withValueSeparator().withDescription("Update user profile").create("update");

		file.setRequired(true);
		OptionGroup og=new OptionGroup();
		og.addOption(addUser);
		og.addOption(removeUser);
		og.addOption(updateUser);
		og.setRequired(true);
		
		Options o=new Options();
		o.addOption(help);
		o.addOption(file);
		o.addOptionGroup(og);

		try {
			CommandLineParser parser=new GnuParser();
			CommandLine line=parser.parse(o,args);
			PasswordGenerator pg=new PasswordGenerator(line);
			pg.execute();
		} catch (UnrecognizedOptionException uoe) {
			System.err.println("Unknown argument: "+uoe.getMessage());
			HelpFormatter hf=new HelpFormatter();
			hf.printHelp("PasswordGenerator",o);
			System.exit(1);
		} catch (MissingOptionException moe) {
			System.err.println("Missing argument: "+moe.getMessage());
			HelpFormatter hf=new HelpFormatter();
			hf.printHelp("PasswordGenerator",o);
			System.exit(1);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}

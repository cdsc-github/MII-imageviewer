/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.table.AbstractTableModel;

import imageserver.client.MoveStatus;

// =======================================================================

public class TrackingTableModel extends AbstractTableModel {

	private static final String[] COLUMN_NAMES=new String[] {"Time requested","Patient ID","Description","Status","% Completed","Transfer time","Message","Retries","Source",
																													 "Final target","Current tracking location","Elapsed time","Series UID"};
	private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("M/dd/yyyy h:mm a");
	private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("E M dd hh:mm:ss zzz yyyy");

	private static final int MILLI_HOURS=(1000*60*60);
	private static final int MILLI_MINUTES=(1000*60);

	// =======================================================================

	ArrayList<TrackingItem> requests=new ArrayList<TrackingItem>();

	public TrackingTableModel() {super();}

	public String getColumnName(int x) {return COLUMN_NAMES[x];}
	public boolean isCellEditable(int row, int col) {return false;}

	public void add(TrackingItem ti) {requests.add(ti); int index=requests.size()-1; fireTableRowsInserted(index,index);}
	public void remove(TrackingItem ti) {int index=requests.indexOf(ti); requests.remove(ti); if (index!=-1) fireTableRowsDeleted(index,index);}
	public void updated(TrackingItem ti) {int index=requests.indexOf(ti); if (index!=-1) fireTableRowsUpdated(index,index);}

	// =======================================================================

	public void clearCompleted() {

		ArrayList<TrackingItem> completed=new ArrayList<TrackingItem>(); 
		for (TrackingItem ti : requests) if (MoveStatus.SUCCESS.equals(ti.getStatus())) completed.add(ti);        // Don't modify the model at the same time you analyze it...
		for (TrackingItem ti : completed) remove(ti);
	}

	public void clearFailed() {

		ArrayList<TrackingItem> errored=new ArrayList<TrackingItem>(); 
		for (TrackingItem ti : requests) if (MoveStatus.ERROR.equals(ti.getStatus())) errored.add(ti);        // Don't modify the model at the same time you analyze it...
		for (TrackingItem ti : errored) remove(ti);
	}

	// =======================================================================

	private String computeElapsedTime(long startTime, long finalTime) {

		if (startTime==0) return "";
		long delta=(finalTime!=0) ? (finalTime-startTime) : (System.currentTimeMillis()-startTime);
		int hours=(int)Math.floor(delta/MILLI_HOURS);
		delta-=MILLI_HOURS*hours;
		int minutes=(int)Math.floor(delta/MILLI_MINUTES);
		delta-=MILLI_MINUTES*minutes;
		int seconds=(int)Math.floor(delta/1000);
		return new String(hours+":"+((minutes<10) ? "0" : "")+minutes+":"+((seconds<10) ? "0" : "")+seconds);
	}

	public void setValueAt(Object value, int row, int col) {}	

	public int getColumnCount() {return COLUMN_NAMES.length;}
	public int getRowCount() {return requests.size();}

	public Object getValueAt(int row, int col) {
		
		TrackingItem ti=requests.get(row);
		switch (col) {
		  case 0: Date d=new Date(ti.getRequestTime());
				      return new String(DATE_FORMAT1.format(d));
		  case 1: return ti.getPatientID();
		  case 2: return (ti.getModality()+" "+ti.getDescription());
		  case 3: String s=ti.getStatus();
				      if (MoveStatus.SUCCESS.equals(s)) return "Move completed";
							if (MoveStatus.ERROR.equals(s)) return "Transfer error";
							if (MoveStatus.MOVE.equals(s)) return "Move in progress";
							if (MoveStatus.PENDING.equals(s)) return "Move pending...";
							return "";
		  case 4: return ti.getPercentComplete();
		  case 5: return computeElapsedTime(ti.getStartTime(),ti.getFinalTime());
		  case 6: return ti.getMessage();
		  case 7: return ti.getRetries();
		  case 8: return ti.getSourceNode();
		  case 9: return ti.getFinalTargetNode();
		 case 10: return ti.getTrackingLocation();
		 case 11: String elapsedTime=ti.getElapsedTrackingTime();
			        if (elapsedTime!=null) {
								try {
									GregorianCalendar gc=new GregorianCalendar();
									gc.setTime(DATE_FORMAT2.parse(elapsedTime));
									return computeElapsedTime(gc.getTimeInMillis(),0);
								} catch (Exception exc) {
									return "";
								}
							} else {
								return "";
							}
	   case 12: return ti.getUID();
		}
		return null;
	}
}

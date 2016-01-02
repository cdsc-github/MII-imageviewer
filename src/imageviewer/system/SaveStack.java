/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.system;

import java.util.ArrayList;

//=======================================================================

public class SaveStack {

	private static final SaveStack INSTANCE=new SaveStack();

	public static SaveStack getInstance() {return INSTANCE;}

	//=======================================================================

	ArrayList<Saveable> unsavedItems=new ArrayList<Saveable>();
	ArrayList<SaveStackListener> listeners=new ArrayList<SaveStackListener>();

	public boolean hasUnsavedItems() {return !unsavedItems.isEmpty();}
	public boolean save(Saveable x) {boolean result=x.save(); if (result) unsavedItems.remove(x); fireSaveStackChange(); return result;}

	public void addSaveable(Saveable x) {if (!unsavedItems.contains(x)) {unsavedItems.add(x); fireSaveStackChange();}}
	public void removeSaveable(Saveable x) {unsavedItems.remove(x); fireSaveStackChange();}
	public void saveAll() {for (Saveable s : unsavedItems) s.save(); unsavedItems.clear(); fireSaveStackChange();}
	public void fireSaveStackChange() {int n=unsavedItems.size(); for (SaveStackListener ssl : listeners) ssl.saveStackUpdate(n);}
	public void addListener(SaveStackListener x) {listeners.add(x);}
	public void removeListener(SaveStackListener x) {listeners.remove(x);}

	public ArrayList<Saveable> getSaveables() {return unsavedItems;}

	private SaveStack() {}
}

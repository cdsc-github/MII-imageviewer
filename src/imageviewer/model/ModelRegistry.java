/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.util.ArrayList;
import java.util.Hashtable;

// =======================================================================
// Singleton pattern to store information on model dependencies in
// the application. All objects implementing the model interface
// have a key (the dependentKey) that specifies what objects should be
// closed given the key. This association is maintained in one
// hashtable. The second set of information associates the actual
// object linked to that key, and is held in a map.

public class ModelRegistry {

	private static ModelRegistry REGISTRY=null;

	public static ModelRegistry getInstance() {if (REGISTRY==null) REGISTRY=new ModelRegistry(); return REGISTRY;}

	// =======================================================================

	Hashtable<Object,ArrayList<ModelObject>> dependentTable=new Hashtable<Object,ArrayList<ModelObject>>();   // Handle dependentKeys
	Hashtable<Object,ArrayList<ModelObject>> closeMap=new Hashtable<Object,ArrayList<ModelObject>>();         // Handle closeKeys

	private ModelRegistry() {}

	// =======================================================================
	
	public Hashtable<Object,ArrayList<ModelObject>> getDependentTable() {return dependentTable;}
	public Hashtable<Object,ArrayList<ModelObject>> getCloseMap() {return closeMap;}

	public ArrayList<ModelObject> getDependents(Object o) {return dependentTable.get(o);}
	public ArrayList<ModelObject> get(Object o) {return closeMap.get(o);}

	public void map(Object o, ModelObject mo) {ArrayList<ModelObject> al=closeMap.get(o); if (al==null) {al=new ArrayList<ModelObject>(); closeMap.put(o,al);}	if (!al.contains(mo)) al.add(mo);}
	public void unmap(Object o, ModelObject mo) {ArrayList<ModelObject> al=closeMap.get(o); if (al==null) return; al.remove(mo);	if (al.isEmpty()) {closeMap.remove(o); dependentTable.remove(o);}}
	public void addDependent(Object o, ModelObject mo) {ArrayList<ModelObject> al=dependentTable.get(o); if (al==null) {al=new ArrayList<ModelObject>(); dependentTable.put(o,al);} if (!al.contains(mo)) al.add(mo);}
	public void removeDependent(Object o, ModelObject mo) {ArrayList<ModelObject> al=dependentTable.get(o); if (al!=null) al.remove(mo); else return; if (al.isEmpty()) dependentTable.remove(o);}

}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

import imageviewer.model.aim.calculation.Calculation;

import java.util.ArrayList;
import java.util.Date;

public class AIMAnnotation {
	
	int id = 0;
	String aimVersion = "AIM 1.0";
	String comment = "";
	Date dateTime = null;
	String name = "";
	String uniqueIdentifier = "";
	
	Equipment equipment;
	User user;
	ArrayList<AnatomicEntity> anatomicEntityCollection = new ArrayList<AnatomicEntity>();
	ArrayList<ImagingObservation> imagingObservationCollection = new ArrayList<ImagingObservation>();
	ArrayList<Calculation> calculationCollection = new ArrayList<Calculation>();	
	
	public AIMAnnotation() {initialize();}
	public AIMAnnotation(String name) { this.name = name; initialize();}
	
	private void initialize() {
		setDateTime(new Date());		
	}
	
	public Equipment getEquipment(){
		return equipment;
	}
	
	public void setEquipment(Equipment e) {
		equipment = e;
	}
	
	public User getUser(){
		return user;
	}
	
	public void setUser(User u) {
		user = u;
	}
	
	public ArrayList<AnatomicEntity> getAnatomicCollection(){
		return anatomicEntityCollection;
	}
	public void setAnatomicCollection(ArrayList<AnatomicEntity> ae){
		anatomicEntityCollection = ae;
	}
	public void addAnatomicEntity(AnatomicEntity ae){
		anatomicEntityCollection.add(ae);
	}
	
	public ArrayList<ImagingObservation> getImagingCollection(){
		return imagingObservationCollection;
	}
	public void setImagingCollection(ArrayList<ImagingObservation> iObs){
		imagingObservationCollection = iObs;
	}
	public void addImagingObservation(ImagingObservation imObs){
		imagingObservationCollection.add(imObs);
	}
	
	public ArrayList<Calculation> getCalculationCollection() {
		return calculationCollection;
	}
	public void setCalculationCollection(ArrayList<Calculation> calculationCollection) {
		this.calculationCollection = calculationCollection;
	}
	public void addCalculation(Calculation c){
		calculationCollection.add(c);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAimVersion() {
		return aimVersion;
	}
	public void setAimVersion(String aimVersion) {
		this.aimVersion = aimVersion;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	
	public String toXML(){
		String xmlString = "";
		return xmlString;
	}

}

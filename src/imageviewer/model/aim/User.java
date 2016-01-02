/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.aim;

public class User {
	
	int id;
	String name;
	String loginName;
	String roleInTrial;
	int numberWithinRoleOfClinicalTrial;
	
	public User() {id = 0;}
	public User(String name, String loginName) {
		this.id = 0;
		this.name = name;
		this.loginName = loginName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getRoleInTrial() {
		return roleInTrial;
	}

	public void setRoleInTrial(String roleInTrial) {
		this.roleInTrial = roleInTrial;
	}

	public int getNumberWithinRoleOfClinicalTrial() {
		return numberWithinRoleOfClinicalTrial;
	}

	public void setNumberWithinRoleOfClinicalTrial(
			int numberWithinRoleOfClinicalTrial) {
		this.numberWithinRoleOfClinicalTrial = numberWithinRoleOfClinicalTrial;
	}
	
	public String toXML() {
		String xmlString = "<user>\n";
		xmlString = xmlString + "<User id=\"" + id + "\" name=\"" + getName() + "\" loginName=\"" + getLoginName() + "\""
											  + " numberWithinRoleOfClinicalTrial=\"" + getNumberWithinRoleOfClinicalTrial()
											  + "\" roleInTrial=\"" + getRoleInTrial() + "\" />\n";
		xmlString = xmlString  + "</user>\n";
		return xmlString;
	}

}

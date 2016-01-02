/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import java.util.ArrayList;

import imageviewer.model.PropertiedObject;

public class LayoutLogicalOp implements RuleConditional {

	public static final int AND=0;
	public static final int OR=1;
	public static final int NOT=2;

	// =======================================================================

	ArrayList<RuleConditional> conditions=new ArrayList<RuleConditional>();
	int operator=AND;

	public LayoutLogicalOp(String type) {

		if (type!=null) {
			String opType=type.toLowerCase();
			if (opType.compareTo("not")==0) operator=NOT;
			else if (opType.compareTo("or")==0) operator=OR;
			else operator=AND;
		}
	}

	// =======================================================================

	public void addConditional(RuleConditional x) {conditions.add(x);}
	public void removeConditional(RuleConditional x) {conditions.remove(x);}

	public int getOperator() {return operator;}
	public void setOperator(int x) {if ((operator>-1)&&(operator<7)) operator=x;}

	// =======================================================================

	public boolean evaluate(PropertiedObject po) {

		switch (operator) {

		  case AND: for (int loop=0, n=conditions.size(); loop<n; loop++) {
				          RuleConditional rc=conditions.get(loop);
									boolean result=rc.evaluate(po);
									if (!result) return false;
			          }
				        return true;

		   case OR: for (int loop=0, n=conditions.size(); loop<n; loop++) {
				          RuleConditional rc=conditions.get(loop);
									boolean result=rc.evaluate(po);
									if (result) return true;
			          }
				        return false;

		  case NOT: RuleConditional rc=conditions.get(0);
				        boolean result=rc.evaluate(po);
								return (!result);
		}
		return false;
	}
}

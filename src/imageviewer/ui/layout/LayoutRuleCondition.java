/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import imageviewer.model.PropertiedObject;

public class LayoutRuleCondition implements RuleConditional {

	public static final int EQ=0;
	public static final int NE=1;
	public static final int LT=2;
	public static final int LE=3;
	public static final int GT=4;
	public static final int GE=5;
	public static final int SUB=6; // Substring capabilities

	// =======================================================================

	String variable=null;
	Object value=null;
	int operator=EQ;

	public LayoutRuleCondition() {}
	public LayoutRuleCondition(String variable, int operator, Object value) {this.variable=variable; this.operator=operator; this.value=value;}

	// =======================================================================

	public String getVariable() {return variable;}
	public Object getValue() {return value;}
	public int getOperator() {return operator;}

	public void setVariable(String x) {variable=x;}
	public void setValue(Object x) {value=x;}
	public void setOperator(int x) {if ((operator>-1)&&(operator<7)) operator=x;}

	// =======================================================================

	public boolean evaluate(PropertiedObject po) {

		Object instanceValue=po.getProperty(variable);
		if (instanceValue==null) return false;
		if ((value instanceof Comparable)&&(instanceValue instanceof Comparable)) {
			Comparable c1=(Comparable)value;
			Comparable c2=(Comparable)instanceValue;
			int comp=c1.compareTo(c2);
			switch (operator) {
			  case EQ: return (comp==0);
			  case NE: return (comp!=0);
			  case LT: return (comp<0);
			  case LE: return (comp<=0);
			  case GT: return (comp>0);
			  case GE: return (comp>=0);
			 case SUB: if ((value instanceof String)&&(instanceValue instanceof String)) return (((String)instanceValue).indexOf((String)value)>=0);
			}
		}
		return false;
	}
}

/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;
import imageviewer.ui.ApplicationContext;
import imageviewer.util.XMLUtil;

// =======================================================================
/**
 * Factory class for LayoutDescription objects.  Use this class to
 * load configuration files for layouts, and then come here for your
 * layout one-stop-shopping.
 *
 */
public class LayoutFactory {

	private static final Hashtable<String,LayoutDescription> LAYOUTS=new Hashtable<String,LayoutDescription>();
	private static final Logger LOG=Logger.getLogger("imageViewer.ui");

	private static final ArrayList<LayoutRule> LAYOUT_RULES=new ArrayList<LayoutRule>();
	private static final ArrayList<CompositionRule> COMPOSITE_RULES=new ArrayList<CompositionRule>();
	private static final ArrayList<GroupingRule> SERIES_GROUPING_RULES=new ArrayList<GroupingRule>();
	private static final ArrayList<GroupingRule> STUDY_GROUPING_RULES=new ArrayList<GroupingRule>();

	private static final String DEFAULT_FILENAME=new String("config/layouts.xml");
	private static String DEFAULT_LAYOUT=new String("Small overview_1400x1050");

	// =======================================================================

	/**
	 * Initialize our factory.  Loads DEFAULT_FILENAME by default. 
	 */
	public static void initialize() {

		String layoutFile=(String)ApplicationContext.getContext().getProperty("CONFIG_LAYOUTS");
		if (layoutFile==null) layoutFile=DEFAULT_FILENAME;
		load(layoutFile);
	}

	/**
	 * Initialize factory with a specific XML configuration file path to read.  
	 * 
	 * @param filename
	 */
	public static void load(String filename) {

		LOG.info("Loading layout configurations: "+filename);

		// Parse XML file with configuration.

		try {
			LayoutXMLHandler handler=new LayoutXMLHandler();
			SAXParser parser=XMLUtil.getSAXParser();
			InputStream is=new FileInputStream(filename);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,handler);
			is.close();
			XMLUtil.releaseSAXParser(parser);
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+filename);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+filename);
			exc.printStackTrace();
		}
	}
	
	// =======================================================================
	/**
	 * Lookup the layout given by the name only.  This method
	 * automatically appends the resolution dimensions of the machine
	 * running this code to the name, e.g. the layout "bluto" would be
	 * "bluto1280x1024" on my machine.
	 * 
	 * @param name
	 * @return
	 */
	public static LayoutDescription doLookup(String name) {

		GraphicsConfiguration gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Rectangle r=gc.getBounds();		
		String resolutionTag=(int)r.getWidth()+"x"+(int)r.getHeight();		
		LayoutDescription ld=LAYOUTS.get(name+"_"+resolutionTag);				
		if (ld==null) {
			ld=LAYOUTS.get(name+"_1024x768");
			if (ld==null) ld=LAYOUTS.get(name);                                        // Try and do it without the resolution
		}
		return ld;
	}

	// =======================================================================
	/**
	 * Come up with a LayoutPlan, given a set of studies.
	 * 
	 * @param studies
	 * @return
	 */
	public static LayoutPlan selectLayouts(ArrayList<? extends ImageSequenceGroup> studies) {

		// First, execute any composition rules...

		for (int i=0, n=studies.size(); i<n; i++) {
			ImageSequenceGroup isg=studies.get(i);
			for (int j=0, m=COMPOSITE_RULES.size(); j<m; j++) {
				CompositionRule cr=COMPOSITE_RULES.get(j);
				if (!cr.isIgnored()) cr.process(isg);
			}
		}
			
		// Create a layout plan based on multiple studies passed in (or,
		// simply a single study with multiple series. We proceed in three
		// steps: 1) first, assign a layout independently to each of the
		// specified series -- this step uses the basic rules assigned in
		// the layout config; 2) examine each series within given study,
		// and group together with a new layout if it matches a
		// groupingRule with scope of series; and 3) finally, group
		// together any series from across studies (if multiple studies),
		// recollapsing layouts as needed based on step #2.

		LayoutPlan lp=new LayoutPlan();
		for (int i=0, n=studies.size(); i<n; i++) {
			ImageSequenceGroup isg=(ImageSequenceGroup)studies.get(i);
			for (int j=0, m=isg.size(); j<m; j++) {
				ImageSequence is=(ImageSequence)isg.get(j);
				LayoutDescription ld=selectLayout(is);
				GroupedLayout gl=new GroupedLayout(new ImageSequence[] {is},new String[] {"PRIMARY"},ld);
				lp.addInitialLayout(is,gl);
			}
		}

		// Second pass, examine each series by passing it to the grouping
		// rules specified as part of the series set. The layout plan is
		// also sent in, so it can be amended by the rules.
		
		lp.initializeLayouts();
		for (int i=0, n=SERIES_GROUPING_RULES.size(); i<n; i++) {
			GroupingRule gr=SERIES_GROUPING_RULES.get(i);
			for (int j=0, m=studies.size(); j<m; j++) {
				ImageSequenceGroup isg=(ImageSequenceGroup)studies.get(j);
				if (!gr.isIgnored()) gr.selectLayout(isg,lp);				
			}
		}
		/*
		// Third and final pass, examine all studies together, and apply
		// the global grouping rules, taking into account the previous
		// series-level grouping.

		for (int i=0, n=STUDY_GROUPING_RULES.size(); i<n; i++) {
			GroupingRule gr=STUDY_GROUPING_RULES.get(i);
			if (!gr.isIgnored()) gr.selectLayout(studies,lp);
		}
		*/
		return lp;
	}

	// =======================================================================

	public static LayoutDescription selectLayout(ImageSequence is) {

		for (int loop=0, n=LAYOUT_RULES.size(); loop<n; loop++) {
			LayoutRule lr=LAYOUT_RULES.get(loop);
			if (lr.evaluate(is)) {
				LayoutDescription ld=doLookup(lr.getSelectedLayout());
				if (ld!=null) return ld;
			}
		}
		return doLookup(DEFAULT_LAYOUT);
	}

	// =======================================================================

	private static class LayoutXMLHandler extends DefaultHandler {

		final Class[] constructorParameterTypes={};
		final Object[] constructorParameters={};

		LayoutRule lr=null;
		LayoutRuleCondition lrc=null;
		LayoutLogicalOp llo=null;
		Stack<LayoutLogicalOp> opStack=new Stack<LayoutLogicalOp>();

		public LayoutXMLHandler() {super();}
		
		// =======================================================================

		public void startElement(String uri, String localname, String qname, Attributes attr) {

			if (localname.compareTo("file")==0) {
				try {
					LayoutDescription ld=LayoutDescriptionReader.load(attr.getValue("name"));
					if (ld!=null) LAYOUTS.put(ld.getName()+"_"+ld.getResolutionTag(),ld);
				} catch (Exception exc) {
					LOG.error("Unable to parse sepcified file in LayoutFactory.");
					exc.printStackTrace();
				}
				return;
			}

			if (localname.compareTo("compositionRule")==0) {
				String targetClass=attr.getValue("class");
				if (targetClass!=null) {
					try {				
						Constructor newConstructor=Class.forName(targetClass).getConstructor(constructorParameterTypes);
						CompositionRule cr=(CompositionRule)newConstructor.newInstance(constructorParameters);
						cr.setIgnored(!(Boolean.parseBoolean(attr.getValue("isInitiallyEnabled"))));
						COMPOSITE_RULES.add(cr);
					} catch (Exception exc) {
						LOG.error("Unable to create specified rule in LayoutFactory: "+targetClass);
					}
				}
			}

			if (localname.compareTo("groupingRule")==0) {
				String targetClass=attr.getValue("class");
				if (targetClass!=null) {
					try {
						Constructor newConstructor=Class.forName(targetClass).getConstructor(constructorParameterTypes);
						GroupingRule gr=(GroupingRule)newConstructor.newInstance(constructorParameters);
						gr.setIgnored(!(Boolean.parseBoolean(attr.getValue("isInitiallyEnabled"))));
					  String ruleType=attr.getValue("type");
						if ((ruleType==null)||(ruleType.compareTo("series")==0)) SERIES_GROUPING_RULES.add(gr); else STUDY_GROUPING_RULES.add(gr);
					} catch (Exception exc) {
						LOG.error("Unable to create specified rule in LayoutFactory: "+targetClass);
					}
				}
			}

			if (localname.compareTo("rule")==0) {
				String ruleName=attr.getValue("name");
				String selectedLayout=attr.getValue("selectedLayout");
				lr=new LayoutRule(ruleName,selectedLayout);
				return;
			}
			if (localname.compareTo("logicalOp")==0) {
				if (llo!=null) opStack.push(llo);
				llo=new LayoutLogicalOp(attr.getValue("opName"));
			}
			if (localname.compareTo("conditional")==0) {
				String variable=attr.getValue("name");
				if (variable==null) return;
				Class c=ImageSequence.class;
				try {
					Field field=(c.getField(variable.toUpperCase()));
					variable=(String)field.get(c);
				} catch (Exception exc) {
					LOG.warn("Property \""+variable+"\" is not mapped to ImageSequenceProperties.");
					variable=attr.getValue("name");
				}
				if (variable==null) return;
				String comparator=attr.getValue("operation");
				int operator=LayoutRuleCondition.EQ;
				if (comparator!=null) {
					comparator.toLowerCase(); 
					if (comparator.compareTo("le")==0) operator=LayoutRuleCondition.LE; 
					else if (comparator.compareTo("lte")==0) operator=LayoutRuleCondition.LT; 
					else if (comparator.compareTo("ge")==0) operator=LayoutRuleCondition.GE; 
					else if (comparator.compareTo("gte")==0) operator=LayoutRuleCondition.GT; 
					else if (comparator.compareTo("ne")==0) operator=LayoutRuleCondition.NE; 				
					else if (comparator.compareTo("contains")==0) operator=LayoutRuleCondition.SUB; 				
				}
				Object value=attr.getValue("value");
				try {value=new Double((String)value);} catch (Exception exc) {}
				lrc=new LayoutRuleCondition(variable,operator,value);
				return;
			}
			if (localname.compareTo("defaultLayout")==0) {
				DEFAULT_LAYOUT=attr.getValue("name"); return;
			}
		}

		public void endElement(String uri, String localname, String qname) {

			if (localname.compareTo("rule")==0) {LAYOUT_RULES.add(lr); return;}

			if (localname.compareTo("logicalOp")==0) {
				if (!opStack.isEmpty()) {
					LayoutLogicalOp lastOp=opStack.pop();
					lastOp.addConditional(llo);
					llo=lastOp;
				} else {
					lr.addConditional(llo);
					llo=null;
				}
				return;
			}

			if (localname.compareTo("conditional")==0) {
				if (llo!=null) {
					llo.addConditional(lrc);
				} else {
					lr.addConditional(lrc);
				}
				lrc=null;
				return;
			}
		}
	}
}

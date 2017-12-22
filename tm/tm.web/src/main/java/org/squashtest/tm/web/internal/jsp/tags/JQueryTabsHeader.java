/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.jsp.tags;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

/**
 *
 * <p>That tag will handle the body and add additional css classes to any element that is part of a jquery tab. Example of the expected body is
 * the following (note which classes or id are used in which situations):</p>
 *
 *
 * <p>&lt; div class="fragment-tabs" &gt;</p>
 *
 * <p>&lt; ul class="tab-menu"&gt;</p>
 * <p>&lt; li &gt;</p>
 * <p>&lt; a href="#tab1"&gt;tab1 &lt; /a&gt;</p>
 * <p>&lt; /li &gt;</p>
 *
 * <p>&lt; li &gt;</p>
 * <p>&lt; a href=&gt;tab2 &lt; /a &gt;</p>
 * <p>&lt; /li &gt;</p>
 *
 * <p>&lt; a href="#tab2"/ul &gt;</p>
 *
 *
 * <p>&lt; div id="tab1" &gt; content 1 &lt; /div &gt;</p>
 *
 * <p>&lt; div id="tab2" &gt; content 2 &lt; /div &gt;</p>
 *
 *<p>&lt; /div &gt;</p>
 *
 *
 *
 *<p>Note that we got here an opening &lt;div&gt; but not the closing one. We only need to </p>
 *
 * <p>@author bsiri</p>
 *
 */
public class JQueryTabsHeader extends SimpleTagSupport {

	/* marker css classes */
	private static final String MAIN_DIV_FETCH_CLASS = "fragment-tabs";
	private static final String MAIN_MENU_FETCH_CLASS = "tab-menu";

	/* additional css */
	private static final String MAIN_DIV_ADDITIONAL_CLASSES = "ui-tabs ui-widget ui-widget-content ui-corner-all";
	private static final String MAIN_MENU_ADDITIONAL_CLASSES = "ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all";

	private static final String MAIN_MENUITEM_ADDITIONAL_CLASSES = "ui-state-default ui-corner-top";
	private static final String MAIN_MENUITEM_ACTIVE_ADD_CLASSES = " ui-tabs-active ui-state-active";
	private static final String MAIN_MENULINK_ADDITIONAL_CLASSES = "ui-tabs-anchor";

	private static final String CONTENT_ADDITIONAL_CLASSES = "ui-tabs-panel ui-widget-content ui-corner-bottom";

	/* pattern that helps perform the substitution */
	private static final Pattern CLASS_MATCHER = Pattern.compile("(class=\"[^\"]+)");
	private static final Pattern STYLE_MATCHER = Pattern.compile("(style=\"[^\"]+)");
	private static final Pattern TAG_MATCHER = Pattern.compile("(<\\w+)");


	/* misc strings to that SONAR keeps quiet about litterals */
	private static final String HREF_ATTRIBUTE = "href";
	private static final String LI_ELT = "li";
	private static final String A_ELT = "a";

	private static final String[] TAB_COOKIES = { "testcase-tab-cookie", "iteration-tab-cookie", "suite-tab-cookie" };

	/* attributes */
	private Collection<String> contentIds = new LinkedList<>();


	private Source source;
	private OutputDocument output;


	private int activeContentIndex=0; 	// UNUSED OR MISUSED for now, it'd require to process the cookies. The cookies themselves are malfunctionning.


	// UNUSED OR MISUSED for now, it'd require to process the cookies. The cookies themselves are malfunctionning
	public void setActiveContentIndex(int activeContentIndex) {
		this.activeContentIndex = activeContentIndex;
	}



	@Override
	public void doTag() throws JspException, IOException {

		JspFragment body = getJspBody();
		JspContext context = getJspContext();

		StringWriter writer = new StringWriter();

		body.invoke(writer);
		String strBody = writer.toString();

		source = new Source(strBody);
		output = new OutputDocument(source);

		modify();

		context.getOut().println(output.toString());

	}


	private void modify(){
		processMainDiv();
		processMainMenu();
		processContent();
	}


	private void processMainDiv(){
		List<Element> elements = source.getAllElementsByClass(MAIN_DIV_FETCH_CLASS);
		for (Element elt : elements){
			process(elt, MAIN_DIV_ADDITIONAL_CLASSES);
		}
	}

	private void processMainMenu(){
		List<Element> elements = source.getAllElementsByClass(MAIN_MENU_FETCH_CLASS);
		for (Element elt : elements){

			processMainMenuItems(elt);

			process(elt, MAIN_MENU_ADDITIONAL_CLASSES);

		}
	}

	private void processMainMenuItems(Element ulElt){

		List<Element> elements = ulElt.getAllElements(LI_ELT);
		int counter = 0;
		String css;

		for (Element elt : elements){

			css = MAIN_MENUITEM_ADDITIONAL_CLASSES;

			// UNUSED for now, it'd require to process the cookies. The cookies themselves are malfunctionning.
			css += counter == activeContentIndex ? MAIN_MENUITEM_ACTIVE_ADD_CLASSES : "";
			process(elt, css);
			counter++;
		}

		processMainLinks(ulElt);

	}

	private void processMainLinks(Element ulElt){
		List<Element> elements = ulElt.getAllElements(A_ELT);

		for (Element elt : elements){

			// find the elements the links are refering to, if they are actual elements (ie, not loaded by url)
			String href = elt.getStartTag().getAttributeValue(HREF_ATTRIBUTE);
			if (href.matches("^#.*")){
				contentIds.add(href.substring(1));
			}

			// change the css class.
			process(elt, MAIN_MENULINK_ADDITIONAL_CLASSES);

		}
	}


	private void processContent(){

		String css;
		boolean hide;
		int counter = 0;

		for (String id : contentIds){

			Element content = source.getElementById(id);

			// remember that non-active content has a special class
			css = CONTENT_ADDITIONAL_CLASSES;

			// MISUSED for now, it'd require to process the cookies. The cookies themselves are malfunctionning.
			hide = counter == activeContentIndex ? false : true;

			process(content, css, hide);

			counter++;
		}
	}


	// ************* mega internal **************

	private void process(Element elt, String additionalClasses){
		process(elt, additionalClasses, false);
	}



	private void process(Element elt, String additionalClasses, boolean hide){
		StartTag sTag = elt.getStartTag();
		String html = sTag.toString();

		String processed = processClasses(html, additionalClasses);

		if (hide){
			processed = processHide(processed);
		}

		output.replace(elt.getStartTag(), processed);
	}


	private String processClasses(String html, String additionalClasses){

		String processed;
		Matcher matcher = CLASS_MATCHER.matcher(html);

		if (matcher.find()){
			processed = matcher.replaceFirst("$1 "+additionalClasses);
		}
		else{
			processed = TAG_MATCHER.matcher(html).replaceFirst("$1 class=\""+additionalClasses+"\" ");
		}

		return processed;
	}

	private String processHide(String html){

		String processed;
		Matcher matcher = STYLE_MATCHER.matcher(html);

		if (matcher.find()){
			processed = matcher.replaceFirst("$1; display=none;");
		}
		else{
			processed = TAG_MATCHER.matcher(html).replaceFirst("$1 style=\"display:none;\"");
		}

		return processed;
	}

}

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
package org.squashtest.tm.jasperreports.processors;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument.RunElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.base.JRBasePrintHyperlink;
import net.sf.jasperreports.engine.type.HyperlinkTypeEnum;
import net.sf.jasperreports.engine.util.JEditorPaneHtmlMarkupProcessor;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.engine.util.JRStyledText.Run;
import net.sf.jasperreports.engine.util.JRStyledTextParser;
import net.sf.jasperreports.engine.util.JRTextAttribute;
import net.sf.jasperreports.engine.util.MarkupProcessor;
import net.sf.jasperreports.engine.util.MarkupProcessorFactory;


/**
 * As a solution to issue https://ci.squashtest.org/mantis/view.php?id=2293 this implementation
 * will handle &lt;strong&gt; and &lt;entityManager&gt; instead of their obsolete versions. This implementation must
 * be supplied in Jasper Report configuration, like
 * net.sf.jasperreports.markup.processor.factory.html=org.squashtest.tm.web.internal.controller.report.CustomHtmlProcessorFactory
 *
 * @author bsiri
 *
 */
public class CustomHtmlProcessorFactory extends JEditorPaneHtmlMarkupProcessor implements MarkupProcessorFactory {

	private static CustomHtmlProcessorFactory custom_instance;

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomHtmlProcessorFactory.class);


	@Override
	public MarkupProcessor createMarkupProcessor(){
		if (custom_instance == null)		{
			custom_instance = new CustomHtmlProcessorFactory();
		}
		return custom_instance;
	}


	//slightly scrapped from JEditorPanelHtmlMarkupProcessor
	@Override
	protected Map<Attribute,Object> getAttributes(AttributeSet attrSet){

		Map<Attribute, Object> attributes = super.getAttributes(attrSet);

		//checks for attributes WEIGHT and POSTURE. If they were not set, checks whether some HTML.Tag named "strong" of "entityManager" exists in the
		//attribute set.

		if (! attributes.containsKey(TextAttribute.WEIGHT) &&
		   hasHtmlTag(attrSet, HTML.Tag.STRONG)){
			attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		}

		if (! attributes.containsKey(TextAttribute.POSTURE) &&
				hasHtmlTag(attrSet, HTML.Tag.EM)){
			attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		}

		return attributes;
	}

	public boolean hasHtmlTag(AttributeSet attrSet, HTML.Tag tag){
		Enumeration<?> attrNames = attrSet.getAttributeNames();
		while (attrNames.hasMoreElements()){
			Object obj = attrNames.nextElement();
			if (tag.equals(obj)){
				return true;
			}
		}
		return false;
	}

	// NOSONAR:START
	// COPY PASTA FROM JEditorPaneHtmlMarkupProcessor to correct bug 2411
	@Override
	public String convert(String srcText)
	{
		JEditorPane editorPane = new JEditorPane("text/html", srcText);
		editorPane.setEditable(false);

		List<Element> elements = new ArrayList<>();

		Document document = editorPane.getDocument();

		Element root = document.getDefaultRootElement();
		if (root != null)
		{
			addElements(elements, root);
		}

		int startOffset = 0;
		int endOffset = 0;
		int crtOffset = 0;
		String chunk = null;
		JRPrintHyperlink hyperlink = null;
		Element element = null;
		Element parent;
		boolean bodyOccurred = false;
		int[] orderedListIndex = new int[elements.size()];
		String whitespace = "    ";
		String[] whitespaces = new String[elements.size()];
		for(int i = 0; i < elements.size(); i++)
		{
			whitespaces[i] = "";
		}
		JRStyledText styledText = new JRStyledText();

		for(int i = 0; i < elements.size(); i++)
		{
			if (bodyOccurred && chunk != null)
			{
				styledText.append(chunk);
				Map<Attribute,Object> styleAttributes = getAttributes(element.getAttributes());
				if (hyperlink != null)
				{
					styleAttributes.put(JRTextAttribute.HYPERLINK, hyperlink);
					hyperlink = null;
				}
				if (!styleAttributes.isEmpty())
				{
					styledText.addRun(new JRStyledText.Run(styleAttributes,
							startOffset + crtOffset, endOffset + crtOffset));
				}
			}

			chunk = null;
			element = elements.get(i);
			parent = element.getParentElement();
			startOffset = element.getStartOffset();
			endOffset = element.getEndOffset();
			AttributeSet attrs = element.getAttributes();

			Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
			Object object = elementName != null ? null : attrs.getAttribute(StyleConstants.NameAttribute);
			if (object instanceof HTML.Tag)
			{

				HTML.Tag htmlTag = (HTML.Tag) object;
				if(htmlTag == Tag.BODY)
				{
					bodyOccurred = true;
					crtOffset = - startOffset;
				}
				else if(htmlTag == Tag.BR)
				{
					chunk = "\n";
				}
				else if(htmlTag == Tag.OL)
				{
					orderedListIndex[i] = 0;
					String parentName = parent.getName().toLowerCase();
					whitespaces[i] = whitespaces[elements.indexOf(parent)] + whitespace;
					if("li".equals(parentName))
					{
						chunk = "";
					}
					else
					{
						chunk = "\n";
						++crtOffset;
					}
				}
				else if(htmlTag == Tag.UL)
				{
					whitespaces[i] = whitespaces[elements.indexOf(parent)] + whitespace;

					String parentName = parent.getName().toLowerCase();
					if("li".equals(parentName))
					{
						chunk = "";
					}
					else
					{
						chunk = "\n";
						++crtOffset;
					}

				}
				else if(htmlTag == Tag.LI)
				{

					whitespaces[i] = whitespaces[elements.indexOf(parent)];
					if(element.getElement(0) != null &&
 ("ol".equalsIgnoreCase((element.getElement(0).getName()))
							|| "ul".equalsIgnoreCase(element.getElement(0).getName()))
							)
					{
						chunk = "";
					}
					else if("ol".equals(parent.getName()))
					{
						int index = elements.indexOf(parent);
						chunk = whitespaces[index] + String.valueOf(++orderedListIndex[index]) + ".  ";
					}
					else
					{
						chunk = whitespaces[elements.indexOf(parent)] + "\u2022  ";
					}
					crtOffset += chunk.length();
				}
				else if (element instanceof LeafElement)
				{
					if (element instanceof RunElement)
					{
						RunElement runElement = (RunElement)element;
						AttributeSet attrSet = (AttributeSet)runElement.getAttribute(Tag.A);
						if (attrSet != null)
						{
							hyperlink = new JRBasePrintHyperlink();
							hyperlink.setHyperlinkType(HyperlinkTypeEnum.REFERENCE);
							hyperlink.setHyperlinkReference((String)attrSet.getAttribute(HTML.Attribute.HREF));
							hyperlink.setLinkTarget((String)attrSet.getAttribute(HTML.Attribute.TARGET));
						}
					}
					try
					{
						chunk = document.getText(startOffset, endOffset - startOffset);
					}
					catch(BadLocationException e)
					{
						if (LOGGER.isDebugEnabled())
						{
							LOGGER.debug("Error converting markup.", e);
						}
					}
				}
			}
		}

		if (chunk != null && !"\n".equals(chunk))
		{
			styledText.append(chunk);
			Map<Attribute,Object> styleAttributes = getAttributes(element.getAttributes());
			if (hyperlink != null)
			{
				styleAttributes.put(JRTextAttribute.HYPERLINK, hyperlink);
				hyperlink = null;
			}
			if (!styleAttributes.isEmpty())
			{
				styledText.addRun(new JRStyledText.Run(styleAttributes,
						startOffset + crtOffset, endOffset + crtOffset));
			}
		}

		styledText.setGlobalAttributes(new HashMap<Attribute,Object>());
		// FIX FOR [Issue 2411]
		List<Run> runs = styledText.getRuns();
		for(Run run : runs){
			if (run.endIndex > styledText.length()){
				run.endIndex = styledText.length();
			}

		}
		// END FIX FOR  [Issue 2411]
		return JRStyledTextParser.getInstance().write(styledText);
	}
	// NOSONAR:END
	// END COPY PASTA FROM JEditorPaneHtmlMarkupProcessor to correct bug 2411
}

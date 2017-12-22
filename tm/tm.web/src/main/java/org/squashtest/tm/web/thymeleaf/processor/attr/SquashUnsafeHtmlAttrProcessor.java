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
package org.squashtest.tm.web.thymeleaf.processor.attr;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.exceptions.TemplateEngineException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.attr.AbstractChildrenModifierAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;
import org.thymeleaf.templatemode.ITemplateModeHandler;

/**
 * This processor processes "unsafe-html" attributes. The attribute value is expected to be a potentially unbalanced
 * html fragment. This processor uses the LEGACYHTML5 parser to balance the html fragment and then replace this
 * element's inner html by this balanced html fragment.
 *
 * @author Gregory Fouquet
 *
 */
public final class SquashUnsafeHtmlAttrProcessor extends AbstractChildrenModifierAttrProcessor implements IProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(SquashUnsafeHtmlAttrProcessor.class);

	/**
	 * @param attribute
	 */
	public SquashUnsafeHtmlAttrProcessor() {
		super("unsafe-html");
	}

	/**
	 * Returns the html attribute of the processed argument parsed using the legacy html5 (tag balancing) parser.
	 *
	 * @see org.thymeleaf.processor.attr.AbstractTextChildModifierAttrProcessor#getText(org.thymeleaf.Arguments,
	 *      org.thymeleaf.dom.Element, java.lang.String)
	 */
	@Override
	protected List<Node> getModifiedChildren(final Arguments arguments, final Element element,
			final String attributeName) {
		final String attributeValue = element.getAttributeValue(attributeName);
		LOGGER.trace("Will process attribute value {} of element {}", attributeValue, element);

		final Object fragment = StandardExpressionProcessor.processExpression(arguments, attributeValue);


		try {
			final Configuration configuration = arguments.getConfiguration();
			final ITemplateModeHandler templateModeHandler = configuration.getTemplateModeHandler("LEGACYHTML5");

			String string = fragment == null ? "" : fragment.toString();
			List<Node> parsedFragment = templateModeHandler.getTemplateParser().parseFragment(configuration,
					string);

			// we cannot lookup the template repository because it is backed by the main template parser. yet the
			// fragment should change quite frequently.
			for (final Node node : parsedFragment) {
				node.setProcessable(false);
			}

			return parsedFragment;

		} catch (final TemplateEngineException e) {
			throw e; // NOSONAR we wanna catch any exception but TemplateEngineException
		} catch (final Exception e) {
			throw new TemplateProcessingException("An error happened during parsing of unsafe html: \""
					+ element.getAttributeValue(attributeName) + "\"", e);
		}

	}

	/**
	 * @see org.thymeleaf.processor.AbstractProcessor#getPrecedence()
	 */
	@Override
	public int getPrecedence() {
		// less precedence than StandardTextAttrProcessor so, if both attrs exist, this one will be the last applied
		return 1000;
	}
}

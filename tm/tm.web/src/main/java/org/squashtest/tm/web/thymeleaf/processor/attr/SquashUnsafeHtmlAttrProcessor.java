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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.model.IElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.*;


/**
 * This processor processes "unsafe-html" attributes. The attribute value is expected to be a potentially unbalanced
 * html fragment. This processor uses the LEGACYHTML5 parser to balance the html fragment and then replace this
 * element's inner html by this balanced html fragment.
 *
 * @author Gregory Fouquet
 */

/*
 * Squash 18 :
 *
 * LEGACYHTML5 processor is deprecated, perhaps this class has no purpose now.
 * If still must be implemented, look at StandardUtextTagProcessor.
 *
 */
public final class SquashUnsafeHtmlAttrProcessor extends AbstractAttributeTagProcessor implements IElementTagProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SquashUnsafeHtmlAttrProcessor.class);

	// a couple of mnemonics to clarify the semantic on the constructor parameters
	private static final String ATTRIBUTE_NAME = "unsafe-html";
	private static final int PRECEDENCE = 1200;

	/**
	 * @param dialectPrefix the dialect prefix
	 */
	public SquashUnsafeHtmlAttrProcessor(String dialectPrefix) {
		super(TemplateMode.HTML,
			dialectPrefix,
			MATCH_ANY_TAG,
			NO_TAG_PREFIX,
			ATTRIBUTE_NAME,
			REQUIRE_BOTH_DIALECT_PREFIX_AND_ATTRIBUTE,
			PRECEDENCE,
			REMOVE_PSEUDO_ATTRIBUTE_WHEN_PROCESSED);
	}


	// TODO: noop ?
	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName,
							 String attributeValue, IElementTagStructureHandler structureHandler) {

		LOGGER.trace("Will process attribute value {} of element {}", attributeValue, tag);
/*
		final IEngineConfiguration configuration = context.getConfiguration();
		final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
		final IStandardExpression expression = parser.parseExpression(context, attributeValue);

		final Object html = expression.execute(context);
		final String htmlString = html == null ? "" : html.toString();

		try{
			final TemplateModel parsed = configuration.getTemplateManager().parseString(
				context.getTemplateData(),
				htmlString,
				0,
				0,
				null,
				false
			);

			configuration.getDocTypeProcessors(TemplateMode.HTML)
		}
*/

		structureHandler.setBody(attributeValue, false);
	}

	/**
	 * Returns the html attribute of the processed argument parsed using the legacy html5 (tag balancing) parser.
	 */

	/*
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
*/
}

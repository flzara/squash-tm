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
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.TemplateModel;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import static org.squashtest.tm.web.internal.util.HTMLCleanupUtils.cleanAndUnescapeHTML;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.MATCH_ANY_TAG;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.NO_TAG_PREFIX;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.REMOVE_PSEUDO_ATTRIBUTE_WHEN_PROCESSED;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.REQUIRE_BOTH_DIALECT_PREFIX_AND_ATTRIBUTE;



/**
 * <p>This processor ensures that the given html will be cleaned of harmful content.</p>
 *
 *  <p>
 *  	Note that this class was repurposed after migration to Thymeleaf 3. Its prior use was to
 *  make Thymeleaf accept malformed html (it would crash otherwise, for instance with unbalanced tags).
 *  This is no longer necessary since the legacy html 5 parser (which is now {@link TemplateMode#LEGACYHTML5})
 *  is now the default for html content.
 *  </p>
 *
 */
/*
 * (History comment)
 *
 * This processor processes "unsafe-html" attributes. The attribute value is expected to be a potentially unbalanced
 * html fragment. This processor uses the LEGACYHTML5 parser to balance the html fragment and then replace this
 * element's inner html by this balanced html fragment.
 *
 * @author Gregory Fouquet
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

		final IEngineConfiguration configuration = context.getConfiguration();
		final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
		final IStandardExpression expression = parser.parseExpression(context, attributeValue);

		final Object html = expression.execute(context);
		/*[Issue 7478] Use of cleanAndUnescapeHTML method instead of cleanHtml because of non html markup reading in
		test-step display during step modification at execution.
		Doesn't seem to break other use of sq:unsafe-html tag...
		 */
		final String htmlString = html == null ? "" : cleanAndUnescapeHTML(html.toString());


		final TemplateModel parsed = configuration.getTemplateManager().parseString(
			context.getTemplateData(),
			htmlString,
			0,
			0,
			null,
			false
		);

		structureHandler.setBody(parsed, false);




	}

}

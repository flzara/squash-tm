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
import org.squashtest.tm.web.internal.context.ServletContextParameters;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.MATCH_ANY_TAG;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.NO_TAG_PREFIX;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.REMOVE_PSEUDO_ATTRIBUTE_WHEN_PROCESSED;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.REQUIRE_BOTH_DIALECT_PREFIX_AND_ATTRIBUTE;

/**
 * Processes <code>sq:css</code> attributes. Attribute value is expected to be the unqualified name of a stylesheet
 * (e.g. <code>squash.core.css</code>). The processor reads this value and substitutes the <code>sq:css</code> attribute
 * with a <code>href</code> pointing to the stylesheet.
 *
 * @author Gregory Fouquet
 *
 */
public class SquashCssAttrProcessor extends AbstractAttributeTagProcessor implements IElementTagProcessor{
	private static final Logger LOGGER = LoggerFactory.getLogger(SquashCssAttrProcessor.class);


	private static final String SQ_CSS = "css";
	private static final String HREF = "href";
	private static final int PRECEDENCE = 1300;

	/**
	 * Partial path starting with a '/' and matching the current squash tm version.
	 */
	private String versionPathToken;

	/**
	 * Creates a processor for the <code>css</code> attribute
	 */
	public SquashCssAttrProcessor(String dialectPrefix) {
		super(
			TemplateMode.HTML,
			dialectPrefix,
			MATCH_ANY_TAG,
			NO_TAG_PREFIX,
			SQ_CSS,
			REQUIRE_BOTH_DIALECT_PREFIX_AND_ATTRIBUTE,
			PRECEDENCE,
			REMOVE_PSEUDO_ATTRIBUTE_WHEN_PROCESSED
		);
	}


	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {

		// resolve the URL
		final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(context.getConfiguration());

		final String cssUrlExpression = "@{" + attributeValue + "}";
		LOGGER.debug("Stylesheet named '{}' will be resolved using the expression '{}'", attributeValue,
			cssUrlExpression);

		final String toEvaluate = "@{/styles" + getVersionPathToken(context) + '/' + attributeValue + "}";
		final IStandardExpression expression = parser.parseExpression(context, toEvaluate);
		final String evaluatedUrl = (String)expression.execute(context);

		LOGGER.trace("Stylesheet resolved to url '{}'", evaluatedUrl);

		// now set the href with it
		structureHandler.setAttribute(HREF, evaluatedUrl);

	}

	/**
	 * @return
	 */
	private String getVersionPathToken(IContext context) {
		if (versionPathToken == null) {
			initSquashVersion(context);
		}
		return versionPathToken;
	}

	private void initSquashVersion(IContext context) {
		if (versionPathToken == null && context instanceof IWebContext) {
			IWebContext webContext = (IWebContext) context;
			String squashVersion = webContext.getServletContext().getInitParameter(ServletContextParameters.SQUASH_TM_VERSION);

			versionPathToken = squashVersion != null ? '-' + squashVersion : "";
		}
	}


}

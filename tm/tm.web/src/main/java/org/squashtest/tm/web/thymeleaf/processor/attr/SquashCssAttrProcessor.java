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
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractSingleAttributeModifierAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;
import org.thymeleaf.util.PrefixUtils;

/**
 * Processes <code>sq:css</code> attributes. Attribute value is expected to be the unqualified name of a stylesheet
 * (e.g. <code>squash.core.css</code>). The processor reads this value and substitutes the <code>sq:css</code> attribute
 * with a <code>href</code> pointing to the stylesheet.
 *
 * @author Gregory Fouquet
 *
 */
public class SquashCssAttrProcessor extends AbstractSingleAttributeModifierAttrProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(SquashCssAttrProcessor.class);

	/**
	 * Partial path starting with a '/' and matching the current squash tm version.
	 */
	private String versionPathToken;

	/**
	 * Creates a priocessor for the <code>css</code> attribute
	 */
	public SquashCssAttrProcessor() {
		super("css");
	}

	/**
	 * @see org.thymeleaf.processor.attr.AbstractSingleAttributeModifierAttrProcessor#getTargetAttributeName(org.thymeleaf.Arguments,
	 *      org.thymeleaf.dom.Element, java.lang.String)
	 */
	@Override
	protected String getTargetAttributeName(Arguments arguments, Element element, String attributeName) {
		return PrefixUtils.getUnprefixed("href");
	}

	/**
	 * @see org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor#getModificationType(org.thymeleaf.Arguments,
	 *      org.thymeleaf.dom.Element, java.lang.String, java.lang.String)
	 */
	@Override
	protected ModificationType getModificationType(Arguments arguments, Element element, String attributeName,
	                                               String newAttributeName) {
		return ModificationType.SUBSTITUTION;
	}

	/**
	 * @see org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor#removeAttributeIfEmpty(org.thymeleaf.Arguments,
	 *      org.thymeleaf.dom.Element, java.lang.String, java.lang.String)
	 */
	@Override
	protected boolean removeAttributeIfEmpty(Arguments arguments, Element element, String attributeName,
	                                         String newAttributeName) {
		return false;
	}

	/**
	 * @see org.thymeleaf.processor.AbstractProcessor#getPrecedence()
	 */
	@Override
	public int getPrecedence() {
		// after StandardSingleNonRemovableAttributeModifierAttrProcessor
		return 1300;
	}

	/**
	 * @see org.thymeleaf.processor.attr.AbstractSingleAttributeModifierAttrProcessor#getTargetAttributeValue(org.thymeleaf.Arguments,
	 *      org.thymeleaf.dom.Element, java.lang.String)
	 */
	@Override
	protected String getTargetAttributeValue(Arguments arguments, Element element, String attributeName) {
		final String attributeValue = element.getAttributeValue(attributeName);

		IContext context = arguments.getContext();
		initSquashVersion(context);

		final String cssUrlExpression = "@{" + attributeValue + "}";
		LOGGER.debug("Stylesheet named '{}' will be resolved using the expression '{}'", attributeValue,
			cssUrlExpression);

		final Object result = StandardExpressionProcessor.processExpression(arguments, "@{/styles"
			+ getVersionPathToken(context) + '/' + attributeValue + "}");
		LOGGER.trace("Stylesheet resolved to url '{}'", result);

		return result == null ? "" : result.toString();
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

	/**
	 * @see org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor#recomputeProcessorsAfterExecution(org.thymeleaf.Arguments,
	 *      org.thymeleaf.dom.Element, java.lang.String)
	 */
	@Override
	protected boolean recomputeProcessorsAfterExecution(Arguments arguments, Element element, String attributeName) {
		return false;
	}

}

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
/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License")
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.squashtest.tm.web.thymeleaf.processor.attr;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.DateUtils;
import static org.squashtest.tm.web.thymeleaf.processor.attr.Constants.*;

/**
 * Processes <code>sq:ts</code> attributes. The attribute value has to be an expression which resolves to
 * <code>java.util.Date</code>. The content of this element is replaced by the corresponding ISO 8601 timestamp
 *
 * This processor takes precedence over <code>th:text</code>
 *
 *
 * @author Gregory Fouquet
 *
 */
public class SquashIso8601DateAttrProcessor extends AbstractAttributeTagProcessor implements IElementTagProcessor {

	private static final int PRECEDENCE = 1000;
	private static final String SQ_TIMESTAMP = "iso-date";

	public SquashIso8601DateAttrProcessor(String dialectPrefix) {
		super(
			TemplateMode.HTML,
			dialectPrefix,
			MATCH_ANY_TAG,
			NO_TAG_PREFIX,
			SQ_TIMESTAMP,
			REQUIRE_BOTH_DIALECT_PREFIX_AND_ATTRIBUTE,
			PRECEDENCE,
			REMOVE_PSEUDO_ATTRIBUTE_WHEN_PROCESSED
		);
	}


	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {

		final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(context.getConfiguration());
		final IStandardExpression expression = parser.parseExpression(context, attributeValue);
		final Object parsed = expression.execute(context);

		String asString = parsed == null ? "" : DateUtils.formatISO(parsed);

		structureHandler.setBody(asString, false);

	}

}

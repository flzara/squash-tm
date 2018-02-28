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
package org.squashtest.tm.web.thymeleaf.dialect;

import java.util.HashSet;
import java.util.Set;


import org.squashtest.tm.annotation.WebComponent;
import org.squashtest.tm.web.thymeleaf.processor.attr.SquashCssAttrProcessor;
import org.squashtest.tm.web.thymeleaf.processor.attr.SquashIso8601DateAttrProcessor;
import org.squashtest.tm.web.thymeleaf.processor.attr.SquashUnsafeHtmlAttrProcessor;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.processor.IProcessor;

import javax.servlet.ServletContext;

/**
 * Squash dialect for Thmymeleaf
 *
 * @author Gregory Fouquet
 * @author bsiri
 *
 */
@WebComponent("thymeleaf.dialect.squashdialect")
public class SquashDialect extends AbstractProcessorDialect implements IProcessorDialect, IExpressionObjectDialect{

	public static final String SQUASH_DIALECT = "SquashDialect";
	public static final String PREFIX = "sq";
	public static final int PRECEDENCE = 1200;
	private static final ExpressionObjectFactory EXPRESSION_OBJECT_FACTORY = new ExpressionObjectFactory();

	public SquashDialect() {
		super(SQUASH_DIALECT, PREFIX, PRECEDENCE);
	}


	/**
	 * @see org.thymeleaf.dialect.IProcessorDialect#getProcessors(String)
	 */
	@Override
	public Set<IProcessor> getProcessors(String dialectPrefix) {
		Set<IProcessor> processors = new HashSet<>(3);
		// TODO : is that one obsolete ?
		processors.add(new SquashUnsafeHtmlAttrProcessor(dialectPrefix));

		processors.add(new SquashCssAttrProcessor(dialectPrefix));
		processors.add(new SquashIso8601DateAttrProcessor(dialectPrefix));
		return processors;
	}

	@Override
	public IExpressionObjectFactory getExpressionObjectFactory() {
		return EXPRESSION_OBJECT_FACTORY;
	}




	private static final class ExpressionObjectFactory implements IExpressionObjectFactory{

		private static final Set<String> HELPERS;
		static {
			HELPERS = new HashSet<>();
			HELPERS.add(WorkspaceHelper.HELPER_NAME);
		}

		@Override
		public Set<String> getAllExpressionObjectNames() {
			return HELPERS;
		}


		/*
			1/ Partly ripped from SpringSecurityDialect
		 	
		 	2/ For now, no test on the expressionObjectName because we only have "workspace"
		*/
		@Override
		public Object buildObject(IExpressionContext context, String expressionObjectName) {

			Object result = null;

			if (context instanceof IWebContext){
				IWebContext webContext = (IWebContext)context;
				ServletContext servletContext = webContext.getServletContext();
				result = new WorkspaceHelper(servletContext);
			}

			return result;
		}

		/*
		 For now, no test on the expressionObjectName because we only have "workspace"
		 */
		@Override
		public boolean isCacheable(String expressionObjectName) {
			return false;
		}
	}


}

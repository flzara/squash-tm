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
package org.squashtest.tm.web.internal.exceptionresolver;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.squashtest.tm.core.foundation.exception.ActionException;
import org.squashtest.tm.exception.testcase.ScriptParsingException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * This handler will format ActionExceptions and subclasses in order to raise a popup clientside and display an
 * exception. This is a complementary system to @HandlerDomainExceptionResolver. The difference here is that in this
 * case the treatment client-side will open a generic popup and display the error text in it.
 *
 * @author bsiri
 * @reviewed-on 2011-12-15
 */

@Component
public class HandlerActionExceptionResolver extends AbstractHandlerExceptionResolver {
	@Inject
	private MessageSource messageSource;

	public HandlerActionExceptionResolver() {
		super();
	}

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {

		if (exceptionIsHandled(ex)) {
			return handleException(request, response, ex);
		}

		return null;
	}

	private ModelAndView handleException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
		ScriptParsingException parsingException = (ScriptParsingException) ex; // NOSONAR Type was checked earlier
		if (ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.APPLICATION_JSON) || ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.ANYTHING)) {
			return formatJsonResponse(response, parsingException, request.getLocale());
		}

		else if (ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.TEXT_PLAIN)) {
			return formatPlainTextResponse(response, parsingException, request.getLocale());

		}

		return null;
	}

	private ModelAndView formatPlainTextResponse(HttpServletResponse response, ScriptParsingException parsingException, Locale locale) {
		response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
		String exception = parsingException.getClass().getSimpleName();
		String message = getLocalizedMessage(locale, parsingException);
		String error = exception + ':' + message;

		AbstractView view = new PlainTextView();

		return new ModelAndView(view, "actionValidationError", error);
	}

	private ModelAndView formatJsonResponse(HttpServletResponse response, ScriptParsingException parsingException, Locale locale) {
		response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
		String exception = parsingException.getClass().getSimpleName();
		String message = getLocalizedMessage(locale, parsingException);
		ActionValidationErrorModel error = new ActionValidationErrorModel(exception, message);
		return new ModelAndView(new MappingJackson2JsonView(), "actionValidationError", error);
	}

	private String getLocalizedMessage(Locale locale, ScriptParsingException parsingException) {
		String message = messageSource.getMessage("squashtm.action.exception.testcase.scripted.parsing", null, locale);
		message = message + "\n\n" + parsingException.getCause().getMessage();
		return message.replace("\n", "<br/>");
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ScriptParsingException.class.isAssignableFrom(ex.getClass());
	}



	/* **************** inner class ***************** */
	private static class PlainTextView extends AbstractView {

		@Override
		protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			for (Object obj : model.values()) {
				response.getOutputStream().write(obj.toString().getBytes());
				response.getOutputStream().write('\n');
			}

		}
	};
}

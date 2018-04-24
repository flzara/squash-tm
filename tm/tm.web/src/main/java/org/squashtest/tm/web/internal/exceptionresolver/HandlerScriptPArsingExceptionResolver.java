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
public class HandlerScriptParsingExceptionResolver extends AbstractHandlerExceptionResolver {
	@Inject
	private MessageSource messageSource;

	public HandlerScriptParsingExceptionResolver() {
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
		ActionException actionEx = (ActionException) ex; // NOSONAR Type was checked earlier
		if (ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.APPLICATION_JSON) || ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.ANYTHING)) {
			return formatJsonResponse(response, actionEx, request.getLocale());
		}

		else if (ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.TEXT_PLAIN)) {
			return formatPlainTextResponse(response, actionEx, request.getLocale());

		}

		return null;
	}

	private ModelAndView formatPlainTextResponse(HttpServletResponse response, ActionException actionEx, Locale locale) {
		response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
		String exception = actionEx.getClass().getSimpleName();
		String message = getLocalizedMessage(locale, actionEx);
		String error = exception + ':' + message;

		AbstractView view = new PlainTextView();

		return new ModelAndView(view, "actionValidationError", error);
	}

	private ModelAndView formatJsonResponse(HttpServletResponse response, ActionException actionEx, Locale locale) {
		response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
		String exception = actionEx.getClass().getSimpleName();
		String message = getLocalizedMessage(locale, actionEx);
		ActionValidationErrorModel error = new ActionValidationErrorModel(exception, message);
		return new ModelAndView(new MappingJackson2JsonView(), "actionValidationError", error);
	}

	private String getLocalizedMessage(Locale locale, ActionException actionEx) {
		String key = actionEx.getI18nKey();
		String message = null;
		if(key == null  && actionEx.getMessage() != null && !actionEx.getMessage().isEmpty()){
			message = actionEx.getMessage();
		}
		else if(key != null){
			message = messageSource.getMessage(key, actionEx.messageArgs(), locale);
		}
		if(message == null){
			message = messageSource.getMessage("error.generic.label", null, locale);
		}
		return message;
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ActionException.class.isAssignableFrom(ex.getClass());
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

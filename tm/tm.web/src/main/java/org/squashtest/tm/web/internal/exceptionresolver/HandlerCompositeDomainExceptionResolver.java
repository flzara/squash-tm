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
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.squashtest.tm.exception.CompositeDomainException;
import org.squashtest.tm.exception.DomainException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class HandlerCompositeDomainExceptionResolver extends AbstractHandlerExceptionResolver {

	@Inject
	private MessageSource messageSource;

	public HandlerCompositeDomainExceptionResolver() {
		super();
	}

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {

		if (exceptionIsHandled(ex) && ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.APPLICATION_JSON)) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

			CompositeDomainException cdex = (CompositeDomainException) ex; // NOSONAR Type was checked earlier
			List<FieldValidationErrorModel> errors = buildFieldValidationErrors(cdex, request.getLocale());

			return new ModelAndView(new MappingJackson2JsonView(), "fieldValidationErrors", errors);
		}

		return null;
	}

	private List<FieldValidationErrorModel> buildFieldValidationErrors(CompositeDomainException cdex, Locale locale) {
		List<FieldValidationErrorModel> ves = new ArrayList<>();
		for (DomainException dex : cdex.getExceptions()) {
			String message = dex.getMessage();
			if (!dex.getI18nKey().isEmpty()) {
				message = messageSource.getMessage(dex.getI18nKey(), dex.getI18nParams(), locale);
			}

			ves.add(new FieldValidationErrorModel(dex.getObjectName(), dex.getField(), message, dex.getFieldValue()));
		}
		return ves;
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof CompositeDomainException;
	}

}

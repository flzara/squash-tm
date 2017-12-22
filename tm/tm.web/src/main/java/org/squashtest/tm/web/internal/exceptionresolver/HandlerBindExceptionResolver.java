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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * This {@link HandlerExceptionResolver} proccesses {@link BindException}s and resolves them to a JSON representation.
 * If the client does not accept Json respnoses (ie no "application/json" in the "Accept" header), this resolver does
 * nothing.
 *
 * The JSON is : {fieldValidationErrors: [{fieldName: "<field name>", errorMessage: "<localized validation message>"}]}
 *
 * When instanciated, this resolver should automatically be referenced by Spring's {@link DispatcherServlet}
 *
 * @author Gregory Fouquet
 *
 */
@ControllerAdvice
public class HandlerBindExceptionResolver extends AbstractHandlerExceptionResolver {


	public HandlerBindExceptionResolver() {
		super();
	}

	@Override
	@ExceptionHandler(value = {BindException.class})
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		if (exceptionIsHandled(ex) && ExceptionResolverUtils.clientAcceptsMIMEOrAnything(request, MimeType.APPLICATION_JSON)) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

			BindException bex = (BindException) ex; // NOSONAR Type was checked earlier
			List<FieldValidationErrorModel> errors = buildFieldValidationErrors(bex);

			return new ModelAndView(new MappingJackson2JsonView(), "fieldValidationErrors", errors);
		}

		return null;
	}

	private List<FieldValidationErrorModel> buildFieldValidationErrors(BindException bex) {
		List<FieldValidationErrorModel> ves = new ArrayList<>();
		List<FieldError> oes = bex.getFieldErrors();

		for (FieldError oe : oes) {
			ves.add(new FieldValidationErrorModel(oe.getObjectName(), oe.getField(), oe.getDefaultMessage()));
		}
		return ves;
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof BindException;
	}

}

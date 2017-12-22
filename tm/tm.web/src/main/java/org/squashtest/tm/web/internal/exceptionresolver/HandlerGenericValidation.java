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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.squashtest.tm.web.internal.http.RequestHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;

@Component
public class HandlerGenericValidation extends AbstractHandlerExceptionResolver {
	private final List<ConstraintViolationHandler> constraintViolationHandlers = new ArrayList<>();


	public HandlerGenericValidation() {
		super();
		constraintViolationHandlers.add(new HasDefaultAsRequiredViolationHandler());
		constraintViolationHandlers.add(new PropertyPathConstraintViolationHandler());
	}

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		if (exceptionIsHandled(ex) && clientAcceptsJson(request)) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

			ConstraintViolationException cve = (ConstraintViolationException) ex; // NOSONAR Type was checked earlier
			List<FieldValidationErrorModel> errors = buildFieldValidationErrors(cve);

			return new ModelAndView(new MappingJackson2JsonView(), "fieldValidationErrors", errors);
		}

		return null;
	}

	private List<FieldValidationErrorModel> buildFieldValidationErrors(ConstraintViolationException cve) {
		List<FieldValidationErrorModel> ves = new ArrayList<>();

		Set<ConstraintViolation<?>> constraintList = cve.getConstraintViolations();

		for (ConstraintViolation<?> aConstraintList : constraintList) {
			addFieldValidationError(aConstraintList, ves);
		}

		return ves;
	}

	private void addFieldValidationError(ConstraintViolation<?> violation, List<FieldValidationErrorModel> ves) {
		for (ConstraintViolationHandler handler : constraintViolationHandlers) {
			if (handler.handle(violation, ves)) {
				break;
			}
		}
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof ConstraintViolationException;
	}

	private boolean clientAcceptsJson(HttpServletRequest request) {
		Enumeration<String> e = request.getHeaders(RequestHeaders.ACCEPT);

		while (e.hasMoreElements()) {
			String header = e.nextElement();
			if (StringUtils.containsIgnoreCase(StringUtils.trimToEmpty(header), MimeType.ANYTHING.requestHeaderValue()) || StringUtils.containsIgnoreCase(StringUtils.trimToEmpty(header), MimeType.APPLICATION_JSON.requestHeaderValue())) {
				return true;
			}
		}
		return false;
	}

}

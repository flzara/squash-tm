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

import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by jthebault on 13/06/2016.
 */
@ControllerAdvice
public class HandlerTransactionSystemExceptionResolver extends AbstractHandlerExceptionResolver {

	@Override
	@ExceptionHandler(value = {TransactionSystemException.class})
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (exceptionIsHandled(ex) && ExceptionResolverUtils.clientAcceptsMIMEOrAnything(request, MimeType.APPLICATION_JSON)) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

			ConstraintViolationException bex = (ConstraintViolationException) ex.getCause().getCause(); // NOSONAR Type was checked earlier
			List<FieldValidationErrorModel> errors = buildFieldValidationErrors(bex);

			return new ModelAndView(new MappingJackson2JsonView(), "fieldValidationErrors", errors);
		}
		return null;
	}

	private List<FieldValidationErrorModel> buildFieldValidationErrors(ConstraintViolationException cve) {
		List<FieldValidationErrorModel> ves = new ArrayList<>();
		Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();

		for (ConstraintViolation violation : violations) {
			ves.add(new FieldValidationErrorModel(violation.getPropertyPath().toString(), violation.getPropertyPath().toString(), violation.getMessage()));
		}
		return ves;
	}

	private boolean exceptionIsHandled(Exception ex) {
		Throwable rootCause = ex.getCause() != null && ex.getCause().getCause() != null ? ex.getCause().getCause() : null;
		if (rootCause == null) {
			return false;
		}

		return rootCause instanceof ConstraintViolationException;
	}
}

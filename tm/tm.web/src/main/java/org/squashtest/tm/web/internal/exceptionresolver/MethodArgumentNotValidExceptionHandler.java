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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

/**
 * This class handle MethodArgumentNotValidException and produce a more
 * convenient http response than the spring default "400 - Bad request"
 * This MethodArgumentNotValidException is threw by spring when a validation
 *  fail on @RequestBody @Valid verification
 * @author jthebault
 *
 */
@Component
public class MethodArgumentNotValidExceptionHandler extends
		AbstractHandlerExceptionResolver {


	public MethodArgumentNotValidExceptionHandler() {
		super();
		//Setting order property to have this handler be placed before DefaultExceptionHandler in spring exception queue resolution
		this.setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		//only handle if ex is a MethodArgumentNotValidException
		if (exceptionIsHandled(ex)) {
			return handleException(request,response,ex);
		}
		return null;
	}

	private ModelAndView handleException(HttpServletRequest request, HttpServletResponse response, Exception ex){
		MethodArgumentNotValidException invalidAgrumentEx = (MethodArgumentNotValidException) ex; // NOSONAR Type was checked earlier
		response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
		List<FieldValidationErrorModel> errors = buildFieldValidationErrors(invalidAgrumentEx);

		return new ModelAndView(new MappingJackson2JsonView(), "fieldValidationErrors", errors);
	}

	private List<FieldValidationErrorModel> buildFieldValidationErrors(MethodArgumentNotValidException invalidAgrumentEx) {
		List<FieldValidationErrorModel> ves = new ArrayList<>();
		List<FieldError> oes = invalidAgrumentEx.getBindingResult().getFieldErrors();

		for (FieldError oe : oes) {
			ves.add(new FieldValidationErrorModel(oe.getObjectName(), oe.getField(), oe.getDefaultMessage()));
		}
		return ves;
	}

	private boolean exceptionIsHandled(Exception ex) {
		// return ex instanceof MethodArgumentNotValidException
		return MethodArgumentNotValidException.class.isAssignableFrom(ex.getClass());
	}

}

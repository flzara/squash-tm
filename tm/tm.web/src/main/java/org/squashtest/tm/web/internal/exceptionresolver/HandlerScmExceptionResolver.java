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

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.squashtest.tm.core.scm.api.exception.ScmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class HandlerScmExceptionResolver extends AbstractHandlerExceptionResolver {

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) {
		if(exceptionIsHandled(ex)) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
			ScmException scmException = (ScmException) ex;

			if(scmException.hasField()) {
				// exception occurred because of a specific field, this happends while creation/preparation
				List<FieldValidationErrorModel> errors = buildFieldValidationErrors(scmException);
				return new ModelAndView(new MappingJackson2JsonView(), "fieldValidationErrors", errors);
			} else {
				// exception occurred in a general context
				ActionValidationErrorModel model =
					new ActionValidationErrorModel(scmException.getClass().getSimpleName(), scmException.getMessage());
				return new ModelAndView(new MappingJackson2JsonView(), "actionValidationError", model);
			}
		}
		return null;
	}

	private List<FieldValidationErrorModel> buildFieldValidationErrors(ScmException scmException) {
		List<FieldValidationErrorModel> ves = new ArrayList<>();
		ves.add(new FieldValidationErrorModel("", scmException.getField(), scmException.getMessage()));
		return ves;
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof ScmException;
	}
}

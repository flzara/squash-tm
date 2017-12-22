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

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

// TODO Append something after Exception in class name
@Component
public class HandlerBugTrackerRemoteExceptionResolver extends AbstractHandlerExceptionResolver {


	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		
		// must not check the MIME type : BugTrackerRemoteException are always serialized as JSON
		if (exceptionIsHandled(ex)/* && ExceptionResolverUtils.clientAcceptsMIME(request, MimeType.APPLICATION_JSON)*/) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

			BugTrackerRemoteException remoteException = (BugTrackerRemoteException) ex; // NOSONAR Type was previously
																						// checked
			List<FieldValidationErrorModel> errors = buildFieldValidationErrors(remoteException);

			return new ModelAndView(new MappingJackson2JsonView(), "fieldValidationErrors", errors);
		}

		return null;
	}

	private List<FieldValidationErrorModel> buildFieldValidationErrors(BugTrackerRemoteException remoteException) {
		List<FieldValidationErrorModel> ves = new ArrayList<>();

		ves.add(new FieldValidationErrorModel("", "bugtracker", remoteException.getMessage()));

		return ves;
	}

	private boolean exceptionIsHandled(Exception ex) {
		return ex instanceof BugTrackerRemoteException;
	}


}

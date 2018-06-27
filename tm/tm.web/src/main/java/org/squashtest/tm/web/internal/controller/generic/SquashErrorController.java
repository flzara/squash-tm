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
package org.squashtest.tm.web.internal.controller.generic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.squashtest.tm.service.feature.FeatureManager;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.stream.Collectors;

/*
 *https://gist.github.com/jonikarppinen/662c38fb57a23de61c8b
 */

// XSS OK
@Controller
public class SquashErrorController implements ErrorController {

	@Value("${squashtm.stack.trace.control.panel.visible:true}")
	private Boolean stackTracePanel;

	@Inject
	private FeatureManager featureManager;

	private static final String PATH = "/error";

	@Inject
	private ErrorAttributes errorAttributes;

	/*
	 * This method will be called when any non handled exception occurs. But we cannot just rethrow that exception :
	 * quis custodiet ipsos custodes? There is no other error handler after this one.
	 *
	 *   So we must manually handle the job of printing the exception.
	 *
	 */
	@RequestMapping(value = PATH)
	public String error(HttpServletRequest request, HttpServletResponse response, Model model) throws Throwable {

		Map<String, Object> errors = getErrorAttributes(request, response);

		model.addAllAttributes(errors);
		model.addAttribute("code", response.getStatus());

		return "page/error";

	}

	@RequestMapping(value = PATH, produces = {"application/json", "application/*+json"})
	@ResponseBody
	public Map<String, Object> errorJson(HttpServletRequest request, HttpServletResponse response, Model model) throws Throwable {
		return getErrorAttributes(request, response);

	}

	@Override
	@SuppressWarnings("squid:S1612")
	public String getErrorPath() {
		return PATH;
	}

	private Map<String, Object> getErrorAttributes(HttpServletRequest request, HttpServletResponse response) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		Map<String, Object> result = errorAttributes.getErrorAttributes(requestAttributes, true);

		if (featureManager.isEnabled(FeatureManager.Feature.STACK_TRACE) && stackTracePanel) {
			response.setHeader("Stack-Trace", "enable");
		} else {
			result = result.entrySet().stream()
				.filter(map -> "status".equals(map.getKey()) || "error".equals(map.getKey()))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		}

		return result;
	}

}

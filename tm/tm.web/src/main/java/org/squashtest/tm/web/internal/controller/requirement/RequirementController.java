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
package org.squashtest.tm.web.internal.controller.requirement;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/requirements")
public class RequirementController {

	@Inject
	private Provider<RequirementCriticalityComboDataBuilder> criticalityComboBuilderProvider;

	@Inject
	private Provider<RequirementStatusComboDataBuilder> statusComboDataBuilderProvider;

	@RequestMapping(value = "/criticality-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public Object buildImportanceComboData(Locale locale) {
		return  criticalityComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public Object buildTypeComboData(Locale locale) {
		return statusComboDataBuilderProvider.get().useLocale(locale).buildMap();
	}
}

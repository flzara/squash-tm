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
package org.squashtest.tm.web.internal.controller.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.web.internal.model.rest.RestExecutionStub;


@Controller
@RequestMapping("/api/bugtracker")
public class BugtrackerRestController {

	@Inject
	private BugTrackersLocalService bugTrackersLocalService;

	@RequestMapping(value = "/{name}/issue/{remoteid}/executions", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<RestExecutionStub> getExecutionsByRemotedId(@PathVariable String name, @PathVariable String remoteid) {
		List<Execution> executions = bugTrackersLocalService.findExecutionsByRemoteIssue(remoteid, name);
		List<RestExecutionStub> restExecutions = new ArrayList<>(executions.size());
		for(Execution execution : executions){
			restExecutions.add(new RestExecutionStub(execution));
		}
		return restExecutions;
	}
}

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
package org.squashtest.tm.web.internal.controller.authorization;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gregory on 22/01/2016.
 *
 * @author Grégory Fouquet
 * @since 1.13.0.RC10
 */
@RestController
@RequestMapping("acls")
public class AclsController {
	public static class Acl {
		public final Collection<String> perms;

		public Acl(@NotNull Collection<String> perms) {
			this.perms = perms;
		}
	}

	private static final Map<String, String> CLASS_NAME_BY_DASH_NAME;

	static {
		Map<String, String> map = new HashMap<>();
		map.put("custom-report-library", CustomReportLibrary.class.getName());
		map.put("custom-report-library-node", CustomReportLibraryNode.class.getName());
		map.put("custom-report-folder", CustomReportFolder.class.getName());
		map.put("custom-report-dashboard", CustomReportDashboard.class.getName());
		map.put("chart-definition", ChartDefinition.class.getName());
		map.put("report-definition", ReportDefinition.class.getName());
		// TODO more stuff goes here - populated a minima.
		CLASS_NAME_BY_DASH_NAME = map;
	}

	@Inject
	private PermissionEvaluationService permissionEvaluator;

	@RequestMapping("/{resType}/{resId}")
	public Acl getAcls(@PathVariable String resType, @PathVariable long resId) {
		String className = CLASS_NAME_BY_DASH_NAME.get(resType);

		if (className == null) {
			throw new IllegalArgumentException("Resource type '" + resType + "' is unknown. Check source code for managed resource types.");
		}

		return new Acl(permissionEvaluator.permissionsOn(className, resId));
	}

}

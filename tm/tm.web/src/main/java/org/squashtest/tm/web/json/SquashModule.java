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
package org.squashtest.tm.web.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.chart.*;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.web.internal.model.json.*;

/**
 * Jackson Module which configures the default object mapper. Mixins definitions go here.
 * For other configuration parameters, see `application.properties`
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Component
public class SquashModule extends SimpleModule {
	public SquashModule() {
		super("SquashModule", new Version(1, 17, 0, "REL"));
	}

	@Override
	public void setupModule(SetupContext context) {
		// configures various domain objects (un)marshalling w/O the use of DTOs or jackson annotations
		context.setMixInAnnotations(InfoList.class, InfoListMixin.class);
		context.setMixInAnnotations(InfoListItem.class, InfoListItemMixin.class);
		context.setMixInAnnotations(CustomReportLibrary.class, CustomReportLibraryMixin.class);
		context.setMixInAnnotations(Project.class, GenericProjectMixin.class);
		context.setMixInAnnotations(CustomReportFolder.class, CustomReportFolderMixin.class);
		context.setMixInAnnotations(ChartDefinition.class, ChartDefinitionMixin.class);
		context.setMixInAnnotations(ChartQuery.class, ChartQueryMixin.class);
		context.setMixInAnnotations(Filter.class, FilterMixin.class);
		context.setMixInAnnotations(AxisColumn.class, AxisColumnMixin.class);
		context.setMixInAnnotations(MeasureColumn.class, MeasureColumnMixin.class);
		context.setMixInAnnotations(ColumnPrototype.class, ColumnPrototypeMixin.class);
		context.setMixInAnnotations(UsersGroup.class, UserGroupMixin.class);
		context.setMixInAnnotations(User.class, UserMixin.class);
		context.setMixInAnnotations(BugTracker.class, BugTrackerMixin.class);
		context.setMixInAnnotations(Credentials.class, CredentialsMixin.class);
	}
}

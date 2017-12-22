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
package org.squashtest.tm.web.internal.controller.testcase.executions;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.service.project.CustomGenericProjectManager;
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

@Component
@Scope("prototype")
public class ExecutionStatusJeditableComboDataBuilder extends
		EnumJeditableComboDataBuilder<ExecutionStatus, ExecutionStatusJeditableComboDataBuilder> {
	@Inject
	private CustomGenericProjectManager projectManager;

	public ExecutionStatusJeditableComboDataBuilder() {
		super();
	}

	@Inject
	public void setLabelFormatter(InternationalizableLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}

	public ExecutionStatusJeditableComboDataBuilder useContext(Object context) {
		Long projectId = (Long) context;
		Set<ExecutionStatus> statuses = projectManager.enabledExecutionStatuses(projectId);
		setModel(statuses.toArray(new ExecutionStatus[statuses.size()]));
		return this;
	}
}

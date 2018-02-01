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
package org.squashtest.tm.web.internal.controller.execution;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.LevelComparator;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

/**
 * Jeditable combo data builder which model is {@link ExecutionStatus}
 *
 * @author Gregory Fouquet
 *
 */
//XSS ok bflessel
@Component
@Scope("prototype")
public class ExecutionStatusComboDataBuilder extends
		EnumJeditableComboDataBuilder<ExecutionStatus, ExecutionStatusComboDataBuilder> {
	public ExecutionStatusComboDataBuilder() {
		super();
		List<ExecutionStatus> executionList = new ArrayList<>();
		for (ExecutionStatus executionStatus : ExecutionStatus.values()) {
			if(executionStatus.isCanonical()){
				executionList.add(executionStatus);
			}
		}
		// Old was setModel(ExecutionStatus.values()); but we must forget non canonical status
		setModel(executionList);
		setModelComparator(LevelComparator.getInstance());
	}

	@Inject
	public void setLabelFormatter(LevelLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder#itemKey(java.lang.Enum)
	 */
	@Override
	protected String itemKey(ExecutionStatus item) {
		String defaultKey = super.itemKey(item);
		ExecutionStatus selected = getSelectedItem();

		// Could be disabled. Be careful. Could had another argument there (look REquirementStatusComboDataBuilder)
		if (selected != null) {
			defaultKey = "disabled." + defaultKey;
		}

		return defaultKey;
	}
}

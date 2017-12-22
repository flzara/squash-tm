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
package org.squashtest.tm.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.builder.CampaignLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.RequirementLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.TestCaseLibraryTreeNodeBuilder;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Spring configuration for tree node builders
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class TreeNodeBuildersConfig {
	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	Provider<TestCaseLibraryTreeNodeBuilder> testCaseLibraryTreeNodeBuilderProvider;

	@Inject
	Provider<RequirementLibraryTreeNodeBuilder> requirementLibraryTreeNodeBuilderProvider;

	@Inject
	Provider<CampaignLibraryTreeNodeBuilder> campaignLibraryTreeNodeBuilderProvider;

	@Bean(name = "testCase.driveNodeBuilder")
	@Scope(SCOPE_PROTOTYPE)
	public DriveNodeBuilder<TestCaseLibraryNode> testCaseDriveNodeBuilder() {
		return new DriveNodeBuilder<>(permissionEvaluationService, testCaseLibraryTreeNodeBuilderProvider);
	}

	@Bean(name = "requirement.driveNodeBuilder")
	@Scope(SCOPE_PROTOTYPE)
	public DriveNodeBuilder<RequirementLibraryNode> requirementDriveNodeBuilder() {
		return new DriveNodeBuilder<>(permissionEvaluationService, requirementLibraryTreeNodeBuilderProvider);
	}

	@Bean(name = "campaign.driveNodeBuilder")
	@Scope(SCOPE_PROTOTYPE)
	public DriveNodeBuilder<CampaignLibraryNode> campaignDriveNodeBuilder() {
		return new DriveNodeBuilder<>(permissionEvaluationService, campaignLibraryTreeNodeBuilderProvider);
	}

}

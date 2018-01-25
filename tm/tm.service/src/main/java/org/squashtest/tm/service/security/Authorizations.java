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
package org.squashtest.tm.service.security;

/**
 * Defines constants for authorization rules.
 *
 * @author Gregory Fouquet
 *
 */
public final class Authorizations {

	/* -- ADMIN -- */
	public static final String READ = "READ";

	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	public static final String HAS_ROLE_ADMIN = "hasRole('ROLE_ADMIN')";

	public static final String OR_HAS_ROLE_ADMIN = " or hasRole('ROLE_ADMIN')";

	public static final String HAS_ROLE_ADMIN_OR_PROJECT_MANAGER = "hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER')";

	public static final String OR_HAS_ROLE_ADMIN_OR_PROJECT_MANAGER = " or (hasRole('ROLE_ADMIN') or hasRole('ROLE_TM_PROJECT_MANAGER'))";

	/* -- MILESTONES -- */
	public static final String MILESTONE_FEAT_ENABLED = "@featureManager.isEnabled('MILESTONE')";



	/* -- REQUIREMENT LIBRARY -- */
	public static final String CREATE_REQLIBRARY_OR_ROLE_ADMIN = "hasPermission(#libraryId, 'org.squashtest.tm.domain.requirement.RequirementLibrary' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN;

	/* -- REQUIREMENT LIBRARY NODE -- */
	public static final String READ_REQ_LIBRARY_NODE_OR_ROLE_ADMIN = "hasPermission(#reqNodeId, 'org.squashtest.tm.domain.requirement.RequirementLibraryNode', 'READ')"
		+ OR_HAS_ROLE_ADMIN;

	/* -- REQUIREMENT FOLDERS -- */
	public static final String CREATE_REQFOLDER_OR_ROLE_ADMIN = "hasPermission(#folderId, 'org.squashtest.tm.domain.requirement.RequirementFolder' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN;

	/* -- REQUIREMENTS -- */
	public static final String READ_REQUIREMENT_OR_ROLE_ADMIN = "hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'READ')" + OR_HAS_ROLE_ADMIN;

	public static final String CREATE_REQUIREMENT_OR_ROLE_ADMIN = "hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'CREATE')" + OR_HAS_ROLE_ADMIN;

	/* -- REQUIREMENT VERSIONS -- */
	public static final String READ_REQVERSION = "hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion' , 'READ')";

	public static final String READ_REQVERSION_OR_ROLE_ADMIN = READ_REQVERSION + OR_HAS_ROLE_ADMIN;

	public static final String WRITE_REQVERSION = "hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')";

	public static final String WRITE_REQVERSION_OR_ROLE_ADMIN = WRITE_REQVERSION + OR_HAS_ROLE_ADMIN;

	public static final String LINK_REQVERSION = "hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'LINK')";
	
	public static final String LINK_REQVERSION_OR_ROLE_ADMIN = LINK_REQVERSION + OR_HAS_ROLE_ADMIN;



	/* -- TEST CASES -- */
	public static final String READ_TC = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')";

	public static final String WRITE_TC = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE')";

	public static final String READ_TC_OR_ROLE_ADMIN = READ_TC + OR_HAS_ROLE_ADMIN;

	public static final String WRITE_TC_OR_ROLE_ADMIN = WRITE_TC + OR_HAS_ROLE_ADMIN;

	public static final String WRITE_PARENT_TC_OR_ROLE_ADMIN = "hasPermission(#parentTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE')" + OR_HAS_ROLE_ADMIN;

	/* TEST STEPS */
	public static final String LINK_TESTSTEP = "hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'LINK')";

	public static final String LINK_TESTSTEP_OR_ROLE_ADMIN = LINK_TESTSTEP + OR_HAS_ROLE_ADMIN;



	/* -- CAMPAIGN FOLDERS -- */
	public static final String READ_CAMPFOLDER_OR_ROLE_ADMIN = "hasPermission(#campFolderId, 'org.squashtest.tm.domain.campaign.CampaignFolder', 'READ')" + OR_HAS_ROLE_ADMIN;

	/* -- ITERATIONS -- */
	public static final String READ_ITERATION = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration' , 'READ')";

	public static final String READ_ITERATION_OR_ROLE_ADMIN = READ_ITERATION + OR_HAS_ROLE_ADMIN;

	public static final String WRITE_ITERATION_OR_ROLE_ADMIN = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'WRITE') "
		+ OR_HAS_ROLE_ADMIN;

	public static final String EXECUTE_ITERATION_OR_ROLE_ADMIN = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'EXECUTE')" + OR_HAS_ROLE_ADMIN;

	public static final String CREATE_ITERATION_OR_ROLE_ADMIN = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'CREATE') "
		+ OR_HAS_ROLE_ADMIN;

	public static final String DELETE_ITERATION_OR_ROLE_ADMIN = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'DELETE') " + OR_HAS_ROLE_ADMIN;

	/* -- TEST SUITES -- */
	public static final String READ_TESTSUITE = "hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'READ')";

	public static final String READ_TS_OR_ROLE_ADMIN = READ_TESTSUITE + OR_HAS_ROLE_ADMIN;

	public static final String EXECUTE_TS_OR_ROLE_ADMIN = "hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'EXECUTE')" + OR_HAS_ROLE_ADMIN;

	/* -- CAMPAIGNS -- */
	public static final String READ_CAMPAIGN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' , 'READ')";

	public static final String READ_CAMPAIGN_OR_ROLE_ADMIN = READ_CAMPAIGN + OR_HAS_ROLE_ADMIN;

	public static final String WRITE_CAMPAIGN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign' , 'WRITE')";

	public static final String WRITE_CAMPAIGN_OR_ROLE_ADMIN = WRITE_CAMPAIGN + OR_HAS_ROLE_ADMIN;

	public static final String CREATE_CAMPAIGN_OR_ROLE_ADMIN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'CREATE') " + OR_HAS_ROLE_ADMIN;

	/* -- EXECUTIONS -- */
	public static final String READ_EXECUTION_OR_ROLE_ADMIN = "hasPermission(#executionId, 'org.squashtest.tm.domain.execution.Execution', 'READ')" + OR_HAS_ROLE_ADMIN;

	public static final String EXECUTE_EXECUTION_OR_ROLE_ADMIN = "hasPermission(#executionId, 'org.squashtest.tm.domain.execution.Execution', 'EXECUTE') "
		+ OR_HAS_ROLE_ADMIN;

	/* -- EXECUTION STEPS -- */
	public static final String READ_EXECSTEP_OR_ROLE_ADMIN = "hasPermission(#executionStepId, 'org.squashtest.tm.domain.execution.ExecutionStep', 'READ')" + OR_HAS_ROLE_ADMIN;

	public static final String EXECUTE_EXECSTEP_OR_ROLE_ADMIN = "hasPermission(#executionStepId, 'org.squashtest.tm.domain.execution.ExecutionStep', 'EXECUTE') "
		+ OR_HAS_ROLE_ADMIN;

	/* -- ITERATION TEST PLAN ITEM -- */

	public static final String EXECUTE_ITPI = "hasPermission(#testPlanItemId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'EXECUTE') ";

	public static final String EXECUTE_ITPI_OR_ROLE_ADMIN = EXECUTE_ITPI + OR_HAS_ROLE_ADMIN;



	/* -- CUSTOM REPORT LIBRARY NODE -- */
	public static final String CREATE_CUR_LIB_NODE_OR_ROLE_ADMIN = "hasPermission(#nodeId, 'org.squashtest.tm.domain.customreport.CustomReportLibraryNode' ,'CREATE') "
		+ OR_HAS_ROLE_ADMIN;



	private Authorizations() {
		super();
	}


}

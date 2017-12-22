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
package org.squashtest.tm.service.internal.library;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.milestone.MilestoneHolder;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.*;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.service.infolist.InfoListItemManagerService;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.IssueDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderSyncExtenderDao;
import org.squashtest.tm.service.internal.repository.RequirementSyncExtenderDao;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;

/**
 * Will update a node regarding it's project settings. The updated attributes will be :
 * <ul>
 * <li>custom fields (see {@linkplain #updateCustomFields(BoundEntity)})</li>
 * <li>issues (see {@linkplain #updateIssues(List)})</li>
 * <li>automated scripts (see {@linkplain #updateAutomationParams(TestCase)})</li>
 * <li>requirement synchronization extenders (see {@linkplain #stripSyncExtender(Requirement)})</li>
 * </ul>
 *
 * @author mpagnon, bsiri
 */
@Component
public class TreeNodeUpdater implements NodeVisitor {

	@Inject
	private PrivateCustomFieldValueService privateCustomFieldValueService;

	@Inject
	private TestCaseModificationService testcaseService;

	@Inject
	private IssueDao issueDao;

	@Inject
	private InfoListItemManagerService infoListItemService;

	@Inject
	private MilestoneManagerService milestoneService;

	@Inject
	private RequirementSyncExtenderDao syncreqDao;

	@Inject
	private RequirementFolderSyncExtenderDao requirementFolderSyncExtenderDao;

	@Override

	public void visit(CampaignFolder campaignFolder) {
		// nothing to update
	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		stripFolderSyncExtender(requirementFolder);
	}


	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		// nothing to update
	}

	@Override
	public void visit(Campaign campaign) {
		updateCustomFields(campaign);
		updateMilestones(campaign);
	}

	@Override
	/**
	 * Iterations cannot be moved , if we go through this method it is because we moved a campaign first.
	 */
	public void visit(Iteration iteration) {
		updateCustomFields(iteration);
		List<Issue> issues = issueDao.findAllForIteration(iteration.getId());
		updateIssues(issues, iteration.getProject());

	}

	@Override
	/**
	 * TestSuite cannot be moved, if we go through this method it is because we moved  an iteration.
	 * Hence there is no need to update executions because there were all updated when iteration was updated.
	 *
	 */
	public void visit(TestSuite testSuite) {
		updateCustomFields(testSuite);
		updateIssues(issueDao.findAllForTestSuite(testSuite.getId()), testSuite.getProject());
	}

	@Override
	public void visit(Requirement requirement) {
		for (RequirementVersion version : requirement.getRequirementVersions()) {
			updateCustomFields(version);
			updateCategory(version);
			updateMilestones(version);
		}
		stripSyncExtender(requirement);
	}

	@Override
	public void visit(TestCase testCase) {
		updateCustomFields(testCase);
		updateMilestones(testCase);

		TestStepVisitor visitor = new TestStepVisitor() {

			@Override
			public void visit(CallTestStep visited) {// nope

			}

			@Override
			public void visit(ActionTestStep visited) {
				updateCustomFields(visited);
			}
		};
		for (TestStep step : testCase.getSteps()) {
			step.accept(visitor);
		}
		updateAutomationParams(testCase);
		updateNatureAndType(testCase);
	}

	/**
	 * @param entity
	 * @see PrivateCustomFieldValueService#migrateCustomFieldValues(BoundEntity)
	 */
	public void updateCustomFields(BoundEntity entity) {
		privateCustomFieldValueService.migrateCustomFieldValues(entity);
	}

	/**
	 * Will remove issue if they are bound to a bugtracker that is not the bugtracker of the current project.
	 *
	 * @param executions
	 */
	public void updateIssues(List<Issue> issues, Project project) {
		for (Issue issue : issues) {
			if (project != null
				&& (project.getBugtrackerBinding() == null || !issue.getBugtracker().getId()
				.equals(project.getBugtrackerBinding().getBugtracker().getId()))) {
				issueDao.delete(issue);
			}
		}
	}

	/**
	 * <p>Will remove script of test-case if the script's automated-project is not bound to the current test-case's
	 * project.<p>
	 * <p>
	 * <p>Here a test case just copied might have been copied from a different project
	 * that his own now. If that test case was referencing an automated script
	 * we must create a copy of that automated script that match the configuration of
	 * the TM project the test case was copied into.</p>
	 * <p>
	 * <p>Of course we do so iif there is a matching TA project bound to the TM project,
	 * namely if they refer to the same TA jobs.</p>
	 *
	 * @param testCase
	 */
	public void updateAutomationParams(TestCase testCase) {

		boolean couldConvert = false;

		AutomatedTest formerTATest = testCase.getAutomatedTest();

		if (formerTATest != null) {

			TestAutomationProject newTAProject = testCase.getProject().findTestAutomationProjectByJob(formerTATest.getProject());

			if (newTAProject != null) {

				testcaseService.bindAutomatedTest(testCase.getId(), newTAProject.getId(), formerTATest.getName());

				couldConvert = true;
			}

		}

		if (!couldConvert) {
			testCase.removeAutomatedScript();
		}

	}

	private void updateNatureAndType(TestCase testCase) {
		Project project = testCase.getProject();
		InfoListItem nature = testCase.getNature();
		InfoListItem type = testCase.getType();

		if (!infoListItemService.isNatureConsistent(project.getId(), nature.getCode())) {
			testCase.setNature(project.getTestCaseNatures().getDefaultItem());
		}

		if (!infoListItemService.isTypeConsistent(project.getId(), type.getCode())) {
			testCase.setType(project.getTestCaseTypes().getDefaultItem());
		}

	}

	private void updateCategory(RequirementVersion requirement) {
		Project project = requirement.getProject();
		InfoListItem category = requirement.getCategory();

		if (!infoListItemService.isCategoryConsistent(project.getId(), category.getCode())) {
			requirement.setCategory(project.getRequirementCategories().getDefaultItem());
		}
	}


	private void updateMilestones(MilestoneHolder element) {
		milestoneService.migrateMilestones(element);
	}

	private void stripSyncExtender(Requirement req) {
		if (req.isSynchronized()) {
			RequirementSyncExtender extender = req.getSyncExtender();
			req.removeSyncExtender();
			syncreqDao.delete(extender);
		}
	}

	private void stripFolderSyncExtender(RequirementFolder requirementFolder) {
		if (requirementFolder.isSynchronized()) {
			RequirementFolderSyncExtender extender = requirementFolder.getRequirementFolderSyncExtender();
			requirementFolder.setRequirementFolderSyncExtender(null);
			requirementFolderSyncExtenderDao.delete(extender);
		}
	}

}

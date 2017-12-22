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
package org.squashtest.tm.web.internal.controller.bugtracker;

import java.util.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemotePriority;
import org.squashtest.tm.bugtracker.definition.RemoteStatus;
import org.squashtest.tm.bugtracker.definition.RemoteUser;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.bugtracker.RequirementVersionIssueOwnership;
import org.squashtest.tm.web.internal.controller.campaign.TestSuiteHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;


@Component
public final class BugTrackerControllerHelper {

	@Inject
	private BugTrackersLocalService service;
	@Inject
	private InternationalizationHelper source;

	private BugTrackerControllerHelper() {

	}

	/**
	 * Will build a string that shows all steps before the bugged step + the bugged step itself.<br>
	 * The string will look like this : <br/>
	 * <br>
	 * <entityManager>
	 * 	=============================================<br>
	 *  |    Step 1/N<br>
	 *  =============================================<br>
	 * 	-Action-<br>
	 * 	action description<br>
	 * 	<br>
	 * 	Expected Result-<br>
	 * 	expected result description<br>
	 * 	<br>
	 * 	<br>
	 * 	=============================================<br>
	 * 	|    Step 2/N<br>
	 * 	=============================================<br>
	 * 	...<br>
	 * 	<br></entityManager>
	 *
	 * @param buggedStep
	 *            the bugged step where the issue will be declared
	 * @return the built string as described
	 */
	public static String getDefaultAdditionalInformations(ExecutionStep buggedStep, Locale locale,
														  MessageSource messageSource) {
		Execution execution = buggedStep.getExecution();
		List<ExecutionStep> steps = execution.getSteps();
		int totalStepNumber = steps.size();
		StringBuilder builder = new StringBuilder();
		for (ExecutionStep step : steps) {
			String actionText = HTMLCleanupUtils.htmlToText(step.getAction());
			String expectedResult = HTMLCleanupUtils.htmlToText(step.getExpectedResult());
			appendStepTitle(locale, messageSource, totalStepNumber, builder, step);
			builder.append(messageSource.getMessage("issue.default.additionalInformation.action", null, locale));
			builder.append(actionText);
			builder.append(messageSource.getMessage("issue.default.additionalInformation.expectedResult", null, locale));
			builder.append(expectedResult);
			builder.append("\n\n\n");
			if (step.getId().equals(buggedStep.getId())) {
				break;
			}
		}
		return builder.toString();
	}

	private static void appendStepTitle(Locale locale, MessageSource messageSource, int totalStepNumber,
										StringBuilder builder, ExecutionStep step) {
		builder.append("=============================================\n|    ");
		builder.append(messageSource.getMessage("issue.default.additionalInformation.step", null, locale));
		builder.append(" ");
		builder.append(step.getExecutionStepOrder() + 1);
		builder.append("/");
		builder.append(totalStepNumber);
		builder.append("\n=============================================\n");
	}

	/**
	 * Will build a default description String that will look like this : <br/>
	 * <br/>
	 * <entityManager># Test Case : [Reference] test case name <br/>
	 * # Execution : execution link <br/>
	 * <br/>
	 * # Issue description :<br/></entityManager>
	 *
	 * @param execution
	 *            an execution where the issue will be declared
	 * @return the description string
	 */
	public static String getDefaultDescription(Execution execution, Locale locale, MessageSource messageSource,
											   String executionUrl) {
		StringBuilder description = new StringBuilder();
		appendTestCaseDesc(execution.getReferencedTestCase(), description, locale, messageSource);
		appendExecutionDesc(description, locale, messageSource, executionUrl);
		appendDescHeader(description, locale, messageSource);
		return description.toString();
	}

	/**
	 * Will build a default description String that will look like this : <br/>
	 * <br/>
	 * <entityManager># Test Case : [Reference] test case name <br/>
	 * # Execution : execution link <br/>
	 * # Concerned Step : step n�/total step nb<br/>
	 * <br/>
	 * # Issue description :<br/></entityManager>
	 *
	 * @param step
	 *            an execution step where the issue will be declared
	 * @return the string built as described
	 */
	public static String getDefaultDescription(ExecutionStep step, Locale locale, MessageSource messageSource,
											   String executionUrl) {
		StringBuilder description = new StringBuilder();
		appendTestCaseDesc(step.getExecution().getReferencedTestCase(), description, locale, messageSource);
		appendExecutionDesc(description, locale, messageSource, executionUrl);
		appendStepDesc(step, description, locale, messageSource);
		appendDescHeader(description, locale, messageSource);
		return description.toString();
	}

	/**
	 * build the url of the execution
	 *
	 * @return <b>"http://</b>serverName<b>:</b>serverPort/contextPath<b>/executions/</b>executionId<b>/info"</b>
	 */
	public static String buildExecutionUrl(HttpServletRequest request, Execution execution) {
		StringBuilder requestUrl = new StringBuilder(request.getScheme());
		// formatter:off
		requestUrl.append("://")
			.append(request.getServerName())
			.append(':')
			.append(request.getServerPort())
			.append(request.getContextPath())
			.append("/executions/")
			.append(execution.getId());
		// formatter:on
		return requestUrl.toString();
	}

	private static void appendDescHeader(StringBuilder description, Locale locale, MessageSource messageSource) {
		description.append("\n# ");
		description.append(messageSource.getMessage("issue.default.description.description", null, locale));
		description.append(" :\n");
	}

	private static void appendStepDesc(ExecutionStep step, StringBuilder description, Locale locale,
									   MessageSource messageSource) {
		description.append("# ");
		description.append(messageSource.getMessage("issue.default.description.concernedStep", null, locale));
		description.append(": ");
		description.append(step.getExecutionStepOrder() + 1);
		description.append("/");
		description.append(step.getExecution().getSteps().size());
		description.append("\n");
	}

	private static void appendExecutionDesc(StringBuilder description, Locale locale, MessageSource messageSource,
											String executionUrl) {
		description.append("# ");
		description.append(messageSource.getMessage("issue.default.description.execution", null, locale));
		description.append(": ");
		description.append(executionUrl);
		description.append("\n");
	}

	private static void appendTestCaseDesc(TestCase testCase, StringBuilder description, Locale locale,
										   MessageSource messageSource) {
		if (testCase != null) {
			description.append("# ");
			description.append(messageSource.getMessage("issue.default.description.testCase", null, locale));
			description.append(": [");
			description.append(testCase.getReference());
			description.append("] ");
			description.append(testCase.getName());
			description.append("\n");
		}
	}



	/* *****************************************************************
	 *
	 * 						Table builders
	 *
	 ***************************************************************** */

	DataTableModelBuilder<RequirementVersionIssueOwnership<RemoteIssueDecorator>> createModelBuilderForRequirementVersion() {

		DataTableModelBuilder<RequirementVersionIssueOwnership<RemoteIssueDecorator>> builder;

		builder = new RequirementVersionIssuesTableModel();

		return builder;
	}

	/**
	 * Factory method. Supports : all public string constant with suffix '_TYPE' declared in {@link BugTrackerController}
	 */
	DataTableModelBuilder<IssueOwnership<RemoteIssueDecorator>> createModelBuilderFor(String entityType) {

		DataTableModelBuilder<IssueOwnership<RemoteIssueDecorator>> builder;

		switch (entityType) {

			case BugTrackerController.TEST_CASE_TYPE:
				builder = new TestCaseIssuesTableModel();
				break;

			case BugTrackerController.CAMPAIGN_FOLDER_TYPE:
			case BugTrackerController.CAMPAIGN_TYPE:
			case BugTrackerController.ITERATION_TYPE:
			case BugTrackerController.TEST_SUITE_TYPE:
				builder = new IterationIssuesTableModel();
				break;

			case BugTrackerController.EXECUTION_TYPE:
				builder = new ExecutionIssuesTableModel();
				break;

			case BugTrackerController.EXECUTION_STEP_TYPE:
				builder = new StepIssuesTableModel();
				break;

			default:
				throw new IllegalArgumentException("BugTrackerController : cannot fetch issues for unknown entity type '" + entityType + "'");

		}

		return builder;
	}

	/**
	 * <p>
	 * the DataTableModel for requirement will hold the same informations than IterationIssuesTableModel (for now) :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the owning entity</li>
	 * <li>the requirement reference</li>
	 * </ul>
	 * </p>
	 */
	private final class RequirementVersionIssuesTableModel extends DataTableModelBuilder<RequirementVersionIssueOwnership<RemoteIssueDecorator>> {

		private IssueOwnershipNameBuilder nameBuilder = new IterationModelOwnershipNamebuilder();

		public RequirementVersionIssuesTableModel() {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(LocaleContextHolder.getLocale());
		}

		@Override
		public Map<String, String> buildItemData(RequirementVersionIssueOwnership<RemoteIssueDecorator> ownership) {

			Map<String, String> result = new HashMap<>();

			RemoteIssue issue = ownership.getIssue();
			RequirementVersion requirementVersion = ownership.getRequirementVersion();
			String strUrl = service.getIssueUrl(ownership.getIssue().getId(), ownership.getOwner().getBugTracker()).toExternalForm();
			String ownerName = nameBuilder.buildName(ownership.getOwner());
			String ownerPath = nameBuilder.buildURLPath(ownership.getOwner());
			String reqRef = requirementVersion.getReference();
			String reqId = String.valueOf(requirementVersion.getRequirement().getId());
			String reqVersionId = String.valueOf(requirementVersion.getId());

			result.put("issue-url", strUrl);
			result.put("issue-id", issue.getId());
			result.put("issue-summary", issue.getSummary());
			result.put("issue-priority", findPriority(issue));
			result.put("issue-status", findStatus(issue));
			result.put("issue-assignee", findAssignee(issue));
			result.put("issue-owner", ownerName);
			result.put("issue-owner-url", ownerPath);
			result.put("BtProject", issue.getProject().getName());
			result.put("requirement-reference", reqRef);
			result.put("requirement-id", reqId);
			result.put("current-version-id", reqVersionId);

			return result;
		}
	}

	/**
	 * <p>
	 * the DataTableModel for an execution will hold the same informations than IterationIssuesTableModel (for now) :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the owning entity</li>
	 * </ul>
	 * </p>
	 */
	private final class IterationIssuesTableModel extends DataTableModelBuilder<IssueOwnership<RemoteIssueDecorator>> {

		private IssueOwnershipNameBuilder nameBuilder = new IterationModelOwnershipNamebuilder();

		public IterationIssuesTableModel() {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(LocaleContextHolder.getLocale());
		}

		@Override
		public Map<String, String> buildItemData(IssueOwnership<RemoteIssueDecorator> ownership) {

			Map<String, String> result = new HashMap<>();

			RemoteIssue issue = ownership.getIssue();
			String strUrl = service.getIssueUrl(ownership.getIssue().getId(),
				ownership.getOwner().getBugTracker()).toExternalForm();
			String ownerName = nameBuilder.buildName(ownership.getOwner());
			String ownerPath = nameBuilder.buildURLPath(ownership.getOwner());

			result.put("issue-url", strUrl);
			result.put("issue-id", issue.getId());
			result.put("issue-summary", issue.getSummary());
			result.put("issue-priority", findPriority(issue));
			result.put("issue-status", findStatus(issue));
			result.put("issue-assignee", findAssignee(issue));
			result.put("issue-owner", ownerName);
			result.put("issue-owner-url", ownerPath);
			result.put("BtProject", issue.getProject().getName());

			return result;

		}

	}


	/**
	 * <p>
	 * the DataTableModel for a TestCase will hold following informations :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the iteration name</li>
	 * </ul>
	 * </p>
	 */
	private final class TestCaseIssuesTableModel extends DataTableModelBuilder<IssueOwnership<RemoteIssueDecorator>> {

		private IssueOwnershipNameBuilder nameBuilder = new TestCaseModelOwnershipNamebuilder();

		public TestCaseIssuesTableModel() {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(LocaleContextHolder.getLocale());
		}

		@Override
		public Map<String, Object> buildItemData(IssueOwnership<RemoteIssueDecorator> ownership) {
			RemoteIssue issue = ownership.getIssue();
			Map<String, Object> row = new HashMap<>();

			String url = service.getIssueUrl(issue.getId(), ownership.getOwner().getBugTracker())
				.toExternalForm();
			String issueOwner = nameBuilder.buildName(ownership.getOwner());

			row.put("url", url);
			row.put("remote-id", issue.getId());
			row.put("summary", issue.getSummary());
			row.put("priority", findPriority(issue));
			row.put("status", findStatus(issue));
			row.put("assignee", findAssignee(issue));
			row.put("execution", issueOwner);
			row.put("execution-id", ownership.getExecution().getId());
			row.put("BtProject", issue.getProject().getName());
			return row;
		}
	}

	/**
	 * <p>
	 * the DataTableModel for an execution will hold the same informations than IterationIssuesTableModel (for now) :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the owning entity</li>
	 * </ul>
	 * </p>
	 */
	private final class ExecutionIssuesTableModel extends DataTableModelBuilder<IssueOwnership<RemoteIssueDecorator>> {

		private IssueOwnershipNameBuilder nameBuilder = new ExecutionModelOwnershipNamebuilder();

		public ExecutionIssuesTableModel() {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(LocaleContextHolder.getLocale());
		}

		@Override
		public Map<String, Object> buildItemData(IssueOwnership<RemoteIssueDecorator> ownership) {

			RemoteIssueDecorator issue = ownership.getIssue();

			Map<String, Object> result = new HashMap<>();

			result.put("issue-url",
				service.getIssueUrl(issue.getId(), ownership.getOwner().getBugTracker())
					.toExternalForm());

			result.put("remote-id", issue.getId());
			result.put("summary", issue.getSummary());
			result.put("priority", findPriority(issue));
			result.put("status", findStatus(issue));
			result.put("assignee", findAssignee(issue));
			result.put("owner", nameBuilder.buildName(ownership.getOwner()));
			result.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
			result.put("local-id", issue.getIssueId());
			result.put("BtProject", issue.getProject().getName());
			return result;
		}
	}

	/**
	 * <p>
	 * the DataTableModel will hold :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary,</li>
	 * <li>the priority</li>
	 * </ul>
	 * </p>
	 */
	private final class StepIssuesTableModel extends DataTableModelBuilder<IssueOwnership<RemoteIssueDecorator>> {

		StepIssuesTableModel() {
		}

		@Override
		public Map<String, Object> buildItemData(IssueOwnership<RemoteIssueDecorator> ownership) {

			RemoteIssueDecorator issue = ownership.getIssue();
			Map<String, Object> result = new HashMap<>();

			result.put("issue-url",
				service.getIssueUrl(issue.getId(), ownership.getOwner().getBugTracker())
					.toExternalForm());

			result.put("remote-id", issue.getId());
			result.put("summary", issue.getSummary());
			result.put("priority", findPriority(issue));
			result.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, "");
			result.put("local-id", issue.getIssueId());
			result.put("BtProject", issue.getProject().getName());
			return result;
		}
	}


	// ****** utility methods *******************

	private static String findAssignee(RemoteIssue issue) {
		String assignee = "";
		RemoteUser remoteUser = issue.getAssignee();
		if (remoteUser != null) {
			assignee = remoteUser.getName();
		}
		return assignee;
	}

	private static String findStatus(RemoteIssue issue) {
		String status = "";
		RemoteStatus remoteStatus = issue.getStatus();
		if (remoteStatus != null) {
			status = remoteStatus.getName();
		}
		return status;
	}

	private static String findPriority(RemoteIssue issue) {
		String priority = "";
		RemotePriority remotePriority = issue.getPriority();
		if (remotePriority != null) {
			priority = remotePriority.getName();
		}
		return priority;
	}

	/* ********** name builders *****************

	/**
	 *
	 * Build a different description String depending on IssueDetectorType.
	 *
	 */
	private interface IssueOwnershipNameBuilder {
		void setMessageSource(MessageSource source);

		void setLocale(Locale locale);

		String buildName(IssueDetector bugged);

		/**
		 * Returns the path of the issue detector. You'll have to find the protocol, address and application context by
		 * yourself.
		 *
		 */
		String buildURLPath(IssueDetector bugged);
	}

	/**
	 *
	 * Holds generic code to differentiate IssueDetectorTypes
	 *
	 */
	private abstract static class IssueOwnershipAbstractNameBuilder implements IssueOwnershipNameBuilder {

		// TODO : use a visitor instead of instanceof

		protected Locale locale;
		protected MessageSource messageSource;

		@Override
		public void setLocale(Locale locale) {
			this.locale = locale;
		}

		@Override
		public void setMessageSource(MessageSource source) {
			this.messageSource = source;
		}

		@Override
		public String buildName(IssueDetector bugged) {
			String name = "this is clearly a bug";

			if (bugged instanceof ExecutionStep) {
				ExecutionStep step = (ExecutionStep) bugged;
				name = buildStepName(step);
			} else if (bugged instanceof Execution) {
				Execution exec = (Execution) bugged;
				name = buildExecName(exec);
			}

			return name;
		}

		@Override
		public String buildURLPath(IssueDetector bugged) {

			Execution exec = bugged instanceof ExecutionStep ? ((ExecutionStep) bugged).getExecution()
				: (Execution) bugged;

			return "/executions/" + exec.getId();
		}

		abstract String buildStepName(ExecutionStep executionStep);

		abstract String buildExecName(Execution execution);

	}

	/**
	 *
	 * Implements builder for IssueDetector's description to display in Iteration's Issues table.
	 *
	 */
	private static final class IterationModelOwnershipNamebuilder extends IssueOwnershipAbstractNameBuilder {
		@Override
		String buildExecName(Execution bugged) {
			String suiteNameList = findTestSuiteNameList(bugged);
			if (suiteNameList.isEmpty()) {
				return messageSource.getMessage("squashtm.generic.hierarchy.execution.name.noSuite", new Object[]{
					bugged.getName(), bugged.getExecutionOrder() + 1}, locale);
			} else {
				return messageSource.getMessage("squashtm.generic.hierarchy.execution.name",
					new Object[]{bugged.getName(), suiteNameList, bugged.getExecutionOrder() + 1}, locale);
			}
		}

		@Override
		String buildStepName(ExecutionStep executionStep) {
			return buildExecName(executionStep.getExecution());
		}
	}

	/**
	 *
	 * Implements builder for IssueDetector's description to display in Execution's Issues table.
	 *
	 */
	private static final class ExecutionModelOwnershipNamebuilder extends IssueOwnershipAbstractNameBuilder {
		@Override
		public String buildExecName(Execution bugged) {
			if (bugged == null) {
				return "";
			} else {
				return bugged.getName();
			}
		}

		@Override
		String buildStepName(ExecutionStep bugged) {
			Integer index = bugged.getExecutionStepOrder() + 1;
			return messageSource.getMessage("squashtm.generic.hierarchy.execution.step.name", new Object[]{index},
				locale);
		}

	}

	/**
	 *
	 * Implements builder for IssueDetector's description to display in TestCase's Issues table.
	 *
	 */
	private static final class TestCaseModelOwnershipNamebuilder extends IssueOwnershipAbstractNameBuilder {

		@Override
		String buildExecName(Execution execution) {
			String iterationName = findIterationName(execution);
			String suiteNameList = findTestSuiteNameList(execution);
			if (suiteNameList.isEmpty()) {
				return messageSource.getMessage("squashtm.test-case.hierarchy.execution.name.noSuite", new Object[]{
					iterationName, execution.getExecutionOrder() + 1}, locale);
			} else {
				return messageSource.getMessage("squashtm.test-case.hierarchy.execution.name", new Object[]{
					iterationName, suiteNameList, execution.getExecutionOrder() + 1}, locale);
			}
		}

		@Override
		String buildStepName(ExecutionStep executionStep) {
			return buildExecName(executionStep.getExecution());
		}

	}

	public static String findOwnerDescForTestCase(IssueDetector bugged, MessageSource messageSource, Locale locale) {
		TestCaseModelOwnershipNamebuilder nameBuilder = new TestCaseModelOwnershipNamebuilder();
		nameBuilder.setMessageSource(messageSource);
		nameBuilder.setLocale(locale);
		return nameBuilder.buildName(bugged);
	}

	private static String findTestSuiteNameList(Execution execution) {
		List<TestSuite> buggedSuites = execution.getTestPlan().getTestSuites();
		return TestSuiteHelper.buildEllipsedSuiteNameList(buggedSuites, 20);
	}

	private static String findIterationName(Execution execution) {
		Iteration iteration = execution.getTestPlan().getIteration();
		String iterationName = "";
		if (iteration != null) {
			iterationName = iteration.getName();
		}
		return iterationName;
	}

}

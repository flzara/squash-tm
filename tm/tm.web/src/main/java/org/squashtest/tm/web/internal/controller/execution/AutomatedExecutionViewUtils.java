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
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.lang.MathsUtils;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

public final class AutomatedExecutionViewUtils {
	private AutomatedExecutionViewUtils() {
		super();
	}

	public static AutomatedSuiteOverview buildExecInfo(AutomatedSuite suite, Locale locale, InternationalizationHelper messageSource) {
		Collection<AutomatedExecutionExtender> executions = suite.getExecutionExtenders();
		List<ExecutionAutoView> executionsViews = new ArrayList<>(executions.size());
		int totalExec = executions.size();
		int totalTerminated = 0;
		for (AutomatedExecutionExtender autoExec : executions) {
			Execution execution = autoExec.getExecution();
			if (execution.getExecutionStatus().isTerminatedStatus()) {
				totalTerminated++;
			}
			ExecutionAutoView execView = translateExecutionInView(autoExec, locale, messageSource);
			executionsViews.add(execView);
		}
		int percentage = percentProgression(totalTerminated, totalExec);
		return new AutomatedSuiteOverview(percentage, suite.getId(), executionsViews);

	}

	private static int percentProgression(int totalTerminated, int totalExec) {
		if(totalExec == 0) {
			return 100;
		}

		return MathsUtils.percent(totalTerminated, totalExec);
	}

	public static ExecutionAutoView translateExecutionInView(AutomatedExecutionExtender autoExec, Locale locale
			, InternationalizationHelper messageSource) {
		String localisedStatus = messageSource.internationalize(autoExec.getExecution().getExecutionStatus(), locale);
		String htmlEscapedLocalizedStatus = HtmlUtils.htmlEscape(localisedStatus);
		ExecutionAutoView execView = new ExecutionAutoView();

		execView.id = autoExec.getExecution().getId();
		execView.name = autoExec.getExecution().getName();
		execView.status = autoExec.getExecution().getExecutionStatus();
		execView.localizedStatus = htmlEscapedLocalizedStatus;
		execView.automatedProject = autoExec.getAutomatedProject().getLabel();
		execView.node = autoExec.getNodeName();

		return execView;
	}

	public static class AutomatedSuiteOverview {
		private String suiteId;
		private List<ExecutionAutoView> executions;
		private int percentage = 0;

		public AutomatedSuiteOverview(int percentage, String suiteId, List<ExecutionAutoView> executions) {
			this.suiteId = suiteId;
			this.executions = executions;
			this.percentage = percentage;

		}

		public String getSuiteId() {
			return suiteId;
		}

		public void setSuiteId(String suiteId) {
			this.suiteId = suiteId;
		}

		public List<ExecutionAutoView> getExecutions() {
			return executions;
		}

		public void setExecutions(List<ExecutionAutoView> executions) {
			this.executions = executions;
		}

		public int getPercentage() {
			return percentage;
		}

		public void setPercentage(int percentage) {
			this.percentage = percentage;
		}

	}

	public static class ExecutionAutoView {
		private Long id;
		private String name;
		private ExecutionStatus status;
		private String localizedStatus;
		private String node;
		private String automatedProject;

		private ExecutionAutoView() {
			super();
		}

		public Long getId() {
			return id;
		}


		public String getName() {
			return name;
		}


		public ExecutionStatus getStatus() {
			return status;
		}

		public String getLocalizedStatus() {
			return localizedStatus;
		}

		/**
		 * @return the node
		 */
		public String getNode() {
			return node;
		}

		/**
		 * @return the automatedProject
		 */
		public String getAutomatedProject() {
			return automatedProject;
		}

	}

}

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
package org.squashtest.tm.domain.testautomation;

import org.hibernate.annotations.GenericGenerator;
import org.squashtest.tm.domain.execution.ExecutionStatus;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NamedQueries({
	@NamedQuery(name = "automatedSuite.findAll", query = "from AutomatedSuite"),
	@NamedQuery(name = "automatedSuite.findAllById", query = "from AutomatedSuite where id in (:suiteIds)"),
	@NamedQuery(name = "automatedSuite.findAllExtenders", query = "select ext from AutomatedExecutionExtender ext join ext.automatedSuite s where s.id = :suiteId"),
	@NamedQuery(name = "automatedSuite.findAllExtendersHavingStatus", query = "select ext from AutomatedExecutionExtender ext join ext.execution exe join ext.automatedSuite s where s.id = :suiteId and exe.executionStatus in (:statusList)"),
	// Fetching mass to prevent massive N + 1 that lead to exponential execution time
	@NamedQuery(name = "automatedSuite.fetchForAutomationExecution", query = "select distinct ext from AutomatedExecutionExtender ext " +
		"join fetch ext.execution exec " +
		"left join fetch exec.scriptedExecutionExtender " +
		"join fetch exec.issueList " +
		"join fetch exec.attachmentList " +
		"join fetch exec.testPlan itpi  " +
		"join fetch itpi.referencedTestCase tc " +
		"join fetch tc.automatedTest autoTest " +
		"join fetch tc.nature nat " +
		"join fetch nat.infoList " +
		"left join fetch tc.parameters " +
		"left join fetch tc.scriptedTestCaseExtender " +
		"left join fetch tc.automationRequest " +
		// can be null
		"left join fetch itpi.referencedDataset dataset " +
		//  it's a set so hibernate should be able to perform reconciliation properly
		"left join fetch dataset.parameterValues " +
		"join fetch itpi.iteration it " +
		"join fetch it.campaign " +
		"where ext.automatedSuite.id = :suiteId " +
		"order by ext.id asc")
})
@Entity
public class AutomatedSuite {

	@Id
	@Column(name = "SUITE_ID")
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	private String id;

	@OneToMany(mappedBy = "automatedSuite", cascade = {CascadeType.ALL})
	//[TMSUP-1910] Goodbye OrderColumn, hello OrderBy.
	// We still need to be sure to get them ordered by id (which is not always the case without the annotation) to respect test plan order when getting the list.
	@OrderBy
	private List<AutomatedExecutionExtender> executionExtenders = new ArrayList<>();

	/**
	 * it's transient because we do not want to persist neither do we want to compute it too often.
	 */
	private transient Boolean manualSlaveSelection;

	public String getId() {
		return id;
	}

	public List<AutomatedExecutionExtender> getExecutionExtenders() {
		return executionExtenders;
	}

	public void setExecutionExtenders(
		List<AutomatedExecutionExtender> executionExtenders) {
		this.executionExtenders = executionExtenders;
	}

	public void addExtender(AutomatedExecutionExtender extender) {
		executionExtenders.add(extender);
		extender.setAutomatedSuite(this);
	}

	public void addExtenders(Collection<AutomatedExecutionExtender> extenders) {
		for (AutomatedExecutionExtender extender : extenders) {
			executionExtenders.add(extender);
		}
	}

	public boolean hasStarted() {
		for (AutomatedExecutionExtender extender : executionExtenders) {
			if (extender.getExecution().getExecutionStatus() != ExecutionStatus.READY) {
				return true;
			}
		}
		return false;
	}

	public boolean hasEnded() {
		for (AutomatedExecutionExtender extender : executionExtenders) {
			if (!extender.getExecution().getExecutionStatus().isTerminatedStatus()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tells if the suite requires manual node selection. A manual node selection is required when at least 1 server is
	 * configured with manual selection.
	 *
	 * @return
	 */
	public boolean isManualNodeSelection() {
		if (manualSlaveSelection == null) {
			boolean manual = false;

			for (AutomatedExecutionExtender autoExec : executionExtenders) {
				manual = autoExec.getAutomatedProject().getServer().isManualSlaveSelection();
				if (manual) {
					break;
				}
			}

			manualSlaveSelection = manual;
		}

		return manualSlaveSelection;

	}

}

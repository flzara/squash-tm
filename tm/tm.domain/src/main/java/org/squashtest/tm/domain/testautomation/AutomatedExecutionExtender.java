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

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

/**
 * this was meant to be a subclass of Execution; that's what the business says. But Hibernate says that doing so would
 * trigger a bug. So we came with an extender instead.
 *
 *
 * @author bsiri
 *
 */
@Entity
public class AutomatedExecutionExtender implements Identified{

	private static final Set<ExecutionStatus> AUTOMATED_EXEC_STATUS;

	static {
		Set<ExecutionStatus> set = new HashSet<>();
		set.add(ExecutionStatus.SUCCESS);
		set.add(ExecutionStatus.WARNING);
		set.add(ExecutionStatus.NOT_RUN);
		set.add(ExecutionStatus.NOT_FOUND);
		set.add(ExecutionStatus.ERROR);
		set.add(ExecutionStatus.FAILURE);
		set.add(ExecutionStatus.RUNNING);
		set.add(ExecutionStatus.READY);
		AUTOMATED_EXEC_STATUS = Collections.unmodifiableSet(set);
	}

	@Id
	@Column(name = "EXTENDER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "automated_execution_extender_extender_id_seq")
	@SequenceGenerator(name = "automated_execution_extender_extender_id_seq", sequenceName = "automated_execution_extender_extender_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne()
	@JoinColumn(name = "TEST_ID", referencedColumnName = "TEST_ID")
	private AutomatedTest automatedTest;

	@OneToOne
	@JoinColumn(name = "MASTER_EXECUTION_ID", referencedColumnName = "EXECUTION_ID")
	private Execution execution;

	private URL resultURL;

	@ManyToOne
	@JoinColumn(name = "SUITE_ID")
	private AutomatedSuite automatedSuite;

	@Lob
	@Type(type="org.hibernate.type.TextType")
	private String resultSummary = "";

	/**
	 * Name of the node where the test is executed. Empty string means
	 */
	@NotNull
	private String nodeName = "";

	/* ******************** constructors ********************************** */

	public AutomatedExecutionExtender() {
		super();
	}

	/* ******************** accessors ************************************ */

	@Override
	public Long getId() {
		return id;
	}

	public Execution getExecution() {
		return execution;
	}


	@AclConstrainedObject
	public CampaignLibrary getCampaignLibrary(){
		return execution.getCampaignLibrary();
	}

	public void setExecution(Execution execution) {
		this.execution = execution;
	}

	public AutomatedTest getAutomatedTest() {
		return automatedTest;
	}

	/**
	 * Sets the automated test and the node, based on the test's host server.
	 *
	 * @param automatedTest
	 */
	public void setAutomatedTest(AutomatedTest automatedTest) {
		this.automatedTest = automatedTest;
	}

	public URL getResultURL() {
		return resultURL;
	}

	public void setResultURL(URL resultURL) {
		this.resultURL = resultURL;
	}

	public AutomatedSuite getAutomatedSuite() {
		return automatedSuite;
	}

	public void setAutomatedSuite(AutomatedSuite automatedSuite) {
		this.automatedSuite = automatedSuite;
	}

	public String getResultSummary() {
		return resultSummary;
	}

	public void setResultSummary(String resultSummary) {
		this.resultSummary = resultSummary;
	}

	public Set<ExecutionStatus> getLegalStatusSet() {
		return AUTOMATED_EXEC_STATUS;
	}

	public void setExecutionStatus(ExecutionStatus status) {
		execution.setExecutionStatus(status);
	}

	public TestAutomationProject getAutomatedProject() {
		return automatedTest.getProject();
	}

	/**
	 * Sets the node name. <code>null</code>s are turned into empty string
	 * @param nodeName
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = StringUtils.trimToEmpty(nodeName);
	}

	/**
	 * @return the nodeName.
	 */
	public String getNodeName() {
		return nodeName;
	}

	public boolean isNotOverYet() {
		return automatedTest != null && resultURL == null;
	}

	public boolean isProjectDisassociated() {
		return automatedTest == null;
	}
}

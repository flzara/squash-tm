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
package org.squashtest.tm.service.internal.repository;

import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;

import java.util.Collection;
import java.util.List;


public interface AutomatedSuiteDao{

	void delete(AutomatedSuite suite);

	void delete(String id);

	AutomatedSuite createNewSuite();

	AutomatedSuite findById(String id);

	List<AutomatedSuite> findAll();

	List<AutomatedSuite> findAllByIds(final Collection<String> ids);

	/**
	 * <p>
	 * Returns the list of TestAutomationProject that would run the automated tests of a given test plan, paired with
	 * the number of such tests. The test plan undef consideration is defined by a context (an entity that owns the test
	 * plan), which we can further restrict to a given list of item ids (this is optional).
	 * </p>
	 *
	 * @param context : a reference to a TestSuite or an Iteration. A reference to any other entity  will be considered as an error.
	 * @param testPlanSubset : optional list of item ids if you need to restrict the test plan. If null or empty, the parameter is ignored.
	 * @throws IllegalArgumentException : if the context is invalid.
	 * @return what is described above.
	 */
	List<Couple<TestAutomationProject, Long>> findAllCalledByTestPlan(EntityReference context, Collection<Long> testPlanSubset);


	/**
	 * Returns the list of test paths for a test plan (optionally restricted), and for a given test automation project.
	 * see {@link #findAllCalledByTestPlan(EntityReference, Collection)} for more details of the test plan definition.
	 *
	 * @param context
	 * @param testPlanSubset
	 * @param automationProjectId
	 * @return
	 */
	List<String> findTestPathForAutomatedSuiteAndProject(EntityReference context, Collection<Long> testPlanSubset, long automationProjectId);

	/**
	 * retrieve all the {@link AutomatedExecutionExtender} that this suite is bound to.
	 *
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllExtenders(String suiteId);

	/**
	 * retrieve all the extenders of executions currently waiting to be run by their test automation servers, for a given {@link AutomatedSuite}
	 *
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllWaitingExtenders(String suiteId);

	/**
	 * retrieve all the extenders of executions currently being run by their test automation servers, for a given {@link AutomatedSuite}
	 *
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllRunningExtenders(String suiteId);

	/**
	 * retrieve all the extenders of executions which had been ran their test automation servers, for a given {@link AutomatedSuite}
	 *
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllCompletedExtenders(String suiteId);

	/**
	 * retrieve all the extenders of executions which status is one of the supplied status, for a given {@link AutomatedSuite}
	 *
	 * @param id
	 * @return
	 */
	Collection<AutomatedExecutionExtender> findAllExtendersByStatus(String suiteId, Collection<ExecutionStatus> statusList);

	List<AutomatedExecutionExtender> findAndFetchForAutomatedExecutionCreation(String id);
}

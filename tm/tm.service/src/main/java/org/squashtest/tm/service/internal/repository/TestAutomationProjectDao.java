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

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.hibernate.NonUniqueEntityException;

public interface TestAutomationProjectDao {

	/**
	 * Will persist a new {@link TestAutomationProject}.
	 * 
	 * @param newProject
	 * @throws NonUniqueEntityException
	 *             if the given server happen to exist already.
	 */
	void persist(TestAutomationProject newProject);

	/**
	 *
	 * @return
	 */
	TestAutomationProject findById(Long projectId);

	/**
	 * <p>
	 * Given a detached (or even attached) {@link TestAutomationProject} example, will fetch a
	 * {@link TestAutomationProject} having the same characteristics. Null attributes will be discarded before the
	 * comparison.
	 * </p>
	 * 
	 * @return a TestAutomation project if one was found, null if none was found.
	 * @throws NonUniqueEntityException
	 *             if more than one match. Causes are either a not restrictive enough example... or a bug.
	 */
	TestAutomationProject findByExample(TestAutomationProject example);

	Collection<Long> findAllByTMProject(long tmProjectId);


	/**
	 * return true if at least one of these projects have been executed, false otherwise
	 * 
	 * @param projectIds
	 * @return
	 */
	boolean haveExecutedTestsByIds(Collection<Long> projectIds);

	/**
	 * When removing one or several TestAutomationProject : the test cases referencing their scripts are unbound, the
	 * AutomatedExecutionExtender have their resultURL and automatedTest to null, then all the AutomatedTests are
	 * removed, and finally the projects.
	 * 
	 * @param projectIds
	 */
	void deleteProjectsByIds(Collection<Long> projectIds);

	/**
	 * <p>
	 * <b style="color:red">Warning :</b> When using this method there is a risk that your Hibernate beans are not up to
	 * date. Use {@link Session#clear()} and {@link Session#refresh(Object)} to make sure your they are.
	 * </p>
	 * 
	 * @param serverId
	 */
	void deleteAllHostedProjects(long serverId);

	/**
	 * return all the projects that the given server hosts.
	 * 
	 * @param serverId
	 * @return
	 */
	List<TestAutomationProject> findAllHostedProjects(long serverId);

	/**
	 * Given an iteration ID, returns all distinct instances of TestAutomationProject
	 * that can run at least one of the tests planned in that iteration. Each project
	 * in the result set is also paired with how many items it will run.
	 *
	 * @param iterationId
	 * @return
	 */
	List<Couple<TestAutomationProject, Long>> findAllCalledByIterationId(long iterationId);

	/**
	 * Given a TestSuite ID, returns all distinct instances of TestAutomationProject
	 * that can run at least one of the tests planned in that TestSuite. Each project
	 * 	 * in the result set is also paired with how many items it will run.
	 *
	 * @param suiteId
	 * @return
	 */
	List<Couple<TestAutomationProject, Long>> findAllCalledByTestSuiteId(long suiteId);

	/**
	 * Given list of item ids, returns all distinct instances of TestAutomationProject
	 * that can run the tests planned by those items. Each project
	 * 	 * in the result set is also paired with how many items it will run.
	 *
	 * @param itemIds
	 * @return
	 */
	List<Couple<TestAutomationProject, Long>> findAllCalledByItemIds(Collection<Long> itemIds);


	/**
	 * return all the ids of the projects that the given server hosts.
	 * 
	 * @param serverId
	 * @return
	 */
	List<Long> findHostedProjectIds(long serverId);

}

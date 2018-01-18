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
package org.squashtest.tm.service.internal.testautomation;

import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

/**
 *
 *
 * @author bsiri
 *
 */
@Transactional
@Service("squashtest.tm.service.AutomatedTestService")
public class AutomatedTestManagerServiceImpl implements UnsecuredAutomatedTestManagerService {

	private static final int DEFAULT_THREAD_TIMEOUT = 30000; // timeout as milliseconds

	private int timeoutMillis = DEFAULT_THREAD_TIMEOUT;

	@Inject
	private TestAutomationProjectDao projectDao;

	@Inject
	private AutomatedTestDao testDao;

	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;


	private TestAutomationTaskExecutor executor;

	@Inject
    @Transactional(propagation = SUPPORTS) // Injection method should not trigger a tx but should not care either
	public void setAsyncTaskExecutor(AsyncTaskExecutor executor) {
		this.executor = new TestAutomationTaskExecutor(executor);
	}

	// ******************** Entity Management ************************

	@Override
	public TestAutomationProject findProjectById(long projectId) {
		return projectDao.findById(projectId);
	}


	@Override
	public AutomatedTest persistOrAttach(AutomatedTest newTest) {
		return testDao.persistOrAttach(newTest);
	}

	@Override
	public void removeIfUnused(AutomatedTest test) {
		testDao.removeIfUnused(test);
	}

	// **************************** Remote Calls ***********************

	@Override
	public Collection<TestAutomationProjectContent> listTestsInProjects(Collection<TestAutomationProject> projects) {

		// 1 : prepare the tasks
		Collection<FetchTestListTask> tasks = prepareAllFetchTestListTasks(projects);

		// 2 : start the tasks
		Collection<FetchTestListFuture> futures = submitAllFetchTestListTasks(tasks);

		// 3 : harvest the results
		return collectAllTestLists(futures);

	}

	// ****************************** fetch test list methods ****************************************

	private Collection<FetchTestListTask> prepareAllFetchTestListTasks(Collection<TestAutomationProject> projects) {
		Collection<FetchTestListTask> tasks = new ArrayList<>();

		for (TestAutomationProject project : projects) {
			tasks.add(new FetchTestListTask(connectorRegistry, project));
		}

		return tasks;
	}

	private Collection<FetchTestListFuture> submitAllFetchTestListTasks(Collection<FetchTestListTask> tasks) {

		Collection<FetchTestListFuture> futures = new ArrayList<>();

		for (FetchTestListTask task : tasks) {
			futures.add(executor.sumbitFetchTestListTask(task));
		}

		return futures;
	}

	private Collection<TestAutomationProjectContent> collectAllTestLists(Collection<FetchTestListFuture> futures) {

		Collection<TestAutomationProjectContent> results = new ArrayList<>();

		for (FetchTestListFuture future : futures) {

			try {
				TestAutomationProjectContent projectContent = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
				results.add(projectContent);
			} catch (Exception ex) {
				results.add(future.getTask().buildFailedResult(ex));
			}
		}

		return results;

	}


}

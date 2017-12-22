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

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.exception.testautomation.DuplicateTMLabelException;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;
import org.squashtest.tm.service.internal.repository.TestAutomationServerDao;
import org.squashtest.tm.service.testautomation.TestAutomationProjectFinderService;
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;

@Transactional
@Service("squashtest.tm.service.TestAutomationProjectManagementService")
public class TestAutomationProjectManagerServiceImpl implements TestAutomationProjectManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	@Inject
	private TestAutomationProjectDao projectDao;

	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;

	@Inject
	private TestAutomationServerDao serverDao;

	@Inject
	private GenericProjectDao genericProjectDao;

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void persist(TestAutomationProject newProject) {
		projectDao.persist(newProject);
	}

	@Override
	public TestAutomationProject findProjectById(long projectId) {
		return projectDao.findById(projectId);
	}

	@Override
	public void deleteProject(long projectId) {
		projectDao.deleteProjectsByIds(Arrays.asList(projectId));
	}

	@Override
	public void deleteAllForTMProject(long tmProjectId) {
		Collection<Long> allprojects = projectDao.findAllByTMProject(tmProjectId);
		projectDao.deleteProjectsByIds(allprojects);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void changeLabel(long projectId, String label) {
		TestAutomationProject project = projectDao.findById(projectId);
		if (!project.getLabel().equals(label)) {
			List<String> taProjectNames = genericProjectDao.findBoundTestAutomationProjectLabels(project.getTmProject().getId());
			if(taProjectNames.contains(label)){
				throw new DuplicateTMLabelException(label);
			}
		}
		project.setLabel(label);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void changeJobName(long projectId, String jobName) {
		TestAutomationProject project = projectDao.findById(projectId);
		project.setJobName(jobName);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void changeSlaves(long projectId, String slaveList) {
		TestAutomationProject project = projectDao.findById(projectId);
		project.setSlaves(slaveList);
	}

	@Override
	public void editProject(long projectId, TestAutomationProject newValues) {
		changeJobName(projectId, newValues.getJobName());
		changeLabel(projectId, newValues.getLabel());
		changeSlaves(projectId, newValues.getSlaves());
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public Collection<TestAutomationProject> listProjectsOnServer(String serverName) {

		TestAutomationServer server = serverDao.findByName(serverName);

		return listProjectsOnServer(server);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public Collection<TestAutomationProject> listProjectsOnServer(Long serverId) {
		TestAutomationServer server = serverDao.findOne(serverId);

		return listProjectsOnServer(server);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) {

		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());

		connector.checkCredentials(server);
		try {
			return connector.listProjectsOnServer(server);
		} catch (TestAutomationException ex) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Test Automation : failed to list projects on server : ", ex);
			}
			throw ex;
		}
	}

	/**
	 * @see TestAutomationProjectFinderService#findProjectUrls(List)
	 */
	@Override
	public Map<String, URL> findProjectUrls(Collection<TestAutomationProject> taProjects) {
		Map<String, URL> result = new HashMap<>(taProjects.size());
		for (TestAutomationProject testAutomationProject : taProjects) {
			URL url = findProjectURL(testAutomationProject);
			result.put(testAutomationProject.getJobName(), url);
		}
		return result;
	}

	@Override
	public URL findProjectURL(TestAutomationProject testAutomationProject) {
		TestAutomationServer server = testAutomationProject.getServer();
		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());
		return connector.findTestAutomationProjectURL(testAutomationProject);
	}

	/**
	 * @see TestAutomationProjectFinderService#hasExecutedTests(long)
	 */
	@Override
	public boolean hasExecutedTests(long projectId) {
		return projectDao.haveExecutedTestsByIds(Arrays.asList(projectId));
	}
}

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
package org.squashtest.tm.service.internal.scmserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.scm.spi.ScmConnector;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.service.internal.repository.ScmRepositoryDao;
import org.squashtest.tm.service.internal.repository.ScmServerDao;
import org.squashtest.tm.service.scmserver.ScmRepositoryManagerService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

@Service
@Transactional
public class ScmRepositoryManagerServiceImpl implements ScmRepositoryManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmRepositoryManagerServiceImpl.class);

	@Inject
	private ScmConnectorRegistry scmRegistry;
	@Inject
	private ScmServerDao scmServerDao;
	@Inject
	private ScmRepositoryDao scmRepositoryDao;

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public List<ScmRepository> findByScmServerOrderByPath(Long scmServerId) {
		return scmRepositoryDao.findByScmServerIdOrderByRepositoryPathAsc(scmServerId);
	}

	@Override
	public Page<ScmRepository> findPagedScmRepositoriesByScmServer(Long scmServerId, Pageable pageable) {
		return scmRepositoryDao.findByScmServerId(scmServerId, pageable);
	}

	@Override
	public void createNewScmRepository(long scmServerId, ScmRepository newScmRepository) throws IOException {
		ScmServer scmServer = scmServerDao.getOne(scmServerId);
		newScmRepository.setScmServer(scmServer);
		ScmRepository createdScmRepository = scmRepositoryDao.save(newScmRepository);

		ScmConnector connector = scmRegistry.createConnector(createdScmRepository);
		connector.initRepository();
		connector.prepareRepository();
	}

	@Override
	public String updateName(long scmRepositoryId, String newName) {
		ScmRepository scmRepository = scmRepositoryDao.getOne(scmRepositoryId);
		String formerName = scmRepository.getName();
		if(formerName.equals(newName)) {
			LOGGER.debug("Did not update the ScmRepository name because the submitted name is identical to the former one");
			return formerName;
		}
		scmRepository.setName(newName);
		scmRepositoryDao.save(scmRepository);
		return newName;
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public String updatePath(long scmRepositoryId, String newPath) {
		ScmRepository scmRepository = scmRepositoryDao.getOne(scmRepositoryId);
		String formerPath = scmRepository.getRepositoryPath();
		if(formerPath.equals(newPath)) {
			LOGGER.debug("Did not update the ScmRepository path because the submitted path is identical to the former one");
			return formerPath;
		}
		scmRepository.setRepositoryPath(newPath);
		scmRepositoryDao.save(scmRepository);
		return newPath;
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public String updateFolder(long scmRepositoryId, String newFolderPath) {
		ScmRepository scmRepository = scmRepositoryDao.getOne(scmRepositoryId);
		String formerFolderPath = scmRepository.getWorkingFolderPath();
		if(formerFolderPath.equals(newFolderPath)) {
			LOGGER.debug("Did not update the ScmRepository folder path because the submitted path is identical to the former one");
			return formerFolderPath;
		}
		scmRepository.setWorkingFolderPath(newFolderPath);
		scmRepositoryDao.save(scmRepository);
		return newFolderPath;
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public String updateBranch(long scmRepositoryId, String newBranch) throws IOException {
		ScmRepository scmRepository = scmRepositoryDao.getOne(scmRepositoryId);
		String formerBranch = scmRepository.getWorkingBranch();
		if(formerBranch.equals(newBranch)) {
			LOGGER.debug("Did not update the ScmRepository branch because the submitted branch is identical to the former one");
			return formerBranch;
		}
		scmRepository.setWorkingBranch(newBranch);
		scmRepositoryDao.save(scmRepository);

		// prepare the local repository to switch branch
		ScmConnector connector = scmRegistry.createConnector(scmRepository);
		connector.prepareRepository();

		return newBranch;
	}

	@Override
	public boolean isOneRepositoryBoundToProject(Collection<Long> scmRepositoryIds) {
		return scmRepositoryDao.isOneRepositoryBoundToProject(scmRepositoryIds);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteScmRepositories(Collection<Long> scmRepositoriesIds) {
		scmRepositoryDao.releaseScmRepositoriesFromProjects(scmRepositoriesIds);
		scmRepositoryDao.deleteByIds(scmRepositoriesIds);
	}
}

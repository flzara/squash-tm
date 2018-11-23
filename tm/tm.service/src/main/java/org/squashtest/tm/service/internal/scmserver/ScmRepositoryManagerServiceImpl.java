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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.service.internal.repository.ScmRepositoryDao;
import org.squashtest.tm.service.scmserver.ScmRepositoryManagerService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

@Service
@Transactional
public class ScmRepositoryManagerServiceImpl implements ScmRepositoryManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmRepositoryManagerServiceImpl.class);

	@Inject
	private ScmRepositoryDao scmRepositoryDao;

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<ScmRepository> findByScmServerOrderByPath(Long scmServerId) {
		return scmRepositoryDao.findByScmServerIdOrderByRepositoryPathAsc(scmServerId);
	}

	@Override
	public Page<ScmRepository> findPagedScmRepositoriesByScmServer(Long scmServerId, Pageable pageable) {
		return scmRepositoryDao.findByScmServerId(scmServerId, pageable);
	}

	@Override
	public ScmRepository createNewScmRepository(ScmRepository newScmRepository) {
		return scmRepositoryDao.save(newScmRepository);
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
	public void deleteScmRepositories(Collection<Long> scmRepositoriesIds) {
		scmRepositoryDao.deleteByIds(scmRepositoriesIds);
	}
}

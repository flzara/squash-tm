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
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

@Service
@Transactional
public class ScmRepositoryManagerServiceImpl implements ScmRepositoryManagerService {

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
}

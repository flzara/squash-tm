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
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.internal.repository.ScmServerDao;
import org.squashtest.tm.service.scmserver.ScmServerManagerService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

@Service
@Transactional
public class ScmServerManagerServiceImpl implements ScmServerManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmServerManagerServiceImpl.class);

	@Inject
	private ScmServerDao scmServerDao;

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public List<ScmServer> findAllOrderByName() {
		return scmServerDao.findAllByOrderByNameAsc();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public Page<ScmServer> findAllSortedScmServers(Pageable pageable) {
		return scmServerDao.findAll(pageable);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public ScmServer findScmServer(long scmServerId) {
		return scmServerDao.getOne(scmServerId);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public ScmServer createNewScmServer(ScmServer newScmServer) {
		
		if(scmServerDao.isServerNameAlreadyInUse(newScmServer.getName())) {
			throw new NameAlreadyInUseException("ScmServer", newScmServer.getName());
		}
		
		// authentication policy : for now ScmServer only supports APP_LEVEL
		newScmServer.setAuthenticationPolicy(AuthenticationPolicy.APP_LEVEL);
		
		return scmServerDao.save(newScmServer);
	}
	
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public String updateName(long scmServerId, String newName) {
		ScmServer scmServer = scmServerDao.getOne(scmServerId);
		String formerName = scmServer.getName();
		if(formerName.equals(newName)) {
			LOGGER.debug("Did not update the ScmServer name because the submitted name is identical to the former one.");
			return formerName;
		}
		if(scmServerDao.isServerNameAlreadyInUse(newName)) {
			throw new NameAlreadyInUseException("ScmServer", newName);
		}
		scmServer.setName(newName);
		scmServerDao.save(scmServer);
		return newName;
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public String updateUrl(long scmServerId, String newUrl) {
		ScmServer scmServer = scmServerDao.getOne(scmServerId);
		String formerUrl = scmServer.getUrl();
		if(formerUrl.equals(newUrl)) {
			LOGGER.debug("Did not update the ScmServer Url because the submitted Url is identical to the former one.");
			return formerUrl;
		}
		scmServer.setUrl(newUrl);
		scmServerDao.save(scmServer);
		return newUrl;
	}

	@Override
	public String updateCommitterMail(long scmServerId, String newCommitterMail) {
		ScmServer scmServer = scmServerDao.getOne(scmServerId);
		String formerCommitterMail = scmServer.getCommitterMail();
		if(formerCommitterMail.equals(newCommitterMail)) {
			LOGGER.debug("Did not update the ScmServer committer mail because the submitted mail is identical to the former one.");
			return formerCommitterMail;
		}
		scmServer.setCommitterMail(newCommitterMail);
		scmServerDao.save(scmServer);
		return newCommitterMail;
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteScmServers(Collection<Long> scmServerIds) {
		scmServerDao.releaseContainedScmRepositoriesFromProjects(scmServerIds);
		for(Long serverId : scmServerIds) {
			ScmServer server = scmServerDao.getOne(serverId);
			scmServerDao.delete(server);
		}
	}

	@Override
	public boolean isOneServerBoundToProject(Collection<Long> scmServerIds) {
		return scmServerDao.isOneServerBoundToProject(scmServerIds);
	}
}

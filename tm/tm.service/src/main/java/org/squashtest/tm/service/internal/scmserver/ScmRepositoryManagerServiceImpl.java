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
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.tm.core.scm.spi.ScmConnector;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.internal.repository.ScmRepositoryDao;
import org.squashtest.tm.service.internal.repository.ScmServerDao;
import org.squashtest.tm.service.scmserver.ScmRepositoryManagerService;
import org.squashtest.tm.service.servers.CredentialsProvider;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

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
	@Inject
	private CredentialsProvider credentialsProvider;

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

		if(scmRepositoryDao.isRepositoryNameAlreadyInUse(scmServerId, newScmRepository.getName())) {
			throw new NameAlreadyInUseException("ScmRepository", newScmRepository.getName());
		}

		ScmServer scmServer = scmServerDao.getOne(scmServerId);
		newScmRepository.setScmServer(scmServer);
		ScmRepository createdScmRepository = scmRepositoryDao.save(newScmRepository);

		initializeAndPrepareRepository(createdScmRepository);
	}

	/**
	 * Given a ScmRepository, check that credentials exist for its ScmServer and are valid.
	 * Then try to initialize the repository on file system and prepare it.
	 * @param scmRepository The ScmRepository to synchronize.
	 */
	private void initializeAndPrepareRepository(ScmRepository scmRepository) throws IOException {
		Credentials credentials = checkAndReturnCredentials(scmRepository);

		ScmConnector connector = scmRegistry.createConnector(scmRepository);

		checkIfProtocolIsSupported(credentials, connector);

		connector.createRepository(credentials);
		connector.prepareRepository(credentials);
	}

	/**
	 * Given a ScmRepository, check if the Credentials of its ScmServer are well defined and returns it.
	 * @param scmRepository The ScmRepository to check
	 * @return The Credentials if they are well defined
	 * @throws BugTrackerNoCredentialsException If no Credentials were defined for the ScmServer
	 */
	private Credentials checkAndReturnCredentials(ScmRepository scmRepository) {
		ScmServer server = scmRepository.getScmServer();
		Optional<Credentials> maybeCredentials = credentialsProvider.getAppLevelCredentials(server);
		Supplier<BugTrackerNoCredentialsException> throwIfNull = () -> {
			throw new BugTrackerNoCredentialsException(
				"Cannot authenticate to the remote server mapped to the repository '" + scmRepository.getName() + "' " +
					"because no valid credentials were found for authentication. " +
					"Squash-TM is supposed to use application-level credentials for that and it seems they were not configured properly. "
					+ "Please contact your administrator in order to fix the situation.", null
			);
		};
		return maybeCredentials.orElseThrow(throwIfNull);
	}

	/**
	 * Check if the given AuthenticationProtocol is supported by the ScmConnector.
	 * @param credentials The Credentials whose AuthentiacationProtocol is to check
	 * @param connector The ScmConnector
	 * @throws UnsupportedAuthenticationModeException If the protocol is not supported by the Connector
	 */
	private void checkIfProtocolIsSupported(Credentials credentials, ScmConnector connector) {
		AuthenticationProtocol protocol = credentials.getImplementedProtocol();
		if(!connector.supports(protocol)) {
			throw new UnsupportedAuthenticationModeException(protocol.toString());
		}
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
		prepareRepository(scmRepository);

		return newBranch;
	}
	/**
	 * Given a ScmRepository, check that credentials exist for its ScmServer and are valid.
	 * Then try prepare this repository.
	 * @param scmRepository The ScmRepository to synchronize.
	 */
	private void prepareRepository(ScmRepository scmRepository) throws IOException {
		Credentials credentials = checkAndReturnCredentials(scmRepository);

		ScmConnector connector = scmRegistry.createConnector(scmRepository);

		checkIfProtocolIsSupported(credentials, connector);

		connector.prepareRepository(credentials);
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

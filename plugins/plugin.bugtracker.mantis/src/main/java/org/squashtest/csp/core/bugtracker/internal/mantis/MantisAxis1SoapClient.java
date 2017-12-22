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
package org.squashtest.csp.core.bugtracker.internal.mantis;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.core.BugTrackerLocalException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.mantis.binding.AccountData;
import org.squashtest.csp.core.bugtracker.mantis.binding.IssueData;
import org.squashtest.csp.core.bugtracker.mantis.binding.MantisConnectLocator;
import org.squashtest.csp.core.bugtracker.mantis.binding.MantisConnectPortType;
import org.squashtest.csp.core.bugtracker.mantis.binding.ObjectRef;
import org.squashtest.csp.core.bugtracker.mantis.binding.ProjectData;
import org.squashtest.csp.core.bugtracker.mantis.binding.ProjectVersionData;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;

/**
 * Provides a soap client to a mantis bugtracker
 * 
 * @author Gregory Fouquet
 * @reviewed-on 2011/11/23
 * 
 */
public class MantisAxis1SoapClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(MantisAxis1SoapClient.class);
	/**
	 * location of mantis's soap api
	 */
	private static final String SOAP_API_LOCATION = "/api/soap/mantisconnect.php";

	/**
	 * Mantis config_var value to retrieve mantis default bug severity
	 */
	private static final String CONFIG_DEFAULT_BUG_SEVERITY = "default_bug_severity";

	private final MantisConnectPortType service;

	private MantisExceptionConverter exceptionConverter;

	public MantisAxis1SoapClient(BugTracker bugTracker) {
		super();

		MantisConnectLocator locator = new MantisConnectLocator();

		try {
			service = locator.getMantisConnectPort(new URL(bugTracker.getUrl() + SOAP_API_LOCATION));
		} catch (MalformedURLException e) {
			LOGGER.error("Bug tracker URL is ill-formad :  " + bugTracker.getUrl(), e);
			throw new BugTrackerLocalException(e);

		} catch (ServiceException e) {
			LOGGER.error("Error while creating SOAP client for " + bugTracker.getUrl(), e);
			throw new BugTrackerRemoteException(e);
		}
	}

	public void setMantisExceptionConverter(MantisExceptionConverter converter) {
		this.exceptionConverter = converter;
	}

	public MantisConnectPortType getService() {
		return service;
	}

	/**
	 * 
	 * @return the list of severities as {@link ObjectRef}s
	 */
	public ObjectRef[] getSeverities(AuthenticationCredentials credentials) {
		try {
			return service.mc_enum_severities(credentials.getUsername(), credentials.getPassword());
		} catch (RemoteException e) {
			LOGGER.error(e.getMessage(), e);
			throw setupException(e);
		}
	}

	public ObjectRef[] getPriorities(AuthenticationCredentials credentials) {
		try {
			// get what mantis calls severities
			return service.mc_enum_severities(credentials.getUsername(), credentials.getPassword());
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	public ProjectData[] findProjects(AuthenticationCredentials credentials) {
		try {
			return service.mc_projects_get_user_accessible(credentials.getUsername(), credentials.getPassword());
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	public ProjectVersionData[] findVersions(AuthenticationCredentials credentials, BigInteger projectId) {
		try {
			return service.mc_project_get_versions(credentials.getUsername(), credentials.getPassword(), projectId);
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	public ObjectRef[] getAccessLevel(AuthenticationCredentials credentials) {
		try {
			return service.mc_enum_access_levels(credentials.getUsername(), credentials.getPassword());
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	public AccountData[] findUsersForProject(AuthenticationCredentials credentials, BigInteger projectId,
			BigInteger access) {
		try {
			return service
					.mc_project_get_users(credentials.getUsername(), credentials.getPassword(), projectId, access);
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	public String[] findCategories(AuthenticationCredentials credentials, BigInteger projectId) {
		try {
			return service.mc_project_get_categories(credentials.getUsername(), credentials.getPassword(), projectId);
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	public BigInteger createIssue(AuthenticationCredentials credentials, IssueData issue) {
		try {
			return service.mc_issue_add(credentials.getUsername(), credentials.getPassword(), issue);
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	/***
	 * This method returns Mantis issue data corresponding to a given issue id
	 * 
	 * @param credentials
	 *            the connection data
	 * @param issueId
	 *            the given issue ID
	 */
	public IssueData getIssue(AuthenticationCredentials credentials, BigInteger issueId) {
		try {
			return service.mc_issue_get(credentials.getUsername(), credentials.getPassword(), issueId);
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	/**
	 * This method return as a String the ID of the default issue severity
	 * 
	 * @param credentials
	 *            the connection data
	 * @return the ID of the default priority
	 */
	public String getDefaultPriority(AuthenticationCredentials credentials) {
		return getConfig(credentials, CONFIG_DEFAULT_BUG_SEVERITY);
	}

	private String getConfig(AuthenticationCredentials credentials, String configVar) {
		try {
			return service.mc_config_get_string(credentials.getUsername(), credentials.getPassword(), configVar);
		} catch (RemoteException rme) {
			LOGGER.error(rme.getMessage(), rme);
			throw setupException(rme);
		}
	}

	/* ********************* private utils ************************ */

	private BugTrackerRemoteException setupException(RemoteException remoteException) {
		return exceptionConverter.convertException(remoteException);
	}

}

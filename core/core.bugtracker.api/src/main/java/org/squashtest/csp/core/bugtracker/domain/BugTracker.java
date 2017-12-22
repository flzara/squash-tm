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
package org.squashtest.csp.core.bugtracker.domain;

import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;

@Entity
@Table(name = "BUGTRACKER")
public class BugTracker {
	public static final BugTracker NOT_DEFINED;

	static {
		NOT_DEFINED = new BugTracker();
		NOT_DEFINED.url = "";
		NOT_DEFINED.kind = "none";
		NOT_DEFINED.name = "";
		NOT_DEFINED.iframeFriendly = true;
	}


	@Id
	@GeneratedValue(generator = "bugtracker_bugtracker_id_seq")
	@SequenceGenerator(name = "bugtracker_bugtracker_id_seq", sequenceName = "bugtracker_bugtracker_id_seq", allocationSize = 1)
	@Column(name = "BUGTRACKER_ID")
	private Long id;

	@NotBlank
	@Size(min = 0, max = 50)
	private String name;

	@NotBlank
	@org.hibernate.validator.constraints.URL
	@Size(min = 0, max = 255)
	private String url;

	@NotBlank
	@Size(min = 0, max = 50)
	private String kind;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name="AUTH_POLICY")
	private AuthenticationPolicy authenticationPolicy = AuthenticationPolicy.USER;
	
	
	private boolean iframeFriendly;


	public BugTracker() {
		super();
	}

	private void doSetName(String name) {
		this.name = name.trim();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		doSetName(name);
	}

	public String getUrl() {
		return url;
	}

	/**
	 * returns the URL of the registered bugtracker. That url is nothing less than the one defined
	 * in the configuration files so there is no warranty that that URL will be valid.
	 *
	 * @return the URL of that bugtracker or null if no bugtracker is defined or if malformed.
	 */
	public URL getURL() {
		URL bugTrackerUrl = null;

		try {

			bugTrackerUrl = new URL(url);

		} catch (MalformedURLException mue) {
			// XXX should throw an exception
			bugTrackerUrl = null;
		}

		return bugTrackerUrl;
	}

	public void setUrl(String url) {
		this.url = StringUtils.trim(url);
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Long getId() {
		return id;
	}

	public boolean isIframeFriendly() {
		return iframeFriendly;
	}

	public void setIframeFriendly(boolean iframeFriendly) {
		this.iframeFriendly = iframeFriendly;
	}

	public AuthenticationPolicy getAuthenticationPolicy() {
		return authenticationPolicy;
	}

	public void setAuthenticationPolicy(AuthenticationPolicy authenticationPolicy) {
		this.authenticationPolicy = authenticationPolicy;
	}


	public BugTracker getDetachedBugTracker() {
		BugTracker detached = new BugTracker();
		detached.url = this.url;
		detached.kind = this.kind;
		detached.name = this.name;
		detached.iframeFriendly = this.iframeFriendly;
		detached.authenticationPolicy = this.authenticationPolicy;
		return detached;
	}

	/**
	 * Modifies this BT with sensible defaults so that it is valid, provided it has a url and a kind.
	 */
	public void normalize() {
		if (StringUtils.isBlank(name)) {
			name = url;
		}
	}

}

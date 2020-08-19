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

import org.squashtest.tm.domain.servers.ThirdPartyServer;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.net.MalformedURLException;
import java.net.URL;

@Entity
@Table(name = "BUGTRACKER")
@PrimaryKeyJoinColumn(name = "BUGTRACKER_ID")
public class BugTracker extends ThirdPartyServer {
	public static final BugTracker NOT_DEFINED;

	static {
		NOT_DEFINED = new BugTracker();

		NOT_DEFINED.setName("");
		NOT_DEFINED.setUrl("");

		NOT_DEFINED.kind = "none";
		NOT_DEFINED.iframeFriendly = true;
	}


	@NotBlank
	@Size(min = 0, max = 50)
	private String kind;


	private boolean iframeFriendly;

	public BugTracker() {
		super();
	}


	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}


	public boolean isIframeFriendly() {
		return iframeFriendly;
	}

	public void setIframeFriendly(boolean iframeFriendly) {
		this.iframeFriendly = iframeFriendly;
	}
	
	
	/**
	 * returns the URL of the registered bugtracker. That url is nothing less than the one defined
	 * in the configuration files so there is no warranty that that URL will be valid.
	 *
	 * @return the URL of that bugtracker or null if no bugtracker is defined or if malformed.
	 */
	// do not use anymore, build your own url using getUrl(): String
	@Deprecated
	public URL getURL() {
		URL bugTrackerUrl = null;

		try {

			bugTrackerUrl = new URL(getUrl());

		} catch (MalformedURLException mue) {
			// XXX should throw an exception
			bugTrackerUrl = null;
		}

		return bugTrackerUrl;
	}

	public BugTracker getDetachedBugTracker() {
		BugTracker detached = new BugTracker();

		detached.setName(this.getName());
		detached.setUrl(this.getUrl());
		detached.setAuthenticationPolicy(this.getAuthenticationPolicy());
		detached.setAuthenticationProtocol(this.getAuthenticationProtocol());

		detached.kind = this.kind;
		detached.iframeFriendly = this.iframeFriendly;
		return detached;
	}


	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("BugTracker{");
		sb.append("id=").append(getId());
		sb.append(", name='").append(getName()).append('\'');
		sb.append(", url='").append(getUrl()).append('\'');
		sb.append(", kind='").append(kind).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

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
package org.squashtest.tm.domain.servers;


import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * That entity represents a remote server with which Squash-TM will exchange information. Note that this is an abstract
 * base class that cannot and should not exist for itself : only specific variants may exist (eg a bugtracker or a scm
 * repository).
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ThirdPartyServer {

	@Id
	@Column(name = "SERVER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "third_party_server_server_id_seq")
	@SequenceGenerator(name = "third_party_server_server_id_seq", sequenceName = "third_party_server_server_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(min = 0, max = 50)
	private String name;

	@NotBlank
	@org.hibernate.validator.constraints.URL
	@Size(min = 0, max = 255)
	private String url;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name="AUTH_POLICY")
	private AuthenticationPolicy authenticationPolicy = AuthenticationPolicy.USER;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name="AUTH_PROTOCOL")
	private AuthenticationProtocol authenticationProtocol = AuthenticationProtocol.BASIC_AUTH;


	public Long getId() {
		return id;
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

	public void setUrl(String url) {
		this.url = StringUtils.trim(url);
	}


	public AuthenticationPolicy getAuthenticationPolicy() {
		return authenticationPolicy;
	}

	public void setAuthenticationPolicy(AuthenticationPolicy authenticationPolicy) {
		this.authenticationPolicy = authenticationPolicy;
	}

	public AuthenticationProtocol getAuthenticationProtocol() {
		return authenticationProtocol;
	}

	public void setAuthenticationProtocol(AuthenticationProtocol authenticationProtocol) {
		this.authenticationProtocol = authenticationProtocol;
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

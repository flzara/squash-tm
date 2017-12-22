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

import org.hibernate.annotations.Type;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.Credentials;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;


/**
 * <p>
 * That entity represents the credentials used at the application-level by Squash to connect to other third party servers
 * persisted in the database. The actual credentials, in their encrypted form, are held by the property "encryptedCredentials"
 * and the other properties have a more peripheric role like the encryption version used or which servers they are used for.
 *</p>
 *
 * <p>
 *     The actual credentials is one of the several implementations of {@link org.squashtest.tm.domain.servers.Credentials}, in an encrypted form. The format is unspecified here
 *     and mostly depend on the service that manage it. See the service layer and usages of {@link #getEncryptedCredentials()}
 *     about that.
 * </p>
 *
 */
@Entity
public class StoredCredentials {

	@Id
	@Column(name = "CREDENTIAL_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "stored_credentials_credential_id_seq")
	@SequenceGenerator(name = "stored_credentials_credential_id_seq", sequenceName = "stored_credentials_credential_id_seq", allocationSize = 1)
	private Long id;

	/**
	 * Hint that indicate the service layer which version of the encryption service has been used.
	 *
	 */
	@Column(name = "ENC_VERSION")
	private int encryptionVersion;

	/*
	 * Credentials may be rather large (possible whole certificates) so we make this a CLOB.
	 *
	 */
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(name="ENC_CREDENTIALS")
	private String encryptedCredentials;



	/**
	 * The server for which this credentials apply
	 */
	/*
	 * TODO : once we really have a proper management of thid party servers, change the class from BugTracker to that class.
	 * TODO : the same day I suspect the mapping to become ManyToManny. But since I don't really think this will happen,
	 * for now I say YAGNI and go with a OneToOne.
	 */
	@OneToOne
	@JoinColumn(name = "AUTHENTICATED_SERVER")
	private BugTracker authenticatedServer;


	public Long getId() {
		return id;
	}

	public int getEncryptionVersion() {
		return encryptionVersion;
	}

	public String getEncryptedCredentials() {
		return encryptedCredentials;
	}

	public void setEncryptionVersion(int encryptionVersion) {
		this.encryptionVersion = encryptionVersion;
	}

	public void setEncryptedCredentials(String encryptedCredentials) {
		this.encryptedCredentials = encryptedCredentials;
	}

	public BugTracker getAuthenticatedServer() {
		return authenticatedServer;
	}

	public void setAuthenticatedServer(BugTracker authenticatedServer) {
		this.authenticatedServer = authenticatedServer;
	}

}

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
import org.squashtest.tm.domain.users.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.EnumType.STRING;

import java.util.Set;


/**
 *
 * <p>
 *     That entity wraps the authentication information used at the application-level by Squash to connect to other third party servers, for persistence in the database. The actual payload, in their encrypted form, are held by the property "encryptedCredentials" and the other properties have a more peripheric role like the encryption version used or which servers they are used for.
 *</p>
 *
 * <p>
 *     The encrypted content can be either credentials, or additional authentication configuration data.
 * </p>
 *
 */

/*
 *	Note : due to its internal nature only this class should be moved in tm.service (be mindful of the persistence config though).
 *
 * TODO :
 *
 * For now we store the following in StoredCredentials :
 * - server authentication information,
 * - user credentials (for third party servers),
 * - machine credentials
 *
 * The variety of possible content represented in this uniform entity poses extra complexity for handling the data properly and
 * makes the business less expressive. It is manageable for now but in the future it could be wise to separate the payload (the encrypted data)
 * from the purpose (user credentials, configuration, or whatever we will need to encrypt next).
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
	 */
	@Column(name = "ENC_VERSION")
	private int encryptionVersion;


	/**
	 * Credentials may be rather large (possible whole certificates) so we make this a CLOB.
	 */
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(name="ENC_CREDENTIALS")
	private String encryptedCredentials;


	/**
	 * Kind of encrypted content
	 */
	@Column(name = "CONTENT_TYPE")
	@Enumerated(STRING)
	private ContentType contentType = ContentType.CRED;


	/**
	 * The server for which this credentials apply
	 */
	@ManyToOne
	@JoinColumn(name = "AUTHENTICATED_SERVER")
	private ThirdPartyServer authenticatedServer;

	/**
	 	The user that own the credentials.
	 	For now can be null. Null value means the credentials owner is Squash-TM itself (indeed Squash-TM has no
	 	user account of it own). And yes, this is sloppy.
	 */

	@ManyToOne
	@JoinColumn(name = "AUTHENTICATED_USER")
	private User authenticatedUser;


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

	public ThirdPartyServer getAuthenticatedServer() {
		return authenticatedServer;
	}

	public void setAuthenticatedServer(ThirdPartyServer authenticatedServer) {
		this.authenticatedServer = authenticatedServer;
	}

	public User getAuthenticatedUser() {
		return authenticatedUser;
	}

	public void setAuthenticatedUser(User authenticatedUser) {
		this.authenticatedUser = authenticatedUser;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * Returns true if these credentials are system credentials (ie Squash-TM credentials).
	 *
	 * @return
	 */
	public boolean isSystemCredentials(){
		// omg this is so wrong
		return authenticatedUser == null;
	}


	public static enum ContentType{
		/**
		 * Indicates that the stored content are credentials
		 */
		CRED,
		/**
		 * Indicates that the stored content are authentication conf
		 */
		CONF;
	}
}

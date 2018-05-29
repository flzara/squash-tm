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
package org.squashtest.tm.service.internal.servers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.servers.StoredCredentials;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.servers.EncryptionKeyChangedException;
import org.squashtest.tm.service.servers.MissingEncryptionKeyException;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

@Transactional
@Service
public class StoredCredentialsManagerImpl implements StoredCredentialsManager{

	private static final String OR_CURRENT_USER_OWNS_CREDENTIALS = " or principal.username = #username";

	private static final String FIND_APP_LEVEL_CREDENTIALS = "StoredCredentials.findAppLevelCredentialsByServerId";
	private static final String FIND_USER_CREDENTIALS = "StoredCredentials.findUserCredentialsByServerId";


	private static final String JACKSON_TYPE_ID_ATTR = "@class";

	private static final Logger LOGGER= LoggerFactory.getLogger(StoredCredentialsManagerImpl.class);

	@PersistenceContext
	private EntityManager em;

	@Inject
	private FeatureManager features;

	@Inject
	private UserDao userDao;

	/*
	 * XXX to make this safer we should keep the secret in memory only when required, which means to read and wipe the key
	 * every time. But for now we will live with a permanent key in memory : after all this is the case for several other
	 * secrets and so far no paranoid complained yet.
	 */
	@Value("${squash.crypto.secret}")
	private char[] secret = new char[0];


	private ObjectMapper objectMapper;
	private boolean caseInsensitive = false;


	@Override
	public boolean isSecretConfigured() {
		if (secret.length == 0){
			return false;
		}

		// password is ok if it is not blank. No strength checked.
		for (int i=0; i<secret.length; i++){
			char digit = secret[i];
			if (digit != ' ' && digit != '\t' ){
				return true;
			}
		}

		return false;
	}

	// ****************** API methods *************************

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN + OR_CURRENT_USER_OWNS_CREDENTIALS)
	public void storeUserCredentials(long serverId, String username, Credentials credentials) {
		storeCredentials(serverId, username, credentials);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN + OR_CURRENT_USER_OWNS_CREDENTIALS)
	public Credentials findUserCredentials(long serverId, String username) {
		return unsecuredFindUserCredentials(serverId, username);
	}

	@Override
	public Credentials unsecuredFindUserCredentials(long serverId, String username) {
		return unsecuredFindCredentials(serverId, username);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN + OR_CURRENT_USER_OWNS_CREDENTIALS)
	public void deleteUserCredentials(long serverId, String username) {
		deleteCredentials(serverId, username);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void storeAppLevelCredentials(long serverId, Credentials credentials) {
		storeCredentials(serverId, null, credentials);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public Credentials findAppLevelCredentials(long serverId) {
		return unsecuredFindAppLevelCredentials(serverId);
	}

	@Override
	public Credentials unsecuredFindAppLevelCredentials(long serverId) {
		return unsecuredFindCredentials(serverId, null);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteAppLevelCredentials(long serverId) {
		deleteCredentials(serverId, null);
	}


	// ****************** implementation **********************

	/*********************************************************
		General note :
	 for the following methods the argument 'username' can be null,
	 in which case it designate application-level credentials.
	 *********************************************************/


	private void storeCredentials(long serverId, String username, Credentials credentials) {

		if (! isSecretConfigured()){
			throw new MissingEncryptionKeyException();
		}

		// serialization of the credentials
		String strCreds = null;
		try {
			strCreds = objectMapper.writeValueAsString(credentials);
		}
		catch (JsonProcessingException ex){
			LOGGER.error("an error occured while storing the credentials for server {} due to serialization error ",serverId, ex);
			throw new RuntimeException(ex);
		}

		// encryption of the credentials
		Crypto crypto = new Crypto(Arrays.copyOf(secret, secret.length));
		Crypto.EncryptionOutcome outcome = crypto.encrypt(strCreds);

		// prepare for storage
		StoredCredentials sc = null;

		// try : the server had credentials so we updated them
		try {
			sc = loadStoredCredentials(serverId, username);

			sc.setEncryptedCredentials(outcome.getEncryptedText());
		}
		// catch : the server had no credentials, so we create new ones
		catch(NoResultException ex){

			BugTracker bt = em.find(BugTracker.class, serverId);
			User user = loadUserOrNull(username);

			sc = new StoredCredentials();

			sc.setAuthenticatedServer(bt);
			sc.setAuthenticatedUser(user);
			sc.setEncryptedCredentials(outcome.getEncryptedText());
			sc.setEncryptionVersion(outcome.getVersion());

			em.persist(sc);
		}
		finally{
			crypto.dispose();
		}

	}



	private Credentials unsecuredFindCredentials(long serverId, String username) {

		if (! isSecretConfigured()){
			throw new MissingEncryptionKeyException();
		}

		Crypto crypto = new Crypto(Arrays.copyOf(secret, secret.length));

		String strDecrypt = null;

		try{
			// retrieve
			StoredCredentials sc = loadStoredCredentials(serverId, username);

			// decrypt
			// TODO : here we are supposed to test the encryption version but that'd be a concern later if we change the implementation one day
			strDecrypt = crypto.decrypt(sc.getEncryptedCredentials());

			Credentials creds = objectMapper.readValue(strDecrypt, Credentials.class);

			return creds;
		}
		catch(NoResultException ex){
			/*
			 * failure on retrieval, this is an expected error case which translates to a null result.
			 */
			LOGGER.debug("No Result on retrieving credentials.", ex);
			return null;
		}
		catch(IOException ex){
			/*
			 * failure on deserialization. Let's try to investigate and refine the error.
			 */
			LOGGER.debug(ex.getMessage(), ex);
			throw investigateDeserializationError(strDecrypt, ex);
		}
		finally{
			crypto.dispose();
		}

	}


	public void deleteCredentials(long serverId, String username) {
		try {
			StoredCredentials sc = loadStoredCredentials(serverId, username);
			em.remove(sc);
		}
		catch(NoResultException ex){
			// well, job already done right?
		}
	}

	// ***************** accessors *********************

	/*
	 * if username is null, returns the application-level credentials.
	 */
	private StoredCredentials loadStoredCredentials(long serverId, String username){

		boolean isForHumanUser = (username != null);

		String queryStr = (isForHumanUser) ? FIND_USER_CREDENTIALS : FIND_APP_LEVEL_CREDENTIALS;

		Query query = em.createNamedQuery(queryStr).setParameter("serverId", serverId);

		if (isForHumanUser){
			query.setParameter("username", username);
		}

		StoredCredentials sc = (StoredCredentials) query.getSingleResult();

		return sc;
	}

	/*
		If username is null, returns null (ie, the user is Squash-TM)
	 */
	private User loadUserOrNull(String username){
		User user = null;
		if (username == null){
			user = (caseInsensitive) ? userDao.findUserByCiLogin(username) : userDao.findUserByLogin(username);
		}
		return user;
	}


	private RuntimeException investigateDeserializationError(String failedDeser, Throwable cause){

		try {
			/*
			 * Check the declared type of credentials
			 */
			Map<String, ?> asMap = objectMapper.readValue(failedDeser, Map.class);
			String clazz = (String)asMap.get(JACKSON_TYPE_ID_ATTR);

			return new RuntimeException("missing implementation for credential type '"+clazz+"', or that type does not implement '"+Credentials.class.getName()+"'", cause);
		}
		catch (IOException e) {
			/**
			 * Woa, definitely not json. Most probably the encryption key changed.
			 */
			return new EncryptionKeyChangedException(e);
		}

	}


	@PostConstruct
	void initialize(){
		ObjectMapper om = new ObjectMapper();
		om.addMixIn(Credentials.class, CredentialsMixin.class);
		objectMapper = om;

		caseInsensitive = features.isEnabled(FeatureManager.Feature.CASE_INSENSITIVE_LOGIN);
	}


	@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS)
	@JsonInclude
	abstract class CredentialsMixin {
		@JsonIgnore
		abstract AuthenticationProtocol getImplementedProtocol();

	}


}

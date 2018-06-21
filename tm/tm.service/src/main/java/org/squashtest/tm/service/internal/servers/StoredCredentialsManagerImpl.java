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
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.servers.StoredCredentials;
import org.squashtest.tm.domain.servers.StoredCredentials.ContentType;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.servers.*;
import com.google.common.collect.ImmutableMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;


/*
 * TODO :
 *
 * For now we store the following in StoredCredentials :
 * - server authentication information,
 * - user credentials (for third party servers),
 * - machine credentials
 *
 * The variety of possible content represented in this uniform entity poses extra complexity for handling the data properly and
 * makes the business less expressibe. It is manageable for now but in the future it could be wise to separate the payload (the encrypted data)
 * from the purpose (user credentials, configuration, or whatever we will need to encrypt next).
 */
@Transactional
@Service
public class StoredCredentialsManagerImpl implements StoredCredentialsManager{

	private static final String OR_CURRENT_USER_OWNS_CREDENTIALS = " or authentication.name == #username";

	private static final String FIND_SERVER_AUTH_CONF = "StoredCredentials.findServerAuthConfByServerId";
	private static final String FIND_APP_LEVEL_CREDENTIALS = "StoredCredentials.findAppLevelCredentialsByServerId";
	private static final String FIND_USER_CREDENTIALS = "StoredCredentials.findUserCredentialsByServerId";


	private static final String JACKSON_TYPE_ID_ATTR = "@class";
	private static final String DEPREC_BASIC_CREDENTIALS = BasicAuthenticationCredentials.class.getName();

	private static final Logger LOGGER= LoggerFactory.getLogger(StoredCredentialsManagerImpl.class);

	private static final Map<Class<?>, ContentType> CONTENT_TYPE_BY_CLS;

	static {
		CONTENT_TYPE_BY_CLS = new ImmutableMap.Builder<Class<?>, ContentType>()
											.put(ServerAuthConfiguration.class, ContentType.CONF)
											.put(ManageableCredentials.class, ContentType.CRED)
											.build();
	}

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
		if (secret.length == 0 || secret[0] == '\0'){
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
	public void storeUserCredentials(long serverId, String username, ManageableCredentials credentials) {
		if (! credentials.allowsUserLevelStorage()){
			throw new IllegalArgumentException(
				"Refused to store credentials of type '"+credentials.getImplementedProtocol()+"' : business rules forbid " +
					"to store such credentials for human users"
			);
		}
		storeContent(serverId, username, credentials, ContentType.CRED);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN + OR_CURRENT_USER_OWNS_CREDENTIALS)
	public ManageableCredentials findUserCredentials(long serverId, String username) {
		return unsecuredFindUserCredentials(serverId, username);
	}

	@Override
	public ManageableCredentials unsecuredFindUserCredentials(long serverId, String username) {
		return unsecuredFindContent(serverId, username, ManageableCredentials.class);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN + OR_CURRENT_USER_OWNS_CREDENTIALS)
	public void invalidateUserCredentials(long serverId, String username) {
		ManageableCredentials creds = unsecuredFindContent(serverId, username, ManageableCredentials.class);
		if (creds != null){
			creds.invalidate();
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN + OR_CURRENT_USER_OWNS_CREDENTIALS)
	public void deleteUserCredentials(long serverId, String username) {
		deleteContent(serverId, username, ContentType.CRED);
	}


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void storeAppLevelCredentials(long serverId, ManageableCredentials credentials){
		if (! credentials.allowsAppLevelStorage()){
			throw new IllegalArgumentException(
				"Refused to store credentials of type '"+credentials.getImplementedProtocol()+"' : business rules forbid " +
					"to store such credentials as application-level credentials"
			);
		}
		storeContent(serverId, null, credentials, ContentType.CRED);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public ManageableCredentials findAppLevelCredentials(long serverId) {
		return unsecuredFindAppLevelCredentials(serverId);
	}

	@Override
	public ManageableCredentials unsecuredFindAppLevelCredentials(long serverId) {
		return unsecuredFindContent(serverId, null, ManageableCredentials.class);
	}

	@Override
	public void invalidateAppLevelCredentials(long serverId) {
		ManageableCredentials creds = unsecuredFindContent(serverId, null, ManageableCredentials.class);
		if (creds != null){
			creds.invalidate();
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteAppLevelCredentials(long serverId) {
		deleteContent(serverId, null, ContentType.CRED);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void storeServerAuthConfiguration(long serverId, ServerAuthConfiguration conf) {
		storeContent(serverId, null, conf, ContentType.CONF);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public ServerAuthConfiguration findServerAuthConfiguration(long serverId) {
		return unsecuredFindServerAuthConfiguration(serverId);
	}

	@Override
	public ServerAuthConfiguration unsecuredFindServerAuthConfiguration(long serverId) {
		return unsecuredFindContent(serverId, null, ServerAuthConfiguration.class);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteServerAuthConfiguration(long serverId) {
		deleteContent(serverId, null, ContentType.CONF);
	}

	// ****************** implementation **********************

	/*********************************************************
		General note :
	 for the following methods the argument 'username' can be null,
	 in which case it designate application-level credentials.
	 *********************************************************/


	private void storeContent(long serverId, String username, Object content, ContentType contentType){

		if (! isSecretConfigured()){
			throw new MissingEncryptionKeyException();
		}

		Crypto.EncryptionOutcome outcome = null;
		try{
			// encrypt
			outcome = toEncryptedForm(content);
		}
		catch(GeneralSecurityException | IOException ex){
			LOGGER.error("Could encrypt the content because the JRE doesn't support the specified encryption algorithms.");
			throw new RuntimeException(ex);
		}

		// prepare for storage
		StoredCredentials sc = null;

		// try : the server has content of that type already so we updated them
		try {
			sc = loadStoredContent(serverId, username, contentType);

			sc.setEncryptedCredentials(outcome.getEncryptedText());
		}
		// catch : the server had no credentials, so we create new ones
		catch(NoResultException ex){

			BugTracker bt = em.find(BugTracker.class, serverId);
			User user = loadUserOrNull(username);

			sc = new StoredCredentials();
			sc.setContentType(contentType);

			sc.setAuthenticatedServer(bt);
			sc.setAuthenticatedUser(user);
			sc.setEncryptedCredentials(outcome.getEncryptedText());
			sc.setEncryptionVersion(outcome.getVersion());

			em.persist(sc);
		}

	}



	/*
	 * Returns the credentials for the server id. If username is not null, this implicitly means
	 * user-level credentials. If username is null, app-level credentials will be retrieved instead.
	 */
	private <TYPE> TYPE unsecuredFindContent(long serverId, String username, Class<TYPE> deserializationClass)  {

		if (! isSecretConfigured()){
			throw new MissingEncryptionKeyException();
		}

		ContentType contentType = findContentType(deserializationClass);

		LOGGER.debug("loading stored content for server '{}', user '{}' and of type '{}'", serverId, username, contentType);

		Crypto crypto = new Crypto(Arrays.copyOf(secret, secret.length));

		StoredCredentials sc = null;
		String strDecrypt = null;

		try{
			// retrieve
			sc = loadStoredContent(serverId, username, contentType);

			// decrypt
			// TODO : here we are supposed to test the encryption version but that'd be a concern later if we change the implementation one day
			strDecrypt = crypto.decrypt(sc.getEncryptedCredentials());

			TYPE creds = objectMapper.readValue(strDecrypt, deserializationClass);

			return creds;
		}

		// -- exception 1 : nothing to retrieve --
		catch(NoResultException ex){
			//failure on retrieval, this is an expected error case which translates to a null result.
			LOGGER.debug("Content not found.");
			return null;
		}

		// -- exception 2 : encryption exceptions --
		catch(GeneralSecurityException | UnsupportedEncodingException cryptoException){
			LOGGER.error("Decryption failed probably because the encryption key changed. " +
							 "Less likely, is also can be that JRE doesn't support the specified " +
							 "encryption algorithms.");
			throw new EncryptionKeyChangedException(cryptoException);
		}

		// -- exception 3 : data format is wrong was wrong, or less probably a rarer encryption exception was thrown --
		// using a catchall here because Jackson might also use IOException, IllegalArgumentException and whatnot
		catch(Exception ex){

			// null string : the decryption failed but thrown a different exception than expected above
			if (strDecrypt == null){
				// we just rethrow it.
				LOGGER.debug("The decryption failed for unknown reasons.");
				throw new RuntimeException(ex);
			}

			// else we can try alternatives
			return fallbackOrDie(sc, strDecrypt);
		}
		finally{
			crypto.dispose();
		}

	}

	private <TYPE> TYPE fallbackOrDie(StoredCredentials sc, String strDecrypt) {

		// possible data format error : the old BasicAuthenticationCredentials (which does not implement ManageableCredentials).
		// such backup is possible only if we are trying to load credentials (no server auth conf needs migration yet).
		LOGGER.debug("The data format is wrong. Perhaps an instance of BasicAuthenticationCredentials," +
					" which cannot now be stored directly, needs migration ?");
		try {
			return (TYPE) migrateToNewFormat(strDecrypt, sc);
		}

		// other format error
		catch (IOException | ClassCastException definitelyWrong) {
			//Let's try to investigate and refine the error.
			LOGGER.error("something has gone definitely wrong.");
			LOGGER.error(definitelyWrong.getMessage(), definitelyWrong);
			throw investigateDeserializationError(strDecrypt, definitelyWrong);
		}

		// sec exception : data were decrypted and migrated successfully but could not encrypt the new credentials
		catch (GeneralSecurityException cryptoException) {
			LOGGER.error("encryption exception while trying to migrate and restore old credentials ! Was the secret key changed ?");
			throw new EncryptionKeyChangedException(cryptoException);
		}
	}


	public void deleteContent(long serverId, String username, ContentType type) {
		try {
			StoredCredentials sc = loadStoredContent(serverId, username, type);
			em.remove(sc);
		}
		catch(NoResultException ex){
			// well, job already done right?
		}
	}

	// ************* encrypt / decrypt *****************


	private Crypto.EncryptionOutcome toEncryptedForm(Object credentials) throws IOException, GeneralSecurityException{

		Crypto crypto = new Crypto(Arrays.copyOf(secret, secret.length));

		try {
			// serialization of the credentials
			String strCreds = null;
			try {
				strCreds = objectMapper.writeValueAsString(credentials);
			} catch (JsonProcessingException ex) {
				LOGGER.error("an error occured while storing the credentials due to serialization error ", ex);
				throw new RuntimeException(ex);
			}

			// encryption of the credentials
			Crypto.EncryptionOutcome outcome = crypto.encrypt(strCreds);

			return outcome;

		}
		finally{
			crypto.dispose();
		}
	}


	// ***************** accessors *********************

	/*
	 * if username is null, returns the application-level credentials.
	 */
	private StoredCredentials loadStoredContent(long serverId, String username, ContentType type){

		boolean isForHumanUser = (username != null);

		String queryStr = locateQuery(username, type);

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
		if (username != null){
			user = (caseInsensitive) ? userDao.findUserByCiLogin(username) : userDao.findUserByLogin(username);
		}
		return user;
	}


	private String locateQuery(String username, ContentType type){
		boolean isConf = (type == ContentType.CONF);
		boolean isForHumanUser = (username != null);

		String queryStr = (isConf) ? 			FIND_SERVER_AUTH_CONF :
							(isForHumanUser) ? 	FIND_USER_CREDENTIALS :
												FIND_APP_LEVEL_CREDENTIALS;

		return queryStr;
	}

	private RuntimeException investigateDeserializationError(String failedDeser, Throwable cause){

		try {
			/*
			 * Check the declared type of credentials
			 */
			Map<String, ?> asMap = objectMapper.readValue(failedDeser, Map.class);
			String clazz = (String)asMap.get(JACKSON_TYPE_ID_ATTR);

			return new RuntimeException("missing implementation for ManageableCredentials type '"+clazz+"', or that type does not implement '"+ManageableCredentials.class.getName()+"'", cause);
		}
		catch (IOException e) {
			/*
			 * Woa, definitely not json. Most probably the encryption key changed. Note that the IOException might have
			 * been thrown by the database but a Jackson failure is more likely, due to decryption returning garbage.
			 */
			return new EncryptionKeyChangedException(e);
		}

	}

	private ContentType findContentType(Class<?> targetClass){
		ContentType type = CONTENT_TYPE_BY_CLS.get(targetClass);
		if (type == null){
			throw new IllegalArgumentException("Content type for class '"+targetClass.getName()+"' is not supported and is definitely a programmatic error");
		}
		return type;
	}


	@PostConstruct
	void initialize(){
		ObjectMapper om = new ObjectMapper();
		om.addMixIn(Credentials.class, InternalCredentialsMixin.class);
		om.addMixIn(ManageableCredentials.class, InternalManageableCredentialsMixin.class);
		om.addMixIn(ServerAuthConfiguration.class, InternalServerAuthConfigurationMixin.class);
		objectMapper = om;

		caseInsensitive = features.isEnabled(FeatureManager.Feature.CASE_INSENSITIVE_LOGIN);
	}


	@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS)
	@JsonInclude
	interface InternalCredentialsMixin {
		@JsonIgnore
		AuthenticationProtocol getImplementedProtocol();
	}

	@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS)
	@JsonInclude
	interface InternalManageableCredentialsMixin {
		@JsonIgnore
		 AuthenticationProtocol getImplementedProtocol();
	}

	@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS)
	@JsonInclude
	interface InternalServerAuthConfigurationMixin {
		@JsonIgnore
		AuthenticationProtocol getImplementedProtocol();
	}


	// ************** sometime, a man has to bury the skeletons way way down below **************************

	// deprecated
	@Override
	public Credentials unsecuredFindCredentials(long serverId) {
		ManageableCredentials manageable = unsecuredFindAppLevelCredentials(serverId);
		BugTracker bt = em.find(BugTracker.class, serverId);
		Credentials creds = manageable.build(this, bt, null);
		return creds;
	}



	private ManageableCredentials migrateToNewFormat(String strDecrypt, StoredCredentials sc) throws IOException, GeneralSecurityException{

		LOGGER.debug("attempting migration of the deprecated stored credentials");

		ManageableCredentials fixed = tryAsBasicAuth(strDecrypt);

		// no error ? Well, let's try to fix the database
		// TODO : will fail if the transaction is set to read-only. But I leave that to the future for now.
		Crypto.EncryptionOutcome outcome = toEncryptedForm(fixed);
		sc.setEncryptedCredentials(outcome.getEncryptedText());

		LOGGER.debug("migration completed");

		// now return the awaited credentials
		return fixed;

	}

	/**
	 * In former versions of Squash, credentials were stored in the database as is. This method is an attempt to migrate
	 * them to the new format.
	 *
	 * @param serialized
	 * @return
	 * @throws IOException
	 */
	private ManageableCredentials tryAsBasicAuth(String serialized) throws IOException{

		BasicAuthenticationCredentials creds = objectMapper.readValue(serialized, BasicAuthenticationCredentials.class);

		// no error ? well let migrate them then
		return new ManageableBasicAuthCredentials(creds.getUsername(), creds.getPassword());

	}

}

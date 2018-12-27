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
package org.squashtest.tm.service.internal.servers

import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.domain.servers.AuthenticationProtocol
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials
import org.squashtest.tm.domain.servers.Credentials
import org.squashtest.tm.domain.servers.StoredCredentials.ContentType
import org.squashtest.tm.domain.servers.ThirdPartyServer
import org.squashtest.tm.domain.servers.StoredCredentials
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.internal.repository.UserDao
import org.squashtest.tm.service.feature.FeatureManager
import org.squashtest.tm.service.servers.EncryptionKeyChangedException
import org.squashtest.tm.service.servers.ManageableCredentials
import org.squashtest.tm.service.servers.MissingEncryptionKeyException
import org.squashtest.tm.service.servers.StoredCredentialsManager
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.Query

class StoredCredentialsManagerImplTest extends Specification{

	private static ManageableCredentials DEFAULT_CREDS = new ManageableBasicAuthCredentials("bob", "you'll never find it" as char[])

	// one possible encryption result of the above
	private static String DEFAULT_ENCRYPTED = "fmilBzv0lvpySZEul1GEdyxyjr02BSxg8dHksU59Or5AyT3BzfLIl1TSmyX6bgP9zBuZWLyg4MN4PtKe1GEduQBI2Ikr2zwk7o7gutsQ0bFVQtpegQQzK/xwN1YYPfcWp1LQllnIL4lYFXym0edaQ5klq59ffaIgjKutFDvQh8X+RfPEEBOKVgcq6aY1dd/JSofdxNhzgU5ww0yrI+ZwLt/Y2jwYhBHt"

	// **************************************************************************

	StoredCredentialsManagerImpl manager
	FeatureManager features = Mock()
	UserDao userDao = Mock()


	EntityManager em = Mock()
	Query findCredQuery = Mock()
	BugTracker bt = Mock()
	User user = Mock()

	def setup(){
		features.isEnabled(FeatureManager.Feature.CASE_INSENSITIVE_LOGIN) >> false

		// manager init
		manager = new StoredCredentialsManagerImpl();

		manager.features = features
		manager.em = em
		manager.secret = "mypassword" as char[]
		manager.userDao = userDao

		manager.initialize()

		// entity manager and dao init
		em.createNamedQuery(_) >> findCredQuery
		findCredQuery.setParameter(_,_) >> findCredQuery

		bt.getId() >> 10L
		em.find(ThirdPartyServer, _) >> bt

		userDao.findUserByLogin(_) >> user
	}

	// *************** test of the object mapper configuration ****************

	def "should serialize credentials"(){

		when :
			String res = manager.objectMapper.writeValueAsString(DEFAULT_CREDS)

		then :
			res == '{"@class":"org.squashtest.tm.service.internal.servers.ManageableBasicAuthCredentials","username":"bob","password":"you\'ll never find it"}'

	}


	def "should deserialize credentials"(){

		given :
			def str = '{"@class":"org.squashtest.tm.service.internal.servers.ManageableBasicAuthCredentials","username":"bob","password":"you\'ll never find it"}'

		when :
			def res = manager.objectMapper.readValue(str, ManageableCredentials)


		then :
			res instanceof ManageableBasicAuthCredentials
			res.username == "bob"
			res.password.join() == "you'll never find it"

	}

	def "should encrypt the credentials and reread encrypted credentials"(){

		given :
			String encrypted = manager.toEncryptedForm(DEFAULT_CREDS).encryptedText

		when :
			Crypto recrypto = new Crypto(manager.secret)
			String decrypted = recrypto.decrypt(encrypted)
			ManageableBasicAuthCredentials recreds = manager.objectMapper.readValue(decrypted, ManageableCredentials)

		then :
			recreds.username == DEFAULT_CREDS.username
			recreds.password == DEFAULT_CREDS.password


	}

	def "should say that the secret is configured"(){

		expect:
			// secret was configured in the setup
			manager.isSecretConfigured() == true

	}

	def "should say that no secret is configured (secret is empty)"(){
		when :
			manager.secret = [] as char[]

		then:
			manager.isSecretConfigured() == false
	}

	def "should say that no secret is configured (secret is blank)"(){
		when :
			manager.secret = "   " as char[]

		then :
			manager.isSecretConfigured() == false
	}


	def "should load credentials for a human"(){

		given :
		def q = Mock(Query)
		def sc = Mock(StoredCredentials)
		q.getSingleResult() >> sc

		when :
		def res = manager.loadStoredContent(10L, "bob", ContentType.CRED)

		then :

		res == sc

		1 * em.createNamedQuery(StoredCredentialsManagerImpl.FIND_USER_CREDENTIALS) >> q
		1 * q.setParameter("username", "bob") >> q
		1 * q.setParameter("serverId", 10L) >> q

	}

	def "should load app-level credentials"(){

		given :
		def q = Mock(Query)
		def sc = Mock(StoredCredentials)
		q.getSingleResult() >> sc

		when :
		def res = manager.loadStoredContent(10L, null, ContentType.CRED)

		then :

		res == sc

		1 * em.createNamedQuery(StoredCredentialsManagerImpl.FIND_APP_LEVEL_CREDENTIALS) >> q
		0 * q.setParameter("username", _) >> q
		1 * q.setParameter("serverId", 10L) >> q

	}


	def "should load a user account"(){

		expect :
		manager.loadUserOrNull("bob") == user

	}

	def "should load squash-tm account (namely, null value)"(){
		expect:
		manager.loadUserOrNull(null) == null
	}


	// ******************** writing *********************************

	def "creating the credentials for a user"(){
		given:
			UserOAuth1aToken userTokens = new UserOAuth1aToken("token", "secret")

		and :
			findCredQuery.getSingleResult() >> { throw new NoResultException() }

		when :
			manager.storeUserCredentials(10L, "bob", userTokens)

		then :
			1 * em.persist( {
				it.authenticatedServer == bt &&
				it.authenticatedUser == user &&
				it.encryptedCredentials.size() > 0 &&
				it.encryptionVersion == 1
			})
	}


	def "update the credentials for a user"(){
		given:
			UserOAuth1aToken userTokens = new UserOAuth1aToken("token", "secret")

		and :
			StoredCredentials sc = Mock()
			findCredQuery.getSingleResult() >> sc

		when :
			manager.storeUserCredentials(10L, "bob", userTokens)

		then :
			1 * sc.setEncryptedCredentials(_)
	}



	def "cannot store credentials because the secret isn't configured"(){
		given :
			manager.secret = [] as char[]
			def creds = mockCredentials()

		when :
			manager.storeAppLevelCredentials(1L, creds)

		then :
			thrown MissingEncryptionKeyException
	}


	def "cannot store because such credentials are not suitable for user-level persistence"(){

		when:
			manager.storeUserCredentials(10L, "bob", new ManageableBasicAuthCredentials("bob", "bobpassword" as char[]))

		then:
			thrown IllegalArgumentException

	}


	def "cannot store because such credentials are not suitable for app-level persistence"(){

		when:
		manager.storeAppLevelCredentials(10L, new PseudoCredentials())

		then:
		thrown IllegalArgumentException

	}

	// ******************* reading *****************************

	def "should find a user credentials"(){

		given :
			def sc = Mock(StoredCredentials)
			sc.getContentType() >> ContentType.CRED
			sc.getEncryptedCredentials() >> DEFAULT_ENCRYPTED

			findCredQuery.getSingleResult() >> sc


		when :
			def result = manager.unsecuredFindContent(10L, "bob", ManageableCredentials)

		then :
			result.username == DEFAULT_CREDS.username
			result.password == DEFAULT_CREDS.password

	}


	def "cannot find credentials because the secret isn't configured"(){

		given:
			manager.secret = [] as char[]

		when :
			manager.findAppLevelCredentials(1L)

		then :
			thrown MissingEncryptionKeyException

	}

	// ******************* reading error recovery *****************************

	def "should migrate old basic auth credentials"(){

		given:
			def unmanagedCreds = new BasicAuthenticationCredentials("bob", "these are my old creds")
			def serialized = manager.objectMapper.writeValueAsString(unmanagedCreds)

			StoredCredentials sc = Mock()

		when :
			def res = manager.migrateToNewFormat(serialized, sc)

		then :
			res instanceof ManageableBasicAuthCredentials
			res.username == "bob"
			res.password.join() == "these are my old creds"

			1 * sc.setEncryptedCredentials(_)

	}


	def "should find that an error at deserialization comes from unknown credential implementation"(){

		given :
			def str = '{"@class":"unknown.credentials.Implementation","username":"bob"}'
			def cause = new Exception()
		when :
			def ex = manager.investigateDeserializationError(str, cause)

		then :
			ex instanceof RuntimeException


	}


	def "should find that an error at deserialization comes from failed decryption"(){

		given :
		def str = '0165584eddf6zer54ggf68h4fr6ty48ret'
		def cause = new Exception()
		when :
		def ex = manager.investigateDeserializationError(str, cause)

		then :
		ex instanceof EncryptionKeyChangedException
	}


	// ****************** helper code ******************************

	private ManageableCredentials mockCredentials(){
		new ManageableBasicAuthCredentials("bob", "you'll never find it" as char[])
	}


	private static final class PseudoCredentials implements ManageableCredentials{

		@Override
		boolean allowsUserLevelStorage() {
			return false
		}

		@Override
		boolean allowsAppLevelStorage() {
			return false
		}

		@Override
		AuthenticationProtocol getImplementedProtocol() {
			return AuthenticationProtocol.BASIC_AUTH
		}

		@Override
		Credentials build(StoredCredentialsManager storeManager, ThirdPartyServer server, String username) {
			return null
		}


	}

}

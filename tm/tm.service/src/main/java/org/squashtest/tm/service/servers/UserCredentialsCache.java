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
package org.squashtest.tm.service.servers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.servers.ThirdPartyServer;


/**
 * <p>
 * 		Maps a user's credentials for a third party server, which is meant to last for a whole user session in order to maintain the credentials live.
 * 		Unlike regular cache, which is used for fastest retrieval, here it is rather used to keep user's credentials alive when they cannot or should not
 * 		be stored in the database as a {@link org.squashtest.tm.domain.servers.StoredCredentials}.
 * </p>
 * <p>
 *     	Whenever possible, the credentials should not be cached ^^ In general, letting sensitive that kind of  information living in memory for
 *     	long is not not safe security-wise. However due to half-baked requirements and half-finished job on that topic, the cache
 *     	is necessary because otherwise the credentials would have no place to persist otherwise.
 * <p>
 *
 * <p>
 * 		For now the decision regarding caching or not caching is the following :
 * </p>
 *
 * <ul>
 *     <li>
 *         	BasicAuthentication credentials are (for now) never stored in the database, so there is no choice but keep them this cache.
 *     		Otherwise they would be lost when the thread ends, and the user would have to reenter them again.
 *     </li>
 * 		<li>
 * 		    On the other hand OAuth1a tokens are stored in the DB. In this case the right choice is to let them live in the DB rather
 * 			than in memory. OAuth1a credentials should thus never be cached.
 * 		</li>
 * </ul>
 *
 * <p>
 *     For the code historians, the philogenetic ancestor of that class was BugTrackerContext, which has gone extinct circa Squash TM 1.18.
 * </p>
 *
 * @author bsiri
 */

/*
	This class should be synchronized because several threads may attempts to read/write on it : the BugTrackerService might read credentials,
	while the BugTrackerAutoconnectCallback might write in it. Call me a fool but I can live with it,
	considering that the probability of collisions are low and consequences are negligible.
 */
@SuppressWarnings("serial")
public class UserCredentialsCache implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserCredentialsCache.class);

	private final String user;

	private final Map<Long, Credentials> cache = new HashMap<>();


	/**
	 * @param user, must be not null
	 */
	public UserCredentialsCache(String user){
		if (user == null){
			throw new NullArgumentException("UserCredentialsCache : username must not be null");
		}
		this.user = user;
	}


	public Credentials getCredentials(ThirdPartyServer server) {
		return cache.get(server.getId());
	}



	public void cacheIfAllowed(ThirdPartyServer server, Credentials credentials) {
		if (credentials == null){
			throw new NullArgumentException("UserCredentialsCache : cannot set credentials if they are null. Perhaps you meant : discardCredentials(ThirdPartyServer) ?");
		}
		LOGGER.trace("UserCredentialsCache #{} : settings credentials of class '{}' for user '{}' (set credentials) ...", credentials.getClass(), this.toString(), user);
		if (isCachable(credentials)){
			cache.put(server.getId(), credentials);
			LOGGER.trace("UserCredentialsCache #{} : credentials are allowed to stay in-memory and were stored.");
		}
		else{
			LOGGER.trace("UserCredentialsCache #{} : credentials cannot reside in memory and were discarded.");
		}
	}

	public void uncache(ThirdPartyServer server){
		cache.remove(server.getId());
	}

	public boolean hasCredentials(ThirdPartyServer server) {
		return cache.get(server.getId()) != null;
	}

	/**
	 * Will merge the other credentials cache into this one. The credentials defined in this instance take precedence in case
	 * of conflicts.
	 *
	 * TODO : this method might not be needed anymore, assess whether we can discard it (eg inspect the logs)
	 *
	 * @param otherCache
	 */
	public void absorb(UserCredentialsCache otherCache){

		LOGGER.debug("UserCredentialsCache : merging credentials cache for user '{}'", user);

		if (! this.getUser().equals(otherCache.getUser())){
			throw new IllegalArgumentException("attempted to merge ");
		}

		for (Entry<Long, Credentials> anotherEntry : otherCache.cache.entrySet()){

			Long id = anotherEntry.getKey();
			Credentials crds = anotherEntry.getValue();

			if (! cache.containsKey(id) && crds!= null){
				LOGGER.trace("UserCredentialsCache : Trying to set credentials : UserCredentialsCache : {} . cache : {}", this.toString(), user);
				LOGGER.trace("UserCredentialsCache #{} : settings credentials for user '{}' (via merge)",this.toString(), user);
				// not testing isCachable, we assume the other instance took care of that already.
				cache.put(id, crds);
			}
		}

	}

	public String getUser() {
		return user;
	}

	@Override
	public String toString(){
		return String.format("[user : %s, credentials count : %d]", user, cache.size());
	}

	/**
	 * Returns whether credentials are allowed to stay live. Currently the test is hardcoded and class-driven (basic auth only).
	 *
	 * @param credentials
	 * @return
	 */
	private boolean isCachable(Credentials credentials){
		return BasicAuthenticationCredentials.class.isAssignableFrom(credentials.getClass());
	}

}

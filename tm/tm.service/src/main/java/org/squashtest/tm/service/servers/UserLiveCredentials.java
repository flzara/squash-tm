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
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;


/**
 * <p>
 * Maps a user's credentials for a third party server, which is meant to last for a whole user session in order to maintain the credentials live.
 * It acts like a (crude) cache for {@link org.squashtest.tm.domain.servers.StoredCredentials}. However the primary role of this cache is not
 * the faster retrieval of the said credentials, but rather because they would not be available in the database. For example :
 * </p>
 *
 * <ul>
 *     <li>
 *         	BasicAuthentication credentials are (for now) never stored in the database, so there is no choice but keep them this cache.
 *     		Otherwise they would be lost when the thread ends, and the user would have to reenter them again.
 *     </li>
 * 		<li>
 * 		    On the other hand OAuth1 tokens are stored in the DB. In this case the right choice is to let them live in the DB rather
 * 			than in memory (this is a security flaw after all).
 * 		</li>
 * </ul>
 *
 * <p>
 *     Historically that class was known as BugTrackerContext.
 * </p>
 *
 * @author bsiri
 */

/*
	This class should be synchronized because several threads may attempts to read/write on it : the BugTrackerService might read credentials
	while the BugTrackerAutoconnectCallback might write in it. Call me a fool but I can live with it, considering that the probability of collisions
	are low and consequences are negligible.
 */
@SuppressWarnings("serial")
public class UserLiveCredentials implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserLiveCredentials.class);

	private final String user;

	private final Map<Long, Credentials> liveCredentials = new HashMap<>();


	/**
	 * @param user, must be not null
	 */
	public UserLiveCredentials(String user){
		if (user == null){
			throw new NullArgumentException("UserLiveCredentials : username must not be null");
		}
		this.user = user;
	}

	public Credentials getCredentials(BugTracker server) {
		return liveCredentials.get(server.getId());
	}



	public void setCredentials(BugTracker server, Credentials credentials) {
		if (credentials == null){
			throw new NullArgumentException("UserLiveCredentials : cannot set credentials if they are null. Perhaps you meant : discardCredentials(BugTracker) ?");
		}
		LOGGER.trace("UserLiveCredentials #{} : settings credentials of class '{}' for user '{}' (set credentials) ...", credentials.getClass(), this.toString(), user);
		if (isAllowedInMemory(credentials)){
			liveCredentials.put(server.getId(), credentials);
			LOGGER.trace("UserLiveCredentials #{} : credentials are allowed to stay in-memory and were stored.");
		}
		else{
			LOGGER.trace("UserLiveCredentials #{} : credentials cannot reside in memory and were discarded.");
		}
	}

	public void removeCredentials(BugTracker server){
		liveCredentials.remove(server.getId());
	}

	public boolean hasCredentials(BugTracker bugTracker) {
		return liveCredentials.get(bugTracker.getId()) != null;
	}

	/**
	 * Will merge the other live credentials into this one. The credentials defined in this instance take precedence in case
	 * of conflicts.
	 *
	 * TODO : assess whether that method is still necessary (I suspect it is not called anymore, check the logs to make sure of that)
	 *
	 * @param otherCredentials
	 */
	public void absorb(UserLiveCredentials otherCredentials){

		LOGGER.debug("UserLiveCredentials : merging live credentials for user '{}'", user);

		if (! this.getUser().equals(otherCredentials.getUser())){
			throw new IllegalArgumentException("attempted to merge ");
		}

		for (Entry<Long, Credentials> anotherEntry : otherCredentials.liveCredentials.entrySet()){

			Long id = anotherEntry.getKey();
			Credentials ctxt = anotherEntry.getValue();

			if (! liveCredentials.containsKey(id) && ctxt!= null){
				LOGGER.trace("UserLiveCredentials : Trying to set credentials : UserLiveCredentials : {} . liveCredentials : {}", this.toString(), user);
				LOGGER.trace("UserLiveCredentials #{} : settings credentials for user '{}' (via merge)",this.toString(), user);
				// not testing isAllowedInMemory, we assume the other instance took care of that already.
				liveCredentials.put(id, ctxt);
			}
		}

	}

	public String getUser() {
		return user;
	}

	@Override
	public String toString(){
		return String.format("[user : %s, credentials count : %d]", user, liveCredentials.size());
	}

	/**
	 * Returns whether credentials are allowed to stay live. Currently the test is hardcoded and class-driven (basic auth only).
	 *
	 * @param credentials
	 * @return
	 */
	private boolean isAllowedInMemory(Credentials credentials){
		return BasicAuthenticationCredentials.class.isAssignableFrom(credentials.getClass());
	}

}

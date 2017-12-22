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
package org.squashtest.csp.core.bugtracker.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.Credentials;

/**
 * Bug tracker information for the current thread. This information is exposed through a {@link BugTrackerContextHolder}
 *
 * @author Gregory Fouquet
 *
 */
@SuppressWarnings("serial")
public class BugTrackerContext implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerContext.class);

	private String user;

	private Map<Long, Credentials> bugTrackersCredentials = new HashMap<>();

	public BugTrackerContext(){

	}

	public BugTrackerContext(String user){
		this.user = user;
	}

	public Credentials getCredentials(BugTracker bugTracker) {
		return bugTrackersCredentials.get(bugTracker.getId());
	}


	public void setCredentials(BugTracker bugTracker, Credentials credentials) {
		LOGGER.trace("BugTrackerContext #{} : settings credentials for user '{}' (set credentials)", this.toString(), user);
		bugTrackersCredentials.put(bugTracker.getId(), credentials);
	}

	public boolean hasCredentials(BugTracker bugTracker) {
		return bugTrackersCredentials.get(bugTracker.getId()) != null;
	}

	/**
	 * Will merge the mapping from the other context into this one. The credentials defined in this instance take precedence in case
	 * of conflicts.
	 *
	 * @param anotherContext
	 */
	public void absorb(BugTrackerContext anotherContext){

		if (this.user == null && anotherContext.user != null){
			this.user = anotherContext.user;
		}
		
		for (Entry<Long, Credentials> anotherEntry : anotherContext.bugTrackersCredentials.entrySet()){

			Long id = anotherEntry.getKey();
			Credentials ctxt = anotherEntry.getValue();

			if (! bugTrackersCredentials.containsKey(id) && ctxt!= null){
				LOGGER.trace("BugTrackerContext : Trying to set credentials : BugTrackerContext : {} . bugTrackersCredentials : {}", this.toString(), user);
				LOGGER.trace("BugTrackerContext #{} : settings credentials for user '{}' (via merge)",this.toString(), user);
				bugTrackersCredentials.put(id, ctxt);
			}
		}

	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}


}

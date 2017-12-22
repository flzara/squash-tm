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
package org.squashtest.tm.web.internal.interceptor.openedentity;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Represents a list of users viewing the same object at the same time and how many views have each user opened.
 * see it's use at {@linkplain OpenedEntities}
 * @author mpagnon
 *
 */
public class OpenedEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenedEntity.class);

	private Map<String, Integer> viewers;

	public OpenedEntity() {
		viewers = new HashMap<>();
	}

	public boolean addViewForViewer(String viewerLogin) {
		boolean otherViewers = false;
		// try to find viewer in list
		Integer numberOfViews = viewers.get(viewerLogin);
		// if already here increment number of his view for this entity
		if (numberOfViews != null) {
			numberOfViews++;
			viewers.put(viewerLogin, numberOfViews);
		} else {// else create input for this user
			viewers.put(viewerLogin, 1);
		}
		// if list of users is higher than 1 return true
		if (viewers.size() > 1) {
			otherViewers = true;
		}
		LOGGER.debug("Other Viewers = " + otherViewers);
		return otherViewers;

	}

	public void removeViewForViewer(String viewerLogin) {
		Integer views = viewers.get(viewerLogin);
		if (views != null) {
			views = views - 1;
			if (views <= 0) {
				viewers.remove(viewerLogin);
			}else{
				viewers.put(viewerLogin, views);
			}
		}

	}

	public void removeAllViewsForViewer(String viewerLogin) {
		viewers.remove(viewerLogin);

	}
}

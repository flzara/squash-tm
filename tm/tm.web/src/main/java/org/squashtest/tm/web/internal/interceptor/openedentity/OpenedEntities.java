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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * Opened Entities represent a list of object of one type among the "MANAGED_ENTITIES_LIST". It is used to notify a user
 * if he is not the only one with to view the element. <br>
 * <br>
 * For example an OpenedEntities for TestCases will store , for each test-case is viewed, an entry in the
 * "entitiesViewers" map with it's id as the key. The OpenedEntity value will store the informations of how many users
 * are viewing the test-case and how many views he has opened. <br>
 * <br>
 * <b>How to add an object as a managed entity ? </b>
 * <ol>
 * <li>create an <span style="color:darkgreen">interceptor</span> in the same model as that extends the
 * ObjectViewsInterceptor. declare it in the <span style="color:darkgreen">servlet.xml</span> and map it to the rightful
 * url, make sure the url will return a mav with the object of interest in it. handle the opening of a new view and add
 * the boolean "otherViewers" to the mav.</li>
 * <li>add the <span style="color:darkgreen">component "opened-object"</span> in the view to notify the user if he is
 * not alone viewing this object and to send a quit request if the user leaves the view</li>
 * <li>add the leaveObject method in the <span style="color:darkgreen">ObjectAccessController</span></li>
 * <li>add the object class.simpleName to the <span style="color:darkgreen">MANAGED_ENTITIES_LIST</span> below</li>.
 * This will allow the OpenedEntitiesLifecycleListener to create the needed OpenedEntities at the start of squash and to
 * close the view of a user the end of his session
 * </ol>
 * <br>
 * <b>How to add a view to a managed entity</b>
 * <ol>
 * <li>add the view's access url to the rightful interceptor in the <span style="color:darkgreen">serlvet.xml</span></li>
 * <li>make sure <span style="color:darkgreen">the object is returned in the mav</span> with the same name as in the
 * other hanldled views</li>
 * <li>add the <span style="color:darkgreen">component "opened-object"</span> in the view</li>
 * </ol>
 * 
 * @author mpagnon
 * 
 */
public class OpenedEntities {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenedEntities.class);

	private Map<Long, OpenedEntity> entitiesViewers;

	public static final List<String> MANAGED_ENTITIES_LIST = Arrays.asList(TestCase.class.getSimpleName(),
			Requirement.class.getSimpleName(), Campaign.class.getSimpleName(), Iteration.class.getSimpleName(),
			TestSuite.class.getSimpleName(), Execution.class.getSimpleName());

	public OpenedEntities() {
		entitiesViewers = new HashMap<>();
	}

	public synchronized boolean addViewerToEntity(Identified object, String userLogin) {
		// get the entity || create one if none
		OpenedEntity openedEntity = findOpenedEntity(object);

		// add viewer to entity and return true if viewer is not the only one
		return openedEntity.addViewForViewer(userLogin);
	}

	private synchronized OpenedEntity findOpenedEntity(Identified object) {
		OpenedEntity openedEntity = this.entitiesViewers.get(object.getId());
		if (openedEntity == null) {
			LOGGER.trace("Entity was not listed => new Entity");
			openedEntity = new OpenedEntity();
			this.entitiesViewers.put(object.getId(), openedEntity);
		} else {
			LOGGER.trace("Entity was already listed");
		}
		return openedEntity;
	}

	public synchronized void removeViewer(String viewerLogin) {
		for (Entry<Long, OpenedEntity> entityViewers : entitiesViewers.entrySet()) {
			OpenedEntity openedEntity = entityViewers.getValue();
			openedEntity.removeAllViewsForViewer(viewerLogin);
		}
	}

	public synchronized void removeView(String name, Long id) {
		OpenedEntity openedEntity = this.entitiesViewers.get(id);
		if (openedEntity != null) {
			openedEntity.removeViewForViewer(name);
		}

	}
}

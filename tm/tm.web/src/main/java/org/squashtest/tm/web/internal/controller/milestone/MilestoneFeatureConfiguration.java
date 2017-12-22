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
package org.squashtest.tm.web.internal.controller.milestone;

import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.service.internal.dto.json.JsonMilestone;


/**
 *
 * That class contains whatever is needed for configuring/rendering the feature in the user interface for the various entities
 * (test case, campaign etc). It helps to know which messages should be displayed, if the feature does appear at all and the like.
 *
 * @author bsiri
 *
 */
public class MilestoneFeatureConfiguration {

	/**
	 * This attribute tells whether the milestone functionality is globally enabled.
	 * False means that nothing should be displayed at all.
	 *
	 */
	private boolean globallyEnabled = true;

	/**
	 * Whether the user is actually using the functionality
	 *
	 */
	private boolean userEnabled = true;


	/**
	 * If the entity cannot be modified due to locks because of the milestone status
	 */
	private boolean milestoneLocked = false;


	/**
	 * How many milestones the entity is bound to
	 *
	 */
	private int totalMilestones = 0;


	/**
	 * The identity of the node, as expected by the interface ( ie a 2-uple resid, restype)
	 */
	private Map<String, String> identity = new HashMap<>();


	private JsonMilestone activeMilestone;


	public MilestoneFeatureConfiguration(){
		super();
	}

	public MilestoneFeatureConfiguration(boolean enabled){
		super();
		this.globallyEnabled = false;
	}


	public MilestoneFeatureConfiguration(boolean globallyEnabled, boolean userEnabled, boolean milestoneLocked,
			int totalMilestones, Map<String, String> identity, JsonMilestone activeMilestone) {
		super();
		this.globallyEnabled = globallyEnabled;
		this.userEnabled = userEnabled;
		this.milestoneLocked = milestoneLocked;
		this.totalMilestones = totalMilestones;
		this.identity = identity;
		this.activeMilestone = activeMilestone;
	}

	public boolean isGloballyEnabled() {
		return globallyEnabled;
	}


        public boolean isNormalMode(){
            return ! globallyEnabled;
        }


	public void setGloballyEnabled(boolean globallyEnabled) {
		this.globallyEnabled = globallyEnabled;
	}

	public boolean isUserEnabled() {
		return userEnabled;
	}


	public void setUserEnabled(boolean userEnabled) {
		this.userEnabled = userEnabled;
	}

	public boolean isEnabled() {
		return globallyEnabled;
	}


	public void setEnabled(boolean enabled) {
		this.globallyEnabled = enabled;
	}


	public boolean isMilestoneLocked() {
		return milestoneLocked;
	}


	public void setMilestoneLocked(boolean milestoneLocked) {
		this.milestoneLocked = milestoneLocked;
	}


	public int getTotalMilestones() {
		return totalMilestones;
	}


	public void setTotalMilestones(int totalMilestones) {
		this.totalMilestones = totalMilestones;
	}


	public Map<String, String> getIdentity() {
		return identity;
	}


	public void setIdentity(Map<String, String> identity) {
		this.identity = identity;
	}



	public JsonMilestone getActiveMilestone() {
		return activeMilestone;
	}



	public void setActiveMilestone(JsonMilestone activeMilestone) {
		this.activeMilestone = activeMilestone;
	}

	// *********** meta predicates **************

	public boolean isDisplayTab(){
		return globallyEnabled;
	}

	public boolean isMessagesEnabled(){
		return globallyEnabled;
	}

	public boolean isLocked(){
		return milestoneLocked;
	}

	public boolean isMultipleBindings(){
		return totalMilestones > 1;
	}

	public boolean isEditable(){
		return ! isLocked();
	}

        public boolean isActiveMilestoneCreatable(){
            return (activeMilestone != null) ? activeMilestone.isCanCreateDelete() : true;
        }

	public boolean isMilestoneDatesColumnVisible(){
		return globallyEnabled;
	}

	// this method is an alias of the other
	public boolean isShowLockMessage(){
		return isMessagesEnabled() && isLocked();
	}

	/*
	 * Cautious : split your eyes and read it as :
	 *
	 * "Regardless of whether it is actually the case,
	 * should the message about multiple milestones binding
	 * be displayed when relevant ? "
	 *
	 *
	 * Here is the point :
	 *
	 * returns true -> the message could appear or disappear
	 * according to the modifications that the user applies
	 * when the page is displayed
	 *
	 * returns false -> no such message is ever displayed
	 * regardless of what the user is doing
	 */
	public boolean isShowMultipleBindingMessage(){
		return isMessagesEnabled() && userEnabled && isEditable();
	}

}

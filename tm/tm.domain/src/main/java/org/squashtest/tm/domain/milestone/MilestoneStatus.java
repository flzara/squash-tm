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
package org.squashtest.tm.domain.milestone;

import java.util.ArrayList;
import java.util.List;

import org.squashtest.tm.domain.Level;

public enum MilestoneStatus implements Level {
	// @formatter::off
	PLANNED(1, true, false, false, false),
	IN_PROGRESS(2, true, true, true, true),
	FINISHED(3, true, true, true, true),
	LOCKED(4, false, false, false, false); 
	// @formatter::on



	
	private static final String I18N_KEY_ROOT = "milestone.status.";

	private final int level;
	private final boolean isBindableToProject;
	private final boolean isBindableToObject;
	private final boolean allowObjectCreateAndDelete;
	private final boolean allowObjectModification;
	
	private MilestoneStatus(int level, boolean isBindableToProject, boolean isBindableToObject, boolean allowObjectCreateAndDelete, boolean allowObjectModification) {
		this.level = level;
		this.isBindableToProject = isBindableToProject;
		this.isBindableToObject = isBindableToObject;
		this.allowObjectCreateAndDelete = allowObjectCreateAndDelete;
		this.allowObjectModification = allowObjectModification;
	}
	
	public static MilestoneStatus getByLevel(int level){
		
		for (MilestoneStatus status : MilestoneStatus.values()){
			if (status.getLevel() == level){
				return status;
			}
		}
		
		throw new IllegalArgumentException("Does not match any level : " + level);
	}
	
	public static List<MilestoneStatus> getAllStatusAllowingObjectBind(){
		
		List<MilestoneStatus> result = new ArrayList<>();
		for (MilestoneStatus status : MilestoneStatus.values()){
			if (status.isBindableToObject){
				result.add(status);
			}
		}
		return result;
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}

	@Override
	public int getLevel() {
		return level;
	}

	public boolean isBindableToProject() {
		return isBindableToProject;
	}

	public boolean isBindableToObject() {
		return isBindableToObject;
	}

	public boolean isAllowObjectCreateAndDelete() {
		return allowObjectCreateAndDelete;
	}

	public boolean isAllowObjectModification() {
		return allowObjectModification;
	}

}

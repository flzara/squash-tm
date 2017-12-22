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
package org.squashtest.tm.plugin.testautomation.jenkins.beans;

import java.util.Arrays;

public class Build {

	private Action[] actions;
	private int number;
	private boolean building;


	public Action[] getActions() {
		return actions;
	}

	public void setActions(Action[] actions) {	//NOSONAR that array is definitely not stored directly
		this.actions = Arrays.copyOf(actions, actions.length);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public boolean isBuilding() {
		return building;
	}

	public void setBuilding(boolean building) {
		this.building = building;
	}

	public Build(){
		super();
	}

	public boolean hasExternalId(String externalId){
		Parameter extIdParam = Parameter.newExtIdParameter(externalId);

		for (Action action : actions){
			if (action.hasParameter(extIdParam)){
				return true;
			}
		}

		return false;
	}

	public boolean hasId(int id){
		return this.getNumber() == id;
	}

	public int getId(){
		return this.getNumber();
	}

}

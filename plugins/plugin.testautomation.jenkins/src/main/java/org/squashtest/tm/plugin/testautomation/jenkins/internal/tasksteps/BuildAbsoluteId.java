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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps;


/**
 * <p>
 * 	That class identifies a build using the job name and the externalId it was given as parameter (as for any TA job).
 * 	Additionally one will specify the buildId (the one Jenkins knows) at some point during the build survey, once it
 * 	has been retrived from Jenkins. The buildId cannot be known before the build starts.
 * </p>
 *
 * <p>
 * 	Basically that class will be shared among many classes, some consuming data (project name, external id), some other
 * 	will feed data (the buildId).
 * </p>
 *
 *
 *
 * @author bsiri
 *
 */
public class BuildAbsoluteId {

	private String projectName;

	private String externalId;

	private Integer buildId = null;

	public BuildAbsoluteId(String projectName, String externalId){
		this.projectName = projectName;
		this.externalId = externalId;
	}

	public Integer getBuildId() {
		return buildId;
	}

	public void setBuildId(Integer buildId) {
		this.buildId = buildId;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getExternalId() {
		return externalId;
	}

	@Override
	public String toString(){
		return "project '"+projectName+"', build : external id '"+externalId+"', jenkins id '"+buildId+"'";
	}

	public boolean hasBuildIdSet(){
		return buildId != null;
	}

}

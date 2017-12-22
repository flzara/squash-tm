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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.Build;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.BuildList;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.ItemList;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.Job;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.JobList;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.TestListElement;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;
import org.squashtest.tm.service.testautomation.spi.UnreadableResponseException;


public class JsonParser {

	private static final String DISABLED_COLOR_STRING = "disabled";

	private ObjectMapper objMapper = new ObjectMapper();

        public JsonParser(){
            super();
            objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

	public Collection<TestAutomationProject> readJobListFromJson(String json){

		try {

			JobList list = objMapper.readValue(json, JobList.class);

			JobList filteredList = filterDisabledJobs(list);

			return toProjectList(filteredList);

		}
		catch (IOException e) {
			throw new UnreadableResponseException(e);
		}

	}


	public ItemList getQueuedListFromJson(String json){
		return safeReadValue(json, ItemList.class);
	}

	public BuildList getBuildListFromJson(String json){
		return safeReadValue(json, BuildList.class);
	}

	public TestListElement getTestListFromJson(String json){
		return safeReadValue(json, TestListElement.class);
	}

	public Build getBuildFromJson(String json){
		return safeReadValue(json, Build.class);
	}

	public String toJson(Object object){
		try {
			return objMapper.writeValueAsString(object);
		}
		catch (IOException e) {
			throw new TestAutomationException("TestAutomationConnector : internal error, could not generate json", e);
		}
	}

	protected <R> R safeReadValue(String json, Class<R> clazz){

		try {
			return objMapper.readValue(json, clazz);
		}
		catch (JsonParseException | JsonMappingException e) {
			throw new UnreadableResponseException("TestAutomationConnector : the response from the server couldn't be treated", e);
		} catch (IOException e) {
			throw new UnreadableResponseException("TestAutomationConnector : internal error :", e);
		}

	}


	protected JobList filterDisabledJobs(JobList fullList){
		JobList newJobList = new JobList();
		for (Job job : fullList.getJobs()){
			if (! job.getColor().equals(DISABLED_COLOR_STRING)){
				newJobList.getJobs().add(job);
			}
		}
		return newJobList;
	}


	protected Collection<TestAutomationProject> toProjectList(JobList jobList){

		Collection<TestAutomationProject> projects = new ArrayList<>();

		for (Job job : jobList.getJobs()){
			projects.add(new TestAutomationProject(job.getName()));
		}

		return projects;


	}


}

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
package org.squashtest.tm.web.internal.controller.testautomation;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

/**
 * json-friendly version of {@link TestAutomationProjectContent}.
 * 
 * @author bsiri
 * 
 */
public class TestAutomationProjectContentModel {

	private TestAutomationProjectModel project;
	private AutomatedTestModel[] tests;
	private boolean orderGuaranteed ;

	public TestAutomationProjectModel getProject() {
		return project;
	}

	public void setProject(TestAutomationProjectModel project) {
		this.project = project;
	}

	public AutomatedTestModel[] getTests() {
		return tests;
	}

	public void setTests(AutomatedTestModel[] tests) {
		this.tests = tests;
	}

	public boolean isOrderGuaranteed() {
		return orderGuaranteed;
	}

	public void setOrderGuaranteed(boolean orderGuaranteed) {
		this.orderGuaranteed = orderGuaranteed;
	}

	public TestAutomationProjectContentModel(TestAutomationProjectContent content){
		this.project = new TestAutomationProjectModel(content.getProject());
		this.orderGuaranteed = content.isOrderGuaranteed();
		Collection<AutomatedTestModel> tmodels = new ArrayList<>(content.getTests().size());
		for (AutomatedTest test : content.getTests()){
			tmodels.add(new AutomatedTestModel(test));
		}
		tests = tmodels.toArray(new AutomatedTestModel[tmodels.size()]);
	}


	public static final class TestAutomationProjectModel{
		private Long id;
		private String jobName;
		private String label;
		private String[] nodes;
		private TestAutomationServerModel server;


		public TestAutomationProjectModel(TestAutomationProject project){
			this.id = project.getId();
			this.jobName = project.getJobName();
			this.label = project.getLabel();
			this.nodes = project.getSlaves()
					.trim()
					.replaceAll("\\s*;\\s*", ";")
					.split(";");

			if (project.getServer() != null){
				this.server = new TestAutomationServerModel(project.getServer());
			}
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getJobName() {
			return jobName;
		}

		public void setJobName(String jobName) {
			this.jobName = jobName;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String[] getNodes() {
			return nodes;
		}

		public void setNodes(String[] nodes) {
			this.nodes = nodes;
		}

		public TestAutomationServerModel getServer() {
			return server;
		}

		public void setServer(TestAutomationServerModel server) {
			this.server = server;
		}

	}

	public static final class TestAutomationServerModel {
		private Long id;
		private String name;
		private URL baseURL;
		private String kind;
		private boolean manualSlaveSelection;

		public TestAutomationServerModel(TestAutomationServer server) {
			this.id = server.getId();
			this.name = server.getName();
			this.baseURL = server.getBaseURL();
			this.kind = server.getKind();
			this.manualSlaveSelection = server.isManualSlaveSelection();
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public URL getBaseURL() {
			return baseURL;
		}

		public void setBaseURL(URL baseURL) {
			this.baseURL = baseURL;
		}

		public String getKind() {
			return kind;
		}

		public void setKind(String kind) {
			this.kind = kind;
		}

		public boolean isManualSlaveSelection() {
			return manualSlaveSelection;
		}

		public void setManualSlaveSelection(boolean manualSlaveSelection) {
			this.manualSlaveSelection = manualSlaveSelection;
		}

	}

	private static final class AutomatedTestModel {
		private String name;

		public AutomatedTestModel(AutomatedTest test) {
			this.name = test.getName();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

}

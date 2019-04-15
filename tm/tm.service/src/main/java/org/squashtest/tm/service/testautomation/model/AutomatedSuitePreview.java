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
package org.squashtest.tm.service.testautomation.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * <p>
 *     This class is a preview of an AutomatedSuite would look like. It is designed to send an instant response to the
 *     user in the GUI; so it has a very dedicated purpose and is not very useful beyond that.
 * </p>
 *
 * <p>
 *     Note that in particular the instances of TestAutomationProjectContent WILL NOT include tests.
 *     Their test attribute will be an empty collection. Because it's faster that way.
 * </p>
 */
public class AutomatedSuitePreview {

	private boolean isManualSlaveSelection = false;	
	
	private AutomatedSuiteCreationSpecification specification = null;

	private Collection<TestAutomationProjectPreview> projects = new ArrayList<>();

	public boolean isManualSlaveSelection() {
		return isManualSlaveSelection;
	}

	public void setManualSlaveSelection(boolean manualSlaveSelection) {
		isManualSlaveSelection = manualSlaveSelection;
	}

	public AutomatedSuiteCreationSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(AutomatedSuiteCreationSpecification specification) {
		this.specification = specification;
	}

	public Collection<TestAutomationProjectPreview> getProjects() {
		return projects;
	}

	public void setProjects(Collection<TestAutomationProjectPreview> projects) {
		this.projects = projects;
	}
	
	

	public static final class TestAutomationProjectPreview{
		private long projectId;
		private String label;
		private String server;
		private Collection<String> nodes;
		private long testCount = 0;
		
		/*
		 * TODO : maybe do the orderGuaranteed check. It involves something along the line of 
		 * AutomatedSuiteManagerServiceImpl#sortByProject().
		 * 
		 * However computing this is a performance killer (one among many) which only purpose is to notify the user of the 
		 * possibility that his hundreds of automated tests may not run sequentially as specified by the test plan, which 
		 * entails that the test statuses may not quite turn from gray to blue and then green orderly from top to bottom, 
		 * which would  make him sad because he really really wanted to sit and watch through the execution of the whole 
		 * things for hours and see his bullets change color in the order he wants. 
		 * 
		 * That flag is an epitomic example of the insane wish of turning an essentially long-running, asynchronous task
		 * into some pseudo-interactive gizmo that only makes sense in a show case (remotely even so).
		 * 
		 * Here is what happens in the real world : the user clicks 'run all those tests' and then closes the dialog and forget 
		 * about it. No one cares of the test plan order. I'm not computing that flag. If someone ever notices the change in
		 * the behavior and raises an issue about it I will reconsider, but I firmly believe that this day will never come.
		 * 
		 * /rant
		 */
		private boolean orderGuaranteed = true;

		public TestAutomationProjectPreview(){

		}

		public TestAutomationProjectPreview(long projectId, String label, String server, Collection<String> nodes, long testCount) {
			super();
			this.projectId = projectId;
			this.label = label;
			this.server = server;
			this.nodes = nodes;
			this.testCount = testCount;
		}
		
		public TestAutomationProjectPreview(long projectId, String label, String server, String nodesAsCsv, long testCount) {
			super();
			this.projectId = projectId;
			this.label = label;
			this.server = server;
			setNodes(nodesAsCsv);
			this.testCount = testCount;
		}



		public long getProjectId() {
			return projectId;
		}

		public void setProjectId(long projectId) {
			this.projectId = projectId;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public Collection<String> getNodes() {
			return nodes;
		}

		public void setNodes(Collection<String> nodes) {
			this.nodes = nodes;
		}
		
		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public boolean isOrderGuaranteed() {
			return orderGuaranteed;
		}

		public void setOrderGuaranteed(boolean orderGuaranteed) {
			this.orderGuaranteed = orderGuaranteed;
		}

		public long getTestCount() {
			return testCount;
		}

		public void setTestCount(long testCount) {
			this.testCount = testCount;
		}

		private void setNodes(String asCsv){
			String[] nodesArray = asCsv
								 .trim()
								 .replaceAll("\\s*;\\s*", ";")
								 .split(";");

			this.nodes = Arrays.asList(nodesArray);
		}
	}
}

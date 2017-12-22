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

import java.util.List;
import java.util.Map;


/**
 * see documentation of this in scripts/milestones/milestone-panel.js
 * 
 * @author bsiri
 *
 */
public class MilestonePanelConfiguration {

	private String rootPath;

	private Map<String, String> identity;

	private String nodeType;

	private List<?> currentModel;

	private boolean editable;
	
	private boolean milestoneInProject;

	// whether you can select more than one milestone in the bind-milestone popup
	private boolean multilines=true;

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public Map<String, String> getIdentity() {
		return identity;
	}

	public void setIdentity(Map<String, String> identity) {
		this.identity = identity;
	}

	public List<?> getCurrentModel() {
		return currentModel;
	}

	public void setCurrentModel(List<?> currentModel) {
		this.currentModel = currentModel;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public boolean isMultilines() {
		return multilines;
	}

	public void setMultilines(boolean multilines) {
		this.multilines = multilines;
	}

	public boolean isMilestoneInProject() {
		return milestoneInProject;
	}

	public void setIsMilestoneInProject(boolean isMilestoneInProject) {
		this.milestoneInProject = isMilestoneInProject;
	}


}

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
package org.squashtest.tm.service.project;

/**
 * Conf class used to limit the scope of synchronisation between two generic projects.
 * @author jthebault
 *
 */
public class GenericProjectCopyParameter {

	private boolean copyPermissions;
	private boolean copyCUF;
	private boolean copyBugtrackerBinding;
	private boolean copyAutomatedProjects;
	private boolean copyInfolists;
	private boolean copyMilestone;
	private boolean copyAllowTcModifFromExec;

	public GenericProjectCopyParameter() {
		//default constructor
	}


	public GenericProjectCopyParameter(boolean copyPermissions,
			boolean copyCUF, boolean copyBugtrackerBinding,
			boolean copyAutomatedProjects, boolean copyInfolists,
			boolean copyMilestone, boolean copyAllowTcModifFromExec) {
		this.copyPermissions = copyPermissions;
		this.copyCUF = copyCUF;
		this.copyBugtrackerBinding = copyBugtrackerBinding;
		this.copyAutomatedProjects = copyAutomatedProjects;
		this.copyInfolists = copyInfolists;
		this.copyMilestone = copyMilestone;
		this.copyAllowTcModifFromExec = copyAllowTcModifFromExec;
	}



	public boolean isCopyPermissions() {
		return copyPermissions;
	}
	public void setCopyPermissions(boolean copyPermissions) {
		this.copyPermissions = copyPermissions;
	}
	public boolean isCopyCUF() {
		return copyCUF;
	}
	public void setCopyCUF(boolean copyCUF) {
		this.copyCUF = copyCUF;
	}
	public boolean isCopyBugtrackerBinding() {
		return copyBugtrackerBinding;
	}
	public void setCopyBugtrackerBinding(boolean copyBugtrackerBinding) {
		this.copyBugtrackerBinding = copyBugtrackerBinding;
	}
	public boolean isCopyAutomatedProjects() {
		return copyAutomatedProjects;
	}
	public void setCopyAutomatedProjects(boolean copyAutomatedProjects) {
		this.copyAutomatedProjects = copyAutomatedProjects;
	}
	public boolean isCopyInfolists() {
		return copyInfolists;
	}
	public void setCopyInfolists(boolean copyInfolists) {
		this.copyInfolists = copyInfolists;
	}
	public boolean isCopyMilestone() {
		return copyMilestone;
	}
	public void setCopyMilestone(boolean copyMilestone) {
		this.copyMilestone = copyMilestone;
	}

	public boolean isCopyAllowTcModifFromExec() {
		return copyAllowTcModifFromExec;
	}

	public void setCopyAllowTcModifFromExec(boolean copyAllowTcModifFromExec) {
		this.copyAllowTcModifFromExec = copyAllowTcModifFromExec;
	}
}

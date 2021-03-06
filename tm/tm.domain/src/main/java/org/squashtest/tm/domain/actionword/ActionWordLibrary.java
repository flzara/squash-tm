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
package org.squashtest.tm.domain.actionword;

import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.tree.GenericTreeLibrary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class ActionWordLibrary extends GenericTreeLibrary implements ActionWordTreeEntity {

	@Id
	@Column(name="AWL_ID")
	@GeneratedValue(strategy= GenerationType.AUTO, generator="action_word_library_awl_id_seq")
	@SequenceGenerator(name="action_word_library_awl_id_seq", sequenceName="action_word_library_awl_id_seq", allocationSize = 1)
	private Long id;

	@OneToOne(mappedBy = "actionWordLibrary")
	private GenericProject project;

	public Long getId() {
		return id;
	}

	@Override
	public Project getProject() {
		return (Project) project;
	}

	/* ActionWordTreeEntity methods */

	@Override
	public void accept(ActionWordTreeEntityVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public ActionWordTreeEntity createCopy() {
		throw new UnsupportedOperationException("Cannot copy a library.");
	}
	/* TreeLibrary methods */

	@Override
	public void notifyAssociatedWithProject(GenericProject project) {
		this.project = project;
	}

	/* TreeEntity methods */

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public void setName(String name) {

	}

	@Override
	public void setProject(Project project) {
		throw new UnsupportedOperationException("A library cannot be renamed. Please rename the project instead.");
	}
}

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
package org.squashtest.tm.domain.library;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectResource;

/**
 * Interface for a content node of a library.
 *
 * @author Gregory Fouquet
 *
 */
public interface LibraryNode extends Copiable, Identified, AttachmentHolder, TreeNode, ProjectResource<Project>  {
	/**
	 * @return Name of this node.
	 */
	@Override
	String getName();

	/**
	 *
	 * @param name
	 *            The name of this node. Should not be blank or null.
	 */
	@Override
	void setName(String name);

	/***
	 *
	 * @param newDescription
	 *            the new node description
	 */
	void setDescription(String newDescription);

	String getDescription();

	/**
	 * Notifies this resource now belongs to the given project. {@link getProject()} should
	 * return this project afterwards.
	 *
	 * @param project
	 *            should not be <code>null</code>
	 */
	void notifyAssociatedWithProject(Project project);
}

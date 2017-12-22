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

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.exception.DuplicateNameException;

import javax.validation.constraints.NotNull;

/**
 * Interface for an object which contains tree node objects.
 *
 * @author Gregory Fouquet
 *
 * @param <NODE>
 *            Type of contained node
 */
public interface NodeContainer<NODE extends TreeNode> extends Identified {
	/**
	 * Adds new content to this container. Should refuse to add null content, should refuse to add content with
	 * duplicate
	 * name.
	 *
	 * @param node
	 */
	void addContent(@NotNull NODE node) throws DuplicateNameException, NullArgumentException;

	/**
	 * Adds new content to this container at the given position. Should refuse to add null content, should refuse to add
	 * content with duplicate
	 * name.
	 *
	 * @param node
	 */
	void addContent(@NotNull NODE node, int position) throws DuplicateNameException, NullArgumentException;

	/**
	 * Indicate if this container allow two contents nodes to have the same name.
	 * By default it's FALSE, only requirement workspace allow this rule to be broke, thanks to the milestone mess.
	 * @return FALSE if not allowed (default), TRUE if allowed , witch mean that a subclass has override this method
	 */
	default boolean allowContentWithIdenticalName(){return false;}

	boolean isContentNameAvailable(String name);

	List<NODE> getContent();

	/**
	 * Will return the ordered (if order there is) content nodes.
	 *
	 * @return
	 */
	Collection<NODE> getOrderedContent();

	boolean hasContent();

	void removeContent(NODE contentToRemove) throws NullArgumentException;

	List<String> getContentNames();

	/**
	 * A node container can be a library. That can be bound to a project template.
	 *
	 * @return
	 */
	GenericProject getProject();

	void accept(NodeContainerVisitor visitor);

}

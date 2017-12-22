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
package org.squashtest.tm.domain.project;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.library.LibraryNodeUtils;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

/**
 * Abstract superclass of {@link Library} implementations based on generics.
 *
 * @author Gregory Fouquet
 *
 * @param <NODE>
 *            The type of nodes this library contains.
 */
@MappedSuperclass
public abstract class GenericLibrary<NODE extends LibraryNode> implements Library<NODE> {
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	public GenericLibrary() {
		super();
	}

	@Override
	public boolean isContentNameAvailable(String name) {
		if(allowContentWithIdenticalName()){
			return true;
		}
		for (NODE content : getContent()) {
			if (content.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @throws UnsupportedOperationException
	 *             when trying to add a node to a project template.
	 */
	@Override
	public void addContent(@NotNull final NODE node) throws UnsupportedOperationException {
		checkContentNameAvailable(node);
		getContent().add(node);

		getProject().accept(new ProjectVisitor() {
			@Override
			public void visit(Project project) {
				node.notifyAssociatedWithProject(project);

			}

			@Override
			public void visit(ProjectTemplate projectTemplate) {
				// should not happen. If so, programming error.
				throw new UnsupportedOperationException(LibraryNodeUtils.toString(node) + " cannot be added to "
						+ ProjectUtils.toString(projectTemplate));

			}
		});
	}

	@Override
	public void addContent(@NotNull final NODE node, int position) throws UnsupportedOperationException {
		checkContentNameAvailable(node);

		if (position >= getContent().size() || position < 0) {
			getContent().add(node);
		} else {
			getContent().add(position, node);
		}

		getProject().accept(new ProjectVisitor() {
			@Override
			public void visit(Project project) {
				node.notifyAssociatedWithProject(project);

			}

			@Override
			public void visit(ProjectTemplate projectTemplate) {
				// should not happen. If so, programming error.
				throw new UnsupportedOperationException(LibraryNodeUtils.toString(node) + " cannot be added to "
						+ ProjectUtils.toString(projectTemplate));

			}
		});

		/*
		 * because Hibernate and the triggers don't work along well we have to make sure that
		 * data are inserted with their correct index (INSERT only, we don't want messy UPDATE)
		 */
		List<NODE> orig = getContent();
		List<NODE> reindexed = new ArrayList<>(orig);
		orig.clear();
		for (NODE n : reindexed) {
			orig.add(n);
		}
	}

	/**
	 * checks that content name has not been already given. Throws exception otherwise.
	 *
	 * @param candidateContent
	 */
	private void checkContentNameAvailable(NODE candidateContent) throws DuplicateNameException {
		if (!isContentNameAvailable(candidateContent.getName())) {
			throw new DuplicateNameException(candidateContent.getName(), candidateContent.getName());
		}
	}

	@AclConstrainedObject
	@Override
	public Library<?> getLibrary() {
		return this;
	}

	@Override
	public List<String> getContentNames() {
		List<String> contentNames = new ArrayList<>(getContent().size());
		for (NODE node : getContent()) {
			contentNames.add(node.getName());
		}
		return contentNames;
	}

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

}

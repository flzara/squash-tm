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

import java.util.ArrayList;
import java.util.List;

import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.DuplicateNameException;

/**
 * This class is meant to be used as a delegate when one implements a {@link Folder}.
 *
 * @author Gregory Fouquet
 *
 * @param <NODE>
 */
public class FolderSupport<NODE extends LibraryNode, FOLDER extends Folder<NODE>> {
	/**
	 * The folder which delegates operations to this object.
	 */
	private final FOLDER folder;

	public FolderSupport(FOLDER folder) {
		super();
		this.folder = folder;
	}

	/**
	 * Adds content to {@link #folder} after checking the content can be added.
	 *
	 * @param node
	 *            the content to add
	 */
	public void addContent(NODE node) {
		checkContentNameAvailable(node);
		folder.getContent().add(node);
		node.notifyAssociatedWithProject(folder.getProject());
	}

	public void addContent(NODE node, int position) {
		checkContentNameAvailable(node);
		if (position >= folder.getContent().size() || position < 0) {
			folder.addContent(node);
		} else {
			folder.getContent().add(position, node);
		}
		node.notifyAssociatedWithProject(folder.getProject());
	}

	private void checkContentNameAvailable(NODE candidateContent) throws DuplicateNameException {
		if (!this.folder.allowContentWithIdenticalName() && !isContentNameAvailable(candidateContent.getName())) {
			throw new DuplicateNameException(candidateContent.getName(), candidateContent.getName());
		}
	}

	/**
	 * Tells if the given name is already attributed to any of {@link #folder}'s content.
	 *
	 * @param name
	 * @return
	 */
	public boolean isContentNameAvailable(String name) {
		for (NODE folderContent : folder.getContent()) {
			if (folderContent.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Notifies that the project was set to something. Notifies each of {@link #folder}'s content it is now associated
	 * with a new project.
	 *
	 * We dont want to expose a "setProject" method in folders, so the folder is reponsible for setting the project
	 * association, then it can extend the operation by calling this method.
	 *
	 * @param formerProject
	 *            former value of {@link #folder}'s associated project
	 * @param currentProject
	 *            new value of {@link #folder}'s associated project
	 */
	public void notifyAssociatedProjectWasSet(Project formerProject, Project currentProject) {
		if (notSameProject(formerProject, currentProject)) {
			for (NODE node : folder.getContent()) {
				node.notifyAssociatedWithProject(currentProject);
			}
		}
	}

	private boolean notSameProject(Project thisProject, Project thatProject) {
		if (thisProject == null && thatProject == null) {
			return false;
		} else if (thisProject == null) {
			return true;
		} else {
			return !thisProject.equals(thatProject);
		}
	}

	public boolean hasContent() {
		return !folder.getContent().isEmpty();
	}

	public FOLDER createCopy(FOLDER newFolder) {
		newFolder.setName(folder.getName());
		newFolder.setDescription(folder.getDescription());
		newFolder.notifyAssociatedWithProject(folder.getProject());
		addCopiesOfAttachments(folder, newFolder);
		return newFolder;
	}

	private void addCopiesOfAttachments(Folder<NODE> source, Folder<NODE> destination) {
		for (Attachment tcAttach : source.getAttachmentList().getAllAttachments()) {
			Attachment atCopy = tcAttach.hardCopy();
			destination.getAttachmentList().addAttachment(atCopy);
		}
	}

	public List<String> getContentNames() {
		List<String> contentNames = new ArrayList<>(folder.getContent().size());
		for (NODE node : folder.getContent()) {
			contentNames.add(node.getName());
		}
		return contentNames;
	}

}

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
package org.squashtest.tm.domain.requirement;

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.FolderSupport;
import org.squashtest.tm.domain.library.NodeContainerVisitor;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.resource.SimpleResource;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@SuppressWarnings("rawtypes")
@Entity
@PrimaryKeyJoinColumn(name = "RLN_ID")
public class RequirementFolder extends RequirementLibraryNode<SimpleResource> implements Folder<RequirementLibraryNode> {
	/**
	 * Delegate implementation of folder responsibilities.
	 */
	@Transient
	private final FolderSupport<RequirementLibraryNode, RequirementFolder> folderSupport = new FolderSupport<>(this);


        /*
        Note about cascading:
        CascadeType.PERSIST is desirable because it allows us to cascade-create a complete grape of object (useful when importing for instance)
        CascadeType.DELETE is not desirable, because we need to call custom code for proper deletion (see the deletion services)
        */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE})
	@OrderColumn(name = "CONTENT_ORDER")
	@JoinTable(name = "RLN_RELATIONSHIP", joinColumns = @JoinColumn(name = "ANCESTOR_ID"), inverseJoinColumns = @JoinColumn(name = "DESCENDANT_ID"))
	private List<RequirementLibraryNode> content = new ArrayList<>();

	@OneToOne(cascade = { CascadeType.ALL })
	@NotNull
	@JoinColumn(name = "RES_ID", updatable = false)
	private SimpleResource resource;

	@OneToOne(mappedBy = "requirementFolder", cascade = CascadeType.ALL, optional = true)
	private RequirementFolderSyncExtender requirementFolderSyncExtender;

	public RequirementFolder() {
		resource = new SimpleResource();
	}
	public RequirementFolder(Date createdOn, String createdBy) {
		AuditableMixin audit = (AuditableMixin) this;

		audit.setCreatedOn(createdOn);
		audit.setCreatedBy(createdBy);

		resource = new SimpleResource();
	}
	@Override
	public List<RequirementLibraryNode> getContent() {
		return content;
	}

	@Override
	public void accept(RequirementLibraryNodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(NodeContainerVisitor visitor) {
		visitor.visit(this);
	}


	@Override
	public void removeContent(RequirementLibraryNode contentToRemove) throws NullArgumentException {
		content.remove(contentToRemove);
		content = new ArrayList<>(content);
	}

	@Override
	public void addContent(RequirementLibraryNode node) {
		folderSupport.addContent(node);
	}

	@Override
	public void addContent(RequirementLibraryNode node, int position) {
		folderSupport.addContent(node, position);
		// the following enforces that hibernate reinsert the data with their index,
		// and makes sure it works along the triggers.
		content = new ArrayList<>(content);
	}

	@Override
	public boolean isContentNameAvailable(String name) {
		return folderSupport.isContentNameAvailable(name);
	}

	@Override
	public RequirementFolder createCopy() {
		return folderSupport.createCopy(new RequirementFolder());
	}


	@Override
	public void notifyAssociatedWithProject(Project project) {
		Project former = getProject();
		super.notifyAssociatedWithProject(project);
		folderSupport.notifyAssociatedProjectWasSet(former, project);

	}

	@Override
	public boolean hasContent() {
		return folderSupport.hasContent();
	}

	@Override
	public void setName(String name) {
		resource.setName(name);
	}

	@Override
	public void setDescription(String description) {
		resource.setDescription(description);
	}

	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public String getDescription() {
		return resource.getDescription();
	}

	@Override
	public SimpleResource getResource() {
		return resource;
	}

	public void emptyContent() {
		this.content.clear();

	}
	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public List<String> getContentNames() {
		return folderSupport.getContentNames();
	}
	@Override
	public Collection<RequirementLibraryNode> getOrderedContent() {
		return content;
	}

	@Override
	public boolean allowContentWithIdenticalName() {
		return true;
	}

	public RequirementFolderSyncExtender getRequirementFolderSyncExtender() {
		return requirementFolderSyncExtender;
	}

	public void setRequirementFolderSyncExtender(RequirementFolderSyncExtender requirementFolderSyncExtender) {
		this.requirementFolderSyncExtender = requirementFolderSyncExtender;
	}

	public boolean isSynchronized(){
		return this.requirementFolderSyncExtender != null;
	}
}

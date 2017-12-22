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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Table;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.resource.Resource;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
@Indexed

/*
 *  the following tells hibernate additional informations about the join table RLN_RESOURCE, that we want to be readonly. Hence we give
 *  it a custom sql string that is effectively useless and hopefully crossplatform. 
 *  
 *  Note that, because RLN_RESOURCE is a view and shall not be targeted by any insert/update/delete command, we have to 
 *  find another table for which the database will not be such a pussy about. Why not the table Requirement itself then ?
 */
@Table(appliesTo="RLN_RESOURCE", sqlDelete=@SQLDelete(sql="delete from REQUIREMENT where RLN_ID=null and RLN_ID=?"))
public abstract class RequirementLibraryNode<RESOURCE extends Resource> implements LibraryNode {
	@Id
	@Column(name = "RLN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_library_node_rln_id_seq")
	@SequenceGenerator(name = "requirement_library_node_rln_id_seq", sequenceName = "requirement_library_node_rln_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "PROJECT_ID")
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Project project;

	@Override
	public Project getProject() {
		return project;
	}
	
	
	/**
	 * <p>This is not a business attribute and should not be used in services. This mapping 
	 * exists solely to make hql queries on it. It allows for fast retrieval of the 
	 * name (of a folder, or of the newest version of a requirement).</p> 
	 * 
	 */
	@OneToOne(fetch=FetchType.LAZY)
	@JoinTable(name="RLN_RESOURCE", 
	joinColumns=@JoinColumn(name="RLN_ID", insertable=false, updatable=false ),
	inverseJoinColumns = @JoinColumn(name="RES_ID"))
	@Immutable
	private Resource mainResource;
	
	public Resource getMainResource(){
		return mainResource;
	}
	
	/**
	 * Notifies this object it is now a resource of the given project.
	 *
	 * @param project
	 */
	@Override
	public void notifyAssociatedWithProject(Project project) {
		this.project = project;

	}

	public RequirementLibraryNode() {
		super();
	}

	public RequirementLibraryNode(String name, String description) {
		setName(name);
		setDescription(description);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	@AclConstrainedObject
	public Library<?> getLibrary() {
		return getProject().getRequirementLibrary();
	}

	@Override
	public AttachmentList getAttachmentList() {
		return getResource().getAttachmentList();
	}

	/**
	 * Implementors should ask the visitor to visit this object.
	 *
	 * @param visitor
	 */
	public abstract void accept(RequirementLibraryNodeVisitor visitor);

	public abstract RESOURCE getResource();
	
}

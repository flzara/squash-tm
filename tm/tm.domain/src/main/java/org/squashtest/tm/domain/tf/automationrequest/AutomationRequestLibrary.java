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
package org.squashtest.tm.domain.tf.automationrequest;


import org.hibernate.annotations.BatchSize;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.project.GenericProject;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This "library" contains the {@link AutomationRequestStatus}. </p>
 *
 * <p>The name "library" here is misleading, because
 * the business rules of the automation requests are drastically different from that of a regular library node,
 * eg TestCase : it has no name, is never displayed in a tree, has no independant lifecycle - in particular the user never
 * creates or deletes them explicitly - etc. Here we are much closer of a classic ticket, which are usually managed as
 * an unstructured bag.
 * </p>
 *
 * <p>
 *     As such it does not extends and implement the other signatures you would expect from other libraries. Still, as a
 *     structure dedicated to hold AutomationRequests, it remains a library on a conceptual level.
 * </p>
 *
 * <p>
 *     On a practical level though, it also acts as an ACL holder, to which the AutomationRequets can delegate to, just
 *     like other entities subjected to domain protection.
 * </p>
 *
 *
 */
@Entity
public class AutomationRequestLibrary {

	@Id
	@Column(name = "ARL_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "automation_request_library_arl_id_seq")
	@SequenceGenerator(name = "automation_request_library_arl_id_seq", sequenceName = "automation_request_library_arl_id_seq", allocationSize = 1)
	private Long id;

	/**
	 * Careful : we absolutely want not to load everything here because as it stands this collections holds every single ticket for
	 * the project. Most of the time you won't need to iterate over the rootContent (use an appropriate service for that).
	 */
	@OneToMany // no cascade is desired because we need to handle it manually
	@JoinTable(name = "AUTOMATION_REQUEST_LIBRARY_CONTENT", joinColumns = @JoinColumn(name = "LIBRARY_ID"), inverseJoinColumns = @JoinColumn(name = "CONTENT_ID"))
	@BatchSize(size=10)
	private List<AutomationRequest> rootContent = new ArrayList<>();

	@OneToOne(mappedBy = "automationRequestLibrary")
	private GenericProject project;

	// XXX for now the use has no way to add/remove/download attachments of a AR library, it is planed for a later time.
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();


	public void notifyAssociatedWithProject(GenericProject p) {
		this.project = p;
	}

	public Long getId() {
		return id;
	}

	public List<AutomationRequest> getRootContent() {
		return rootContent;
	}

	public void setRootContent(List<AutomationRequest> rootContent) {
		this.rootContent = rootContent;
	}

	public GenericProject getProject() {
		return project;
	}

	public void setProject(GenericProject project) {
		this.project = project;
	}

	public AttachmentList getAttachmentList() {
		return attachmentList;
	}


	public void addContent(AutomationRequest request){
		this.rootContent.add(request);
	}

	public void removeContent(AutomationRequest request){
		this.rootContent.remove(request);
	}
}

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
package org.squashtest.tm.domain.attachment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import org.squashtest.tm.domain.Identified;

@Entity
public class AttachmentList implements Identified{

	@Id
	@Column(name = "ATTACHMENT_LIST_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "attachment_list_attachment_list_id_seq")
	@SequenceGenerator(name = "attachment_list_attachment_list_id_seq", sequenceName = "attachment_list_attachment_list_id_seq", allocationSize = 1)
	private Long id;

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumn(name = "ATTACHMENT_LIST_ID", nullable = false, updatable = false)
	private final Set<Attachment> attachments = new HashSet<>();

	@Override
	public Long getId() {
		return id;
	}

	public void addAttachment(Attachment attachment) {
		attachments.add(attachment);
	}

	public void removeAttachment(Attachment attachment) {
		removeAttachment(attachment.getId());
	}

	public void removeAttachment(long attachmentId) {
		Iterator<Attachment> iter = attachments.iterator();
		while (iter.hasNext()) {
			Attachment at = iter.next();
			if (at.getId() == attachmentId) {
				iter.remove();
				break;
			}
		}
	}

	public Attachment getAttachmentById(long attachmentId) {
		Attachment result = null;

		for (Attachment at : attachments) {
			if (at.getId() == attachmentId) {
				result = at;
				break;
			}
		}

		return result;
	}

	public boolean hasAttachments() {
		return !attachments.isEmpty();
	}

	/**
	 * Identical to {@link #hasAttachments()} but can be reached through EL...
	 * @return
	 */
	public boolean isNotEmpty() {
		return hasAttachments();
	}
	public Set<Attachment> getAllAttachments() {
		return attachments;
	}

	public int size() {
		return getAllAttachments().size();
	}

}

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

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;

import org.hibernate.JDBCException;

/**
 * the BLOB part of the attachment was kept apart from the Attachment class itself to enforce the lazy loading of
 * potentially large data.
 * 
 * 
 * @author bsiri
 * 
 */

@Entity
public class AttachmentContent {

	@Id
	@Column(name = "ATTACHMENT_CONTENT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "attachment_content_attachment_content_id_seq")
	@SequenceGenerator(name = "attachment_content_attachment_content_id_seq", sequenceName = "attachment_content_attachment_content_id_seq", allocationSize = 1)
	private Long id;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	private Blob streamContent;

	public InputStream getStream() {
		try {
			return streamContent.getBinaryStream();
		} catch (SQLException e) {
			throw new JDBCException("Cannot read blob property as a stream", e);
		}
	}

	public void setContent(Blob content) {
		this.streamContent = content;
	}

	public AttachmentContent hardCopy() {
		AttachmentContent clone = new AttachmentContent();

		// note : we don't really deep copy the input stream here since Hibernate won't care : it's not an entity,
		// whereas the
		// AttachmentContent is. Do you really want to clone an input stream anyway ?
		clone.streamContent = this.streamContent;
		return clone;
	}

	public Long getId() {
		return id;
	}

}

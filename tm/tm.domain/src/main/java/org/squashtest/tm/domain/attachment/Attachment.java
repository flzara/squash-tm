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

import java.util.Date;
import java.util.Locale;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.squashtest.tm.domain.Identified;

@Entity
public class Attachment implements Identified {
	private static final float MEGA_BYTE = 1048576.000f;

	@Id
	@Column(name = "ATTACHMENT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "attachment_attachment_id_seq")
	@SequenceGenerator(name = "attachment_attachment_id_seq", sequenceName = "attachment_attachment_id_seq", allocationSize = 1)
	private Long id;

	@NotEmpty
	private String name;

	private String type;

	/** attachment size in bytes */
        /*
        * The name of the attribute slightly differs from the name of the DB column because it allows Hibernate to 
        * desambiguate 'size' as a method of a collection (such as AttachmentList.attachments.size()) from 'size' as 
        * a property (such as Attachment.size)
        */
        @Column(name = "SIZE")
	private Long contentSize = 0L;

	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DETACH })
	@JoinColumn(name = "CONTENT_ID")
	private AttachmentContent content;

	public Attachment() {
		super();
	}

	public Attachment(String name) {
		doSetName(name);
	}

	@Override
	public Long getId() {
		return id;
	}

	public AttachmentContent getContent() {
		return content;
	}

	public void setContent(AttachmentContent content) {
		this.content = content;
	}

	@Temporal(TemporalType.TIMESTAMP)
	private Date addedOn;

	/**
	 * @return the full name of the file (including extension)
	 *
	 */

	public String getName() {
		return name;
	}

	/**
	 * sets the full name (including extensions). The file type will be set on the fly.
	 *
	 * @param String
	 *            name
	 */
	public void setName(String name) {
		doSetName(name);
	}
	/**
	 * @see #setName(String)
	 * @param name
	 */
	private void doSetName(String name){
		this.name = name;
		setType();
	}
	/**
	 * When dealing with name this is the one you want most of the time
	 *
	 * @return the filename without extension
	 */
	@NotBlank
	public String getShortName() {
		int position = name.lastIndexOf('.');
		return name.substring(0, position);
	}

	/**
	 * When dealing with names this is the one you want most of the time
	 *
	 * @param shortName
	 *            represents the filename without extension
	 */
	public void setShortName(String shortName) {
		// OMFG what if type is null !
		name = shortName + '.' + type;
	}

	public final void setType(String strType) {
		this.type = strType.trim();
	}

	private void setType() {
		if (name != null) {
			int position = name.lastIndexOf('.');
			setType(name.substring(position + 1));
		}
	}

	public final String getType() {
		return type;
	}

	public final void setAddedOn(Date addedOn) {
		this.addedOn = addedOn;
	}

	public final Date getAddedOn() {
		return addedOn;
	}

	public Long getSize() {
		return contentSize;
	}

	public void setSize(Long size) {
		this.contentSize = size;
	}

	public String getFormattedSize() {
		return getFormattedSize(Locale.getDefault());
	}

	// TODO text formatting should not be the responsibility of domain object. computing size in megs is, though
	public String getFormattedSize(Locale locale) {
		Float megaSize = contentSize / MEGA_BYTE;
		return String.format(locale, "%.2f", megaSize);
	}

	/**
	 * will perform a deep copy of this Attachment. All attributes will be duplicated including the content.
	 *
	 * Note : the properties 'id' and 'addedOn' won't be duplicated and will be automatically set by the system.
	 *
	 */
	public Attachment hardCopy() {
		Attachment clone = new Attachment();

		clone.setName(this.getName());
		clone.setSize(this.getSize());
		clone.setType(this.getType());
		clone.setAddedOn(new Date());
		if (this.getContent() != null) {
			clone.setContent(this.getContent().hardCopy());
		}

		return clone;
	}

}

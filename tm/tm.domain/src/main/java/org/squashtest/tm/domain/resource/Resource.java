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
package org.squashtest.tm.domain.resource;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.search.UpperCasedStringBridge;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A Resource is the actual "things" which are organized in a library tree.
 *
 * @author Gregory Fouquet
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
public abstract class Resource implements AttachmentHolder, Identified {
	@Id
	@Column(name = "RES_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "resource_res_id_seq")
	@SequenceGenerator(name = "resource_res_id_seq", sequenceName = "resource_res_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(max = Sizes.NAME_MAX)
	@Fields({
		@Field,
		@Field(name = "label", analyze = Analyze.NO, store = Store.YES),
		@Field(
			name = "labelUpperCased",
			analyze = Analyze.NO,
			store = Store.YES,
			bridge = @FieldBridge(impl = UpperCasedStringBridge.class)
		),
	})
	private String name;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Fields({
		@Field(),
		@Field(
			name = "hasDescription",
			analyze = Analyze.NO,
			store = Store.YES,
			bridge = @FieldBridge(impl = RequirementVersionDescriptionBridge.class)
		),

	})
	private String description;

	@NotNull
	@OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch=FetchType.LAZY)
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	@Override
	public Long getId() {
		return id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see org.squashtest.tm.domain.attachment.AttachmentHolder#getAttachmentList()
	 */
	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}


	// ******************* other utilities ****************************

	/*
	 * Issue 1713
	 *
	 * Due to the mixed use of actual instances and javassist proxies, comparisons may fail. Thus the
	 * redefinition of hashCode() and equals() below, that take account of the lazy loading and
	 * the fact that the compared objects may be of different classes.
	 *
	 */

	@Override
	public int hashCode() {
		final int prime = 61;
		int result = 97;
		result = prime * result
			+ (getAttachmentList() == null ? 0 : getAttachmentList().hashCode());
		result = prime * result
			+ (getDescription() == null ? 0 : getDescription().hashCode());
		result = prime * result + (getId() == null ? 0 : getId().hashCode());
		result = prime * result + (getName() == null ? 0 : getName().hashCode());
		return result;
	}

	@Override//NOSONAR code generation, assumed to be safe
	public boolean equals(Object obj) { // GENERATED:START
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(this.getClass().isAssignableFrom(obj.getClass()) || obj.getClass().isAssignableFrom(getClass()))) {
			return false;
		}
		Resource other = (Resource) obj;
		if (getAttachmentList() == null) {
			if (other.getAttachmentList() != null) {
				return false;
			}
		} else if (!getAttachmentList().equals(other.getAttachmentList())) {
			return false;
		}
		if (getDescription() == null) {
			if (other.getDescription() != null) {
				return false;
			}
		} else if (!getDescription().equals(other.getDescription())) {
			return false;
		}
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	} // GENERATED:END


}

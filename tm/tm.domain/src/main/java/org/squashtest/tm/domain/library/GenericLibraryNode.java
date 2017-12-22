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

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.CharFilterDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.UpperCasedStringBridge;

/**
 * Generic superclass for library nodes.
 *
 * @author Gregory Fouquet
 *
 */
@MappedSuperclass
@AnalyzerDef(name = "htmlStrip", charFilters = {
		@CharFilterDef(factory = HTMLStripCharFilterFactory.class) },  filters = {
				@TokenFilterDef(factory = LowerCaseFilterFactory.class) }, tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class) )
public abstract class GenericLibraryNode implements LibraryNode, AttachmentHolder {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROJECT_ID")
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Project project;

	@NotBlank
	@Fields({
		@Field,
		@Field(name = "label", analyze = Analyze.NO, store = Store.YES),
		@Field(name = "labelUpperCased", analyze = Analyze.NO, store = Store.YES, bridge = @FieldBridge(impl = UpperCasedStringBridge.class)), })
	@Size(max = Sizes.LABEL_MAX)
	private String name;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Field(analyzer = @Analyzer(definition = "htmlStrip"))
	private String description;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REMOVE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ATTACHMENT_LIST_ID", updatable = false)
	private final AttachmentList attachmentList = new AttachmentList();

	public GenericLibraryNode() {
		super();
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void setName(String name) {
		if (name != null) {
			this.name = name.trim();
		}
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * Notifies this object it is now a resource of the given project.
	 *
	 */
	@Override
	public void notifyAssociatedWithProject(Project project) {
		this.project = project;
	}

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}


	// ******************* other utilities ****************************

	/**
	 * Used internally, this class must be implemented by first-descendant of this (GenericLibraryNode) class. We need it
	 * in order to implement a better, accurate hashcode and equals. Now that Squash has only one classloader, that shouldn't
	 * be a problem right ?
	 *
	 */
	protected abstract Class<? extends GenericLibraryNode> getGenericNodeClass();


	/*
	 * Issue 1713
	 *
	 * Due to the mixed use of actual instances and javassist proxies, comparisons may fail. Thus the redefinition of
	 * hashCode() and equals() below, that take account of the lazy loading and the fact that the compared objects may
	 * be of different classes.
	 */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		Long id = getId();
		Class<?> mygenericClass = getGenericNodeClass();
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (mygenericClass == null ? 0 : mygenericClass.hashCode());
		return result;
	}

	// GENERATED:START
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (! GenericLibraryNode.class.isAssignableFrom(obj.getClass()))
			return false;

		GenericLibraryNode other = (GenericLibraryNode) obj;
		Long id = getId();
		Class<?> mygenericClass = getGenericNodeClass();

		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;

		if (!mygenericClass.equals(other.getGenericNodeClass()))
			return false;

		return true;
	}
	// GENERATED:END


}

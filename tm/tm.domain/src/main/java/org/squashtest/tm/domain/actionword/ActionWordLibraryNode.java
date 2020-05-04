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
package org.squashtest.tm.domain.actionword;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.MetaValue;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ACTION_WORD_LIBRARY_NODE")
public class ActionWordLibraryNode implements ActionWordTreeLibraryNode {

	private static final String AWLN_ID = "AWLN_ID";

	@Id
	@Column(name = AWLN_ID)
	@GeneratedValue(strategy= GenerationType.AUTO, generator="action_word_library_node_awln_id_seq")
	@SequenceGenerator(name="action_word_library_node_awln_id_seq", sequenceName="action_word_library_node_awln_id_seq", allocationSize = 1)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "ENTITY_TYPE", insertable = false, updatable = false)
	private ActionWordTreeDefinition entityType;

	@Column(name = "ENTITY_ID", insertable = false, updatable = false)
	private Long entityId;

	@Size(max = Sizes.NAME_MAX)
	private String name;

	@JoinTable(name = "AWLN_RELATIONSHIP",
		joinColumns = { @JoinColumn(name = "DESCENDANT_ID", referencedColumnName = AWLN_ID, insertable = false, updatable = false) },
		inverseJoinColumns = { @JoinColumn(name = "ANCESTOR_ID", referencedColumnName= AWLN_ID, insertable = false, updatable = false) })
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = ActionWordLibraryNode.class)
	private ActionWordTreeLibraryNode parent;

	@JoinTable(name="AWLN_RELATIONSHIP",
		joinColumns = { @JoinColumn(name="ANCESTOR_ID", referencedColumnName= AWLN_ID) },
		inverseJoinColumns = { @JoinColumn(name="DESCENDANT_ID", referencedColumnName= AWLN_ID) })
	@OneToMany(fetch = FetchType.LAZY, targetEntity = ActionWordLibraryNode.class, cascade = { CascadeType.ALL })
	@OrderColumn(name="CONTENT_ORDER")
	private List<ActionWordTreeLibraryNode> children = new ArrayList<>();

	@Any( metaColumn = @Column( name = "ENTITY_TYPE" ), fetch = FetchType.LAZY)
	@AnyMetaDef(
		idType = "long",
		metaType = "string",
		metaValues = {
			@MetaValue( value = ActionWordNodeType.LIBRARY_NAME, targetEntity = ActionWordLibrary.class ),
			@MetaValue( value = ActionWordNodeType.ACTION_WORD_NAME, targetEntity = ActionWord.class )
		})
	@JoinColumn( name = "ENTITY_ID" )
	@Cascade(value = org.hibernate.annotations.CascadeType.ALL)
	private ActionWordTreeEntity entity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AWL_ID")
	private ActionWordLibrary library;

	public ActionWordLibraryNode() {
	}

	public ActionWordLibraryNode(
		ActionWordTreeDefinition entityType,
		Long entityId,
		@Size(max = Sizes.NAME_MAX) String name,
		ActionWordLibrary library) {

		this.entityType = entityType;
		this.entityId = entityId;
		this.name = name;
		this.library = library;
	}

	@Override
	public Long getId() {
		return id;
	}

	/* TreeLibraryNode methods */

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public long getEntityId() {
		return entityId;
	}

	@Override
	public void isCoherentWithEntity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasContent() {
		if (!getEntityType().isContainer()) {
			return false;
		}
		return !children.isEmpty();
	}

	@Override
	public void renameNode(String newName) {
		throw new UnsupportedOperationException();
	}

	/* ActionWordTreeLibraryNode methods */

	@Override
	public ActionWordTreeDefinition getEntityType() {
		return entityType;
	}
	@Override
	public void setEntityType(ActionWordTreeDefinition entityType) {
		this.entityType = entityType;
	}

	@Override
	public ActionWordTreeEntity getEntity() {
		return entity;
	}
	@Override
	public void setEntity(ActionWordTreeEntity treeEntity) {
		this.entity = treeEntity;
	}

	@Override
	@AclConstrainedObject
	public ActionWordLibrary getLibrary() {
		return library;
	}

	@Override
	public void setLibrary(ActionWordLibrary library) {
		this.library = library;
	}

	@Override
	public ActionWordTreeLibraryNode getParent() {
		return parent;
	}
	@Override
	public void setParent(ActionWordTreeLibraryNode parent) {
		this.parent = parent;
	}

	@Override
	public List<ActionWordTreeLibraryNode> getChildren() {
		return children;
	}

	@Override
	public void addChild(ActionWordTreeLibraryNode treeLibraryNode)
		throws UnsupportedOperationException, IllegalArgumentException, NameAlreadyInUseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeChild(ActionWordTreeLibraryNode treeLibraryNode) {
		throw new UnsupportedOperationException();
	}
}

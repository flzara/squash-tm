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
package org.squashtest.tm.domain.campaign;

import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.library.GenericLibraryNode;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
public abstract class CampaignLibraryNode extends GenericLibraryNode {
	@Id
	@Column(name = "CLN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "campaign_library_node_cln_id_seq")
	@SequenceGenerator(name = "campaign_library_node_cln_id_seq", sequenceName = "campaign_library_node_cln_id_seq", allocationSize = 1)
	private Long id;

	public CampaignLibraryNode() {
		super();
	}

	@Override
	public Long getId() {
		return id;
	}

	public abstract void accept(CampaignLibraryNodeVisitor visitor);

	@Override
	@AclConstrainedObject
	public Library<?> getLibrary() {
		return getProject().getCampaignLibrary();
	}
	
	@Override
	protected Class<? extends GenericLibraryNode> getGenericNodeClass() {
		return CampaignLibraryNode.class;
	}
	
}

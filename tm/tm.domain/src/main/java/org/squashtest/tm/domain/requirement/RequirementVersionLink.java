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

import org.squashtest.tm.domain.Identified;

import javax.persistence.*;

/**
 * Created by jlor on 09/05/2017.
 */

@Entity
@Table(name = "REQUIREMENT_VERSION_LINK")
public class RequirementVersionLink implements Identified {

	@Id
	@Column(name = "LINK_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_version_link_link_id_seq")
	@SequenceGenerator(name = "requirement_version_link_link_id_seq", sequenceName = "requirement_version_link_link_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REQUIREMENT_VERSION_ID", referencedColumnName = "RES_ID")
	private RequirementVersion requirementVersion;

	@ManyToOne
	@JoinColumn(name = "RELATED_REQUIREMENT_VERSION_ID", referencedColumnName = "RES_ID")
	private RequirementVersion relatedRequirementVersion;

	@ManyToOne
	@JoinColumn(name="LINK_TYPE_ID", referencedColumnName = "TYPE_ID")
	private RequirementVersionLinkType linkType;

	/**
	 * If linkDirection is false, it means that the role of requirementVersion is linkType.role1 and the role of
	 * relatedRequirementVersion is linkType.role2. If linkDirection is true, it is the other way around.
	 * */
	@Column(name="LINK_DIRECTION")
	private boolean linkDirection;

	@Override
	public Long getId() {
		return id;
	}

	public RequirementVersionLink() {
		super();
	}

	public RequirementVersionLink(RequirementVersion requirementVersion, RequirementVersion relatedRequirementVersion) {
		this.requirementVersion = requirementVersion;
		this.relatedRequirementVersion = relatedRequirementVersion;
	}

	public RequirementVersionLink(
		RequirementVersion requirementVersion,
		RequirementVersion relatedRequirementVersion,
		RequirementVersionLinkType linkType,
		boolean linkDirection) {

		this.requirementVersion = requirementVersion;
		this.relatedRequirementVersion = relatedRequirementVersion;
		this.linkType = linkType;
		this.linkDirection = linkDirection;
	}

	private String getRequirementVersionRole() {
		if(!linkDirection) {
			return this.linkType.getRole1();
		} else {
			return this.linkType.getRole2();
		}
	}

	private String getRelatedRequirementVersionRole() {
		if(linkDirection) {
			return this.linkType.getRole1();
		} else {
			return this.linkType.getRole2();
		}
	}

	public LinkedRequirementVersion getLinkedRequirementVersion() {
		return new LinkedRequirementVersion(this.requirementVersion, getRequirementVersionRole());
	}

	public LinkedRequirementVersion getRelatedLinkedRequirementVersion() {
		return new LinkedRequirementVersion(this.relatedRequirementVersion, getRelatedRequirementVersionRole());
	}
	public RequirementVersionLinkType getLinkType() {
		return linkType;
	}

	public void setLinkType(RequirementVersionLinkType linkType) {
		this.linkType = linkType;
	}

	public boolean getLinkDirection() {
		return this.linkDirection;
	}

	public void setLinkDirection(boolean linkDirection) {
		this.linkDirection = linkDirection;
	}

	public RequirementVersionLink createSymmetricalRequirementVersionLink() {
		return new RequirementVersionLink(
				this.relatedRequirementVersion,
				this.requirementVersion,
				this.linkType,
				!this.linkDirection);
	}

	public RequirementVersionLink copyForRequirementVersion(RequirementVersion copyVersion) {
		return new RequirementVersionLink(copyVersion, this.relatedRequirementVersion, this.linkType, this.linkDirection);
	}
}

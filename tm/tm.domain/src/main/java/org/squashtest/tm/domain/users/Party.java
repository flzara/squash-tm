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
package org.squashtest.tm.domain.users;

import org.squashtest.tm.domain.Identified;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "CORE_PARTY")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Party implements Identified{

	private static final String TYPE = "PARTY";

	@Id
	@Column(name = "PARTY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "core_party_party_id_seq")
	@SequenceGenerator(name = "core_party_party_id_seq", sequenceName = "core_party_party_id_seq", allocationSize = 1)
	protected Long id;

	/**
	 * This is only used when administering the user, we can fetch lazy
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "CORE_GROUP_MEMBER", joinColumns = @JoinColumn(name = "PARTY_ID"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID"))
	private UsersGroup group;

	@OneToMany(mappedBy = "party", cascade = CascadeType.REMOVE)
	private List<PartyPreference> preferences;

	public UsersGroup getGroup() {
		return group;
	}

	public void setGroup(UsersGroup group) {
		this.group = group;
	}
	@Override
	public Long getId() {
		return id;
	}

	public String getName(){
		return "";
	}

	public String getType(){
		return TYPE;
	}

	abstract void accept(PartyVisitor visitor);


}

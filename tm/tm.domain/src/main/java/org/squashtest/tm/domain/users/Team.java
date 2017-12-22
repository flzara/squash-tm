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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.audit.Auditable;

@Entity
@Auditable
@Table(name = "CORE_TEAM")
@PrimaryKeyJoinColumn(name = "PARTY_ID")
@NamedQueries(value = {
@NamedQuery(name="Team.countAssociatedTeams", query="select count(*) from Team t join t.members m where m.id = ?1"),
@NamedQuery(name="Team.findAllNonAssociatedTeams", query="select t from Team t where t.id not in (select ti.id from Team ti join ti.members m where m.id = ?1)"),

})
public class Team extends Party{

	private static final String TYPE = "TEAM";

	@NotBlank
	@Size(min = 0, max = 50)
	private String name;

	private String description;

	@ManyToMany
	@JoinTable(name = "CORE_TEAM_MEMBER", joinColumns = @JoinColumn(name = "TEAM_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
	private final Set<User> members = new HashSet<>();

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<User> getMembers() {
		return members;
	}

	public void addMember(User user){
		members.add(user);
	}

	public void addMembers(Collection<User> users){
		members.addAll(users);
	}

	public void removeMember(User user){
		members.remove(user);
	}

	public void removeMember(Collection<User> users){
		members.removeAll(users);
	}

	@Override
	public String getType(){
		return TYPE;
	}

	@Override
	void accept(PartyVisitor visitor) {
		visitor.visit(this);
	}

}

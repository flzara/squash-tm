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

import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.audit.Auditable;

@Entity
@Auditable
@Table(name = "CORE_USER")
@PrimaryKeyJoinColumn(name = "PARTY_ID")
public class User extends Party {

	private static final String TYPE = "USER";

	@Transient
	public static final Long NO_USER_ID = 0L;

	@NotNull
	@Size(min = 0, max = 50)
	private String firstName = "";

	@NotBlank
	@Size(min = 0, max = 50)
	private String lastName;

	@NotBlank
	@Size(min = 0, max = 50)
	private String login;

	@NotNull
	@Size(min = 0, max = 50)
	private String email = "";

	// TODO is it nullable ? Aint "boolean" ok ?
	// 2017/11/14 this is actually legit, see https://thedailywtf.com/articles/What_Is_Truth_0x3f_
	private Boolean active = true;

	// Feature 6763 - Add a new column 'last connected on'
	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastConnectedOn;

	@NotNull
	@ManyToMany(mappedBy = "members")
	private final Set<Team> teams = new HashSet<>();

	public User() {
		super();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login.trim();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setLastConnectedOn(Date lastConnectedOn) {
		this.lastConnectedOn = lastConnectedOn;
	}

	public Date getLastConnectedOn() {
		return lastConnectedOn;
	}

	public Set<Team> getTeams() {
		return teams;
	}

	public void addTeam(Team team) {
		this.teams.add(team);

	}

	public void removeTeams(Collection<Long> teamIds) {
		Iterator<Team> iterator = teams.iterator();

		while (iterator.hasNext()) {
			Team team = iterator.next();
			if (teamIds.contains(team.getId())) {
				team.removeMember(this);
				iterator.remove();
			}
		}

	}

	@Override
	public String getName() {
		return appendFullName(new StringBuilder()).append(" (").append(this.login).append(")").toString();
	}

	/**
	 * appends the user's full name ("John Doe") to the given builder and returns it for method chaining purposes
	 *
	 * @param builder
	 * @return the builder
	 */
	private StringBuilder appendFullName(StringBuilder builder) {
		if (StringUtils.isNotBlank(firstName)) {
			builder.append(firstName).append(' ');
		}

		builder.append(lastName);

		return builder;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	void accept(PartyVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * Factory method which creates a user from a login. last name is populated with login.
	 *
	 * @param login
	 */
	public static User createFromLogin(@NotNull String login) {
		User user = new User();
		user.login = login;
		user.normalize();

		return user;
	}

	/**
	 * Tells if this user matches the given login.
	 *
	 * @param candidate
	 * @return
	 */
	public boolean loginIs(String candidate) {
		return StringUtils.equals(login, candidate);
	}

	/**
	 * Modifies this user with sensible defaults so that it is valid, provided it has a login.
	 */
	public void normalize() {
		if (StringUtils.isBlank(lastName)) {
			lastName = login;
		}
		firstName = firstName != null ? firstName : "";
		email = email != null ? email : "";
		active = active != null ? active : true;

	}
}

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
package org.squashtest.tm.service.user;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;

/**
 * Decorator which adds authentication data to a user. Should be used to push more information to controllers.
 *
 * @author Gregory Fouquet
 */
@Auditable
public class AuthenticatedUser extends User implements AuditableMixin {
	private final User decorated;
	private final boolean hasAuthentication;

	public AuthenticatedUser(User decorated, boolean hasAuthentication) {
		super();
		this.decorated = decorated;
		this.hasAuthentication = hasAuthentication;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return decorated.hashCode();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.Party#getGroup()
	 */
	@Override
	public UsersGroup getGroup() {
		return decorated.getGroup();
	}

	/**
	 * @param group
	 * @see org.squashtest.tm.domain.users.Party#setGroup(org.squashtest.tm.domain.users.UsersGroup)
	 */
	@Override
	public void setGroup(UsersGroup group) {
		decorated.setGroup(group);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.Party#getId()
	 */
	@Override
	public Long getId() {
		return decorated.getId();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getFirstName()
	 */
	@Override
	public String getFirstName() {
		return decorated.getFirstName();
	}

	/**
	 * @param firstName
	 * @see org.squashtest.tm.domain.users.User#setFirstName(java.lang.String)
	 */
	@Override
	public void setFirstName(String firstName) {
		decorated.setFirstName(firstName);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getLastName()
	 */
	@Override
	public String getLastName() {
		return decorated.getLastName();
	}

	/**
	 * @param lastName
	 * @see org.squashtest.tm.domain.users.User#setLastName(java.lang.String)
	 */
	@Override
	public void setLastName(String lastName) {
		decorated.setLastName(lastName);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getLogin()
	 */
	@Override
	public String getLogin() {
		return decorated.getLogin();
	}

	/**
	 * @param login
	 * @see org.squashtest.tm.domain.users.User#setLogin(java.lang.String)
	 */
	@Override
	public void setLogin(String login) {
		decorated.setLogin(login);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getEmail()
	 */
	@Override
	public String getEmail() {
		return decorated.getEmail();
	}

	/**
	 * @param email
	 * @see org.squashtest.tm.domain.users.User#setEmail(java.lang.String)
	 */
	@Override
	public void setEmail(String email) {
		decorated.setEmail(email);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getActive()
	 */
	@Override
	public Boolean getActive() {
		return decorated.getActive();
	}

	/**
	 * @param active
	 * @see org.squashtest.tm.domain.users.User#setActive(java.lang.Boolean)
	 */
	@Override
	public void setActive(Boolean active) {
		decorated.setActive(active);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getLastConnectedOn()
	 */
	@Override
	public Date getLastConnectedOn() {
		return decorated.getLastConnectedOn();
	}

	/**
	 * @param lastConnectedOn
	 * @see org.squashtest.tm.domain.users.User#setLastConnectedOn(java.util.Date)
	 */
	@Override
	public void setLastConnectedOn(Date lastConnectedOn) {
		decorated.setLastConnectedOn(lastConnectedOn);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getTeams()
	 */
	@Override
	public Set<Team> getTeams() {
		return decorated.getTeams();
	}

	/**
	 * @param team
	 * @see org.squashtest.tm.domain.users.User#addTeam(org.squashtest.tm.domain.users.Team)
	 */
	@Override
	public void addTeam(Team team) {
		decorated.addTeam(team);
	}

	/**
	 * @param teamIds
	 * @see org.squashtest.tm.domain.users.User#removeTeams(java.util.Collection)
	 */
	@Override
	public void removeTeams(Collection<Long> teamIds) {
		decorated.removeTeams(teamIds);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getName()
	 */
	@Override
	public String getName() {
		return decorated.getName();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return decorated.equals(obj);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.users.User#getType()
	 */
	@Override
	public String getType() {
		return decorated.getType();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return decorated.toString();
	}

	/**
	 * ugly method name for javabean access.
	 *
	 * @return the hasAuthentication
	 * @see #hasAuthentication()
	 */
	public boolean isHasAuthentication() {
		return hasAuthentication();
	}

	/**
	 * @return true if this user is referenced by internal authentication system.
	 */
	public boolean hasAuthentication() {
		return hasAuthentication;
	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#getCreatedOn()
	 */
	@Override
	public Date getCreatedOn() {
		return getAuditable().getCreatedOn();
	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#getCreatedBy()
	 */
	@Override
	public String getCreatedBy() {
		return getAuditable().getCreatedBy();
	}

	/**
	 * @return
	 */
	private AuditableMixin getAuditable() {
		return (AuditableMixin) decorated;
	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#getLastModifiedOn()
	 */
	@Override
	public Date getLastModifiedOn() {
		return getAuditable().getLastModifiedOn();
	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#getLastModifiedBy()
	 */
	@Override
	public String getLastModifiedBy() {
		return getAuditable().getLastModifiedBy();
	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#isSkipModifyAudit()
	 */
	@Override
	public boolean isSkipModifyAudit() {
		return getAuditable().isSkipModifyAudit();
	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setCreatedBy(java.lang.String)
	 */
	@Override
	public void setCreatedBy(String createdBy) {
		getAuditable().setCreatedBy(createdBy);

	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setCreatedOn(java.util.Date)
	 */
	@Override
	public void setCreatedOn(Date createdOn) {
		getAuditable().setCreatedOn(createdOn);

	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setLastModifiedBy(java.lang.String)
	 */
	@Override
	public void setLastModifiedBy(String lastModifiedBy) {
		getAuditable().setLastModifiedBy(lastModifiedBy);

	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setLastModifiedOn(java.util.Date)
	 */
	@Override
	public void setLastModifiedOn(Date lastModifiedOn) {
		getAuditable().setLastModifiedOn(lastModifiedOn);

	}

	/**
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setSkipModifyAudit(boolean)
	 */
	@Override
	public void setSkipModifyAudit(boolean skipModifyAudit) {
		getAuditable().setSkipModifyAudit(skipModifyAudit);
	}
}

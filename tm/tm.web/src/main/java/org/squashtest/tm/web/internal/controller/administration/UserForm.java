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
package org.squashtest.tm.web.internal.controller.administration;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.squashtest.tm.domain.users.User;

/**
 * Form bean for user definition.
 * 
 * @author Gregory Fouquet
 * 
 */
public class UserForm {
	@Valid
	private User user = new User();

	private long groupId;

	@Size(min = 6, max = 256)
	private String password;

	public UserForm() {
		super();
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the groupId
	 */
	public long getGroupId() {
		return groupId;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 * @see org.squashtest.tm.tm.domain.users.User#getFirstName()
	 */
	public String getFirstName() {
		return getUser().getFirstName();
	}

	/**
	 * @param firstName
	 * @see org.squashtest.tm.tm.domain.users.User#setFirstName(java.lang.String)
	 */
	public void setFirstName(String firstName) {
		getUser().setFirstName(firstName);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.tm.domain.users.User#getLastName()
	 */
	public String getLastName() {
		return getUser().getLastName();
	}

	/**
	 * @param lastName
	 * @see org.squashtest.tm.tm.domain.users.User#setLastName(java.lang.String)
	 */
	public void setLastName(String lastName) {
		getUser().setLastName(lastName);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.tm.domain.users.User#getLogin()
	 */
	public String getLogin() {
		return getUser().getLogin();
	}

	/**
	 * @param login
	 * @see org.squashtest.tm.tm.domain.users.User#setLogin(java.lang.String)
	 */
	public void setLogin(String login) {
		getUser().setLogin(login);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.tm.domain.users.User#getEmail()
	 */
	public String getEmail() {
		return getUser().getEmail();
	}

	/**
	 * @param email
	 * @see org.squashtest.tm.tm.domain.users.User#setEmail(java.lang.String)
	 */
	public void setEmail(String email) {
		getUser().setEmail(email);
	}

	/**
	 * @return
	 * @see org.squashtest.tm.tm.domain.users.User#getActive()
	 */
	public Boolean getActive() {
		return getUser().getActive();
	}

	/**
	 * @param active
	 * @see org.squashtest.tm.tm.domain.users.User#setActive(java.lang.Boolean)
	 */
	public void setActive(Boolean active) {
		getUser().setActive(active);
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

}

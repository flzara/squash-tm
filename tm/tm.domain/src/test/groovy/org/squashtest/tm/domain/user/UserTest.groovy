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
package org.squashtest.tm.domain.user;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.squashtest.tm.domain.users.User;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class UserTest extends Specification {

	@Test
	public void "normalized user should be valid"() {
		given:
		User u = new User(login: "batman", email: null, firstName: null, lastName: null, active: null)

		when:
		u.normalize()

		then:
		u.firstName != null
		StringUtils.isNotBlank(u.lastName)
		u.email != null
		u.active != null

	}

	@Test
	public void "valid user should not be normalized"() {
		given:
		User u = new User(login: "batman", email: "batman@batcave.com", firstName: "bruce", lastName: "wayne", active: null)

		when:
		u.normalize()

		then:
		u.firstName == "bruce"
		u.lastName == "wayne"
		u.email == "batman@batcave.com"
		u.active != null

	}
}

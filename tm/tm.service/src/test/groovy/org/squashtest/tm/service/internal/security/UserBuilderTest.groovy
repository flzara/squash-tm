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
package org.squashtest.tm.service.internal.security

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class UserBuilderTest extends Specification {

	def "should create an enabled user"() {
		when:
		User res = UserBuilder.forUser("jericho").password("y2j").active(true).build()

		then:
		res.username == "jericho"
		res.password == "y2j"
		res.enabled
		res.accountNonExpired
		res.accountNonLocked
		res.credentialsNonExpired
		res.authorities.empty
	}

	def "should create a disabled user"() {
		when:
		User res = UserBuilder.forUser("jericho").password("y2j").build()

		then:
		res.username == "jericho"
		res.password == "y2j"
		!res.enabled
		!res.accountNonExpired
		!res.accountNonLocked
		!res.credentialsNonExpired
		res.authorities.empty
	}

	@Unroll
	def "should create from user #model"() {
		when:
		UserDetails res = UserBuilder.duplicate(model).password("same again").build();

		then:
		res.username == model.username
		res.password == "same again"
		res.enabled == model.enabled
		res.accountNonExpired == model.accountNonExpired
		res.accountNonLocked == model.accountNonLocked
		res.credentialsNonExpired == model.credentialsNonExpired
		res.authorities == model.authorities

		where:
		model << [
			UserBuilder.forUser("jericho").password("y2j").active(true).build(),
			UserBuilder.forUser("jericho").password("y2j").active(false).build(),
			UserBuilder.forUser("jericho").password("y2j").build()
		]
	}
}

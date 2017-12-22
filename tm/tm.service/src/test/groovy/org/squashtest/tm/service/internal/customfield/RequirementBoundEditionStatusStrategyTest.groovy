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
package org.squashtest.tm.service.internal.customfield

import javax.persistence.EntityManager;

import org.hibernate.Session
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.service.security.PermissionEvaluationService;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class RequirementBoundEditionStatusStrategyTest extends Specification {
	RequirementBoundEditionStatusStrategy strategy = new RequirementBoundEditionStatusStrategy()
	PermissionEvaluationService permService = Mock()
	EntityManager em = Mock()
	Session session = Mock()

	def setup() {
		em.unwrap(_) >> session
		strategy.em = em
		use(ReflectionCategory) {
			ValueEditionStatusHelper.set field: "permissionEvaluator", of: strategy, to: permService
		}
	}

	@Unroll
	def "CF values should be editable : #expected when requirement is modifiable : #modif and user has perm : #perm"() {
		given:
		permService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", 0, RequirementVersion.name) >> perm

		and:
		RequirementVersion ver = Mock()
		em.getReference(RequirementVersion, 0) >> ver
		ver.isModifiable() >> modif

		expect:
		strategy.isEditable(0, BindableEntity.REQUIREMENT_VERSION) == expected

		where:
		perm  | modif | expected
		true  | true  | true
		true  | false | false
		false | true  | false
		false | false | false
	}
}

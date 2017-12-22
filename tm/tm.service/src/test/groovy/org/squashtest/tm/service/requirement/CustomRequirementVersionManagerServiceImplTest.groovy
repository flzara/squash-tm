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
package org.squashtest.tm.service.requirement;


import javax.persistence.EntityManager;

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.service.advancedsearch.IndexationService
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.repository.RequirementVersionDao
import org.squashtest.tm.service.internal.requirement.CustomRequirementVersionManagerServiceImpl
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService

import spock.lang.Specification

class CustomRequirementVersionManagerServiceImplTest extends Specification {
	CustomRequirementVersionManagerServiceImpl service = new CustomRequirementVersionManagerServiceImpl()
	RequirementVersionDao requirementVersionDao = Mock()
	EntityManager em = Mock()
	Session currentSession = Mock()
	PrivateCustomFieldValueService customFieldService = Mock()
	IndexationService indexationService = Mock()
	LinkedRequirementVersionManagerService requirementLinkService = Mock()

	def setup() {
		service.requirementVersionDao = requirementVersionDao
		service.em = em
		service.indexationService = indexationService;

		em.unwrap(_) >> currentSession
		service.customFieldValueService = customFieldService
		service.requirementLinkService = requirementLinkService

	}

	def "should increase the version of the requirement and persist it"() {
		given:
		Requirement req = Mock()
		requirementVersionDao.findRequirementById(10L) >> req

		and:
		RequirementVersion newVersion = Mock()
		req.currentVersion >> newVersion

		when:
		service.createNewVersion(10L, false,false)

		then:
		1 * em.persist(newVersion)

	}

}

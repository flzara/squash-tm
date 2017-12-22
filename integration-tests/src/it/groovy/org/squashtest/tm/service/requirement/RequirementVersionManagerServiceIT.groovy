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
package org.squashtest.tm.service.requirement

import javax.inject.Inject

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.infolist.ListItemReference
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Stepwise;
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@Stepwise
class RequirementVersionManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	RequirementVersionManagerService modService;

	long requirementId = -10
	@DirtiesContext
	def "useless test to clean context "(){
		expect : true
	}

	@DirtiesContext
	@DataSet("RequirementVersionManagerServiceIT.should successfully rename a requirement.xml")
	def "should successfully rename a requirement"(){

		when :
		modService.rename(requirementId, "new req");
		def rereq = modService.findById(requirementId);

		then :
		rereq != null
		rereq.id != null
		rereq.name == "new req"

	}
	@DirtiesContext
	@DataSet("RequirementVersionManagerServiceIT.should change requirement criticality.xml")
	def "should change requirement criticality"(){
		when:
		modService.changeCriticality (requirementId, RequirementCriticality.CRITICAL)
		def requirement = modService.findById(requirementId)

		then:
		requirement.criticality == RequirementCriticality.CRITICAL
	}
	@DirtiesContext
	@DataSet("RequirementVersionManagerServiceIT.should change requirement category.xml")
	def "should change requirement category"(){
		when:
		modService.changeCategory(requirementId, "CAT_BUSINESS")
		def requirement = modService.findById(requirementId)

		then:
		new ListItemReference("CAT_BUSINESS").references(requirement.category)
	}
	@DirtiesContext
	@DataSet("RequirementVersionManagerServiceIT.should change requirement reference.xml")
	def "should change requirement reference"(){
		given:
		def reference = "something"

		when:
		modService.changeReference(requirementId, reference)
		def requirement = modService.findById(requirementId)

		then:
		requirement.reference == reference
	}


	/*
	 * [Feat 3611] The test is broken for now because the business rule must be redesigned and reimplemented,
	 * see comment in CustomRequirementVersionManagerService#rename(long, String)
	 * [Feat 3611] (gfouquet 15-03-06) Apparently, from now on, we should allow duplicate names so I negated the test.
	 */
	@DirtiesContext
	@DataSet("RequirementVersionManagerServiceIT.should fail to rename a requirement because duplicated name.xml")
	def "should NOT fail to rename a requirement because duplicated name"(){
		when :
		modService.rename(requirementId, "req 2")
		then :
		notThrown(DuplicateNameException)
	}
}

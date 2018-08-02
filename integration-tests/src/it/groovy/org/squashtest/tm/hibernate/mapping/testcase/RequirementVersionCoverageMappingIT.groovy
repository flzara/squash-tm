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
package org.squashtest.tm.hibernate.mapping.testcase

import org.squashtest.it.basespecs.DbunitMappingSpecification
import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage
import org.squashtest.tm.domain.testcase.TestCase

class RequirementVersionCoverageMappingIT extends DbunitMappingSpecification {



	def "should add a Requirement Version verified by a TestCase"() {

		given :
		def someItem = doInTransaction{
			it.get(InfoListItem, 1l)
		}

		and:

		TestCase tc = new TestCase(name: "link", nature:someItem, type : someItem)
		Requirement r = new Requirement(new RequirementVersion(name: "link", category:someItem))
		RequirementVersionCoverage rvc = new RequirementVersionCoverage(r.currentVersion,tc)

		and :
		persistFixture r, tc, rvc

		when :
		TestCase res = doInTransaction ({
			it.createQuery("from TestCase tc left join fetch tc.requirementVersionCoverages where tc.id = " + tc.id).uniqueResult()
		})

		then:
		res.verifiedRequirementVersions.size() == 1

		cleanup:
		deleteFixture rvc
		deleteFixture r, tc
	}

}

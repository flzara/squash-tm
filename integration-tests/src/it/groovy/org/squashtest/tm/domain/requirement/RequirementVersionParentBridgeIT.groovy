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
package org.squashtest.tm.domain.requirement

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.BridgeSpecification;
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Specification
import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;

/**
 * @author Gregory Fouquet
 *
 */
@Transactional
@UnitilsSupport
class RequirementVersionParentBridgeIT extends BridgeSpecification {
	RequirementVersionHasParentBridge bridge = new RequirementVersionHasParentBridge()
	
	
	def setup(){
		def rv402 = em.find(RequirementVersion, -402l)
		def r400 = em.find(Requirement, -400l)
		rv402.setRequirement(r400)
		em.flush()
	}
	
	@DataSet("RequirementVersionBridgeIT.dataset.xml")
	@Unroll
	def "requirement version #reqId should have parent : #parent"() {
		given:
		RequirementVersion req = session.load(RequirementVersion, reqId)
		
		when:
		bridge.writeFieldToDocument("foo", session, req, doc, lucene)
		
		then:
		doc.fields.size() == 1
		doc.fields[0].name == "foo"
		doc.fields[0].fieldsData == parent
		
		where:
		reqId  | parent
		-10L    | "0"
		-402L   | "1"
		-60L    | "0"
	}
}

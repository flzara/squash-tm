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

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.bridge.LuceneOptions;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.BridgeSpecification;
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
@Transactional
class RequirementVersionAttachmentBridgeIT extends BridgeSpecification {
	RequirementVersionAttachmentBridge bridge = new RequirementVersionAttachmentBridge()
	
	@DataSet("RequirementVersionBridgeIT.dataset.xml")
	def "should count requirement's attachments"() {
		given:
		RequirementVersion req = session.load(RequirementVersion, -10L)
		
		when:
		bridge.writeFieldToDocument("foo", session, req, doc, lucene)
		
		then:
		doc.fields.size() == 1
		doc.fields[0].name == "foo"
		doc.fields[0].fieldsData == "0000002"
	}

}


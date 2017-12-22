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
package org.squashtest.tm.service.internal.repository.hibernate

import org.spockframework.util.NotThreadSafe
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.event.RequirementAuditEvent
import org.squashtest.tm.domain.event.RequirementCreation
import org.squashtest.tm.domain.event.RequirementLargePropertyChange
import org.squashtest.tm.domain.event.RequirementPropertyChange
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.service.internal.repository.RequirementAuditEventDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@NotThreadSafe
@UnitilsSupport
@Transactional
class HibernateRequirementAuditEventDaoIT extends DbunitDaoSpecification {

	@Inject
	RequirementAuditEventDao eventDao;


	def setupSpec() {
		List.metaClass.init = { howmany, item ->
			return (1..howmany).collect {
				return item
			}
		}
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementCreation event"() {
		given:
		RequirementVersion requirement = em.getReference(RequirementVersion.class, -1L);

		when:
		def createEvent = new RequirementCreation(requirement, requirement.createdBy);
		eventDao.save(createEvent);

		em.flush()
		em.clear()

		def revent = em.createQuery("from RequirementCreation rc where rc.requirementVersion.id=:req")
			.setParameter("req", requirement.id)
			.singleResult

		then:
		revent.id != null
		revent.requirementVersion.id == requirement.id
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementPropertyChange event"() {
		given:
		RequirementVersion requirement = em.getReference(RequirementVersion.class, -1L);

		when:
		RequirementPropertyChange pptChangeEvent = RequirementPropertyChange.builder()
			.setSource(requirement)
			.setAuthor(requirement.createdBy)
			.setModifiedProperty("property")
			.setOldValue("oldValue")
			.setNewValue("newValue")
			.build()
		eventDao.save(pptChangeEvent);

		em.flush()
		em.clear()

		def revent = em.createQuery("from RequirementPropertyChange rpc where rpc.requirementVersion=:req")
			.setParameter("req", requirement)
			.singleResult

		then:
		revent.id != null
		revent.requirementVersion.id == requirement.id
		revent.propertyName == "property"
		revent.oldValue == "oldValue"
		revent.newValue == "newValue"
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should persist various events.xml")
	def "should persist a RequirementLargeProperty event"() {
		given:
		RequirementVersion requirement = em.getReference(RequirementVersion.class, -1L);

		when:
		def pptChangeEvent = RequirementLargePropertyChange.builder()
			.setSource(requirement)
			.setAuthor(requirement.createdBy)
			.setModifiedProperty("property")
			.setOldValue("oldValue")
			.setNewValue("newValue")
			.build()
		eventDao.save(pptChangeEvent);

		em.flush()
		em.clear()

		def revent = em.createQuery("from RequirementLargePropertyChange rpc where rpc.requirementVersion=:req")
			.setParameter("req", requirement)
			.singleResult

		then:
		revent.id != null
		revent.requirementVersion.id == requirement.id
		revent.propertyName == "property"
		revent.oldValue == "oldValue"
		revent.newValue == "newValue"
	}


	@DataSet("HibernateRequirementAuditEventDaoIT.should fetch lists of events.xml")
	def "should fetch list of event for a requirement sorted by date"() {
		given:
		def requirementId = -1L

		and:
		PageRequest paging = new PageRequest(0, 10)

		when:
		Page<RequirementAuditEvent> events = eventDao.findAllByRequirementVersionIdOrderByDateDesc(requirementId, paging);

		then:
		events.content.size() == 4


		events*.date == [
			parse("2010-08-04"),
			parse("2010-06-03"),
			parse("2010-04-02"),
			parse("2010-02-01")


		]

		events*.author == [
			"editor 13",
			"editor 12",
			"editor 11",
			"creator 1"
		]

		events*.class == [
			RequirementLargePropertyChange.class,
			RequirementPropertyChange.class,
			RequirementPropertyChange.class,
			RequirementCreation.class
		]
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should fetch lists of events.xml")
	def "should fetch paged list of event for a requirement"() {
		given:
		def requirementId = -1L

		and:
		PageRequest paging = new PageRequest(1, 2)

		when:
		Page<RequirementAuditEvent> events = eventDao.findAllByRequirementVersionIdOrderByDateDesc(requirementId, paging);

		then:
		events.content*.id as Set == [-11L, -14L] as Set
	}

	@DataSet("HibernateRequirementAuditEventDaoIT.should fetch lists of events.xml")
	def "should count events for a requirement"() {
		when:
		def res = eventDao.countByRequirementVersionId(-1L)


		then:
		res == 4L
	}

	//the method parse looks deprecated for java.util.Date, but the actual Date class is provided by the Groovy JDK
	private static Date parse(String arg) {
		return Date.parse("yyyy-MM-dd", arg);
	}


}

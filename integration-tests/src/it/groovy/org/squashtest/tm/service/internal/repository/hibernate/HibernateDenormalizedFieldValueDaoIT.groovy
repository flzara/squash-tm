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
package org.squashtest.tm.service.internal.repository.hibernate;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.service.internal.repository.DenormalizedFieldValueDao;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
public class HibernateDenormalizedFieldValueDaoIT  extends DbunitDaoSpecification {
	@Inject
	DenormalizedFieldValueDao dfvDao
	
	@DataSet("HibernateDenormalizedFieldValueDaoIT.dfvs.xml")
	def "should find all dfv for entity"(){
		given: 
		def entityId = -500L
		def entityType = DenormalizedFieldHolderType.EXECUTION
		
		when : 
		def result = dfvDao.findDFVForEntity(entityId, entityType)
		
		then : 
		result.size() == 2
		result.get(0).value == "truc0"
		result.get(1).value == "truc1"
	}
	
	@DataSet("HibernateDenormalizedFieldValueDaoIT.dfvs.xml")
	def "should count all dfv for entity"(){
		given: 
		def entityId = -500L
		def entityType = DenormalizedFieldHolderType.EXECUTION
		
		when : 
		def result = dfvDao.countDenormalizedFields(entityId, entityType)
		
		then : 
		result == 2
	}

		@DataSet("HibernateDenormalizedFieldValueDaoIT.dfvs.xml")
	def "should find all dfv for entity and renderingLocation"(){
		given:
		def entityId = -6001L
		def entityType = DenormalizedFieldHolderType.EXECUTION_STEP
		
		when :
		def result = dfvDao.findDFVForEntityAndRenderingLocation(entityId, entityType, RenderingLocation.STEP_TABLE)
		
		then :
		result.size() == 1
		result.get(0).value == "truc2" 
	}
}

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
package org.squashtest.tm.service.internal.batchexport

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.unitils.dbunit.annotation.DataSet
import org.squashtest.tm.domain.requirement.Requirement

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class RequirementExcelExporterIT extends DbunitServiceSpecification{
	
	@Inject
	@Named("requirementExcelExporter")
	RequirementExcelExporter exporter
	@Inject
	ExportDao exportDao
	@Inject
	RequirementDao requirementDao


	def setup(){
		
		def findVersion = {id -> findEntity(RequirementVersion.class, id)}
		def findReq = {id -> findEntity(Requirement.class, id)}
		def attachVersionToReq = {id ->  Requirement req = findReq(id); req.addVersion(findVersion(id))}	
		
		def ids = (1..4).collect{it * 10 + 1}.collect{it * -1}
		ids.each(attachVersionToReq)

	}
	
	@DataSet("RequirementExcelExportIT.should create models.xml")
	def "should create models"(){
		
		given :
		def paths = ["/ok/folder1/requirement11",
			"/ok/folder1/requirement21",
			"/ok/folder1/folder3/requirement41", 
			"/ok/folder1/requirement11/requirement31"]
		
		def coveragePaths = ["/ok/folder1/requirement11",
			"/ok/folder1/requirement21",
			"/ok/folder1/requirement21",
			"/ok/folder1/folder3/requirement41",
			"/ok/folder1/folder3/requirement41"]
		
		List<Long> reqVersionIds = requirementDao.findIdsVersionsForAll(new ArrayList<Long>([-11,-21,-31,-41]).collect{it as Long});
		when :
		RequirementExportModel exportModel = exportDao.findAllRequirementModel(reqVersionIds);
		exporter.appendToWorkbook(exportModel, true);
		then :
	
		exportModel.coverages*.reqPath == coveragePaths
		exportModel.requirementsModels*.path  == paths
		exportModel.requirementsModels*.requirementVersionNumber == [1, 1, 1, 1]
	}
	
	
}

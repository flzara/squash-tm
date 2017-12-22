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

import javax.inject.Inject

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel.RequirementLinkModel
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class ExportDaoIT extends DbunitServiceSpecification{

	@Inject
	private ExportDao exporter
	
	
	@DataSet("ExportDaoIT.should create models.xml")
	def "should create models"(){
		given :
		def testCaseIds = [-10L, -11L]

		when :
		ExportModel result = exporter.findModel(testCaseIds)

		then :
		result.testCases.size() == 2
		result.datasets.size() == 1
		result.parameters.size() == 1
		result.testSteps.size() == 2

	}
	
	
	/*
	 * See dataset description in the dataset file
	 */
	@DataSet("export-req-links.xml")
	def "should collect the requirement links model"(){
		
		given :
			// setup : must fix the reciprocal references from requirement to requirement version 
			// (it had to be stripped from the dataset because it wouldn't be inserted otherwise)
			// the following maps Requirement ids to RequirementVersion ids 
			[ 
				(-255) : -255,
				(-256) : -256,
				(-257) : -259,
				(-258) : -260,
				(-259) : -261
			]
			.each { k,v -> executeSQL("update REQUIREMENT set current_version_id = $v where rln_id = $k") }
		
		
		and :
			def ids = -259l..-255l
		
		when :
			List<RequirementLinkModel> linkModels = exporter.findRequirementLinksModel(ids)
		
		then :
			linkModels.size() == 4
		
			// first, test what should be contained
			def req1v1_req3v1_related = model([reqPath : "/Test Project-1/Test Folder 1/Test Requirement 1", reqVersion : 1, relReqPath : "/Test Project-1/Test Folder 1/Test Requirement 3", relReqVersion : 1, relatedReqRole : "RELATED"])
			def req1v1_req3v3_duplicate = model([reqPath : "/Test Project-1/Test Folder 1/Test Requirement 1", reqVersion : 1, relReqPath : "/Test Project-1/Test Folder 1/Test Requirement 3", relReqVersion : 3, relatedReqRole : "DUPLICATE"])
			def req2v1_req1v1_child = model([reqPath : "/Test Project-1/Test Folder 1/Test Requirement 2", reqVersion : 1, relReqPath : "/Test Project-1/Test Folder 1/Test Requirement 1", relReqVersion : 1, relatedReqRole : "CHILD"])
			def req3v3_otherv1_child = model([reqPath : "/Test Project-1/Test Folder 1/Test Requirement 3", reqVersion : 3, relReqPath : "/Test Project-1/related to ex3", relReqVersion : 1, relatedReqRole : "CHILD"])

			linkModels.find { match(it, req1v1_req3v1_related) } != null
			linkModels.find { match(it, req1v1_req3v3_duplicate) } != null
			linkModels.find { match(it, req2v1_req1v1_child) } != null
			linkModels.find { match(it, req3v3_otherv1_child) } != null
			
			// now those that should not be found
			def unrelated = model([reqPath : "/Test Project-1/unrelated", reqVersion : 1, relReqPath : "/Test Project-1/related to ex3", relReqVersion : 1, relatedReqRole : "DUPLICATE"])
		
			linkModels.find { match(it, unrelated) } == null
			
			true
		
	}
	
	
	private boolean match(RequirementLinkModel model1, RequirementLinkModel model2){
		return model1.reqPath.equals(model2.reqPath) &&
				(model1.reqVersion == model2.reqVersion) &&
				model1.relReqPath.equals(model2.relReqPath) &&
				(model1.relReqVersion == model2.relReqVersion) &&
				model1.relatedReqRole.equals(model2.relatedReqRole)
	}
	
	private RequirementLinkModel model(Map ppts){
		return RequirementLinkModel.create(ppts["reqVersion"], ppts["relReqVersion"], ppts["relatedReqRole"], ppts["reqPath"], ppts["relReqPath"])
	}
}

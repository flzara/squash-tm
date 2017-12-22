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
package org.squashtest.tm.service.internal.testcase;

import javax.inject.Inject

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.it.basespecs.DbunitServiceSpecification;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.DatasetParamValueDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.testcase.ParameterFinder;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
class DatasetModificationServiceIT extends DbunitServiceSpecification {
	
	@Inject
	ParameterModificationService paramService;
	
	@Inject
	DatasetModificationService datasetService;
	
	@DataSet("DatasetModificationServiceIT.xml")
	def "should persist a dataset"(){
		
		when : 
			Dataset dataset = new Dataset();
			dataset.name = "newDataset";
			datasetService.persist(dataset, -100L);
			
		then : 
			TestCase testcase = session.get(TestCase.class, -100L);
			testcase.datasets.size() == 2;
			testcase.parameters.size() == 1;
			
			Dataset[] result = testcase.getDatasets().toArray(new Dataset[testcase.datasets.size()]);
			
			//result[0].name == "newDataset";
			result[0].parameterValues.size() == 1;
			result[0].parameterValues.first().parameter.name == "param101";
			result[0].parameterValues.first().paramValue == "";
			
			//result[1].name == "dataset1";
			result[1].parameterValues.size() == 1;
			result[1].parameterValues.first().parameter.name == "param101";
			result[1].parameterValues.first().paramValue == "";
	}
	
	// FIXME this test datas are not removed because of the hql update instruction. why exactly ? no idea
//	@DataSet("DatasetModificationServiceIT.xml")	
//	def "should remove a dataset"(){
//		
//		when :
//			Dataset dataset = session.get(Dataset.class, -100L)
//			datasetService.remove(dataset)
//		then :
//		
//			!found(Dataset.class, -100L)
//	}
	
	@DataSet("DatasetModificationServiceIT.xml")
	def "should change the name of a dataset"(){
		
		when :
			datasetService.changeName(-100L,"newName")
		then :
			Dataset dataset = session.get(Dataset.class, -100L)
			dataset.name == "newName";
	}
	
	@DataSet("DatasetModificationServiceIT.xml")
	def "should change the param value of a dataset"(){
		
		when :
			DatasetParamValue paramValue = session.get(DatasetParamValue.class, -100L);
			paramValue.paramValue == "";	
			datasetService.changeParamValue(-100L, "newValue");
		then :
			paramValue.paramValue == "newValue";		
	}
	
	@DataSet("DatasetModificationServiceIT.xml")
	def "should add a param value in all datasets when a param is added to the test case"(){
		given :"a test case with a dataset" 
		Dataset dataset = new Dataset();
		dataset.name = "newDataset";
		dataset.parameterValues = new HashSet<DatasetParamValue>();
		datasetService.persist(dataset, -100L);
		
		and :"a new param"
		Parameter param = new Parameter();
		param.name = "paramAjoute"
		param.description = ""
		
		when :			
			paramService.addNewParameterToTestCase(param ,-100L );
			
		then : 
			TestCase testcase = session.get(TestCase.class, -100L);
			testcase.datasets.size() == 2;
			testcase.parameters.size() == 2;
		
			Dataset[] result = testcase.getDatasets().toArray(new Dataset[testcase.datasets.size()]);
		
			//result[0].name == "newDataset";
			result[0].parameterValues.size() == 2;
			
		
			//result[1].name == "dataset1";
			result[1].parameterValues.size() == 2;
	}
}

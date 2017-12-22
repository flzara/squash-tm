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
package org.squashtest.tm.service.internal.campaign

import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService
import org.squashtest.tm.service.customfield.CustomFieldHelperService
import org.squashtest.tm.service.internal.campaign.export.CampaignExportCSVModelImpl
import spock.lang.Specification

class CampaignExportCSVModelImplTest extends Specification {

	CustomFieldHelperService cufHelperService

	CampaignExportCSVModelImpl model

	BugTrackersLocalService bugTrackerService


	def setup(){

		cufHelperService = Mock()

		model = new CampaignExportCSVModelImpl()
		model.cufHelperService = cufHelperService
		model.bugTrackerService = bugTrackerService

	}



	def "should collect all the test cases of a bunch of iterations"(){

		given :

			def iterations = []

			3.times{ itindex ->

				def iteration = new Iteration(name : "iteration ${itindex}")

				4.times { tcindex ->
					def tc = new TestCase(name : "test case ${itindex} - ${tcindex}")
					iteration.addTestPlan(new IterationTestPlanItem(tc))
				}

				iterations << iteration

			}

			//now let's "delete" some test cases

			iterations[0].testPlans[1].referencedTestCase = null	// that'd be test case 0 - 1
			iterations[1].testPlans[2].referencedTestCase = null	// that'd be test case 1 - 2
			iterations[2].testPlans[3].referencedTestCase = null	// that'd be test case 2 - 3


		when :

			def collected = model.collectAllTestCases(iterations)

		then :
			collected.collect{ it.name } as List == [
				"test case 0 - 0",
				"test case 0 - 2",
				"test case 0 - 3",
				"test case 1 - 0",
				"test case 1 - 1",
				"test case 1 - 3",
				"test case 2 - 0",
				"test case 2 - 1",
				"test case 2 - 2",

			] as List
	}


	def "should index the custom field values"(){

		given :
			CustomFieldValue iterCUFValue11 = Mock()
			iterCUFValue11.getBoundEntityId() >> new Long(1l)

			CustomFieldValue iterCUFValue12 = Mock()
			iterCUFValue12.getBoundEntityId() >> new Long(1l)

			CustomFieldValue iterCUFValue21 = Mock()
			iterCUFValue21.getBoundEntityId() >> new Long(2l)

			CustomFieldValue iterCUFValue22 = Mock()
			iterCUFValue22.getBoundEntityId() >> new Long(2l)

			def iterValues = [iterCUFValue11, iterCUFValue22, iterCUFValue12, iterCUFValue21]

		and :

			CustomFieldValue tcCUFValue11 = Mock()
			tcCUFValue11.getBoundEntityId() >> new Long(10l)

			CustomFieldValue tcCUFValue12 = Mock()
			tcCUFValue12.getBoundEntityId() >> new Long(10l)

			CustomFieldValue tcCUFValue21 = Mock()
			tcCUFValue21.getBoundEntityId() >> new Long(20l)

			CustomFieldValue tcCUFValue22 = Mock()
			tcCUFValue22.getBoundEntityId() >> new Long(20l)

			def tcValues = [tcCUFValue11, tcCUFValue21, tcCUFValue22, tcCUFValue12]


		when :
			model.createCustomFieldValuesIndex(iterValues, tcValues)

		then :

			model.iterCUFValues[1l] as Set == [iterCUFValue11, iterCUFValue12] as Set
			model.iterCUFValues[2l] as Set == [iterCUFValue21, iterCUFValue22] as Set
			model.tcCUFValues[10l] as Set == [tcCUFValue11, tcCUFValue12] as Set
			model.tcCUFValues[20l] as Set == [tcCUFValue21, tcCUFValue22] as Set


	}



	// ********************** tests on DataIterator ************************

	def "DataIterator shoud move to next test plan item"(){

		given : "configure the environment"
			def data = createCampaign()

			def campaign = data["campaign"]

			model.campaign = campaign


		and : "initial iterator state : iteration2, itp2"

			def iterator = model.dataIterator()
			iterator.iterIndex = 1
			iterator.itpIndex = 1
			iterator.iteration = data["iter2"]

		when :

			def res = iterator.moveToNextTestCase()

		then :
			res == true
			iterator.itpIndex == 2
			iterator.itp == data["item23"]


	}

	def "DataIterator should skip next item test plan and move to the one after it because the test case was deleted"(){

		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign

		and : "initial iterator state : iteration 4 itp 2"
			def iterator = model.dataIterator()
			iterator.iterIndex = 3
			iterator.itpIndex = 1
			iterator.iteration = data["iter4"]

		when :
			def res = iterator.moveToNextTestCase()

		then :
			res == true
			iterator.itpIndex == 3
			iterator.itp == data["item44"]
	}


	def "DataIterator should tell that there are no more itp to check in this iteration"(){

		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign

		and : "initial iterator state : iteration 2 itp 3"
			def iterator = model.dataIterator()
			iterator.iterIndex = 1
			iterator.itpIndex = 2
			iterator.iteration = data["iter2"]

		when :
			def res = iterator.moveToNextTestCase()

		then :
			res == false
			iterator.itp == null
	}


	def "DataIterator should move to next iteration"(){

		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign

		and : "initial iterator state : iteration 2"
			def iterator = model.dataIterator()
			iterator.iterIndex = 1
			iterator.itpIndex = 2
			iterator.iteration = data["iter2"]


		when :
			def res = iterator.moveToNextIteration()

		then :
			res == true
			iterator.iteration == data["iter3"]
			iterator.iterIndex == 2
			iterator.itpIndex == -1
	}


	def "DataIterator should say that there are no more iteration"(){

		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign

		and : "initial iterator state : iteration 4"
			def iterator = model.dataIterator()
			iterator.iterIndex = 3
			iterator.itpIndex = 3
			iterator.iteration = data["iter4"]

		when :
			def res = iterator.moveToNextIteration()

		then :
			res == false

	}


	def "DataIterator should skip the first iteration because it's empty"(){


		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign

		and : "initial iterator state : iteration 0 (dummy)"
			def iterator = model.dataIterator()
			iterator.iterIndex = -1
			iterator.itpIndex = -1

		when :
			iterator.moveNext()

		then :
			iterator.iteration == data["iter2"]

	}


	def "DataIterator should skip iteration 3 because all referenced test cases were deleted"(){

		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign

		and : "initial iterator state : iteration 2"
			def iterator = model.dataIterator()
			iterator.iterIndex = 1
			iterator.itpIndex = 2
			iterator.iteration = data["iter2"]

		when :
			iterator.moveNext()

		then :
			iterator.iteration == data["iter4"]

	}


	def "DataIterator should enumerate all the itp correctly (ie skipping empty test plan or deleted tc)"(){

		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign

		and : "we don't tweak initial iterator state "
			def iterator = model.dataIterator()

		when :

			def itps = []
			while (iterator.hasNext()) {
				itps << iterator.itp
				iterator.moveNext()
			}

		then :
			def expected = [];
			["item21","item22","item23","item42","item44"].each{ expected << data[it] }


			itps == expected

	}



	// ******************* stub methods **********************

	def populateRequirementCoverage = { tc, howmany ->
		howmany.times { tc.addRequirementCoverage(new RequirementVersionCoverage()) }
	}



	def createCampaign(){

		//the test cases

		TestCase tc1 = new TestCase(name:"tc1");
		TestCase tc2 = new TestCase(name:"tc2");
		TestCase tc3 = new TestCase(name:"tc3");
		TestCase tc4 = new TestCase(name:"tc4");
		TestCase tc5 = new TestCase(name:"tc5");
		TestCase tc6 = new TestCase(name:"tc6");



		//the iterations

		Iteration iter1 = new Iteration(name:"iter1")
		Iteration iter2 = new Iteration(name:"iter2")
		Iteration iter3 = new Iteration(name:"iter3")
		Iteration iter4 = new Iteration(name:"iter4")


		//the test plan

		IterationTestPlanItem item21 = new IterationTestPlanItem(tc1)
		IterationTestPlanItem item22 = new IterationTestPlanItem(tc2)
		IterationTestPlanItem item23 = new IterationTestPlanItem(tc3)

		IterationTestPlanItem item31 = new IterationTestPlanItem(tc6)

		IterationTestPlanItem item41 = new IterationTestPlanItem(tc5)
		IterationTestPlanItem item42 = new IterationTestPlanItem(tc2)
		IterationTestPlanItem item43 = new IterationTestPlanItem(tc6)
		IterationTestPlanItem item44 = new IterationTestPlanItem(tc4)



		//populate the iterations. Iterations 1 has no test cases.

		[item21, item22, item23].each { iter2.addTestPlan it }
		[item31].each{ iter3.addTestPlan it }
		[item41, item42, item43, item44].each { iter4.addTestPlan it}

		//now let's assume that test case 5 and 6 where deleted
		item41.referencedTestCase = null
		item43.referencedTestCase = null
		item31.referencedTestCase = null

		//the campaign

		Campaign campaign = new Campaign()

		[iter1, iter2, iter3, iter4].each{ campaign.addIteration it }


		//return a map that holds direct references on them
		return [
				"tc1" : tc1,
				"tc2" : tc2,
				"tc3" : tc3,
				"tc4" : tc4,
				"iter1" : iter1,
				"iter2" : iter2,
				"iter3" : iter3,
				"iter4" : iter4,
				"item21": item21,
				"item22": item22,
				"item23": item23,
				"item31": item31,
				"item41": item41,
				"item42": item42,
				"item43": item43,
				"item44": item44,
				"campaign" : campaign
		]
	}



}

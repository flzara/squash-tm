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
package org.squashtest.tm.service.internal.chart.engine

import com.querydsl.core.Tuple
import org.squashtest.tm.domain.chart.AxisColumn
import org.squashtest.tm.domain.chart.DataType
import org.squashtest.tm.domain.chart.MeasureColumn
import org.squashtest.tm.domain.customfield.CustomFieldOption
import org.squashtest.tm.domain.customfield.SingleSelectField
import org.squashtest.tm.domain.infolist.InfoListItem
import org.squashtest.tm.domain.infolist.UserListItem
import org.squashtest.tm.service.internal.repository.CustomFieldDao
import org.squashtest.tm.service.internal.repository.InfoListItemDao
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

import static org.squashtest.tm.domain.chart.DataType.*
import static org.squashtest.tm.domain.execution.ExecutionStatus.*
import static org.squashtest.tm.domain.testcase.TestCaseImportance.*

class TupleProcessorTest extends Specification{



	TupleProcessor processor = new TupleProcessor()

	InfoListItemDao infoListItemDao = Mock()
	CustomFieldDao customFieldDao = Mock()

	def setup(){
		processor.infoListItemDao = infoListItemDao
		processor.customFieldDao = customFieldDao
	}



	// ************** sorting *******************************

	@Unroll("should detect that re-sorting is #not necessary because #reason")
	def "should detect that re-sorting is necessary (or not)"(){

		expect :
		processor.setDefinition(definition).isResortRequired () == answer

		where :

		answer	| not	| reason						|	definition
		true	| ""	| "axes use execution status"	|	definitionWithAxes(NUMERIC, EXECUTION_STATUS, TAG)
		true	| ""	| "axes use level enum"			|	definitionWithAxes(NUMERIC, LEVEL_ENUM, TAG)
		true	| ""	| "axes use boolean"			|	definitionWithAxes(BOOLEAN, STRING, TAG)
		true	| ""	| "axes use existence"			|	definitionWithAxes(NUMERIC, TAG, EXISTENCE)
		false	| "not"	| "axes use none of the above"	|	definitionWithAxes(NUMERIC, DATE, STRING)


	}


	def "should create a comparator that sort re-sort while taking the special execution status ordering into account "(){

		given:
		def chartDefinition = definitionWithAxes(NUMERIC, EXECUTION_STATUS, TAG)
		processor.setDefinition(chartDefinition)

		and:
		def tuples = tuples([
			[3, NOT_RUN, "uh ?"],
			[3, SUCCESS, "yeah"],
			[2,  BLOCKED, "ooh"],
			[1, READY, "ready..."],
			[1, READY, "go !"]
		])

		when :
		processor.initializeTupleSorter();

		def sorted = processor.sortTuples(tuples)

		then:
		/*
		 tuple should be sorted by :
		 	1/ natural order then
		 	2/ execution status specific order then
		 	3/ natural order again
		  */

		sorted.collect { tuple -> (0..2).collect { idx -> tuple.get(idx, Object) } } == [
			[1, READY, 		"go !"],
			[1, READY, 		"ready..."],

			[2, BLOCKED, 	"ooh"],

			[3, SUCCESS, 	"yeah"],
			[3, NOT_RUN, 	"uh ?"]
		]

	}


	def "should create a composite sorter that sort by date, test case importance, execution status then test case importance again"(){

		given:
		def chartDefinition = definitionWithAxes(DATE, LEVEL_ENUM, EXECUTION_STATUS, LEVEL_ENUM)
		processor.setDefinition(chartDefinition)

		and:
		def tuples = tuples([
			// testing : should end swapped because of dates
			[date("2019/03/15"), VERY_HIGH, 	BLOCKED, 	MEDIUM],
			[date("2019/03/10"), LOW, 			BLOCKED, 	MEDIUM],

			// testing : should end swapped because of importance (first)
			[date("2019/01/15"), MEDIUM, 			SUCCESS, 	LOW],
			[date("2019/01/15"), HIGH, 			SUCCESS, 	VERY_HIGH],

			// testing : should end swapped because of execution status
			[date("2019/02/01"), MEDIUM, 		BLOCKED, 	MEDIUM],
			[date("2019/02/01"), MEDIUM,		READY, 		MEDIUM],

			// testing : should end swapped because of importance (second)
			[date("2019/01/20"), HIGH, 			SUCCESS, 	LOW],
			[date("2019/01/20"), HIGH, 			SUCCESS, 	VERY_HIGH]
		])

		when:
		processor.initializeTupleSorter();

		def sorted = processor.sortTuples(tuples)

		then:
		/*
		 tuple should be sorted by :
			 1/ natural order then
			 2/ test case importance then
			 3/ execution status specific order then
			 4/ test case importance
		  */

		def asListList = sorted.collect { tuple -> (0..3).collect { idx -> tuple.get(idx, Object) } }
		asListList == [

			// swapped because of importance (first)
			[date("2019/01/15"), HIGH, 			SUCCESS, 	VERY_HIGH],
			[date("2019/01/15"), MEDIUM, 		SUCCESS, 	LOW],

			// swapped because importance (second)
			[date("2019/01/20"), HIGH, 			SUCCESS, 	VERY_HIGH],
			[date("2019/01/20"), HIGH, 			SUCCESS, 	LOW],

			// swapped because of execution status
			[date("2019/02/01"), MEDIUM,		READY, 		MEDIUM],
			[date("2019/02/01"), MEDIUM, 		BLOCKED, 	MEDIUM],

			// swapped because of date (natural order)
			[date("2019/03/10"), LOW, 			BLOCKED, 	MEDIUM],
			[date("2019/03/15"), VERY_HIGH, 	BLOCKED, 	MEDIUM]

		]

	}

	def "the composite sorter other policies are : true < false for booleans and null first for all"(){
		given:
		def chartDefinition = definitionWithAxes(DATE, BOOLEAN)
		processor.setDefinition(chartDefinition)

		def tuples = tuples([
			[date("2019/03/10"), true],
			[null, false],
			[date("2019/03/10"), false],
			[null, true],
			[date("2019/03/10"), null]
		])

		when:
		processor.initializeTupleSorter();

		def sorted = processor.sortTuples(tuples)

		then:
		def asListList = sorted.collect { tuple -> (0..1).collect { idx -> tuple.get(idx, Object) } }
		asListList == [
			[null, true],
			[null, false],
			[date("2019/03/10"), null],
			[date("2019/03/10"), true],
			[date("2019/03/10"), false]
		]

	}

	// ********* abscissa and series extraction ********


	def "should split a tuple into abscissa and series"(){

		given :
		def chartDefinition = definitionWithAxes(DATE, BOOLEAN)
		andWithMeasures(chartDefinition, NUMERIC, NUMERIC, NUMERIC)

		processor.setDefinition(chartDefinition)

		and :
		def tuples = tuples([
			[date("2019/05/05"), true,  11, 21, 31],
			[date("2019/05/05"), false, 12, 22, 32],
			[date("2019/05/06"), true, 	13, 23, 33],
			[date("2019/05/06"), false, 14, 24, 34]
		])

		when :
		processor.extractAbscissaAndSeries(tuples)

		then :

		processor.abscissa == abscissa([
			[date("2019/05/05"), true],
			[date("2019/05/05"), false],
			[date("2019/05/06"), true],
			[date("2019/05/06"), false]
		])

		processor.series == [
			[11, 12, 13, 14],
			[21, 22, 23, 24],
			[31, 32, 33, 34]
		]


	}

	// ********** colours extraction ******************


	def "post processing : should extract the colours for axes that are infolists"() {
		given: "the definition"
		DetailedChartQuery definition = new DetailedChartQuery(
			measures: [
				measure("total testcase")
			],
			axis: [
				axis("project label", STRING),
				axis("test case category", INFO_LIST_ITEM)
			]
		)
		processor.definition = definition

		and: "the abscissa"
		def abscissa = [
			["project1", "code1"] as Object[],
			["project1", "code2"] as Object[],
			["project1", "code3"] as Object[],
			["project2", "code1"] as Object[],
			["project2", "code2"] as Object[]]

		processor.abscissa = abscissa

		and: "the rest"

		def item1 = new UserListItem(code: "code1", colour: "#000000")
		def item2 = new UserListItem(code: "code2", colour: "#FFFFFF")
		def item3 = new UserListItem(code: "code3", colour: "#000FFF")


		infoListItemDao.findByCodeIn(_) >> [item1, item2, item3]

		when:
		processor.extractColours()

		then:

		processor.colours == ["#000000", "#FFFFFF", "#000FFF"]

	}


	def "post processing : should extract the colours for axes that are cuf lists"() {
		given: "the definition"
		DetailedChartQuery definition = new DetailedChartQuery(
			measures: [
				measure("total testcase")
			],
			axis: [
				axis("project label", STRING),
				axis("test case flavour", LIST)
			]
		)
		processor.definition = definition

		and: "the abscissa"
		def abscissa = [
			["project1", "vanilla"] as Object[],
			["project1", "chocolate"] as Object[],
			["project1", "peanuts"] as Object[],
			["project2", "vanilla"] as Object[],
			["project2", "chocolate"] as Object[]]

		processor.abscissa = abscissa

		and: "the rest"

		SingleSelectField listCuf = new SingleSelectField(options:[
			new CustomFieldOption("vanilla", "vanilla", "#AAAAAA"),
			new CustomFieldOption("chocolate", "chocolate", "#BBBBBB"),
			new CustomFieldOption("peanuts", "peanuts", "#CCCCCC")
		])

		customFieldDao.findSingleSelectFieldById(_) >> listCuf

		when:
		processor.extractColours()

		then:

		processor.colours == ["#AAAAAA", "#BBBBBB", "#CCCCCC"]

	}


	// ********** abscissa post processing ************

	@Unroll("abscissa post processing is #not required because #reason")
	def "should know whether post processing is required or not"(){

		expect :
		processor.setDefinition(definition).isPostProcessingRequired() == answer

		where:
		answer	| not	| reason								|	definition
		true	| ""	| "axes use info list item"				|	definitionWithAxes(NUMERIC, INFO_LIST_ITEM, TAG)
		false	| "not"	| "axes use no datatype requiring it"	|	definitionWithAxes(NUMERIC, LEVEL_ENUM, TAG)

	}


	def "should create an abscissa post processor adequate for axes : numeric, infolist, infolist "(){

		given:
		def chartDefinition = definitionWithAxes(NUMERIC, INFO_LIST_ITEM, INFO_LIST_ITEM)
		processor.setDefinition(chartDefinition)

		and :
		processor.abscissa = abscissa([
			[1, "ITEM_1", "OBJ_5"],
			[1, "ITEM_2", "OBJ_6"],
			[1, "ITEM_3", "OBJ_7"]
		])

		and :
		infoListItemDao.findByCode(_) >> { args ->
			new UserListItem(label:"LABEL_${args[0]}")
		}


		when :
		processor.initializeAbscissaPostProcessors()
		processor.postProcessAbscissa()

		then :

		// the info list item codes are replaced by their label
		processor.abscissa == abscissa([
			[1, "LABEL_ITEM_1", "LABEL_OBJ_5"],
			[1, "LABEL_ITEM_2", "LABEL_OBJ_6"],
			[1, "LABEL_ITEM_3", "LABEL_OBJ_7"]
		])

	}


	def "abscissa post processing should handle null data"(){

		given:
		def chartDefinition = definitionWithAxes(NUMERIC, INFO_LIST_ITEM, INFO_LIST_ITEM)
		processor.setDefinition(chartDefinition)

		and :
		processor.abscissa = abscissa([
			[1, "ITEM_1", "OBJ_5"],
			[1, "ITEM_2", null],
			[1, null, "OBJ_7"]
		])

		and :
		infoListItemDao.findByCode(_) >> { args ->
			Mock(InfoListItem){ getLabel() >> "LABEL_${args[0]}"}
		}

		when :
		processor.initializeAbscissaPostProcessors()
		processor.postProcessAbscissa()

		then :

		// the info list item codes are replaced by their label
		processor.abscissa == abscissa([
			[1, "LABEL_ITEM_1", "LABEL_OBJ_5"],
			[1, "LABEL_ITEM_2", null],
			[1, null, "LABEL_OBJ_7"]
		])


	}

	// ********** create series ******************************************

	def "should create the series based once the data were processed"(){

		given :
		def definition = new DetailedChartQuery(
			measures: [
				measure("total testcase", NUMERIC)
			],
			axis: [
				axis("project label", STRING),
				axis("test case flavour", LIST)
			]
		)

		processor.definition = definition

		and:
		processor.abscissa = abscissa([
			["future cool things", "ham"],
			["current business", "chicken"],
		])

		processor.series = [[1, 3]]
		processor.colours = ["#111111", "#222222"]

		when :
		def chartSeries = processor.createChartSeries()


		then :
		chartSeries.abscissa == processor.abscissa
		chartSeries.colours == processor.colours
		chartSeries.series == [
			"total testcase" : [1, 3]
		]


	}


	// ********** scaffolding (non test methods )*************************


	def axis(label, dataType) {
		Mock(AxisColumn) {
			getLabel() >> label
			getDataType() >> dataType
		}
	}

	def measure(label, dataType = null) {
		Mock(MeasureColumn) {
			getLabel() >> label
			getDataType() >> dataType
		}
	}


	def definitionWithAxes(DataType... types){
		def axes = types.collect { axis("axis", it)	}

		return Mock(DetailedChartQuery){
			getAxis() >> axes
		}
	}

	def andWithMeasures(theMock, DataType... types){
		def measures = types.collect { measure("measure", it) }

		theMock.getMeasures() >> measures;

		return theMock;
	}


	def tuples(listList){
		return listList.collect { tupleValues ->
			Mock(Tuple){
				get(_, _) >> { args -> tupleValues[args[0]]}
			}
		}
	}

	// cast to List<Object[]>
	def abscissa(listList){
		return listList.collect {
			it as Object[]
		}
	}

	def date(str){
		new SimpleDateFormat("yyyy/MM/dd").parse(str)
	}



}

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
package org.squashtest.tm.service.internal.helper


import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.PathBuilder
import org.joda.time.LocalDate
import org.springframework.data.domain.Sort
import org.squashtest.tm.core.foundation.collection.ColumnFiltering
import org.squashtest.tm.core.foundation.collection.SimpleColumnFiltering
import org.squashtest.tm.core.foundation.lang.Couple
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import static org.squashtest.tm.domain.testcase.TestCaseImportance.*
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat
import java.time.temporal.Temporal

import static org.squashtest.tm.service.internal.helper.PagingToQueryDsl.*
import static org.squashtest.tm.service.internal.helper.PagingToQueryDsl.ColumnFilteringConverter.CompOperator.*

class PagingToQueryDslTest extends Specification {

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")





	// that one is because (at the time this test class was first written), I want my 100% code coverage :
	def "private constructor is private"(){
		expect :
		new PagingToQueryDsl() instanceof PagingToQueryDsl
	}

	// **************** the base builder (used subclass is SortConverter) ***********************

	@Unroll("should create entity alias #alias for class #classname")
	def "should properly create an entity alias"(){

		expect :
		sortConverter(clazz).createAlias() == alias

		where :
		clazz   	|	alias     	|	classname
		Project 	|	"project" 	|	"Project"
		TestCase	|	"testCase"	|	"TestCase"

	}


	def "should create the base path for an entity"(){

		given :
		def converter = sortConverter(TestCase)
		when :
		converter.initBasePath()

		then :
		def base
		use(ReflectionCategory){
			base = BaseConverter.get field: "basePath", of: converter
		}
		base instanceof PathBuilder
		base.toString() == "testCase"
	}


	def "should register the type for multiple properties"(){

		given :
		SortConverter converter = sortConverter().forEntity(TestCase)

		when :
		converter.typeFor("name", "reference", "description").isClass(String)

		then :
		Map propertyTypes
		// accessing protected field of superclass
		use(ReflectionCategory){
			propertyTypes = BaseConverter.get field: "propertyTypes", of : converter
		}


		propertyTypes["name"] == String
		propertyTypes["reference"] == String
		propertyTypes["description"] == String

	}

	def "create an entity path for the given property"(){

		given :
			def converter = sortConverter(TestCase)
			converter.initBasePath()
		when :
			def path = converter.toEntityPath("project.name")

		then :
			path instanceof EntityPathBase
			path.toString() == "testCase.project.name"
	}


	// **************** tests for SortConverter ****************

	@Unroll("should convert #spring to #querydsl")
	def "should convert null handling"(){

		expect :
			sortConverter(TestCase).toQdslNullhandling(spring) == querydsl


		where :
		spring								|	querydsl
		Sort.NullHandling.NULLS_FIRST		|	OrderSpecifier.NullHandling.NullsFirst
		Sort.NullHandling.NULLS_LAST		|	OrderSpecifier.NullHandling.NullsLast
		Sort.NullHandling.NATIVE			|	OrderSpecifier.NullHandling.Default
	}


	@Unroll("should convert #spring to #querydsl")
	def "should convert sort direction"(){

		expect :
			sortConverter(TestCase).toQdslOrder(spring) == querydsl

		where :
		spring					|	querydsl
		Sort.Direction.ASC		|	Order.ASC
		Sort.Direction.DESC		|	Order.DESC
	}


	@Unroll("should identity class #classname as #neg level enum")
	def "should detect level enum"(){

		expect :
			sortConverter(TestCase).isLevelEnum(clazz) == res

		where :
		clazz              |	res   | classname            | neg
		TestCaseImportance |	true  | "TestCaseImportance" | ""
		BindableEntity     |	false | "BindableEntity"     | "not"
		String			   |	false | "String"			 | "not"

	}

	def "should generate an expression appropriate for level enums"(){

		given :
			def converter = sortConverter(TestCase)
			converter.initBasePath()
			def pathImportance = converter.toEntityPath("importance")

		when :
			def res = converter.orderByLevel(pathImportance, TestCaseImportance)

		then :
			res.toString() == "case when testCase.importance = VERY_HIGH then 1 when testCase.importance = HIGH then 2 when testCase.importance = MEDIUM then 3 when testCase.importance = LOW then 4 else -1000 end"
	}



	def "and now, should sort by creation date descending, importance ascending and name ascending"(){

		given :

		Sort sort = new Sort([order("createdOn", "desc"),
							  order("importance", "asc", "native"),
							  order("name", "asc", "first")])

		when :
		List<OrderSpecifier> res = sortConverter(TestCase).from(sort)
										.typeFor("importance").isClass(TestCaseImportance)
									.build()

		then :
		def targets = res.collect { it.target.toString() }
		targets[0] == "testCase.createdOn"
		targets[1] =~ /case when testCase.importance = .../
		targets[2] == "testCase.name"

		res.collect { it.order } == [Order.DESC, Order.ASC, Order.ASC]
		res.collect { it.nullHandling } == [OrderSpecifier.NullHandling.NullsLast, OrderSpecifier.NullHandling.Default, OrderSpecifier.NullHandling.NullsFirst]


	}



	@Unroll("should rant because sort converter was not given #reason")
	def "should rant because sort converter not fully initialized"(){

		when :
			sortConverter().forEntity(entity).from(springsort).build()

		then :
			thrown IllegalStateException

		where :
		entity		|	springsort		| reason
		null		|	Sort.unsorted()	| "the class for the root entity"
		TestCase	|	null			| "the sort object to convert"

	}

	// ****************** tests for filterConverter **************************

	@Unroll("should resolve the class of a property as #clazz.simpleName because #reason")
	def "should resolve the class of a property"(){

		expect :
		converter.resolveClass(property) == clazz

		where :

		converter                                         |	property    |	clazz  |	reason
		fconv()                                           |	"name"      |	String |	"no type specified so resorting to default"
		fconv(["name": String])                           |	"name"      |	String |	"because type was explicitly specified"
		fconv(["createdOn": Date])                        |	"createdOn" |	Date   |	"because type was explicitly specified"
		fconv([:], ["createdOn": "datebetween"])          |	"createdOn" |	Date   |	"because was inferred from comparison operator"
		fconv(["createdOn": Date], ["createdOn": "like"]) |	"createdOn" |	Date   |	"because when declared type and comparison operator are conflicting, the declared type takes precedence"
	}


	@Unroll("should resolve the operator for a property as #operator because #reason")
	def "should resolve the operator"(){

		expect :
		converter.resolveOperator(property) == operator

		where :

		converter                                | property    | operator     |	reason
		fconv()                                  | "name"      | LIKE         |	"property class resolved to string and default operation for string is like"
		fconv(["createdOn":Date])                | "createdOn" | EQUALITY     |	"property is not a string and default operation in this case is equality"
		fconv([:], ["createdOn": "datebetween"]) | "createdOn" | DATE_BETWEEN |	"operator was specified as date between"
		fconv([:], ["createdOn": "like"])        | "createdOn" | LIKE         |	"operator was specified as like"
		fconv([:], ["createdOn": "equality"])    | "createdOn" | EQUALITY     |	"operator was specified as equality"

	}


	// ok that one is a bit silly
	def "should test which classes can coerce to enum and which can not"(){

		given :
		def can = [TestCaseImportance]
		def cannot = [Date, Temporal, LocalDate, Short, Integer, Long, BigInteger, Float, Double, BigDecimal]

		when :
		def converter = filterConverter(TestCase)

		then :
		can.every { converter.isEnum it }
		cannot.every {converter.isEnum(it) == false}

	}

	def "should test which classes can coerce to dates and which cannot"(){

		given :
		def can = [Date, Temporal, LocalDate]
		def cannot = [TestCaseImportance, Short, Integer, Long, BigInteger, Float, Double, BigDecimal]

		when :
		def converter = filterConverter(TestCase)

		then :
		can.every { converter.canCoerceToDate it }
		cannot.every {converter.canCoerceToDate(it) == false}

	}

	def "should test which classes can coerce to integer and which can not"(){

		given :
		def can = [Short, Integer, Long, BigInteger]
		def cannot = [TestCaseImportance, Date, Temporal, LocalDate, Float, Double, BigDecimal]

		when :
		def converter = filterConverter(TestCase)

		then :
		can.every { converter.canCoerceToInteger it }
		cannot.every {converter.canCoerceToInteger(it) == false}

	}

	def "should test which classes can coerce to decimal and which can not"(){

		given :
		def can = [ Float, Double, BigDecimal]
		def cannot = [TestCaseImportance, Date, Temporal, LocalDate,  BigInteger, Short, Integer, Long]

		when :
		def converter = filterConverter(TestCase)

		then :
		can.every { converter.canCoerceToDecimal it }
		cannot.every {converter.canCoerceToDecimal(it) == false}

	}

	def "should parse the parameter as a date"(){

		expect:
		filterConverter(TestCase).parseAsDate("2018-10-19") instanceof Date

	}

	def "should fail to parse the parameter as a date"(){
		when :
		filterConverter(TestCase).parseAsDate("uh?")

		then:
		thrown RuntimeException
	}

	def "should parse a duration (a pair of date)"(){

		given :
		def duration = "2018-10-11 - 2018-10-22" // happy Apollo 7 anniversary !

		when :
		def res = filterConverter(TestCase).parseAsCoupleDates(duration)

		then :
		res.a1 instanceof Date
		formatter.format(res.a1) == "2018-10-11"

		res.a2 instanceof Date
		formatter.format(res.a2) == "2018-10-22"

	}

	@Unroll("should resolve parameters as #resolvedmsg")
	def "should resolve parameters"(){

		expect :
		correct converter.resolveParameters("ppt", value)

		where :

		value  						| converter							| correct							|	resolvedmsg
		"bob"						| fconv(["ppt":String])				| { it == "bob" }					|	"a String"
		"MEDIUM"					| fconv(["ppt":TestCaseImportance])	| { it == MEDIUM}					|	"an Enum"
		"2018-10-19"				| fconv(["ppt":Date])				| { it instanceof Date} 			|	"a Date"
		"2018-10-11 - 2018-10-22"	| fconv(["ppt":Date])				| { it instanceof Couple}			|	"a duration (pair of date)"
		"205"						| fconv(["ppt":Integer])			| { it == 205L}						|	"a long int"
		"5.5"						| fconv(["ppt":Float])				| { it > 5.4999D && it <5.5001D}	|	"a double precision"

	}


	def "should create a between date expression"(){

		given :
			def converter = filterConverter().forEntity(TestCase)
			converter.initBasePath()

			def path = converter.toEntityPath("property")
			def duration = new Couple(formatter.parse("2018-10-11"), formatter.parse("2018-10-22"))


		when :
			def expr = converter.asBetweenDateExpression(path, duration)

		then :
			expr.toString() == "testCase.property between Thu Oct 11 00:00:00 CEST 2018 and Mon Oct 22 00:00:00 CEST 2018"

	}


	def "should create a like expression"(){

		given :
		def converter = filterConverter(TestCase)
		converter.initBasePath()

		def path = converter.toEntityPath("property")
		def searchTerm = "bob"

		when :
		def expr = converter.asLikeExpression(path, searchTerm)

		then :
		expr.toString() == "lower(testCase.property) like %bob%"

	}


	def "should create the boolean expression corresponding to each of a set of property"(){

		given :
		def filter = filter(name: "Bob", createdOn: "2018-10-11 - 2018-10-22", "project.name": "Project")

		and :
		def converter = filterConverter(TestCase).from(filter)
							.typeFor("name", "project.name").isClass(String)
							.typeFor("createdOn").isClass(Date)
							.compare("name").withEquality()
							.compare("project.name").withLike()
							.compare("createdOn").withBetweenDates()

		converter.initBasePath()

		when :
		def nameExpr = converter.convert "name"
		def projExpr = converter.convert "project.name"
		def creaExpr = converter.convert "createdOn"

		then :
		nameExpr.toString() == "testCase.name = Bob"
		projExpr.toString() == "lower(testCase.project.name) like %project%"
		creaExpr.toString() =~ /testCase.createdOn between /


	}


	def "and now, should generate a complete predicate"(){

		given :
		def filter = filter(name: "Bob", createdOn: "2018-10-11 - 2018-10-22", "project.name": "Project")

		and :
		// this definition of converter is same as method "should create a boolean expression etc" above,
		// but uses default behaviors and infer-by-context mechanism for types and operation resolution
		def converter = filterConverter(TestCase).from(filter)
			.compare("createdOn").withBetweenDates()
			.compare("name").withEquality()

		when:
		def expr = converter.build()

		then:
		expr.toString() == "testCase.name = Bob && lower(testCase.project.name) like %project% && testCase.createdOn between Thu Oct 11 00:00:00 CEST 2018 and Mon Oct 22 00:00:00 CEST 2018"

	}


	@Unroll("should rant because sort converter was not given #reason")
	def "should rant because filter converter not fully initialized"(){

		when :
		filterConverter().forEntity(entity).from(springsort).build()

		then :
		thrown IllegalStateException

		where :
		entity		|	springsort                 		| reason
		null		|	ColumnFiltering.unfiltered() | "the class for the root entity"
		TestCase	|	null                         | "the filter object to convert"

	}

	// ************ infrastructure ************

	def order(property, direction = "asc", nullhandling="last"){
		Sort.Direction dir = (direction == "asc") ? Sort.Direction.ASC : Sort.Direction.DESC

		Sort.NullHandling nullhdl
		switch(nullhandling){
			case "first" : nullhdl = Sort.NullHandling.NULLS_FIRST; break;
			case "last" : nullhdl = Sort.NullHandling.NULLS_LAST; break;
			case "native" : nullhdl = Sort.NullHandling.NATIVE; break;

		}

		return new Sort.Order(dir, property, nullhdl)

	}


	def fconv(Map classmap = [:], Map opermap = [:]){
		def converter = filterConverter(TestCase)
		classmap.each { k,v -> converter.typeFor(k).isClass(v)}
		opermap.each { k,v ->
			def opConfig = converter.compare(k)
			switch(v){
				case "datebetween" 	: opConfig.withBetweenDates(); break;
				case "equality"		: opConfig.withEquality(); break;
				case "like"			: opConfig.withLike(); break;
				default				: throw new UnsupportedOperationException("operator $v not yet supported, update this test !")
			}
		}
		return converter
	}

	def filter(attrs){
		return new SimpleColumnFiltering().with {
			attrs.each { k,v -> it.addFilter(k,v)}
			it
		}
	}

}

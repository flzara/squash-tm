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

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.PathBuilder
import org.springframework.data.domain.Sort
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.service.internal.helper.PagingToQueryDsl.*

class PagingToQueryDslTest extends Specification {


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
			base = BaseDslProcessor.get field: "basePath", of: converter
		}
		base instanceof PathBuilder
		base.toString() == "testCase"
	}


	def "should register the type for multiple properties"(){

		given :
		SortConverter converter = sortConverter(TestCase)

		when :
		converter.typeFor("name", "reference", "description").isClass(String)

		then :
		Map propertyTypes
		// accessing protected field of superclass
		use(ReflectionCategory){
			propertyTypes = BaseDslProcessor.get field: "propertyTypes", of : converter
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

	def "should sort by creation date descending and name ascending"(){

		given :

		Sort sort = new Sort([order("createdOn", "desc"), order("name", "asc", "first")])

		when :
		List<OrderSpecifier> res = sortConverter(TestCase).from(sort).build()

		then :
		res.collect { it.target.toString() } == ["testCase.createdOn", "testCase.name"]
		res.collect { it.order } == [Order.DESC, Order.ASC]
		res.collect { it.nullHandling } == [OrderSpecifier.NullHandling.NullsLast, OrderSpecifier.NullHandling.NullsFirst]


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

}

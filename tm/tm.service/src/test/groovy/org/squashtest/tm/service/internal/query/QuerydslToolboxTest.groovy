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
package org.squashtest.tm.service.internal.query

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.hibernate.HibernateQuery
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery
import org.squashtest.tm.domain.query.ColumnType
import org.squashtest.tm.domain.query.DataType
import org.squashtest.tm.domain.query.Operation
import org.squashtest.tm.domain.query.QueryColumnPrototype
import org.squashtest.tm.domain.query.QueryOrderingColumn
import org.squashtest.tm.domain.query.SpecializedEntityType
import org.squashtest.tm.domain.requirement.QRequirement
import org.squashtest.tm.domain.requirement.QRequirementVersion
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage
import org.squashtest.tm.domain.testcase.QTestCase
import spock.lang.Specification

class QuerydslToolboxTest extends Specification{

	static QTestCase tc = QTestCase.testCase;
	static QRequirementVersionCoverage cov = QRequirementVersionCoverage.requirementVersionCoverage
	static QRequirementVersion v = new QRequirementVersion("version5")
	static QRequirement r = QRequirement.requirement


	def QuerydslToolbox utils = new QuerydslToolbox()

	def "should collect aliases in the given query"(){

		given :
		HibernateQuery q = new ExtendedHibernateQuery();
		q.from(tc).innerJoin(tc.requirementVersionCoverages, cov).leftJoin(cov.verifiedRequirementVersion, v)
				.where(v.name.like(Expressions.constant("bob")))

		when :
		Set<String> aliases = utils.getJoinedAliases(q)

		then :
		aliases == ["testCase", "requirementVersionCoverage", "version5"] as Set

	}

	def "should build a join path using an existing path base"(){


		when :
		def builder = utils.makePath(r, v, "versions")

		then :
		builder.toString()=="requirement.versions"

	}

	def "should return the QBean with the default alias"(){
		when :
		EntityPathBase<?> path = utils.getQBean(InternalEntityType.REQUIREMENT);

		then :
		path.metadata.name == "requirement"
	}

	def "should return the QBean with a suffixed alias - for subcontext -"(){
		given :
		def toolbox = new QuerydslToolbox("sub")

		when :
		EntityPathBase<?> path = toolbox.getQBean(InternalEntityType.REQUIREMENT)

		then :
		path.metadata.name == "requirement_sub"
	}

	def "should return the QBean with a forced alias"(){
		given :
		utils.forceAlias(InternalEntityType.REQUIREMENT, "req")

		when :
		EntityPathBase path = utils.getQBean(InternalEntityType.REQUIREMENT)

		then :
		path.metadata.name == "req"


	}

	def "should create path for standard Custom Field Value"(){
		when:
		PathBuilder pathBuilder = utils.makePathForValueCFV("TEST_CASE_CUF_TEXT");

		then:
		pathBuilder.toString().equals("TEST_CASE_CUF_TEXT.value");
	}

	def "should create path for numeric Custom Field Value"(){
		when:
		PathBuilder pathBuilder = utils.makePathForNumericValueCFV("TEST_CASE_CUF_NUM");

		then:
		pathBuilder.toString().equals("TEST_CASE_CUF_NUM.numericValue");
	}

	def "should create path for TAG Custom Field Value"(){
		when:
		PathBuilder pathBuilder = utils.makePathForTagValueCFV("TEST_CASE_CUF_TAG");

		then:
		pathBuilder.toString().equals("TEST_CASE_CUF_TAG.label");
	}


	def "should create an expression suitable for sorting on a level num"(){
		given:
			def column = Mock(QueryOrderingColumn){
				getColumn() >> Mock(QueryColumnPrototype){
					getColumnType() >> ColumnType.ATTRIBUTE
					representsEntityItself() >> false
					getAttributeName() >> "importance"
					getDataType() >> DataType.LEVEL_ENUM
					getSpecializedType() >> new SpecializedEntityType(EntityType.TEST_CASE, null)
				}
				getSpecializedType() >> new SpecializedEntityType(EntityType.TEST_CASE, null)
				getOperation() >> Operation.NONE
				getDataType() >> DataType.LEVEL_ENUM
			}

		when:
		def resultExpr = utils.createAsCaseWhen(column)

		then:
		resultExpr.toString() == "case when testCase.importance = VERY_HIGH then 1 when testCase.importance = HIGH then 2 when testCase.importance = MEDIUM then 3 when testCase.importance = LOW then 4 else -1000 end"
	}

	def "should throw an IllegalArgumentException when trying to create an Entity Predicate for an invalid operation"() {
		when:
		utils.createEntityPredicate(Operation.BY_YEAR, Mock(Expression))

		then:
		thrown IllegalArgumentException
	}

	def "should create an expression for Entity DataType"() {
		given:
		def operation = Operation.IS_CLASS

		when:
		def resultExpr = utils.createEntityPredicate(operation, Mock(Expression), Mock(Expression))

		then:
		resultExpr != null
		resultExpr instanceof BooleanExpression
	}

}

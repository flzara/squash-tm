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

import com.querydsl.core.types.dsl.PathBuilder
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage
import org.squashtest.tm.domain.testcase.QTestCase

import spock.lang.Specification

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.hibernate.HibernateQuery

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



}

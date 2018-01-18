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
package org.squashtest.tm.service.internal.chart.engine;

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.QIssue;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.QCampaign;
import org.squashtest.tm.domain.campaign.QIteration;
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem;
import org.squashtest.tm.domain.chart.SpecializedEntityType;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.QCustomFieldValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.QExecution;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.QInfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.QMilestone;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.QAutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.QAutomatedTest;
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.QTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.users.QUser;
import org.squashtest.tm.domain.users.User;

import com.querydsl.core.types.dsl.EntityPathBase;


/**
 * This enum extends {@link EntityType} and includes table real names and hidden tables that aren't officially
 * disclosed to the end user. Internal usage only.
 *
 *
 * @author bsiri
 *
 */
enum InternalEntityType {

	// @formatter:off
	REQUIREMENT(Requirement.class , QRequirement.requirement){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QRequirement(alias);
		}

	},
	REQUIREMENT_VERSION(RequirementVersion.class, QRequirementVersion.requirementVersion){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QRequirementVersion(alias);
		}
	},

	REQUIREMENT_VERSION_COVERAGE(RequirementVersionCoverage.class, QRequirementVersionCoverage.requirementVersionCoverage){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QRequirementVersionCoverage(alias);
		}
	},
	TEST_CASE(TestCase.class, QTestCase.testCase){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QTestCase(alias);
		}
	},
	CAMPAIGN(Campaign.class, QCampaign.campaign){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QCampaign(alias);
		}
	},
	ITERATION(Iteration.class, QIteration.iteration){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QIteration(alias);
		}
	},
	ITEM_TEST_PLAN(IterationTestPlanItem.class, QIterationTestPlanItem.iterationTestPlanItem){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QIterationTestPlanItem(alias);
		}
	},
	EXECUTION(Execution.class, QExecution.execution){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QExecution(alias);
		}
	},
	ISSUE(Issue.class, QIssue.issue){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QIssue(alias);
		}
	},
	TEST_CASE_STEP(TestStep.class, QTestStep.testStep){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QTestStep(alias);
		}

	},

	TEST_CASE_NATURE(InfoListItem.class, new QInfoListItem("testcaseNature")){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QInfoListItem(alias);
		}
	},

	TEST_CASE_TYPE(InfoListItem.class, new QInfoListItem("testcaseType")){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QInfoListItem(alias);
		}

	},

	REQUIREMENT_VERSION_CATEGORY(InfoListItem.class, new QInfoListItem("reqversionCategory")){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QInfoListItem(alias);
		}

	},
	ITERATION_TEST_PLAN_ASSIGNED_USER(User.class, new QUser("iterTestPlanAssignedUser")){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QUser(alias);
		}

	},
	TEST_CASE_MILESTONE(Milestone.class, new QMilestone("testCaseMilestone")){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QMilestone(alias);
		}

	},
	REQUIREMENT_VERSION_MILESTONE(Milestone.class, new QMilestone("reqversionMilestone")){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QMilestone(alias);
		}

	},
	AUTOMATED_TEST(AutomatedTest.class, QAutomatedTest.automatedTest){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QAutomatedTest(alias);
		}

	},
	CAMPAIGN_MILESTONE(Milestone.class, new QMilestone("campaignMilestone")){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QMilestone(alias);
		}

	},

	AUTOMATED_EXECUTION_EXTENDER(AutomatedExecutionExtender.class, QAutomatedExecutionExtender.automatedExecutionExtender){
		@Override
		EntityPathBase<?> getAliasedQBean(String alias) {
			return new QAutomatedExecutionExtender(alias);
		}

	};

	// @formatter:on



	private Class<?> entityClass;
	private EntityPathBase<?> qBean;

	InternalEntityType(Class<?> entityClass, EntityPathBase<?> qBean){
		this.entityClass = entityClass;
		this.qBean = qBean;
	}


	Class<?> getEntityClass(){
		return entityClass;
	}

	// fun fact : in the querydsl domain a QBean is not exactly an EntityPathBase
	EntityPathBase<?> getQBean(){
		return qBean;
	}

	abstract EntityPathBase<?> getAliasedQBean(String alias);


	static InternalEntityType fromSpecializedType(SpecializedEntityType domainType){
		String name =  domainType.getEntityType().name();
		SpecializedEntityType.EntityRole entityRole = domainType.getEntityRole();
		if (entityRole != null && entityRole != SpecializedEntityType.EntityRole.CUSTOM_FIELD) {
			name = entityRole.name();
		}
		if (entityRole != null && entityRole == SpecializedEntityType.EntityRole.CUSTOM_FIELD) {
			name = domainType.getEntityType().name();
		}
		try{
			return InternalEntityType.valueOf(name);
		}
		catch(Exception ex){
			throw new IllegalArgumentException("Unimplemented : cannot convert type '"+domainType+"' to a corresponding internal type", ex);
		}

	}


}

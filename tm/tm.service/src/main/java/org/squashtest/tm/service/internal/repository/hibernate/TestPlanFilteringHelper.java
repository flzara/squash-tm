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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;

import javax.persistence.Query;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Gregory Fouquet
 *
 */
final class TestPlanFilteringHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestPlanFilteringHelper.class);

	public static final String PROJECT_FILTER = "projectFilter";
	public static final String REFERENCE_FILTER = "referenceFilter";
	public static final String TESTCASE_FILTER = "testcaseFilter";
	public static final String WEIGHT_FILTER = "weightFilter";
	public static final String DATASET_FILTER = "datasetFilter";
	public static final String STATUS_FILTER = "statusFilter";
	public static final String USER_FILTER = "userFilter";
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String TESTSUITE_FILTER = "testsuiteFilter";

	// TODO these strings come from UI but are hidden deep plus they are defined in HCD and HTSD. They should be
	// factored out.
	public static final String PROJECT_DATA = "project-name";
	public static final String REFERENCE_DATA = "reference";
	public static final String TESTCASE_DATA = "tc-name";
	public static final String WEIGHT_DATA = "importance";
	public static final String DATASET_DATA = "dataset.selected.name";
	public static final String TESTSUITE_DATA = "suite";
	public static final String STATUS_DATA = "status";
	public static final String USER_DATA = "assignee-login";
	public static final String MODE_DATA = "exec-mode";
	public static final String LASTEXEC_DATA = "last-exec-on";

	private static final String HQL_INDEXED_TEST_PLAN_PROJECT_FILTER = "and Project.name like :projectFilter ";
	private static final String HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER = "and TestCase.reference like :referenceFilter ";
	private static final String HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER = "and TestCase.name like :testcaseFilter ";
	private static final String HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER = "and TestCase.importance = :weightFilter ";
	private static final String HQL_INDEXED_TEST_PLAN_DATASET_FILTER = "and Dataset.name like :datasetFilter ";
	private static final String HQL_INDEXED_TEST_PLAN_STATUS_FILTER = "and IterationTestPlanItem.executionStatus = :statusFilter ";

	public static final String HQL_INDEXED_TEST_PLAN_MODEAUTO_FILTER = "and TestCase.automatedTest is not null ";
	public static final String HQL_INDEXED_TEST_PLAN_MODEMANUAL_FILTER = "and TestCase.automatedTest is null ";
	public static final String HQL_INDEXED_TEST_PLAN_USER_FILTER = "and IterationTestPlanItem.user.id = :userFilter ";
	public static final String HQL_INDEXED_TEST_PLAN_NULL_USER_FILTER = "and IterationTestPlanItem.user is null ";
	public static final String HQL_INDEXED_TEST_PLAN_EXECUTIONDATE_FILTER = "and IterationTestPlanItem.lastExecutedOn between :startDate and :endDate ";

	private static final Map<String, String> SIMPLE_FILTER_CLAUSES;
	static {
		HashMap<String, String> clauses = new HashMap<>();
		clauses.put(PROJECT_DATA, HQL_INDEXED_TEST_PLAN_PROJECT_FILTER);
		clauses.put(REFERENCE_DATA, HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER);
		clauses.put(TESTCASE_DATA, HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER);
		clauses.put(WEIGHT_DATA, HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER);
		clauses.put(DATASET_DATA, HQL_INDEXED_TEST_PLAN_DATASET_FILTER);
		clauses.put(STATUS_DATA, HQL_INDEXED_TEST_PLAN_STATUS_FILTER);
		clauses.put(LASTEXEC_DATA, HQL_INDEXED_TEST_PLAN_EXECUTIONDATE_FILTER);
		SIMPLE_FILTER_CLAUSES = Collections.unmodifiableMap(clauses);
	}

	private static String anywhereToken(String token) {
		return '%' + token + '%';
	}

	public static void setFilters(Query query, Filtering filtering, ColumnFiltering columnFiltering) { // NOSONAR:START This is basically a huge switch

		if (filtering.isDefined()) {
			query.setParameter("userLogin", filtering.getFilter());
		}

		if (columnFiltering.hasFilter(PROJECT_DATA)) {
			query.setParameter(PROJECT_FILTER, anywhereToken(columnFiltering.getFilter(PROJECT_DATA)));
		}

		if (columnFiltering.hasFilter(REFERENCE_DATA)) {
			query.setParameter(REFERENCE_FILTER, anywhereToken(columnFiltering.getFilter(REFERENCE_DATA)));
		}

		if (columnFiltering.hasFilter(TESTCASE_DATA)) {
			query.setParameter(TESTCASE_FILTER, anywhereToken(columnFiltering.getFilter(TESTCASE_DATA)));
		}

		if (columnFiltering.hasFilter(WEIGHT_DATA)) {
			String filter = columnFiltering.getFilter(WEIGHT_DATA);
			TestCaseImportance tci = TestCaseImportance.valueOf(filter);
			query.setParameter(WEIGHT_FILTER, tci);
		}

		if (columnFiltering.hasFilter(DATASET_DATA)) {
			query.setParameter(DATASET_FILTER, anywhereToken(columnFiltering.getFilter(DATASET_DATA)));
		}

		if (columnFiltering.hasFilter(TESTSUITE_DATA)) {
			query.setParameter(TESTSUITE_FILTER, anywhereToken(columnFiltering.getFilter(TESTSUITE_DATA)));
		}

		if (columnFiltering.hasFilter(STATUS_DATA)) {
			String filter = columnFiltering.getFilter(STATUS_DATA);
			ExecutionStatus executionStatus = ExecutionStatus.valueOf(filter);
			query.setParameter(STATUS_FILTER, executionStatus);
		}

		if (columnFiltering.hasFilter(USER_DATA) && !"0".equals(columnFiltering.getFilter(USER_DATA))) {
			query.setParameter(USER_FILTER, Long.parseLong(columnFiltering.getFilter(USER_DATA)));
		}

		if (columnFiltering.hasFilter(TestPlanFilteringHelper.LASTEXEC_DATA)) {
			setQueryStartAndEndDateParameters(columnFiltering, query);
		}

	}

	private static void setQueryStartAndEndDateParameters(ColumnFiltering columnFiltering, Query query) {
		String dates = columnFiltering.getFilter(TestPlanFilteringHelper.LASTEXEC_DATA);
		Date startDate = null;
		Date endDate = null;

		if (dates.contains("-")) {
			String[] dateArray = dates.split("-");
			try {
				startDate = DateUtils.parseDdMmYyyyDate(dateArray[0].trim());
				endDate = DateUtils.parseDdMmYyyyDate(dateArray[1].trim());
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}

		} else {
			try {
				startDate = DateUtils.parseDdMmYyyyDate(dates.trim());
				endDate = DateUtils.nextDay(startDate);
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}

		}
		query.setParameter(TestPlanFilteringHelper.START_DATE, startDate);
		query.setParameter(TestPlanFilteringHelper.END_DATE, endDate);
	}

	public static void appendFilteringRestrictions(StringBuilder hqlbuilder, ColumnFiltering columnFiltering) {
		for (Entry<String, String> simpleFilterClause : SIMPLE_FILTER_CLAUSES.entrySet()) {
			String filterName = simpleFilterClause.getKey();
			String filterClause = simpleFilterClause.getValue();
			if (columnFiltering.hasFilter(filterName)) {
				hqlbuilder.append(filterClause);
			}
		}
	}

	public TestPlanFilteringHelper() {
		super();
	}
}

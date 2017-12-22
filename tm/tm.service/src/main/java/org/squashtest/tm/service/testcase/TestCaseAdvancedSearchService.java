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
package org.squashtest.tm.service.testcase;

import java.util.List;
import java.util.Locale;

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.AdvancedSearchService;

public interface TestCaseAdvancedSearchService extends AdvancedSearchService{


	//Querying

	PagedCollectionHolder<List<TestCase>> searchForTestCases(AdvancedSearchModel model, PagingAndMultiSorting sorting, Locale locale);

	PagedCollectionHolder<List<TestCase>> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model, PagingAndMultiSorting sorting, Locale locale);

	List<TestCase> searchForTestCases(AdvancedSearchModel model, Locale locale);

	List<TestCase> searchForTestCasesThroughRequirementModel(AdvancedSearchModel model, Locale locale);

	List<String> findAllUsersWhoModifiedTestCases(List<Long> idList);

	List<String> findAllUsersWhoCreatedTestCases(List<Long> idList);

}

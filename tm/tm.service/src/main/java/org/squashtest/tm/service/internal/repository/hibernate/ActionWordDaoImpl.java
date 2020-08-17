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

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.internal.repository.CustomActionWordDao;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.squashtest.tm.jooq.domain.Tables.ACTION_WORD;
import static org.squashtest.tm.jooq.domain.Tables.AUTOMATION_REQUEST;
import static org.squashtest.tm.jooq.domain.Tables.KEYWORD_TEST_STEP;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_STEPS;
import static org.squashtest.tm.jooq.domain.tables.KeywordTestCase.KEYWORD_TEST_CASE;

@Repository
@Transactional
public class ActionWordDaoImpl implements CustomActionWordDao {

	@Inject
	private DSLContext dsl;

	@Override
	public void updateActionWordImplInfoFromAutomRequestIds(Collection<Long> automationRequestIds) {
		Map<String, List<Long>> actionWordIdsMappedByImplTechno = findActionWordIdMappedByBddImplTechno(automationRequestIds);
		for(Map.Entry<String, List<Long>> entry : actionWordIdsMappedByImplTechno.entrySet()) {
			String implementationTechnology = entry.getKey();
			Collection<Long> actionWordIds = entry.getValue();
				updateActionWordsImplInfo(actionWordIds, implementationTechnology);
		}
	}

	private Map<String, List<Long>> findActionWordIdMappedByBddImplTechno(Collection<Long> automationRequestIds) {
		return dsl.selectDistinct(PROJECT.BDD_IMPLEMENTATION_TECHNOLOGY, ACTION_WORD.ACTION_WORD_ID)
			.from(AUTOMATION_REQUEST)
			.join(KEYWORD_TEST_CASE).on(KEYWORD_TEST_CASE.TCLN_ID.eq(AUTOMATION_REQUEST.TEST_CASE_ID))
			.join(TEST_CASE_STEPS).on(TEST_CASE_STEPS.TEST_CASE_ID.eq(KEYWORD_TEST_CASE.TCLN_ID))
			.join(KEYWORD_TEST_STEP).on(KEYWORD_TEST_STEP.TEST_STEP_ID.eq(TEST_CASE_STEPS.STEP_ID))
			.join(ACTION_WORD).on(ACTION_WORD.ACTION_WORD_ID.eq(KEYWORD_TEST_STEP.ACTION_WORD_ID))
			.join(PROJECT).on(PROJECT.PROJECT_ID.eq(ACTION_WORD.PROJECT_ID))
			.where(AUTOMATION_REQUEST.AUTOMATION_REQUEST_ID.in(automationRequestIds))
			.fetchGroups(PROJECT.BDD_IMPLEMENTATION_TECHNOLOGY, ACTION_WORD.ACTION_WORD_ID);
	}

	private void updateActionWordsImplInfo(Collection<Long> actionWordIds, String implemenationTechnology) {
		dsl.update(ACTION_WORD)
			.set(ACTION_WORD.LAST_IMPLEMENTATION_TECHNOLOGY, implemenationTechnology)
			.set(ACTION_WORD.LAST_IMPLEMENTATION_DATE, DSL.currentTimestamp())
			.where(ACTION_WORD.ACTION_WORD_ID.in(actionWordIds))
			.execute();
	}

}

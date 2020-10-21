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

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.project.QProject;
import org.squashtest.tm.domain.scm.QScmRepository;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.QKeywordTestCase;
import org.squashtest.tm.domain.testcase.QScriptedTestCase;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.CustomScmRepositoryDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static org.squashtest.tm.jooq.domain.Tables.THIRD_PARTY_SERVER;
import static org.squashtest.tm.jooq.domain.Tables.SCM_REPOSITORY;

public class ScmRepositoryDaoImpl implements CustomScmRepositoryDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmRepositoryDaoImpl.class);

	private static final String URL_SEPARATOR = "/";

	@PersistenceContext
	private EntityManager em;

	@Inject
	private DSLContext DSL;

	@Override
	public Map<ScmRepository, Set<TestCase>> findScriptedAndKeywordTestCasesGroupedByRepoById(Collection<Long> testCaseIds) {

		LOGGER.debug("looking for test cases and repositories which are corresponding to these test cases' projects to commit into");

		if (testCaseIds.isEmpty()){
			return Collections.emptyMap();
		}

		QTestCase testCase = QTestCase.testCase;
		QScriptedTestCase scriptedTestCase = QScriptedTestCase.scriptedTestCase;
		QKeywordTestCase keywordTestCase = QKeywordTestCase.keywordTestCase;
		QProject project = QProject.project1;
		QScmRepository scm = QScmRepository.scmRepository;


		return new JPAQueryFactory(em)
			.select(scm, testCase)
			.from(testCase)
			.join(testCase.project, project)
			.join(project.scmRepository, scm)
			.where(testCase.id.in(
				createQueryForValueFiltering(keywordTestCase, keywordTestCase.id, testCaseIds))
				.or(testCase.id.in(
					createQueryForValueFiltering(scriptedTestCase, scriptedTestCase.id, testCaseIds))
				)
			).transform(
				groupBy(scm).as(set(testCase))
			);

	}

	@Override
	public List<String> findDeclaredScmRepositoriesUrl() {
		List<String> repositoriesUrl = new ArrayList<>();

		DSL.select(THIRD_PARTY_SERVER.URL, SCM_REPOSITORY.NAME)
			.from(SCM_REPOSITORY)
			.innerJoin(THIRD_PARTY_SERVER).on(SCM_REPOSITORY.SERVER_ID.eq(THIRD_PARTY_SERVER.SERVER_ID))
			.fetch()
			.forEach(r -> {
				String scmUrl = r.get(THIRD_PARTY_SERVER.URL);
				String repoName = r.get(SCM_REPOSITORY.NAME);
				if(scmUrl.endsWith(URL_SEPARATOR)){
					repositoriesUrl.add(scmUrl.concat(repoName));
				} else {
					repositoriesUrl.add(scmUrl.concat(URL_SEPARATOR).concat(repoName));
				}
			});
		return repositoriesUrl;
	}

	/**
	 * This method is to: from a given id list, we do a filtration and get all ids that belong to a given Data Table.
	 * @param entityPathBase {@link EntityPathBase} represents the table in which the filtration is executed.
	 * @param entityPathBaseId {@link NumberPath} represents the ID column in which the filtration is executed.
	 * @param testCaseIds is all the ID to be filtered.
	 * @return a {@link JPAQuery<Long>}
	 */
	private <Y extends TestCase, T extends EntityPathBase<Y>> JPAQuery<Long> createQueryForValueFiltering(
		T entityPathBase, NumberPath<Long> entityPathBaseId, Collection<Long> testCaseIds) {
		return new JPAQueryFactory(em)
			.select(entityPathBaseId)
			.from(entityPathBase)
			.where(entityPathBaseId.in(testCaseIds));
	}
}

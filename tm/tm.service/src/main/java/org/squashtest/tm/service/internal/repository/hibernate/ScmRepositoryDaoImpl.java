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

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.project.QProject;
import org.squashtest.tm.domain.scm.QScmRepository;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.QScriptedTestCase;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.service.internal.repository.CustomScmRepositoryDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;

public class ScmRepositoryDaoImpl implements CustomScmRepositoryDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmRepositoryDaoImpl.class);

	@PersistenceContext
	private EntityManager em;

	@Override
	public Map<ScmRepository, Set<ScriptedTestCase>> findScriptedTestCasesGroupedByRepoById(Collection<Long> testCaseIds) {

			LOGGER.debug("looking for repositories and the test cases that should be committed into them");


			if (testCaseIds.isEmpty()){
				return Collections.emptyMap();
			}

			QScriptedTestCase scriptedTestCase = QScriptedTestCase.scriptedTestCase;
			QProject project = QProject.project1;
			QScmRepository scm = QScmRepository.scmRepository;


			return new JPAQueryFactory(em)
				.select(scm, scriptedTestCase)
				.from(scriptedTestCase)
				.join(scriptedTestCase.project, project)
				.join(project.scmRepository, scm)
				.fetchJoin()
				.where(scriptedTestCase.id.in(testCaseIds))
				.transform(
					groupBy(scm).as(set(scriptedTestCase))
				);

		}
}

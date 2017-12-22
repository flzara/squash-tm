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

import org.hibernate.Query;
import org.jooq.AggregateFunction;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.lang.PathUtils;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryNodeDao;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.groupConcat;
import static org.squashtest.tm.jooq.domain.Tables.*;

@Repository("squashtest.tm.repository.TestCaseLibraryNodeDao")
public class HibernateTestCaseLibraryNodeDao extends HibernateEntityDao<TestCaseLibraryNode> implements
TestCaseLibraryNodeDao {

	@Inject
	private DSLContext DSL;

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getParentsName(long entityId) {
		Query query = currentSession().getNamedQuery("TestCasePathEdge.findSortedParentNames");
		query.setParameter(ParameterNames.NODE_ID, entityId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getParentsIds(long entityId) {
		Query query = currentSession().getNamedQuery("TestCasePathEdge.findSortedParentIds");
		query.setParameter(ParameterNames.NODE_ID, entityId);
		return query.list();
	}

	@Override
	public List<TestCaseLibraryNode> findNodesByPath(List<String> path) {
		List<Long> ids = findNodeIdsByPath(path);
		List<TestCaseLibraryNode> result = findAllByIds(ids);

		// post process the result to ensure the correct order of the result
		TestCaseLibraryNode[] toReturn = new TestCaseLibraryNode[ids.size()];
		for (TestCaseLibraryNode node : result) {
			toReturn[ids.indexOf(node.getId())] = node;
		}

		return Arrays.asList(toReturn);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findNodeIdsByPath(List<String> paths) {
		//we will make a select with jooq, flushing session as hibernate would do if the request was made in hql
		entityManager.flush();
		if (!paths.isEmpty()) {
			// process the paths parameter : we don't want escaped '/' in there
			List<String> effectiveParameters = unescapeSlashes(paths);

			//get all the last node names
			List<String> tclnNames = paths.stream()
				.map(PathUtils::splitPath)//split path
				.map(Arrays::asList)//into a stream of List<String>
				//keeping only size > 1 ie more than just a project name
				.filter(pathParts -> pathParts.size() > 1)
				//want to keep only last element in names, ie the name of the last node in path
				.map(pathParts -> pathParts.get(pathParts.size()-1))
				.map(this::unescapeSlashes)
				.collect(Collectors.toList());

			if(tclnNames.isEmpty()){//avoiding mysql crash with empty list...
				return Collections.emptyList();
			}

			//now let's go for some SQL
			//the basic idea here is to concat all paths for descendants witch have a name in terminal list and only them
			//and thus comparing theses paths with our list in having clause
			org.squashtest.tm.jooq.domain.tables.TestCaseLibraryNode ancestor = TEST_CASE_LIBRARY_NODE.as("ancestor");
			org.squashtest.tm.jooq.domain.tables.TestCaseLibraryNode descendant = TEST_CASE_LIBRARY_NODE.as("descendant");
			AggregateFunction<String> groupConcatFunction = groupConcat(ancestor.NAME).orderBy(TCLN_RELATIONSHIP_CLOSURE.DEPTH.desc()).separator("/");
			Field<String> concatPath = concat(concat("/",PROJECT.NAME),concat("/", groupConcatFunction));

			Map<String, Long> idByPath = DSL.select(concatPath.as("path"), TCLN_RELATIONSHIP_CLOSURE.DESCENDANT_ID)
				.from(TCLN_RELATIONSHIP_CLOSURE)
				.innerJoin(ancestor).on(TCLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.eq(ancestor.TCLN_ID))
				.innerJoin(PROJECT).on(ancestor.PROJECT_ID.eq(PROJECT.PROJECT_ID))
				.innerJoin(descendant).on(TCLN_RELATIONSHIP_CLOSURE.DESCENDANT_ID.eq(descendant.TCLN_ID))
				.where(descendant.NAME.in(tclnNames))
				.groupBy(TCLN_RELATIONSHIP_CLOSURE.DESCENDANT_ID, PROJECT.NAME)
				.having(concatPath.in(effectiveParameters))
				.fetch()
				.stream()
				.collect(Collectors.toMap(r -> r.get("path", String.class), r -> r.get(TCLN_RELATIONSHIP_CLOSURE.DESCENDANT_ID)));

			//this should preserve order of initial list
			return effectiveParameters.stream()
				.map(idByPath::get)
				.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.internal.repository.LibraryNodeDao#findNodeIdByPath(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Long findNodeIdByPath(String path) {
		return findNodeIdsByPath(Arrays.asList(path)).get(0);

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.internal.repository.LibraryNodeDao#findNodeByPath(java.lang.String)
	 */
	@Override
	public TestCaseLibraryNode findNodeByPath(String path) {
		Long id = findNodeIdByPath(path);

		return (TestCaseLibraryNode) (id != null ? currentSession().load(TestCaseLibraryNode.class, id) : null);
	}


	@Override
	public int countSiblingsOfNode(long nodeId) {

		Query q;
		Integer count;

		q = currentSession().getNamedQuery("testCase.countSiblingsInFolder");
		q.setParameter(ParameterNames.NODE_ID, nodeId);
		count = (Integer)q.uniqueResult();

		if (count == null ){
			q = currentSession().getNamedQuery("testCase.countSiblingsInLibrary");
			q.setParameter(ParameterNames.NODE_ID, nodeId);
			count = (Integer)q.uniqueResult();
		}

		// if NPE here it's probably because nodeId corresponds to nothing. The +1 is because the queries use 'maxindex' instead of 'count'
		return count + 1;
	}



	private List<String> unescapeSlashes(List<String> paths) {
		List<String> unescaped = new ArrayList<>(paths.size());
		for (String orig : paths) {
			unescaped.add(orig.replaceAll("\\\\/", "/"));
		}
		return unescaped;
	}

	private String unescapeSlashes(String path) {
		return path.replaceAll("\\\\/", "/");
	}

}

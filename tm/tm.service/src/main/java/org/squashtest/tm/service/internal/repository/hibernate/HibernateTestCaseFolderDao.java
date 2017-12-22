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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;

@Repository
public class HibernateTestCaseFolderDao extends HibernateEntityDao<TestCaseFolder> implements TestCaseFolderDao {
	@Override
	public List<TestCaseLibraryNode> findAllContentById(final long folderId) {
		SetQueryParametersCallback setParams = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setLong("folderId", folderId);
			}
		};

		return executeListNamedQuery("testCaseFolder.findAllContentById", setParams);

	}

	@Override
	public TestCaseFolder findByContent(final TestCaseLibraryNode node) {
		SetQueryParametersCallback callback = new SetNodeContentParameter(node);
		return executeEntityNamedQuery("testCaseFolder.findByContent", callback);
	}

	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(folderId, nameStart);
		return executeListNamedQuery("testCaseFolder.findNamesInFolderStartingWith", newCallBack1);
	}

	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(libraryId, nameStart);
		return executeListNamedQuery("testCaseFolder.findNamesInLibraryStartingWith", newCallBack1);
	}

	@Override
	public List<Long> findTestCasesFolderIdsInFolderContent(final long folderId) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("folderId", folderId);
			}
		};
		return executeListNamedQuery("testCaseFolder.findTestCasesFolderIdsInFolderContent", newCallBack1);
	}

	@Override
	public List<Long[]> findPairedContentForList(final List<Long> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}

		SQLQuery query = currentSession().createSQLQuery(
				NativeQueries.TEST_CASE_FOLDER_SQL_FIND_PAIRED_CONTENT_FOR_FOLDERS);
		query.setParameterList("folderIds", ids, LongType.INSTANCE);
		query.addScalar("ancestor_id", LongType.INSTANCE);
		query.addScalar("descendant_id", LongType.INSTANCE);

		List<Object[]> result = query.list();

		return toArrayOfLong(result);
	}

	@Override
	public List<Long> findContentForList(List<Long> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}

		SQLQuery query = currentSession().createSQLQuery(NativeQueries.TEST_CASE_FOLDER_SQL_FIND_CONTENT_FOR_FOLDER);
		query.setParameterList("folderIds", ids, LongType.INSTANCE);
		query.addScalar("descendant_id", LongType.INSTANCE);

		return query.list();
	}


	private List<Long[]> toArrayOfLong(List<Object[]> input) {
		List<Long[]> result = new ArrayList<>();

		for (Object[] pair : input) {
			Long[] newPair = new Long[] { (Long) pair[0], (Long) pair[1] };
			result.add(newPair);
		}

		return result;
	}

	@Override
	public TestCaseFolder findParentOf(final Long id) {
		SetQueryParametersCallback newCallBack = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("contentId", id, LongType.INSTANCE);
			}
		};
		return executeEntityNamedQuery("testCaseFolder.findParentOf", newCallBack);
	}

}

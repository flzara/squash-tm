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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.internal.repository.hibernate.TestCaseDaoImpl;

/**
 * DAO for computing nodes paths. Factored out of {@link TestCaseDaoImpl}
 *
 * @author Gregory Fouquet
 *
 *
 */
@Service
@Transactional(readOnly = true)
public class HibernatePathService implements PathService {
	/**
	 * The PATH_SEPARATOR is not '/' because we couldn't distinguish with slashes guenuinely part of
	 * a name. Of course to disambiguate we could have used MySQL / H2 function replace(targetstr, orig, replace)
	 * and escape the '/' but the functions don't work the same way on both database and what works in one
	 * doesn't work on the other.
	 *
	 * So the separator is not / but some other improbable character, that I hope
	 * improbable enough in the context of a normal use of Squash.
	 * Currently it's the ASCII character "US", or "Unit separator", aka "Information separator one",
	 * that was precisely intended for similar purpose back in the prehistoric era.
	 *
	 * It's up to the caller to then post process the chain and replace that character
	 * by anything it sees fit.
	 **/
	public static final String PATH_SEPARATOR = "\u001F";

	@PersistenceContext
	private EntityManager em;

	private Session currentSession() {
		return em.unwrap(Session.class);
	}

	/**
	 * @see org.squashtest.tm.service.internal.library.PathService#buildTestCasePath(long)
	 */
	@Override
	public String buildTestCasePath(long id) {
		return buildPath("TestCasePathEdge.findPathById", id);
	}

	/**
	 * @see org.squashtest.tm.service.internal.library.PathService#buildTestCasesPaths(java.util.List)
	 */
	@Override
	public List<String> buildTestCasesPaths(List<Long> ids) {
		return buildAllPaths("TestCasePathEdge.findPathsByIds", ids);
	}


	@Override
	public String buildRequirementPath(long id) {
		return buildPath("RequirementPathEdge.findPathById", id);
	}


	@Override
	public List<String> buildRequirementsPaths(List<Long> ids) {
		return buildAllPaths("RequirementPathEdge.findPathsByIds", ids);
	}

	@Override
	public String buildCampaignPath(long id) {
		return buildPath("CampaignPathEdge.findPathById", id);
	}

	@Override
	public List<String> buildCampaignPaths(List<Long> ids) {
		return buildAllPaths("CampaignPathEdge.findPathsByIds", ids);
	}


	/**
	 * @param string
	 * @return
	 */
	public static String escapePath(String fetchedPath) {
		return fetchedPath != null ? fetchedPath.replace("/", "\\/").replace(PATH_SEPARATOR, "/") : null;
	}



	// ************************* private methods *************************************

	private String buildPath(String queryname, long id) {
		List<String> paths = findPathById(queryname, id);

		if (paths.isEmpty()) {
			return null;
		}

		return escapePath(paths.get(0));
	}


	private List<String> buildAllPaths(String queryname, List<Long> ids) {

		// the DB dies if you query with an empty list argument
		if (ids.isEmpty()){
			return new ArrayList<>();
		}
		List<Object[]> paths = findPathsByIds(queryname, ids);

		String[] res = new String[ids.size()];

		for (Object[] path : paths) {

			int pos = ids.indexOf(path[0]);
			res[pos] = escapePath((String) path[1]);
		}

		return Arrays.asList(res);
	}


	@SuppressWarnings("unchecked")
	private List<String> findPathById(String queryname, long id) {
		Query query = currentSession().getNamedQuery(queryname);
		query.setParameter("nodeId", id);
		return query.list();
	}

	/**
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Object[]> findPathsByIds(String queryname, List<Long> ids) {
		Query query = currentSession().getNamedQuery(queryname/*"TestCasePathEdge.findPathsByIds"*/);
		query.setParameterList("nodeIds", ids);
		return query.list();
	}



}

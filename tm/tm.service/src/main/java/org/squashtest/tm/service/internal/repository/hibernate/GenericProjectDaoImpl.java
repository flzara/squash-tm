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

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.service.internal.repository.CustomGenericProjectDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

/**
 * @author Gregory Fouquet
 *
 */

public class GenericProjectDaoImpl implements CustomGenericProjectDao {
	@PersistenceContext
	private EntityManager em;

	/**
	 * @return the coerced project
	 * @see org.squashtest.tm.service.internal.repository.CustomGenericProjectDao#coerceProjectIntoTemplate(long) (long)
	 */
	@Override
	public ProjectTemplate coerceProjectIntoTemplate(long projectId) {
		Session session = getCurrentSession();

		Project project = (Project)session.load(Project.class, projectId);
		session.flush();
		session.evict(project);

		SQLQuery query = session.createSQLQuery("update PROJECT set PROJECT_TYPE = 'T',  TEMPLATE_ID = null where PROJECT_ID = :id");
		query.setParameter("id", projectId);
		final int changedRows = query.executeUpdate();
		if (changedRows != 1) {
			throw new HibernateException("Expected 1 changed row but got " + changedRows + " instead");
		}
		session.flush();

		return (ProjectTemplate) session.load(ProjectTemplate.class, projectId);
	}

	@Override
	public boolean isProjectTemplate(long projectId) {

		Query query = em.createNamedQuery("GenericProject.findProjectTypeOf");
		query.setParameter(ParameterNames.PROJECT_ID, projectId);

		String type = (String) query.getSingleResult();

		return "T".equals(type);
	}

	@Override
	public boolean isBoundToATemplate(long genericProjectId) {
		Query query = em.createNamedQuery("GenericProject.findBoundTemplateId");
		query.setParameter(ParameterNames.PROJECT_ID, genericProjectId);
		List<Long> templateIdss = query.getResultList();
		return !templateIdss.isEmpty();
	}

	@Override
	public boolean oneIsBoundToABoundProject(Collection<Long> bindingIds) {
		Query query = em.createNamedQuery("GenericProject.findBoundTemplateIdsFromBindingIds");
		query.setParameter("bindingIds", bindingIds);
		List<Long> templateIds = query.getResultList();
		return !templateIds.isEmpty();
	}

	/**
	 *
	 * @see org.squashtest.tm.service.internal.repository.CustomGenericProjectDao#findAllWithTextProperty(java.lang.Class,
	 *      org.squashtest.tm.core.foundation.collection.Filtering)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends GenericProject> List<T> findAllWithTextProperty(Class<T> entity, Filtering filtering) {

		Criteria allEntities = getCurrentSession().createCriteria(entity);

		if (filtering.isDefined() && StringUtils.isNotEmpty(filtering.getFilter())) {
			final String ex = filtering.getFilter();
			final String[] textProps = { "name", "label", "audit.createdBy", "audit.lastModifiedBy" };
			Disjunction orPropsLikeFilter = Restrictions.disjunction();

			for (String prop : textProps) {
				orPropsLikeFilter.add(Restrictions.ilike(prop, ex, MatchMode.ANYWHERE));
			}

			allEntities.add(orPropsLikeFilter);
		}

		return allEntities.list();
	}

	private Session getCurrentSession() {
		return em.unwrap(Session.class);
	}
}

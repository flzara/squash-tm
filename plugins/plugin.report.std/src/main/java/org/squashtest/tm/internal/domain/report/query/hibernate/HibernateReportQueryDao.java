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
package org.squashtest.tm.internal.domain.report.query.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.internal.domain.report.query.ReportQuery;
import org.squashtest.tm.internal.domain.report.query.ReportQueryFlavor;
import org.squashtest.tm.internal.domain.report.query.UnsupportedFlavorException;
import org.squashtest.tm.internal.repository.ReportQueryDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class HibernateReportQueryDao implements ReportQueryDao {

	@PersistenceContext
	private EntityManager entityManager;

	private Session currentSession() {
		return entityManager.unwrap(Session.class);
	}

	private final ReportQueryFlavor flavor = new HibernateQueryFlavor();

	@Override
	public boolean doesSupportFlavor(ReportQueryFlavor flavor) {
		return this.flavor.getClass() == flavor.getClass();
	}

	@Override
	public List<?> executeQuery(ReportQuery query) throws UnsupportedFlavorException {
		if (!doesSupportFlavor(query.getFlavor())) {
			throw new UnsupportedFlavorException("Error : ReportQueryDao implementation does not support queries of class " + query.getClass().getName());
		}
		HibernateReportQuery hibQuery = (HibernateReportQuery) query;

		DetachedCriteria dCriteria = hibQuery.createHibernateQuery();

		List<?> result;

		if (dCriteria != null) {

			result = executeDetachedCriteria(dCriteria);

		} else {

			result = hibQuery.doInSession(currentSession());

		}


		return hibQuery.convertToDto(result);

	}

	private List<?> executeDetachedCriteria(DetachedCriteria dCriteria) {

		Session session = currentSession();
		Criteria criteria = dCriteria.getExecutableCriteria(session);
		return criteria.list();
	}

	@Override
	public ReportQueryFlavor[] getSupportedFlavors() {
		return new ReportQueryFlavor[]{flavor};
	}

}

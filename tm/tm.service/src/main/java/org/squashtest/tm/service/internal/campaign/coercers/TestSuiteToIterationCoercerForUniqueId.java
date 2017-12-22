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
package org.squashtest.tm.service.internal.campaign.coercers;

import java.io.Serializable;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.service.annotation.IdCoercer;
import org.squashtest.tm.service.internal.hibernate.HibernateStatelessSessionHelper;

/**
 * @author Julien Thebault
 * @since 1.13
 */
@Configurable
public class TestSuiteToIterationCoercerForUniqueId implements IdCoercer {

	@Inject
	private HibernateStatelessSessionHelper hibernateStatelessSessionHelper;

	@Override
	public Serializable coerce(Object id) {
		StatelessSession s = hibernateStatelessSessionHelper.openStatelessSession();
		Transaction tx = s.beginTransaction();

		try {
			Query q = s.createSQLQuery("SELECT DISTINCT iteration_id FROM iteration_test_suite WHERE test_suite_id = :suiteId");
			q.setParameter("suiteId", id);
			return (Serializable) q.uniqueResult();

		} finally {
			tx.commit();
			s.close();
		}
	}

}

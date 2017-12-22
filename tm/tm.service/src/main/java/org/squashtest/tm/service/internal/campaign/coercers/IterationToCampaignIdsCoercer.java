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
import java.util.Collection;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.service.annotation.IdsCoercer;
import org.squashtest.tm.service.internal.hibernate.HibernateStatelessSessionHelper;

/**
 * @author Gregory Fouquet
 * @since 1.11.6
 */
@Configurable
public class IterationToCampaignIdsCoercer implements IdsCoercer {

	@Inject
	private HibernateStatelessSessionHelper hibernateStatelessSessionHelper;

	@Override
	public Collection<? extends Serializable> coerce(Object ids) {
		StatelessSession s = hibernateStatelessSessionHelper.openStatelessSession();
		Transaction tx = s.beginTransaction();

		try {
			Query q = s.createQuery("select distinct c.id from Iteration i join i.campaign c where i.id in (:iterIds)");
			q.setParameterList("iterIds", (Collection<? extends Serializable>) ids);
			return q.list();

		} finally {
			tx.commit();
			s.close();
		}
	}

}

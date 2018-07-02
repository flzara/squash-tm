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
package org.squashtest.tm.service.internal.requirement.coercers.extenders;

import java.io.Serializable;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Configurable;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.IdsCoercerExtender;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.internal.hibernate.HibernateStatelessSessionHelper;

/**
 * Extender used for {@link RequirementLibraryNode} move operations. This class is used with {@link PreventConcurrents} and {@link BatchPreventConcurrent} annotations.
 *
 * Will give the ids of the libraries that we need to lock when we move the {@link TestCaseLibraryNode}.
 * Each library witch content can be changed by the operation must be locked to prevent weird concurrency results.
 *
 * @author Julien Thebault
 * @since 1.13
 */
@Configurable
@Named("requirementLibraryExtender")
public class RequirementLibraryExtender implements IdsCoercerExtender {

	@Inject
	private HibernateStatelessSessionHelper hibernateStatelessSessionHelper;

	@Override
	public Collection<? extends Serializable> doCoerce (Collection<? extends Serializable>  ids) {
		StatelessSession s = hibernateStatelessSessionHelper.openStatelessSession();
		Transaction tx = s.beginTransaction();

		try {
			Query q = s.createQuery("select distinct l.id from RequirementLibrary l join l.rootContent c where c.id in (:rlnIds) ");
			q.setParameterList("rlnIds", ids);
			return q.list();

		} finally {
			tx.commit();
			s.close();
		}
	}
}

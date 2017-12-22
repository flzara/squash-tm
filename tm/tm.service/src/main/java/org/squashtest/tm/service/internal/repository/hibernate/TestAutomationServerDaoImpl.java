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


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Session;
import org.squashtest.tm.service.internal.repository.CustomTestAutomationServerDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

public class TestAutomationServerDaoImpl implements CustomTestAutomationServerDao{

	@PersistenceContext
	private EntityManager em;



	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationServerDao#hasBoundProjects(long)
	 */
        @Override
	public boolean hasBoundProjects(long serverId) {
		Query q = em.createNamedQuery("testAutomationServer.hasBoundProjects");
		q.setParameter(ParameterNames.SERVER_ID, serverId);
		Long count = (Long) q.getSingleResult();
		return count > 0;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.TestAutomationServerDao#deleteServer(long)
	 */
        @Override
	public void deleteServer(long serverId) {
		dereferenceProjects(serverId);
		em.flush();
		deleteServerById(serverId);
		em.flush();
	}

	// ***************** private stuffs ***************

	private void dereferenceProjects(long serverId) {
		Query q = em.createNamedQuery("testAutomationServer.dereferenceProjects");
		q.setParameter(ParameterNames.SERVER_ID, serverId);
		q.executeUpdate();

	}

	private void deleteServerById(long serverId) {
		Query q = em.createNamedQuery("testAutomationServer.deleteServer");
		q.setParameter(ParameterNames.SERVER_ID, serverId);
		q.executeUpdate();
	}

}

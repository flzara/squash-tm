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
package org.squashtest.tm.service.internal.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

/**
 * Helper class used to factorise the unwrapping of HibernateSessionFactory and
 * the creation of a stateless session.
 * Created by jthebault on 11/04/2016.
 */
@Component
public class HibernateStatelessSessionHelper {

	@Inject
	EntityManagerFactory entityManagerFactory;
	
	public HibernateStatelessSessionHelper() {
		super();
	}

	/**
	 * Create a new Hibernate stateless session, by unwrapping the HibernateSessionFactory from EntityManagerFactory and return the session.
	 * Don't forget to close it after usage.
	 * @return
     */
	public StatelessSession openStatelessSession(){
		SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		return sessionFactory.openStatelessSession();
	}
}

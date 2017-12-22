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
package test.hibernate.hql

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.squashtest.tm.tools.unittest.hibernate.HibernateOperationCategory;

import spock.lang.Specification;

class HqlSpecification extends Specification {
	Configuration config = new Configuration()
	SessionFactory sf

	def setup() {
		config.configure("hql-hibernate.cfg.xml")
		config.addAnnotatedClass Owner
		config.addAnnotatedClass MultiOwned
		config.addAnnotatedClass SingleOwned

		sf = config.buildSessionFactory()
	}

	def ""() {
		given:
		Owner o = new Owner()
		MultiOwned mo = new MultiOwned()
		o.multiOwneds << mo

		use (HibernateOperationCategory) {
			sf.doInSession {
				it.persist mo
				it.persist o
			}
		}

		when:

		def read

		use (HibernateOperationCategory) {
			read = sf.doInSession {
				def q = it.createQuery("select o from Owner o where :mo in elements(o.multiOwneds)")
				q.setParameter("mo", mo)
				q.uniqueResult()
			}
		}

		then:
		read.id == o.id
	}
}

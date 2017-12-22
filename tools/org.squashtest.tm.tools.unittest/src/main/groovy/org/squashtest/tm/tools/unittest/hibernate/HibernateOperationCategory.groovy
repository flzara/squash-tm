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
package org.squashtest.tm.tools.unittest.hibernate

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

class HibernateOperationCategory {
	static def doInSession(SessionFactory sf, def closure) {
		Session s = sf.openSession()
		Transaction tx = s.beginTransaction()
		def res
		
		try {
			res = closure(s)
			s.flush()
			tx.commit()
		} catch (RuntimeException ex) {
			tx?.rollback()
			throw ex
		} finally {
			s?.close()
		}
		
		return res
	}
}

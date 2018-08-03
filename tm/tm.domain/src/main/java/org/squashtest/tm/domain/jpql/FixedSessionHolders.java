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
package org.squashtest.tm.domain.jpql;

import com.querydsl.jpa.hibernate.NoSessionHolder;
import com.querydsl.jpa.hibernate.SessionHolder;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

/**
 * Implementations of {@link com.querydsl.jpa.hibernate.SessionHolder} that wont crash due to
 * https://github.com/querydsl/querydsl/issues/1917. It works basically because it will compile against
 * Hibernate 5.2, thus the linkage at runtime won't fail as indicated above.
 *
 * This workaround should not be necessary in the future once Hibernate and QueryDsl are in agreement again.
 *
 */
class FixedSessionHolders {

	private FixedSessionHolders(){}

	public static SessionHolder defaultSessionHolder(Session session){
		return new FixedDefaultSessionHolder(session);
	}

	public static SessionHolder statelessSessionHolder(StatelessSession session){
		return new FixedStatelessSessionHolder(session);
	}

	public static SessionHolder noSessionHolder(){
		return NoSessionHolder.DEFAULT;
	}


	private static final class FixedDefaultSessionHolder implements SessionHolder{
		private final Session session;

		public FixedDefaultSessionHolder(Session session) {
			this.session = session;
		}

		@Override
		public Query createQuery(String queryString) {
			return session.createQuery(queryString);
		}

		@Override
		public SQLQuery createSQLQuery(String queryString) {
			return session.createSQLQuery(queryString);
		}

	}

	private static final class FixedStatelessSessionHolder implements SessionHolder {

		private final StatelessSession session;

		public FixedStatelessSessionHolder(StatelessSession session) {
			this.session = session;
		}

		@Override
		public Query createQuery(String queryString) {
			return session.createQuery(queryString);
		}

		@Override
		public SQLQuery createSQLQuery(String queryString) {
			return session.createSQLQuery(queryString);
		}

	}

}

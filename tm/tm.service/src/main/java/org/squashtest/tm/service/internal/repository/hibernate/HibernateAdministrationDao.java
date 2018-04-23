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

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.AdministrationStatistics;
import org.squashtest.tm.service.internal.repository.AdministrationDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * Dao for no specific workspace.
 *
 * @author mpagnon
 *
 */
@Repository
public class HibernateAdministrationDao implements AdministrationDao {

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	private DataSourceProperties dataSourceProperties;

	private static final String DATABASE_LABEL_MYSQL = "mysql";
	private static final String DATABASE_LABEL_POSTGRESQL = "postgresql";
	private static final String REQ_DATABASE_SIZE_MYSQL = "select sum((data_length + index_length) / 1024 / 1024) from information_schema.tables where table_schema = database()";
	private static final String REQ_DATABASE_SIZE_POSTGRESQL = "select pg_database_size(current_database()) / 1024 / 1024";


	@Override
	public AdministrationStatistics findAdministrationStatistics() {
		Object[] result = (Object[]) entityManager.createNamedQuery("administration.findAdministrationStatistics").getSingleResult();

		// Feat 6855 - Adding the database size in the statistics panel. The req is different for MySQL and PostgreSQL
		// At first, we retrieve the url which contains the database type and the database name.
		// Then, we execute a request to retrieve the database size (in Mo), we return 0 for another database type than MySQL or PostgreSQL
		BigInteger databaseSize = BigInteger.ZERO;
		String url = dataSourceProperties.getUrl();
		if (url.contains(DATABASE_LABEL_MYSQL) || url.contains(DATABASE_LABEL_POSTGRESQL)) {
			if (url.contains(DATABASE_LABEL_MYSQL)) {
				databaseSize = getDatabaseSizeForMysql();
			}
			if (url.contains(DATABASE_LABEL_POSTGRESQL)) {
				databaseSize = getDatabaseSizeForPostgresql();
			}
		}
		return new AdministrationStatistics(result, databaseSize);
	}

	// SQL query retrieving database size for MySQL. The query's result is a BigDecimal, we convert it into a BigInteger.
	private BigInteger getDatabaseSizeForMysql() {
		return ((BigDecimal) entityManager.createNativeQuery(REQ_DATABASE_SIZE_MYSQL).getSingleResult()).toBigInteger();
	}

	// SQL query retrieving database size for PostgreSQL
	private BigInteger getDatabaseSizeForPostgresql() {
		return (BigInteger) entityManager.createNativeQuery(REQ_DATABASE_SIZE_POSTGRESQL).getSingleResult();
	}

}

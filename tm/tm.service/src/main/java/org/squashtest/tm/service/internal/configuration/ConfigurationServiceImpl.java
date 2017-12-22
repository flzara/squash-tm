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
package org.squashtest.tm.service.internal.configuration;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.HibernateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.configuration.ConfigurationService;

@Service("squashtest.core.configuration.ConfigurationService")
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

	// TODO make these named queries
	private static final String INSERT_KEY_SQL = "insert into CORE_CONFIG (STR_KEY, VALUE) values (?1, ?2)";
	private static final String FIND_VALUE_BY_KEY_SQL = "select VALUE from CORE_CONFIG where STR_KEY = ?1";
	private static final String UPDATE_KEY_SQL = "update CORE_CONFIG set VALUE = ?1 where STR_KEY = ?2";

	@PersistenceContext
	private EntityManager em;

	@Override
	public void createNewConfiguration(String key, String value) {
		Query sqlQuery = em.createNativeQuery(INSERT_KEY_SQL);
		sqlQuery.setParameter(1, key);
		sqlQuery.setParameter(2, value);
		sqlQuery.executeUpdate();
	}

	@Override
	public void updateConfiguration(String key, String value) {
		Query sqlQuery = em.createNativeQuery(UPDATE_KEY_SQL);
		sqlQuery.setParameter(1, value);
		sqlQuery.setParameter(2, key);
		sqlQuery.executeUpdate();

	}

	@Override
	@Transactional(readOnly = true)
	public String findConfiguration(String key) {
		Object value = findValue(key);
		return value == null ? null : value.toString();
	}

	private Object findValue(String key) throws HibernateException {
		Query sqlQuery = em.createNativeQuery(FIND_VALUE_BY_KEY_SQL);
		sqlQuery.setParameter(1, key);
		try {
			return sqlQuery.getSingleResult();
		} catch (NoResultException e) {//NOSONAR we will not log or cast each time an optional prop isn't in db
			return null;
		}
	}

	/**
	 * As per interface spec, when stored value is "true" (ignoring case), this returns <code>true</code>, otherwise it
	 * returns <code>false</code>
	 *
	 * @see org.squashtest.tm.service.configuration.ConfigurationService#getBoolean(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(findConfiguration(key));
	}

	/**
	 * @see org.squashtest.tm.service.configuration.ConfigurationService#set(java.lang.String, boolean)
	 */
	@Override
	public void set(String key, boolean value) {
		String strVal = Boolean.toString(value);
		set(key, strVal);
	}

	/**
	 *
	 * @see org.squashtest.tm.service.configuration.ConfigurationService#set(java.lang.String, java.lang.String)
	 */
	@Override
	public void set(String key, String value) {
		if (findValue(key) == null) {
			createNewConfiguration(key, value);
		} else {
			updateConfiguration(key, value);
		}
	}

}

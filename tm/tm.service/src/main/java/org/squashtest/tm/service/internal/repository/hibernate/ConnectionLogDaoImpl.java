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


import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomConnectionLogDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * @author aguilhem
 */
public class ConnectionLogDaoImpl implements CustomConnectionLogDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionLogDaoImpl.class);

	private static final String CONNECTION_DATE_DATA = "connection-date";
	private static final String LOGIN_DATA = "login";
	private static final String CONNECTION_DATE_COLUMN = "connectionDate";


	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<ConnectionLog> findSortedConnections(PagingAndSorting paging, ColumnFiltering columnFiltering) {

		Session session = entityManager.unwrap(Session.class);

		Criteria criteria = session.createCriteria(ConnectionLog.class, "ConnectionLog");

		if(columnFiltering.isDefined()){
			String login = columnFiltering.getFilter(LOGIN_DATA);
			String dates = columnFiltering.getFilter(CONNECTION_DATE_DATA);
			if(!login.isEmpty()){
				criteria.add(Restrictions.like(LOGIN_DATA, login, MatchMode.ANYWHERE));
			}
			if(!dates.isEmpty()){
				setQueryStartAndEndDateParameterQuery(dates, criteria);
			}

		}

		SortingUtils.addOrder(criteria, paging);

		PagingUtils.addPaging(criteria, paging);

		return criteria.list();

	}

	@Override
	public List<ConnectionLog> findFilteredConnections(ColumnFiltering columnFiltering) {
		DefaultPagingAndSorting sorting = new DefaultPagingAndSorting();
		sorting.setShouldDisplayAll(true);
		return findSortedConnections(sorting, columnFiltering);
	}

	private void setQueryStartAndEndDateParameterQuery(String dates, Criteria criteria){
		Date startDate = null;
		Date endDate = null;


		if (dates.contains("-")) {
			String[] dateArray = dates.split("-");
			try {
				startDate = DateUtils.parseDdMmYyyyDate(dateArray[0].trim());
				endDate = DateUtils.parseDdMmYyyyDate(dateArray[1].trim());
				endDate = Date.from(endDate.toInstant().plus(1, ChronoUnit.DAYS));
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}

		} else {
			try {
				startDate = DateUtils.parseDdMmYyyyDate(dates.trim());
				endDate = DateUtils.nextDay(startDate);
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}

		}
		criteria.add(Restrictions.between(CONNECTION_DATE_COLUMN, startDate, endDate));
	}
}

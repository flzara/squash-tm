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
package org.squashtest.tm.internal.domain.report.query.hibernate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.squashtest.tm.internal.domain.report.query.ReportQuery;
import org.squashtest.tm.internal.domain.report.query.ReportQueryFlavor;
import org.squashtest.tm.plugin.report.std.service.DataFilteringService;

/**
 * This class is an implementation of ReportQuery meant for a Hibernate repository.
 *
 * The goal of that Hibernate-oriented ReportQuery is to provide the Dao executing it with a DetachedCriteria query.
 * Note that an HibernateReportQuery will return a HibernateQueryFlavor by design.
 *
 * That abstract class is a superclass for other queries. Note that this abstract class extends the ReportQuery
 * interface by far, including a post processing method {@see HibernateReportQuery#convertToDto(java.util.List)}. The
 * corresponding ReportQueryDao, namely {@see HibernateReportQueryDao} is aware of that method and will invoke it.
 *
 * That method is useful for any post processing like the use of a DataFilteringService {@see DataFilteringService},
 * {@see ReportQuery} or complex computations that would have been difficult to get directly from the repository.
 *
 *
 * For convenience this implementation uses a class to explicitly design the criteria for that query, the
 * ReportCriterion. A ReportCriterion is meant to hold informations and possibly generate a Hibernate Criterion that
 * will be used in the main DetachedCriteria query, but it's up to the implementor to use it or rebuild the Criterion
 * from scratch.
 *
 * Subclassing a HibernateReportQuery : ========================================
 *
 * - The constructor must create the needed ReportCriterion and add then to the list of ReportCriterions.
 *
 * - createHibernateQuery() may generate a DetachedCriteria, or return null.
 *
 * - doInSession(Session session) will be executed if createHibernateQuery() returned null.
 *
 * - convertToDto() accepting a list of Hibernate entities and returning a list of different objects that fits the
 * definition of the view (probably a .jasper). Trick : if you couldn't translate your criterion into Hibernate
 * Criterion when implementing the createHibernateQuery() interface, you can postprocess the results with the remaining
 * criteria in this step.
 *
 * @author bsiri
 *
 */

public abstract class HibernateReportQuery implements ReportQuery {

	private final ReportQueryFlavor flavor = new HibernateQueryFlavor();

	protected Map<String, ReportCriterion> criterions = new HashMap<>();

	private DataFilteringService filterService;

	@Override
	public ReportQueryFlavor getFlavor() {
		return flavor;
	}

	@Override
	public void setCriterion(String name, Object... values) {
		ReportCriterion criterion = criterions.get(name);
		if (criterion != null) {
			criterion.setParameter(values);
		} else {
			throw new IllegalArgumentException("parameter " + name + " does not exists for query "
					+ this.getClass().getSimpleName());
		}
	}

	@Override
	public void setDataFilteringService(DataFilteringService service) {
		this.filterService = service;
	}

	protected DataFilteringService getDataFilteringService() {
		return filterService;
	}

	/**
	 * For internal use of the subclasses, this will return the concrete implementation of the criterions holder.
	 *
	 * @return the map of ReportCriterion, identified by their names (the name does exists both in the Map and in the
	 *         ReportCriterion).
	 */
	protected Map<String, ReportCriterion> getCriterions() {
		return criterions;
	}

	@Override
	public Collection<String> getCriterionNames() {
		return criterions.keySet();
	}

	@Override
	public boolean isCriterionExists(String name) {
		return criterions.get(name) != null;
	}

	@Override
	public Object[] getValue(String key) {
		ReportCriterion criterion = criterions.get(key);
		if (criterion != null) {
			return criterion.getParameters();
		} else {
			return null;
		}
	}

	/**
	 * Short hand for including a Hibernate Criterion in a DetachedCriteria.
	 *
	 * @param criteria
	 *            the DetachedCriteria query to which we add a Criterion.
	 * @param criterionName
	 *            the name of the corresponding ReportCriterion.
	 * @return
	 */
	protected DetachedCriteria addCriterion(DetachedCriteria criteria, String criterionName) {
		ReportCriterion criterion = criterions.get(criterionName);
		if (criterion != null) {
			Criterion hibCriterion = criterion.makeCriterion();
			if (hibCriterion != null) {
				criteria.add(hibCriterion);
			}
		}
		return criteria;
	}

	/**
	 * @return a DetachedCriteria to run in a Hibernate Dao, or null.
	 */
	public abstract DetachedCriteria createHibernateQuery();

	/**
	 * if you really need it. Will be executed if createHibernateQuery() returned null;
	 *
	 * @param session
	 * @return
	 */
	public abstract List<?> doInSession(Session session);

	/**
	 * This method will convert the raw results from Hibernate into a suitable list of Dto object that the view will
	 * process in turn. Should also use the DataFilteringService if need be.
	 *
	 * @param rawData
	 *            a List of Hibernate entities.
	 * @return a List of Dtos.
	 */
	public abstract List<?> convertToDto(List<?> rawData);

}

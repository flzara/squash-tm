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
package org.squashtest.tm.plugin.report.std.query;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.query.ReportQuery;
import org.squashtest.tm.internal.domain.report.common.dto.HasMilestoneLabel;
import org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery;
import org.squashtest.tm.plugin.report.std.service.ReportService;

/**
 * Superclass of legacy query adapters
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class LegacyQueryAdapter<QUERY extends HibernateReportQuery> implements ReportQuery {
	@Inject
	private ReportService reportService;

	/**
	 * 
	 */
	public LegacyQueryAdapter() {
		super();
	}

	/**
	 * @see org.squashtest.tm.api.report.query.ReportQuery#executeQuery(java.util.Map, java.util.Map) Template method
	 *      which delegates to {@link #processNonStandardCriteria(Map, HibernateReportQuery)} and then to
	 *      {@link #processStandardCriteria(Map, HibernateReportQuery)}
	 */
	@Override
	public final void executeQuery(Map<String, Criteria> criteria, Map<String, Object> model) {
		HibernateReportQuery legacyQuery = getLegacyQueryProvider().get();

		processNonStandardCriteria(criteria, legacyQuery);
		processStandardCriteria(criteria, legacyQuery);

		Collection<?> data = reportService.executeQuery(legacyQuery);
		model.put("data", data);

		/*
		 * Feat 3629 : we need to put the milestone label as a parameter of the reports.
		 * We can fetch it from the first DTO of the list. Screw it if this is a hack
		 * because the whole thing is a hack anyway.
		 */

		if (! data.isEmpty()){
			HasMilestoneLabel dto  = (HasMilestoneLabel) data.iterator().next();
			model.put("milestoneLabel", dto.getMilestone());
		}

	}

	/**
	 * Should add any non standard criteria to the legacy query.
	 * 
	 * @param criteria
	 * @param legacyQuery
	 * @see #processStandardCriteria(Map, HibernateReportQuery)
	 */
	protected abstract void processNonStandardCriteria(Map<String, Criteria> criteria, HibernateReportQuery legacyQuery);

	/**
	 * Adds any standard criteria as is to the legacy query. "standardness" is defined by #isStandardCriteria()
	 * 
	 * @param criteria
	 * @param legacyQuery
	 * @see #isStandardCriteria(String)
	 */
	private void processStandardCriteria(Map<String, Criteria> criteria, HibernateReportQuery legacyQuery) {
		for (Map.Entry<String, Criteria> entry : criteria.entrySet()) {
			if (isStandardCriteria(entry.getKey())) {
				legacyQuery.setCriterion(entry.getKey(), entry.getValue().getValue());
			}
		}
	}
	/**
	 * Should return true if criteria is standard, meaning it will be passed as is to the legacy query.
	 * 
	 * @param criterionName
	 * @return
	 */
	protected abstract boolean isStandardCriteria(String criterionName);

	/**
	 * @return the legacyQueryProvider
	 */
	protected abstract Provider<QUERY> getLegacyQueryProvider();

}
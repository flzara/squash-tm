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
import org.squashtest.tm.internal.domain.report.common.hibernate.HibernateRequirementCoverageByTestsQuery;
import org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery;

/**
 * @author Gregory Fouquet
 *
 */
public class RequirementCoverageByTestsQueryAdapter extends LegacyQueryAdapter<HibernateRequirementCoverageByTestsQuery> {

	@Inject
	private Provider<HibernateRequirementCoverageByTestsQuery> legacyQueryProvider;

	/**
	 *
	 */
	private static final String LEGACY_PROJECT_IDS = "projectIds[]";

	private static final String MILESTONE_IDS = "milestones";

	/**
	 * @see org.squashtest.tm.plugin.report.std.query.LegacyQueryAdapter#processNonStandardCriteria(java.util.Map,
	 *      org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery)
	 */
	@Override
	protected void processNonStandardCriteria(Map<String, Criteria> criteria, HibernateReportQuery legacyQuery) {

		String mode = (String)criteria.get("selectionMode").getValue();

		if ("PROJECT_PICKER".equals(mode)){
			Criteria idsCrit = criteria.get("projectIds");
			legacyQuery.setCriterion(LEGACY_PROJECT_IDS, ((Collection<?>) idsCrit.getValue()).toArray());

			Criteria modeCrit = criteria.get("mode");
			legacyQuery.setCriterion("mode" , modeCrit.getValue());

		}
		else{
			Criteria mIds = criteria.get(MILESTONE_IDS);
			legacyQuery.setCriterion(MILESTONE_IDS, ((Collection<?>) mIds.getValue()).toArray());

			// when using the milestone picker, the "mode" is set to ad-hoc value "0"
			legacyQuery.setCriterion("mode" , "0");
		}

	}

	/**
	 * @see org.squashtest.tm.plugin.report.std.query.LegacyQueryAdapter#isStandardCriteria(java.lang.String)
	 */
	@Override
	protected boolean isStandardCriteria(String criterionName) {
		return false;
	}

	/**
	 * @return the legacyQueryProvider
	 */
	@Override
	public Provider<HibernateRequirementCoverageByTestsQuery> getLegacyQueryProvider() {
		return legacyQueryProvider;
	}
}

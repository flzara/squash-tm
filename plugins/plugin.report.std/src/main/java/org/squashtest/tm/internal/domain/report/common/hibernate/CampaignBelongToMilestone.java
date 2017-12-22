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
package org.squashtest.tm.internal.domain.report.common.hibernate;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.squashtest.tm.domain.campaign.Campaign;




public class CampaignBelongToMilestone extends IsInSet<Long> {


	public CampaignBelongToMilestone() {
		super();
	}


	@Override
	public Criterion makeCriterion() {
		Criterion result = null;

		List<Long> milestoneIds = (List<Long>)getTypedParameters();

		if (milestoneIds!=null && ! milestoneIds.isEmpty()){

			DetachedCriteria subQuery = DetachedCriteria.forClass(Campaign.class, "milestonecpgs")
					.createAlias("milestonecpgs.milestones", "milestones")
					.add(Restrictions.in("milestones.id", milestoneIds))
					.setProjection(Projections.projectionList().add(Property.forName("milestonecpgs.id")));

			result = Property.forName("campaigns.id").in(subQuery);
		}

		return result;
	}

	@Override
	public Long fromValueToTypedValue(Object o) {
		return Long.parseLong(o.toString());
	}

}

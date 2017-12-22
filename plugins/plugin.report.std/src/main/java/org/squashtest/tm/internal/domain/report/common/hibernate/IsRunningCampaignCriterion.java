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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.internal.domain.report.common.dto.ExProgressCampaignStatus;
import org.squashtest.tm.internal.domain.report.query.QueryOperator;
import org.squashtest.tm.internal.domain.report.query.hibernate.ReportCriterion;

/*
 * This ReportCriterion cannot be reused due to its very specific content. It'll basically check that a campaign have
 * at least one iteration having a state of ExecutionStatus.READY or ExecutionStatus.RUNNING, or not, depending on the
 * parameter (an ExProgressCampaignStatus).
 *
 * Actually it uses a subquery that returns the list of ids of campaigns like described above, then embbed the subquery in
 * a Criterion testing if a given campaign (from the main query and identified by its alias) is
 * 	- among them (if the parameter is CAMPAIGN_RUNNING),
 *  - or not among them (parameter is CAMPAIGN_OVER),
 *  - or if we don't care at all (parameter is CAMPAIGN_ALL).
 *
 *
 */
public class IsRunningCampaignCriterion extends ReportCriterion {

	public IsRunningCampaignCriterion(){
		super();
		setOperator(QueryOperator.COMPARATOR_SPECIAL);
		setParamClass(ExProgressCampaignStatus.class);
	}

	public IsRunningCampaignCriterion(String criterionName, String attributePath){
		setCriterionName(criterionName);
		setAttributePath(attributePath);

	}

	@Override
	public Criterion makeCriterion() {
		try{
			Criterion result = null;
			Object[] values = getParameters();
			if (values!=null && values.length>0){

				//here we go
				ExProgressCampaignStatus status = ExProgressCampaignStatus.valueOf(values[0].toString());

				//creation of the subquery.
				DetachedCriteria subQuery = DetachedCriteria.forClass(Campaign.class,"campaigns2")
				.createCriteria("iterations")
				.createCriteria("testPlans", "tps")
				.add(
				Restrictions.disjunction()
							.add(Restrictions.eq("tps.executionStatus", ExecutionStatus.READY))
							.add(Restrictions.eq("tps.executionStatus", ExecutionStatus.RUNNING))

				).setProjection(
						Projections.projectionList().add(Property.forName("campaigns2.id"))
				);



				//embbed the subquery in a Criterion that depends on the parameter.
				if (status==ExProgressCampaignStatus.CAMPAIGN_RUNNING){
					result=Property.forName("campaigns.id").in(subQuery);
				}
				else if (status==ExProgressCampaignStatus.CAMPAIGN_OVER){
					result= Property.forName("campaigns.id").notIn(subQuery);
				}
				else{
					//we do not filter this time
					result=null;
				}


			}
			return result;
		}catch(Exception e){
			return null;
		}
	}




}

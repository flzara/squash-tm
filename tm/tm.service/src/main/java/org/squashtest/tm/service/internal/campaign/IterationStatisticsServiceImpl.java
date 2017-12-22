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
package org.squashtest.tm.service.internal.campaign;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IterationStatisticsService;
import org.squashtest.tm.service.statistics.campaign.CampaignNonExecutedTestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseStatusStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseSuccessRateStatistics;
import org.squashtest.tm.service.statistics.campaign.ScheduledIteration;
import org.squashtest.tm.service.statistics.iteration.IterationProgressionStatistics;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;
import org.squashtest.tm.service.statistics.iteration.TestSuiteTestInventoryStatistics;

@Transactional(readOnly=true)
@Service("IterationStatisticsService")
public class IterationStatisticsServiceImpl implements IterationStatisticsService{

	private static final String PERM_CAN_READ_CAMPAIGN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ') ";
	private static final String PERM_CAN_READ_ITERATION = "hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') ";
	private static final String ID = "id";
	private static final Logger LOGGER = LoggerFactory.getLogger(IterationStatisticsService.class);

	@PersistenceContext
	private EntityManager em;



	@Override
	@PreAuthorize(PERM_CAN_READ_ITERATION + OR_HAS_ROLE_ADMIN)
	public CampaignTestCaseStatusStatistics gatherIterationTestCaseStatusStatistics(long iterationId){

		CampaignTestCaseStatusStatistics result = new CampaignTestCaseStatusStatistics();

		//get the data
		Query query = getCurrentSession().getNamedQuery("IterationStatistics.globaltestinventory");
		query.setParameter(ID, iterationId);
		List<Object[]> res = query.list();

		for (Object[] tuple : res){

			ExecutionStatus status = (ExecutionStatus)tuple[0];
			Long howmany = (Long)tuple[1];

			result.addNumber(howmany.intValue(), status.getCanonicalStatus());

		}

		return result;
	}

	@Override
	@PreAuthorize(PERM_CAN_READ_ITERATION + OR_HAS_ROLE_ADMIN)
	public CampaignNonExecutedTestCaseImportanceStatistics gatherIterationNonExecutedTestCaseImportanceStatistics(long iterationId){

		CampaignNonExecutedTestCaseImportanceStatistics result = new CampaignNonExecutedTestCaseImportanceStatistics();

		//get the data
		Query query = getCurrentSession().getNamedQuery("IterationStatistics.nonexecutedTestcaseImportance");
		query.setParameter(ID, iterationId);
		List<Object[]> res = query.list();

		for (Object[] tuple : res){

			TestCaseImportance importance = (TestCaseImportance)tuple[0];
			Long howmany = (Long)tuple[1];

			switch(importance){
			case HIGH: result.setPercentageHigh(howmany.intValue()); break;
			case LOW: result.setPercentageLow(howmany.intValue()); break;
			case MEDIUM: result.setPercentageMedium(howmany.intValue()); break;
			case VERY_HIGH: result.setPercentageVeryHigh(howmany.intValue()); break;
			}
		}

		return result;
	}

	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + OR_HAS_ROLE_ADMIN)
	public CampaignTestCaseSuccessRateStatistics gatherIterationTestCaseSuccessRateStatistics(long iterationId) {

		CampaignTestCaseSuccessRateStatistics result = new CampaignTestCaseSuccessRateStatistics();

		//get the data
		Query query = getCurrentSession().getNamedQuery("IterationStatistics.successRate");
		query.setParameter(ID, iterationId);
		List<Object[]> res = query.list();

		for (Object[] tuple : res){

			TestCaseImportance importance = (TestCaseImportance)tuple[0];
			ExecutionStatus status = (ExecutionStatus)tuple[1];
			Long howmany = (Long)tuple[2];

			switch(importance){
			case HIGH: result.addNbHigh(status, howmany.intValue()); break;
			case LOW: result.addNbLow(status, howmany.intValue()); break;
			case MEDIUM: result.addNbMedium(status, howmany.intValue()); break;
			case VERY_HIGH: result.addNbVeryHigh(status, howmany.intValue()); break;
			}
		}

		return result;
	}



	/*
	 * A few comments about test suites having an empty test plan or referencing test case(s) that were deleted.
	 *
	 *  We want to report the test suites with the following rules :
	 *  a) test suites with an empty test plan must be reported anyway,
	 *  b) deleted test cases must be excluded of the statistics.
	 *
	 *  This implies that we must detect those exceptions. We can achieve that thanks to the following behavior :
	 *
	 * 1/ By the virtue of "left outer join" there always will be at least one row in the resultset for each test suite
	 * even when its test plan is empty. In that later case the ExecutionStatus will be null.
	 *
	 * 2/ When the test plan is not empty but contains one or several deleted test cases there will be a row for them anyway.
	 * In that later case the TestCaseImportance will be null.
	 *
	 *
	 * (non-Javadoc)
	 * @see org.squashtest.tm.service.campaign.IterationStatisticsService#gatherTestSuiteTestInventoryStatistics(long)
	 */
	@Override
	@PreAuthorize(PERM_CAN_READ_ITERATION + OR_HAS_ROLE_ADMIN)
	public List<TestSuiteTestInventoryStatistics> gatherTestSuiteTestInventoryStatistics(long iterationId) {

		List<TestSuiteTestInventoryStatistics> result = new LinkedList<>();


		// ****************** gather the model *******************

		// get the test suites and their tests
		Query query = getCurrentSession().getNamedQuery("IterationStatistics.testSuiteStatistics");
		query.setParameter(ID, iterationId);
		List<Object[]> res = query.list();

		// get tests that belongs to no test suite
		Query requery = getCurrentSession().getNamedQuery("IterationStatistics.testSuiteStatistics-testsLeftover");
		requery.setParameter(ID, iterationId);
		List<Object[]> reres = requery.list();

		// merge the second list into the first. The first element of the tuple must be set to '--' - see the reason in IterationStatistics.testSuiteStatistics-testsLeftover
		for (Object[] retuple : reres){
			retuple[0]=null;
			res.add(retuple);
		}


		// ************* processing *********************

		TestSuiteTestInventoryStatistics newStatistics = new TestSuiteTestInventoryStatistics();
		String previousSuiteName = "";

		for (Object[] tuple : res){

			// basic information on the test suite : we always want them.
			String suiteName = (String)tuple[0];
			Date scheduledStart = (Date)tuple[4];
			Date scheduledEnd = (Date)tuple[5];

			if(! sameSuite(previousSuiteName, suiteName)){
				newStatistics = new TestSuiteTestInventoryStatistics();
				newStatistics.setTestsuiteName(suiteName);
				newStatistics.setScheduledStart(scheduledStart);
				newStatistics.setScheduledEnd(scheduledEnd);
				result.add(newStatistics);
			}


			previousSuiteName = suiteName;

			/*
			 * corner cases as discussed in the comments above. We skip the rest of that test suite if :
			 *
			 * 1/ (status == null) because it means that the test plan is empty,
			 * 2/ (importance == null) because means that those test cases were deleted
			 *
			 */
			ExecutionStatus status = (ExecutionStatus)tuple[1];
			TestCaseImportance importance = (TestCaseImportance)tuple[2];
			Long howmany = (Long)tuple[3];

			if (status == null || importance == null){
				continue;
			}

			/*
			 * In any other cases we can process the row
			 */
			newStatistics.addNumber(howmany.intValue(), status.getCanonicalStatus());

			if(status == ExecutionStatus.RUNNING || status == ExecutionStatus.READY){
				addImportance(newStatistics, importance, howmany);

			}

		}

		return result;
	}

	private boolean sameSuite(String name1, String name2){
		return  name1 == null && name2 == null ||
			name1 != null && name1.equals(name2);
	}

	private void addImportance(TestSuiteTestInventoryStatistics newStatistics, TestCaseImportance importance, Long howmany){
		switch(importance){
		case HIGH: newStatistics.addNbHigh(howmany.intValue()); break;
		case LOW: newStatistics.addNbLow(howmany.intValue()); break;
		case MEDIUM: newStatistics.addNbMedium(howmany.intValue()); break;
		case VERY_HIGH: newStatistics.addNbVeryHigh(howmany.intValue()); break;
		}
	}

	@Override
	@PreAuthorize(PERM_CAN_READ_ITERATION + OR_HAS_ROLE_ADMIN)
	public IterationStatisticsBundle gatherIterationStatisticsBundle(long iterationId) {

		IterationStatisticsBundle bundle = new IterationStatisticsBundle();

		CampaignTestCaseStatusStatistics testcaseStatuses = gatherIterationTestCaseStatusStatistics(iterationId);
		CampaignNonExecutedTestCaseImportanceStatistics testcaseImportance = gatherIterationNonExecutedTestCaseImportanceStatistics(iterationId);
		CampaignTestCaseSuccessRateStatistics testCaseSuccessRate = gatherIterationTestCaseSuccessRateStatistics(iterationId);
		List<TestSuiteTestInventoryStatistics> testSuiteTestInventoryStatistics = gatherTestSuiteTestInventoryStatistics(iterationId);
		IterationProgressionStatistics iterationProgressionStatistics = gatherIterationProgressionStatistics(iterationId);
		bundle.setIterationTestCaseStatusStatistics(testcaseStatuses);
		bundle.setIterationNonExecutedTestCaseImportanceStatistics(testcaseImportance);
		bundle.setIterationTestCaseSuccessRateStatistics(testCaseSuccessRate);
		bundle.setTestsuiteTestInventoryStatisticsList(testSuiteTestInventoryStatistics);
		bundle.setIterationProgressionStatistics(iterationProgressionStatistics);
		bundle.setSelectedId(iterationId);
		return bundle;

	}

	@Override
	public IterationProgressionStatistics gatherIterationProgressionStatistics(long iterationId) {
		IterationProgressionStatistics progression = new IterationProgressionStatistics();
		Session session = getCurrentSession();
		Query query = session.getNamedQuery("IterationStatistics.findScheduledIterations");
		query.setParameter("id", iterationId, LongType.INSTANCE);
		ScheduledIteration scheduledIteration = (ScheduledIteration) query.uniqueResult();

		//TODO : have the db do the job for me
		Query requery = session.getNamedQuery("IterationStatistics.findExecutionsHistory");
		requery.setParameter("id", iterationId, LongType.INSTANCE);
		requery.setParameterList("nonterminalStatuses", ExecutionStatus.getNonTerminatedStatusSet());
		List<Date> executionHistory = requery.list();

		try{
			progression.setScheduledIteration(scheduledIteration);
			ScheduledIteration.checkIterationDatesAreSet(scheduledIteration);

			progression.computeSchedule();

			// actual executions
			progression.computeCumulativeTestPerDate(executionHistory);
		} catch(IllegalArgumentException ex){
			if (LOGGER.isInfoEnabled()){
				LOGGER.info("CampaignStatistics : could not generate iteration progression statistics for iteration "+ iterationId+" : some dates are wrong");
			}
			progression.addi18nErrorMessage(ex.getMessage());
		}

		return progression;
	}

	private Session getCurrentSession(){
		return em.unwrap(Session.class);
	}
}

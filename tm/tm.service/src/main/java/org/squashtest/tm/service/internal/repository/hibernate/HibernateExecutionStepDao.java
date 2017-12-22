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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;


@Repository
public class HibernateExecutionStepDao extends HibernateEntityDao<ExecutionStep>
		implements ExecutionStepDao {

	@Override
	public int findExecutionStepRank(Long executionStepId) {
		Execution execution = (Execution)currentSession().createCriteria(Execution.class)
		.createCriteria("steps")
		.add(Restrictions.eq("id",executionStepId))
		.uniqueResult();

		int index=0;
		for (ExecutionStep step : execution.getSteps()){
			if (step.getId().equals(executionStepId)){ return index;}
			index++;
		}
		return index;
	}

	@Override
	public Execution findParentExecution(final Long executionStepId) {
		SetQueryParametersCallback newCallBack = new ChildIdQueryParameterCallback(executionStepId);

		Execution exec = executeEntityNamedQuery("executionStep.findParentNode",
				newCallBack);

		Hibernate.initialize(exec.getSteps());

		return exec;

	}

	private static final class ChildIdQueryParameterCallback implements SetQueryParametersCallback{
		private Long childId;
		private ChildIdQueryParameterCallback(Long childId){
			this.childId = childId;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setLong("childId", childId);
		}
	}

	@Override
	public MultiMap findStepExecutionsStatus(List<Long> testCaseIds,List<Long> testStepIds) {
		if (testStepIds.isEmpty()) {
			return new MultiValueMap();
		}
		List<ExecutionStep> execSteps = new ArrayList<>();
		for (Long tcId : testCaseIds) {
			Query q = currentSession().getNamedQuery("execution.findAllByTestCaseIdAndItIdOrderByRunDate");
			q.setParameter("testCaseId", tcId, LongType.INSTANCE);
			List<Execution> execs = q.list();
			if (!execs.isEmpty()) {
				execSteps.addAll(execs.get(0).getSteps());
			}
		}
		MultiMap result = new MultiValueMap();
		for (ExecutionStep executionStep : execSteps) {
			// [Issue 6943] In case the referencedTestStep of an executionStep has been deleted, to avoid a npe, Optional util is utilized here
			Optional.of(executionStep).map(ExecutionStep :: getReferencedTestStep).map(TestStep :: getId).ifPresent(
				g -> result.put(executionStep.getReferencedTestStep().getId(), executionStep)
			);
		}
		return result;
	}


}

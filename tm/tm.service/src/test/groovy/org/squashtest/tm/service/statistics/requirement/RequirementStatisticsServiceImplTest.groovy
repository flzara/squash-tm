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
package org.squashtest.tm.service.statistics.requirement

import javax.persistence.EntityManager
import javax.persistence.Query;

import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.service.internal.campaign.CampaignStatisticsServiceImpl;
import org.squashtest.tm.service.internal.requirement.RequirementStatisticsServiceImpl
import static org.squashtest.tm.domain.execution.ExecutionStatus.*;
import spock.lang.Specification


class RequirementStatisticsServiceImplTest extends Specification {

	RequirementStatisticsServiceImpl service = new RequirementStatisticsServiceImpl()
	
	EntityManager em = Mock()

	def setup() {
		service.entityManager = em
	}
	
	def addMockQuery(result) {
		Query q = Mock()
		em.createNativeQuery(_) >> q
		em.createNamedQuery(_) >> q
		q.getResultList() >> result
	}

	
	def "gatherBoundTestCaseStatistics should return bound testCases statistics"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>(); 
			if(zeroTestCases > 0) tuples.add([0, new BigInteger(zeroTestCases)] as Object[]);
			if(oneTestCase > 0) tuples.add([1, new BigInteger(oneTestCase)] as Object[]);
			if(manyTestCases > 0) tuples.add([2, new BigInteger(manyTestCases)] as Object[]);
			
			addMockQuery(tuples)
		
		when:
			RequirementBoundTestCasesStatistics res = service.gatherBoundTestCaseStatistics([1l])

		then:
			res.getZeroTestCases() == zeroTestCases;
			res.getOneTestCase() == oneTestCase;
			res.getManyTestCases() == manyTestCases;
		
		where:
			zeroTestCases | oneTestCase | manyTestCases
			7 			  | 11 			| 23
			0 			  | 11 			| 23
			7 			  | 0 			| 23
			7 			  | 11 			| 0
			0 			  | 0 			| 23
			7 			  | 0 			| 0
			0 			  | 11 			| 0
			0 			  | 0			| 0
	}
	
	def "gatherBoundTestCaseStatistics should throw IllegalArgumentException"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>();
			tuples.add([0, new BigInteger(7)] as Object[]);
			tuples.add([1, new BigInteger(11)] as Object[]);
			tuples.add([2, new BigInteger(23)] as Object[]);
			tuples.add([3, new BigInteger(31)] as Object[]);
			
			addMockQuery(tuples)
		
		when:
			RequirementBoundTestCasesStatistics res = service.gatherBoundTestCaseStatistics([1l])

		then:
			thrown(IllegalArgumentException)
	}
	
	
	def "gatherRequirementCriticalityStatistics should return criticality statistics"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>(); 
			if(undefined > 0) tuples.add([RequirementCriticality.UNDEFINED, undefined] as Object[]);
			if(minor > 0) tuples.add([RequirementCriticality.MINOR, minor] as Object[]);
			if(major > 0) tuples.add([RequirementCriticality.MAJOR, major] as Object[]);
			if(critical > 0) tuples.add([RequirementCriticality.CRITICAL, critical] as Object[]);
			addMockQuery(tuples)
		
		when:
			RequirementCriticalityStatistics res = service.gatherRequirementCriticalityStatistics([1l])
		
		then:
			res.getUndefined() == undefined;
			res.getMinor() == minor;
			res.getMajor() == major;
			res.getCritical() == critical;
		
		where:
			undefined | minor | major | critical
			7l		  |	11l   |	23l | 31l
			0l		  |	11l   |	23l | 31l
			0l		  |	0l    |	23l | 31l
			0l		  | 0l    |	0l  | 31l
			0l		  | 0l 	  | 0l  | 0l
	}
	
	def "gatherRequirementCriticalityStatistics should throw ClassCastException"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>();
			tuples.add([RequirementCriticality.UNDEFINED, 7l] as Object[]);
			tuples.add([RequirementCriticality.MINOR, 11l] as Object[]);
			tuples.add([RequirementCriticality.MAJOR, 23l] as Object[]);
			tuples.add([RequirementCriticality.CRITICAL, 31l] as Object[]);
			tuples.add(["CustomCriticality", 42l] as Object[]);
		
		addMockQuery(tuples)
	
	when:
		RequirementBoundTestCasesStatistics res = service.gatherBoundTestCaseStatistics([1l])

	then:
		thrown(ClassCastException)
	}
	
	
	def "gatherRequirementStatusesStatistics should return statuses statistics"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>();
			if(workInProgress > 0) tuples.add([RequirementStatus.WORK_IN_PROGRESS, workInProgress] as Object[]);
			if(underReview > 0) tuples.add([RequirementStatus.UNDER_REVIEW, underReview] as Object[]);
			if(approved > 0) tuples.add([RequirementStatus.APPROVED, approved] as Object[]);
			if(obsolete > 0) tuples.add([RequirementStatus.OBSOLETE, obsolete] as Object[]);
			addMockQuery(tuples)
		
		when:
			RequirementStatusesStatistics res = service.gatherRequirementStatusesStatistics([1l])
		
		then:
			res.getWorkInProgress() == workInProgress
			res.getUnderReview() == underReview
			res.getApproved() == approved
			res.getObsolete() == obsolete
		
		where:
			workInProgress | underReview | approved | obsolete
			7l		 	   | 11l  		 | 23l 		| 31l
			0l		 	   | 11l  		 | 23l	    | 31l
			0l		 	   | 0l   		 | 23l 	 	| 31l
			0l		 	   | 0l   		 | 0l 	    | 31l
			0l		  	   | 0l    		 | 0l		| 0l
	}
	
	def "gatherRequirementStatusesStatistics should throw ClassCastException"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>();
			tuples.add([RequirementStatus.WORK_IN_PROGRESS, 7l] as Object[]);
			tuples.add([RequirementStatus.UNDER_REVIEW, 11l] as Object[]);
			tuples.add([RequirementStatus.APPROVED, 23l] as Object[]);
			tuples.add([RequirementStatus.OBSOLETE, 31l] as Object[]);
			tuples.add(["CustomStatus", 42l] as Object[]);
			addMockQuery(tuples)
		
		when:
			RequirementStatusesStatistics res = service.gatherRequirementStatusesStatistics([1l])
		
		then:
			thrown(ClassCastException)
	}

	
	def "gatherRequirementBoundDescriptionStatistics should return bound descriptions statistics"() {
		given:
		List<Object[]>tuples = new ArrayList<Object[]>();
		if(hasNoDescription > 0) tuples.add([0, new BigInteger(hasNoDescription)] as Object[]);
		if(hasDescription > 0) tuples.add([1, new BigInteger(hasDescription)] as Object[]);
		addMockQuery(tuples)
	
	when:
		RequirementBoundDescriptionStatistics res = service.gatherRequirementBoundDescriptionStatistics([1l]);
	
	then:
		res.getHasDescription() == hasDescription
		res.getHasNoDescription() == hasNoDescription
	
	where:
		hasDescription | hasNoDescription
		7		 	   | 11
		0		 	   | 11
		7		 	   | 0
		0		 	   | 0
	}
	
	
	def "gatherRequirementCoverageStatistics should return coverage statistics"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>();
			if(totalUndefined > 0) tuples.add(["UNDEFINED", new BigInteger(undefined), new BigInteger(totalUndefined)] as Object[]);
			if(totalMinor > 0) tuples.add(["MINOR", new BigInteger(minor), new BigInteger(totalMinor)] as Object[]);
			if(totalMajor > 0) tuples.add(["MAJOR", new BigInteger(major), new BigInteger(totalMajor)] as Object[]);
			if(totalCritical > 0) tuples.add(["CRITICAL", new BigInteger(critical), new BigInteger(totalCritical)] as Object[]);
			addMockQuery(tuples)

		when:
			RequirementCoverageStatistics res = service.gatherRequirementCoverageStatistics([1l]);

		then:
			res.getUndefined() == undefined
			res.getTotalUndefined() == totalUndefined
			res.getMinor() == minor
			res.getTotalMinor() == totalMinor
			res.getMajor() == major
			res.getTotalMajor() == totalMajor
			res.getCritical() == critical
			res.getTotalCritical() == totalCritical

		where:
			undefined | totalUndefined | minor | totalMinor | major | totalMajor | critical | totalCritical
			7		  | 10	 		   | 11	   | 20			| 23	| 30		 | 31		| 40
			0		  | 10	 		   | 0	   | 20			| 0		| 30		 | 31		| 40
			0		  | 0	 		   | 0	   | 0			| 0		| 0			 | 31		| 40
			0		  | 0	 		   | 0	   | 0			| 0		| 0			 | 0		| 0
	}

	def "gatherRequirementCoverageStatistics should throw IllegalArgumentException"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>();
			tuples.add(["UNDEFINED", new BigInteger(7), new BigInteger(10)] as Object[]);
			tuples.add(["MINOR", new BigInteger(11), new BigInteger(20)] as Object[]);
			tuples.add(["MAJOR", new BigInteger(23), new BigInteger(30)] as Object[]);
			tuples.add(["CRITICAL", new BigInteger(31), new BigInteger(40)] as Object[]);
			tuples.add(["CustomCriticality", new BigInteger(41), new BigInteger(50)] as Object[]);
			addMockQuery(tuples)

		when:
			RequirementCoverageStatistics res = service.gatherRequirementCoverageStatistics([1l]);

		then:
			thrown(IllegalArgumentException)
	}

	
	def "gatherRequirementValidationStatistics should return validation statistics"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>();
			if(successUndefined > 0) tuples.add(["UNDEFINED", "SUCCESS", new BigInteger(successUndefined)] as Object[]);
			if(successMinor> 0) tuples.add(["MINOR", "SUCCESS", new BigInteger(successMinor)] as Object[]);
			if(successMajor> 0) tuples.add(["MAJOR", "SUCCESS", new BigInteger(successMajor)] as Object[]);
			if(successCritical> 0) tuples.add(["CRITICAL", "SUCCESS", new BigInteger(successCritical)] as Object[]);
			
			if(failureUndefined > 0) tuples.add(["UNDEFINED", "FAILURE", new BigInteger(failureUndefined)] as Object[]);
			if(failureMinor> 0) tuples.add(["MINOR", "FAILURE", new BigInteger(failureMinor)] as Object[]);
			if(failureMajor> 0) tuples.add(["MAJOR", "FAILURE", new BigInteger(failureMajor)] as Object[]);
			if(failureCritical> 0) tuples.add(["CRITICAL", "FAILURE", new BigInteger(failureCritical)] as Object[]);

			if(blockedUndefined > 0) tuples.add(["UNDEFINED", "BLOCKED", new BigInteger(blockedUndefined)] as Object[]);
			if(blockedMinor> 0) tuples.add(["MINOR", "BLOCKED", new BigInteger(blockedMinor)] as Object[]);
			if(blockedMajor> 0) tuples.add(["MAJOR", "BLOCKED", new BigInteger(blockedMajor)] as Object[]);
			if(blockedCritical> 0) tuples.add(["CRITICAL", "BLOCKED", new BigInteger(blockedCritical)] as Object[]);

			if(errorUndefined > 0) tuples.add(["UNDEFINED", "ERROR", new BigInteger(errorUndefined)] as Object[]);
			if(errorMinor> 0) tuples.add(["MINOR", "ERROR", new BigInteger(errorMinor)] as Object[]);
			if(errorMajor> 0) tuples.add(["MAJOR", "ERROR", new BigInteger(errorMajor)] as Object[]);
			if(errorCritical> 0) tuples.add(["CRITICAL", "ERROR", new BigInteger(errorCritical)] as Object[]);
			
			if(notFoundUndefined > 0) tuples.add(["UNDEFINED", "NOT_FOUND", new BigInteger(notFoundUndefined)] as Object[]);
			if(notFoundMinor> 0) tuples.add(["MINOR", "NOT_FOUND", new BigInteger(notFoundMinor)] as Object[]);
			if(notFoundMajor> 0) tuples.add(["MAJOR", "NOT_FOUND", new BigInteger(notFoundMajor)] as Object[]);
			if(notFoundCritical> 0) tuples.add(["CRITICAL", "NOT_FOUND", new BigInteger(notFoundCritical)] as Object[]);

			if(notRunUndefined > 0) tuples.add(["UNDEFINED", "NOT_RUN", new BigInteger(notRunUndefined)] as Object[]);
			if(notRunMinor> 0) tuples.add(["MINOR", "NOT_RUN", new BigInteger(notRunMinor)] as Object[]);
			if(notRunMajor> 0) tuples.add(["MAJOR", "NOT_RUN", new BigInteger(notRunMajor)] as Object[]);
			if(notRunCritical> 0) tuples.add(["CRITICAL", "NOT_RUN", new BigInteger(notRunCritical)] as Object[]);

			if(readyUndefined > 0) tuples.add(["UNDEFINED", "READY", new BigInteger(readyUndefined)] as Object[]);
			if(readyMinor> 0) tuples.add(["MINOR", "READY", new BigInteger(readyMinor)] as Object[]);
			if(readyMajor> 0) tuples.add(["MAJOR", "READY", new BigInteger(readyMajor)] as Object[]);
			if(readyCritical> 0) tuples.add(["CRITICAL", "READY", new BigInteger(readyCritical)] as Object[]);

			if(runningUndefined > 0) tuples.add(["UNDEFINED", "RUNNING", new BigInteger(runningUndefined)] as Object[]);
			if(runningMinor> 0) tuples.add(["MINOR", "RUNNING", new BigInteger(runningMinor)] as Object[]);
			if(runningMajor> 0) tuples.add(["MAJOR", "RUNNING", new BigInteger(runningMajor)] as Object[]);
			if(runningCritical> 0) tuples.add(["CRITICAL", "RUNNING", new BigInteger(runningCritical)] as Object[]);
			
			if(settledUndefined > 0) tuples.add(["UNDEFINED", "SETTLED", new BigInteger(settledUndefined)] as Object[]);
			if(settledMinor> 0) tuples.add(["MINOR", "SETTLED", new BigInteger(settledMinor)] as Object[]);
			if(settledMajor> 0) tuples.add(["MAJOR", "SETTLED", new BigInteger(settledMajor)] as Object[]);
			if(settledCritical> 0) tuples.add(["CRITICAL", "SETTLED", new BigInteger(settledCritical)] as Object[]);

			if(untestableUndefined > 0) tuples.add(["UNDEFINED", "UNTESTABLE", new BigInteger(untestableUndefined)] as Object[]);
			if(untestableMinor> 0) tuples.add(["MINOR", "UNTESTABLE", new BigInteger(untestableMinor)] as Object[]);
			if(untestableMajor> 0) tuples.add(["MAJOR", "UNTESTABLE", new BigInteger(untestableMajor)] as Object[]);
			if(untestableCritical> 0) tuples.add(["CRITICAL", "UNTESTABLE", new BigInteger(untestableCritical)] as Object[]);

			if(warningUndefined > 0) tuples.add(["UNDEFINED", "WARNING", new BigInteger(warningUndefined)] as Object[]);
			if(warningMinor> 0) tuples.add(["MINOR", "WARNING", new BigInteger(warningMinor)] as Object[]);
			if(warningMajor> 0) tuples.add(["MAJOR", "WARNING", new BigInteger(warningMajor)] as Object[]);
			if(warningCritical> 0) tuples.add(["CRITICAL", "WARNING", new BigInteger(warningCritical)] as Object[]);

			addMockQuery(tuples)

	when:
			RequirementValidationStatistics res = service.gatherRequirementValidationStatistics([1l]);

	then:
			res.getConclusiveUndefined() == successUndefined
			res.getConclusiveMinor() == successMinor
			res.getConclusiveMajor() == successMajor
			res.getConclusiveCritical() == successCritical
			
			res.getInconclusiveUndefined() == failureUndefined
			res.getInconclusiveMinor() == failureMinor
			res.getInconclusiveMajor() == failureMajor
			res.getInconclusiveCritical() == failureCritical
			
			res.getUndefinedUndefined() == blockedUndefined + errorUndefined + notFoundUndefined + notRunUndefined + readyUndefined + 
											runningUndefined + settledUndefined + untestableUndefined + warningUndefined
			res.getUndefinedMinor() == blockedMinor + errorMinor + notFoundMinor + notRunMinor + readyMinor + 
											runningMinor + settledMinor + untestableMinor + warningMinor
			res.getUndefinedMajor() == blockedMajor + errorMajor + notFoundMajor + notRunMajor + readyMajor + 
											runningMajor + settledMajor + untestableMajor + warningMajor
			res.getUndefinedCritical() == blockedCritical + errorCritical + notFoundCritical + notRunCritical + readyCritical + 
											runningCritical + settledCritical + untestableCritical + warningCritical

	where:
		successUndefined    | successMinor	  | successMajor 	| successCritical	 |
		failureUndefined    | failureMinor 	  | failureMajor 	| failureCritical	 |
		blockedUndefined    | blockedMinor	  | blockedMajor 	| blockedCritical	 |
		errorUndefined 	    | errorMinor 	  | errorMajor 	 	| errorCritical		 |
		notFoundUndefined   | notFoundMinor	  | notFoundMajor 	| notFoundCritical	 |
		notRunUndefined     | notRunMinor 	  | notRunMajor 	| notRunCritical	 |
		readyUndefined      | readyMinor 	  | readyMajor 		| readyCritical		 |
		runningUndefined    | runningMinor 	  | runningMajor 	| runningCritical	 |
		settledUndefined    | settledMinor 	  | settledMajor 	| settledCritical 	 |
		untestableUndefined | untestableMinor | untestableMajor | untestableCritical |
		warningUndefined	| warningMinor	  | warningMajor 	| warningCritical
		1		  		    | 2	 		  	  | 3	  		 	| 4 				 |
		5		  		    | 6	 			  | 7	  	  	 	| 8 				 |
		9		  		    | 10	 	  	  | 11	  	  	 	| 12 				 |
		13		  	        | 14	 		  | 15	  	 	 	| 16 				 |
		17		  		    | 18	 		  | 19	  	      	| 20 				 |
		21		  		    | 22	 	  	  | 23	  	   	    | 24 				 |
		25		  	        | 26	 		  | 27	  	 		| 28 				 |
		29		  		    | 30	 		  | 31	  	   		| 32 				 |
		33		  		    | 34	 		  | 35	  	   		| 36 				 |
		37		  		    | 38	 		  | 39	  	   		| 40 				 |
		41		  		 	| 42	 		  | 43	  	   	 	| 44 				 
		0		  		    | 0	 		  	  | 0	  		 	| 0 				 |
		0		  		    | 0	 			  | 0	  	  	 	| 0 				 |
		0		  		    | 0		 	  	  | 0	  	  	 	| 0 				 |
		0		  	        | 0	 			  | 0	  	 	 	| 0 				 |
		0		  		    | 0	 			  | 0	  	      	| 0 				 |
		0		  		    | 0	 		  	  | 0	  	   	    | 0 				 |
		0		  	        | 0	 			  | 0	  	 		| 0 				 |
		0		  		    | 0	 			  | 0	  	   		| 0 				 |
		0		  		    | 0	 			  | 0	  	   		| 0 				 |
		0		  		    | 0	 			  | 0	  	   		| 0 				 |
		0		  		 	| 0	 			  | 0	  	   	 	| 0				 
	}
	
	def "gatherRequirementValidationStatistics should throw IllegalArgumentException"() {
		given:
			List<Object[]>tuples = new ArrayList<Object[]>();
			tuples.add(["UNDEFINED", "SUCCESS", new BigInteger(7)] as Object[]);
			tuples.add(["MINOR", "FAILURE", new BigInteger(11)] as Object[]);
			tuples.add(["MAJOR", "BLOCKED", new BigInteger(23)] as Object[]);
			tuples.add([criticality, status, new BigInteger(31)] as Object[]);
			addMockQuery(tuples)

		when:
			RequirementValidationStatistics res = service.gatherRequirementValidationStatistics([1l]);

		then:
			thrown(IllegalArgumentException)
			
		where:
		criticality 		 | status
		"CRITICAL"			 | "OTHER_STATUS"
		"OTHER_CRITICALITY"  | "READY"
	}
	
	
	def "gatherRequirementIdsFromValidation should return requirement Ids"() {
		given:
			List<BigInteger> reqIdsFromValidation = reqIdsList
			
			addMockQuery(reqIdsFromValidation)
		
		when:
			List<Long> res = service.gatherRequirementIdsFromValidation([1l, 42l, 6l], RequirementCriticality.CRITICAL, ["test", "test", "test"]);
			Set<Long> resSet = new HashSet<Long>(res);
		then:
			resSet ==  expectedSet
				
		where:
			reqIdsList | expectedSet
			[new BigInteger(3), new BigInteger(5), 
			new BigInteger(7), new BigInteger(9), 
			new BigInteger(13), new BigInteger(21),
			new BigInteger(27), new BigInteger(29)] | [3l, 5l, 7l, 9l, 13l, 21l, 29l, 27l] as Set
			
			[new BigInteger(42)] | [42] as Set
			
			[] | [] as Set
	}
	
}

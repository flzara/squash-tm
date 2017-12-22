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
package org.squashtest.tm.domain.testcase

import static org.squashtest.tm.domain.requirement.RequirementStatus.*

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testutils.MockFactory;
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException

import spock.lang.Specification
import spock.lang.Unroll

class RequirementVersionCoverageTest extends Specification {

	MockFactory mockFactory = new MockFactory()

	def "should not be able to verify an obsolete requirement"() {
		given:
		TestCase tc = new TestCase()

		and:
		RequirementVersion req = new RequirementVersion(status: RequirementStatus.OBSOLETE)

		when:
		new RequirementVersionCoverage(req, tc)

		then:
		thrown(RequirementVersionNotLinkableException)
	}
	def "should not verify 2 versions of same requirement"() {
		given:
		TestCase tc = new TestCase()
		RequirementVersion version = new RequirementVersion();
		version.setName("version");
		Requirement req = new Requirement(version)
		new RequirementVersionCoverage(req, tc)

		and:
		req.increaseVersion()

		when:
		new RequirementVersionCoverage(req.currentVersion, tc)

		then:
		thrown(RequirementAlreadyVerifiedException)
	}


	def "when creating a new coverage, tc and req also contain the new coverage"() {
		given:
		TestCase tc = new TestCase()

		and:
		RequirementVersion req = new RequirementVersion()

		when:
		new RequirementVersionCoverage(req, tc)

		then:
		req.verifyingTestCases.contains tc
		tc.verifiedRequirementVersions.contains req
	}

	def "should copy rvc for Test Case"() {
		given:
		TestCase source = new TestCase()
		source.setName("source");
		source.notifyAssociatedWithProject(mockFactory.mockProject())
		RequirementVersion req = new RequirementVersion(name: "")
		RequirementVersionCoverage coverage = new RequirementVersionCoverage(req, source)
		TestCase copy = source.createCopy()
		when:
		List<RequirementVersionCoverage> copies = source.createRequirementVersionCoveragesForCopy(copy);

		then:
		copies.get(0).getVerifiedRequirementVersion() == req
		copies.get(0).getVerifyingTestCase() == copy
	}

	def "should copy rvc for Test Case with steps"() {
		given:
		TestCase called = new TestCase()
		TestCase source = new TestCase()
		source.setName("source");
		ActionTestStep step1 = new ActionTestStep()
		CallTestStep step2 = new CallTestStep()
		use (ReflectionCategory) {
			// reflection on fields to override workflow
			TestStep.set field: "id", of: step1, to: 1L
			TestStep.set field: "id", of: step2, to: 2L
		}
		source.addStep(step1);
		source.addStep(step2)
		source.notifyAssociatedWithProject(mockFactory.mockProject())
		RequirementVersion req = new RequirementVersion(name: "")
		RequirementVersionCoverage coverage = new RequirementVersionCoverage(req, source)
		coverage.addAllVerifyingSteps([step1]);
		TestCase copy = source.createCopy()
		use (ReflectionCategory) {
			// reflection on fields to override workflow
			TestStep.set field: "id", of: copy.steps.get(0), to: 3L
			TestStep.set field: "id", of: copy.steps.get(1), to: 4L
		}
		when:
		List<RequirementVersionCoverage> copies = source.createRequirementVersionCoveragesForCopy(copy);

		then:
		copies.get(0).getVerifyingSteps().size() == 1
		copies.get(0).getVerifyingSteps().iterator().next() == copy.getSteps().get(0)
	}


	def "should copy rvc for requirement"() {
		given:"a requirement with current version"
		Requirement requirement = new Requirement();
		RequirementVersion source = new RequirementVersion()
		source.setName("source");
		source.setRequirement(requirement)
		and : "a test case"
		TestCase tc = new TestCase()
		and :"with 2 steps"
		ActionTestStep step1 = new ActionTestStep()
		CallTestStep step2 = new CallTestStep()
		use (ReflectionCategory) {
			// reflection on fields to override workflow
			TestStep.set field: "id", of: step1, to: 1L
			TestStep.set field: "id", of: step2, to: 2L
			Requirement.set field : "resource", of:requirement, to: source
		}
		tc.addStep(step1)
		tc.addStep(step2)
		and:"a requirement coverage on tc and source + step1"
		RequirementVersionCoverage rvc = new RequirementVersionCoverage(source,tc)
		rvc.addAllVerifyingSteps([step1])
		and:"a copy of the requirement with a copy of the version"
		Requirement copiedRequirement = requirement.createCopy();
		RequirementVersion copy = copiedRequirement.currentVersion
		when :
		List<RequirementVersionCoverage> copies = source.createRequirementVersionCoveragesForCopy(copy)
		then:
		copies.get(0).getVerifiedRequirementVersion() == copy
		copies.get(0).getVerifyingTestCase() == tc
		copies.get(0).getVerifyingSteps().iterator().next() == step1
	}

	@Unroll("should allow removal of a test case for #status ")
	def "non obsolete requirements should allow removal of a test case "() {
		given :
		def tc = new TestCase(name:"tc", description:"tc")
		RequirementVersionCoverage rvc = prepareRVC(status, tc)
		when :
		rvc.checkCanRemoveTestCaseFromRequirementVersion()
		then :
		notThrown(RequirementVersionNotLinkableException)

		where :
		status << [
			WORK_IN_PROGRESS,
			UNDER_REVIEW,
			APPROVED
		]
	}


	def "obsolete requirements should not allow removal of a test case "() {
		given :
		def tc = new TestCase(name:"tc", description:"tc")
		RequirementVersionCoverage rvc = prepareRVC(OBSOLETE, tc)

		when :
		rvc.checkCanRemoveTestCaseFromRequirementVersion()

		then :
		thrown(RequirementVersionNotLinkableException)
	}


	def "obsolete requirements should not allow verification of a test case"() {
		given :
		def tc = new TestCase(name:"tc", description:"tc")
		RequirementVersion requirementVersion = prepareRequirement(OBSOLETE)

		when :
		new RequirementVersionCoverage(requirementVersion, tc)

		then :
		thrown(RequirementVersionNotLinkableException)
	}

	//that (naive) method builds requirements with initial status that could bypass the workflow.
	private RequirementVersion prepareRequirement(RequirementStatus status){
		def req = new RequirementVersion(name:"req", description:"this is a req");

		for (iterStatus in RequirementStatus.values()) {
			req.status = iterStatus;
			if (iterStatus == status) {
				break;
			}
		}

		return req;
	}

	private RequirementVersionCoverage prepareRVC(RequirementStatus status, TestCase tc){
		def req = new RequirementVersion(name:"req", description:"this is a req")
		RequirementVersionCoverage rvc = new RequirementVersionCoverage(req, tc)
		for (iterStatus in RequirementStatus.values()) {
			req.status = iterStatus;
			if (iterStatus == status) {
				break;
			}
		}

		return rvc;
	}

	@Unroll("should allow verification of a test case for #status ")
	def "non obsolete requirements should allow verification of a test case"() {
		given :
		def tc = new TestCase(name:"tc", description:"tc")
		RequirementVersion requirementVersion = prepareRequirement(status)
		when :
		new RequirementVersionCoverage(requirementVersion, tc)
		then :
		notThrown(IllegalRequirementModificationException)

		where :
		status << [
			WORK_IN_PROGRESS,
			UNDER_REVIEW,
			APPROVED
		]
	}


}

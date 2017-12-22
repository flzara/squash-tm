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
package org.squashtest.tm.domain.requirement

import static org.squashtest.tm.domain.requirement.RequirementStatus.*

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testutils.MockFactory;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.NoVerifiableRequirementVersionException;
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException;

import spock.lang.Specification
import spock.lang.Unroll

class RequirementTest extends Specification {

	Requirement requirement = new Requirement(new RequirementVersion(name: "test req", description: "this is a test req"))

	MockFactory mockFactory = new MockFactory()

	@Unroll("should allow modification of property '#property' for status WORK_IN_PROGRESS")
	def "should allow modification for status WORK_IN_PROGRESS"(){
		given :
		requirement.setStatus(WORK_IN_PROGRESS)

		when :
		requirement[property] = valueToSet

		then :
		notThrown(IllegalRequirementModificationException)

		where :
		property      | valueToSet
		"name"        | "toto"
		"description" | "successful test"
		"reference"   | "blahblah"
		"criticality" | RequirementCriticality.MAJOR
	}

	@Unroll("should allow modification of property '#property' for status UNDER_REVIEW")
	def "should allow modification for status UNDER_REVIEW"(){

		given :
		requirement.setStatus(UNDER_REVIEW)

		when :
		requirement[property] = valueToSet

		then :
		notThrown(IllegalRequirementModificationException)

		where :
		property      | valueToSet
		"name"        | "toto"
		"description" | "successful test"
		"reference"   | "blahblah"
		"criticality" | RequirementCriticality.MAJOR
	}


	@Unroll("should not allow modification of property '#property' for status APPROVED")
	def "should not allow modification for status APPROVED"(){

		given :
		requirement.setStatus(UNDER_REVIEW)//needed because of the workflow
		requirement.setStatus(APPROVED)

		when :
		requirement[property] = valueToSet

		then :
		thrown(IllegalRequirementModificationException)

		where :
		property      | valueToSet
		"description" | "successful test"
		"reference"   | "blahblah"
		"criticality" | RequirementCriticality.MAJOR

	}


	@Unroll("should not allow modification of property '#property' for status OBSOLETE")
	def "should not allow modification for status OBSOLETE"(){

		given :
		requirement.setStatus(OBSOLETE)

		when :
		requirement[property] = valueToSet

		then :
		thrown(IllegalRequirementModificationException)

		where :
		property      | valueToSet
		"description" | "successful test"
		"reference"   | "blahblah"
		"criticality" | RequirementCriticality.MAJOR
	}

	@Unroll("should allow status change when current status is #status")
	def "should allow status change"() {
		given :
		def req = prepareRequirement(status)
		when :
		req.setStatus(status)
		then :
		notThrown(IllegalRequirementModificationException)
		where :
		status << [
			WORK_IN_PROGRESS,
			UNDER_REVIEW,
			APPROVED
		]
	}


	@Unroll("the following workflow transition for #status are legal : #availableStatuses")
	def "check workflow legal"(){

		when :
		def arrayResult = [];
		for (tester in availableStatuses){
			def req = prepareRequirement(status)
			req.setStatus(tester)
			arrayResult << req.getStatus()
		}

		then :
		arrayResult  == availableStatuses

		where :
		status				|	availableStatuses
		WORK_IN_PROGRESS  	|	[ OBSOLETE, WORK_IN_PROGRESS, UNDER_REVIEW ]
		UNDER_REVIEW		|	[ OBSOLETE, UNDER_REVIEW, APPROVED, WORK_IN_PROGRESS ]
		APPROVED			|	[ OBSOLETE, APPROVED, UNDER_REVIEW, WORK_IN_PROGRESS ]
	}

	@Unroll("the following workflow transition for #status are not legal : #illegalStatuses")
	def "check workflow illegal"(){

		when :
		def arrayResult = [];
		for (tester in illegalStatuses){
			def req = prepareRequirement(status)
			try{
				req.setStatus(tester)
			}catch(IllegalRequirementModificationException){
				arrayResult << tester
			}
		}

		then :
		arrayResult  == illegalStatuses

		where :
		status				|	illegalStatuses
		WORK_IN_PROGRESS  	|	[APPROVED]
		UNDER_REVIEW		|	[]
		APPROVED			|	[]
		OBSOLETE			|	[]
	}

	//that (naive) method builds requirements with initial status that could bypass the workflow.
	private Requirement prepareRequirement(RequirementStatus status){
		def req = new Requirement(new RequirementVersion(name:"req", description:"this is a req"));

		for (iterStatus in RequirementStatus.values()) {
			req.status = iterStatus;
			if (iterStatus == status) {
				break;
			}
		}

		return req;
	}

	//same
	private Requirement prepareRequirement(RequirementStatus status, TestCase testCase){
		def req = new Requirement(new RequirementVersion(name:"req", description:"this is a req"));
		req.addVerifyingTestCase(testCase)

		for (iterStatus in RequirementStatus.values()){
			req.status = iterStatus;
			if (iterStatus == status) {
				break;
			}
		}

		return req;
	}

	def "should create a 'pastable' copy"() {
		given:
		RequirementVersion ver = new RequirementVersion(name: "ver")
		Requirement source = new Requirement(ver);

		and:
		Project project = mockFactory.mockProject()
		source.notifyAssociatedWithProject(project);

		when:
		Requirement copy = source.createCopy()

		then:
		copy.project == source.project

		copy.resource.name == "ver"
		copy.resource != ver


		copy.versions.size() == 1
		copy.versions.contains(copy.resource)
		copy.versions*.requirement == [copy]
	}

	def "'pastable' copy should not have versions"() {
		given : "a requirement and it's current version"
		RequirementVersion ver = new RequirementVersion(name: "ver")
		Requirement source = new Requirement(ver);
		source.notifyAssociatedWithProject(mockFactory.mockProject())

		and: "an older requirement version"
		RequirementVersion old = new RequirementVersion(name: "old")
		source.versions << old

		when:
		Requirement copy = source.createCopy()

		then: "the requirement versions contain only the current version"
		copy.resource.name == "ver"
		copy.resource != ver

		copy.versions.size() == 1
		copy.versions*.name.containsAll(["ver"])
		copy.versions*.requirement == [copy]
	}

	def "'pastable' copy of versions should not have obsolete versions"() {
		given:"a requirement"
		RequirementVersion ver = new RequirementVersion(name: "ver")
		Requirement source = new Requirement(ver);
		and:"with 2 versions, one being obsolete"
		RequirementVersion obsolete = new RequirementVersion(name: "obsolete")
		use(ReflectionCategory) {
			RequirementVersion.set field: "status", of: obsolete, to: RequirementStatus.OBSOLETE
		}
		source.versions << obsolete
		RequirementVersion old = new RequirementVersion(name: "old")
		source.versions << old
		and: "it's copy"
		RequirementVersion ver2 = new RequirementVersion(name: "ver")
		Requirement copy = new Requirement(ver2);


		when:
		def result = source.addPreviousVersionsCopiesToCopy(copy)

		then:
		copy.versions.size() == 2
		copy.versions*.name.containsAll(["ver", "old"])
		copy.versions*.requirement == [copy, copy]
	}

	def "should increase the current version"() {
		given:
		RequirementVersion ver = new RequirementVersion(name: "ver")
		Requirement req = new Requirement(ver);

		when:
		req.increaseVersion();

		then:
		req.currentVersion != ver
		req.currentVersion.requirement == req
		req.versions.size() == 2
		req.versions[1] == ver
	}

	def "verifiableVersion should be the latest approved version"() {
		given: "a req with an approved version"
		RequirementVersion v1 = new RequirementVersion()
		v1.setName("name");
		use (ReflectionCategory) {
			RequirementVersion.set field: "status", of: v1, to: RequirementStatus.APPROVED
		}
		Requirement req = new Requirement(v1)

		and: "a new approved version"
		req.increaseVersion()
		def expectedVerifiable = req.currentVersion
		use (ReflectionCategory) {
			RequirementVersion.set field: "status", of: expectedVerifiable, to: RequirementStatus.APPROVED
		}

		and: "a new non approved version"
		req.increaseVersion()

		when:
		def res = req.defaultVerifiableVersion

		then:
		res == expectedVerifiable
	}

	def "verifiableVersion should be the latest non-obsolete version"() {
		given: "a req with an approved version"
		RequirementVersion v1 = new RequirementVersion()
		v1.setName("name");
		use (ReflectionCategory) {
			RequirementVersion.set field: "status", of: v1, to: RequirementStatus.UNDER_REVIEW
		}
		Requirement req = new Requirement(v1)

		and: "a new approved version"
		req.increaseVersion()
		def expectedVerifiable = req.currentVersion
		use (ReflectionCategory) {
			RequirementVersion.set field: "status", of: expectedVerifiable, to: RequirementStatus.UNDER_REVIEW
		}

		and: "a new non approved version"
		req.increaseVersion()
		use (ReflectionCategory) {
			RequirementVersion.set field: "status", of: req.currentVersion, to: RequirementStatus.OBSOLETE
		}

		when:
		def res = req.defaultVerifiableVersion

		then:
		res == expectedVerifiable
	}

	def "should throw an exception when no version is verifiable"() {
		given: "a req with an approved version"
		RequirementVersion v1 = new RequirementVersion()
		use (ReflectionCategory) {
			RequirementVersion.set field: "status", of: v1, to: RequirementStatus.OBSOLETE
		}
		Requirement req = new Requirement(v1)

		when:
		def res = req.defaultVerifiableVersion

		then:
		thrown(NoVerifiableRequirementVersionException)
	}

	def "should accept another Requirement as children"(){
		given :
		Requirement root = newRequirement("root")
		root.notifyAssociatedWithProject(mockFactory.mockProject())
		Requirement son = newRequirement("son")

		when :
		root.addContent son

		then :
		root.content as Set ==  [son] as Set
	}

	def "should say the name of requirements it contains"(){
		given :
		Requirement root = newRequirement("root")
		root.notifyAssociatedWithProject(mockFactory.mockProject())

		when :
		["bob", "mike", "robert"].each { root.addContent newRequirement(it) }

		then :
		root.contentNames as Set == ["bob", "mike", "robert"] as Set
	}

	def "should say that the given name is available "(){
		when :
		Requirement root = newRequirement("root")
		root.notifyAssociatedWithProject(mockFactory.mockProject())
		["bob", "mike", "robert"].each { root.addContent newRequirement(it) }

		then :
		root.isContentNameAvailable("larry")
	}

	def "should say that the given name is not available "(){
		when :
		Requirement root = newRequirement("root")
		root.notifyAssociatedWithProject(mockFactory.mockProject())
		["bob", "mike", "robert"].each { root.addContent newRequirement(it) }

		then :
		! root.isContentNameAvailable("bob")
	}



	def "should not accept to append a requirement if another requirement having the same name is present"(){
		given :
		Requirement root = newRequirement("root")
		root.notifyAssociatedWithProject(mockFactory.mockProject())
		["bob", "mike", "robert"].each { root.addContent newRequirement(it) }

		when :
		root.addContent newRequirement("bob")

		then :
		thrown(DuplicateNameException)
	}


	def newRequirement(String name){
		return new Requirement(new RequirementVersion(name:name))
	}


}

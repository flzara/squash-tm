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
package org.squashtest.tm.domain.execution

import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.domain.execution.ExecutionStatus.BLOCKED
import static org.squashtest.tm.domain.execution.ExecutionStatus.ERROR
import static org.squashtest.tm.domain.execution.ExecutionStatus.FAILURE
import static org.squashtest.tm.domain.execution.ExecutionStatus.NOT_FOUND
import static org.squashtest.tm.domain.execution.ExecutionStatus.NOT_RUN
import static org.squashtest.tm.domain.execution.ExecutionStatus.READY
import static org.squashtest.tm.domain.execution.ExecutionStatus.RUNNING
import static org.squashtest.tm.domain.execution.ExecutionStatus.SETTLED
import static org.squashtest.tm.domain.execution.ExecutionStatus.SUCCESS
import static org.squashtest.tm.domain.execution.ExecutionStatus.UNTESTABLE
import static org.squashtest.tm.domain.execution.ExecutionStatus.WARNING
import static org.squashtest.tm.domain.execution.ExecutionStatus.computeNewStatus
import static org.squashtest.tm.domain.execution.ExecutionStatus.getCanonicalStatusSet
import static org.squashtest.tm.domain.execution.ExecutionStatus.getNonTerminatedStatusSet
import static org.squashtest.tm.domain.execution.ExecutionStatus.getTerminatedStatusSet
import static org.squashtest.tm.domain.execution.ExecutionStatus.toCanonicalStatusList
import static org.squashtest.tm.domain.execution.ExecutionStatus.values

/**
 * @author Gregory Fouquet
 *
 */
class ExecutionStatusTest extends Specification {


	def "checks the constant sets"(){

		when :
		def canonical = getCanonicalStatusSet()
		def terminal = getTerminatedStatusSet()
		def nonTerminal = getNonTerminatedStatusSet()


		then :

		canonical == [
			UNTESTABLE,
			SUCCESS,
			RUNNING,
			BLOCKED,
			FAILURE,
			READY] as Set
		terminal == [
			UNTESTABLE,
			SUCCESS,
			WARNING,
			BLOCKED,
			ERROR,
			FAILURE,
			SETTLED,
			NOT_RUN,
			NOT_FOUND] as Set
		nonTerminal == [RUNNING, READY] as Set
	}


	def "should turn a list of ExecutionStatus to canonical status list"(){

		given :
		def nonCanon = [
			RUNNING,
			ERROR,
			BLOCKED,
			WARNING
		]

		when :
		def canon = toCanonicalStatusList(nonCanon)

		then :
		canon == [
			RUNNING,
			BLOCKED,
			BLOCKED,
			SUCCESS] as List
	}


	@Unroll("i18n key of #status should be '#key'")
	def "should return i18n key"() {
		when:
		def actualKey = status.i18nKey

		then:
		actualKey == key

		where:
		status  	| key
		UNTESTABLE  | "execution.execution-status.UNTESTABLE"
		BLOCKED 	| "execution.execution-status.BLOCKED"
		FAILURE 	| "execution.execution-status.FAILURE"
		SUCCESS 	| "execution.execution-status.SUCCESS"
		RUNNING 	| "execution.execution-status.RUNNING"
		READY   	| "execution.execution-status.READY"
		ERROR	    | "execution.execution-status.ERROR"
		WARNING	    | "execution.execution-status.WARNING"
		NOT_FOUND   | "execution.execution-status.NOT_FOUND"
		NOT_RUN   | "execution.execution-status.NOT_RUN"
	}

	def "a report with blocked statuses should produce a BLOCKED status"(){
		given :
		ExecutionStatusReport report = new ExecutionStatusReport()
		values().each { report.set(it, 3) }

		expect :
		computeNewStatus(report) == BLOCKED
	}

	def "a non-blocked report with error statuses should produce a FAILURE status"(){

		given :
		ExecutionStatusReport report = new ExecutionStatusReport()
		values().each { report.set(it, 3) }
		report.set(BLOCKED, 0)

		expect :
		FAILURE == computeNewStatus(report)
	}

	def "a non-blocked, non error, non not_run report with failure statuses should produce a FALURE status"(){
		given :
		ExecutionStatusReport report = new ExecutionStatusReport()
		values().each { report.set(it, 3) }
		report.set(BLOCKED, 0)
		report.set(ERROR, 0)
		report.set(NOT_RUN, 0)

		expect :
		computeNewStatus(report) == FAILURE
	}

	def "should compute new status RUNNING"(){
		given :
		ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(SUCCESS, 3)
		report.set(WARNING, 3)
		report.set(RUNNING, 3)
		report.set(READY, 3)
		report.set(SETTLED, 3)

		expect :
		computeNewStatus(report) == RUNNING
	}

	@Unroll
	def "should compute new status #expected from #statuses"(){
		given :
		ExecutionStatusReport report = new ExecutionStatusReport()
		statuses.each { report.set(it, 3) }

		expect :
		computeNewStatus(report) == expected

		where:
		statuses                    | expected
		[SUCCESS, BLOCKED]   		| BLOCKED
		[SUCCESS, FAILURE]          | FAILURE
		[SUCCESS, SETTLED, UNTESTABLE] | SUCCESS
		[SUCCESS, SETTLED] 			| SUCCESS
		[SUCCESS]          			| SUCCESS
		[SETTLED]                   | SETTLED
		[SETTLED, UNTESTABLE]       | SETTLED
		[UNTESTABLE]                | UNTESTABLE
		[SUCCESS, SETTLED, READY] 	| RUNNING
		[RUNNING, RUNNING, RUNNING] | RUNNING
	}

	def "should never invoke resolveStatus on non canon status when using plublic methods"(){
		expect :
		FAILURE == WARNING.deduceNewStatus(FAILURE, RUNNING)
	}

	def "should crash when trying to resolveStatus on non canon status through class-protected methods"(){
		when :
		WARNING.resolveStatus(FAILURE, RUNNING)
		then :
		thrown(UnsupportedOperationException)
	}

	def "should compute statuses count"() {
		given:
		def count = { it.name().length() }

		ExecutionStatusReport report = new ExecutionStatusReport()
		values().each { report.set(it, count(it)) }

		and:
		def expectedCount = values().inject(0) { sum, elem -> sum + count(elem) } // "inject" means "reduce"

		expect:
		expectedCount == report.total
	}

	@Unroll
	def "should have #expected"() {
		given:
		ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(expected, 10)

		expect:
		report.has(expected)

		where:
		expected << values()
	}

	@Unroll
	def "should all be #queried : #expected"() {
		given:
		ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(BLOCKED, 10)
		report.set(UNTESTABLE, 10)
		report.set(SETTLED, 10)

		expect:
		expected == report.allOf(queried as ExecutionStatus[])

		where:
		queried | expected
		[]      | false
		[BLOCKED] | false
		[BLOCKED, UNTESTABLE, SETTLED] | true
		[BLOCKED, UNTESTABLE, SETTLED, SETTLED, UNTESTABLE] | true
		[BLOCKED, UNTESTABLE, SETTLED, READY] | true
		[READY] | false

	}

	@Unroll
	def "should there have any of #queried : #expected"() {
		given:
		ExecutionStatusReport report = new ExecutionStatusReport()
		report.set(BLOCKED, 10)
		report.set(UNTESTABLE, 10)
		report.set(SETTLED, 10)

		expect:
		expected == report.anyOf(queried as ExecutionStatus[])

		where:
		queried   | expected
		[]        | false
		[BLOCKED] | true
		[BLOCKED, UNTESTABLE, SETTLED] | true
		[BLOCKED, UNTESTABLE, SETTLED, SETTLED, UNTESTABLE] | true
		[BLOCKED, UNTESTABLE, SETTLED, READY] | true
		[READY]   | false

	}

}

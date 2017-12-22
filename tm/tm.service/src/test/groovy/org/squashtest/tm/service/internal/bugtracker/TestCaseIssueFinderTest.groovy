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
package org.squashtest.tm.service.internal.bugtracker

import com.google.common.collect.Multimap
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.bugtracker.definition.RemoteIssue
import org.squashtest.tm.domain.bugtracker.Issue
import org.squashtest.tm.domain.bugtracker.IssueOwnership
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.service.internal.repository.BugTrackerDao
import spock.lang.Specification

/**
 * @author Gregory Fouquet
 * @since 1.14.0  04/04/16
 */
class TestCaseIssueFinderTest extends Specification {
	TestCaseIssueFinder finder = new TestCaseIssueFinder();
	BugTrackerDao bugTrackerDao = Mock()

	def setup() {
		finder.bugTrackerDao = bugTrackerDao
	}

	def "should map pairs by bugtracker"() {
		given:
		def ex1 = new Execution()
		def ex2 = new Execution()
		def ex3 = new Execution()

		def pairs = [
			new Pair(ex1, new Issue()),
			new Pair(ex2, new Issue()),
			new Pair(ex2, new Issue()),
			new Pair(ex3, new Issue())
		]

		and:
		def bt1 = new BugTracker();
		def bt2 = new BugTracker();

		// /!\ without parenthesis around map key, key will be a string literal !
		def bugtrackerByExecution = [(ex1): bt1, (ex2): bt2, (ex3): bt2]

		when:
		Multimap res = finder.mapPairsByBugTracker(pairs, bugtrackerByExecution)

		then:
		res.get(bt1) == [pairs[0]]
		res.get(bt2) == [pairs[1], pairs[2], pairs[3]]
		res.asMap().size() == 2
	}

	def "should map bugtracker by execution"() {
		given:
		def ex1 = new Execution()
		def ex2 = new Execution()
		def ex3 = new Execution()
		def execs = [ex1, ex2, ex3] as Set

		and:
		def bt1 = new BugTracker()
		def bt2 = new BugTracker()
		bugTrackerDao.findAllPairsByExecutions(execs) >> [new Pair(ex1, bt1), new Pair(ex2, bt2), new Pair(ex3, bt2)]

		when:
		def res = finder.mapBugtrackerByExecution(execs)

		then:
		res[ex1] == bt1
		res[ex2] == bt2
		res[ex3] == bt2
		res.size() == 3

	}

	def "should sort ownership as pairs"() {
		given:
		def ex = [new Execution(), new Execution(), new Execution()]
		def ish = [new Issue(), new Issue(), new Issue(), new Issue()]

		def pairs = [
			new Pair(ex[0], ish[0]),
			new Pair(ex[1], ish[1]),
			new Pair(ex[1], ish[2]),
			new Pair(ex[2], ish[3])
		]

		and:
		def expected = [
			new IssueOwnership(new RemoteIssueDecorator(Mock(RemoteIssue), 0), ex[0]),
			new IssueOwnership(new RemoteIssueDecorator(Mock(RemoteIssue), 1), ex[1]),
			new IssueOwnership(new RemoteIssueDecorator(Mock(RemoteIssue), 3), ex[2])
		]

		def owns = [
			(pairs[1]): expected[1],
			(pairs[0]): expected[0],
			(pairs[3]): expected[2]
		]

		when:
		def res = finder.sortOwnershipsAsPairs(owns, pairs)

		then:
		res == expected
	}

	def "should coerce pairs into ownerchips"() {
		given:
		def ex = [new Execution(), new Execution(), new Execution()]
		def ish = [
			new Issue(id: 0, remoteIssueId: "00"),
			new Issue(id: 1, remoteIssueId: "10"),
			new Issue(id: 2, remoteIssueId: "20"),
			new Issue(id: 3, remoteIssueId: "30")
		]

		def pair = [
			new Pair(ex[0], ish[0]),
			new Pair(ex[1], ish[1]),
			new Pair(ex[1], ish[2]),
			new Pair(ex[2], ish[3])
		]

		and:
		def remote = [
			Mock(RemoteIssue),
			Mock(RemoteIssue),
			Mock(RemoteIssue),
			Mock(RemoteIssue)
		]

		def remoteById = [
			"00": remote[0],
			"10": remote[1],
			"20": remote[2],
			"30": remote[3]
		]

		when:
		def res = finder.coerceIntoIssueOwnerships(pair, remoteById);

		then:
		res[pair[0]].execution == ex[0]
		res[pair[1]].execution == ex[1]
		res[pair[2]].execution == ex[1]
		res[pair[3]].execution == ex[2]

		res[pair[0]].issue.issue == remote[0]
		res[pair[1]].issue.issue == remote[1]
		res[pair[2]].issue.issue == remote[2]
		res[pair[3]].issue.issue == remote[3]

		res.size() == 4
	}
}

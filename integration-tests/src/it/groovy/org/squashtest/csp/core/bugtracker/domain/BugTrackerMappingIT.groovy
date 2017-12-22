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
package org.squashtest.csp.core.bugtracker.domain

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitMappingSpecification;

/**
 * @author Gregory Fouquet
 *
 */
class BugTrackerMappingIT extends DbunitMappingSpecification {
	def "[Issue 3928] should persist a bugtracker"() {
		given:
		BugTracker bt = new BugTracker(url: "http://foo/bar", name: "foo", kind: "bar")

		when:
		persistFixture bt
		
		BugTracker bt2 = doInTransaction{session -> session.get(BugTracker, bt.id)}

		then:
		bt2 != null
		bt2.name == "foo"
		
		cleanup :
		deleteFixture bt
	}

}

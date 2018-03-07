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
package test

import org.squashtest.tm.domain.milestone.Milestone
import org.squashtest.tm.web.internal.controller.milestone.MilestoneController
import spock.lang.Specification

/**
 * @author Bertillon Flessel
 *
 */
class CompareDateTest extends Specification {
	MilestoneController milestoneController = new MilestoneController()

	def "should compare rightfully 2 milestones by their dates"() {
		given:

		Milestone o1 = Mock()
		Milestone o2 = Mock()

		Date date = new Date()
		o1.getEndDate() >> date
		o2.getEndDate() >> date

		when:

		int result = MilestoneController.COMPARATOR.compare(o1, o2)

		then:

		result == 0;
	}


}

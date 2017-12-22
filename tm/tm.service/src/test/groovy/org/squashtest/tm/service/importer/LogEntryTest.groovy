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
package org.squashtest.tm.service.importer

import spock.lang.Specification

import static org.squashtest.tm.service.importer.ImportStatus.*

/**
 * @author Gregory Fouquet
 *
 */
class LogEntryTest extends Specification {
	def "should create failure entry"() {
		given:
		Target t = Mock()
		String msg = "foo"
		String arg1 = "arg1"
		String arg2 = "arg2"

		when:
		def entry = LogEntry.failure().forTarget(t).atLine(10).withMessage(msg, arg1, arg2).build()

		then:
		entry.status == ImportStatus.FAILURE
		entry.target == t
		entry.line == 10
		entry.i18nError == msg
		entry.errorArgs == [arg1, arg2]

	}

	def "should create warning entry"() {
		given:
		Target t = Mock()
		String msg = "foo"
		String arg1 = "arg1"
		String arg2 = "arg2"

		when:
		def entry = LogEntry.warning().forTarget(t).atLine(10).withMessage(msg, arg1, arg2).build()

		then:
		entry.status == ImportStatus.WARNING
		entry.target == t
		entry.line == 10
		entry.i18nError == msg
		entry.errorArgs == [arg1, arg2]

	}


	def "should compare nicely with each others"() {

		given:
		def s11 = logentry(1, OK)
		def s12 = logentry(1, FAILURE)
		def s13 = logentry(1, WARNING)
		def s14 = logentry(1, WARNING)

		def s21 = logentry(5, OK)

		def s31 = logentry(17, FAILURE)
		def s32 = logentry(17, WARNING)

		and:
		def randomThenSorted = [s31, s13, s21, s14, s12, s32, s11]

		when:
		Collections.sort(randomThenSorted)

		then:
		randomThenSorted == [s11, s12, s13, s14, s21, s31, s32] ||
			randomThenSorted == [s11, s12, s14, s13, s21, s31, s32] // s13.comparesTo(s14) == 0 so they can be ordered either way,
		// yet they are not equal

		// upset reviewer said : LogEntry.compareTo is not correctly implemented (see comment in compareTo)
		// yet this broken implementation is described as "goot enough" with no further explanation.
		// I guess the above "OR" in assertion does not qualifies as "good enough" a test, yet i'm clueless
		// about the author's intent...

	}


	def logentry(Integer line, ImportStatus status) {
		return LogEntry.status(status).atLine(line).build();
	}

}

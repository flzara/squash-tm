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

import org.squashtest.tm.service.internal.batchimport.DatasetTarget
import spock.lang.Specification

import static org.squashtest.tm.service.importer.EntityType.DATASET
import static org.squashtest.tm.service.importer.ImportStatus.*

class ImportLogTest extends Specification {


	ImportLog logs
	Integer counter = 0;


	def setup() {
		logs = new ImportLog()
	}

	/*
	 * The following methods are all tests for the method #packLogs()
	 *
	 *
	 */

	def "for one imported line, should leave a single entry with status OK alone"() {

		given:
		def s11 = logentry(1, OK)

		and:
		logs.addLogEntry s11

		when:
		logs.packLogs()

		then:
		logs.findAllFor(DATASET).collect { it.status.name() } == ["OK"]

	}

	def "for one imported line, should remove multiple status OK if redudancy occured"() {

		given:
		def s11 = logentry(1, OK)
		def s12 = logentry(1, OK)


		and:
		logs.addLogEntry s11
		logs.addLogEntry s12

		when:
		logs.packLogs()

		then:
		logs.findAllFor(DATASET).collect { it.status.name() } == ["OK"]

	}


	def "should remove all the OK statuses because there are more (non ok) statuses"() {

		given:
		def s51 = logentry(5, OK)
		def s52 = logentry(5, OK)
		def s53 = logentry(5, OK)
		def s54 = logentry(5, FAILURE)
		def s55 = logentry(5, FAILURE)
		def s56 = logentry(5, WARNING)
		def s57 = logentry(5, WARNING)

		and:
		logs.logEntriesPerType.putAll EntityType.DATASET, [s51, s52, s53, s54, s55, s56, s57]

		when:
		logs.packLogs()


		then:
		logs.findAllFor(DATASET).collect { it.status.name() } == ["FAILURE", "FAILURE", "WARNING", "WARNING"]
	}


	def "should pack all the log entries for the dataset sheet (ie remove unecessary entries with status OK)"() {

		given:

		// entry 1 : case of 1 unique OK
		def s11 = logentry(1, OK)

		// entry 2 : case of 3 OK
		def s21 = logentry(2, OK)
		def s22 = logentry(2, OK)
		def s23 = logentry(2, OK)

		// entry 3 : case of 0 OK and 1 FAILURE
		def s33 = logentry(3, FAILURE)

		// entry 4 : case of 1 OK, 1 FAILURE and 1 WARNING
		def s41 = logentry(4, OK)
		def s42 = logentry(4, FAILURE)
		def s43 = logentry(4, WARNING)

		// entry 5 : case of 3 OK, 2 FAILURE and 2 WARNING

		def s51 = logentry(5, OK)
		def s52 = logentry(5, OK)
		def s53 = logentry(5, OK)
		def s54 = logentry(5, FAILURE)
		def s55 = logentry(5, FAILURE)
		def s56 = logentry(5, WARNING)
		def s57 = logentry(5, WARNING)

		and:
		logs.logEntriesPerType.putAll EntityType.DATASET,
			[s11,
			 s21, s22, s23,
			 s33,
			 s41, s42, s43,
			 s51, s52, s53, s54, s55, s56, s57
			]

		when:
		logs.packLogs()

		then:
		logs.findAllFor(EntityType.DATASET).collect { it.line + " : " + it.status.name() } ==
			["1 : OK",
			 "2 : OK",
			 "3 : FAILURE",
			 "4 : FAILURE",
			 "4 : WARNING",
			 "5 : FAILURE",
			 "5 : FAILURE",
			 "5 : WARNING",
			 "5 : WARNING",
			]


	}


	def logentry(Integer line, ImportStatus status) {
		return LogEntry.status(status).forTarget(new DatasetTarget()).atLine(line).withMessage("${counter++}").build();
	}

}

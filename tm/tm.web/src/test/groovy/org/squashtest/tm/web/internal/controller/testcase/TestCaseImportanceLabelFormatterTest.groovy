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
package org.squashtest.tm.web.internal.controller.testcase;


import static org.squashtest.tm.domain.testcase.TestCaseImportance.*
import org.springframework.context.MessageSource
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter

import spock.lang.Specification
import spock.lang.Unroll


/**
 * @author Gregory Fouquet
 *
 */
class TestCaseImportanceLabelFormatterTest extends Specification {
	Locale locale = Locale.FRENCH
	MessageSource messageSource = Mock()
	LevelLabelFormatter formatter = new LevelLabelFormatter(messageSource)
		
	@Unroll("#item should be formatted as '#expectedLabel'")
	def "TestCaseImportance item should be formatted as '<item level> - <localized item message>'"() {
		given:
		formatter.useLocale(locale)
		
		and:
		messageSource.getMessage(_, _, locale) >> { code, args, locale -> code }
		
		when:
		def res = formatter.formatLabel(item)
		
		then:
		res == expectedLabel
		
		where:
		item      | expectedLabel
		VERY_HIGH | "1-test-case.importance.VERY_HIGH"
		HIGH      | "2-test-case.importance.HIGH"
		MEDIUM    | "3-test-case.importance.MEDIUM"
		LOW       | "4-test-case.importance.LOW"
	}
	def "should encode html entities"() {
		given:
		messageSource.getMessage(_, _,_) >> { "môdits àccents" }
		
		when:
		def res = formatter.formatLabel(VERY_HIGH)
		
		then:
		res == "1-m&ocirc;dits &agrave;ccents"		
	}
}

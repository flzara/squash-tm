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
package org.squashtest.tm.domain.campaign

import java.text.SimpleDateFormat
import javax.validation.ConstraintValidatorContext
import org.squashtest.tm.domain.campaign.ScheduledTimePeriod
import org.squashtest.tm.validation.validator.ActualTimePeriodIsActualPeriodValidValidator
import org.squashtest.tm.validation.validator.ScheduledTimePeriodIsScheduledPeriodValidValidator

import spock.lang.Specification
import spock.lang.Unroll;

class ActualTimePeriodIsActualPeriodValidValidatorTest extends Specification {

	@Unroll("StartDate #actualStartDate with actualStartAuto #actualStartAuto and EndDate #actualEndDate with actualEndAuto #actualEndAuto should be consistent : #isConsistent")
	def "should accept / reject some dates"() {
		
		given:
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy")
			ActualTimePeriodIsActualPeriodValidValidator validator = new ActualTimePeriodIsActualPeriodValidValidator()
			ConstraintValidatorContext context = Mock()
			
			ActualTimePeriod period = new ActualTimePeriod()
			period.actualStartAuto = actualStartAuto
			period.actualEndAuto = actualEndAuto
			if(actualStartDate != null)
				period.actualStartDate = format.parse(actualStartDate)
			if(actualEndDate != null)
				period.actualEndDate = format.parse(actualEndDate)		
		when:
			boolean result = validator.isValid(period, context)
		then:
			result == isConsistent
		
		where:
			actualStartAuto | actualEndAuto | actualStartDate | actualEndDate | isConsistent
			true 			| true 			| "01/01/2015"    | "01/01/2010"  | true
			true 			| false			| "01/01/2015"    | "01/01/2010"  | true
			false 			| true			| "01/01/2015"    | "01/01/2010"  | true
			false 			| false			| null			  | null		  | true
			false 			| false			| null			  | "01/01/2015"  | true
			false 			| false			| "01/01/2010"	  | null		  | true
			false 			| false			| "01/01/2010"    | "01/01/2015"  | true
			false 			| false			| "01/01/2010"    | "01/01/2000"  | false
	}
}

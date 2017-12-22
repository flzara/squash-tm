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
package org.squashtest.tm.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.squashtest.tm.domain.campaign.ScheduledTimePeriod;
import org.squashtest.tm.validation.constraint.IsScheduledPeriodValid;

/**
 * @author Alix Pierre
 * @author Johan Lor
 *
 */

public class ScheduledTimePeriodIsScheduledPeriodValidValidator implements ConstraintValidator<IsScheduledPeriodValid, ScheduledTimePeriod>{

	@Override
	public void initialize(IsScheduledPeriodValid constraintAnnotation) {
		// NOOP
		
	}

	/**
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(ScheduledTimePeriod scheduledTimePeriod, ConstraintValidatorContext context) {
		
		boolean result = true;
		if(scheduledTimePeriod.getScheduledStartDate() != null && scheduledTimePeriod.getScheduledEndDate() !=null) {
			if(scheduledTimePeriod.getScheduledStartDate().getTime() > scheduledTimePeriod.getScheduledEndDate().getTime()) {
				result = false;
			}
		}
		return result;
	}

}

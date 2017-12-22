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

import org.squashtest.tm.domain.campaign.ActualTimePeriod;
import org.squashtest.tm.validation.constraint.IsActualPeriodValid;

/**
 * @author Alix Pierre
 * @author Johan Lor
 *
 */

public class ActualTimePeriodIsActualPeriodValidValidator implements ConstraintValidator<IsActualPeriodValid, ActualTimePeriod>{

	@Override
	public void initialize(IsActualPeriodValid constraintAnnotation) {
		// NOOP
		
	}

	/**
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(ActualTimePeriod actualTimePeriod, ConstraintValidatorContext context) {
		boolean result = true;
		if(!actualTimePeriod.isActualStartAuto() && !actualTimePeriod.isActualEndAuto()
				&& actualTimePeriod.getActualStartDate() != null && actualTimePeriod.getActualEndDate() !=null) {
			if(actualTimePeriod.getActualStartDate().getTime() > actualTimePeriod.getActualEndDate().getTime()) {
				result = false;
			}
		}
		return result;
	}

}

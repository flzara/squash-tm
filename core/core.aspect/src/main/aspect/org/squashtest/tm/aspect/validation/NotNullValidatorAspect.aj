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
package org.squashtest.tm.aspect.validation;

import org.squashtest.tm.core.foundation.exception.NullArgumentException;
import javax.validation.constraints.NotNull;

/**
 * Validates that a passed method argument does not break a @NonNull constraint. Validates up to third argument only (if
 * more than 3 args, consider refactoring !)
 * 
 * @author Gregory Fouquet
 * 
 */
public aspect NotNullValidatorAspect {
	pointcut methodExpectingNonNullFirstArg(Object arg):
		execution(* *(@NotNull (*), ..)) && args(arg, ..);

	pointcut methodExpectingNonNullSecondArg(Object arg):
		execution(* *(*,@NotNull (*), ..)) && args(*, arg, ..);

	pointcut methodExpectingNonNullThirdArg(Object arg):
		execution(* *(*,*, @NotNull (*), ..)) && args(*, *, arg, ..);

	before(Object arg) : methodExpectingNonNullFirstArg(arg) {
		validateArg(arg, "first arg");
	}

	before(Object arg) : methodExpectingNonNullSecondArg(arg) {
		validateArg(arg, "second arg");
	}

	before(Object arg) : methodExpectingNonNullThirdArg(arg) {
		validateArg(arg, "third arg");
	}

	private void validateArg(Object arg, String argName) {
		if (arg == null) {
			throw new NullArgumentException(argName);
		}
	}
}

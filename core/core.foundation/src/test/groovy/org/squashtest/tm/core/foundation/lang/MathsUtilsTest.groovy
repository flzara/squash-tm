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
package org.squashtest.tm.core.foundation.lang;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class MathsUtilsTest extends Specification {

	@Unroll
	def "percent should be #prog"() {
		expect: MathsUtils.percent(part, total) == prog
		where:
		part	| total	| prog
		0		| 4		| 0
		1		| 4		| 25
		2		| 4		| 50
		3		| 4		| 75
		4		| 4		| 100
		1		| 3		| 33
		2		| 3		| 67
		10		| 30	| 33
		11		| 30	| 37
		12		| 30	| 40
		13		| 30	| 43
		20		| 30	| 67
	}
}

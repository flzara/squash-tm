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

import java.math.BigDecimal;

/**
 * @author Gregory Fouquet
 *
 */
public final class MathsUtils {
	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private MathsUtils() {
		super();
	}

	/**
	 * Computes the rate part / total in percent *without any floating point error* (hopefully)
	 *
	 * @param part part
	 * @param total should not be 0
	 * @return int
	 * @throws ArithmeticException when total is 0
	 */
	public static int percent(long part, long total) throws ArithmeticException {
		return BigDecimal.valueOf(part)
				.divide(BigDecimal.valueOf(total), 2, BigDecimal.ROUND_HALF_UP)
				.multiply(HUNDRED)
				.intValue();
	}
}

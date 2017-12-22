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
package org.squashtest.tm.domain.chart;

import static org.squashtest.tm.domain.chart.Operation.AVG;
import static org.squashtest.tm.domain.chart.Operation.BETWEEN;
import static org.squashtest.tm.domain.chart.Operation.BY_DAY;
import static org.squashtest.tm.domain.chart.Operation.BY_WEEK;
import static org.squashtest.tm.domain.chart.Operation.BY_MONTH;
import static org.squashtest.tm.domain.chart.Operation.BY_YEAR;
import static org.squashtest.tm.domain.chart.Operation.COUNT;
import static org.squashtest.tm.domain.chart.Operation.EQUALS;
import static org.squashtest.tm.domain.chart.Operation.GREATER;
import static org.squashtest.tm.domain.chart.Operation.GREATER_EQUAL;
import static org.squashtest.tm.domain.chart.Operation.IN;
import static org.squashtest.tm.domain.chart.Operation.LIKE;
import static org.squashtest.tm.domain.chart.Operation.LOWER;
import static org.squashtest.tm.domain.chart.Operation.LOWER_EQUAL;
import static org.squashtest.tm.domain.chart.Operation.MAX;
import static org.squashtest.tm.domain.chart.Operation.MIN;
import static org.squashtest.tm.domain.chart.Operation.NONE;
import static org.squashtest.tm.domain.chart.Operation.NOT_EQUALS;
import static org.squashtest.tm.domain.chart.Operation.SUM;

import java.util.Arrays;
import java.util.EnumSet;

public enum ColumnRole {

	// @formatter:off
	AXIS (BY_DAY, BY_WEEK, BY_MONTH, BY_YEAR, NONE),
	MEASURE(AVG, COUNT, MIN, MAX, SUM),
	FILTER (BETWEEN, EQUALS, GREATER, GREATER_EQUAL, LIKE, LOWER, LOWER_EQUAL, IN, NOT_EQUALS);
	// @formatter:on

	private EnumSet<Operation> operations;

	private ColumnRole(Operation... operations) {
		this.operations = EnumSet.copyOf(Arrays.asList(operations));
	}

	public EnumSet<Operation> getOperations() {
		return EnumSet.copyOf(operations);
	}



}

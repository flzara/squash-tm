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
package org.squashtest.tm.domain.query;

import org.squashtest.tm.domain.query.Operation;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static org.squashtest.tm.domain.query.Operation.AVG;
import static org.squashtest.tm.domain.query.Operation.BETWEEN;
import static org.squashtest.tm.domain.query.Operation.BY_DAY;
import static org.squashtest.tm.domain.query.Operation.BY_MONTH;
import static org.squashtest.tm.domain.query.Operation.BY_WEEK;
import static org.squashtest.tm.domain.query.Operation.BY_YEAR;
import static org.squashtest.tm.domain.query.Operation.COUNT;
import static org.squashtest.tm.domain.query.Operation.EQUALS;
import static org.squashtest.tm.domain.query.Operation.GREATER;
import static org.squashtest.tm.domain.query.Operation.GREATER_EQUAL;
import static org.squashtest.tm.domain.query.Operation.IN;
import static org.squashtest.tm.domain.query.Operation.IS_NULL;
import static org.squashtest.tm.domain.query.Operation.LIKE;
import static org.squashtest.tm.domain.query.Operation.LOWER;
import static org.squashtest.tm.domain.query.Operation.LOWER_EQUAL;
import static org.squashtest.tm.domain.query.Operation.MAX;
import static org.squashtest.tm.domain.query.Operation.MIN;
import static org.squashtest.tm.domain.query.Operation.NONE;
import static org.squashtest.tm.domain.query.Operation.NOT_EQUALS;
import static org.squashtest.tm.domain.query.Operation.NOT_NULL;
import static org.squashtest.tm.domain.query.Operation.SUM;

public enum DataType {

	// @formatter:off
	NUMERIC (AVG, BETWEEN, COUNT, EQUALS, GREATER, GREATER_EQUAL, LOWER, LOWER_EQUAL, MAX, MIN, SUM, NONE, NOT_EQUALS),
	STRING (EQUALS, LIKE, COUNT, NONE),
	DATE (BETWEEN, COUNT, EQUALS, GREATER, GREATER_EQUAL, LOWER, LOWER_EQUAL, BY_DAY, BY_WEEK, BY_MONTH, BY_YEAR, NOT_EQUALS),
	DATE_AS_STRING (BETWEEN, COUNT, EQUALS, GREATER, GREATER_EQUAL, LOWER, LOWER_EQUAL, BY_DAY, BY_MONTH, BY_YEAR, NOT_EQUALS),
	EXISTENCE(NOT_NULL, IS_NULL),
	BOOLEAN(EQUALS, COUNT, NONE),
	BOOLEAN_AS_STRING(EQUALS, COUNT, NONE),
	LEVEL_ENUM(EQUALS, IN, COUNT, NONE),
	REQUIREMENT_STATUS (EQUALS, IN, COUNT, NONE),
	EXECUTION_STATUS (EQUALS, IN, COUNT, NONE),
	LIST(EQUALS, IN, COUNT, NONE),
	INFO_LIST_ITEM (EQUALS, IN, COUNT, NONE),
	TAG (EQUALS, IN, COUNT, NONE);
	// @formatter:on

	private EnumSet<Operation> operations;

	private DataType(Operation... operations) {
		this.operations = EnumSet.copyOf(Arrays.asList(operations));
	}


	public Set<Operation> getOperations() {
		return EnumSet.copyOf(operations);
	}

}

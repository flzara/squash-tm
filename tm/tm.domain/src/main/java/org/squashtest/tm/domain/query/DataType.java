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
import static org.squashtest.tm.domain.query.Operation.FULLTEXT;
import static org.squashtest.tm.domain.query.Operation.GREATER;
import static org.squashtest.tm.domain.query.Operation.GREATER_EQUAL;
import static org.squashtest.tm.domain.query.Operation.IN;
import static org.squashtest.tm.domain.query.Operation.IS_CLASS;
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

/**
 * <p>The datatypes of the column prototypes. Optionally, as parameters they receive the set of operations that applies to them.</p>
 *
 * <p> Exceptions :</p>
 * <ul> 
 * 	<li>Operation S_MATCHES for STRING : It's not listed in yet because otherwise it would appear in
 * the ChartWizard. We can fix the wizard configuration page to filter it out, but we will do so
 * at a less hurried time.
 * 	</li>
 *
 * </ul>
 *
 */
public enum DataType {

	// @formatter:off
	NUMERIC(AVG, BETWEEN, COUNT, EQUALS, GREATER, GREATER_EQUAL, LOWER, LOWER_EQUAL, MAX, MIN, SUM, NONE, NOT_EQUALS),
	STRING(EQUALS, LIKE, COUNT, NONE),
	DATE(BETWEEN, COUNT, EQUALS, GREATER, GREATER_EQUAL, LOWER, LOWER_EQUAL, BY_DAY, BY_WEEK, BY_MONTH, BY_YEAR, NOT_EQUALS),
	DATE_AS_STRING(BETWEEN, COUNT, EQUALS, GREATER, GREATER_EQUAL, LOWER, LOWER_EQUAL, BY_DAY, BY_MONTH, BY_YEAR, NOT_EQUALS),
	EXISTENCE(NOT_NULL, IS_NULL),
	BOOLEAN(EQUALS, COUNT, NONE),
	BOOLEAN_AS_STRING(EQUALS, COUNT, NONE),
	LEVEL_ENUM(EQUALS, IN, COUNT, NONE),
	REQUIREMENT_STATUS(EQUALS, IN, COUNT, NONE),
	EXECUTION_STATUS(EQUALS, IN, COUNT, NONE),
	LIST(EQUALS, IN, COUNT, NONE),
	INFO_LIST_ITEM(EQUALS, IN, COUNT, NONE),
	TAG(EQUALS, IN, COUNT, NONE),
	ENUM(EQUALS, IN, COUNT, NONE),

	// type ENTITY means that columns of that datatype represent the entity itself rather than one of its attributes.
	// IN, COUNT, NONE were not used for the type ENTITY
	ENTITY(IS_CLASS/*, IN, COUNT, NONE*/),
	// @formatter:on

	TEXT(LIKE, FULLTEXT, NONE);


	private EnumSet<Operation> operations;

	private DataType(Operation... operations) {
		this.operations = EnumSet.copyOf(Arrays.asList(operations));
	}


	public Set<Operation> getOperations() {
		return EnumSet.copyOf(operations);
	}


	/**
	 * Returns true if this instance of DataType is congruent to a LEVEL_ENUM. It currently includes the DataTypes : 
	 * REQUIREMENT_STATUS, EXECUTION_STATUS and LEVEL_ENUM.
	 *
	 * @return
	 */
	public boolean isAssignableToLevelEnum() {
		boolean isAssignable = false;
		switch (this) {
			case REQUIREMENT_STATUS:
			case EXECUTION_STATUS:
			case LEVEL_ENUM:
				isAssignable = true;
				break;
			default:
				isAssignable = false;
				break;
		}
		return isAssignable;
	}

}

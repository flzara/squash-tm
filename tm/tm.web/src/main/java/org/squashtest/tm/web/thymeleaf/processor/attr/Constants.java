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
package org.squashtest.tm.web.thymeleaf.processor.attr;

/*
 * A few mnemonics to clarify the semantic off the Processors constructor parameters
 */
interface Constants {

	static final String MATCH_ANY_TAG = null;
	static final boolean NO_TAG_PREFIX = false;
	static final boolean REQUIRE_BOTH_DIALECT_PREFIX_AND_ATTRIBUTE = true;
	static final boolean REMOVE_PSEUDO_ATTRIBUTE_WHEN_PROCESSED = true;
}

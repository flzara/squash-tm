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
package org.squashtest.tm.domain;

public enum EntityType {
	
	// @formatter:off
	PROJECT,
	TEST_CASE_LIBRARY,
	TEST_CASE_FOLDER,
	TEST_CASE,
	TEST_CASE_STEP,
	REQUIREMENT_LIBRARY,
	REQUIREMENT_FOLDER,
	REQUIREMENT,
	REQUIREMENT_VERSION,
	CAMPAIGN_LIBRARY,
	CAMPAIGN_FOLDER,
	CAMPAIGN,
	ITERATION,
	EXECUTION,
	TEST_SUITE,
	EXECUTION_STEP,
	ISSUE,
	ITEM_TEST_PLAN,
	INFO_LIST_ITEM,
	USER,
	MILESTONE,
	AUTOMATED_TEST,
	AUTOMATED_EXECUTION_EXTENDER;
	// @formatter:on

}

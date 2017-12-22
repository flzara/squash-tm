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
package org.squashtest.tm.web.internal.controller;

/**
 * Constants to be used as request param names.
 * 
 */
public final class RequestParams {

	/**
	 * request DataTable datas
	 */
	public static final String S_ECHO_PARAM = "sEcho";

	/**
	 * multiple folder ids post param
	 */
	public static final String FOLDER_IDS = "folderIds[]";

	/**
	 * simple folder id post param
	 */
	public static final String FOLDER_ID = "folderId";

	/**
	 * multiple ids post param
	 */
	public static final String IDS = "ids[]";

	/**
	 * Dry-run some app-state-modifying request
	 */
	public static final String DRY_RUN = "dry-run";

	public static final String ITERATION_ID = "iterationId";
	public static final String TEST_SUITE_ID = "testSuiteId";
	public static final String TEST_PLAN_ITEMS_IDS = "testPlanItemsIds[]";

	public static final String REQUIREMENT_ID ="requirementId";
	public static final String RTEFORMAT = "keep-rte-format";
	public static final String PROJECT_ID = "projectId";
	public static final String NAME = "name";
	public static final String MODEL = "model";
	public static final String NODE_IDS = "nodeIds";
	public static final String CAMPAIGN_ID = "campaignId";
	public static final String EXECUTION_ID = "executionId";

	

	private RequestParams() {
		super();
	}
	
}

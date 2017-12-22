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
package org.squashtest.tm.web.internal.controller.testcase.importer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.importer.EntityType;

@Component
public class RequirementImportLogHelper extends ImportLogHelper {

	private static final String IMPORT_LOG_PREFIX = "requirement-import-log-";

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementImportLogHelper.class);

	//linked hashmap is used here because we need to preserve order to present logs tabs in the same order than in the template.
	private static final Map<String, EntityType> ENTITY_BY_TAB = new LinkedHashMap<>();

	static {
		ENTITY_BY_TAB.put("REQUIREMENT", EntityType.REQUIREMENT_VERSION);
		ENTITY_BY_TAB.put("LINK_REQ_TC", EntityType.COVERAGE);
		ENTITY_BY_TAB.put("LINK_REQ_REQ", EntityType.REQUIREMENT_LINK);
	}

	@Override
	protected String getImportLogPrefix() {
		return IMPORT_LOG_PREFIX;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected Map<String, EntityType> getEntityTypeByTab() {

		return ENTITY_BY_TAB;
	}


}

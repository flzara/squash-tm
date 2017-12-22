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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum which defines custom field pattern used in import template.
 * 
 * @author Gregory Fouquet
 * 
 */
public enum TemplateCustomFieldPattern {
	TEST_CASE_CUSTOM_FIELD("TC_CUF_"), 
	STEP_CUSTOM_FIELD("TC_STEP_CUF_"), 
	REQUIREMENT_VERSION_CUSTOM_FIELD("REQ_VERSION_CUF_"),
	NO_CUSTOM_FIELD;

	private final String prefix;

	private TemplateCustomFieldPattern() {
		prefix = null;
	}

	private TemplateCustomFieldPattern(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Parses a custom field code from a column header. Headers leading to a blank code will be considered as
	 * non-parsing and produce a <code>null</code> result.
	 * 
	 * @param header
	 * @return the field code. When the header don't parse, returns <code>null</code>.
	 */
	public String parseFieldCode(String header) {
		if (prefix == null || header == null) {
			return null;
		}
		if (header.startsWith(prefix)) {
			return StringUtils.trimToNull(header.substring(prefix.length()));
		}
		return null;
	}

}

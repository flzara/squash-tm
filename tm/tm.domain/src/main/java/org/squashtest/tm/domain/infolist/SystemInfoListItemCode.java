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
package org.squashtest.tm.domain.infolist;

import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.exception.customfield.CodeAlreadyExistsException;

public enum SystemInfoListItemCode {
	CAT_FUNCTIONAL("CAT_FUNCTIONAL"), CAT_NON_FUNCTIONAL("CAT_NON_FUNCTIONAL"), CAT_USE_CASE("CAT_USE_CASE"), CAT_BUSINESS("CAT_BUSINESS")
	, CAT_TEST_REQUIREMENT("CAT_TEST_REQUIREMENT"), CAT_UNDEFINED("CAT_UNDEFINED"), CAT_ERGONOMIC("CAT_ERGONOMIC"),
	CAT_PERFORMANCE("CAT_PERFORMANCE"), CAT_TECHNICAL("CAT_TECHNICAL"), CAT_USER_STORY("CAT_USER_STORY"), CAT_SECURITY("CAT_SECURITY"),
	NAT_UNDEFINED("NAT_UNDEFINED"), NAT_FUNCTIONAL_TESTING("NAT_FUNCTIONAL_TESTING"), NAT_BUSINESS_TESTING("NAT_BUSINESS_TESTING"),
	NAT_USER_TESTING("NAT_USER_TESTING"), NAT_NON_FUNCTIONAL_TESTING("NAT_NON_FUNCTIONAL_TESTING"),
	NAT_PERFORMANCE_TESTING("NAT_PERFORMANCE_TESTING"), NAT_SECURITY_TESTING("NAT_SECURITY_TESTING"), NAT_ATDD("NAT_ATDD"),
	TYP_UNDEFINED("TYP_UNDEFINED"), TYP_COMPLIANCE_TESTING("TYP_COMPLIANCE_TESTING"), TYP_CORRECTION_TESTING("TYP_CORRECTION_TESTING"),
	TYP_EVOLUTION_TESTING("TYP_EVOLUTION_TESTING"), TYP_REGRESSION_TESTING("TYP_REGRESSION_TESTING"),
	TYP_END_TO_END_TESTING("TYP_END_TO_END_TESTING"), TYP_PARTNER_TESTING("TYP_PARTNER_TESTING");

	private final String code;

	private SystemInfoListItemCode(String code) {
		this.code = code;
	}


	public static void verifyModificationPermission(InfoListItem item) {

		for (SystemInfoListItemCode id : SystemInfoListItemCode.values()) {
			if (id.getCode().equals(item.getCode())) {
				throw new CodeAlreadyExistsException(item.getCode(), id.getCode(), CustomField.class);

			}
		}

	}

	public String getCode() {
		return code;
	}



}

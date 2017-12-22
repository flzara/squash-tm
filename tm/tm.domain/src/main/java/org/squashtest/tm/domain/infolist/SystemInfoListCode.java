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

public enum SystemInfoListCode {
	TEST_CASE_NATURE("DEF_TC_NAT"),
	TEST_CASE_TYPE("DEF_TC_TYP"),
	REQUIREMENT_CATEGORY("DEF_REQ_CAT");

	private final String code;

	private SystemInfoListCode(String code) {
		this.code = code;
	}


	public static void verifyModificationPermission(InfoList infoList) {

		for (SystemInfoListCode id : SystemInfoListCode.values()) {
			if (id.getCode().equals(infoList.getCode())) {
				throw new IllegalAccessError("You shall not pass ! This is a system info list, go away ! Play with your own info lists");
			}
		}
	}

	public static boolean isSystem(String code) {
		for (SystemInfoListCode sys : SystemInfoListCode.values()) {
			if (sys.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSystem(InfoList list) {
		return isSystem(list.getCode());
	}

	public static boolean isNotSystem(String code) {
		return !isSystem(code);
	}

	public static boolean isNotSystem(InfoList list) {
		return !isSystem(list);
	}

	public String getCode() {
		return code;
	}


}

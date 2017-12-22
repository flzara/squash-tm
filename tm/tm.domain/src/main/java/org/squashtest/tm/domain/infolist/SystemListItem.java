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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseType;

import static org.squashtest.tm.domain.infolist.SystemListItem.SYSTEM_INFO_LIST_IDENTIFIER;


/**
 * a SystemListItem is an InfoListItem that belongs to the vanilla business originally implemented in Squash.
 * They are the now database-backed versions of the deprecated {@link RequirementCategory}, {@link TestCaseNature}
 * and {@link TestCaseType}.
 * It may have in the future a different signification, compared to the {@link UserListItem} which is
 * user-defined (as the name suggests).
 *
 *
 * @author bsiri
 *
 */

@Entity
@DiscriminatorValue(SYSTEM_INFO_LIST_IDENTIFIER)
public class SystemListItem extends InfoListItem {

	public static final String SYSTEM_INFO_LIST_IDENTIFIER = "SYS";

	public static final String SYSTEM_REQ_CATEGORY = "CAT_FUNCTIONAL";
	public static final String SYSTEM_TC_NATURE = "NAT_UNDEFINED";
	public static final String SYSTEM_TC_TYPE = "TYP_UNDEFINED";


	public SystemListItem(){
		super();
	}
}

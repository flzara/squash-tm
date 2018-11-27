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
package org.squashtest.tm.api.security.acls;


/**
 * <p>List of roles for role-based security checks that complements the ACL system.</p>
 * <p>Note : the values are stored in table CORE_GROUP_AUTHORITY.</p>
 * <p>Note 2 : we chose string over an enum type because this is essentially how Spring security expect them to be represented</p>  
 * 
 * @author bsiri
 *
 */
public interface Roles {

	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	
	public static final String ROLE_TM_PROJECT_MANAGER = "ROLE_TM_PROJECT_MANAGER";
	
	public static final String ROLE_TM_USER = "ROLE_TM_USER";
	
	public static final String ROLE_TA_API_CLIENT = "ROLE_TA_API_CLIENT";

	public static final String ROLE_TF_FUNCTIONAL_TESTER = "ROLE_TF_FUNCTIONAL_TESTER";

	public static final String ROLE_TF_AUTOMATION_PROGRAMMER = "ROLE_TF_AUTOMATION_PROGRAMMER";
	
}

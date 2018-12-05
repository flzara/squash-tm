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
package org.squashtest.tm.api.security.authentication;

import org.springframework.security.core.Authentication;

/**
 * Implementors carry more information regarding the user account. These extra information are typically hosted on the remote authentication provider and used by Squash TM when (and if) the user account must 
 * be created. Remember that account creation can occur only if the associated {@link AuthenticationProviderFeatures} permits it, eg {@link AuthenticationProviderFeatures#shouldCreateMissingUser()} is true. 
 * 
 * @author bsiri
 *
 */

/*
 * This solution for the user account information is not really satisfying but is imposed by package visibility : we cannot manipulate org.squashtest.tm.domain.users.User directly, because it is not visible to 
 * this artifact.  
 */
public interface ExtraAccountInformationAuthentication extends Authentication{

	String getFirstName();
	
	String getLastName();
	
	String getEmail();
	
}

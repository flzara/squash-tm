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

/**
 * @author Gregory Fouquet
 * 
 */
public interface AuthenticationProviderFeatures {
	/**
	 * Should return true when the authentication provider manages itself the passwords ie. passwords re not modifiable
	 * from Squash.
	 * 
	 * @return
	 */
	boolean isManagedPassword();

	/**
	 * Should return the provider name, ie the one that is configured using the "authentication.provider" property.
	 * 
	 * @return
	 */
	String getProviderName();

	/**
	 * Indicates whether Squash TM should create a (business) User when authentication is successful but no matching
	 * User can be found.
	 * 
	 * @return
	 */
	boolean shouldCreateMissingUser();
}

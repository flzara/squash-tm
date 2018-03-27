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

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;


/**
 * <p>
 * 	Third-party {@link AuthenticationProvider} may tie to the Authentication object they produce their own {@link AuthenticationProviderFeatures} with this.
 * 	When Squash is presented an FeaturesAwareAuthentication, it will try first to honor these features instead of the primary {@link AuthenticationProviderFeatures}.
 * </p> 
 * 
 *  <p>
 *   This is very useful when the said authentication provider is not the primary one (ie, not set as such in application property 'authentication.provider').
 *  </p>
 * 
 * @author bsiri
 *
 */
public interface FeaturesAwareAuthentication extends Authentication {

	/**
	 * Return the features specific to the authentication provider that created this Authentication instead of 
	 * the primary. If null, the primary will be used instead.
	 * 
	 * @return
	 */
	AuthenticationProviderFeatures getFeatures();
	
}

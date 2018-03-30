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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * <p>An AuthenticationProviderFeatures drives how Squash will behave in certain User management use-cases, eg regarding credential managements.</p>
 *  
 * <p>
 * By default Squash comes with an {@link AuthenticationManager} backed by a db-based {@link AuthenticationProvider}, which allow password management. 
 * It publishes an instance of {@link AuthenticationProviderFeatures} which name is 'internal' and states that actions like password changes 
 * are supported and permitted.  
 * </p>  
 * 
 * <p>
 * 	Third party plugins can provide alternate security contexts, namely : additional AuthenticationManagers, 
 * AuthenticationProviders, security filter chain etc. If they do so, they should provide a companion
 * {@link AuthenticationProviderFeatures} that tells whether they support the given operations.
 * </p>
 * 
 * <p>
 * 	In particular, a plugin can completely replace the global {@link AuthenticationManager} of the main security filter chain. 
 * 	If it does so, {@link AuthenticationProviderFeatures} MUST be published as a Spring Bean and the name returned by {@link #getProviderName()} 
 *  must also be set in the application property 'authentication.provider'. 	
 * </p>
 * 
 * <p>
 * 	 A plugin can merely propose a {@link AuthenticationManager} and/or {@link AuthenticationProvider} local to its own security filter chain.
 * 	 If so, it will not be considered as the main authentication provider, and thus the features of that main provider would apply by default.   
 * 	 If this is undesirable, the AuthenticationProvider can return {@link FeaturesAwareAuthentication} tokens (see documentation). 
 * </p>
 * 
 * @author Gregory Fouquet
 * @documented by bsiri
 * 
 */
public interface AuthenticationProviderFeatures {
	/**
	 * Should return true when the authentication provider manages itself the passwords ie. passwords re not modifiable
	 * from Squash. If the value is false Squash-TM will allow users/admins to change the passwords... by modifying those 
	 * stored in the database (ie even if the plugin supplied its own {@link UserDetailsService} it won't be used for that purpose).
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

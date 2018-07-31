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
 

/**
 * <p>A few guidelines about creating an authentication providing plugin. TODO : document more extensively</p>
 * 
 * <p>
 * 	Notes : 
 * </p>
 * 
 * <ul>
 <li>Read dSpring security documentation, also read the code of SecurityConfig in module tm.service</li>
 *
 * 	<li>
 * 		If your plugin is satisfied with the regular login/form entry point but want to swap how the authentication is
 * 		done behind the curtain, it can add an AuthenticationProvider to the primary AuthenticationManager. However, as
 * 		the app and it quirks stand now, If you need to	*add* new AuthenticationProvider or *replace* the primary
 * 		AuthenticationProvider by another one, you can configure the primary AuthenticationManager by doing as following:
 *
 * 		<ul>
 * 			<li>registering a GlobalAuthenticationConfigurerAdapter</li>
 * 			<li>defining the {@link AuthenticationProviderFeatures} of your provider</li>
 * 			<li>
 * 				changing the value or adding a new value to the application property 'authentication.provider'
 * 				(e.g. name of plugin). 'internal' is a reserved value used by the native AuthenticationManager of Squash.
 * 			</li>
 * 		</ul> 
 * 	</li>
 * 
 * 	<li>
 * 		If your plugin publish new endpoints for client authentication, their base url should all start by '(contextPath)/auth/your-service'. Eg '/squash/auth/your-service'.
 * 		You can then define and add your own security filter chain to the security context.  
 * 
 * 	</li>
 * 
 * </ul>
 * 
 * 
 * 
 */
package org.squashtest.tm.api.security.authentication;


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
package org.squashtest.tm.service.internal.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.api.security.authentication.FeaturesAwareAuthentication;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.security.UserContextService;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * A {@link UserContextService} backed by Spring Security.
 *
 *
 * @author Gregory Fouquet
 */
@Component("squashtest.core.user.UserContextService")
public class SpringSecurityUserContextService implements UserContextService {

	@Override
	public String getUsername() {
		return UserContextHolder.getUsername();
	}

	@Override
	public boolean hasRole(String role) {
		Collection<? extends GrantedAuthority> grantedAuths = getGrantedAuthorities();

		for (GrantedAuthority grantedAuth : grantedAuths) {
			if (grantedAuth.getAuthority().equals(role)) {
				return true;
			}
		}

		return false;
	}

	private Collection<? extends GrantedAuthority> getGrantedAuthorities() {
		Authentication principal = getAuthentication();

		Collection<? extends GrantedAuthority> grantedAuths;

		if (principal == null) {
			grantedAuths = Collections.emptyList();
		} else {
			grantedAuths = principal.getAuthorities();

		}
		return grantedAuths;
	}

	@Override
	public Authentication getAuthentication() {
		return UserContextHolder.getAuthentication();
	}
	

	@Override 
	public Optional<AuthenticationProviderFeatures> getUserContextAuthProviderFeatures(){
		Authentication auth = UserContextHolder.getAuthentication();
		Optional<AuthenticationProviderFeatures> optResult = null;
		if (auth == null || ! FeaturesAwareAuthentication.class.isAssignableFrom(auth.getClass())){
			optResult = Optional.empty();
		}
		else{
			AuthenticationProviderFeatures features = ((FeaturesAwareAuthentication)auth).getFeatures();
			optResult = (features != null) ? Optional.of(features) : Optional.empty();
		}
		return optResult;
	}
}

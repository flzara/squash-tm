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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.api.security.authentication.FeaturesAwareAuthentication;


/**
 * This class will wrap the authentication object in a {@link FeaturesAwareAuthentication}
 *
 * @author zyang
 *
 */
public class SquashDaoAuthenticationProvider extends DaoAuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication auth = super.authenticate(authentication);
		return new FeatureAwareDaoAuthenticationToken((UsernamePasswordAuthenticationToken) auth);
	}

	public static final class FeatureAwareDaoAuthenticationToken extends UsernamePasswordAuthenticationToken implements FeaturesAwareAuthentication {

		private static final long serialVersionUID = 1L;

		private FeatureAwareDaoAuthenticationToken(UsernamePasswordAuthenticationToken token) {
			super(token.getPrincipal(), token.getCredentials(), token.getAuthorities());
		}

		@Override
		public AuthenticationProviderFeatures getFeatures() {
			return InternalAuthenticationProviderFeatures.INSTANCE;
		}
	}
}

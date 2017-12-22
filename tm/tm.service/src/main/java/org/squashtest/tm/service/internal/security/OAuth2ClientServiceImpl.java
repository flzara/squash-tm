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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Service;
import org.squashtest.tm.service.security.OAuth2ClientService;

@Service("squashtest.tm.service.OAuth2ClientService")
public class OAuth2ClientServiceImpl implements OAuth2ClientService{

	private static Collection<String> DEFAULT_GRANT_TYPES = Arrays.asList("password","authorization_code","refresh_token","implicit");
	private static Collection<SimpleGrantedAuthority> DEFAULT_AUTHORITIES = Arrays.asList(new SimpleGrantedAuthority("ROLE_CLIENT"), new SimpleGrantedAuthority("ROLE_TRUSTED_CLIENT"));
	private static Collection<String> DEFAULT_SCOPE = Arrays.asList("location", "locationhistory");

	@Inject
	private JdbcClientDetailsService jdbcClientDetailsService;

	@Override
	public List<ClientDetails> findClientDetailsList() {
		return jdbcClientDetailsService.listClientDetails();
	}

	@Override
	public void addClientDetails(String name, String secret) {
		BaseClientDetails clientDetails = new BaseClientDetails();
		clientDetails.setClientId(name);
		clientDetails.setClientSecret(secret);
		clientDetails.setAuthorizedGrantTypes(DEFAULT_GRANT_TYPES);
		clientDetails.setAuthorities(DEFAULT_AUTHORITIES);
		clientDetails.setScope(DEFAULT_SCOPE);
		jdbcClientDetailsService.addClientDetails(clientDetails);
	}

	@Override
	public void removeClientDetails(String clientId) {
		jdbcClientDetailsService.removeClientDetails(clientId);
	}

	@Override
	public void changeClientSecret(String name, String newSecret) {
		BaseClientDetails clientDetails = new BaseClientDetails();
		clientDetails.setClientId(name);
		clientDetails.setClientSecret(newSecret);
		jdbcClientDetailsService.updateClientDetails(clientDetails);
	}

	@Override
	public void removeClientDetails(List<String> idList) {
		for(String clientId : idList){
			jdbcClientDetailsService.removeClientDetails(clientId);
		}
	}

	@Override
	public void addClientDetails(ClientDetails clientDetails) {
		BaseClientDetails baseClientDetails = new BaseClientDetails();
		baseClientDetails.setClientId(clientDetails.getClientId());
		baseClientDetails.setClientSecret(clientDetails.getClientSecret());
		baseClientDetails.setRegisteredRedirectUri(clientDetails.getRegisteredRedirectUri());
		baseClientDetails.setAuthorizedGrantTypes(DEFAULT_GRANT_TYPES);
		baseClientDetails.setAuthorities(DEFAULT_AUTHORITIES);
		baseClientDetails.setScope(DEFAULT_SCOPE);
		jdbcClientDetailsService.addClientDetails(baseClientDetails);
	}

	@Override
	public ClientDetails findClientDetailsById(String clientId) {
		return jdbcClientDetailsService.loadClientByClientId(clientId);
	}
}

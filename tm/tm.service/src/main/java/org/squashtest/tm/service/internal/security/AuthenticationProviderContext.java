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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.api.security.authentication.FeaturesAwareAuthentication;
import org.squashtest.tm.core.foundation.lang.Assert;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registers all {@link AuthenticationProviderFeatures} available in the context, and provides with the main instance (that corresponds to 
 * the main authentication provider). For this to work, {@link AuthenticationProviderFeatures#getProviderName()} should match the 
 * value of application property 'authentication.provider' (not to be confused with the bean name).
 * 
 * <p>Registers all the {@link AuthenticationProviderFeatures} available in the context and provides which one is relevant depending on the context.</p>
 * 
 * <ul>
 * 		<ol>
 * 			General case : return the primary AuthenticationProviderFeatures. The primary instance is returned as default value the authentication provider 
 * 			that authenticated the current user does not specify its own (see {@link AuthenticationProviderFeatures} for documentation).
 * 			Furthermore when no user context is available then the main provider features are returned as defaults.
 * 		</ol>
 * 
 * 		<ol>
 * 			Specific-case : return the instance attached to the user context if any. If there is a user context and the user was authenticated through 
 * 			a provider that declared its own features (and attached them to the Authentication object, see {@link FeaturesAwareAuthentication}), 
 * 			those features will be used instead.
 * 		</ol>
 * </ul>
 * 
 * @author Gregory Fouquet
 * @author bsiri
 * 
 */
/*
 * TODO : about registering many AuthenticationProviderFeatures even though we only use one.
 * 
 * I suspect it registers all available instances because many may exist in the bean factory simultaneously but we cannot pimpoint which 
 * one is required by bean name only, because that bean name is hard to predict. Hence the need to check for the value of #getProviderName().
 * Still it add a bit of complexity. Perhaps was it necessary with older versions of Spring Security ?
 */
//@ApplicationComponent
@Component
public class AuthenticationProviderContext {
	/**
	 * logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProviderContext.class);
	/**
	 * names of the auth provider.
	 */
	@Value("${authentication.provider:internal}")
	private String[] currentProviderNames;

	/**
	 * list of configured provider features.
	 */
	@Inject
	private List<AuthenticationProviderFeatures> providersFeatures = new ArrayList<>();
	
	private AuthenticationProviderFeatures defaultProviderFeature = DefaultAuthenticationProviderFeatures.INSTANCE;
	private boolean internalProviderEnabled = false;
	
	@Inject
	@Named("squashtest.core.user.UserContextService")
	private SpringSecurityUserContextService secService;

	/**
	 * 
	 */
	public AuthenticationProviderContext() {
		super();
	}
	
	/**
	 * <p>Returns the features supported by the Authentication provider. If the current user authenticated via 
	 * a provider that support different features than the primary they will be returned, otherwise the primary
	 * is returned. </p>
	 * 
	 * <p>
	 * 	This implementation will retrieve the Authentication informations from the security context. If there 
	 * is no Authentication available (eg, the security context is undefined at the moment), the default
	 * features will be returned.
	 * </p>
	 * 
	 * @return
	 */
	public AuthenticationProviderFeatures getCurrentProviderFeatures() {
		
		// first look for specific user features associated to the user account
		Optional<AuthenticationProviderFeatures> optFeatures = secService.getUserContextAuthProviderFeatures();
		
		if (optFeatures.isPresent()){
			return optFeatures.get();
		}
		
		// if not available, use the default provider
		else{
			return defaultProviderFeature;
		}

	}
	
	
	/**
	 * <p>Returns the features supported by the Authentication provider. If the current user authenticated via 
	 * a provider that support different features than the primary they will be returned, otherwise the primary 
	 * is returned.</p> 
	 * 
	 * <p>Unlike {@link #getCurrentProviderFeatures()}, this implementation will check the supplied Authentication
	 * object instead of retrieving it from the security context.</p>
	 * @return
	 */
	public AuthenticationProviderFeatures getProviderFeatures(Authentication authentication){
		AuthenticationProviderFeatures features = null;
		
		if (FeaturesAwareAuthentication.class.isAssignableFrom(authentication.getClass())){
			features = ((FeaturesAwareAuthentication)authentication).getFeatures();
		}
		
		return (features != null) ? features : defaultProviderFeature;
	}
	
	/**
	 * Returns the primary authentication provider features.
	 * 
	 * @return
	 */
	public AuthenticationProviderFeatures getPrimaryProviderFeatures() {
		return defaultProviderFeature;
	}
	
	
	public boolean isInternalProviderEnabled(){
		return internalProviderEnabled;
	}

	/**
	 * initialization and checks.
	 */
	@PostConstruct
	public void initializeContext() {
		checkConfiguration();
	}

	private void checkConfiguration() {

		// check that there is at least one provider configured
		if (currentProviderNames.length == 0) {
			LOGGER.error("The number of defined authentication.provider is {}", currentProviderNames.length);
			throw new IllegalStateException("currentPropertyName should not be empty");
		} 
		
		// check that there were no misspelling
		Arrays.asList(currentProviderNames).forEach(name -> Assert.propertyNotBlank(name,"currentPropertyName should not be blank" ));
		

		// check that all providers are known 
		Collection<String> knownProviders =  providersFeatures.stream().map(AuthenticationProviderFeatures::getProviderName).collect(Collectors.toList());

		for (String providerName : currentProviderNames) {

			if (! knownProviders.contains(providerName)) {

				String knownAsString = knownProviders.stream().collect(Collectors.joining(", "));

				LOGGER.error("Provider features named {} could not be found in list {}", providerName, providersFeatures);

				throw new IllegalStateException("\nAuthentication Provider named '" + providerName
					+ "' was not found. Please review the application property 'authentication.provider'. \n"
					+ "The default value 'internal' enables the  native authentication manager of Squash-TM. "
					+ "To enable extra authentication providers (eg from a plugin) please refer to the documentation. \n"
					+ "Providers currently loaded are : " + knownAsString);
			}
			else {
				LOGGER.trace("located the authentication provider features named '{}'", providerName);
			}
			
		}
		
		// is the internal provider enabled ?
		internalProviderEnabled = Arrays.asList(currentProviderNames).contains(InternalAuthenticationProviderFeatures.NAME);
				
		// TODO : why is getCurrentProviderFeatures invoked ? Removing this ought to be a quick win but two days from the release I have no time to investigate 
		getCurrentProviderFeatures();
	}

}

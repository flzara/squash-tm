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
package org.squashtest.tm.web.internal.fileupload;

import java.util.Map;
import javax.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.exceptionresolver.HandlerMaxUploadSizeExceptionResolver;


/**
 * <p>Will redirect a request to a specific MultipartResolver, with specific settings, with respect to the matched URL. This chain is
 * completely dumb and will pick the first match it finds, or the default if none was found.</p>
 * 
 * @author bsiri
 *
 */
public class MultipartResolverDispatcher extends CommonsMultipartResolver {
	
	private SquashMultipartResolver defaultResolver;
	
	private Map<String, SquashMultipartResolver> resolverMap;
        
        private ConfigurationService confService;
	
	public void setResolverMap(Map<String, SquashMultipartResolver> chain){
		this.resolverMap = chain;
	}
	
	public void setDefaultResolver(SquashMultipartResolver defaultResolver){
		this.defaultResolver = defaultResolver;
	}
	
	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {	
            
            String target = request.getRequestURI();

            for (String matcher : resolverMap.keySet()){
                    if (target.matches(matcher)){
                            return resolverMap.get(matcher).resolveMultipart(request);
                    }
            }

            //else
            return defaultResolver.resolveMultipart(request);
	}
        
        /*
        * Because the resolvers in resolverMap aren't known to Spring, they can't listen to ApplicationEvents 
        * directly. This class being the main entry point to the application and multipart résolution, 
        * it is its job to listen to those events and pass them down to the others.
        */
        @EventListener
        public void onContextReady(ContextRefreshedEvent event) {
            if (confService == null) {
                    confService = ((ContextRefreshedEvent) event).getApplicationContext()
                                    .getBean(ConfigurationService.class);                    
                
                    for (SquashMultipartResolver resolver : resolverMap.values()){
                        resolver.setConfigurationService(confService);
                        resolver.updateConfig();
                    }
            }
           
        }
        
        @EventListener
        public void onConfigChange(ConfigUpdateEvent update){
            for (SquashMultipartResolver resolver : resolverMap.values()){
               resolver.updateConfig();
            }
        }
	
	
}

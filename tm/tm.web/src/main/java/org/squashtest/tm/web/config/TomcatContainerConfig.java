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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.squashtest.tm.web.config;

import java.io.FileNotFoundException;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

/**
 * Initiated as a workaround  for http://stackoverflow.com/questions/31605129/spring-file-upload-with-multipart-resolver-causes-connection-reset-when-file-is
 *
 * @author bsiri
 */
@Configuration
public class TomcatContainerConfig {
	

    /**
     * This method defines a modified servlet container that will run Squash TM when deployed as a standalone application.
     * If Squash TM is deployed as a war, the prefered way to configure the container is via the standard file server.xml.
     * Therefore you should turn this method off by setting the property squash.run-as-war to "true".
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix="squash", name="run-as-war", havingValue = "false", matchIfMissing = true)
    public TomcatEmbeddedServletContainerFactory containerFactory(){
		
    	TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory = new CustomizedTomcatContainerFactory();

		return tomcatEmbeddedServletContainerFactory;
	}
    
    
    
    private static final class CustomizedTomcatContainerFactory extends TomcatEmbeddedServletContainerFactory {
    	
    	private static final Logger LOGGER = LoggerFactory.getLogger(CustomizedTomcatContainerFactory.class);
    	
    	
    	@Override
    	protected void customizeConnector(Connector connector) {
    		super.customizeConnector(connector);
    		if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
    			((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(-1);
    		}
    	}

    	@Override
    	protected void postProcessContext(Context context) {
    		final int cacheSize = 40 * 1024;
    		StandardRoot standardRoot = new StandardRoot(context);
    		standardRoot.setCacheMaxSize(cacheSize);
    		context.setResources(standardRoot);
    	}

    	/*
    	 * Issue 6757
    	 * 
    	 * As of Squash 1.15.1, the use of Spring Boot 1.4.0 with Tomcat 8.0.23 breaks the creation of an 
    	 * SSL connector, because Spring expected to configure a Tomcat 8.5. Specifically it supplies the 
    	 * connector with the URL of the keystore, while the connector expects a plain path to the file. 
    	 * After some boilerplate, the final path to the keystore is malformed and cannot be found.
    	 * 
    	 * This fix attempts to configure the paths to keystore and trustore, in case they are 
    	 * hosted on the filesystem. This will not work for classpath-hosted keystore, but we don't 
    	 * use that feature in Squash.
    	 * 
    	 * This fix patches the problem for the version 1.15. The proper fix for 1.16 would rather 
    	 * rely on the correct version of Tomcat, and fix what is wrong with it instead. 
    	 */
    	
    	/*
    	 * 29/05/2017 : looks like the temporary fix made it to Squash 16 too.
    	 */
    	@Override
    	protected void configureSsl(AbstractHttp11JsseProtocol<?> protocol, Ssl ssl) {
    		// first, call the super method as usual
    		super.configureSsl(protocol, ssl);
    		
    		// now, if SSL is not supplied by an internal provider, 
    		// reconfigure it using the correct path 
    		// (the following is adapted from the super method)
    		if (getSslStoreProvider() == null) {
    			configureSslKeyStore(protocol, ssl);
    			configureSslTrustStore(protocol, ssl);
    		}
    		
    	}

    	// copy pasta of super.configureSsslKeyStore
    	private void configureSslKeyStore(AbstractHttp11JsseProtocol<?> protocol, Ssl ssl) {
    		try {
    			String ksUrl = ResourceUtils.getURL(ssl.getKeyStore()).toString();
    			String ksPath = removeProtocol(ksUrl);
    			protocol.setKeystoreFile(ksPath);
    			
    			if (LOGGER.isInfoEnabled()){
    				LOGGER.info("Issue 6757 : fixed path to keystore is '"+ksPath+"'");
    			}
    		}
    		catch (FileNotFoundException ex) {
    			throw new EmbeddedServletContainerException(
    					"Could not load key store: " + ex.getMessage(), ex);
    		}
    		if (ssl.getKeyStoreType() != null) {
    			protocol.setKeystoreType(ssl.getKeyStoreType());
    		}
    		if (ssl.getKeyStoreProvider() != null) {
    			protocol.setKeystoreProvider(ssl.getKeyStoreProvider());
    		}
    	}

    	// copy pasta of super.configureSsslTrustStore
    	private void configureSslTrustStore(AbstractHttp11JsseProtocol<?> protocol, Ssl ssl) {

    		if (ssl.getTrustStore() != null) {
    			try {
    				String tsUrl = ResourceUtils.getURL(ssl.getTrustStore()).toString();
    				String tsPath = removeProtocol(tsUrl);
    				protocol.setTruststoreFile(tsPath);
    				
        			if (LOGGER.isInfoEnabled()){
        				LOGGER.info("Issue 6757 : fixed path to truststore is '"+tsPath+"'");
        			}
    			}
    			catch (FileNotFoundException ex) {
    				throw new EmbeddedServletContainerException(
    						"Could not load trust store: " + ex.getMessage(), ex);
    			}
    		}
    		protocol.setTruststorePass(ssl.getTrustStorePassword());
    		if (ssl.getTrustStoreType() != null) {
    			protocol.setTruststoreType(ssl.getTrustStoreType());
    		}
    		if (ssl.getTrustStoreProvider() != null) {
    			protocol.setTruststoreProvider(ssl.getTrustStoreProvider());
    		}
    	}

    	private String removeProtocol(String toclean){
    		// we remove file: if specified, 
    		// if protocol classpath: is used we just let it go and see whatever happens 
    		return toclean.replaceAll("(file:)", "");
    	}
    }
    
}

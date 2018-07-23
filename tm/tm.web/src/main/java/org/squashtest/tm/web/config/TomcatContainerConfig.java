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
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
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
    public TomcatServletWebServerFactory containerFactory(){

		TomcatServletWebServerFactory tomcatServerFactory = new CustomizedTomcatServerFactory();

		return tomcatServerFactory;
	}



    private static final class CustomizedTomcatServerFactory extends TomcatServletWebServerFactory  {

    	private static final Logger LOGGER = LoggerFactory.getLogger(CustomizedTomcatServerFactory.class);


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
    }

}

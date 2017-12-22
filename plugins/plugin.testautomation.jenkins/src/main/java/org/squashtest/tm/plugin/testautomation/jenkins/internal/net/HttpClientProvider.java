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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.net;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;

/*
 * TODO : have the client shutdown and disposed of when it is not needed after a certain amount
 * of time passed. See http://hc.apache.org/httpclient-3.x/performance.html#Reuse_of_HttpClient_instance
 *
 */
@Component
@SuppressWarnings("deprecation") // spring support of httpclient 3.1 is deprecated yet we heavily rely on httpclient 3.1
public class HttpClientProvider {

	private CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {
		@Override
		public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
			AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

			// If no auth scheme avaialble yet, try to initialize it
			// preemptively
			if (authState.getAuthScheme() == null) {
				AuthScheme authScheme = new BasicScheme();
				CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
				HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
				Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
				if (creds == null) {
                                    throw new HttpException("No credentials for preemptive authentication");
                                }
				authState.setAuthScheme(authScheme);
				authState.setCredentials(creds);
			}

		}
	}
        
	private final CloseableHttpClient client;

	private final ClientHttpRequestFactory requestFactory;

	public HttpClientProvider() {
		PoolingHttpClientConnectionManager  manager = new PoolingHttpClientConnectionManager();
		manager.setMaxTotal(25);

		client = HttpClients.custom()
			.setConnectionManager(manager)
			.addInterceptorFirst(new PreemptiveAuthInterceptor())
			.setDefaultCredentialsProvider(credentialsProvider)
			.build();

		requestFactory = new HttpComponentsClientHttpRequestFactory(client);
	}

	/**
	 * Returns the instance of HttpClient, registering the required informations from the TestAutomationServer instance
	 * first if needed
	 *
	 * @param server
	 * @return
	 */
        /*
         * XXX Concurrency issues should virtually never happen, however remember that since Feat 6370 the same Jenkins instance
         * could be addressed with different credentials : the project-manager own credential when fetching jenkins jobs, and 
         * the regular credentials when an user execute the test, and this code does not shield the app from both event happening 
         * simultaneously.
         */
	public CloseableHttpClient getClientFor(TestAutomationServer server) {

		URL baseURL = server.getBaseURL();

		credentialsProvider.setCredentials(
			new AuthScope(baseURL.getHost(), baseURL.getPort(), AuthScope.ANY_REALM),
			new UsernamePasswordCredentials(server.getLogin(), server.getPassword()));


		return client;

	}

	public ClientHttpRequestFactory getRequestFactoryFor(TestAutomationServer server) {
		getClientFor(server);
		return requestFactory;
	}


}

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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import org.apache.http.client.utils.URIBuilder;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.JenkinsCrumb;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.ParameterArray;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.service.testautomation.spi.AccessDenied;
import org.squashtest.tm.service.testautomation.spi.NotFoundException;
import org.squashtest.tm.service.testautomation.spi.ServerConnectionFailed;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class configure and execute a unique HTTP request.
 * This case is simple enough, we don't need to watch a full build.
 *
 *
 */

public class StartTestExecution {

	private static final Logger LOGGER = LoggerFactory.getLogger(StartTestExecution.class);
	private static final String UNUSED = "unused";

	private final BuildDef buildDef;

	private final HttpClientProvider clientProvider;

	private final String externalId;

        private RestTemplate template;

	public StartTestExecution(BuildDef buildDef, HttpClientProvider clientProvider, String externalId) {
		super();
		this.buildDef = buildDef;
		this.clientProvider = clientProvider;
		this.externalId = externalId;

                this.template = new RestTemplate(clientProvider.getRequestFactoryFor(
			buildDef.getProject().getServer()));
	}

	public void run() {

		TestAutomationProject project = buildDef.getProject();

                // [Issue 6460]
                JenkinsCrumb crumb = getCrumb(project.getServer());

		URI url = createUrl( project);
		MultiValueMap<String, ?> postData = createPostData(buildDef, externalId);

		Object bime = execute(url, crumb, postData);

		// TODO : inspect the result to check errors and such a la RequestExecutor
		LOGGER.info("started build {}", bime);

	}

	private Object execute(URI url, JenkinsCrumb crumb,  MultiValueMap<String, ?> postData) {
		try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                    // [Issue 6460]
                    if (crumb != null){
                        headers.add(crumb.getCrumbRequestField(), crumb.getCrumb());
                    }

                    RequestEntity<?> request = new RequestEntity(postData, headers, HttpMethod.POST, url);

                    return template.exchange(request, Void.class);
		} catch (ResourceAccessException ex) {
			throw new ServerConnectionFailed(ex);
		} catch (HttpClientErrorException ex) {
			switch (ex.getStatusCode()) {
				case FORBIDDEN:
				case UNAUTHORIZED:
				case PROXY_AUTHENTICATION_REQUIRED:
					throw new AccessDenied(); // NOSONAR no need for actual call stack
				case NOT_FOUND:
					throw new NotFoundException(ex);
				default:
					throw new TestAutomationException(ex.getMessage(), ex);
			}
		}
		catch(HttpServerErrorException ex){
			LOGGER.error("build fail due to Jenkins error. Root error is :");
			LOGGER.error(ex.getMessage());
			LOGGER.error(ex.getResponseBodyAsString());
			throw new TestAutomationException(ex.getMessage(), ex);
		}
	}

        // **************** helper for the 'fetch crumb' request ***********************

        // [Issue 6460]
        private JenkinsCrumb getCrumb(TestAutomationServer server){

            try{
                LOGGER.trace("fetching CSRF jenkins crumb");
                URI uri = new URI(server.getBaseURL()+"/crumbIssuer/api/json");
                LOGGER.trace("crumb found");
                return template.getForObject(uri, JenkinsCrumb.class);
            }
            catch(HttpClientErrorException e){
                // A 404 is fine if Jenkins has not enabled CSRF protection
                if (e.getStatusCode() == HttpStatus.NOT_FOUND){
                    LOGGER.trace("no crumb found, CSRF protection seems disabled");
                    return null;
                }
                else{
                    throw new ServerConnectionFailed(e);
                }
            }
            catch(URISyntaxException ex){
                if (LOGGER.isErrorEnabled()){
                    LOGGER.error("cannot fetch crumb from server '"+
                            server.getBaseURL()+
                            "' due to URI syntax exception. Is the server URL correct ?");
                }
                throw new RuntimeException(ex);
            }

        }

        // ************ helper for the start build request itself ***************************

	private URI createUrl(TestAutomationProject project) {
            TestAutomationServer server = project.getServer();
            try{
                URI base = new URI(server.getBaseURL().toString());
                return new URIBuilder(base)
                            .setPath(base.getPath()+"/job/"+project.getJobName()+"/build")
                            .build();
            }
            catch(URISyntaxException use){
                if (LOGGER.isErrorEnabled()){
                    LOGGER.error("cannot execute build '"+project.getJobName()+"', hosted on server '"+
                            server.getBaseURL()+
                            "' due to URI syntax exception. Is the server URL correct ?");
                }
                throw new RuntimeException(use);
            }
	}


	private MultiValueMap<String, ?> createPostData(BuildDef buildDef, String externalId) {

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

		ParameterArray stdParams = new HttpRequestFactory().getStartTestSuiteBuildParameters(externalId,
			buildDef.getNode());

		File tmp;
		try {

			tmp = createJsonSuite(buildDef);
			parts.add(HttpRequestFactory.MULTIPART_BUILDFILENAME, new FileSystemResource(tmp));
			parts.add(HttpRequestFactory.MULTIPART_JENKINSARGS, new ObjectMapper().writeValueAsString(stdParams));

		} catch (JsonProcessingException e) {
			LOGGER.error("Error while mashalling json model. Maybe a bug ?", e);

		} catch (IOException e) {
			LOGGER.error("Error while writing json model into temp file. Maybe temp folder is not writable ?", e);

		}

		return parts;
	}

	private File createJsonSuite(BuildDef buildDef) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();

		File tmp = File.createTempFile("ta-suite", ".json");
		tmp.deleteOnExit();

		objectMapper.writeValue(tmp, new JsonSuiteAdapter(buildDef));

		return tmp;
	}

	/**
	 * Adapts a TestAutomationProjectContent into something which can be marshalled into a json test suite
	 * (payload of "execute tests" request).
	 *
	 * @author Gregory Fouquet
	 *
	 */
	private static final class JsonSuiteAdapter {
		private final BuildDef buildDef;
		private List<JsonTestAdapter> tests;

		private JsonSuiteAdapter(BuildDef buildDef) {
			super();
			this.buildDef = buildDef;
		}

		@SuppressWarnings(UNUSED)
		public List<JsonTestAdapter> getTest() {
			if (tests == null) {
				tests = new ArrayList<>();

				for (Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec : buildDef
					.getParameterizedExecutions()) {
					JsonTestAdapter json = new JsonTestAdapter(paramdExec);
					tests.add(json);
				}
			}

			return tests;
		}
	}

	/**
	 * Adapts a parameterized test (<code>Couple<AutomatedTest, Map></code>) into something suitable for the
	 * "execute tests" request.
	 *
	 * @author Gregory Fouquet
	 *
	 */
	private static final class JsonTestAdapter {
		private final Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec;

		private JsonTestAdapter(@NotNull Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec) {
			super();
			this.paramdExec = paramdExec;
		}

		@SuppressWarnings(UNUSED)
		public String getScript() {
			return paramdExec.getA1().getAutomatedTest().getName();
		}

		@SuppressWarnings(UNUSED)
		public String getId() {
			return paramdExec.getA1().getId().toString();
		}

		@SuppressWarnings(UNUSED)
		public Map<String, Object> getParam() {
			return paramdExec.getA2();
		}

	}

}

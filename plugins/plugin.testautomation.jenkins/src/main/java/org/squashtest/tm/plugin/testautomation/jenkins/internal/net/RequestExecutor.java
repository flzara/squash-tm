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

import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.service.testautomation.spi.AccessDenied;
import org.squashtest.tm.service.testautomation.spi.ServerConnectionFailed;

import java.io.IOException;

import static org.apache.http.HttpStatus.*;

public class RequestExecutor {

	public static final Logger LOGGER = LoggerFactory.getLogger(RequestExecutor.class);

	private static RequestExecutor INSTANCE = new RequestExecutor();

	private RequestExecutor() {
		super();
	}

	public static RequestExecutor getInstance() {
		return INSTANCE;
	}

	public String execute(CloseableHttpClient client, HttpUriRequest method) {
		try (CloseableHttpResponse resp = client.execute(method)) {
			checkResponseCode(resp.getStatusLine());

			ResponseHandler<String> handler = new BasicResponseHandler();

			return handler.handleResponse(resp);
		} catch (AccessDenied ex) {
			throw new AccessDenied(
				"Test automation - jenkins : operation rejected the operation because of wrong credentials"); // NOSONAR no need for actual call stack
		} catch (IOException ex) {
			throw new ServerConnectionFailed(
				"Test automation - jenkins : could not connect to server due to technical error : ", ex);
		}
	}

	private void checkResponseCode(StatusLine statusLine) {
		int sc = statusLine.getStatusCode();

		if (sc == SC_OK) {
			return;
		}

		switch (sc) {
			case SC_FORBIDDEN:
			case SC_UNAUTHORIZED:
			case SC_PROXY_AUTHENTICATION_REQUIRED:
				throw new AccessDenied();
		}
	}

}

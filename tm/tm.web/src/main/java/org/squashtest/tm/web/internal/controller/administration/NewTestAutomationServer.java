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
package org.squashtest.tm.web.internal.controller.administration;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.exception.WrongUrlException;

/**
 * @author mpagnon
 *
 */
public class NewTestAutomationServer extends TestAutomationServer {
	private String baseUrl;



	public TestAutomationServer createTransientEntity() {
		TestAutomationServer res  = new TestAutomationServer();
		res.setBaseURL(getBaseURLAsURL());
		res.setName(getName());
		res.setDescription(getDescription());
		res.setLogin(getLogin());
		res.setPassword(getPassword());
		res.setManualSlaveSelection(isManualSlaveSelection());
		return res;
	}


	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}
	private URL getBaseURLAsURL() {
		try {
			return new URL(baseUrl);
		} catch (MalformedURLException mue) {
			throw new WrongUrlException("baseUrl", mue);
		}
	}

	@Override
	public String toString() {
		if (StringUtils.isNotBlank(baseUrl)){
			return baseUrl;
		}
		return super.toString();
	}
}

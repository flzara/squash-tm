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
package org.squashtest.tm.web.internal.model.json;

import org.apache.logging.log4j.util.Strings;
import org.squashtest.tm.domain.tf.automationrequest.RemoteAutomationRequestExtender;

public class JsonRemoteAutomationRequestExtender {

	private static final String DASH = "-";

	private String remoteRequestStatus;
	private String remoteRequestUrl;
	private String remoteIssueKey;

	public JsonRemoteAutomationRequestExtender(RemoteAutomationRequestExtender remoteAutomationRequestExtender) {
		String remoteStatus = remoteAutomationRequestExtender.getRemoteRequestStatus();
		this.remoteRequestStatus = Strings.isNotBlank(remoteStatus) ? remoteStatus : DASH;
		String remoteUrl = remoteAutomationRequestExtender.getRemoteRequestUrl();
		this.remoteRequestUrl = Strings.isNotBlank(remoteUrl) ? remoteUrl : DASH;
		String remoteIssueKey = remoteAutomationRequestExtender.getRemoteIssueKey();
		this.remoteIssueKey = Strings.isNotBlank(remoteIssueKey) ? remoteIssueKey : DASH;
	}

	public String getRemoteRequestStatus() {
		return remoteRequestStatus;
	}
	public void setRemoteRequestStatus(String remoteRequestStatus) {
		this.remoteRequestStatus = remoteRequestStatus;
	}

	public String getRemoteRequestUrl() {
		return remoteRequestUrl;
	}
	public void setRemoteRequestUrl(String remoteRequestUrl) {
		this.remoteRequestUrl = remoteRequestUrl;
	}

	public String getRemoteIssueKey() {
		return remoteIssueKey;
	}

	public void setRemoteIssueKey(String remoteIssueKey) {
		this.remoteIssueKey = remoteIssueKey;
	}
}

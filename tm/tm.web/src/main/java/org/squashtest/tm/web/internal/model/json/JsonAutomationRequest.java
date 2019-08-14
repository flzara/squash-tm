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

import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import java.util.Locale;

public class JsonAutomationRequest {

	private Locale locale;
	private InternationalizationHelper i18nHelper;
	private String requestStatus;
	private String transmissionDate;
	private Integer automationPriority;
	private JsonRemoteAutomationRequestExtender remoteAutomationRequestExtender;

	public JsonAutomationRequest(AutomationRequest automationRequest, InternationalizationHelper i18nHelper) {
		this.i18nHelper = i18nHelper;
		this.requestStatus = i18nHelper.internationalize(automationRequest.getRequestStatus().getI18nKey(), locale);
		this.transmissionDate = i18nHelper.localizeDate(automationRequest.getTransmissionDate(), locale);
		this.automationPriority = automationRequest.getAutomationPriority();
		this.remoteAutomationRequestExtender = new JsonRemoteAutomationRequestExtender(automationRequest.getRemoteAutomationRequestExtender());
	}

	public String getRequestStatus() {
		return requestStatus;
	}
	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	public String getTransmissionDate() {
		return transmissionDate;
	}
	public void setTransmissionDate(String transmissionDate) {
		this.transmissionDate = transmissionDate;
	}

	public Integer getAutomationPriority() {
		return automationPriority;
	}
	public void setAutomationPriority(Integer automationPriority) {
		this.automationPriority = automationPriority;
	}

	public JsonRemoteAutomationRequestExtender getRemoteAutomationRequestExtender() {
		return remoteAutomationRequestExtender;
	}
	public void setRemoteAutomationRequestExtender(JsonRemoteAutomationRequestExtender remoteAutomationRequestExtender) {
		this.remoteAutomationRequestExtender = remoteAutomationRequestExtender;
	}

}

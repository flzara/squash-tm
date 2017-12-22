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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.Item;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.ItemList;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildStep;


public class CheckBuildQueue extends BuildStep<CheckBuildQueue> implements HttpBasedStep {

	/* *** technically needed for the computation **** */

	private RequestExecutor requestExecutor = RequestExecutor.getInstance();

	private CloseableHttpClient client;

	private HttpUriRequest method;

	private JsonParser parser;

	private BuildAbsoluteId absoluteId;

	// **** output of the computation *** */

	private boolean buildIsQueued = true;


	// ****** accessors ********** */


	@Override
	public void setClient(CloseableHttpClient client) {
		this.client = client;
	}

	@Override
	public void setMethod(HttpUriRequest method) {
		this.method = method;
	}

	@Override
	public void setParser(JsonParser parser) {
		this.parser = parser;
	}


	@Override
	public void setBuildAbsoluteId(BuildAbsoluteId absoluteId) {
		this.absoluteId = absoluteId;
	}


	//************* constructor ******************

	public CheckBuildQueue(BuildProcessor processor) {
		super(processor);
	}

	// *************** code ******************** */


	@Override
	public boolean needsRescheduling() {
		return buildIsQueued;
	}


	@Override
	public void perform() throws Exception {

		String result = requestExecutor.execute(client, method);

		ItemList queuedBuilds = parser.getQueuedListFromJson(result);

		Item buildOfInterest = queuedBuilds.findQueuedBuildByExtId(absoluteId.getProjectName(), absoluteId.getExternalId());

		buildIsQueued = buildOfInterest != null;

	}

	@Override
	public void reset() {
		buildIsQueued = true;
	}

	@Override
	public Integer suggestedReschedulingInterval() {
		return null;
	}


}

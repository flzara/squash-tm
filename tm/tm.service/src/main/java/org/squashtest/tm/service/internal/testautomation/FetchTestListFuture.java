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
package org.squashtest.tm.service.internal.testautomation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;



public class FetchTestListFuture implements Future<TestAutomationProjectContent> {

	private FetchTestListTask task;
	private Future<TestAutomationProjectContent> wrappedFuture;
	
	public FetchTestListTask getTask(){
		return task;
	}
	
	public FetchTestListFuture(FetchTestListTask task, Future<TestAutomationProjectContent> wrappedFuture){
		this.task = task;
		this.wrappedFuture = wrappedFuture;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return wrappedFuture.cancel(mayInterruptIfRunning);
	}

	@Override
	public TestAutomationProjectContent get() throws InterruptedException, ExecutionException {
		return wrappedFuture.get();
	}

	@Override
	public TestAutomationProjectContent get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return wrappedFuture.get(timeout, unit);
	}

	@Override
	public boolean isCancelled() {
		return wrappedFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		return wrappedFuture.isDone();
	}

}

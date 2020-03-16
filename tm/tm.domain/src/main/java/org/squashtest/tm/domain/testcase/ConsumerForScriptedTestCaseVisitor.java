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
package org.squashtest.tm.domain.testcase;

import java.util.function.Consumer;

/**
 * This Visitor is to apply a specific method onto a TestCase if it is scripted.
 * Otherwise, throws the RuntimeException given in the constructor if not null or do nothing if null.
 */
public class ConsumerForScriptedTestCaseVisitor implements TestCaseVisitor {

	private Consumer<ScriptedTestCase> consumer;
	private RuntimeException exception;

	public ConsumerForScriptedTestCaseVisitor(Consumer<ScriptedTestCase> consumer, RuntimeException exception) {
		this.consumer = consumer;
		this.exception = exception;
	}

	public ConsumerForScriptedTestCaseVisitor(Consumer<ScriptedTestCase> consumer) {
		this.consumer = consumer;
	}

	@Override
	public void visit(TestCase testCase) {
		if(exception != null) throw exception;
	}

	@Override
	public void visit(KeywordTestCase keywordTestCase) {
		if(exception != null) throw exception;
	}

	@Override
	public void visit(ScriptedTestCase scriptedTestCase) {
		consumer.accept(scriptedTestCase);
	}
}

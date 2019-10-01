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
package org.squashtest.tm.service.internal.campaign.scripted;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseLanguage;
import org.squashtest.tm.service.internal.testcase.scripted.gherkin.GherkinStepGenerator;
import org.squashtest.tm.service.internal.testcase.scripted.gherkin.GherkinTestCaseParser;
import org.squashtest.tm.service.internal.testcase.scripted.robot.RobotTestCaseParser;
import org.squashtest.tm.service.testcase.scripted.ScriptedTestCaseParser;

import java.util.function.Function;


@Configuration
public class ScriptedExecutionConfiguration {

	//This bean is only a singleton factory method
	//As the called method is a spring managed prototype bean, these will be like a Provider but with arguments
	@Bean
	public Function<ScriptedTestCaseExtender, ScriptedTestCaseParser> scriptedTestCaseParserFactory() {
		return this::parser; //just return the function that will instantiate the bean when called
	}

	@Bean
	@Scope(value = "prototype")
	public ScriptedTestCaseParser parser(ScriptedTestCaseExtender extender) {
		ScriptedTestCaseLanguage language = extender.getLanguage();
		switch (language) {
			case GHERKIN:
				return new GherkinTestCaseParser(gherkinStepGenerator());
			case ROBOT:
				return new RobotTestCaseParser();
			default:
				throw new IllegalArgumentException("No parser defined for script of language : " + language.name());
		}
	}

	@Bean
	@Scope(value = "prototype")
	public GherkinStepGenerator gherkinStepGenerator(){
		return new GherkinStepGenerator();
	}

}

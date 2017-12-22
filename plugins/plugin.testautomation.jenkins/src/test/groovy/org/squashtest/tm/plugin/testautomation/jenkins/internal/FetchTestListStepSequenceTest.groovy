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
package org.squashtest.tm.plugin.testautomation.jenkins.internal

import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.AbstractBuildProcessor
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.CheckBuildQueue
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.CheckBuildRunning
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GatherTestList
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.HttpBasedStep
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.StartBuild

import spock.lang.Specification

class FetchTestListStepSequenceTest extends Specification {

	FetchTestListStepSequence sequence
	
	HttpRequestFactory factory
	JsonParser parser
	CloseableHttpClient client
	TestAutomationProject project
	BuildAbsoluteId buildId
	AbstractBuildProcessor processor
	
	def setup(){
		
		factory = Mock()
		parser = Mock()
		client = Mock()
		project = Mock()
		buildId = new BuildAbsoluteId("bob","robert")
		buildId.buildId = 6
		processor = Mock()
		
		sequence = new FetchTestListStepSequence(processor)
		sequence.requestFactory =factory
		sequence.jsonParser = parser
		sequence.client = client
		sequence.project = project
		sequence.absoluteId = buildId
		sequence.processor = processor
		
	}
	
	def "should stuff a step with everything it needs"(){
		
		given :
			HttpBasedStep hStep = Mock()
			HttpUriRequest method = Mock()
			
		when :
			sequence.wireHttpSteps(hStep, method)
			
		then :
			1 * hStep.setClient(client)
			1 * hStep.setBuildAbsoluteId(buildId)
			1 * hStep.setMethod(method)
			1 * hStep.setParser(parser)
		
	}

	
	def "should return the correct sequence of build steps"(){
		
		when :
			def steps = []
			5.times { steps << sequence.nextElement() }
		
		then :
			steps.collect {it.class} == [
					StartBuild,
					CheckBuildQueue,
					GetBuildID,
					CheckBuildRunning,
					GatherTestList
				]
			sequence.hasMoreElements() == false
					
	}
	
	
	def "should throw an exception if one exceed the sequence"(){
		
		when :
			6.times { sequence.nextElement() }
		then :
			thrown NoSuchElementException
		
	}
}

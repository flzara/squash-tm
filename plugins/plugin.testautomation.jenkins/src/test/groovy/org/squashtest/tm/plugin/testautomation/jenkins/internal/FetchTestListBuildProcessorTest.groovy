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


import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildProcessor
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.CheckBuildQueue
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.CheckBuildRunning
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GatherTestList
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GetBuildID
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.StartBuild

import spock.lang.Specification

class FetchTestListBuildProcessorTest extends Specification {
	FetchTestListBuildProcessor processor
	
	FetchTestListStepSequence sequence	
	TestAutomationProject project	
	int defaultDelay = 500
	
	
	def setup(){
		
		sequence = Mock()
		project = Mock()
		
		processor = new FetchTestListBuildProcessor()
		processor.stepSequence = sequence
		processor.project = project	
		processor.setDefaultReschedulingDelay(defaultDelay)
		
	}
	
	def "should run the sequence smoothly"(){
		
		given :
		
			def names = [ "tests/toto.txt", "tests/sub/mike.txt" ]
		
			StartBuild start = mockStartBuild()
			CheckBuildQueue queue = mockQueue(false)
			GetBuildID getID = mockGetId()
			CheckBuildRunning running = mockRunning(false)
			GatherTestList gather = mockGather(names)

		and :
			sequence.nextElement() >>> [start, queue, getID, running, gather ]
			sequence.hasMoreElements() >> { gather.wasRan == false }
		
		when :
			processor.run()		
			def res = processor.getResult()
			
		then :
			1 * start.run()
			1 * queue.run()
			1 * getID.run()
			1 * running.run()
			gather.wasRan
		
			res.collect{it.class}.unique() == [AutomatedTest] 
			res.collect{it.name} == names
			res.collect{it.project}.unique() == [project]
		
	}
	
	
	def "that build needed a couple of rescheduling"(){
		
		given :
		
			def names = [ "tests/toto.txt", "tests/sub/mike.txt" ]
		
			StartBuild start = mockStartBuild()
			CheckBuildQueue queue = mockQueue(true)
			GetBuildID getID = mockGetId()
			CheckBuildRunning running = mockRunning(true)
			GatherTestList gather = mockGather(names)

		and :
			sequence.nextElement() >>> [start, queue, getID, running, gather ]
			sequence.hasMoreElements() >> { gather.wasRan == false }
		
		when :
			processor.run()
			def res = processor.getResult()
			
		then :
			1 * start.run()
			2 * queue.run()
			1 * getID.run()
			2 * running.run()
			gather.wasRan
		
			res.collect{it.class}.unique() == [AutomatedTest]
			res.collect{it.name} == names
			res.collect{it.project}.unique() == [project]
		
	}
	
	
	def mockStartBuild(){
		StartBuild start = Mock()
		start.needsRescheduling() >> false
		return start
	}
	
	def mockQueue(resch){
		CheckBuildQueue queue = Mock()
		queue.needsRescheduling() >>> [resch, false]
		return queue
	}
	
	def mockGetId(){
		GetBuildID getID = Mock()
		getID.needsRescheduling() >> false
		return getID
	}
	
	def mockRunning(resch){
		CheckBuildRunning running = Mock()
		running.needsRescheduling() >>> [resch, false]
		return running
	}
	
	def mockGather(arg){
		GatherTestList gather = new MockGatherTest(processor,arg)
		return gather
	}
	
	private class MockGatherTest extends GatherTestList{
		
		def response
		def wasRan = false
		
		
		public MockGatherTest(BuildProcessor processor, Collection<String> response){
			super(processor)
			this.response = response;
		}
		
		@Override
		public void run(){
			wasRan=true
		}
		@Override
		public Collection<String> getTestNames(){
			return response
		}
	}
	
}

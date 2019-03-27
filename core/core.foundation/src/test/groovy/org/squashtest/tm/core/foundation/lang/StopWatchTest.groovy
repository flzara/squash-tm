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
package org.squashtest.tm.core.foundation.lang

import spock.lang.Specification

class StopWatchTest extends Specification{

	def "should print time for an empty stopwatch"(){

		expect:

		new StopWatch("Test").toString() == """StopWatch 'Test': running time (millis) = 0
-----------------------------------------
ms     %     Task name
-----------------------------------------
"""

	}


	def "should print time and statistics for three tasks, in the order they were declared"(){

		given :
		def sw = new StopWatch("Test")

		sw.watchmap = [
			"task 1" : mockWatch(30L),
			"task 2" : mockWatch(20L),
			"task 3" : mockWatch(50L)
		] as LinkedHashMap

		when :
		def asString = sw.toString()

		then :
		asString == """StopWatch 'Test': running time (millis) = 100
-----------------------------------------
ms     %     Task name
-----------------------------------------
00030  030%  task 1
00020  020%  task 2
00050  050%  task 3
"""

	}


	def "should throw NPE if using a task not declared beforehand (you have been warned)"(){

		when :
		new StopWatch("fail !").start("bob")

		then:
		thrown NullPointerException
	}

	def "should start, suspend, resume and stop a task"(){
		given:
		def sw = new StopWatch("Test")

		when :
		sw.addTask("task1")
		sw.start("task1")
		Thread.sleep(200)
		sw.suspend("task1")
		Thread.sleep(400)
		sw.resume("task1")
		Thread.sleep(100)
		sw.stop("task1")
		Thread.sleep(400)

		then :
		// unprecise I know, but unfortunately we do not run a realtime OS
		sw.watchmap["task1"].time < 330
		sw.watchmap["task1"].time > 270
	}

	def "resume a task that hasn't started should start it instead of crashing"(){

		given :
		def sw = new StopWatch("Test")

		when :
		sw.addTask("task1")

		sw.resume("task1")
		Thread.sleep(100)
		sw.stop("task1")

		then:

		notThrown Throwable
		sw.watchmap["task1"].time > 0

	}


	private mockWatch(time){
		Mock(org.apache.commons.lang3.time.StopWatch){
			getTime() >> time
		}
	}
}

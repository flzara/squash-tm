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
package org.squashtest.tm.domain.scm

import org.apache.commons.io.FileUtils
import org.squashtest.tm.domain.testutils.MockFactory
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ScmRepositoryTest extends Specification {

	@Shared
	private ScmRepository scm1 = new MockFactory().mockScmRepository("SRTest_").with{
		def self = delegate
		use(ReflectionCategory){
			ScmRepository.set field: "id", of: self, to: 10L
		}
		self
	}


	@Shared
	private ScmRepository scm2 = new MockFactory().mockScmRepository("SRTest2_").with{
		def self = delegate
		use(ReflectionCategory){
			ScmRepository.set field: "id", of: self, to: 20L
		}
		self
	}

	// hmm, maybe the executor should not be shared
	@Shared
	private ExecutorService executor = Executors.newFixedThreadPool(3)

	def cleanupSpec(){
		FileUtils.forceDelete(scm1.baseRepositoryFolder)
		FileUtils.forceDelete(scm2.baseRepositoryFolder)
		executor.shutdown()
	}

	// ************* lock mechanism tests *****************

	@Timeout(5)
	def "let a thread reacquire its own lock"(){

		when :
		String res

		Future<String> future = executor.submit ( {
			return scm1.doWithLock {
				return scm1.doWithLock {
					return scm1.doWithLock {
						return "acquired trice !"
					}
				}
			}
		} as Callable<String>)

		try{
			res = future.get(3, TimeUnit.SECONDS)
		}
		catch(TimeoutException ex){
			res = "Ooooh... timed out :("
		}


		then :
		res == "acquired trice !"

	}

	@Timeout(5)
	def "two thread calling for the same repository should work in sequence"(){

		given : "the timers"
		Timings timeTask1 = new Timings()
		Timings timeTask2 = new Timings()


		and : "the tasks"

		// task1 is a long task (see the pause inside)
		def task1 = chronoTask (timeTask1, scm1){
			Thread.sleep(500)
			return 0
		}

		// task 2 is a short task
		def task2 = chronoTask(timeTask2, scm1){
			return 10
		}


		when :

		// task 1 is scheduled to run immediately and executed before task 2
		Future<Integer> futureTask1 = executor.submit( task1  as Callable<Integer>)
		Thread.sleep(100)
		Future<Integer> futureTask2 = executor.submit( task2  as Callable<Integer>)

		// we even ask for the result of task2 before task1
		def res2 = futureTask2.get(3, TimeUnit.SECONDS)
		def res1 = futureTask1.get(3, TimeUnit.SECONDS)


		then :

		res1 == 0
		res2 == 10

		/*
		 * Timing checks :
		 * - task 1 has started before task 2
		 * - task 2 started before task 1 completed the job
		 * - yet, task 2 acquired the lock only after the job of task 1 was completed
		 */

		timeTask1.start <= timeTask2.start
		timeTask2.start <= timeTask1.beforeLockRelease
		timeTask1.beforeLockRelease <= timeTask2.lockAcquired

	}


	@Timeout(5)
	def "two threads can access two different repositories simultaneously, no problems"(){

		given : "the timers"
		Timings timeTask1 = new Timings()
		Timings timeTask2 = new Timings()


		and : "the tasks"

		// task1 is a long task (see the pause inside)
		def task1 = chronoTask (timeTask1, scm1){
			Thread.sleep(500)
			return 0
		}

		// task 2 is a short task
		def task2 = chronoTask(timeTask2, scm2){
			return 10
		}


		when :

		// task 1 is scheduled to run immediately and executed before task 2
		Future<Integer> futureTask1 = executor.submit( task1  as Callable<Integer>)
		Thread.sleep(100)
		Future<Integer> futureTask2 = executor.submit( task2  as Callable<Integer>)

		// we even ask for the result of task2 before task1
		def res2 = futureTask2.get(3, TimeUnit.SECONDS)
		def res1 = futureTask1.get(3, TimeUnit.SECONDS)


		then :

		res1 == 0
		res2 == 10

		/*
		 * Timing checks :
		 * - task 1 has started before task 2
		 * - task 2 completed before task 1
		 */

		timeTask1.start <= timeTask2.start
		timeTask1.lockReleased >= timeTask2.lockReleased

	}

	// ************* scaffolding *************


	def chronoTask(Timings timings, ScmRepository repo, Closure job){
		return {
			timings.start = timestamp()

			def res = repo.doWithLock {
				timings.lockAcquired = timestamp()

				def subres = job()

				timings.beforeLockRelease = timestamp()

				return subres
			}

			timings.lockReleased = timestamp()
			return res

		}
	}


	private static final class Timings {
		long start			// thread start
		long lockAcquired	// lock acquired
		long beforeLockRelease        // actual job end
		long lockReleased	// lock released, also last instruction
	}

	def timestamp(){
		System.nanoTime()
	}

}

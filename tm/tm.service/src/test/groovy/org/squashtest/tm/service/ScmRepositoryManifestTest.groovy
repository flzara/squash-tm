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
package org.squashtest.tm.service

import org.apache.commons.io.FileUtils
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseKind
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors
import org.squashtest.tm.service.testutils.MockFactory

class ScmRepositoryManifestTest extends Specification{

	// see the file structure on the repo in MockFactory
	@Shared
	ScmRepository repo = new MockFactory().mockScmRepository()


	def cleanupSpec(){
		FileUtils.forceDelete(repo.baseRepositoryFolder)
	}


	def "should init the manifest using the cache"(){

		when :
		ScmRepositoryManifest manifest = new ScmRepositoryManifest(repo, true)

		then :
		manifest.useCache == true
		manifest.pathCache.isEmpty() == false

	}


	def "should init the manifest without cache"(){

		when :
		ScmRepositoryManifest manifest = new ScmRepositoryManifest(repo, false)

		then :
		manifest.useCache == false
		manifest.pathCache.isEmpty() == true
	}


	def "should retrieve a file from the drive (no cache)"(){

		given :
		def manifest = new ScmRepositoryManifest(repo, false)
		def pattern = "^815_.*"

		when :
		def files = manifest.searchOnDrive(pattern)

		then :
		files.size() == 1
		files[0].getName() == "815_test1.ta"
	}


	def "should retrieve a file from the cache (using cache)"(){

		given :
		def manifest = new ScmRepositoryManifest(repo, true)
		def pattern = "^815_.*"

		when :
		def files = manifest.searchInCache(pattern)

		then :
		files.size() == 1
		files[0].getName() == "815_test1.ta"

	}


	def "should return the file path of a file relative to the repo root folder"(){

		given :
		def manifest = new ScmRepositoryManifest(repo)
		def file = manifest.searchInCache("999_test3.ta")

		when :
		def relative = manifest.getRelativePath(file)

		then :
		relative == "squash/subfolder/999_test3.ta"

	}

	def "should return the list of test path relative to the repo root folder"(){

		given :
		def manifest = new ScmRepositoryManifest(repo)

		when :
		def res = manifest.streamTestsRelativePath().collect(Collectors.toList()).sort()

		then:
		res == ["squash/220_test2.ta", "squash/815_test1.ta", "squash/subfolder/999_test3.ta"]

	}

	def "should retrieve the file corresponding to a gherkin test case"(){

		setup:
		def gherkinFile = new File(repo.workingFolder, "411_test.feature")
		gherkinFile.createNewFile()

		and :
		def manifest = new ScmRepositoryManifest(repo)

		and:
		def tc = Mock(TestCase){
			getId() >> 411L
			getKind() >> TestCaseKind.GHERKIN
			getName() >> "test"
		}

		when :
		def maybeFile = manifest.locateTest(tc)

		then :
		maybeFile.isPresent()
		maybeFile.get().getName() == "411_test.feature"

		cleanup:
		gherkinFile.delete()

	}


	def "should retrieve the file for a gherkin test case, resolving potential ambiguity by taking the first in lexicographical order"(){


		setup:
		def gherkinFile1 = new File(repo.workingFolder, "411_test1.feature")
		def gherkinFile2 = new File(repo.workingFolder, "411_test2.feature")
		gherkinFile1.createNewFile()
		gherkinFile2.createNewFile()

		and :
		def manifest = new ScmRepositoryManifest(repo)

		and:
		def tc = Mock(TestCase){
			getId() >> 411L
			getKind() >> TestCaseKind.GHERKIN
			getName() >> "test"
		}

		when :
		def maybeFile = manifest.locateTest(tc)

		then :
		maybeFile.isPresent()
		maybeFile.get().getName() == "411_test1.feature"

		cleanup:
		gherkinFile1.delete()
		gherkinFile2.delete()


	}


}

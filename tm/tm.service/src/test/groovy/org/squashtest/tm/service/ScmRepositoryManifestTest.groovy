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
import spock.lang.Unroll

import java.util.stream.Collectors
import org.squashtest.tm.service.testutils.MockFactory

class ScmRepositoryManifestTest extends Specification{

	// see the file structure on the repo in MockFactory
	@Shared
	ScmRepository repo = new MockFactory().mockScmRepository()

	// special file structure, with a somewhat overrepresented filename
	@Shared
	ScmRepository repoDuplicates = new MockFactory().mockScmRepository(12L, "duplicates", "squash"){
		dir("squash"){
			file "42_ahahah.feature"
			dir("sub1"){
				file "42_ahahah.feature"
			}
			dir("sub2"){
				file "42_ahahah.feature"
				file"555_test5.feature"
			}
		}
	}

	def cleanupSpec(){
		FileUtils.forceDelete(repo.baseRepositoryFolder)
		FileUtils.forceDelete(repoDuplicates.baseRepositoryFolder)
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


	def "should return the file path of a file relative to the repo working folder"(){

		given :
		def manifest = new ScmRepositoryManifest(repo)
		def file = manifest.searchInCache("999_test3.ta")

		when :
		def relative = manifest.getRelativePath(file)

		then :
		relative == "subfolder/999_test3.ta"

	}

	def "should return the list of test path relative to the repo working folder"(){

		given :
		def manifest = new ScmRepositoryManifest(repo)

		when :
		def res = manifest.streamTestsRelativePath().collect(Collectors.toList()).sort()

		then:
		res == ["220_test2.ta", "815_test1.ta", "subfolder/999_test3.ta"]

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

	def "should retrieve the file corresponding to a gherkin test case in a nested folder"(){

		given :
		def manifest = new ScmRepositoryManifest(repoDuplicates)

		and:
		def tc = Mock(TestCase){
			getId() >> 555L
			getKind() >> TestCaseKind.GHERKIN
			getName() >> "test5"
		}

		when :
		def maybeFile = manifest.locateTest(tc)

		then :
		maybeFile.isPresent()
		def file = maybeFile.get()
		file.getName() == "555_test5.feature"
		manifest.getRelativePath(file) == "sub2/555_test5.feature"


	}


	def "should retrieve the script file for a gherkin test case, resolving ambiguity (on the prefix only) by taking the first in lexicographical order"(){


		setup:
		def gherkinFile1 = new File(repo.workingFolder, "411_test1.feature")
		def gherkinFile2 = new File(repo.workingFolder, "411_test2.feature")
		gherkinFile1.createNewFile()
		gherkinFile2.createNewFile()

		and :
		// disabling cache in order to test a different branching and increasing code coverage
		def manifest = new ScmRepositoryManifest(repo, false)

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


	@Unroll("should retrieve the script file for a gherkin test case, resolving ambiguity (on the entire filename) by taking the first in lexicographical order, #humanmsg using cache")
	def "should retrieve the script file for a gherkin test case, resolving ambiguity (on the entire filename) by taking the first in lexicographical order"(){

		given:
		def manifest = new ScmRepositoryManifest(repoDuplicates, useCache)

		and:
		def tc = Mock(TestCase){
			getId() >> 42L
			getKind() >> TestCaseKind.GHERKIN
			getName() >> "ahahah"
		}

		when:
		def maybeFile = manifest.locateTest(tc)

		then:
		maybeFile.isPresent()
		maybeFile.get().name == "42_ahahah.feature"

		where:
		humanmsg << ["not", ""]
		useCache << [false, true]

	}



	// ********* additional methods to reach 100% **************

	def "getter on the scm"(){

		expect:
		new ScmRepositoryManifest(repo).scm == repo

	}

	def "constructor should fail if the ScmRepository is badly configured"(){

		when :
		new ScmRepositoryManifest(new ScmRepository(name:"no base directory !"))

		then:
		def ex = thrown IllegalArgumentException
		ex.message == "the repository 'no base directory !' has no base directory defined !"

	}


	def "should throw on IO failure when listing the filesystem"(){

		given:
			def scm = new ScmRepository(){
				{
					name = "dead repo"
					repositoryPath = "/dev/null"
				}
				Collection<File> listWorkingFolderContent() throws IOException{
					throw new IOException("daaaaamn !")
				}
			}

		when:
			new ScmRepositoryManifest(scm)

		then:
			def ex = thrown Exception
			ex.message == "cannot list content of scm 'dead repo'"

	}

}

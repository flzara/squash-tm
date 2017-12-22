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
package org.squashtest.tm.domain.testautomation

import org.squashtest.tm.domain.testautomation.AutomatedTest
import org.squashtest.tm.domain.testautomation.TestAutomationProject

import spock.lang.Shared
import spock.lang.Specification

class AutomatedTestTest extends Specification {

	@Shared
	AutomatedTest test

	@Shared
	def name

	def setupSpec(){

		name = "folder/subfolder/test.txt"

		def project = new TestAutomationProject("the-project")
		test = new AutomatedTest( name , project)

	}


	def "should return the name"(){
		expect :
		test.name == name
	}


	def "should return the full name"(){
		expect :
		test.fullName == "/the-project/"+name
	}


	def "should return the path"(){
		expect :
		test.path == "folder/subfolder/"
	}

	def "should return the short name"(){
		expect :
		test.shortName == "test.txt"
	}

	def "should return name without root folder"(){
		expect :
		test.nameWithoutRoot == "subfolder/test.txt"
	}

	def "should return the root folder name"(){
		expect :
		test.rootFolderName == "folder/"
	}


	def "should say that test is not at the root"(){
		expect :
		test.atTheRoot == false

	}

	def "should say that test is at the root"(){

		given :
		def test = new AutomatedTest("tests/my-test.txt", null)

		when :
		def res = test.atTheRoot

		then :
		res == true

	}


}

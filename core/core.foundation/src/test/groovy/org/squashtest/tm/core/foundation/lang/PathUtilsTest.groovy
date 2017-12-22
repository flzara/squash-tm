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


import spock.lang.Specification;
import spock.lang.Unroll;

class PathUtilsTest extends Specification {

	@Unroll("should say that path '#path' is #textres valid because #why")
	def "should say that the following pathes are valid"() {

		expect:
		PathUtils.isPathWellFormed(path) == predicate

		where:
		path                             | predicate | textres | why
		"/toto/titi"                     | true      | ""      | "it's well formed"
		"/toto/tata/tutu/rere"           | true      | ""      | "it's well formed"
		"/to \\/ to/tutu \\/ slash/tata" | true      | ""      | "it's well formed and escaped slashes are harmless"
		"/toto/tata \\/"                 | true      | ""      | "it's well formed and escaped slashes are harmless"
		"toto/tata"                      | false     | "not"   | "it doesn't begin with a '/'"
		"/toto/tuytu\\/ends with slash/" | false     | "not"   | "it ends with a slash"
		"/toto"                          | false     | "not"   | "it's too short (only project name)"

	}


	@Unroll
	def "should extract project name #name from path #path"() {

		expect:
		def res = PathUtils.extractProjectName(path)
		name.equals(res);

		where:
		path                                       | name
		"/toto/tata"                               | "toto"
		"/toto \\/ with escape \\/ slash \\//tutu" | "toto \\/ with escape \\/ slash \\/"
		"/tata"                                    | null // this test was added to match the actual behaviour, behaviour which looks suspicious
	}

	@Unroll
	def "should extract unescaped project name #name from path #path"() {

		expect:
		def res = PathUtils.extractUnescapedProjectName(path)
		name.equals(res);

		where:
		path                                       | name
		"/toto/tata"                               | "toto"
		"/toto \\/ with unescaped \\/ slashes \\//tutu" | "toto / with unescaped / slashes /"
		"/tata"                                    | null // this test was added to match the actual behaviour, behaviour which looks suspicious
	}

	@Unroll("the test case name in '#path' is '#name'")
	def "should extract test case names"() {
		expect:
		def res = PathUtils.extractTestCaseName(path)
		name.equals(res);

		where:
		path                             | name
		"/toto/tutu"                     | "tutu"
		"/toto/tata/tete/titi/tutu"      | "tutu"
		"/\\/yeah\\//yo\\/yo/tu\\/tu\\/" | "tu\\/tu\\/"
	}


	@Unroll("path '#path' splits into #num elements")
	def "should split a path"() {

		expect:
		def pathes = PathUtils.splitPath(path) as List
		pathes == names

		where:
		path                             | num | names
		"/toto/tutu"                     | 2   | ["toto", "tutu"]
		"/toto/tata/tete/titi/tutu"      | 5   | ["toto", "tata", "tete", "titi", "tutu"]
		"/\\/yeah\\//yo\\/yo/tu\\/tu\\/" | 3   | ["\\/yeah\\/", "yo\\/yo", "tu\\/tu\\/"]

	}


	def "should return uniques project names"() {

		given:
		def paths = [
			"/project 1/toto/titi",
			"/project \\/2/toto/titi",
			"/project 1/tata",
			"/project \\/2//bob, mike"
		]

		when:
		def res = PathUtils.extractProjectNames(paths)

		then:
		res == ["project 1", "project \\/2"]
	}


	@Unroll("should rename #path to #newpath")
	def "should 'rename' a path with a new name"() {
		expect:
		newpath == PathUtils.rename(path, newname)

		where:
		path               | newname | newpath
		"/bob/robert/toto" | "mike"  | "/bob/robert/mike"
		"/home/couch"      | "bed"   | "/home/bed"
	}

	@Unroll("should return the path of all parent of the node refered to by this node #comment")
	def "should return the path of all parent of the node refered to by this node"() {

		expect:
		parentpaths == PathUtils.scanPath(path)


		where:
		path                  | parentpaths                                       | comment

		"/bob/robert/toto"    | ["/bob", "/bob/robert", "/bob/robert/toto"]       | ", normal case"
		"//bob/robert/toto/"  | ["/bob", "/bob/robert", "/bob/robert/toto"]       | ", with extra slashes before"
		"/bob/ro\\/bert/toto" | ["/bob", "/bob/ro\\/bert", "/bob/ro\\/bert/toto"] | ", with names having a '/' inside"
	}

	def "should replace multiple slashes"(){

		expect:
		resultPath == PathUtils.cleanMultipleSlashes(path);

		where:
		path 										|| resultPath
		"project1/dossier/tc01"						|| "project1/dossier/tc01"
		"project1/doss\\/ier/tc01"					|| "project1/doss\\/ier/tc01"
		"project1/dossier////tc01"					|| "project1/dossier/tc01"
		"project1////dossier////tc01"				|| "project1/dossier/tc01"
		"project1////dossier////dossier2/tc01"		|| "project1/dossier/dossier2/tc01"
		"project1\\/////dossier\\/////dossier2/tc01"|| "project1\\//dossier\\//dossier2/tc01"
	}
}

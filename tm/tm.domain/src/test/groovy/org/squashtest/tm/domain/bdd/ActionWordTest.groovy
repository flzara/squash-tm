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
package org.squashtest.tm.domain.bdd

import org.squashtest.tm.domain.project.Project
import spock.lang.Specification
import spock.lang.Unroll

class ActionWordTest extends Specification {

	@Unroll
	def "should create an ActionWord with a basic text"() {
		given:
			def fragment = new ActionWordText(word)
		when:
			ActionWord actionWord = new ActionWord([fragment] as List)
		then:
			actionWord.createWord() == expectedWord
			actionWord.generateToken() == expectedToken
		where:
			word 								|| expectedWord							|| expectedToken
			"hello" 							|| "hello"								|| "T-hello-"
			"hello   is it   me ?" 				|| "hello is it me ?"					|| "T-hello is it me ?-"
			"wôrd; wïth sp&cia! charActers?" 	||	"wôrd; wïth sp&cia! charActers?"	|| "T-wôrd; wïth sp&cia! charActers?-"
	}

	def "should create an ActionWord with some fragments"() {
		given:
			def fragment1 = new ActionWordText("An action word with a ")
			def fragment2 = new ActionWordParameter("name1", "")
			def fragment3 = new ActionWordText(" parameter !")
		when:
			ActionWord actionWord = new ActionWord([fragment1, fragment2, fragment3] as List)
		then:
			actionWord.createWord() == "An action word with a \"name1\" parameter !"
			actionWord.generateToken() == "TPT-An action word with a - parameter !-"
	}

	def "should copy an ActionWord"() {
		given:
			def project = Mock(Project) {
				getId() >> 90L
			}
		and:
			def fragment1 = new ActionWordText("An action word with a ")
			fragment1.setId(71L)
			def fragment2 = new ActionWordParameter("name1", "")
			fragment1.setId(72L)
			def fragment3 = new ActionWordText(" parameter !")
			fragment1.setId(73L)
		and:
			ActionWord awSource = new ActionWord([fragment1, fragment2, fragment3] as List)
			awSource.setDescription("Il s'agit d'une action avec paramètres.")
			awSource.setProject(project)
		when:
			ActionWord awCopy = awSource.createCopy()
		then:
			awCopy != null
			awCopy.getProject() == project
			awCopy.createWord() == awSource.createWord()
			awCopy.getName() == awSource.getName()
			awCopy.generateToken() == awSource.generateToken()
			awCopy.getDescription() == awSource.getDescription()
			awCopy.getFragments().every {
				it.getId() == null
				it.getActionWord() == awCopy
			}
	}
}

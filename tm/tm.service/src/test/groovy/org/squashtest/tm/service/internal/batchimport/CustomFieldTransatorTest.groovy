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
/**
 *   This file is part of the Squash TM management services for SaaS / Squash On Demand (saas.management.fragment) project.
 *     Copyright (C) 2015 - 2016 Henix, henix.fr - All Rights Reserved
 *
 *     Unauthorized copying of this file, via any medium is strictly prohibited
 *     Proprietary and confidential
 *
 *      (C)Henix. Tous droits réservés.
 *
 *     Avertissement : ce programme est protégé par la loi relative au droit d'auteur et par les conventions internationales. Toute reproduction ou distribution partielle ou totale du logiciel, par quelque moyen que ce soit, est strictement interdite.
 */
package org.squashtest.tm.service.internal.batchimport

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.InputType
import org.squashtest.tm.domain.customfield.MultiSelectField
import org.squashtest.tm.service.internal.repository.CustomFieldDao
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 * @since x.y.z  16/06/16
 */
class CustomFieldTransatorTest extends Specification {
	CustomFieldTransator transator = new CustomFieldTransator()
	CustomFieldDao customFieldDao = Mock()

	def setup() {
		transator.customFieldDao = customFieldDao;
	}
	/**
	 * Rem : This test was created while fixing an issue. Rules for blank tags are inferred from what the method actually does
	 */
	@Unroll
	@Issue("#6299 - null string values triggers NPE")
	def "should coerce '#strVal' of TAG field into #rawVal"() {
		given:
		def requested = [heroes: strVal]

		and:
		def heroes = new MultiSelectField()
		use(ReflectionCategory) {
			CustomField.set(field: "id", of: heroes, to: 10L)
			CustomField.set(field: "code", of: heroes, to: "heroes")
		}

		customFieldDao.findByCode("heroes") >> heroes

		when:
		def res = transator.toAcceptableCufs(requested)

		then:
		res[10L] != null
		res[10L].values == rawVal

		where:
		strVal                | rawVal
		""                    | [""]
		null                  | []                                // rem : nulls should be accepted cos we dont know how to coerce'entityManager until the actual CF is fetched
		"the batman|| |robin" | ["the batman", "", " ", "robin"]    // rem : blanks are kept
		"the batman|robin|"   | ["the batman", "robin"]            // rem : EXCEPT WHEN THEY'RE LAST WFT
		"the batman|robin| "  | ["the batman", "robin", " "]        // rem : BUT NOT-EMPTIES ARE KEPT WFT
		" the batman "        | [" the batman "]                    // rem : not trimmed
	}

	@Unroll
	def "should coerce '#strVal' of PLAIN_TEXT field into '#strVal'"() {
		given:
		def requested = [hero: strVal]

		and:
		def hero = new CustomField(id: 10l, code: "hero", inputType: InputType.PLAIN_TEXT)

		customFieldDao.findByCode("hero") >> hero

		when:
		def res = transator.toAcceptableCufs(requested)

		then:
		res[10L] != null
		res[10L].value == strVal

		where:
		strVal << [null, "", "  ", "the goddamn batman", "  the spaced batman  "]
	}
	
	
	def "should return the input type of a customfield (that exists)"(){
		
		given :
			transator.cufInfosCache['hero'] = new CustomFieldInfos(1L, InputType.PLAIN_TEXT) 
		
		when :
			def typ = transator.getInputTypeFor('hero')
		
		then :
			typ == InputType.PLAIN_TEXT
	}
	
	
	
	def "should return the input type of a customfield (when not exists)"(){
		
		given :
			customFieldDao.findByCode("hero") >> null
		
		when :
			def typ = transator.getInputTypeFor('hero')
		
		then :
			typ == null
	}
}

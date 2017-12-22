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
package org.squashtest.tm.domain.customfield

import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 * @since x.y.z  16/06/16
 */
class TagsValueTest extends Specification {
	@Unroll
	@Issue("#6299 - setting to null issues a NPE")
	def "should set values to #values from string representation '#strRep'"() {
		given:
		TagsValue value = new TagsValue(binding: new CustomFieldBinding(customField: new MultiSelectField()))

		when:
		value.setValue(strRep)

		then:
		value.values == values

		where:
		strRep                | values
		""                    | [""]
		null                  | []                               // rem : that the #6299 bugfix. not sure nulls should be valid values, though
		"the batman|| |robin" | ["the batman", "", " ", "robin"] // rem : blanks are kept
		"the batman|robin|"   | ["the batman", "robin"]          // rem : EXCEPT WHEN THEY'RE LAST WFT
		"the batman|robin| "  | ["the batman", "robin", " "]     // rem : BUT NOT-EMPTIES ARE KEPT WFT
		" the batman "        | [" the batman "]                 // rem : not trimmed
	}

	/**
	 * this test was created before refactoring, expectations are inferred from actual behaviour, which smells of bugs
     */
	@Unroll
	def "should get string representation '#strRep' from values #values"() {
		given:
		TagsValue value = new TagsValue(binding: new CustomFieldBinding(customField: new MultiSelectField()))

		when:
		value.setValues(values)

		then:
		value.value == strRep

		where:
		strRep                | values
		""                    | [""]
		""                    | []                               // smells of bug : different values have same string rep
		"the batman|| |robin" | ["the batman", "", " ", "robin"]
		"the batman|robin"    | ["the batman", "robin"]
		"the batman|robin| "  | ["the batman", "robin", " "]
		" the batman "        | [" the batman "]
	}
}

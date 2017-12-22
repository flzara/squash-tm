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
package org.squashtest.tm.service.internal.library

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class LibraryUtilsTest extends Specification {
	@Unroll
	def "should generate non clashing name '#expected' for '#clashing' among #siblings"() {
		expect:
		expected == LibraryUtils.generateNonClashingName(clashing, siblings, 255)
		
		where:
		siblings                            | clashing    | expected
		[]                                  | "batman"    | "batman"
		["spiderman"]                       | "spiderman" | "spiderman (1)"
		["wolverine", "wolverine (1)"]      | "wolverine" | "wolverine (2)"
		["rogue (1)", "rogue (3)", "rogue"] | "rogue"     | "rogue (4)"
		["cloak", "dagger"]                 | "iron man"  | "iron man"
		["booster gold (1)"]                | "booster gold (1)"  | "booster gold (1) (1)"
		["martian manhunter (x)"]           | "martian manhunter" | "martian manhunter"
		["(\\d{2})"]                        | "(\\d{2})"          | "(\\d{2}) (1)"
		["(\\d{2})", "(\\d{2}) (1)"]        | "(\\d{2})"          | "(\\d{2}) (2)"
		["the lamplighter (1)"]             | "the lamplighter"   | "the lamplighter"
	}

	def "Should build escaped regexp"() {
		given: 
		def source = "a{2}"
		
		when:
		def escaped = Pattern.quote(source)
		Pattern p = Pattern.compile(escaped + "\\d")
		Matcher m = p.matcher("a{2}1")
		
		then:
		m.find()
		
	}
	
	@Unroll
	def "should generate unique copy name '#expected' for '#clashing' among #siblings"() {
		expect:
		expected == LibraryUtils.generateUniqueCopyName(siblings, clashing, 255)
		
		where:
		siblings                            | clashing    | expected
		[]                                  | "batman"    | "batman-Copie1"
		["spiderman"]                       | "spiderman" | "spiderman-Copie1"
		["wolverine", "wolverine-Copie1"]      | "wolverine" | "wolverine-Copie2"
		["rogue-Copie1", "rogue-Copie3", "rogue"] | "rogue"     | "rogue-Copie4"
		["cloak", "dagger"]                 | "iron man"  | "iron man-Copie1"
		["booster gold-Copie1"]                | "booster gold-Copie1"  | "booster gold-Copie1-Copie1"
		["martian manhunter-Copiex"]           | "martian manhunter" | "martian manhunter-Copie1"
		["(\\d{2})"]                            | "(\\d{2})"              | "(\\d{2})-Copie1"
		["(\\d{2})", "(\\d{2})-Copie1"]        | "(\\d{2})"          | "(\\d{2})-Copie2"
		["the lamplighter-Copie1"]             | "the lamplighter"   | "the lamplighter-Copie2"
	}

	@Unroll
	def "should generate unique substringed copy name '#expected' for '#clashing' among #siblings"() {
		expect:
		expected == LibraryUtils.generateUniqueCopyName(siblings, clashing, maxNameSize)
		
		where:
		maxNameSize | siblings                                           | clashing                | expected
		12          |["batman"]                                          | "batman"                | "ba...-Copie1"
		15          |["spiderman", "spide...-Copie1"]                    | "spiderman"             | "spide...-Copie2"
		14          |["the hulk", "the ...-Copie1",  "the...-Copie10"]   | "the hulk"              | "the ...-Copie2"
		15          |["spide...-Copie1"]          			             | "spiderman"             | "spide...-Copie2"
		15          |["spiderman", "spide...-Copie1", "spide...-Copie9"] | "spiderman"             | "spid...-Copie10"
		15          |["spiderman", "spide...-Copie1", "spide...-Copie9", "spid...-Copie10"] | "spiderman"             | "spid...-Copie11"
		12          |["cloak", "dagger"]                                 | "iron man"              | "ir...-Copie1"
		25          |["booster gold-Copie1"]                             | "booster gold-Copie1"   | "booster gold-Co...-Copie1"
	}
}

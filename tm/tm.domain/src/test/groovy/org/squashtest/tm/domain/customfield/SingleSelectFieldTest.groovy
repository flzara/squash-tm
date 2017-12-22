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
package org.squashtest.tm.domain.customfield;

import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.SingleSelectField;

import spock.lang.Specification


/**
 * @author Gregory
 *
 */
class SingleSelectFieldTest extends Specification {
	def "should add and remove options"() {
		given: 
		SingleSelectField field = new SingleSelectField()
		//field.inputType = InputType.DROPDOWN_LIST
		
		when:
		field.addOption(new CustomFieldOption("batman", "code1"))
		
		field.addOption(new CustomFieldOption("robin", "code2"))
		 
		then:
		field.options*.label == ["batman", "robin"]
		field.options*.code == ["code1", "code2"]

		when:
		field.removeOption("batman")
		
		then:
		field.options*.label == ["robin"]
		
	}

}

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
package org.squashtest.tm.domain.customfield

import org.squashtest.tm.exception.customfield.WrongCufNumericFormatException
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by jthebault on 26/07/2016.
 */
class NumericCustomFieldValueTest extends Specification{

	@Unroll
	def "should reject invalid numbers" (){
		given:
		NumericCustomFieldValue numericValue = new NumericCustomFieldValue();

		when:
		numericValue.setValue(value);

		then:
		thrown(WrongCufNumericFormatException)


		where:
		value 			| _
		"toto"			| _
		"1,65,89"		| _
		"1 236 897"		| _
		"89+96"			| _
	}

	@Unroll
	def "should accept valid numbers" (){
		given:
		NumericCustomFieldValue numericValue = new NumericCustomFieldValue();

		when:
		numericValue.setValue(value);

		then:
		noExceptionThrown()


		where:
		value 			| _
		"1"				| _
		"1,6589"		| _
		"8971.6589"		| _
		"1236897"		| _
		"-8996"			| _
		"0"				| _
		"-0"			| _
	}

	@Unroll
	def "should convert decimal separator" (){
		given:
		NumericCustomFieldValue numericValue = new NumericCustomFieldValue();

		when:
		numericValue.setValue(value);

		then:
		numericValue.getValue().equals(expectedValue)


		where:
		value 			| expectedValue
		"1,00"			| "1.00"
		"1,6589"		| "1.6589"
		"8971.6589"		| "8971.6589"
		"1236897"		| "1236897"
		"-8996"			| "-8996"
		"0"				| "0"
		"-0"			| "0"
	}
}

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
package org.squashtest.tm.service.internal.batchimport

import org.apache.commons.lang3.StringUtils
import org.aspectj.apache.bcel.generic.SwitchBuilder
import org.spockframework.compiler.model.SetupBlock
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldOption
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.customfield.InputType
import org.squashtest.tm.domain.customfield.SingleSelectField
import org.squashtest.tm.service.importer.ImportMode
import org.squashtest.tm.service.importer.LogEntry

import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author mpagnon
 *
 */
public class CustomFieldValidatorTest extends Specification {

	CustomFieldValidator cufValidator = new CustomFieldValidator()
	private static String overMaxSizeValue = "Le Lorem Ipsum est simplement du faux texte employé dans la composition et la mise en page avant impression. Le Lorem Ipsum est le faux texte standard de l'imprimerie depuis les années 1500, quand un peintre anonyme assembla ensemble des morceaux de texte pour réaliser un livre spécimen de polices de texte. Il n'a pas fait que survivre cinq siècles, "
	private static String substringedValue = overMaxSizeValue.substring(0, CustomFieldValue.MAX_SIZE)
	@Unroll("should not validate custom field where : #humanMsg")
	def "should not validate custom field "() {
		given:
		Map<String, String> cufs = new HashMap<String, String>()
		def code = "001"
		def value = cufValue
		cufs.put(code, value)

		and:
		InputType inputType = cufType

		CustomField cuf = new CustomField(inputType)
		cuf.setCode(code)
		cuf.setDefaultValue(defaultValue)
		cuf.setOptional(optional)
		def defs = [cuf]

		and:
		TestCaseTarget testCase = Mock()
		ImportMode mode = importMode
		when:
		LogTrain createLog = checkCuf(mode,testCase, cufs, defs)

		then:
		LogEntry entry = createLog.entries[0]
		entry.getI18nError() == errorKey
		entry.getI18nImpact() == impactKey
		cufs.get(code)  == newValue

		where:
		importMode 			|humanMsg 									|cufType				| optional 	| defaultValue 	| cufValue 			| errorKey 								| impactKey 					| newValue
		//CREATE
		ImportMode.CREATE 	|"create mandatory oversized plain text"	|InputType.PLAIN_TEXT 	| false 	| "default" 	| overMaxSizeValue	| Messages.ERROR_MAX_SIZE 				| Messages.IMPACT_MAX_SIZE		| substringedValue
		ImportMode.CREATE 	|"create optional  oversized plain text"	|InputType.PLAIN_TEXT 	| true	 	| "" 			| overMaxSizeValue	| Messages.ERROR_MAX_SIZE 				| Messages.IMPACT_MAX_SIZE		| substringedValue
		ImportMode.CREATE 	|"create mandatory plain text"				|InputType.PLAIN_TEXT 	| false 	| "default" 	| "" 				| Messages.ERROR_MANDATORY_CUF 			| Messages.IMPACT_DEFAULT_VALUE | "default"
		ImportMode.CREATE 	|"create mandatory date picker"				|InputType.DATE_PICKER 	| false 	| "2014-05-13"	| "" 				| Messages.ERROR_MANDATORY_CUF 			| Messages.IMPACT_DEFAULT_VALUE | "2014-05-13"
		ImportMode.CREATE 	|"create unparsable optional date picker"	|InputType.DATE_PICKER 	| true 		| null			| "not a date" 		| Messages.ERROR_UNPARSABLE_DATE 		| Messages.IMPACT_DEFAULT_VALUE | null
		ImportMode.CREATE 	|"create unparsable mandatory datepicker"	|InputType.DATE_PICKER 	| false 	| "2014-05-13"	| "not a date" 		| Messages.ERROR_UNPARSABLE_DATE 		| Messages.IMPACT_DEFAULT_VALUE | "2014-05-13"
		ImportMode.CREATE 	|"create unparsable checkbox"				|InputType.CHECKBOX 	| false 	| "false" 		| "not a boolean" 	| Messages.ERROR_UNPARSABLE_CHECKBOX 	| Messages.IMPACT_DEFAULT_VALUE | "false"
		//UPDATE
		ImportMode.UPDATE 	|"update mandatory oversized plain text"	|InputType.PLAIN_TEXT 	| false 	| "default" 	| overMaxSizeValue	| Messages.ERROR_MAX_SIZE 				| Messages.IMPACT_MAX_SIZE		| substringedValue
		ImportMode.UPDATE 	|"update optional  oversized plain text"	|InputType.PLAIN_TEXT 	| true	 	| "" 			| overMaxSizeValue	| Messages.ERROR_MAX_SIZE 				| Messages.IMPACT_MAX_SIZE		| substringedValue
		ImportMode.UPDATE 	|"update mandatory plain text"				|InputType.PLAIN_TEXT 	| false 	| "default" 	| "" 				| Messages.ERROR_MANDATORY_CUF 			| Messages.IMPACT_NO_CHANGE 	| null
		ImportMode.UPDATE 	|"update mandatory date picker"				|InputType.DATE_PICKER 	| false 	| "2014-05-13"	| "" 				| Messages.ERROR_MANDATORY_CUF 			| Messages.IMPACT_NO_CHANGE 	| null
		ImportMode.UPDATE 	|"update unparsable optional date picker"	|InputType.DATE_PICKER 	| true 		| null			| "not a date" 		| Messages.ERROR_UNPARSABLE_DATE 		| Messages.IMPACT_NO_CHANGE 	| null
		ImportMode.UPDATE 	|"update unparsable mandatory datepicker"	|InputType.DATE_PICKER 	| false 	| "2014-05-13"	| "not a date" 		| Messages.ERROR_UNPARSABLE_DATE 		| Messages.IMPACT_NO_CHANGE 	| null
		ImportMode.UPDATE 	|"update unparsable checkbox"				|InputType.CHECKBOX 	| false 	| "false" 		| "not a boolean" 	| Messages.ERROR_UNPARSABLE_CHECKBOX 	| Messages.IMPACT_NO_CHANGE 	| null
	}

	@Unroll("should validate custom field : #humanMsg")
	def "should validate custom field "() {
		given:
		Map<String, String> cufs = new HashMap<String, String>()
		def code = "001"
		def value = cufValue
		cufs.put(code, value)

		and:
		InputType inputType = cufType

		CustomField cuf = new CustomField(inputType)
		cuf.setCode(code)
		cuf.setOptional(optional)
		def defs = [cuf]

		and:
		TestCaseTarget testCase = Mock()
		ImportMode mode = importMode

		when:
		LogTrain createLog = checkCuf(mode,testCase, cufs, defs)

		then:
		createLog.hasNoErrorWhatsoever()
		where:
		importMode			|humanMsg 										|cufType				| optional	|  cufValue
		//CREATE
		ImportMode.CREATE 	|"create mandatory plain text"					|InputType.PLAIN_TEXT 	| false 	| "a string"
		ImportMode.CREATE 	|"create optional plain text" 					|InputType.PLAIN_TEXT 	| true 		| "a string"
		ImportMode.CREATE 	|"create optional plain text empty string"		|InputType.PLAIN_TEXT 	| true 		| ""
		ImportMode.CREATE 	|"create optional datepicker null"				|InputType.DATE_PICKER 	| true 		| null
		ImportMode.CREATE 	|"create optional datepicker empty string" 		|InputType.DATE_PICKER 	| true 		| ""
		ImportMode.CREATE 	|"create mandatory datepicker"					|InputType.DATE_PICKER 	| false		| "2014-05-13"
		ImportMode.CREATE 	|"create optional datepicker"					|InputType.DATE_PICKER 	| true		| "2014-05-13"
		ImportMode.CREATE 	|"create checkbox false"						|InputType.CHECKBOX 	| false		| "false"
		ImportMode.CREATE 	|"create checkbox true"							|InputType.CHECKBOX 	| false		| "true"
		//UPDATE
		ImportMode.UPDATE 	|"update mandatory plain text"					|InputType.PLAIN_TEXT 	| false 	| "a string"
		ImportMode.UPDATE 	|"update optional plain text" 					|InputType.PLAIN_TEXT 	| true 		| "a string"
		ImportMode.UPDATE 	|"update optional plain text empty string"		|InputType.PLAIN_TEXT 	| true 		| ""
		ImportMode.UPDATE 	|"update optional datepicker null"				|InputType.DATE_PICKER 	| true 		| null
		ImportMode.UPDATE 	|"update optional datepicker empty string" 		|InputType.DATE_PICKER 	| true 		| ""
		ImportMode.UPDATE 	|"update mandatory datepicker"					|InputType.DATE_PICKER 	| false		| "2014-05-13"
		ImportMode.UPDATE 	|"update optional datepicker"					|InputType.DATE_PICKER 	| true		| "2014-05-13"
		ImportMode.UPDATE 	|"update checkbox false"						|InputType.CHECKBOX 	| false		| "false"
		ImportMode.UPDATE 	|"update checkbox true"							|InputType.CHECKBOX 	| false		| "true"
	}

	@Unroll("should validate custom field dropdown list where : #humanMsg")
	def "should validate custom field dropdown list"() {
		given:
		Map<String, String> cufs = new HashMap<String, String>()
		def code = "001"
		def value = cufValue
		cufs.put(code, value)

		and:
		SingleSelectField cuf = new SingleSelectField()
		cuf.setCode(code)
		cuf.setOptional(optional)
		cuf.addOption(new CustomFieldOption("orange", "o"))
		cuf.addOption(new CustomFieldOption("red", "r"))
		cuf.addOption(new CustomFieldOption("blue", "b"))
		cuf.addOption(new CustomFieldOption("pink", "p"))
		def defs = [cuf]

		and:
		TestCaseTarget testCase = Mock()
		ImportMode mode = importMode

		when:
		LogTrain createLog = checkCuf(mode, testCase, cufs, defs)

		then:
		createLog.hasNoErrorWhatsoever()

		where:
		importMode 			|humanMsg 						| optional	|  cufValue
		//CREATE
		ImportMode.CREATE 	|"create mandatory"				| false 	| "blue"
		ImportMode.CREATE 	|"create optional " 			| true 		| "orange"
		ImportMode.CREATE 	|"create optional empty string"	| true 		| ""
		ImportMode.CREATE 	|"create optional null"			| true 		| ""
		//UPDATE
		ImportMode.UPDATE 	|"update mandatory"				| false 	| "blue"
		ImportMode.UPDATE 	|"update optional " 			| true 		| "orange"
		ImportMode.UPDATE 	|"update optional empty string"	| true 		| ""
		ImportMode.UPDATE 	|"update optional null"			| true 		| ""
	}

	@Unroll("should not validate custom field dropdown list where : #humanMsg")
	def "should not validate custom field dropdown list"() {
		given:
		Map<String, String> cufs = new HashMap<String, String>()
		def code = "001"
		def value = cufValue
		cufs.put(code, value)

		and:

		SingleSelectField cuf = new SingleSelectField()
		cuf.setCode(code)
		cuf.setOptional(optional)
		cuf.addOption(new CustomFieldOption("orange", "o"))
		cuf.addOption(new CustomFieldOption("red", "r"))
		cuf.addOption(new CustomFieldOption("blue", "b"))
		cuf.addOption(new CustomFieldOption("pink", "p"))
		cuf.setDefaultValue(defaultValue)
		def defs = [cuf]

		and:
		TestCaseTarget testCase = Mock()
		ImportMode mode = importMode
		when:
		LogTrain createLog = checkCuf(mode,testCase, cufs, defs)

		then:
		LogEntry entry = createLog.entries[0]
		entry.getI18nError() == errorKey
		entry.getI18nImpact() == impactKey
		cufs.get(code)  == newValue
		where:
		importMode 			|humanMsg 							| optional 	| defaultValue 	| cufValue 			| errorKey 								| impactKey 					| newValue
		//CREATE
		ImportMode.CREATE 	|"create mandatory unparsable"		| false 	| "orange" 		| "not a color" 	| Messages.ERROR_UNPARSABLE_OPTION 		| Messages.IMPACT_DEFAULT_VALUE	| "orange"
		ImportMode.CREATE 	|"create mandatory void"			| false 	| "pink"		| "" 				| Messages.ERROR_MANDATORY_CUF 			| Messages.IMPACT_DEFAULT_VALUE	| "pink"
		ImportMode.CREATE 	|"create mandatory null"			| false 	| "blue"		| null 				| Messages.ERROR_MANDATORY_CUF 			| Messages.IMPACT_DEFAULT_VALUE	| "blue"
		ImportMode.CREATE 	|"create optional unparsable"		| true 		| null			| "not a color" 	| Messages.ERROR_UNPARSABLE_OPTION 		| Messages.IMPACT_DEFAULT_VALUE	| null
		//UPDATE
		ImportMode.UPDATE 	|"update mandatory unparsable"		| false 	| "orange" 		| "not a color" 	| Messages.ERROR_UNPARSABLE_OPTION 		| Messages.IMPACT_NO_CHANGE		| null
		ImportMode.UPDATE 	|"update mandatory void"			| false 	| "pink"		| "" 				| Messages.ERROR_MANDATORY_CUF 			| Messages.IMPACT_NO_CHANGE		| null
		ImportMode.UPDATE 	|"update mandatory null"			| false 	| "blue"		| null 				| Messages.ERROR_MANDATORY_CUF 			| Messages.IMPACT_NO_CHANGE		| null
		ImportMode.UPDATE 	|"update optional unparsable"		| true 		| null			| "not a color" 	| Messages.ERROR_UNPARSABLE_OPTION 		| Messages.IMPACT_NO_CHANGE		| null
	}

	private LogTrain checkCuf(ImportMode mode, TestCaseTarget testCase, Map cufs, List defs) {
		LogTrain createLog = null
		switch(mode){
			case ImportMode.CREATE :
				createLog = cufValidator.checkCreateCustomFields(testCase, cufs, defs)
				break
			case ImportMode.UPDATE:
				createLog = cufValidator.checkUpdateCustomFields(testCase, cufs, defs)
				break
		}

		return createLog
	}
}

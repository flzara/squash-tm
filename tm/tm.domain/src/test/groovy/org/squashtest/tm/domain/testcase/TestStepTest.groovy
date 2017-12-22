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
package org.squashtest.tm.domain.testcase;

import java.lang.reflect.Modifier;

import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.audit.AuditableSupport;
import org.squashtest.tm.domain.testcase.ActionTestStep;

import spock.lang.Specification


class TestStepTest extends Specification {
	def "new step should not have attachments"() {
		when:
		ActionTestStep testStep = new ActionTestStep()


		then:
		!testStep.attachmentList.hasAttachments()
	}

	def "step should say it has attachments"() {
		given:
		ActionTestStep testStep = new ActionTestStep()

		when:
		Attachment att = new Attachment()
		testStep.getAttachmentList().addAttachment att

		then:
		testStep.attachmentList.hasAttachments()
	}
	def "should create blank test case"() {
		given:
		def findFields
		findFields = { it ->
			List fields = it.declaredFields
			def sc = it.superclass
			if (sc != null) {
				fields.addAll(findFields(sc))
			}

			return fields
		}

		and:
		List blankableFields = findFields(ActionTestStep)
				.findAll({ !(Collection.isAssignableFrom(it.type) || Map.isAssignableFrom(it.type)) })
				.findAll({ !(Modifier.isStatic(it.modifiers) || Modifier.isFinal(it.modifiers))})
				.findAll({ ! [Long.TYPE, Integer.TYPE, Boolean.TYPE, Float.TYPE, Double.TYPE].contains(it.type) })
				.each { it.setAccessible(true) }
		println blankableFields
		when:
		def res = ActionTestStep.createBlankActionStep()

		then:
		blankableFields.findAll({ it.get(res) != null })*.name == []

	}

}

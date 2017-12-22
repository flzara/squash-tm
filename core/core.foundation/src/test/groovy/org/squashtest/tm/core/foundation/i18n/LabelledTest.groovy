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
package org.squashtest.tm.core.foundation.i18n

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class LabelledTest extends Specification {
	Labelled labelled = new Labelled()
	def labelKey = "label.key"
	MessageSource messageSource = Mock()
	
	@Unroll
	def "should return label '#label' for current locale '#locale'"() {
		given:
		labelled.labelKey = labelKey
		
		and:
		LocaleContextHolder.setLocale(locale)
		
		and:
		messageSource.getMessage(labelKey, _, labelKey, locale) >> label
		labelled.messageSource = messageSource
		
		expect:
		label == labelled.label
		
		where:
		label           | locale
		"teh label"     | Locale.ENGLISH
		"l'étiquette"   | Locale.FRENCH
		"ザラベル"         | Locale.JAPANESE
	}
}

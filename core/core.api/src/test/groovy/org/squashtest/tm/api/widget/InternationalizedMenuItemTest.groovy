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
package org.squashtest.tm.api.widget

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.api.widget.InternationalizedMenuItem;
import org.squashtest.tm.core.foundation.i18n.Labelled;

import spock.lang.Specification
import spock.lang.Unroll;

/**
 * @author Gregory　Fouquet
 *
 */
class InternationalizedMenuItemTest extends Specification {
	InternationalizedMenuItem item = new InternationalizedMenuItem()
	def tooltipKey = "tooltip.key"
	MessageSource messageSource = Mock()
	
	@Unroll
	def "should return tooltip '#tooltip' for current locale '#locale'"() {
		given:
		item.tooltipKey = tooltipKey
		
		and:
		LocaleContextHolder.setLocale(locale)
		
		and:
		messageSource.getMessage(tooltipKey, _, tooltipKey, locale) >> tooltip
		item.messageSource = messageSource
		
		expect:
		tooltip == item.tooltip
		
		where:
		tooltip       | locale
		"the tooltip" | Locale.ENGLISH
		"l'infobulle" | Locale.FRENCH
		"ラベル"        | Locale.JAPANESE
	}

}

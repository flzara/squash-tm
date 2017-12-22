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
package org.squashtest.tm.web.internal.helper;

import java.lang.reflect.Constructor;

import org.springframework.context.MessageSource;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.Level;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class InternationalisableLabelFormatterTest extends Specification {
	MessageSource messages = Mock()

	@Unroll
	def "#clazz.simpleName should produce escaped html by default"() {
		given:
		messages.getMessage(_, _, _) >> "échappé"

		Constructor ctor = clazz.getDeclaredConstructor(MessageSource)
		LabelFormatter formatter = ctor.newInstance(messages)
		
		Level level = Mock()
		level.i18nKey >> "some.key"
		level.level >> 1
		

		expect:
		label == formatter.formatLabel(level)
		
		where: 
		clazz << [	InternationalizableLabelFormatter, LevelLabelFormatter ]
		label << [ "&eacute;chapp&eacute;", "1-&eacute;chapp&eacute;" ] 
	}
	@Unroll
	def "#clazz.simpleName should produce escaped html as asked"() {
		given:
		messages.getMessage(_, _, _) >> "échappé"

		Constructor ctor = clazz.getDeclaredConstructor(MessageSource)
		LabelFormatter formatter = ctor.newInstance(messages)
		
		Level level = Mock()
		level.i18nKey >> "some.key"
		level.level >> 1
		
		when:
		formatter.plainText().escapeHtml()
		
		then:
		label == formatter.formatLabel(level)
		
		where: 
		clazz << [	InternationalizableLabelFormatter, LevelLabelFormatter ]
		label << [ "&eacute;chapp&eacute;", "1-&eacute;chapp&eacute;" ] 
	}
	@Unroll
	def "#clazz.simpleName should produce unescaped html as asked"() {
		given:
		messages.getMessage(_, _, _) >> "échappé"

		Constructor ctor = clazz.getDeclaredConstructor(MessageSource)
		LabelFormatter formatter = ctor.newInstance(messages)
		
		Level level = Mock()
		level.i18nKey >> "some.key"
		level.level >> 1
		
		when:
		formatter.plainText()
		
		then:
		label == formatter.formatLabel(level)
		
		where: 
		clazz << [	InternationalizableLabelFormatter, LevelLabelFormatter ]
		label << [ "échappé", "1-échappé" ] 
	}
}
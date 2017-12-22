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
package org.squashtest.tm.web.internal.model.builder

import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.helper.LabelFormatter;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder

import com.fasterxml.jackson.databind.ObjectMapper;

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class EnumJeditableComboDataBuilderTest extends Specification {
	EnumJeditableComboDataBuilder builder = new EnumJeditableComboDataBuilder();
	Locale locale = Locale.default
	
	def setup() {
		new JsonHelper(new ObjectMapper())
		
		builder.model = DummyEnum.values()
		builder.labelFormatter = new DummyLabelFormatter()
		builder.modelComparator = new DummyEnumComparator()
	}
	
	def "should build ordered map using given comparator"() {
		when:
		def res = builder
			.useLocale(locale)
			.buildMap()

		then:
		res == ["ONE": "un", "TWO" : "deux"]
	}
	
	def "should build unordered map"() {
		given:
		builder.modelComparator = null

		when:
		def res = builder
			.useLocale(locale)
			.buildMap()

		then:
		res == ["TWO": "deux", "ONE" : "un"]
	}
	
	def "should build map containing the selected item"() {
		given:
		builder.modelComparator = null

		when:
		def res = builder
			.useLocale(locale)
			.selectItem(DummyEnum.TWO)
			.buildMap()

		then:
		res == ["ONE": "un", "TWO" : "deux", "selected": "TWO"]
	}
	
	def "should build json using given comparator"() {
		when:
		def res = builder
			.useLocale(locale)
			.buildMarshalled()

		then:
		res == '{"ONE":"un","TWO":"deux"}';
	}
	
	def "labels should be formatted using the given locale"() {
		given:
		builder.modelComparator = null
		
		and: 
		LabelFormatter formatter = Mock()
		builder.labelFormatter = formatter

		when:
		def res = builder
			.useLocale(locale)
			.selectItem(DummyEnum.TWO)
			.buildMarshalled()

		then:
		1 * formatter.useLocale(locale)
		1 * formatter.formatLabel(DummyEnum.TWO)
		1 * formatter.formatLabel(DummyEnum.ONE)
	}


}


public class DummyEnumComparator implements Comparator<DummyEnum> {
	public int compare(DummyEnum a,  DummyEnum b) {
		return a.order.compareTo(b.order)
	}
}

public class DummyLabelFormatter implements LabelFormatter<DummyEnum> {

	@SuppressWarnings("unchecked")
	@Override
	public LabelFormatter useLocale(Locale locale) {
		// NOOP
		return this;
	}

	@Override
	public String formatLabel(DummyEnum toFormat) {
		switch (toFormat) {
			case DummyEnum.ONE : return "un"
			case DummyEnum.TWO : return "deux"
			default : return "default"
		}		
	}

	/**
	 * @see org.squashtest.tm.web.internal.helper.LabelFormatter#escapeHtml()
	 */
	@Override
	public LabelFormatter<DummyEnum> escapeHtml() {
		return this;
	}

	/**
	 * @see org.squashtest.tm.web.internal.helper.LabelFormatter#plainText()
	 */
	@Override
	public LabelFormatter<DummyEnum> plainText() {
		return this;
	}
	
}

public enum DummyEnum {
	TWO(2),
	ONE(1);

	final int order;

	DummyEnum(int order) {
		this.order = order
	}
	
}

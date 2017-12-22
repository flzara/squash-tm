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
package org.squashtest.tm.web.internal.model.builder;

import org.squashtest.tm.domain.LevelComparator;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Builds a serialized json data model which can be used by a Jeditable combobox. Usage : <code>
 * String json = new EnumJeditableComboDataBuilder()
 *     .selectItem(selectedItem)
 *     .useLocale(locale)
 *     .buildMarshalled()
 * </code>
 *
 * The builder can produce a Map view instead of marshalled JSON. Selected item and comparator are optional.
 *
 * Objects of this class are not thread-safe.
 *
 * @author Gregory Fouquet
 *
 */
public class ListJeditableComboDataBuilder<T extends List<?>, B extends ListJeditableComboDataBuilder<T, B>> {
	/**
	 * The locale that should be used to format the labels.
	 */
	private Locale locale;
	/**
	 * The required model. Should be injected.
	 */
	private List<String> model;
	/**
	 * The optional comparator used to sort the model. Should be injected.
	 */
	private LevelComparator modelComparator;

	/**
	 * The required formatter which produces item labels. Should be injected.
	 */
	private LevelLabelFormatter labelFormatter;

	/**
	 * The list of items used as the model for the combobox.
	 *
	 * @param userList
	 *            The combobox model. Should not be <code>null</code>.
	 */
	public void setModel(@NotNull List<String> userList) {
		this.model = userList;
	}

	/**
	 * @param formatter
	 *            the formatter to set
	 */
	public void setLabelFormatter(LevelLabelFormatter formatter) {
		this.labelFormatter = formatter;
	}

	/**
	 * The array of items used as the model for the combobox.
	 *
	 * @param model
	 *            The combobox model. Should not be <code>null</code>.
	 */
	public void setModel(@NotNull String[] model) {
		this.model = Arrays.asList(model);
	}
	/**
	 * @param levelComparator
	 *            the comparator to set
	 */
	public void setModelComparator(LevelComparator levelComparator) {
		this.modelComparator = levelComparator;
	}

	/**
	 *
	 * @return a {@link Map} representation of the model compatible with a JEditable combobox.
	 */
	public Map<String, String> buildMap() {

		return createComboData();
	}

	private Map<String, String> createComboData() {
		Map<String, String> comboData = new LinkedHashMap<>(model.size());

		labelFormatter.useLocale(locale);

		for (String item : model) {
			comboData.put(item, item);
		}

		return comboData;
	}

	@SuppressWarnings("unchecked")
	public B useLocale(Locale locale) {
		this.locale = locale;
		return (B) this;
	}

}

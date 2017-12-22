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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.helper.LabelFormatter;

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
public class EnumJeditableComboDataBuilder<T extends Enum<?>, B extends EnumJeditableComboDataBuilder<T, B>> {
	/**
	 * The locale that should be used to format the labels.
	 */
	private Locale locale;
	/**
	 * The required model. Should be injected.
	 */
	private List<T> model;
	/**
	 * The optional comparator used to sort the model. Should be injected.
	 */
	private Comparator<? super T> modelComparator;

	/**
	 * The required formatter which produces item labels. Should be injected.
	 */
	private LabelFormatter<? super T> labelFormatter;
	/**
	 * The optional selected item.
	 */
	private T selectedItem;

	/**
	 * The list of items used as the model for the combobox.
	 * 
	 * @param model
	 *            The combobox model. Should not be <code>null</code>.
	 */
	public void setModel(@NotNull List<T> model) {
		this.model = model;
	}

	/**
	 * @param comparator
	 *            the comparator to set
	 */
	public void setModelComparator(Comparator<? super T> comparator) {
		this.modelComparator = comparator;
	}

	/**
	 * @param formatter
	 *            the formatter to set
	 */
	public void setLabelFormatter(LabelFormatter<? super T> formatter) {
		this.labelFormatter = formatter;
	}

	/**
	 * The array of items used as the model for the combobox.
	 * 
	 * @param model
	 *            The combobox model. Should not be <code>null</code>.
	 * @return
	 */
	public void setModel(@NotNull T[] model) {
		this.model = Arrays.asList(model);
	}

	/**
	 * 
	 * @return a marshalled JSON representation of the model compatible with a JEditable combobox.
	 */
	public String buildMarshalled() {
		return JsonHelper.serialize(buildMap());
	}

	/**
	 * 
	 * @return a {@link Map} representation of the model compatible with a JEditable combobox.
	 */
	public Map<String, String> buildMap() {
		sortModelIfRequired();

		Map<String, String> comboData = createComboData();

		addSelectedItemIfRequired(comboData);

		return comboData;
	}

	private Map<String, String> createComboData() {
		Map<String, String> comboData = new LinkedHashMap<>(model.size());

		labelFormatter.useLocale(locale);

		for (T item : model) {
			comboData.put(itemKey(item), labelFormatter.formatLabel(item));
		}

		return comboData;
	}

	/**
	 * Returns the key which will be used in combo data for the given item. defaults to the items's name.
	 * 
	 * @param item
	 * @return the key  for the given item.
	 */
	protected String itemKey(T item) {
		return item.name();
	}

	private void addSelectedItemIfRequired(Map<String, String> comboData) {
		if (selectedItem != null) {
			comboData.put("selected", selectedItem.name());
		}
	}

	private void sortModelIfRequired() {
		if (modelComparator != null) {
			Collections.sort(model, modelComparator);
		}
	}

	/**
	 * /!\ if no <code>selectedItem</code> is indicated, the widget will perform a selection using its label when in
	 * unediting state. This behaviour is usually sufficient. On the other hand, adding a selected item induces boundary
	 * effects when the combobox model is not fetched when switching to editing state (ie widget is build with a 'data'
	 * attribute instead of a 'loadurl' attribute).
	 * 
	 * @param selectedItem
	 *            The item to select. Can be <code>null</code>.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public B selectItem(T selectedItem) {
		this.selectedItem = selectedItem;
		return (B) this;
	}

	@SuppressWarnings("unchecked")
	public B useLocale(Locale locale) {
		this.locale = locale;
		return (B) this;
	}

	/**
	 * For internal use only.
	 * 
	 * @return the selected item, might be null.
	 */
	protected final T getSelectedItem() {
		return selectedItem;
	}

}

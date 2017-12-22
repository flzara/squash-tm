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
package org.squashtest.tm.web.internal.controller.search.advanced.searchinterface;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.core.foundation.i18n.Internationalizable;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.LevelComparator;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.internal.dto.json.JsonProject;
import org.squashtest.tm.service.testcase.TestCaseAdvancedSearchService;
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

/**
 * Common code factored out of subclasses
 *
 * @author Gregory Fouquet
 *
 */
public abstract class SearchInterfaceDescription {
	/**
	 * Compares formatted label using the thread-bound locale when instanciated -> not thread safe.
	 *
	 * @author Gregory Fouquet
	 *
	 * @param <T>
	 */
	private class InternationalizableComparator implements Comparator<Internationalizable> {
		private InternationalizableLabelFormatter helper = (InternationalizableLabelFormatter) internationalizableLabelFormatter
				.get().useLocale(LocaleContextHolder.getLocale());

		@Override
		public int compare(Internationalizable o1, Internationalizable o2) {
			String name1 = helper.formatLabel(o1);
			String name2 = helper.formatLabel(o2);

			return name1.compareTo(name2);
		}
	}

	/**
	 * Builder for options aka {@link SearchInputPossibleValueModel}
	 *
	 * @author Gregory Fouquet
	 *
	 */
	protected final class OptionBuilder {
		private final Locale locale;
		private String label;
		private String key;
		private boolean selected;

		private OptionBuilder(Locale locale) {
			super();
			this.locale = locale;
		}

		public OptionBuilder labelI18nKey(String i18nKey) {
			this.label = messageSource.internationalize(i18nKey, locale);
			return this;
		}

		public OptionBuilder label(String label) {
			this.label = label;
			return this;
		}

		public OptionBuilder optionKey(String key) {
			this.key = key;
			return this;
		}

		public SearchInputPossibleValueModel build() {
			SearchInputPossibleValueModel res = new SearchInputPossibleValueModel(label, key, selected);
			reset();
			return res;
		}

		private void reset() {
			selected = false;
		}

		public OptionBuilder selected() {
			selected = true;
			return this;
		}
	};

	protected final class OptionListBuilder {
		private final EnumJeditableComboDataBuilder<?, ?> delegateBuilder;
		private Locale locale;

		private OptionListBuilder(EnumJeditableComboDataBuilder<?, ?> delegateBuilder) {
			super();
			this.delegateBuilder = delegateBuilder;
		}

		public OptionListBuilder useLocale(Locale locale) {
			delegateBuilder.useLocale(locale);
			this.locale = locale;
			return this;
		}

		public List<SearchInputPossibleValueModel> build() {
			Map<String, String> map = delegateBuilder.buildMap();
			return decorate(map);

		}

		private List<SearchInputPossibleValueModel> decorate(Map<String, String> map) {
			OptionBuilder optionBuilder = new OptionBuilder(locale);
			ArrayList<SearchInputPossibleValueModel> res = new ArrayList<>(map.size());

			for (Entry<String, String> entry : map.entrySet()) {
				res.add(optionBuilder.optionKey(entry.getKey()).label(entry.getValue()).build());
			}

			return res;
		}
	}

	protected final class PerimeterPanelBuilder {
		private final Locale locale;
		private String cssClass;
		private String htmlId;

		private PerimeterPanelBuilder(Locale locale) {
			super();
			this.locale = locale;
		}

		/**
		 * the css class of the panel
		 *
		 * @param cssClass
		 * @return
		 */
		public PerimeterPanelBuilder cssClass(String cssClass) {
			this.cssClass = cssClass;
			return this;
		}

		/**
		 * the html id of the search field
		 *
		 * @param id
		 * @return
		 */
		public PerimeterPanelBuilder htmlId(String id) {
			this.htmlId = id;
			return this;
		}

		public SearchInputPanelModel build(Collection<JsonProject> jsProjects) {

			SearchInputPanelModel panel = new SearchInputPanelModel();
			panel.setTitle(messageSource.internationalize("search.testcase.perimeter.field.title", locale));
			panel.setOpen(true);
			panel.setId("perimeter");
			panel.setLocation("column1");
			panel.addCssClass(cssClass);


			SearchInputFieldModel projectField = new SearchInputFieldModel(htmlId, messageSource.internationalize(
					"search.testcase.perimeter.field.title", locale), MULTISELECTPERIMETER);
			panel.addField(projectField);

			OptionBuilder optionBuilder = optionBuilder(locale);

			List<JsonProject> orderedProjects = jsProjects.stream()
				.sorted(Comparator.comparing(JsonProject::getName))
				.collect(Collectors.toList());

			for (JsonProject project : orderedProjects) {
				SearchInputPossibleValueModel projectOption = optionBuilder.label(project.getName())
					.optionKey(String.valueOf(project.getId())).build();
				projectField.addPossibleValue(projectOption);
			}

			return panel;
		}
	}

	protected static final String TEXTFIELD = "textfield";
	protected static final String TEXTFIELDID = "textfieldid";
	protected static final String TEXTFIELDREFERENCE = "textfieldreference";
	protected static final String TEXTAREA = "textarea";
	protected static final String RANGE = "range";
	//we need a specific input type for "true" numeric value,
	//because with the range type above, some padding is applied to index and queries
	protected static final String NUMERICRANGE = "numericrange";
	protected static final String EXISTS = "exists";
	protected static final String DATE = "date";
	protected static final String MULTISELECT = "multiselect";
	protected static final String MULTISELECTPERIMETER = "multiselectperimeter";
	protected static final String MULTIAUTOCOMPLETE = "multiautocomplete";
	protected static final String MULTICASCADEFLAT = "multicascadeflat";
	protected static final String RADIOBUTTON = "radiobutton";
	protected static final String CHECKBOX = "checkbox";
	protected static final String ATLEASTONE = "1";
	protected static final String NONE = "0";
	protected static final String EMPTY = "";
	protected static final String TRUE = "true";
	protected static final String FALSE = "false";

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatter;

	@Inject
	private Provider<InternationalizableLabelFormatter> internationalizableLabelFormatter;

	@Inject
	TestCaseAdvancedSearchService advancedSearchService;

	/**
	 *
	 */
	public SearchInterfaceDescription() {
		super();
	}


	public SearchInputPanelModel createMilestonePanel(Locale locale){

		/*
		 * Additional specs from issue 4667
		 *
		 * 1/ the whole feature is now activated by a checkbox,
		 * 2/ sort the milestones alphabetically,
		 * 3/ don't show milestones having status=PLANNED
		 */


		SearchInputPanelModel panel = new SearchInputPanelModel();
		panel.setTitle(getMessageSource().internationalize("label.Milestones", locale));
		panel.setOpen(true);
		panel.setId("milestone");
		panel.setLocation("column1");
		panel.addCssClass("search-icon-attributes");

		OptionBuilder optionBuilder = optionBuilder(locale);

		// fields declaration

		SearchInputFieldModel searchByMilestone = new SearchInputFieldModel("searchByMilestone", getMessageSource()
				.internationalize("search.milestone.search-by-milestone", locale), CHECKBOX);

		SearchInputFieldModel labelField = new SearchInputFieldModel("milestone.label", getMessageSource()
				.internationalize("label.Label", locale), MULTISELECT);

		SearchInputFieldModel statusField = new SearchInputFieldModel("milestone.status", getMessageSource()
				.internationalize("label.Status", locale), MULTISELECT);

		SearchInputFieldModel endDateField = new SearchInputFieldModel("milestone.endDate", getMessageSource()
				.internationalize("label.EndDate", locale), DATE);


		panel.addField(searchByMilestone);
		panel.addField(labelField);
		panel.addField(statusField);
		panel.addField(endDateField);

		// populate the content of these fields

		List<JsonMilestone> milestones = advancedSearchService.findAllVisibleMilestonesToCurrentUser();
		Collections.sort(milestones, (p1, p2) -> p1.getLabel().compareTo(p2.getLabel()));

		for (JsonMilestone milestone : milestones){
			Integer integer = new Integer(String.valueOf(milestone.getId()));
			if (milestone.getStatus() != MilestoneStatus.PLANNED){
				labelField.addPossibleValue(optionBuilder.label(milestone.getLabel()).optionKey(integer.toString()).build());
			}
		}

		List<SearchInputPossibleValueModel>  statusOptions = levelComboBuilder(new MilestoneStatus[]{
				MilestoneStatus.IN_PROGRESS, MilestoneStatus.FINISHED, MilestoneStatus.LOCKED
		})
		.useLocale(locale).build();
		statusField.addPossibleValues( statusOptions);
		return panel;

	}

	protected final <T extends Enum<?> & Level> OptionListBuilder levelComboBuilder(T[] values) {
		return new OptionListBuilder(delegateLevelComboBuilder(values));
	}

	protected final <T extends Enum<?> & Level, B extends LevelComboDataBuilder<T, B>> EnumJeditableComboDataBuilder<T, B> delegateLevelComboBuilder(T[] values) {
		EnumJeditableComboDataBuilder<T, B> builder = new LevelComboDataBuilder<>();
		builder.setLabelFormatter(levelLabelFormatter.get().plainText());
		builder.setModel(values);
		builder.setModelComparator(LevelComparator.getInstance());
		return builder;
	}

	protected final <T extends Enum<?> & Level> OptionListBuilder internationalizableComboBuilder(T[] values) {
		return new OptionListBuilder(delegateInternationalizableComboBuilder(values));
	}

	protected final <T extends Enum<?> & Level, B extends EnumJeditableComboDataBuilder<T, B>> EnumJeditableComboDataBuilder<T, B> delegateInternationalizableComboBuilder(
			T[] values) {
		EnumJeditableComboDataBuilder<T, B> builder = new EnumJeditableComboDataBuilder<>();
		builder.setLabelFormatter(internationalizableLabelFormatter.get().plainText());
		builder.setModel(values);
		builder.setModelComparator(new InternationalizableComparator());
		return builder;
	}

	/**
	 * @return the messageSource
	 */
	protected final InternationalizationHelper getMessageSource() {
		return messageSource;
	}

	protected final OptionBuilder optionBuilder(Locale locale) {
		return new OptionBuilder(locale);
	}

	public PerimeterPanelBuilder perimeterPanelBuilder(Locale locale) {
		return new PerimeterPanelBuilder(locale);
	}
}

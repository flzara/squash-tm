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
package org.squashtest.tm.service.internal.customfield;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;

/**
 * @author bsiri, Gregory Fouquet
 *
 * @param <X>
 */
public abstract class AbstractCustomFieldHelper<X extends BoundEntity> implements CustomFieldHelper<X> {

	private CustomFieldValueManagerService cufValuesService;

	protected CustomFieldBindingFinderService cufBindingService;

	/**
	 * When called, this method should initialize the {@link #customFields} attribute.
	 */
	protected abstract void initCustomFields();

	protected abstract List<CustomFieldValue> doGetCustomFieldValues();

	private Collection<RenderingLocation> locations;
	private CustomFieldDefinitionStrategy addingStrategy = CustomFieldDefinitionStrategy.INTERSECTION;
	protected List<CustomField> customFields;

	protected static enum CustomFieldDefinitionStrategy {

		INTERSECTION() {
			@SuppressWarnings("unchecked")
			@Override
			List<CustomField> add(List<CustomField> orig, List<CustomField> addition) {
				if (orig.isEmpty()) {
					return addition;
				} else {
					return new ArrayList<>(CollectionUtils.intersection(orig, addition));
				}
			}
		},
		UNION() {
			@Override
			List<CustomField> add(List<CustomField> orig, List<CustomField> addition) {
				orig.addAll(addition);
				return orig;
			}
		};

		abstract List<CustomField> add(List<CustomField> orig, List<CustomField> addition);
	}

	/**
	 *
	 */
	public AbstractCustomFieldHelper() {
		super();
	}

	@Override
	public CustomFieldHelper<X> setRenderingLocations(RenderingLocation... locations) {
		this.locations = Arrays.asList(locations);
		return this;
	}

	@Override
	public CustomFieldHelper<X> setRenderingLocations(Collection<RenderingLocation> locations) {
		this.locations = locations;
		return this;
	}

	@Override
	public CustomFieldHelper<X> restrictToCommonFields() {
		addingStrategy = CustomFieldDefinitionStrategy.INTERSECTION;
		return this;
	}

	@Override
	public CustomFieldHelper<X> includeAllCustomFields() {
		addingStrategy = CustomFieldDefinitionStrategy.UNION;
		return this;
	}

	@Override
	public final List<CustomField> getCustomFieldConfiguration() {
		if (!isInited()) {
			initCustomFields();
		}

		return customFields;
	}

	@Override
	public final List<CustomFieldValue> getCustomFieldValues() {

		if (!isInited()) {
			initCustomFields();
		}

		return doGetCustomFieldValues();

	}

	private boolean isInited() {
		return customFields != null;
	}

	/**
	 * Return the CustomFields referenced by the CustomFieldBindings for the given project and BindableEntity type,
	 * ordered by their position. The location argument is optional, if set then only the custom fields that are
	 * rendered in at least one of these locations will be returned.
	 *
	 * @param projectId
	 * @param entityType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final List<CustomField> findCustomFields(long projectId, BindableEntity entityType,
			Collection<RenderingLocation> optionalLocations) {

		List<CustomFieldBinding> bindings = cufBindingService
				.findCustomFieldsForProjectAndEntity(projectId, entityType);

		Collections.sort(bindings, new BindingSorter());

		if (optionalLocations != null && !optionalLocations.isEmpty()) {
			CollectionUtils.filter(bindings, new BindingLocationFilter(optionalLocations));
		}

		return (List<CustomField>) CollectionUtils.collect(bindings, new BindingFieldCollector());

	}

	@SuppressWarnings("serial")
	protected static final class BindingSorter implements Comparator<CustomFieldBinding>, Serializable {

		@Override
		public int compare(CustomFieldBinding o1, CustomFieldBinding o2) {
			return o1.getPosition() - o2.getPosition();
		}
	}

	protected static final class BindingLocationFilter implements Predicate {

		private Collection<RenderingLocation> locations;
		private boolean automaticallyPassed = false;

		BindingLocationFilter(Collection<RenderingLocation> locations) {
			this.locations = locations;
			automaticallyPassed = locations == null || locations.isEmpty();
		}

		@Override
		public boolean evaluate(Object binding) {
			return automaticallyPassed
					|| CollectionUtils.containsAny(locations, ((CustomFieldBinding) binding).getRenderingLocations());
		}

	}

	protected static final class BindingFieldCollector implements Transformer {

		@Override
		public Object transform(Object arg0) {
			CustomFieldBinding binding = (CustomFieldBinding) arg0;
			return binding.getCustomField();
		}

	}

	protected void setCufBindingService(CustomFieldBindingFinderService cufBindingService) {
		this.cufBindingService = cufBindingService;
	}

	/**
	 * @return the cufValuesService
	 */
	protected CustomFieldValueManagerService getCufValuesService() {
		return cufValuesService;
	}

	protected void setCufValuesService(CustomFieldValueManagerService cufValuesService) {
		this.cufValuesService = cufValuesService;
	}

	/**
	 * @return the addingStrategy
	 */
	protected final CustomFieldDefinitionStrategy getAddingStrategy() {
		return addingStrategy;
	}

	/**
	 * @return the locations
	 */
	protected Collection<RenderingLocation> getLocations() {
		return locations;
	}

}

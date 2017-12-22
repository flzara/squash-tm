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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.service.customfield.CustomFieldHelper;

public class CustomFieldHelperImpl<X extends BoundEntity> extends AbstractCustomFieldHelper<X> implements
		CustomFieldHelper<X> {

	// ************ attributes ******************

	private List<CustomFieldValue> values;
	private final Collection<X> entities;

	// ************* code ************************

	public CustomFieldHelperImpl(X entity) {
		this.entities = new ArrayList<>();
		this.entities.add(entity);
	}

	public CustomFieldHelperImpl(Collection<X> entities) {
		this.entities = entities;
	}

	@Override
	protected List<CustomFieldValue> doGetCustomFieldValues() {
		if (values == null) {
			values = findRestrictedCustomFieldValues(entities, customFields);
		}

		return values;
	}

	/**
	 * returns the flattened collection of custom fields associated to all the entities in arguments, restricted to only
	 * the supplied customfields.
	 *
	 * @param entities
	 * @param optionalLocations
	 * @return
	 */
	protected List<CustomFieldValue> findRestrictedCustomFieldValues(Collection<? extends BoundEntity> entities,
			Collection<CustomField> customFields) {

		if (entities.isEmpty() || customFields.isEmpty()) {
			return Collections.emptyList();
		}

		return getCufValuesService().findAllCustomFieldValues(entities, customFields);
	}

	// ******************* utilities **************************

	@Override
	@SuppressWarnings("unchecked")
	protected void initCustomFields() {
		if (!entities.isEmpty()) {

			// restrict the number of queries we must perform : 1 per pair of (project, bindableentity)
			Collection<BindingTarget> targets = CollectionUtils.collect(entities, new BindingTargetCollector());
			retainUniques(targets);

			customFields = new ArrayList<>();

			// collect the result
			for (BindingTarget target : targets) {
				customFields = getAddingStrategy().add(customFields,
						findCustomFields(target.getProjectId(), target.getBindableEntity(), getLocations()));

			}

			// eliminate multiple occurences
			retainUniques(customFields);
		} else {
			customFields = Collections.emptyList();
		}
	}

	private <Y> void retainUniques(Collection<Y> argument) {
		Set<Y> set = new LinkedHashSet<>(argument);
		argument.clear();
		argument.addAll(set);
	}

	// *************************** utility classes **********************************
	private static class BindingTarget {

		private Long projectId;
		private BindableEntity bindableEntity;

		BindingTarget(BoundEntity entity) {
			this.projectId = entity.getProject().getId();
			this.bindableEntity = entity.getBoundEntityType();
		}

		public Long getProjectId() {
			return projectId;
		}

		public BindableEntity getBindableEntity() {
			return bindableEntity;
		}

		@Override
		public int hashCode() { // GENERATED:START
			final int prime = 73;
			int result = 17;
			result = prime * result + (bindableEntity == null ? 0 : bindableEntity.hashCode());
			result = prime * result + (projectId == null ? 0 : projectId.hashCode());
			return result;
		} // GENERATED:END

		@Override
		public boolean equals(Object obj) { // GENERATED:START
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BindingTarget other = (BindingTarget) obj;
			if (bindableEntity != other.bindableEntity)
				return false;
			if (projectId == null) {
				if (other.projectId != null)
					return false;
			} else if (!projectId.equals(other.projectId))
				return false;
			return true;
		} // GENERATED:END

	}

	private static final class BindingTargetCollector implements Transformer {
		@Override
		public Object transform(Object arg0) {
			return new BindingTarget((BoundEntity) arg0);
		}
	}

}

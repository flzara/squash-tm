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
package org.squashtest.tm.domain.event;

import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.requirement.RequirementVersion;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Will log when the value of a property of a requirement changed. For technical reasons and optimization, large
 * properties (typically CLOBS) will be logged in a sister class : RequirementLargePropertyChange
 *
 * @author bsiri
 */
@Entity
@PrimaryKeyJoinColumn(name = "EVENT_ID")
public class RequirementPropertyChange extends RequirementAuditEvent implements RequirementVersionModification,
	ChangedProperty {


	@NotNull
	@Size(max = 100)
	private String propertyName;
	@Size(max = 255)
	private String oldValue;
	@Size(max = 255)
	private String newValue;

	public RequirementPropertyChange() {
		super();
	}

	private RequirementPropertyChange(RequirementVersion requirementVersion, String author) {
		super(requirementVersion, author);
	}

	/**
	 * @see org.squashtest.tm.domain.event.RequirementVersionModification#getPropertyName()
	 */
	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public String getOldValue() {
		return oldValue;
	}

	@Override
	public String getNewValue() {
		return newValue;
	}

	@Override
	public void accept(RequirementAuditEventVisitor visitor) {
		visitor.visit(this);
	}
	
	public static RequirementPropertyChangeEventBuilder<RequirementPropertyChange> builder() {
		return new Builder();
	}

	private static class Builder extends AbstractRequirementPropertyChangeEventBuilder<RequirementPropertyChange> {

		@Override
		public RequirementPropertyChange build() {
			RequirementPropertyChange event = new RequirementPropertyChange(eventSource, author);

			event.propertyName = modifiedProperty;
			event.oldValue = valueToString(oldValue);
			event.newValue = valueToString(newValue);

			return event;
		}

		private String valueToString(Object value) {
			if (value != null && value instanceof InfoListItem) {
				return ((InfoListItem) value).getLabel();
			} else {
				return Objects.toString(value, "");
			}
		}
	}

}

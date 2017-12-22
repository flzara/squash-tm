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
package org.squashtest.tm.domain.customfield;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Defines the binding of a {@link CustomField} to instances of {@link BindableEntity}s contained in a {@link Project}
 *
 * @author Gregory Fouquet
 */
@Entity
public class CustomFieldBinding implements Identified{
	@Id
	@Column(name = "CFB_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_field_binding_cfb_id_seq")
	@SequenceGenerator(name = "custom_field_binding_cfb_id_seq", sequenceName = "custom_field_binding_cfb_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false, targetEntity = CustomField.class, cascade = CascadeType.DETACH)
	@JoinColumn(name = "CF_ID", updatable = false)
	@NotNull
	private CustomField customField;

	@Enumerated(EnumType.STRING)
	@Column(updatable = false)
	private BindableEntity boundEntity;

	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_RENDERING_LOCATION", joinColumns = @JoinColumn(name = "CFB_ID"))
	@Enumerated(EnumType.STRING)
	@Column(name = "RENDERING_LOCATION")
	private Set<RenderingLocation> renderingLocations = new HashSet<>(5);

	@ManyToOne
	@JoinColumn(name = "BOUND_PROJECT_ID", updatable = false)
	@NotNull
	private GenericProject boundProject;


	@Column
	private int position = 0;


	/**
	 * @return the renderingLocations
	 */
	public Set<RenderingLocation> getRenderingLocations() {
		return renderingLocations;
	}

	public Set<RenderingLocation> copyRenderingLocations() {
		Set<RenderingLocation> copy = new HashSet<>(5);
		copy.addAll(renderingLocations);
		return copy;
	}

	public CustomField getCustomField() {
		return customField;
	}

	public void setCustomField(CustomField customField) {
		this.customField = customField;
	}

	public BindableEntity getBoundEntity() {
		return boundEntity;
	}

	public void setBoundEntity(BindableEntity boundEntity) {
		this.boundEntity = boundEntity;
	}

	public GenericProject getBoundProject() {
		return boundProject;
	}

	public void setBoundProject(GenericProject boundProject) {
		this.boundProject = boundProject;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setRenderingLocations(Set<RenderingLocation> renderingLocations) {
		this.renderingLocations = renderingLocations;
	}

	public void addRenderingLocation(RenderingLocation location) {
		this.renderingLocations.add(location);
	}

	public void removeRenderingLocation(RenderingLocation location) {
		this.renderingLocations.remove(location);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public CustomFieldValue createNewValue() {

		CustomFieldValue value;
		switch (customField.getInputType()) {
			case RICH_TEXT:
				value = new RichTextValue();
				break;
			case TAG:
				value = new TagsValue();
				break;
			case NUMERIC:
				value = new NumericCustomFieldValue();
				break;
			default:
				value = new CustomFieldValue();
		}
		value.setBinding(this);
		value.setValue(customField.getDefaultValue());
		value.setCufId(customField.getId());
		return value;
	}


	// ******************* static inner classes, publicly available for once ******************

	/**
	 * In case you wonder : NOT THREAD SAFE !
	 *
	 * @author bsiri
	 */
	public static class PositionAwareBindingList extends LinkedList<CustomFieldBinding> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public PositionAwareBindingList(Collection<CustomFieldBinding> items) {
			super(items);
		}

		public void reorderItems(List<Long> itemIds, int newIndex) {
			moveCarelessly(itemIds, newIndex);
			reindex();
		}

		private void moveCarelessly(List<Long> ids, int newIndex) {

			Set<Long> targetIds = new HashSet<>(ids);
			LinkedList<CustomFieldBinding> removed = new LinkedList<>();

			ListIterator<CustomFieldBinding> iterator = this.listIterator();

			while (iterator.hasNext()) {

				CustomFieldBinding binding = iterator.next();
				Long id = binding.getId();

				//if the id matches, the current binding moves from the current list to the list of removed binding
				//then remove the id from the target id set
				if (targetIds.contains(id)) {
					removed.add(binding);
					iterator.remove();
					targetIds.remove(id);
				}

			}

			//now we reinsert the removed entities to their new position
			this.addAll(newIndex, removed);
		}

		private void reindex() {
			int position = 1;
			for (CustomFieldBinding binding : this) {
				binding.setPosition(position++);
			}
		}

	}


}

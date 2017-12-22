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
package org.squashtest.tm.service.internal.chart;

import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.hibernate.Session;
import org.springframework.context.ApplicationListener;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnRole;
import org.squashtest.tm.domain.chart.ColumnType;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.QColumnPrototype;
import org.squashtest.tm.domain.chart.QFilter;
import org.squashtest.tm.domain.chart.SpecializedEntityType;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.event.ChangeCustomFieldCodeEvent;
import org.squashtest.tm.event.ColumnPrototypeEvent;
import org.squashtest.tm.event.CreateCustomFieldBindingEvent;
import org.squashtest.tm.event.DeleteCustomFieldBindingEvent;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;

import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;


/*
 * COMMENTING COLUMN PROTOTYPE HANDLING BECAUSE OUT OF SCOPE OF 1.13
 * RE-ACTIVATE WHEN DEV OF 1.14 BEGINS
 * AND MIGRATE THAT SHIT TO JPA WHILE YOURE AT IT
 */
//@Service
public class ColumnPrototypeModification implements ApplicationListener<ColumnPrototypeEvent> {

	private static final Transformer TYPE_COLLECTOR = new Transformer() {

		@Override
		public Object transform(Object cufBinding) {
			BindableEntity bind = ((CustomFieldBinding) cufBinding).getBoundEntity();
			return convertBindableEntityToEntityType(bind);
		}
	};

	private static final QColumnPrototype PROTOTYPE = QColumnPrototype.columnPrototype;
	private static final QFilter FILTER = QFilter.filter;

	@PersistenceContext
	private EntityManager em;

	@Inject
	private CustomFieldBindingDao cufBindingDao;

	@Override
	public void onApplicationEvent(ColumnPrototypeEvent event) {

		if (event instanceof CreateCustomFieldBindingEvent) {
			CustomFieldBinding cufBinding = (CustomFieldBinding) event.getSource();
			handleCUFBindingEvent(cufBinding);

		} else if (event instanceof DeleteCustomFieldBindingEvent) {

			@SuppressWarnings("unchecked")
			List<Long> cufBindingIds = (List<Long>) event.getSource();
			handleCUFBindingDeleteEvent(cufBindingIds);

		} else if (event instanceof ChangeCustomFieldCodeEvent) {
			String[] result = (String[]) event.getSource();
			String oldCode = result[0];
			String newCode = result[1];
			handleCUFCodeChange(oldCode, newCode);

		} else {
			throw new IllegalArgumentException("event not supported : " + event);
		}

	}

	@SuppressWarnings("unchecked")
	private void handleCUFCodeChange(String oldCode, String newCode) {
		HibernateQuery<?> query = createBaseQuery();
		addCodeToQuery(query, oldCode);
		List<ColumnPrototype> prototypes = (List<ColumnPrototype>) query.fetch();

		for (ColumnPrototype prototype : prototypes) {
			prototype.setAttributeName(newCode);
			prototype.setLabel(getColumnLabel(newCode, prototype.getEntityType()));
			session().update(prototype);
		}
	}

	private MultiValueMap<Long, CustomFieldBinding> populateBindingByCufId(List<Long> cufBindingIds) {

		List<CustomFieldBinding> bindings = cufBindingDao.findAllByIds(cufBindingIds);

		MultiValueMap<Long, CustomFieldBinding> bindingByCufId = new LinkedMultiValueMap<>();

		for (CustomFieldBinding binding : bindings) {
			Long id = binding.getCustomField().getId();
			bindingByCufId.add(id, binding);
		}
		return bindingByCufId;
	}


	private void handleCUFBindingDeleteEvent(List<Long> cufBindingIds) {

		if (!cufBindingIds.isEmpty()) {
			MultiValueMap<Long, CustomFieldBinding> bindingByCufId = populateBindingByCufId(cufBindingIds);
			removeColumnPrototypes(bindingByCufId.entrySet());
		}
	}

	@SuppressWarnings("unchecked")
	private Set<EntityType> findTypeToRemove(Entry<Long, List<CustomFieldBinding>> entry) {

		List<CustomFieldBinding> allBinding = cufBindingDao.findAllByCustomFieldIdOrderByPositionAsc(entry.getKey());
		allBinding.removeAll(entry.getValue());

		Set<EntityType> remainingType = CollectionUtils.isEmpty(allBinding) ? EnumSet.noneOf(EntityType.class)
			: EnumSet.copyOf(CollectionUtils.collect(allBinding, TYPE_COLLECTOR));

		Set<EntityType> typeToRemove = EnumSet.copyOf(CollectionUtils.collect(entry.getValue(), TYPE_COLLECTOR));

		typeToRemove.removeAll(remainingType);

		return typeToRemove;
	}

	private void removeColumnPrototypes(Set<Entry<Long, List<CustomFieldBinding>>> entrySet) {

		for (Entry<Long, List<CustomFieldBinding>> entry : entrySet) {
			removeColumnPrototype(entry);
		}

	}


	private void removeColumnPrototype(Entry<Long, List<CustomFieldBinding>> entry) {
		for (EntityType type : findTypeToRemove(entry)) {

			String code = entry.getValue().get(0).getCustomField().getCode();

			HibernateQuery<?> query = createBaseQuery();
			addCodeToQuery(query, code);
			addTypeToQuery(query, type);
			ColumnPrototype proto = (ColumnPrototype) query.fetchFirst();
			deleteFilterForColumn(proto);
			session().delete(proto);
		}
	}

	private boolean columnAlreadyExist(String code, EntityType type) {
		HibernateQuery<?> query = createBaseQuery();
		addCodeToQuery(query, code);
		addTypeToQuery(query, type);
		return query.fetchCount() > 0;
	}

	private void handleCUFBindingEvent(CustomFieldBinding cufBinding) {

		CustomField cuf = cufBinding.getCustomField();
		String code = cuf.getCode();
		EntityType type = convertBindableEntityToEntityType(cufBinding.getBoundEntity());

		if (!columnAlreadyExist(code, type)) {
			createColumnPrototype(cuf, code, type);
		}
	}

	private DataType getDataTypeFromInputType(InputType inputType) {

		DataType dataType;

		switch (inputType) {
			case CHECKBOX:
				dataType = DataType.BOOLEAN;
				break;
			case DATE_PICKER:
				dataType = DataType.DATE;
				break;
			case DROPDOWN_LIST:
				dataType = DataType.LIST;
				break;
			case PLAIN_TEXT:
			case RICH_TEXT:
				dataType = DataType.STRING;
				break;
			case TAG:
			default:
				throw new IllegalArgumentException(inputType + "not yet supported");
		}

		return dataType;
	}

	private void createColumnPrototype(CustomField cuf, String code, EntityType type) {

		InputType inputType = cuf.getInputType();

		if (cufIsNotSupported(inputType)) {
			return;
		}

		String label = getColumnLabel(code, type);
		SpecializedEntityType entityType = new SpecializedEntityType(type, null);
		DataType dataType = getDataTypeFromInputType(inputType);
		Set<ColumnRole> roles = EnumSet.of(ColumnRole.FILTER);

		ColumnPrototype newProto = new ColumnPrototype(label, entityType, dataType, ColumnType.CUF, null, code, true,
			roles);
		session().persist(newProto);

	}

	private String getColumnLabel(String code, EntityType type) {
		return String.valueOf(type) + "_CUF_" + code;
	}

	private boolean cufIsNotSupported(InputType inputType) {

		boolean result;

		switch (inputType) {
			case CHECKBOX:
			case DATE_PICKER:
			case DROPDOWN_LIST:
			case PLAIN_TEXT:
			case RICH_TEXT:
				result = false;
				break;
			case TAG:
			default:
				result = true;
				break;
		}
		return result;
	}

	private HibernateQuery<?> createBaseQuery() {
		HibernateQueryFactory factory = new HibernateQueryFactory(session());
		return factory.from(PROTOTYPE).where(PROTOTYPE.columnType.eq(ColumnType.CUF));
	}

	private void deleteFilterForColumn(ColumnPrototype column) {
		HibernateQueryFactory factory = new HibernateQueryFactory(session());
		HibernateQuery<?> query = factory.from(FILTER).where(FILTER.column.eq(column));
		for (Object o : query.fetch()) {
			session().delete(o);
		}

	}

	private void addCodeToQuery(HibernateQuery<?> query, String code) {
		query.where(PROTOTYPE.attributeName.eq(code));
	}

	private void addTypeToQuery(HibernateQuery<?> query, EntityType type) {
		query.where(PROTOTYPE.specializedType.entityType.eq(type));
	}

	private Session session() {
		return em.unwrap(Session.class);
	}

	private static EntityType convertBindableEntityToEntityType(BindableEntity bind) {
		// we need to fix the name for test step...because it's not called the same in bindable entity and
		// entityType
		// Maybe we could do something better later to avoid having different name for the same thing
		String name = bind == BindableEntity.TEST_STEP ? EntityType.TEST_CASE_STEP.name() : bind.name();
		return EntityType.valueOf(name);

	}
}

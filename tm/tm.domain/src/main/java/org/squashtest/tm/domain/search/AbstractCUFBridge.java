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
package org.squashtest.tm.domain.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.builtin.NumericEncodingDateBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>Base class for bridges that index the custom fields of a given entity.</p>
 *
 * <p>
 *    Some custom fields require analysis and some don't. However we have cannot control the behavior from here because
 *    it is specified at the declaration site (the @ClassBridge annotation above the entity has a property 'analyze').
 *    Previously we relied on bridge parameters for this, but the usage was confusing. The new approach is now to use
 *    a specific subtype of the bridge we need : check the subclasses.
 * </p>
 *
 */
public abstract class AbstractCUFBridge extends SessionFieldBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCUFBridge.class);

	private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");


	@SuppressWarnings("unchecked")
	private List<CustomFieldValue> findCufValuesForType(Session session, BoundEntity entity) {

		BindableEntity entityType = entity.getBoundEntityType();
		Long id = entity.getId();

		Criteria crit =
			session.createCriteria(CustomFieldValue.class)
				.createAlias("binding", "binding")
				.createAlias("binding.customField", "cuf");

		crit.add(Restrictions.eq("boundEntityId", id))
			.add(Restrictions.eq("boundEntityType", entityType));


		filterOnCufType(crit);

		return crit.list();

	}

	/**
	 * May (and should) add a restriction on the custom field that will be indexed by this bridge, to the criteria.
	 * Note that the alias of the 'cuf' entity in the query is 'cuf'.
	 *
	 * @param criteria
	 */
	protected abstract void filterOnCufType(Criteria criteria);



	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document,
										LuceneOptions luceneOptions) {

		Class<?> valClass = value.getClass();
		BoundEntity boundEntity = null;
		if (BoundEntity.class.isAssignableFrom(valClass)){
			boundEntity = (BoundEntity) value;
		}
		else{
			LOGGER.debug("Attempted to write the customfields for an entity that cannot have cufs : '"+valClass+"', skipping");
			return;
		}

		List<CustomFieldValue> cufValues = findCufValuesForType(session, boundEntity);

		for (CustomFieldValue cufValue : cufValues) {

			InputType theType = cufValue.getBinding().getCustomField().getInputType();
			String code = cufValue.getBinding().getCustomField().getCode();
			String val = null;

			switch (theType) {
				case DATE_PICKER:
					// TODO quick fix for #6031. Refactor that ugly crap !
					Date date = coerceToDate(cufValue);
					if (date != null) {
						NumericEncodingDateBridge.DATE_DAY.set(code, date, document, luceneOptions);
						return;
					}
					break;
				case DROPDOWN_LIST:
					val = cufValue.getValue();
					if (val != null && val.isEmpty()) {
						val = "$NO_VALUE";
					}
					break;
				default:
					val = cufValue.getValue();
			}

			// TODO use the correct API
			if (StringUtils.isNotBlank(val) && theType == InputType.NUMERIC) {
//				Issue #6431: Impossible Cast because hibernate returned a proxy CustomFieldValue
				Double doubleValue = Double.valueOf(val);
				Field field = new DoubleField(code, doubleValue, luceneOptions.getStore());
				document.add(field);
			} else if (val != null) {
				Field field = new Field(code, val, luceneOptions.getStore(), luceneOptions.getIndex(),
					luceneOptions.getTermVector());
				document.add(field);
			}
		}
	}

	/**
	 * Coerce a CFV into a Calendar. The CFV should be of type DATE !
	 *
	 * @param fieldValue
	 * @return
	 */
	protected Date coerceToDate(CustomFieldValue fieldValue) {
		String value = fieldValue.getValue();

		if (StringUtils.isEmpty(value)) {
			// wont parse as date, early exit
			return null;
		}

		// TODO either the CFV or a utility class should be responsible for the parsing because the CSV is responsible for its own string representation of the date
		try {
			return inputFormat.parse(fieldValue.getValue());
		} catch (ParseException e) {
			LOGGER.debug("Cannot parse as date custom field of value '{}'", fieldValue.getValue(), e);
		}

		return null;
	}
}

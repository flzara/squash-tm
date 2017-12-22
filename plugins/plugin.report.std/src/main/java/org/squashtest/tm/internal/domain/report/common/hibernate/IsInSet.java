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
package org.squashtest.tm.internal.domain.report.common.hibernate;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Property;
import org.squashtest.tm.internal.domain.report.query.QueryOperator;
import org.squashtest.tm.internal.domain.report.query.hibernate.ReportCriterion;

/*
 * This ReportCriterion will check if the value at the given propertyPath is an element of the list of parameters.
 * This ReportCriterion is special because we need to define a callback for it, see fromValueToTypedValue(). This callback
 * exists to define how to cast each elements of the parameter list to the right type.
 *
 */
public abstract class IsInSet<T extends Number> extends ReportCriterion {

	public IsInSet() {
		setOperator(QueryOperator.COMPARATOR_IN);
		setParamClass(Long.class);
	}

	public IsInSet(String criterionName, String attributePath, Class<?> entityClass, String entityAlias) {
		setCriterionName(criterionName);
		setAttributePath(attributePath);
		setEntityClass(entityClass);
		setEntityAlias(entityAlias);
	}

	@Override
	public Criterion makeCriterion() {
		Criterion criterion = null;

		List<?> values = getTypedParameters();

		if (values != null) {
			criterion = Property.forName(getEntityAlias() + "." + getAttributePath()).in(values);

		}
		return criterion;

	}

	/*
	 * this method casts to the correct type the raw Object parameters using the closure implemented below.
	 */
	protected List<?> getTypedParameters() {

		Object[] rawParameters = getParameters();

		if (rawParameters == null || rawParameters.length == 0){
			return null;}
		try {
			List<Object> typedValues = new LinkedList<>();

			for (Object o : rawParameters) {
				typedValues.add(fromValueToTypedValue(o));
			}

			return typedValues;

		} catch (Exception e) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " : cannot cast values to Long", e);
		}

	}

	/*
	 * The cast closure (see comments above).
	 */
	public abstract T fromValueToTypedValue(Object o);

}

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
package org.squashtest.tm.service.internal.query;

import com.google.common.base.Functions;
import org.springframework.util.ReflectionUtils;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.DataType;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.SpecializedEntityType;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This helper retrieves the (typed) Enum value that corresponds to
 * its stringified representation.
 *
 * Because of the name clashes between all the different enum values
 * (eg TestCaseStatus#WORK_IN_PROGRESS and RequirementStatus#WORK_IN_PROGRESS)
 * we need to lift the ambiguity. The best way would have bene to have the string
 * value itself hold the information but it's too late for that now, or create
 * a specific {@link org.squashtest.tm.domain.query.DataType} for each, but it's
 * too late for that now.
 *
 * The solution for now is to inspect the column and, using the entity type and
 * attribute name, retrieve by reflection the actual enum type, then retrieve the
 * typed value from the string representation.
 *
 * It works for simple cases (the column is a ATTRIBUTE, that is directly owned by
 * the entity) but more complex case (like for REQUIREMENT_CRITICALITY is actually a
 * nested property delegated to its most recent version, or if the column is a CALCULATED column) it will
 * break (again).
 *
 */
class EnumHelper {


	private Class<?> enumType;

	EnumHelper(QueryColumnPrototype column) {
		// crash early if the datatype is not an enum
		if (column.getDataType() != DataType.LEVEL_ENUM){
			throw new IllegalArgumentException("The EnumHelper can help only with Enums, but received column '"+column.getLabel()+
												   "' of type '"+column.getDataType()+"'");
		}
		// Crash early if attempting to do that with a calculated subquery
		// In order to do that we would need to analyse the subquery. It's doable,
		// but not for now.
		if (column.getColumnType() != ColumnType.ATTRIBUTE){
			throw new IllegalArgumentException("Sorry but for now the EnumHelper cannot help with column '"+ column.getLabel()
												   +"' because it is calculated subquery of a custom field");
		}

		initialize(column);
	}




	<E extends Enum<E>> E valueOf(String value) {
		// might throw if the value is wrong but it's fine, no recovery is possible anyway
		// plus, it manipulates only data we have 100% control of.
		return Enum.valueOf((Class<E>)enumType, value);
	}


	private void initialize(QueryColumnPrototype proto){
		// first retrieve the class from the EntityType. We need again to to so in a weird way.
		Class<?> ownerType = findOwnerType(proto);

		// find the field class
		Class<?> fieldClass = findFieldClass(ownerType, proto.getAttributeName());

		// check this is indeed an enum
		throwIfNotEnum(fieldClass);

		// now we can reflect on it
		enumType = (Class<Enum<?>>)fieldClass;
	}


	// finds the Class<?> of the owner type, going the long way through InternalEntityType that knows
	// for sure the information.
	private Class<?> findOwnerType(QueryColumnPrototype proto){
		SpecializedEntityType specType = proto.getSpecializedType();
		InternalEntityType internalType  = InternalEntityType.fromSpecializedType(specType);
		return internalType.getEntityClass();
	}


	// retrieves the Field designated by the attributePath. attributePath
	// can designate nested properties, if they are separated by a dot '.'
	// we naively assume that the attribute is not null and not blank, because it comes from our
	// database
	private Class<?> findFieldClass(Class<?> ownerType, String attributePath){

		List<String> splitPath = Arrays.asList(attributePath.split("\\."));

		Class<?> fieldClass = ownerType;

		Iterator<String> iterPath = splitPath.iterator();
		while(iterPath.hasNext()){
			String attrName = iterPath.next();

			Field attrField = ReflectionUtils.findField(fieldClass, attrName);
			if (attrField == null){
				throw new RuntimeException("Could not find attribute '"+attrName+"' in class '"+fieldClass+"'");
			}
			fieldClass = attrField.getType();
		}

		return fieldClass;
	}


	private void throwIfNotEnum(Class<?> possibleEnum){
		if (! possibleEnum.isEnum()){
			// no need to provide much context, we already have checked that the column datatype is 'LEVEL_ENUM'
			// at this point. Error could only happen due to database content error, which are all static referential
			// data.
			throw new IllegalArgumentException("class '"+possibleEnum+"' is not an enum type");
		}
	}



}

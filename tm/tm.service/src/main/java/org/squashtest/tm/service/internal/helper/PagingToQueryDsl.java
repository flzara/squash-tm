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
package org.squashtest.tm.service.internal.helper;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Level;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * DSL class that will translate generic paging, sorting and filtering beans into their QueryDsl-specific variants,
 * e.g. a {@link org.springframework.data.querydsl.QSort} out of a reqular {@link org.springframework.data.domain.Sort}
 * </p>
 *
 * <p>
 *     This toolkit proposes two dsl-like modules, one for converting the sort objects, and the other for the filtering
 *     objects.
 * </p>
 *
 * <h4>SortConverter usage</h4>
 *
 * <p>
 *     SortConverter is intended for conversion from Spring's {@link Sort} to QueryDsl's {@link OrderSpecifier}.
 *     A SortConverter is created using PagingToQueryDsl{@link #sortConverter()}. It requires configuration, the bare minimum
 *     are of course the Sort object to be converted, and the class of the query root entity (the type of the entity returned
 *     in the paged result).
 * </p>
 *
 * <p>
 *     From there the Sort object can be converted as is, with respect to the name of the sorted property, the sort direction
 *     and the null handling.
 * </p>
 *
 * <p>
 *     Important : please note that the name of the property must be the full path starting from the root entity.
 *     For instance, if the root entity is a test case and we wish to sort on its project name, the property name
 *     must be 'project.name' and not just 'name'.
 * </p>
 *
 * <p>
 *     The properties will be sorted according to their natural ordering, which is fine most of the time. However in some
 *     occasions the sorting is unnatural and needs to be specified. For this kind of corner cases you can tell which type
 *     the property is and if a special sort semantic applies to that type it will be used instead.
 *     The prominent use case is {@link Level} enums. By telling the SortConverter that an property
 *     is a Level enum, the generated {@link OrderSpecifier} will use a case-when-else structure, with one entry
 *     for each level.
 * </p>
 *
 *
 * <h4>ColumnFilteringConverter usage</h4>
 *
 * <p>
 *     ColumnFilteringConverter is intended for conversion from our own API {@link ColumnFiltering} to QueryDsl's {@link Predicate}.
 *     The generated predicate will be the conjunction of all the conditions stated in the filter, meaning they are AND'ed together.
 *     A ColumnFilteringConverter is created using PagingToQueryDsl{@link #filterConverter()}. It requires configuration, the bare
 *     minimum are of course the ColumnFiltering object to be converted, and the class of the query root entity (the type of the entity
 *     returned in the paged result).
 * </p>
 *
 * <p>
 *     Important : same remark about the property names as for SortConverter : please use qualified property path.
 * </p>
 *
 * <p>
 *     By default every property will be considered as a String, and will filtered using a Like. Most of the time though this
 *     is not enough, because the exact type is often required by the underlying Hibernate's parameter assignation routines.
 *     To help the converter parsing the filter values and deserializing (from their initial String form) into the proper type
 *     you can specify the property types just like for the SortConverter.
 * </p>
 *
 * <p>
 * 		Moreover you can also tune the comparison operator. For instance you can use either Like or (strict) Equality for
 * 	String comparison. Unless specified otherwise Equality will be the default operation (because it is suitable for every type).
 * 	The alternate comparison operators are :
 * 		<ul>
 * 		 <li>for Strings : like </li>
 * 		 <li>dates : date between</li>
 * 		</ul>
 *
 * 	Note : More operator might be desired in the future (eg the IN list operation).
 * </p>
 *
 * <p>
 *     Important : no input validation will be made here. Be sure that your inputs are correctly formatted, any exception
 *     will be rethrowed.
 * </p>
 *
 *
 */
public final class PagingToQueryDsl {

	private static final Logger LOGGER = LoggerFactory.getLogger(PagingToQueryDsl.class);
	public static final String LIST_SEPARATOR = ";";

	private PagingToQueryDsl(){
		super();
	}

	public static SortConverter sortConverter(){
		return new SortConverter();
	}

	public static SortConverter sortConverter(Class<?> entity){
		return new SortConverter(entity);
	}

	public static ColumnFilteringConverter filterConverter(){
		return new ColumnFilteringConverter();
	}

	public static ColumnFilteringConverter filterConverter(Class<?> entity){
		return new ColumnFilteringConverter(entity);
	}

	// ************** Spring Sort conversion *******************************


	public static final class SortConverter extends BaseConverter {

		private Sort from;

		SortConverter() {
			super(null);
		}

		SortConverter(Class<?> entity) {
			super(entity);
		}

		public SortConverter forEntity(Class<?> entity) {
			this.entity = entity;
			return this;
		}

		public SortConverter from(Sort from) {
			this.from = from;
			return this;
		}

		public PropertyTypesConfigurer<SortConverter> typeFor(String... propertyNames){
			return new PropertyTypesConfigurer<>(this, propertyNames);
		}

		public OrderSpecifier<?>[] build() {

			if (entity == null || from == null) {
				throw new IllegalStateException("Programming error : PagingToQueryDsl.SortConverter invoked while not fully initialized");
			}

			initBasePath();

			OrderSpecifier<?>[] orderSpecifiers = from.stream().map(this::convert).toArray(OrderSpecifier<?>[]::new);

			return orderSpecifiers;
		}


		private OrderSpecifier convert(Sort.Order nativeOrder) {
			Sort.Direction direction = nativeOrder.getDirection();
			Sort.NullHandling nullHandling = nativeOrder.getNullHandling();
			String property = nativeOrder.getProperty();

			Expression expr = createOrderExpression(property);

			Order qdslOrder = toQdslOrder(direction);
			OrderSpecifier.NullHandling qdslNullHandling = toQdslNullhandling(nullHandling);

			OrderSpecifier spec = new OrderSpecifier(qdslOrder, expr, qdslNullHandling);
			return spec;
		}


		private Expression createOrderExpression(String property){

			// create the base expression
			EntityPathBase<?> pptPath = toEntityPath(property);

			Expression expression = pptPath;

			// look for special sorting semantics
			if (propertyTypes.containsKey(property)){
				Class pptClass = propertyTypes.get(property);

				if (isLevelEnum(pptClass)){
					expression = orderByLevel(pptPath, pptClass);
				}
			}

			return expression;
		}

		private boolean isLevelEnum(Class<?> clazz){
			return Level.class.isAssignableFrom(clazz) && Enum.class.isAssignableFrom(clazz);
		}

		private <T extends Enum<T> & Level> Expression orderByLevel(EntityPathBase<?> path, Class<T> pptClass){

			CaseBuilder.Cases interm = null;

			Enum<T>[] enums = pptClass.getEnumConstants();

			// make that a map of (level <-> querydsl constant value)
			Map<Integer, Expression<?>> levelByEnum = Arrays.stream(enums).collect(
				Collectors.toMap(
					e -> ((Level)e).getLevel(),
					Expressions::constant
				)
			);

			// now create the case expression
			for (Map.Entry<Integer, Expression<?>> entry : levelByEnum.entrySet()){
				Integer level = entry.getKey();
				Expression value = entry.getValue();
				// sad we have to initialize it
				if (interm == null){
					interm = new CaseBuilder().when(path.eq(value)).then(level);
				}
				else{
					interm = interm.when(path.eq(value)).then(level);
				}
			}

			return interm.otherwise(Expressions.constant(-1000));
		}


		private Order toQdslOrder(Sort.Direction direction) {
			return direction.isAscending() ? Order.ASC : Order.DESC;
		}


		private OrderSpecifier.NullHandling toQdslNullhandling(Sort.NullHandling handling) {
			OrderSpecifier.NullHandling qdslHandling;
			switch (handling) {
				case NULLS_FIRST:
					qdslHandling = OrderSpecifier.NullHandling.NullsFirst;
					break;
				case NULLS_LAST:
					qdslHandling = OrderSpecifier.NullHandling.NullsLast;
					break;
				default:
					qdslHandling = OrderSpecifier.NullHandling.Default;
					break;
			}
			return qdslHandling;
		}


	}


	// ************** Squash ColumnFiltering conversion ********************

	public static final class ColumnFilteringConverter extends BaseConverter {

		private ColumnFiltering from;

		private Map<String, CompOperator> propertyComparison = new HashMap<>();


		public ColumnFilteringConverter forEntity(Class<?> entity){
			this.entity = entity;
			return this;
		}

		public ColumnFilteringConverter(){
			super(null);
		}

		public ColumnFilteringConverter(Class<?> entity) {
			super(entity);
		}

		public ColumnFilteringConverter from(ColumnFiltering filtering){
			this.from = filtering;
			return this;
		}


		public PropertyTypesConfigurer<ColumnFilteringConverter> typeFor(String... propertyNames){
			return new PropertyTypesConfigurer<>(this, propertyNames);
		}

		public ComparisonOperationConfigurer compare(String... properties){
			return new ComparisonOperationConfigurer(this, properties);
		}


		public Predicate build(){

			if (entity == null || from == null) {
				throw new IllegalStateException("Programming error : PagingToQueryDsl.ColumnFilteringConverter invoked while not fully initialized");
			}

			initBasePath();

			BooleanBuilder builder = new BooleanBuilder();

			from.getFilteredAttributes().stream().map(this::convert).forEach( expr -> builder.and(expr));

			return builder.getValue();

		}

		// ************** expression building *************************

		private BooleanExpression convert(String property){

			EntityPathBase<?> pptPath = toEntityPath(property);
			String value = from.getFilter(property);

			BooleanExpression finalExpression;
			Object comparisonParameters = resolveParameters(property, value);

			CompOperator operator = resolveOperator(property, value);

			switch(operator){
				case DATE_BETWEEN:
					finalExpression = asBetweenDateExpression(pptPath, (Couple<Date, Date>) comparisonParameters);
					break;

				case DATE:
					finalExpression = asDateExpression(pptPath, (Date) comparisonParameters);
					break;

				case LIKE:
					finalExpression = asLikeExpression(pptPath, (String) comparisonParameters);
					break;

				case IN:
					finalExpression = asInExpression(pptPath, comparisonParameters);
					break;

				case IS_NULL:
					finalExpression = isNull(pptPath);
					break;

				default:
					// else defaults to strict equality
					finalExpression = pptPath.eq(Expressions.constant(comparisonParameters));
					break;
			}

			return finalExpression;
		}


		private BooleanExpression asInExpression(EntityPathBase<?> pptPath, Object comparisonParameters) {
			Collection<Object> effectiveParameters;
			if (Collection.class.isAssignableFrom(comparisonParameters.getClass())){
				effectiveParameters = (Collection)comparisonParameters;
			}
			else{
				effectiveParameters = new ArrayList<>(1);
				effectiveParameters.add(comparisonParameters);
			}

			return pptPath.in((ArrayList) effectiveParameters);
		}


		private BooleanExpression asLikeExpression(Expression pptPath, String value){
			String searchTerm = "%"+value.replaceAll("%", "\\%")+"%";
			StringExpression asString = Expressions.asString(pptPath);
			return asString.likeIgnoreCase(searchTerm);
		}

		private BooleanExpression isNull(Expression pptPath) {
			StringExpression asString = Expressions.asString(pptPath);
			return asString.isNull();
		}

		private BooleanExpression asDateExpression(Expression pptPath, Date value){
			return asBetweenDateExpression(pptPath, new Couple<>(value, value));
		}

		private BooleanExpression asBetweenDateExpression(Expression pptPath, Couple<Date, Date> dates) {
			BooleanExpression finalExpression;
			DateExpression asDate = Expressions.asDate(pptPath);

			Date begin = dates.getA1();
			// end date is excluded...
			Date end = new Date(dates.getA2().getTime() + TimeUnit.DAYS.toMillis( 1 ));

			finalExpression = asDate.between(begin, end);
			return finalExpression;
		}


		// ******************** types resolution *************************


		private Class<?> resolveClass(String property){

			// was an operator specified for that property ?
			CompOperator operator = propertyComparison.get(property);

			// was a class specified for that property ? If not found, see if we can deduce it from
			// the operation, and if not consider it a String
			Class<?> pptClass = propertyTypes.computeIfAbsent(property, s -> {
				Class<?> res;
				// default is String if no context can help
				if (operator == null){
					res = String.class;
				}
				else switch (operator){
					case DATE: res = Date.class; break; // ah, possibly a date
					default : res = String.class; break; // no luck
				}
				return res;
			});

			return pptClass;

		}

		private CompOperator resolveOperator(String property, String value){

			// was a type specified for that property ? Default is String
			Class<?> pptClass = propertyTypes.getOrDefault(property, String.class);

			// was an operator specified for that property ? If specified, use it, else compute the default.
			CompOperator operator = propertyComparison.computeIfAbsent(property, s ->
				(String.class.isAssignableFrom(pptClass)) ? CompOperator.LIKE : CompOperator.EQUALITY
			);

			if (operator.equals(CompOperator.DATE) && value.contains(" - ")) {
				operator = CompOperator.DATE_BETWEEN;
			}

			return operator;

		}



		// ************ parameters parsing ***********************

		private Object resolveParameters(String property, String value){
			Object result;

			Class<?> pptClass = resolveClass(property);
			if (isEnumList(pptClass, value)) {
				String[] values = value.split(LIST_SEPARATOR);
				Collection c = new ArrayList<>();
				for (int i = 0; i < values.length; i++) {
					c.add(Enum.valueOf((Class<? extends Enum>)pptClass, values[i]));
				}
				return c;
			}

			if (isEnum(pptClass)){
				result = Enum.valueOf((Class<? extends Enum>)pptClass, value);
			}
			else if (canCoerceToDate(pptClass)){
				if (value.contains(" - ")){
					result = parseAsCoupleDates(value);
				}
				else {
					result = parseAsDate(value);
				}
			}
			else if (canCoerceToInteger(pptClass)){
				if (value != null) {
					try {
						result = Long.valueOf(value);
					} catch (NumberFormatException nfe) {
						LOGGER.error("Wrong format for numeric value", nfe);
						result = -52214; // default value
					}
				} else {
					result = value;
				}

			}
			else if (canCoerceToDecimal(pptClass)){
				result = Double.valueOf(value);
			}
			//default is String
			else{
				result = value;
			}

			return result;

		}

		private boolean canCoerceToInteger(Class<?> clazz){
			return (Long.class.isAssignableFrom(clazz)) ||
					   (Integer.class.isAssignableFrom(clazz)) ||
					   (BigInteger.class.isAssignableFrom(clazz)) ||
					   Short.class.isAssignableFrom(clazz);
		}

		private boolean canCoerceToDecimal(Class<?> clazz){
			return (Float.class.isAssignableFrom(clazz)) ||
					   Double.class.isAssignableFrom(clazz) ||
					   BigDecimal.class.isAssignableFrom(clazz);
		}

		private boolean isEnum(Class<?> clazz){
			return clazz.isEnum();
		}

		private boolean isEnumList(Class<?> clazz, String value) {
			boolean isEnumList = false;
			if(value != null) {
				isEnumList = (value.contains(LIST_SEPARATOR) && clazz.isEnum()) ;
			}
			return isEnumList;
		}

		private boolean canCoerceToDate(Class<?> clazz){
			return Date.class.isAssignableFrom(clazz) ||
					   Temporal.class.isAssignableFrom(clazz) ||
					   LocalDate.class.isAssignableFrom(clazz);
		}


		private Date parseAsDate(String value){
			try{
				return DateUtils.parseDdMmYyyyDate(value);
			}
			catch (ParseException ex){
				throw new RuntimeException("Encountered exception while parsing dates, probably not in yyyy-mm-dd format : '"+ value +"'", ex);
			}
		}

		private Couple<Date, Date> parseAsCoupleDates(String strDates){
			String[] splitDates = strDates.split(" - ");

			Date begin = parseAsDate(splitDates[0]);
			Date end = parseAsDate(splitDates[1]);

			return new Couple<>(begin, end);
		}

		// ********* DSL submodule for comparison operations *******

		public static final class ComparisonOperationConfigurer {

			private ColumnFilteringConverter converter;
			private String[] properties;

			ComparisonOperationConfigurer(ColumnFilteringConverter converter, String[] propertyNames){
				this.converter = converter;
				this.properties = propertyNames;
			}

			public ColumnFilteringConverter withEquality(){
				registerHint(CompOperator.EQUALITY);
				return converter;
			}

			public ColumnFilteringConverter withLike(){
				registerHint(CompOperator.LIKE);
				return converter;
			}

			public ColumnFilteringConverter withIn(){
				registerHint(CompOperator.IN);
				return converter;
			}

			public ColumnFilteringConverter withBetweenDates(){
				registerHint(CompOperator.DATE_BETWEEN);
				return converter;
			}

			public ColumnFilteringConverter withDates(){
				registerHint(CompOperator.DATE);
				return converter;
			}

			public ColumnFilteringConverter isNull(){
				registerHint(CompOperator.IS_NULL);
				return converter;
			}

			private void registerHint(CompOperator operation){
				for (String prop : properties){
					converter.propertyComparison.put(prop, operation);
				}
			}



		}

		private enum CompOperator{
			EQUALITY,
			LIKE,
			DATE_BETWEEN,
			DATE,
			IN,
			IS_NULL
		}
	}


	// ********************** base class for both **************************

	static class BaseConverter {

		Class<?> entity;
		PathBuilder basePath;
		Map<String, Class<?>> propertyTypes = new HashMap<>();

		BaseConverter(Class<?> entity){
			this.entity = entity;
		}


		void initBasePath() {
			String alias = createAlias();
			basePath = new PathBuilder<Object>(entity, alias);
		}

		String createAlias() {
			String simpleName = entity.getSimpleName();
			return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
		}


		void registerPropertyType(String propertyName, Class<?> propClass){
			propertyTypes.put(propertyName, propClass);
		}


		EntityPathBase<?> toEntityPath(String property){
			PathBuilder<?> finalPath = basePath;

			String[] pathElts = property.split("\\.");
			for (String elt : pathElts){
				finalPath = ((PathBuilder) finalPath).get(elt);
			}

			return finalPath;
		}



		// Sub-DSL module for property type configuration
		public static final class PropertyTypesConfigurer<CONVERTER_SUBTYPE extends BaseConverter> {
			private CONVERTER_SUBTYPE converter;
			private String[] propertyNames;

			PropertyTypesConfigurer(CONVERTER_SUBTYPE parent, String... propertyNames){
				this.converter = parent;
				this.propertyNames = propertyNames;
			}

			public CONVERTER_SUBTYPE isClass(Class<?> clazz){
				for (String prop : propertyNames) {
					converter.registerPropertyType(prop, clazz);
				}
				return converter;
			}

		}


	}

}

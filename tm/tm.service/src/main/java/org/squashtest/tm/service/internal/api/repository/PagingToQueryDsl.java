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
package org.squashtest.tm.service.internal.api.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Level;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DSL class that will translate generic paging, sorting and filtering beans into their QueryDsl-specific variants,
 * e.g. a {@link org.springframework.data.querydsl.QSort} out of a reqular {@link org.springframework.data.domain.Sort}
 *
 */
public final class PagingToQueryDsl {

	private static final Logger LOGGER = LoggerFactory.getLogger(PagingToQueryDsl.class);

	private PagingToQueryDsl(){
		super();
	}

	public static SortConverter sort(){
		return new SortConverter();
	}

	public static SortConverter sortFor(Class<?> entity){
		return new SortConverter(entity);
	}

	public static ColumnFilteringConverter filteringFor(){
		return new ColumnFilteringConverter();
	}

	public static ColumnFilteringConverter filteringFor(Class<?> entity){
		return new ColumnFilteringConverter(entity);
	}

	// ************** Spring Sort conversion *******************************


	public static final class SortConverter extends BaseDslProcessor{

		private Sort from;
		private Map<String, Class<?>> typesByProperty = new HashMap<>();

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


		public KnowingThat knowingThat(String... propertyNames){
			return new KnowingThat(this, propertyNames);
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

			// now look for possible "knowingThat" clauses, which may trigger special treatment
			if (typesByProperty.containsKey(property)){
				Class pptClass = typesByProperty.get(property);

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


		private void registerPropertyType(String propertyName, Class<?> propClass){
			typesByProperty.put(propertyName, propClass);
		}

		// more DSL sugar coating
		public static final class KnowingThat{
			private SortConverter converter;
			private String[] propertyNames;
			KnowingThat(SortConverter parent, String[] propertyNames){
				this.converter = parent;
				this.propertyNames = propertyNames;
			}

			public SortConverter hasClass(Class<?> clazz){
				for (String prop : propertyNames) {
					converter.registerPropertyType(prop, clazz);
				}
				return converter;
			}

		}

	}


	// ************** Squash ColumnFiltering conversion ********************

	public static final class ColumnFilteringConverter extends BaseDslProcessor{

		private ColumnFiltering from;

		private Map<String, CompOperator> comparisonByProperty = new HashMap<>();


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

		public ComparisonHint comparing(String... properties){
			return new ComparisonHint(this, properties);
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

		private BooleanExpression convert(String property){

			EntityPathBase<?> pptPath = toEntityPath(property);
			String value = from.getFilter(property);

			BooleanExpression finalExpression;
			CompOperator operator = comparisonByProperty.getOrDefault(property, CompOperator.EQUALITY);
			switch(operator){
				case DATE_BETWEEN:
					finalExpression = asBetweenDateExpression(pptPath, value);
					break;

				case LIKE:
					finalExpression = asLikeExpression(pptPath, value);
					break;
				default:
					// else defaults to strict equality
					finalExpression = pptPath.eq(Expressions.constant(value));
					break;
			}

			return finalExpression;
		}

		private BooleanExpression asLikeExpression(Expression pptPath, String value){
			String searchTerm = "%"+value.replaceAll("%", "\\%")+"%";
			StringExpression asString = Expressions.asString(pptPath);
			return asString.likeIgnoreCase(searchTerm);

		}


		private BooleanExpression asBetweenDateExpression(Expression pptPath, String strDates) {
			BooleanExpression finalExpression;
			DateExpression asDate = Expressions.asDate(pptPath);

			Date begin = null;
			Date end = null;
			try {
				String[] dates = strDates.split(" - ");
				begin = DateUtils.parseIso8601Date(dates[0]);
				end = DateUtils.parseIso8601Date(dates[1]);

				// actually we want begin -1 day and end +1 day
				// because 1/ the boundaries are inclusive and 2/ unfortunately we have no certainties on the timezone
				GregorianCalendar calendar = new GregorianCalendar();

				calendar.setTime(begin);
				calendar.add(Calendar.DATE, -1);
				begin = calendar.getTime();

				calendar.setTime(end);
				calendar.add(Calendar.DATE, 1);
				end = calendar.getTime();

			}
			catch (ParseException ex){
				throw new RuntimeException("Encountered exception while parsing dates, probably not in yyyy-mm-dd format : '"+ strDates +"'", ex);
			}

			finalExpression = asDate.after(begin).and(asDate.before(end));
			return finalExpression;
		}


		// more DSL sugar coating
		public static final class ComparisonHint{

			private ColumnFilteringConverter converter;
			private String[] properties;

			ComparisonHint(ColumnFilteringConverter converter, String[] propertyNames){
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

			public ColumnFilteringConverter withBetweenDates(){
				registerHint(CompOperator.DATE_BETWEEN);
				return converter;
			}

			private void registerHint(CompOperator operation){
				for (String prop : properties){
					converter.comparisonByProperty.put(prop, operation);
				}
			}

		}

		private enum CompOperator{
			EQUALITY,
			LIKE,
			DATE_BETWEEN
		}
	}


	// ********************** base class for both **************************

	private static class BaseDslProcessor {

		Class<?> entity;
		PathBuilder basePath;

		BaseDslProcessor(Class<?> entity){
			this.entity = entity;
		}



		void initBasePath() {
			String alias = createAlias();
			basePath = new PathBuilder<Object>(entity, alias);
		}

		private String createAlias() {
			String simpleName = entity.getSimpleName();
			return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
		}



		EntityPathBase<?> toEntityPath(String property){
			PathBuilder<?> finalPath = basePath;

			String[] pathElts = property.split("\\.");
			for (String elt : pathElts){
				finalPath = ((PathBuilder) finalPath).get(elt);
			}

			return finalPath;
		}



	}

}

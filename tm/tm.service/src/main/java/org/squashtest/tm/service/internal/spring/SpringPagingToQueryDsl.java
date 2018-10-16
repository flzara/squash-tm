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
package org.squashtest.tm.service.internal.spring;


import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DSL class that will generate a {@link org.springframework.data.querydsl.QSort} out of a reqular {@link org.springframework.data.domain.Sort}
 *
 */
public final class SpringPagingToQueryDsl {

	private SpringPagingToQueryDsl(){
		super();
	}

	public static SortConverter sort(){
		return new SortConverter();
	}

	public static SortConverter sortFor(Class<?> entity){
		return new SortConverter(entity);
	}


	public static final class SortConverter {

		private Class<?> entity;
		private PathBuilder basePath;

		private Sort from;
		private Map<String, Class<?>> typeByProperties = new HashMap<>();

		SortConverter() {
			super();
		}

		SortConverter(Class<?> entity) {
			super();
			this.entity = entity;
		}

		public SortConverter forEntity(Class<?> entity) {
			this.entity = entity;
			return this;
		}

		public SortConverter from(Sort from) {
			this.from = from;
			return this;
		}

		public KnowingThat knowingThat(String propertyName){
			return new KnowingThat(this, propertyName);
		}

		public OrderSpecifier<?>[] build() {

			if (entity == null || from == null) {
				throw new IllegalStateException("Programming error : SpringPagingToQueryDsl invoked with either a null target entity or ");
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


		private String createAlias() {
			String simpleName = entity.getSimpleName();
			return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
		}

		private void initBasePath() {
			String alias = createAlias();
			basePath = new PathBuilder<Object>(entity, alias);
		}

		private Expression createOrderExpression(String property){

			// TODOOO : make something smart that uses the "knowingThat" clauses

			Expression expr = basePath.getString(property);
			return expr;
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
			typeByProperties.put(propertyName, propClass);
		}


		// more DSL sugar coating, because that code was too legible.
		public static final class KnowingThat{
			private SortConverter converter;
			private String propertyName;
			KnowingThat(SortConverter parent, String propertyName){
				this.converter = parent;
				this.propertyName = propertyName;
			}

			public SortConverter hasClass(Class<?> clazz){
				converter.registerPropertyType(propertyName, clazz);
				return converter;
			}

		}

	}

}

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
package org.squashtest.tm.infrastructure.springdata;


import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import org.aspectj.weaver.patterns.OrSignaturePattern;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder class that will generate a {@link org.springframework.data.querydsl.QSort} out of a reqular {@link org.springframework.data.domain.Sort}
 *
 */
public class QSortBuilder {

	private Class<?> entity;
	private PathBuilder basePath;
	private Sort from;

	public QSortBuilder(){
		super();
	}

	public QSortBuilder(Class<?> entity){
		super();
		this.entity = entity;
	}

	public QSortBuilder forEntity(Class<?> entity){
		this.entity = entity;
		return this;
	}

	public QSortBuilder from(Sort from){
		this.from = from;
		return this;
	}

	public QSort build(){

		if (entity == null || from == null){
			throw new IllegalStateException("Programming error : QSortBuilder invoked with either a null target entity or ");
		}

		initBasePath();

		if (from.isUnsorted()){
			return QSort.unsorted();
		}

		List<OrderSpecifier> orderSpecifiers = from.stream().map(this::convert).collect(Collectors.toList());

		return new QSort();
	}


	private OrderSpecifier convert(Sort.Order nativeOrder){
		Sort.Direction direction = nativeOrder.getDirection();
		String property = nativeOrder.getProperty();
		Sort.NullHandling nullHandling = nativeOrder.getNullHandling();

		Expression expr = basePath.getString(property);
		Order qdslOrder = toQslOrder(direction);
		OrderSpecifier.NullHandling qdslNullHandling = toQdslNullhandling(nullHandling);

		OrderSpecifier spec = new OrderSpecifier(qdslOrder, expr, qdslNullHandling);
		return spec;
	}


	private String createAlias(){
		String simpleName = entity.getSimpleName();
		return simpleName.substring(0,1).toLowerCase() + simpleName.substring(1);
	}

	private void initBasePath(){
		String alias = createAlias();
		basePath = new PathBuilder<Object>(entity, alias);
	}

	private Order toQslOrder(Sort.Direction direction){
		return direction.isAscending() ? Order.ASC : Order.DESC;
	}

	private OrderSpecifier.NullHandling toQdslNullhandling(Sort.NullHandling handling){
		OrderSpecifier.NullHandling qdslHandling;
		switch(handling){
			case NULLS_FIRST: qdslHandling = OrderSpecifier.NullHandling.NullsFirst; break;
			case NULLS_LAST: qdslHandling = OrderSpecifier.NullHandling.NullsLast; break;
			default : qdslHandling = OrderSpecifier.NullHandling.Default; break;
		}
		return qdslHandling;
	}

}

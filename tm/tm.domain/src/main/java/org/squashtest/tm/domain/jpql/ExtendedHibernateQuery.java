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
package org.squashtest.tm.domain.jpql;

import org.hibernate.Session;
import org.hibernate.StatelessSession;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.hibernate.DefaultSessionHolder;
import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.hibernate.NoSessionHolder;
import com.querydsl.jpa.hibernate.SessionHolder;
import com.querydsl.jpa.hibernate.StatelessSessionHolder;

public class ExtendedHibernateQuery<T> extends HibernateQuery<T> implements ExtendedJPQLQuery<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// ****************** all the constructors ******************


	public ExtendedHibernateQuery(SessionHolder session, JPQLTemplates templates, QueryMetadata metadata) {
		super(session, templates, metadata);
	}


	public ExtendedHibernateQuery() {
		this(NoSessionHolder.DEFAULT, ExtHQLTemplates.INSTANCE, new DefaultQueryMetadata());
	}

	public ExtendedHibernateQuery(Session session) {
		this(new DefaultSessionHolder(session),ExtHQLTemplates.INSTANCE, new DefaultQueryMetadata());
	}

	public ExtendedHibernateQuery(Session session, QueryMetadata metadata) {
		this(new DefaultSessionHolder(session), ExtHQLTemplates.INSTANCE, metadata);
	}

	public ExtendedHibernateQuery(Session session, JPQLTemplates templates) {
		this(new DefaultSessionHolder(session), templates, new DefaultQueryMetadata());
	}


	public ExtendedHibernateQuery(SessionHolder session, JPQLTemplates templates) {
		this(session, templates, new DefaultQueryMetadata());
	}

	public ExtendedHibernateQuery(StatelessSession session) {
		this(new StatelessSessionHolder(session), ExtHQLTemplates.INSTANCE, new DefaultQueryMetadata());
	}

	@Override
	protected ExtendedHibernateQuery<T> clone(SessionHolder sessionHolder) {
		ExtendedHibernateQuery<T> q = new ExtendedHibernateQuery<>(sessionHolder,
				getTemplates(), getMetadata().clone());
		q.clone(this);
		return q;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> ExtendedHibernateQuery<U> select(Expression<U> expr) {
		queryMixin.setProjection(expr);
		return (ExtendedHibernateQuery<U>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ExtendedHibernateQuery<Tuple> select(Expression<?>... exprs) {
		queryMixin.setProjection(exprs);
		return (ExtendedHibernateQuery<Tuple>) this;
	}

	// ************************ operations ****************

	@Override
	public NumberExpression<Double> s_avg() {
		return Expressions.numberOperation(Double.class, ExtOps.S_AVG, this);
	}

	@Override
	public  NumberExpression<?>  s_min() {
		return Expressions.numberOperation(Long.class, ExtOps.S_MIN, this);
	}

	@Override
	public  NumberExpression<?>  s_max() {
		return Expressions.numberOperation(Long.class, ExtOps.S_MAX, this);
	}

	@Override
	public  NumberExpression<?>  s_sum() {
		return Expressions.numberOperation(Long.class, ExtOps.S_SUM, this);
	}

	@Override
	public NumberExpression<Long> s_count() {
		return Expressions.numberOperation(Long.class, ExtOps.S_COUNT, this);
	}

}

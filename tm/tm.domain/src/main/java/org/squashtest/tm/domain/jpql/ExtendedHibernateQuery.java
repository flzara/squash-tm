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

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.hibernate.DefaultSessionHolder;
import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.hibernate.SessionHolder;
import com.querydsl.jpa.hibernate.StatelessSessionHolder;

import org.hibernate.Session;
import org.hibernate.StatelessSession;

import static org.squashtest.tm.domain.jpql.FixedSessionHolders.*;

/**
 * Extension of {@link HibernateQuery} that make use of our ExtHQLTemplate, ie they make the extra functions available
 * in our Hibernate Dialect extensions available as dsl extensions. If you don't need them explicitly, consider using
 * {@link com.querydsl.jpa.impl.JPAQuery} or {@link org.springframework.data.jpa.repository.query.JpaQueryFactory} instead
 * (see below why).
 *
 * Also, Hibernate 5.2 broke compatibility with previous API which led QueryDSL to crash at runtime due to linkage error
 * with some methods of the Session interface (see https://github.com/querydsl/querydsl/issues/1917).
 * To circumvent this problem and waiting for a proper fix, this extension
 * will use its own implementation of {@link SessionHolder}, which is essentially a copy pasta but will work, due to the virtue
 * of being compiled for 5Hibernate 5.2 upfront.
 *
 * Also, see {@link ExtendedHibernateQueryFactory}.
 *
 * @param <T>
 */
public class ExtendedHibernateQuery<T> extends HibernateQuery<T> implements ExtendedJPQLQuery<T> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	// ****************** all the constructors ******************


	/**
	 * If you use this constructor, be sure that your {@link SessionHolder} has been issued by one of the factory
	 * methods of {@link FixedSessionHolders}.
	 *
	 * @param session
	 * @param templates
	 * @param metadata
	 */
	public ExtendedHibernateQuery(SessionHolder session, JPQLTemplates templates, QueryMetadata metadata) {
		super(session, templates, metadata);
	}


	public ExtendedHibernateQuery() {
		this(noSessionHolder(), ExtHQLTemplates.INSTANCE, new DefaultQueryMetadata());
	}

	/**
	 * This one is most likely your preferred constructor.
	 *
	 * @param session
	 */
	public ExtendedHibernateQuery(Session session) {
		this(defaultSessionHolder(session),ExtHQLTemplates.INSTANCE, new DefaultQueryMetadata());
	}

	public ExtendedHibernateQuery(Session session, QueryMetadata metadata) {
		this(defaultSessionHolder(session), ExtHQLTemplates.INSTANCE, metadata);
	}

	public ExtendedHibernateQuery(Session session, JPQLTemplates templates) {
		this(defaultSessionHolder(session), templates, new DefaultQueryMetadata());
	}

	/**
	 * If you use this constructor, be sure that your {@link SessionHolder} has been issued by one of the factory
	 * methods of {@link FixedSessionHolders}.
	 *
	 * @param session
	 * @param templates
	 */
	public ExtendedHibernateQuery(SessionHolder session, JPQLTemplates templates) {
		this(session, templates, new DefaultQueryMetadata());
	}

	public ExtendedHibernateQuery(StatelessSession session) {
		this(statelessSessionHolder(session), ExtHQLTemplates.INSTANCE, new DefaultQueryMetadata());
	}

	@Override
	protected ExtendedHibernateQuery<T> clone(SessionHolder sessionHolder) {
		ExtendedHibernateQuery<T> q = new ExtendedHibernateQuery<>(sessionHolder,
				getTemplates(), getMetadata().clone());
		q.clone(this);
		return q;
	}
	

	@Override
	public HibernateQuery<T> clone(Session session) {
		return this.clone(defaultSessionHolder(session));
	}


	@Override
	public HibernateQuery<T> clone(StatelessSession session) {
		return this.clone(statelessSessionHolder(session));
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

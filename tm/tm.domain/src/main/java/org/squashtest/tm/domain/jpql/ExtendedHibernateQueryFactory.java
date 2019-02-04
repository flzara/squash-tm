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


import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.hibernate.HibernateDeleteClause;
import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import com.querydsl.jpa.hibernate.HibernateUpdateClause;
import org.hibernate.Session;

import javax.inject.Provider;

import static org.squashtest.tm.domain.jpql.FixedSessionHolders.defaultSessionHolder;

/**
 * Adaptation of {@link HibernateQueryFactory} that works around https://github.com/querydsl/querydsl/issues/1917.
 * Remember that most of the time you want to use {@link com.querydsl.jpa.impl.JPAQueryFactory}, use it only
 * if the EntityManager is not available (you only have access to a Hibernate session).
 *
 * If you need to create a Query, the returned type is {@link ExtendedHibernateQuery}. In other cases (deletion etc)
 * a vanilla QueryDsl object will be returned.
 *
 */
public class ExtendedHibernateQueryFactory implements JPQLQueryFactory {


	private final JPQLTemplates templates;

	private final Provider<Session> session;

	public ExtendedHibernateQueryFactory(Session session) {
		this(ExtHQLTemplates.INSTANCE, session);
	}

	public ExtendedHibernateQueryFactory(JPQLTemplates templates, final Session session) {
		this.session = new Provider<Session>() {
			@Override
			public Session get() {
				return session;
			}
		};
		this.templates = templates;
	}

	public ExtendedHibernateQueryFactory(Provider<Session> session) {
		this(ExtHQLTemplates.INSTANCE, session);
	}

	public ExtendedHibernateQueryFactory(JPQLTemplates templates, Provider<Session> session) {
		this.session = session;
		this.templates = templates;
	}

	@Override
	public HibernateDeleteClause delete(EntityPath<?> path) {
		return new HibernateDeleteClause(defaultSessionHolder(session.get()), path, templates);
	}

	@Override
	public <T> HibernateQuery<T> select(Expression<T> expr) {
		return query().select(expr);
	}

	@Override
	public HibernateQuery<Tuple> select(Expression<?>... exprs) {
		return query().select(exprs);
	}

	@Override
	public <T> HibernateQuery<T> selectDistinct(Expression<T> expr) {
		return select(expr).distinct();
	}

	@Override
	public HibernateQuery<Tuple> selectDistinct(Expression<?>... exprs) {
		return select(exprs).distinct();
	}

	@Override
	public HibernateQuery<Integer> selectOne() {
		return select(Expressions.ONE);
	}

	@Override
	public HibernateQuery<Integer> selectZero() {
		return select(Expressions.ZERO);
	}

	@Override
	public <T> HibernateQuery<T> selectFrom(EntityPath<T> from) {
		return select(from).from(from);
	}

	@Override
	public HibernateQuery<?> from(EntityPath<?> from) {
		return query().from(from);
	}

	@Override
	public HibernateQuery<?> from(EntityPath<?>... from) {
		return query().from(from);
	}

	@Override
	public HibernateUpdateClause update(EntityPath<?> path) {
		return new HibernateUpdateClause(defaultSessionHolder(session.get()), path, templates);
	}

	@Override
	public HibernateQuery<?> query() {
		return new ExtendedHibernateQuery<Void>(session.get(), templates);
	}


}

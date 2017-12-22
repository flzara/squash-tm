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
package org.squashtest.tm.service.security.acls.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Component;
import org.squashtest.tm.security.annotation.InheritsAcls;

/**
 * This {@link ObjectIdentityRetrievalStrategy} checks if an entity should inherit ACLs from another entity. If so, the
 * object identity will be retrieved from the "constrained" object.
 *
 * @author Gregory Fouquet
 *
 */
@Component("squashtest.core.security.ObjectIdentityRetrievalStrategy")
public class InheritableAclsObjectIdentityRetrievalStrategy implements ObjectIdentityRetrievalStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(InheritableAclsObjectIdentityRetrievalStrategy.class);

	private static final class Key {
		public final Class<?> constrained; // NOSONAR final immutable field
		public final Class<?> heir; // NOSONAR final immutable field

		private Key(Class<?> constrained, Class<?> heir) {
			super();
			this.constrained = constrained;
			this.heir = heir;
		}

		// GENERATED:START
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 17;
			result = prime * result + (heir == null ? 0 : heir.hashCode());
			result = prime * result + (constrained == null ? 0 : constrained.hashCode());
			return result;
		}
		// GENERATED:END

		// GENERATED:START
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Key other = (Key) obj;
			if (heir == null) {
				if (other.heir != null) {
					return false;
				}
			} else if (!heir.equals(other.heir)) {
				return false;
			}
			if (constrained == null) {
				if (other.constrained != null) {
					return false;
				}
			} else if (!constrained.equals(other.constrained)) {
				return false;
			}
			return true;
		}
		// GENERATED:END

	}

	/**
	 * Strategy responsible for actually retrieving the object identities.
	 */
	@Inject
	@Named("annotatedPropertyObjectIdentityRetrievalStrategy")
	private ObjectIdentityRetrievalStrategy delegate;
	/**
	 * Session factory, used to retrieve constrained objects.
	 */
	@PersistenceContext
	private EntityManager em;
	/**
	 * Cache of hql queries used to retrieved constrained objects.
	 */
	private final Map<InheritableAclsObjectIdentityRetrievalStrategy.Key, String> hqlCache = new ConcurrentHashMap<>();

	/**
	 * If the domain object is annotated with {@link InheritsAcls}, will return an {@link ObjectIdentity} according to
	 * the annotation rules. Otherwise, simple delegates the retrieval.
	 */
	@Override
	public ObjectIdentity getObjectIdentity(Object domainObject) {
		InheritsAcls inherits = AnnotationUtils.findAnnotation(domainObject.getClass(), InheritsAcls.class);

		Object identityHolder;

		if (inherits == null) {
			identityHolder = domainObject;

			LOGGER.trace("Will use domain object for OID retrieval of {}", identityHolder);
		} else {
			identityHolder = findAclHolder(domainObject, inherits);

			LOGGER.trace("Will use constrained object {} for OID retrieval of {}", identityHolder, domainObject);

		}

		return delegate.getObjectIdentity(identityHolder);
	}

	private Object findAclHolder(Object domainObject, InheritsAcls inherits) {
		LOGGER.trace("Looking for constrained object for OID retrieval of {}", domainObject);

		String hql;

		hql = previouslyBuildQuery(domainObject, inherits);

		if (hql == null) {
			hql = buildQuery(domainObject, inherits);
			cacheQuery(domainObject, inherits, hql);
		}

		Query query = em.unwrap(Session.class).createQuery(hql);
		query.setParameter("heir", domainObject);

		return query.uniqueResult();
	}

	private void cacheQuery(Object domainObject, InheritsAcls inherits, String hql) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Caching query : [{},{}] -> '{}'", new Object[] { inherits.constrainedClass().getSimpleName(),
					domainObject.getClass().getSimpleName(), hql });
		}
		hqlCache.put(new Key(inherits.constrainedClass(), domainObject.getClass()), hql);

	}

	private String buildQuery(Object domainObject, InheritsAcls inherits) {
		String hql;
		checkAnnotation(inherits, domainObject.getClass());

		String entityName = inherits.constrainedClass().getSimpleName();

		if (isSingleValuedAssociation(inherits)) {
			// should be cached
			hql = "select constrained from " + entityName + " constrained where constrained." + inherits.propertyName()
					+ " = :heir";
		} else {
			// should be cached
			hql = "select constrained from " + entityName + " constrained where :heir in elements(constrained."
					+ inherits.collectionName() + ")";
		}
		return hql;
	}

	private String previouslyBuildQuery(Object domainObject, InheritsAcls inherits) {
		String hql;
		hql = hqlCache.get(new Key(inherits.constrainedClass(), domainObject.getClass()));

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Cache returned query '" + hql + "' for [" + inherits.constrainedClass().getSimpleName() + ','
					+ domainObject.getClass().getSimpleName() + ']');
		}

		return hql;
	}

	private boolean isSingleValuedAssociation(InheritsAcls annotation) {
		return StringUtils.isNotBlank(annotation.propertyName());
	}

	private void checkAnnotation(InheritsAcls annotation, Class<?> targetClass) {
		if (annotation.constrainedClass() == null) {
			throw new IllegalStateException(
					"Property constrainedClass of annotation InheritsAcls should not be null. Target class: "
							+ targetClass);
		}

		boolean singleValuedCandidate = StringUtils.isNotBlank(annotation.propertyName());
		boolean multiValuedCandidate = StringUtils.isNotBlank(annotation.collectionName());

		if (singleValuedCandidate && multiValuedCandidate) {
			throw new IllegalStateException(
					"Only one of 'propertyName' and 'collectionName' property of annotation InheritsAcls should be set. Target class: "
							+ targetClass);
		}

		if (!singleValuedCandidate && !multiValuedCandidate) {
			throw new IllegalStateException(
					"At least one of 'propertyName' and 'collectionName' properties of annotation InheritsAcls should be set. Target class: "
							+ targetClass);
		}

	}

}

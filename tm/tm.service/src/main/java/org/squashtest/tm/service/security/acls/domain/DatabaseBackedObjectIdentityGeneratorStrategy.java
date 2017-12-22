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

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Component;

/**
 * This {@link ObjectIdentityGenerator} fetches the entity using hibernate and
 * delegates the creation of {@link ObjectIdentity} to a
 * {@link ObjectIdentityRetrievalStrategy}.
 *
 * @author bsiri
 * @reviewed-on 2011/11/23
 */
@Component("squashtest.core.security.ObjectIdentityGeneratorStrategy")
public class DatabaseBackedObjectIdentityGeneratorStrategy implements ObjectIdentityGenerator {
	/**
	 * Object identity which won't match anything. Identifier is "0" to prevent
	 * funky behaviour when querying the ACLs. Type is target type suffixed with
	 * ":Unknown", which should not match any type known by the ACL system.
	 *
	 * @author Gregory Fouquet
	 *
	 */
	@SuppressWarnings("serial")
	private static class UnknownObjectIdentity implements ObjectIdentity {
		private final String type;

		private UnknownObjectIdentity(String type) {
			super();
			this.type = type + ":Unknown";
		}

		/**
		 * @see org.springframework.security.acls.model.ObjectIdentity#getIdentifier()
		 */
		@Override
		public Serializable getIdentifier() {
			return 0;
		}

		/**
		 * @see org.springframework.security.acls.model.ObjectIdentity#getType()
		 */
		@Override
		public String getType() {
			return type;
		}

	}

	@PersistenceContext
	private EntityManager em;

	@Inject
	@Named("squashtest.core.security.ObjectIdentityRetrievalStrategy")
	private ObjectIdentityRetrievalStrategy objectRetrievalStrategy;

	/**
	 * Creates an ObjectIdentity by : 1. fetching the entity using the given id
	 * and type 2. delegating to <code>objectRetrivalStrategy</code>
	 *
	 * When the entity is unknown, this return an object identity which matches
	 * nothing
	 */
	@Override
	public ObjectIdentity createObjectIdentity(Serializable id, String type) {
		try {
			Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(type);

			Object instance = em.find(clazz, id);

			if (instance == null) {
				return new UnknownObjectIdentity(type);
			}

			return objectRetrievalStrategy.getObjectIdentity(instance);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

	}
}

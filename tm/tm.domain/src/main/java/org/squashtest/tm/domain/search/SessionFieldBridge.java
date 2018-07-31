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
package org.squashtest.tm.domain.search;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.lucene.document.Document;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.squashtest.tm.domain.Identified;

@Configurable
public abstract class SessionFieldBridge implements FieldBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionFieldBridge.class);


	/*
	 * 2016-04-13
	 *
	 * Note : This is not supposed to be @Inject, rather @PersistenceContext. However the later doesn't work well with @Lazy, which we really need because of
	 * avoiding circular dependencies during the creation of the entity manager factory.
	 *
	 *  Using @Inject here seems to be ok because the bean actually injected is a shared entitymanager factorybean, which is the same result of using @PersistenceContext.
	 *  But I am not 100% sure there would be no problems (like, not thread-safe instances etc) so I'm leaving here this comment.
	 *
	 */

	@Inject
	@Lazy
	private EntityManager em;

	private Session getCurrentSession() {
		return em.unwrap(Session.class);
	}

	private SessionFactory getSessionFactory() {
		return em.getEntityManagerFactory().unwrap(SessionFactory.class);
	}

	

	protected abstract void writeFieldToDocument(String name, Session session, Object value, Document document,
												 LuceneOptions luceneOptions);

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		long start = 0;

		if (LOGGER.isDebugEnabled()) {
			start = System.nanoTime();
		}

		Session session;
		Transaction tx;

		try {
			session = getCurrentSession();
		} catch (InvalidDataAccessApiUsageException | IllegalStateException ex) { //NOSONAR, we create the session just after if we have not session
			session = null;
		}

		if (session == null) {
			session = getSessionFactory().openSession();
			try {
				tx = session.beginTransaction();
				try {
					writeFieldToDocument(name, session, value, document, luceneOptions);
					tx.commit();
				} catch (Exception indexationException) {
					LOGGER.warn("Exception while writing index to Lucene document for field {}, value {}.", name, value, indexationException);
					tx.rollback();
					throw indexationException;
				}
			} finally {
				try {
					session.close();
				} catch (Exception e) {
					//Do not rethrow the closing exception here as it will eat the eventual original indexationException witch can be in the flow at this stage
					LOGGER.error("UNABLE TO CLOSE HIBERNATE SESSION DEDICATED TO INDEXATION. Could lead to a DB connection starving...", e);
				}
			}
		} else {
			writeFieldToDocument(name, session, value, document, luceneOptions);
		}

		if (LOGGER.isTraceEnabled()) {
			long end = System.nanoTime();
			int timeInMilliSec = Math.round((end - start) / 1000000f);
			LOGGER.trace(this.getClass().getSimpleName() + ".set(..) took {} ms for entity {}", timeInMilliSec,
				((Identified) value).getId());
			final int threshold = 10;
			if (timeInMilliSec > threshold) {
				LOGGER.trace("BEWARE : " + this.getClass().getSimpleName() + ".set(..) took more than {} ms", threshold);
			}
		}
	}
}

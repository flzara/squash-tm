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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentContent;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.service.internal.repository.DeletionDao;

public abstract class HibernateDeletionDao implements DeletionDao {

	@PersistenceContext
	private EntityManager em;

	/**
	 * @deprecated use an entity manager instead
	 */
	@Deprecated
	protected Session getSession() {
		return em.unwrap(Session.class);
	}

	protected final EntityManager entityManager() {
		return em;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void removeAttachmentsLists(final List<Long> attachmentListIds) {

		List<Object[]> attachmentContentIds;

		if (attachmentListIds.isEmpty()) {
			return;
		}

		// we handle the cascade ourselves to make sure that the deletion is carried properly
		attachmentContentIds = executeSelectNamedQuery("attachment.getAttachmentAndContentIdsFromList", "listIds",
				attachmentListIds);

		// due to foreign key constraints and the like, the following
		// operations
		// must be carried in that order :

		if (!attachmentContentIds.isEmpty()) {

			Collection<Long> attachIds = CollectionUtils.collect(attachmentContentIds, new CellTableTransformer(0));

			Collection<Long> contentIds = CollectionUtils.collect(attachmentContentIds, new CellTableTransformer(1));

			executeDeleteNamedQuery("attachment.removeAttachments", "attachIds", attachIds);
			executeDeleteNamedQuery("attachment.removeContents", "contentIds", contentIds);
		}

		executeDeleteNamedQuery("attachment.deleteAttachmentLists", "listIds", attachmentListIds);

	}

	private static final class CellTableTransformer implements Transformer {
		private int cellIndex;

		private CellTableTransformer(int cellIndex) {
			this.cellIndex = cellIndex;
		}

		@Override
		public Object transform(Object input) {
			return ((Object[]) input)[cellIndex];
		}
	}

	@Override
	public void removeEntity(Object entity) {
		em.remove(entity);
	}

	@Override
	public void removeEntityNQ(String namedQuery, String namedParam, Long paramId){
		Query query = getSession().getNamedQuery(namedQuery);
		query.setParameter(namedParam, paramId, LongType.INSTANCE);
		query.executeUpdate();
	}

	@Override
	public void removeAttachmentList(AttachmentList list) {

		if (list == null) {
			return;
		}

		//[Issue 1456 problem with h2 database that will try to delete 2 times the same lob]
		Set<Attachment> attachmentList = list.getAllAttachments();

		for (Attachment attachment : attachmentList) {
			AttachmentContent content = attachment.getContent();
			content.setContent(null);
		}

		flush();
		//End [Issue 1456]

		removeEntity(list);

	}

	@Override
	public void flush() {
		getSession().flush();
	}

	/* **************** convenient shorthands **************************************************** */

	protected void executeDeleteNamedQuery(String namedQuery, String paramName, Collection<Long> ids) {
		if (!ids.isEmpty()) {
			Query query = getSession().getNamedQuery(namedQuery);
			query.setParameterList(paramName, ids, LongType.INSTANCE);
			query.executeUpdate();
		}
	}

	@SuppressWarnings("unchecked")
	protected <R> List<R> executeSelectNamedQuery(String namedQuery, String paramName, Collection<Long> ids) {
		if (!ids.isEmpty()) {
			Query query = getSession().getNamedQuery(namedQuery);
			query.setParameterList(paramName, ids, LongType.INSTANCE);
			return query.list();
		} else {
			return Collections.emptyList();
		}
	}

	protected void executeDeleteSQLQuery(String queryString, String paramName, Collection<Long> ids) {
		if (!ids.isEmpty()) {
			Query query = getSession().createSQLQuery(queryString);
			query.setParameterList(paramName, ids, LongType.INSTANCE);
			query.executeUpdate();
		}
	}

	@SuppressWarnings("unchecked")
	protected <R> List<R> executeSelectSQLQuery(String queryString, String paramName, Collection<Long> ids) {
		if (!ids.isEmpty()) {
			Query query = getSession().createSQLQuery(queryString);
			query.setParameterList(paramName, ids, LongType.INSTANCE);
			return query.list();
		} else {
			return Collections.emptyList();
		}
	}

}

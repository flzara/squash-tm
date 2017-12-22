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

import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.service.internal.foundation.collection.JpaPagingUtils;
import org.squashtest.tm.service.internal.repository.CustomCustomFieldBindingDao;

import javax.persistence.Query;
import java.util.List;


public class CustomFieldBindingDaoImpl extends HibernateEntityDao<CustomFieldBinding> implements CustomCustomFieldBindingDao {
	public static class NewBindingPosition {

		private Long bindingId;
		private int formerPosition;
		private int newPosition;

		public NewBindingPosition() {
		}

		public Long getBindingId() {
			return bindingId;
		}

		public void setBindingId(Long bindingId) {
			this.bindingId = bindingId;
		}

		public int getFormerPosition() {
			return formerPosition;
		}

		public void setFormerPosition(int formerPosition) {
			this.formerPosition = formerPosition;
		}

		public int getNewPosition() {
			return newPosition;
		}

		public void setNewPosition(Long newPosition) {
			this.newPosition = newPosition.intValue();
		}

		public boolean needsUpdate() {
			return formerPosition != newPosition;
		}

	}

	@Override
	public List<CustomFieldBinding> findAllForProjectAndEntity(long projectId, BindableEntity boundEntity,
		Paging paging) {
		Query q = entityManager.createNamedQuery("CustomFieldBinding.findAllForProjectAndEntity");
		q.setParameter("projectId", projectId);
		q.setParameter("entityType", boundEntity);

		JpaPagingUtils.addPaging(q, paging);
		return q.getResultList();
	}


	@Override
	public void removeCustomFieldBindings(List<Long> bindingIds) {

		if (!bindingIds.isEmpty()) {
			entityManager.createNamedQuery("CustomFieldBinding.removeCustomFieldBindings")
				.setParameter("cfbIds", bindingIds)
				.executeUpdate();

			List<NewBindingPosition> newPositions = recomputeBindingPositions();
			updateBindingPositions(newPositions);

		}
	}


	@SuppressWarnings("unchecked")
	protected List<NewBindingPosition> recomputeBindingPositions() {

		return entityManager.unwrap(Session.class).getNamedQuery("CustomFieldBinding.recomputeBindingPositions")
			.setResultTransformer(Transformers.aliasToBean(NewBindingPosition.class))
			.list();
	}


	protected void updateBindingPositions(List<NewBindingPosition> newPositions) {

		Query q = entityManager.createNamedQuery("CustomFielBinding.updateBindingPosition");

		for (NewBindingPosition newPos : newPositions) {
			if (newPos.needsUpdate()) {
				q.setParameter("newPos", newPos.getNewPosition());
				q.setParameter("id", newPos.getBindingId());
				q.executeUpdate();
			}
		}

	}


}

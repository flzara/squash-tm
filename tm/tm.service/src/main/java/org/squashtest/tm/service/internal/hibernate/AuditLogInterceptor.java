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
package org.squashtest.tm.service.internal.hibernate;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.squashtest.tm.domain.RelatedToAuditable;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.audit.AuditableSupport;
import org.squashtest.tm.security.UserContextHolder;

import java.io.Serializable;
import java.util.Date;

/**
 * This interceptor transparently logs creation / last modification data of any {@link Auditable} entity.
 *
 * @author Gregory Fouquet
 *
 */
@SuppressWarnings("serial")
public class AuditLogInterceptor extends EmptyInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogInterceptor.class);

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Deleting entity {}", entity);
		}
		if(RelatedToAuditable.class.isAssignableFrom(entity.getClass())){
			checkAndLogAuditableRelatedEntityModificationData(entity);
		}
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Entity {} is dirty and will be flushed", entity);
		}
		boolean currentStateIsModified = false;
		if (isAuditable(entity)) {
			checkAndLogModificationData(entity, currentState);
			currentStateIsModified = true;
		}
		if(RelatedToAuditable.class.isAssignableFrom(entity.getClass())){
			checkAndLogAuditableRelatedEntityModificationData(entity);
		}
		return currentStateIsModified;
	}

	private boolean isAuditable(Object entity) {
		return AnnotationUtils.findAnnotation(entity.getClass(), Auditable.class) != null;
	}

	private void checkAndLogModificationData(Object entity, Object[] currentState) {
		try {
			AuditableSupport audit = findAudit(currentState);
			// Feature 6763 - the 'last connected on' date was also updating the 'last modified on' date
			// so we added a boolean and now we check that the boolean is false before making changes.
			if (!audit.isSkipModifyAudit()) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("Updating audit {} of entity {}", audit, entity);
				}
				audit.setLastModifiedBy(getCurrentUser());
				audit.setLastModifiedOn(new Date());
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Non Auditable entity is : " + entity, e);
		}
	}

	private AuditableSupport findAudit(Object[] state) {
		for (Object field : state) {
			if (field != null && field.getClass().isAssignableFrom(AuditableSupport.class)) {
				return (AuditableSupport) field;
			}
		}
		throw new IllegalArgumentException("Could not find property of type '" + AuditableSupport.class + "'");
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Saving entity {}", entity);
		}

		boolean stateIsModified = false;
		if (isAuditable(entity)) {
			logCreationData(entity, state);
			stateIsModified = true;
		}
		if(RelatedToAuditable.class.isAssignableFrom(entity.getClass())){
			checkAndLogAuditableRelatedEntityModificationData(entity);
		}
		return stateIsModified;
	}

	private void logCreationData(Object entity, Object[] state) {
		try {
			AuditableSupport audit = findAudit(state);

			//one sets defaults only if they aren't provided at creation time
			if ( audit.getCreatedBy() ==null && audit.getCreatedOn()==null){

				audit.setCreatedBy(getCurrentUser());
				audit.setCreatedOn(new Date());

			}

		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Non Auditable entity is : " + entity, e);
		}
	}

	private String getCurrentUser() {
		return UserContextHolder.getUsername();
	}

	private void checkAndLogAuditableRelatedEntityModificationData(Object entity){
		RelatedToAuditable relatedToAuditable = (RelatedToAuditable) entity;
		relatedToAuditable.getAssociatedAuditableList().forEach(auditable -> {
			if(auditable != null){
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("Updating auditable {} related to entity {}", auditable, entity);
				}
				auditable.setLastModifiedOn(new Date());
				auditable.setLastModifiedBy(getCurrentUser());
			} else {
				throw new IllegalArgumentException("Could not update modification data. Unknown related auditable.");
			}
		});
	}
}

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
package org.squashtest.tm.exception;


/**
 * This should be raised when a transient entity name is already used by another entity.
 * 
 * @author Gregory Fouquet
 * 
 */
public class NameAlreadyInUseException extends DomainException {
	public enum EntityType {
		GENERIC("message.exception.nameAlreadyInUse", "unknown"),
		BUG_TRACKER("squashtm.action.exception.bugtracker.name.exists.label", "BugTracker");
		public final String i18nKey;
		public final String entityName;

		private EntityType(String i18nKey, String entityName) {
			this.i18nKey = i18nKey;
			this.entityName = entityName;
		}

	}

	private static final long serialVersionUID = 1395737862096099500L;
	private final String entityName;
	private final String name;
	private final EntityType entityType;

	public NameAlreadyInUseException(String entityName, String name) {
		super("The name '" + name + "' is already used by another " + entityName + " entity", "name");
		this.entityName = entityName;
		this.name = name;
		this.entityType = EntityType.GENERIC;
	}

	public NameAlreadyInUseException(EntityType type, String name) {
		super("The name '" + name + "' is already used by another " + type.entityName + " entity", "name");
		this.entityName = type.entityName;
		this.name = name;
		this.entityType = type;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.core.foundation.i18n.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return entityType.i18nKey;
	}

	/**
	 * @return the entityName
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public Object[] getI18nParams() {
		return new Object[] {name, entityName};
	}

}

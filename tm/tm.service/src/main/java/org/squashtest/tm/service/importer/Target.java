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
package org.squashtest.tm.service.importer;


/**
 * A Target is a reference to an entity that an import instruction will create, modify or delete. It is different from
 * {@link org.squashtest.tm.domain.EntityReference} in the sense that the later identities an instance by its ID while the
 * Target identifies it by other features (eg its path, or index within a collection + target of the owner of the collection).
 * This allows for useful use cases like exporting a test case from an instance of Squash A and importing in instance B,
 * which have each their own ID spaces.
 *
 */
public interface Target {
	EntityType getType();

	/**
	 * Self-validation method, which says whether the target instance has every piece of information required to
	 * completely designate a specific instance of the entity (see class-level comment). In other word, is the
	 * target syntactically correct.
	 *
	 * @return true if all required information are present and with no formatting errors.
	 */
	boolean isWellFormed();
}

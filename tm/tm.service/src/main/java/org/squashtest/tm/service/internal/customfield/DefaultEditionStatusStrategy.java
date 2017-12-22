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
package org.squashtest.tm.service.internal.customfield;


import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customfield.BindableEntity;

/**
 * ValueEditionStrategy which only checks for ACLs
 * 
 * @author Gregory Fouquet
 * 
 */
@Component
class DefaultEditionStatusStrategy extends ValueEditionStatusHelper implements ValueEditionStatusStrategy {
	/**
	 * 
	 */
	public DefaultEditionStatusStrategy() {
		super();
	}

	/**
	 * @see org.squashtest.tm.service.internal.customfield.ValueEditionStatusStrategy#isEditable(long, org.squashtest.tm.domain.customfield.BindableEntity)
	 */
	@Override
	public boolean isEditable(long boundEntityId, BindableEntity bindableEntity) {
		return userHasPermission(boundEntityId, bindableEntity);
	}

}

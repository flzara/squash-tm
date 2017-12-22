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
package org.squashtest.tm.service.customfield;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.service.security.Authorizations;

/**
 * Facade service for custom fields management.
 * 
 * @author Gregory Fouquet
 * 
 */
@Transactional
@DynamicManager(name = "squashtest.tm.service.CustomFieldManagerService", entity = CustomField.class)
public interface CustomFieldManagerService extends CustomCustomFieldManagerService, CustomFieldFinderService {
	String HAS_ROLE_ADMIN = Authorizations.HAS_ROLE_ADMIN;

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeLabel(long customFieldId, String label);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeDefaultValue(long customFieldId, String defaultValue);

}

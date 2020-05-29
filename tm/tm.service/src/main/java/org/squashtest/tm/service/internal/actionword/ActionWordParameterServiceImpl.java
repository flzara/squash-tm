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
package org.squashtest.tm.service.internal.actionword;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.service.actionword.ActionWordParameterService;
import org.squashtest.tm.service.internal.repository.ActionWordParameterDao;

import javax.inject.Inject;

@Service
@Transactional
public class ActionWordParameterServiceImpl implements ActionWordParameterService {

	@Inject
	private ActionWordParameterDao actionWordParameterDao;

	@Override
	public String renameParameter(long parameterId, String newName) {
		ActionWordParameter parameter = actionWordParameterDao.getOne(parameterId);
		parameter.setName(newName);
		return newName;
	}
}

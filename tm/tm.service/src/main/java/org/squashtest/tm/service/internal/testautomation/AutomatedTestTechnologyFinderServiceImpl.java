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
package org.squashtest.tm.service.internal.testautomation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testautomation.AutomatedTestTechnology;
import org.squashtest.tm.service.internal.repository.AutomatedTestTechnologyDao;
import org.squashtest.tm.service.testautomation.AutomatedTestTechnologyFinderService;

import javax.inject.Inject;
import java.util.List;

@Service("squashtest.tm.service.testautomation.AutomatedTestTechnologyFinderService")
@Transactional
public class AutomatedTestTechnologyFinderServiceImpl implements AutomatedTestTechnologyFinderService {

	@Inject
	private AutomatedTestTechnologyDao automatedTestTechnologyDao;

	@Override
	public List<AutomatedTestTechnology> getAllAvailableAutomatedTestTechnology() {
		return automatedTestTechnologyDao.findAll();
	}

	@Override
	public AutomatedTestTechnology findById(long automatedTestTechnologyId) {
		return automatedTestTechnologyDao.getOne(automatedTestTechnologyId);
	}

	@Override
	public AutomatedTestTechnology findByName(String automatedTestTechnologyName) {
		return automatedTestTechnologyDao.findByName(automatedTestTechnologyName);
	}
}

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
package org.squashtest.tm.internal.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.internal.domain.report.query.ReportQuery;
import org.squashtest.tm.internal.domain.report.query.UnsupportedFlavorException;
import org.squashtest.tm.internal.repository.ReportQueryDao;
import org.squashtest.tm.plugin.report.std.service.DataFilteringService;
import org.squashtest.tm.plugin.report.std.service.ReportService;

@Service("squashtest.tm.service.ReportService")
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

	@Inject
	private ReportQueryDao reportQueryDao;

	@Inject
	private DataFilteringService filterService;

	@Override
	public List<?> executeQuery(ReportQuery query) {
		try {
			query.setDataFilteringService(filterService);
			return reportQueryDao.executeQuery(query);
		}catch(UnsupportedFlavorException ufe){
			throw new RuntimeException(ufe);
		}
	}
}

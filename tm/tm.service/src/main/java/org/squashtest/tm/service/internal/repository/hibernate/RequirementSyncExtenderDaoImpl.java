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

import org.jooq.DSLContext;
import org.squashtest.tm.service.internal.repository.CustomRequirementSyncExtenderDao;

import javax.inject.Inject;
import java.util.List;

import static org.squashtest.tm.jooq.domain.Tables.*;

/**
 * Implementation of {@link CustomRequirementSyncExtenderDao}
 * @author aguilhem
 */
public class RequirementSyncExtenderDaoImpl implements CustomRequirementSyncExtenderDao {
	@Inject
	DSLContext DSL;

	@Override
	public List<String> findAllRemoteReqIdVerifiedByATestCaseByServerUrl(String serverUrl, Long testCaseId) {
		return DSL.select(REQUIREMENT_SYNC_EXTENDER.REMOTE_REQ_ID)
			.from(REQUIREMENT_SYNC_EXTENDER)
			.leftJoin(REQUIREMENT).on(REQUIREMENT.RLN_ID.eq(REQUIREMENT_SYNC_EXTENDER.REQUIREMENT_ID))
			.leftJoin(REQUIREMENT_VERSION).on(REQUIREMENT_VERSION.REQUIREMENT_ID.eq(REQUIREMENT.RLN_ID))
			.leftJoin(REQUIREMENT_VERSION_COVERAGE).on(REQUIREMENT_VERSION_COVERAGE.VERIFIED_REQ_VERSION_ID.eq(REQUIREMENT_VERSION.RES_ID))
			.leftJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(REQUIREMENT_VERSION_COVERAGE.VERIFYING_TEST_CASE_ID))
			.leftJoin(BUGTRACKER).on(BUGTRACKER.BUGTRACKER_ID.eq(REQUIREMENT_SYNC_EXTENDER.SERVER_ID))
		   	.leftJoin(THIRD_PARTY_SERVER).on(BUGTRACKER.BUGTRACKER_ID.eq(THIRD_PARTY_SERVER.SERVER_ID))
			.where(THIRD_PARTY_SERVER.URL.eq(serverUrl))
			.and(TEST_CASE.TCLN_ID.eq(testCaseId)).fetch(REQUIREMENT_SYNC_EXTENDER.REMOTE_REQ_ID);
	}
}

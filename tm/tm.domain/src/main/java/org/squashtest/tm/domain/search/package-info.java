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
/**
 * This file contains Hibernate named queries used by hibernate search bridges.
 *
 * /!\ Before adding a query, check it does not exist somewhere else !
 *
 * @author Gregory Fouquet
 */
@NamedQueries({
	// ====== RequirementVersion queries ======
	// returns how many children a given requirement has
	@NamedQuery(name="requirement.countChildren", query="select count(elements(r.children)) from Requirement r where r.id = :id"),
	// returns either 0 or 1 when the given version has a parent requirement (ie is nested)
	@NamedQuery(name = "requirementVersion.countParentRequirement", query = "select count(r) from Requirement r join r.children c join c.versions v where v.id = :id"),
	// returns either 0 or 1 when the given version is the current version of the requirement
	@NamedQuery(name = "requirementVersion.countCurrentVersion", query = "select count(cur) from Requirement r join r.resource cur where cur.id = :id and cur.status != :obsolete"),
	@NamedQuery(name = "requirementVersion.countAttachments", query = "select count(att) from RequirementVersion rv join rv.attachmentList al join al.attachments att where rv.id = :id"),

	// ====== TestCase queries ======
	@NamedQuery(name = "testCase.countAttachments", query = "select count(att) from TestCase tc join tc.attachmentList al join al.attachments att where tc.id = :id"),

	// ====== Execution queries ======
	// I believe that query is not used. TODO : assert whether we can remove this
	@NamedQuery(name = "execution.countAttachments", query = "select count(att) from Execution tc join tc.attachmentList al join al.attachments att where tc.id = :id"),

})
package org.squashtest.tm.domain.search;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;


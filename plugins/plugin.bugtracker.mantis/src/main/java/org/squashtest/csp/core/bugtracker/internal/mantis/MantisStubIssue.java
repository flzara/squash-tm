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
package org.squashtest.csp.core.bugtracker.internal.mantis;

import org.squashtest.csp.core.bugtracker.mantis.binding.IssueData;
import org.squashtest.csp.core.bugtracker.mantis.binding.ObjectRef;

import java.math.BigInteger;

/**
 * Created by jprioux on 14/03/2019.
 * Empty Mantis Issue, created especially for deleted issues
 */
public class MantisStubIssue extends IssueData {

	public MantisStubIssue(BigInteger key, String summary) {
		super(key,
			null,
			null,
			new ObjectRef(BigInteger.valueOf(-1L), ""),
			null,
			null,
			null,
			null,
			null,
			summary,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null);
	}
}

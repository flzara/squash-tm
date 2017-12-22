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
package org.squashtest.tm.domain.search;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

public class IndexMonitor {

	private static final BigInteger CENT = BigInteger.valueOf(100);

	public static Map<Class<?>, IndexMonitor> monitors = new HashMap<>();
	public static IndexMonitor total = new IndexMonitor();

	static {
		monitors.put(TestCase.class, new IndexMonitor());
		monitors.put(RequirementVersion.class, new IndexMonitor());
		monitors.put(IterationTestPlanItem.class, new IndexMonitor());
	}

	private final AtomicLong totalCount = new AtomicLong();
	private final AtomicLong documentsBuilt = new AtomicLong();


	public void addToTotalCount(long count) {
		totalCount.addAndGet(count);
	}

	public void addToDocumentsBuilded(int doc) {
		documentsBuilt.addAndGet(doc);
	}



	public BigInteger getTotalCount() {
		return BigInteger.valueOf(totalCount.get());
	}

	public BigInteger getDocumentsBuilt() {
		return BigInteger.valueOf(documentsBuilt.get());
	}

	public BigInteger getPercentComplete() {

		if (getTotalCount().equals(BigInteger.ZERO)) {
			return CENT;
		}
		return getDocumentsBuilt().multiply(CENT).divide(getTotalCount());

	}

	public static void resetTotal() {
		total = new IndexMonitor();
	}

}

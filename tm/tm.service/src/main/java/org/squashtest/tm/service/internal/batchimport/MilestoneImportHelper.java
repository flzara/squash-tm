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
package org.squashtest.tm.service.internal.batchimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.service.milestone.MilestoneManagerService;

/**
 * Helper which can be used by importers to process milestones
 *
 * @author Gregory Fouquet
 *
 */
@Component
public class MilestoneImportHelper {
	/**
	 * Immutable result of a partition operation.
	 *
	 * @author Gregory Fouquet
	 *
	 */
	public static class Partition {
		/**
		 * Collection of items which passed the partitioning criteria
		 */
		public final List<String> passing; // NOSONAR immutable collection

		/**
		 * Collection of items which failed the partitioning criteria
		 */
		public final List<String> rejected; // NOSONAR immutable collection

		private Partition(List<String> rejected, List<String> passing) {
			super();
			this.rejected = Collections.unmodifiableList(rejected);
			this.passing = Collections.unmodifiableList(passing);
		}

	}

	private static final Partition EMPTY_PARTITION = new Partition(Collections.<String> emptyList(),
			Collections.<String> emptyList());

	private final MilestoneManagerService milestoneFinder;

	@Inject
	protected MilestoneImportHelper(MilestoneManagerService milestoneFinder) { // NOSONAR This should be used by Spring only
		super();
		this.milestoneFinder = milestoneFinder;
	}

	public Partition partitionExisting(@NotNull Collection<String> names) {
		if (names.isEmpty()) {
			return EMPTY_PARTITION;
		}

		List<String> existing = milestoneFinder.findExistingNames(names);
		List<String> unknown = new ArrayList<>(names);
		unknown.removeAll(existing);

		return new Partition(unknown, existing);
	}

	public Partition partitionBindable(@NotNull Collection<String> names) {
		if (names.isEmpty()) {
			return EMPTY_PARTITION;
		}
		
		List<MilestoneStatus> bindableStatus = MilestoneStatus.getAllStatusAllowingObjectBind();

		List<String> bindable = milestoneFinder.findBindableExistingNames(names, bindableStatus);
		List<String> notBindable = new ArrayList<>(names);
		notBindable.removeAll(bindable);

		return new Partition(notBindable, bindable);
	}

	/**
	 * @param milestones
	 * @return
	 */
	public List<Milestone> findBindable(Collection<String> names) {
		List<MilestoneStatus> bindableStatus = MilestoneStatus.getAllStatusAllowingObjectBind();
		List<Milestone> result = new ArrayList<>();
		for (MilestoneStatus milestoneStatus : bindableStatus) {
			result.addAll( milestoneFinder.findAllByNamesAndStatus(names, milestoneStatus));
		}
		return result;
	}
	
	/**
	 * @param milestones
	 * @return
	 */
	public List<Milestone> findInProgressAndFinished(Collection<String> names) {
		List<Milestone> milestones = new ArrayList<>();
		milestones.addAll(milestoneFinder.findAllByNamesAndStatus(names, MilestoneStatus.IN_PROGRESS));
		milestones.addAll(milestoneFinder.findAllByNamesAndStatus(names, MilestoneStatus.FINISHED));
		return milestones;
	}
}

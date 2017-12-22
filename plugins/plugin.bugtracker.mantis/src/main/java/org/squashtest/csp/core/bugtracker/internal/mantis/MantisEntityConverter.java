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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Category;
import org.squashtest.csp.core.bugtracker.domain.Identifiable;
import org.squashtest.csp.core.bugtracker.domain.Permission;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.domain.Severity;
import org.squashtest.csp.core.bugtracker.domain.Status;
import org.squashtest.csp.core.bugtracker.domain.User;
import org.squashtest.csp.core.bugtracker.domain.Version;
import org.squashtest.csp.core.bugtracker.mantis.binding.AccountData;
import org.squashtest.csp.core.bugtracker.mantis.binding.IssueData;
import org.squashtest.csp.core.bugtracker.mantis.binding.ObjectRef;
import org.squashtest.csp.core.bugtracker.mantis.binding.ProjectData;
import org.squashtest.csp.core.bugtracker.mantis.binding.ProjectVersionData;

public final class MantisEntityConverter {
	private MantisEntityConverter() {
		super();
	}

	public static List<Severity> convertSeverities(ObjectRef[] mantisSeverities) {
		ArrayList<Severity> severities = new ArrayList<>(mantisSeverities.length);

		for (ObjectRef ms : mantisSeverities) {
			String id = mantis2SquashId(ms.getId());
			String name = ms.getName();
			severities.add(new Severity(id, name));
		}

		return severities;
	}

	public static List<Priority> mantis2SquashPriority(ObjectRef[] mantisSeverities) {
		List<Priority> priorities = new LinkedList<>();

		for (ObjectRef mp : mantisSeverities) {
			String id = mantis2SquashId(mp.getId());
			String name = mp.getName();
			priorities.add(new Priority(id, name));
		}

		return priorities;
	}

	public static List<BTProject> mantis2SquashProject(ProjectData[] mantisProjects){

		if (mantisProjects==null || mantisProjects.length==0){
			return Collections.emptyList();
		}
		else{
			List<BTProject> projects = new LinkedList<>();

			for (ProjectData mpd : mantisProjects){
				String id = mantis2SquashId(mpd.getId());
				String name = mpd.getName();
				projects.add(new BTProject(id, name));

				//also remember to add its subprojects
				projects.addAll(mantis2SquashProject(mpd.getSubprojects()));
			}

			return projects;
		}
	}

	/***
	 * This method convert a mantis project into BTProject
	 *
	 * @param data
	 *            the mantis project data
	 * @return the corresponding BTProject
	 */
	public static BTProject mantis2SquashSingleProject(ObjectRef data) {
		return new BTProject(mantis2SquashId(data.getId()), data.getName());
	}

	/***
	 * Convert a mantis priority into Priority
	 *
	 * @param data
	 *            the mantis data
	 * @return a Priority
	 */
	public static Priority mantis2SquashPriority(ObjectRef data) {
		return new Priority(mantis2SquashId(data.getId()), data.getName());
	}

	/**
	 * Convert a mantis status into Status
	 *
	 * @param mantisStatus
	 *
	 * @return a Status
	 *
	 */
	public static Status mantis2SquashStatus(ObjectRef mantisStatus){
		if (mantisStatus == null){
			return Status.NO_STATUS;
		}
		else{
			return new Status(mantis2SquashId(mantisStatus.getId()), mantisStatus.getName());
		}
	}


	/**
	 *
	 * @return the list of the Squash Version corresponding to the input, or a list containing only Version.NO_VERSION
	 * if the input is null or empty.
	 */
	public static List<Version> mantis2SquashVersion(ProjectVersionData[] mantisVersions){
		List<Version> versions = new LinkedList<>();

		if (mantisVersions == null || mantisVersions.length==0){
			versions.add(Version.NO_VERSION);
		}

		else {
			for (ProjectVersionData pvd : mantisVersions){
				String id = mantis2SquashId(pvd.getId());
				String name = pvd.getName();
				versions.add(new Version(id, name));
			}
		}

		return versions;

	}


	/**
	 *
	 * Note : Mantis never gives an id to its categories. The ID provided to the corresponding
	 * Squash entity is purely artificial and practicaly never used. It exists only for
	 * the coherence of the code so do not rely on it for more than temporary use.
	 *
	 * @param mantisCcategories
	 * @return a list of Squahs Category corresponding to the input, or a list containing Category.NO_CATEGORY
	 * only if the input is null or empty.
	 */
	public static List<Category> mantis2SquashCategory(String[] mantisCategories){

		List<Category> categories  = new LinkedList<>();
		long dummyId=0;

		if (mantisCategories==null || mantisCategories.length==0){
			categories.add(Category.NO_CATEGORY);
		}
		else{
			for (String category : mantisCategories){
				String id=Long.valueOf(dummyId++).toString();
				String name = category;
				categories.add(new Category(id,name));
			}
		}


		return categories;
	}

	public static List<Permission> mantis2SquashPermission(ObjectRef[] mantisPermissions){
		List<Permission> accessLevels = new LinkedList<>();

		for (ObjectRef level : mantisPermissions){
			String id = mantis2SquashId(level.getId());
			String name = level.getName();
			accessLevels.add(new Permission(id, name));
		}

		return accessLevels;
	}

	public static List<User> mantis2SquashUser(AccountData[] mantisUsers){
		List<User> users = new LinkedList<>();

		for (AccountData muser : mantisUsers){
			String id = mantis2SquashId(muser.getId());
			String name = muser.getName();
			users.add(new User(id, name));
		}

		return users;

	}

	/***
	 * Converts a mantis User into Squash User
	 *
	 * @param data
	 *            the mantis User data
	 * @return the corresponding User
	 */
	public static User mantis2SquashSingleUser(AccountData data) {
		return new User(mantis2SquashId(data.getId()), data.getName());
	}

	public static AccountData squash2MantisUser(User squashUser){
		AccountData data = new AccountData();
		data.setId(squash2MantisId(squashUser.getId()));
		data.setName(squashUser.getName());

		return data;

	}



	public static BigInteger squash2MantisId(String squashId){
		return BigInteger.valueOf(Long.parseLong(squashId));
	}

	public static String mantis2SquashId(BigInteger mantisId){
		return mantisId.toString();
	}


	/* **** the project of a mantis issue is just a stub (an ObjectRef) **** */
	public static IssueData squashToMantisIssue(BTIssue squashIssue){
		/*
		 * will set the :
		 * 		project,
		 * 		priority,
		 * 		version,
		 * 	    category,
		 * 		assignee,
		 * 		summary,
		 * 		description,
		 * 		notes
		 */

		IssueData data = new IssueData();

		data.setProject(makeObjectRef(squashIssue.getProject()));

		//nope, that's not a bug. the Priority in Squash maps to Severity in Mantis, accordingly to the specs. Don't touch that.
		data.setSeverity(makeObjectRef(squashIssue.getPriority()));

		//the next two may be empty, we need to check that first.
		Version sqVersion = squashIssue.getVersion();
		if (sqVersion.isDummy()){
			data.setVersion(null);
		}else{
			data.setVersion(sqVersion.getName());
		}

		Category sqCategory = squashIssue.getCategory();
		if (sqCategory.isDummy()){
			data.setCategory(null);
		}else{
			data.setCategory(sqCategory.getName());
		}



		AccountData assignee = squash2MantisUser(squashIssue.getAssignee());
		data.setHandler(assignee);

		data.setSummary(squashIssue.getSummary());
		data.setDescription(squashIssue.getDescription());
		data.setAdditional_information(squashIssue.getComment());

		return data;
	}

	/***
	 * Returns a BTIssue with the following params : id, summary, project, severity (as Priority), version, reporter, category,
	 * assignee, description, comment, created on, status
	 *
	 * @param mantisIssue
	 *            the raw mantis issue
	 * @return the corresponding BTIssue
	 */
	public static BTIssue mantis2squashIssue(IssueData mantisIssue) {
		BTIssue issue = new BTIssue();

		issue.setId(mantis2SquashId(mantisIssue.getId()));

		issue.setSummary(mantisIssue.getSummary());
		issue.setProject(mantis2SquashSingleProject(mantisIssue.getProject()));
		issue.setPriority(mantis2SquashPriority(mantisIssue.getSeverity()));
		issue.setVersion(mantisIssue.getVersion() != null ? new Version(null, mantisIssue.getVersion())
				: Version.NO_VERSION);
		issue.setReporter(mantis2SquashSingleUser(mantisIssue.getReporter()));
		issue.setCategory(mantisIssue.getCategory() != null ? new Category(null, mantisIssue.getCategory())
				: Category.NO_CATEGORY);
		issue.setAssignee(mantisIssue.getHandler() != null ? mantis2SquashSingleUser(mantisIssue.getHandler())
				: User.NO_USER);
		issue.setDescription(mantisIssue.getDescription());
		issue.setComment(mantisIssue.getAdditional_information() != null ? mantisIssue.getAdditional_information() : "");
		issue.setCreatedOn(mantisIssue.getDate_submitted().getTime());
		issue.setStatus(mantis2SquashStatus(mantisIssue.getStatus()));

		return issue;
	}

	public static BTIssue issueNotFound(String issueKey, MantisExceptionConverter conv) {
		BTIssue issue = new BTIssue();
		issue.setId(issueKey);
		issue.setSummary(conv.getIssueNotFoundMsg());
		issue.setProject(new BTProject("", ""));
        return issue;
	}



	/* **** private utilities **** */

	private static ObjectRef makeObjectRef(Identifiable<?> squashEntity){
		if (! squashEntity.isDummy()){
			ObjectRef oRef = new ObjectRef();
			oRef.setId(squash2MantisId(squashEntity.getId()));
			oRef.setName(squashEntity.getName());
			return oRef;
		}
		else{
			return null;
		}
	}


}

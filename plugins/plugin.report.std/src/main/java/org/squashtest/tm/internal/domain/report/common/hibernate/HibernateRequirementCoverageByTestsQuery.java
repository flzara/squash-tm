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
package org.squashtest.tm.internal.domain.report.common.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.internal.domain.report.common.dto.ReqCoverageByTestProjectDto;
import org.squashtest.tm.internal.domain.report.common.dto.ReqCoverageByTestRequirementSingleDto;
import org.squashtest.tm.internal.domain.report.common.dto.ReqCoverageByTestStatType;
import org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery;
import org.squashtest.tm.internal.domain.report.query.hibernate.ReportCriterion;

/**
 *
 * Manage hibernate query to get the requirements covered by a given test-case
 *
 * @author bsiri
 * @reviewed-on 2011-11-30
 */
public class HibernateRequirementCoverageByTestsQuery extends HibernateReportQuery {
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateRequirementCoverageByTestsQuery.class);
	/***
	 * The rates default value
	 */
	private static final double DEFAULT_RATE_VALUE = 100D;

	/***
	 * Name of the project created only to display totals in the report
	 */
	private static final String TOTAL_PROJECT_NAME = "TOTAL";

	/**
	 * when picking by milestone, will only treat versions bound to
	 * the given milestone.
	 *
	 */
	private static final int REPORT_VERSION_BOUND_TO_MILESTONE =  0;

	/**
	 * will treat each version of requirement as a separate requirement
	 */
	private static final int REPORT_EACH_VERSION = 1;
	/**
	 * will take into account only the last version of the requirement
	 */
	private static final int REPORT_LAST_VERSION = 2;

	private static final String PROJECT_IDS = "projectIds[]";

	private static final String MILESTONE_IDS = "milestones";


	private static final String FIND_REQUIREMENT_PARENT_NAMES =
			"select rlnc.descendant_id, res.name "+
					"from RLN_RELATIONSHIP rlnc  "+
					"inner join REQUIREMENT_FOLDER rf on rlnc.ancestor_id = rf.rln_id  "+
					"inner join RESOURCE res on rf.res_id = res.res_id "+
					"where rlnc.descendant_id in (:reqIds) "+

			"UNION "+

			"select rlnc.descendant_id, res.name "+
			"from RLN_RELATIONSHIP rlnc  "+
			"inner join REQUIREMENT r on rlnc.ancestor_id = r.rln_id  "+
			"inner join RESOURCE res on r.current_version_id = res.res_id "+
			"where rlnc.descendant_id in (:reqIds) "+

			"UNION "+

			"select req.rln_id, '-' "+
			"from REQUIREMENT req  "+
			"left join RLN_RELATIONSHIP rlnc on req.rln_id = rlnc.descendant_id "+
			"where rlnc.descendant_id is null "+
			"and req.rln_id in (:reqIds) ";


	public HibernateRequirementCoverageByTestsQuery() {
		Map<String, ReportCriterion> criterions = getCriterions();

		ReportCriterion projectIds = new ProjectIdsIsInIds(PROJECT_IDS, "id", Project.class, "projects");
		// note : the name here follows the naming convention of http requests for array parameters. It allows the
		// controller to directly map the http query string to that criterion.
		criterions.put(PROJECT_IDS, projectIds);

		ReportCriterion milestoneIds = new MilestoneIdsIsInIds(MILESTONE_IDS, "id", Milestone.class, "milestones");
		criterions.put(MILESTONE_IDS, milestoneIds);

		ReportCriterion reportMode = new RequirementReportTypeCriterion("mode", "on s'en fout");
		criterions.put("mode", reportMode);
	}

	private static class MilestoneIdsIsInIds extends IsInSet<Long>{
		public MilestoneIdsIsInIds(String criterionName, String attributePath, Class<?> entityClass, String entityAlias) {
			super(criterionName, attributePath, entityClass, entityAlias);
		}

		@Override
		public Long fromValueToTypedValue(Object o) {
			return Long.parseLong(o.toString());
		}
	}

	private static class ProjectIdsIsInIds extends IsInSet<Long> {

		public ProjectIdsIsInIds(String criterionName, String attributePath, Class<?> entityClass, String entityAlias) {
			super(criterionName, attributePath, entityClass, entityAlias);
		}

		@Override
		public Long fromValueToTypedValue(Object o) {
			return Long.parseLong(o.toString());
		}
	}

	@Override
	public DetachedCriteria createHibernateQuery() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<?> doInSession(Session session) {

		// find the ids of all the requirements encompassed in the scope of this report instance.
		List<Long> ids = findRequirementIds(session);

		// find the corresponding requirements
		List<Requirement> requirements = findRequirements(session, ids);

		// find the name of their parents. The result is a pair of (requirementid, parentname)
		List<Object[]> parentsNameOfRequirements = findParentsNames(session, ids);

		// pair the requirements with their parent's name
		List<Object[]> requirementsAndParents = pairRequirementAndParents(requirements, parentsNameOfRequirements);

		// return
		return requirementsAndParents;
	}


	private List<Long> findRequirementIds(Session session){

		// select by milestone
		if (this.getCriterions().get(MILESTONE_IDS).getParameters() != null){
			return idsByMilestones(session);
		}
		else{
			return idsByProject(session);
		}


	}

	private List<Long> idsByMilestones(Session session){
		Object[] milestoneIds = this.getCriterions().get(MILESTONE_IDS).getParameters();

		Collection<Long> mIds = new ArrayList<>(milestoneIds.length);
		for (Object o : milestoneIds){
			mIds.add(Long.valueOf(o.toString()));
		}

		String hql = "select req.id from Requirement req join req.versions version join version.milestones mstones "+
				"where mstones.id in (:milestones)";

		Query q = session.createQuery(hql);
		q.setParameterList("milestones", mIds, LongType.INSTANCE);

		return q.list();

	}

	private List<Long> idsByProject(Session session){

		List<Long> projectIds = new ArrayList<>();
		boolean runOnAllProjects = true;

		if (this.getCriterions().get(PROJECT_IDS).getParameters() != null) {
			runOnAllProjects = false;
			// Put ids in a list
			for (Object id : this.getCriterions().get(PROJECT_IDS).getParameters()) {
				projectIds.add(Long.parseLong((String) id));
			}
		}

		String hql = "select req.id from Requirement req";
		if (! runOnAllProjects){
			hql +=" where req.project.id in (:projectIds)";
		}

		Query query = session.createQuery(hql);

		if (! runOnAllProjects){
			query.setParameterList("projectIds", projectIds, LongType.INSTANCE);
		}

		return query.list();
	}


	private List<Requirement> findRequirements(Session session, List<Long> ids) {
		if(ids.isEmpty()){
			return Collections.emptyList();
		}
		return session.createQuery("from Requirement where id in (:ids)")
				.setParameterList("ids", ids, LongType.INSTANCE)
				.list();

	}


	private List<Object[]> findParentsNames(Session session, List<Long> ids){
		if(ids.isEmpty()){
			return Collections.emptyList();
		}
		return session.createSQLQuery(FIND_REQUIREMENT_PARENT_NAMES)
				.setParameterList("reqIds", ids, LongType.INSTANCE)
				.list();
	}


	private List<Object[]> pairRequirementAndParents(List<Requirement> requirements, List<Object[]>parentsNameOfRequirements){

		List<Object[]> requirementsAndParents = new LinkedList<>();

		for (Requirement req : requirements){

			ListIterator<Object[]> iterNames = parentsNameOfRequirements.listIterator();

			while (iterNames.hasNext()){
				Object[] tuple = iterNames.next();
				Long id = ((BigInteger)tuple[0]).longValue();
				if (id.equals(req.getId())){
					requirementsAndParents.add(new Object[]{req, tuple[1]});
					iterNames.remove();
					break;
				}
			}

		}

		return requirementsAndParents;

	}


	// ************************ post processing part **********************************

	@SuppressWarnings("unchecked")
	@Override
	public List<?> convertToDto(List<?> rawData) {
		// a bit of cleaning first.
		List<Object[]> filteredData = filterUnwantedDataOut((List<Object[]>) rawData);
		// Browse data
		Map<Long, ReqCoverageByTestProjectDto> projectList = populateRequirementDtosAndUpdateProjectStatistics(filteredData);
		// Create projectTotals to calculate... Totals
		ReqCoverageByTestProjectDto projectTotals = createProjectDto(TOTAL_PROJECT_NAME);
		// Now that we have all requirement numbers, we can update projectDto rates
		calculateProjectsCoverageRates(projectList, projectTotals);
		// update projectTotals rates
		calculateProjectCoverageRates(projectTotals);
		// add it to the list. We only need the sorted Map values...
		List<ReqCoverageByTestProjectDto> toReturn = new ArrayList<>(projectList.values());

		toReturn.add(projectTotals);

		return toReturn;
	}

	private void calculateProjectsCoverageRates(Map<Long, ReqCoverageByTestProjectDto> projectList,
			ReqCoverageByTestProjectDto projectTotals) {
		for (ReqCoverageByTestProjectDto project : projectList.values()) {
			// Update current project rate
			calculateProjectCoverageRates(project);
			// update projectTotals
			projectTotals.increaseTotals(project.getRequirementNumbers(), project.getRequirementStatusNumbers());
		}
	}

	// Issue 4818 : if the scope used for this report is a given milestone,
	// here is where we find its label. We extract it from one of the projects
	// and stuff it in  the dtos.
	// another maggot on the pile of shit.
	private Map<Long, ReqCoverageByTestProjectDto> populateRequirementDtosAndUpdateProjectStatistics(
			List<Object[]> filteredData) {

		if (filteredData.isEmpty()){
			return new HashMap<>();
		}

		String milestone = extractMilestoneLabel((Requirement)filteredData.get(0)[0]);

		// First initiate the projectDTO map, project id is the key
		Map<Long, ReqCoverageByTestProjectDto> projectList = new HashMap<>();
		for (Object[] objects : filteredData) {
			// Current project
			ReqCoverageByTestProjectDto currentProject;
			// easier to identify the two objects this way
			Requirement requirement = (Requirement) objects[0];
			String parentName = (String) objects[1];
			// Get current project Id from requirement
			Long projectId = requirement.getProject().getId();

			// Create the requirementSingleDtos depending on the mode
			List<ReqCoverageByTestRequirementSingleDto> requirementSingleDtos = createRequirementSingleDtos(
					requirement, parentName);

			currentProject = findProjectDto(projectList, requirement, projectId);
			if (milestone!=null){
				currentProject.setMilestone(milestone);
			}

			// add the requirementDtos
			for (ReqCoverageByTestRequirementSingleDto requirementSingleDto : requirementSingleDtos) {
				currentProject.addRequirement(requirementSingleDto);
				// update statistics and the project in the Map
				updateProjectStatistics(currentProject, requirementSingleDto);
			}

		}
		return projectList;
	}

	private String extractMilestoneLabel(Requirement req){

		// stuff for
		String milestone = null;

		ReportCriterion milestoneCrit = criterions.get(MILESTONE_IDS);
		if (milestoneCrit != null){
			Object[] ids = milestoneCrit.getParameters();
			if (ids != null && ids.length > 0){
				Long milestoneId = Long.valueOf(ids[0].toString());

				Project p = req.getProject();
				for (Milestone m : p.getMilestones()){
					if (m.getId().equals(milestoneId)){
						milestone = m.getLabel();
					}
				}
			}
		}

		return milestone;

	}

	/**
	 * check if the project is not here and create if necessary
	 *
	 * @param projectList
	 * @param requirement
	 * @param projectId
	 * @return
	 */
	private ReqCoverageByTestProjectDto findProjectDto(Map<Long, ReqCoverageByTestProjectDto> projectList,
			Requirement requirement, Long projectId) {
		ReqCoverageByTestProjectDto currentProject;
		if (!projectList.containsKey(projectId)) {
			currentProject = createProjectDto(requirement);
			// add to the Map
			projectList.put(projectId, currentProject);
		} else {
			// ... or find it
			currentProject = projectList.get(projectId);
		}
		return currentProject;
	}

	private ReqCoverageByTestProjectDto createProjectDto(Requirement requirement) {
		String projectName = requirement.getProject().getName();
		return createProjectDto(projectName);
	}

	private ReqCoverageByTestProjectDto createProjectDto(String projectName) {
		ReqCoverageByTestProjectDto currentProject;
		// Create the projectDto...
		currentProject = new ReqCoverageByTestProjectDto();
		currentProject.setProjectName(projectName);
		return currentProject;
	}

	protected List<Object[]> filterUnwantedDataOut(List<Object[]> list) {
		List<Object[]> toReturn = new LinkedList<>();

		for (Object[] array : list) {
			Requirement requirement = (Requirement) array[0];
			if (getDataFilteringService().isFullyAllowed(requirement)) {
				toReturn.add(array);
			}
		}

		return toReturn;
	}

	/***
	 * This method create a new ReqCoverageByTestRequirementSingleDto with informations from requirement and the name of
	 * the requirement folder if it exists
	 *
	 * @param requirement
	 *            the requirement
	 * @param folder
	 *            the requirementFolder
	 * @return a ReqCoverageByTestRequirementSingleDto
	 */
	private List<ReqCoverageByTestRequirementSingleDto> createRequirementSingleDtos(Requirement requirement,
			String parentName) {
		Object mode = this.criterions.get("mode").getParameters()[0];
		int reqMode = Integer.parseInt((String) mode);
		List<ReqCoverageByTestRequirementSingleDto> reqCovByTestReqSingleDtos = new ArrayList<>();
		switch (reqMode) {
		case REPORT_VERSION_BOUND_TO_MILESTONE :
			LOGGER.debug("creation of reqCovByTestReqSingleDtos for Report mode 0 : only versions bound to the specified milestones are taken into account");
			createSingleDtoReportMilestoneVersion(requirement, parentName, reqCovByTestReqSingleDtos);
			break;
		case REPORT_EACH_VERSION:
			LOGGER.debug("creation of reqCovByTestReqSingleDtos for Report mode 1 : all versions of requirement taken into account");
			createSingleDtoReportEachVersion(requirement, parentName, reqCovByTestReqSingleDtos);
			break;
		case REPORT_LAST_VERSION:
			LOGGER.debug("creation of reqCovByTestReqSingleDtos for Report mode 2 : only last version of requirement taken into account");
			createSingleDtoReportLastVersion(requirement, parentName, reqCovByTestReqSingleDtos);
			break;
		default:
			LOGGER.warn("mode selection problem : default value");
			LOGGER.debug("creation of reqCovByTestReqSingleDtos for Report default mode : all versions of requirement taken into account");
			createSingleDtoReportEachVersion(requirement, parentName, reqCovByTestReqSingleDtos);
			break;
		}

		return reqCovByTestReqSingleDtos;
	}

	private void createSingleDtoReportMilestoneVersion(Requirement requirement, String parentName,
			List<ReqCoverageByTestRequirementSingleDto> reqCovByTestReqSingleDtos) {


		Object[] oIds =  getCriterions().get(MILESTONE_IDS).getParameters();
		Collection<Long> milestoneIds = new ArrayList<>(oIds.length);
		for (Object o : oIds){
			milestoneIds.add(Long.valueOf(o.toString()));

		}

		List<RequirementVersion> requirementVersions = requirement.getRequirementVersions();
		for (RequirementVersion version : requirementVersions){
			for(Milestone m : version.getMilestones()){
				if (milestoneIds.contains(m.getId())){
					ReqCoverageByTestRequirementSingleDto requirementSingleDto = createRequirementSingleDto(version, parentName,
							requirement);
					reqCovByTestReqSingleDtos.add(requirementSingleDto);
				}
			}
		}

	}

	private void createSingleDtoReportEachVersion(Requirement requirement, String parentName,
			List<ReqCoverageByTestRequirementSingleDto> reqCovByTestReqSingleDtos) {
		List<RequirementVersion> requirementVersions = requirement.getRequirementVersions();
		for (RequirementVersion version : requirementVersions) {
			ReqCoverageByTestRequirementSingleDto requirementSingleDto = createRequirementSingleDto(version, parentName,
					requirement);
			reqCovByTestReqSingleDtos.add(requirementSingleDto);
		}
	}

	private ReqCoverageByTestRequirementSingleDto createRequirementSingleDto(RequirementVersion version,
			String parentName, Requirement requirement) {
		ReqCoverageByTestRequirementSingleDto requirementSingleDto = new ReqCoverageByTestRequirementSingleDto();
		requirementSingleDto.setLabel(requirement.getName());
		requirementSingleDto.setReference(requirement.getReference());
		requirementSingleDto.setCriticality(version.getCriticality());
		requirementSingleDto.setStatus(version.getStatus());
		requirementSingleDto.setVersionNumber(version.getVersionNumber());
		int verifyingTestCases = version.getVerifyingTestCases().size();
		requirementSingleDto.setAssociatedTestCaseNumber(verifyingTestCases);
		if (parentName != null) {
			requirementSingleDto.setParent(parentName);
		}

		return requirementSingleDto;
	}

	private void createSingleDtoReportLastVersion(Requirement requirement, String parentName,
			List<ReqCoverageByTestRequirementSingleDto> reqCovByTestReqSingleDtos) {
		RequirementVersion lastVersion = requirement.getCurrentVersion();
		ReqCoverageByTestRequirementSingleDto requirementSingleDto = createRequirementSingleDto(lastVersion, parentName,
				requirement);
		reqCovByTestReqSingleDtos.add(requirementSingleDto);
	}

	/***
	 * This method update the projectDto statistics
	 *
	 * @param project
	 * @param requirementSingleDto
	 */
	private void updateProjectStatistics(ReqCoverageByTestProjectDto project,
			ReqCoverageByTestRequirementSingleDto requirementSingleDto) {
		project.incrementReqNumber(ReqCoverageByTestStatType.TOTAL);
		project.incrementReqStatusNumber(requirementSingleDto.getStatus().toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
		// if verified by testCase
		boolean isVerifiedByTestCase = false;
		if (requirementSingleDto.hasAssociatedTestCases()) {
			isVerifiedByTestCase = true;
			project.incrementReqNumber(ReqCoverageByTestStatType.TOTAL_VERIFIED);
			project.incrementReqStatusNumber(requirementSingleDto.getStatus().toString()
					+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
		}
		project.incrementReqNumber(requirementSingleDto.convertCrit());
		project.incrementReqStatusNumber(requirementSingleDto.getStatus().toString()
				+ requirementSingleDto.convertCrit().toString());
		if (isVerifiedByTestCase) {
			project.incrementReqNumber(requirementSingleDto.convertCritVerif());
			project.incrementReqStatusNumber(requirementSingleDto.getStatus().toString()
					+ requirementSingleDto.convertCritVerif().toString());
		}

	}

	/***
	 * Method which sets all project's rates
	 *
	 * @param givenProject
	 *            the project to modify
	 */
	private void calculateProjectCoverageRates(ReqCoverageByTestProjectDto givenProject) {
		calculateProjectCoverageRatesAllStatus(givenProject);
		calculateProjectCoverageRatesWorkInProgress(givenProject);
		calculateProjectCoverageRateUnderReview(givenProject);
		calculateProjectCoverageRateApproved(givenProject);
		calculateProjectCoverageRateObsolete(givenProject);

	}

	private void calculateProjectCoverageRateObsolete(ReqCoverageByTestProjectDto givenProject) {
		// Global rate
		givenProject.setObsoleteGlobalRequirementCoverage(calculateAndRoundRate(
				givenProject.getObsoleteTotalVerifiedRequirementNumber(),
				givenProject.getObsoleteTotalRequirementNumber()));
		// Critical rate
		givenProject.setObsoleteCriticalRequirementCoverage(calculateAndRoundRate(
				givenProject.getObsoleteCriticalVerifiedRequirementNumber(),
				givenProject.getObsoleteCriticalRequirementNumber()));
		// Major rate
		givenProject.setObsoleteMajorRequirementCoverage(calculateAndRoundRate(
				givenProject.getObsoleteMajorVerifiedRequirementNumber(),
				givenProject.getObsoleteMajorRequirementNumber()));
		// Minor rate
		givenProject.setObsoleteMinorRequirementCoverage(calculateAndRoundRate(
				givenProject.getObsoleteMinorVerifiedRequirementNumber(),
				givenProject.getObsoleteMinorRequirementNumber()));
		// Undefined rate
		givenProject.setObsoleteUndefinedRequirementCoverage(calculateAndRoundRate(
				givenProject.getObsoleteUndefinedVerifiedRequirementNumber(),
				givenProject.getObsoleteUndefinedRequirementNumber()));

	}

	private void calculateProjectCoverageRateApproved(ReqCoverageByTestProjectDto givenProject) {
		// Global rate
		givenProject.setApprovedGlobalRequirementCoverage(calculateAndRoundRate(
				givenProject.getApprovedTotalVerifiedRequirementNumber(),
				givenProject.getApprovedTotalRequirementNumber()));
		// Critical rate
		givenProject.setApprovedCriticalRequirementCoverage(calculateAndRoundRate(
				givenProject.getApprovedCriticalVerifiedRequirementNumber(),
				givenProject.getApprovedCriticalRequirementNumber()));
		// Major rate
		givenProject.setApprovedMajorRequirementCoverage(calculateAndRoundRate(
				givenProject.getApprovedMajorVerifiedRequirementNumber(),
				givenProject.getApprovedMajorRequirementNumber()));
		// Minor rate
		givenProject.setApprovedMinorRequirementCoverage(calculateAndRoundRate(
				givenProject.getApprovedMinorVerifiedRequirementNumber(),
				givenProject.getApprovedMinorRequirementNumber()));
		// Undefined rate
		givenProject.setApprovedUndefinedRequirementCoverage(calculateAndRoundRate(
				givenProject.getApprovedUndefinedVerifiedRequirementNumber(),
				givenProject.getApprovedUndefinedRequirementNumber()));

	}

	private void calculateProjectCoverageRateUnderReview(ReqCoverageByTestProjectDto givenProject) {
		// Global rate
		givenProject.setUnderReviewGlobalRequirementCoverage(calculateAndRoundRate(
				givenProject.getUnderReviewTotalVerifiedRequirementNumber(),
				givenProject.getUnderReviewTotalRequirementNumber()));
		// Critical rate
		givenProject.setUnderReviewCriticalRequirementCoverage(calculateAndRoundRate(
				givenProject.getUnderReviewCriticalVerifiedRequirementNumber(),
				givenProject.getUnderReviewCriticalRequirementNumber()));
		// Major rate
		givenProject.setUnderReviewMajorRequirementCoverage(calculateAndRoundRate(
				givenProject.getUnderReviewMajorVerifiedRequirementNumber(),
				givenProject.getUnderReviewMajorRequirementNumber()));
		// Minor rate
		givenProject.setUnderReviewMinorRequirementCoverage(calculateAndRoundRate(
				givenProject.getUnderReviewMinorVerifiedRequirementNumber(),
				givenProject.getUnderReviewMinorRequirementNumber()));
		// Undefined rate
		givenProject.setUnderReviewUndefinedRequirementCoverage(calculateAndRoundRate(
				givenProject.getUnderReviewUndefinedVerifiedRequirementNumber(),
				givenProject.getUnderReviewUndefinedRequirementNumber()));


	}

	private void calculateProjectCoverageRatesWorkInProgress(ReqCoverageByTestProjectDto givenProject) {
		// Global rate
		givenProject.setWorkInProgressGlobalRequirementCoverage(calculateAndRoundRate(
				givenProject.getWorkInProgressTotalVerifiedRequirementNumber(),
				givenProject.getWorkInProgressTotalRequirementNumber()));
		// Critical rate
		givenProject.setWorkInProgressCriticalRequirementCoverage(calculateAndRoundRate(
				givenProject.getWorkInProgressCriticalVerifiedRequirementNumber(),
				givenProject.getWorkInProgressCriticalRequirementNumber()));
		// Major rate
		givenProject.setWorkInProgressMajorRequirementCoverage(calculateAndRoundRate(
				givenProject.getWorkInProgressMajorVerifiedRequirementNumber(),
				givenProject.getWorkInProgressMajorRequirementNumber()));
		// Minor rate
		givenProject.setWorkInProgressMinorRequirementCoverage(calculateAndRoundRate(
				givenProject.getWorkInProgressMinorVerifiedRequirementNumber(),
				givenProject.getWorkInProgressMinorRequirementNumber()));
		// Undefined rate
		givenProject.setWorkInProgressUndefinedRequirementCoverage(calculateAndRoundRate(
				givenProject.getWorkInProgressUndefinedVerifiedRequirementNumber(),
				givenProject.getWorkInProgressUndefinedRequirementNumber()));

	}

	private void calculateProjectCoverageRatesAllStatus(ReqCoverageByTestProjectDto givenProject) {
		// Global rate
		givenProject.setGlobalRequirementCoverage(calculateAndRoundRate(
				givenProject.getTotalVerifiedRequirementNumber(), givenProject.getTotalRequirementNumber()));
		// Critical rate
		givenProject.setCriticalRequirementCoverage(calculateAndRoundRate(
				givenProject.getCriticalVerifiedRequirementNumber(), givenProject.getCriticalRequirementNumber()));
		// Major rate
		givenProject.setMajorRequirementCoverage(calculateAndRoundRate(
				givenProject.getMajorVerifiedRequirementNumber(), givenProject.getMajorRequirementNumber()));
		// Minor rate
		givenProject.setMinorRequirementCoverage(calculateAndRoundRate(
				givenProject.getMinorVerifiedRequirementNumber(), givenProject.getMinorRequirementNumber()));
		// Undefined rate
		givenProject.setUndefinedRequirementCoverage(calculateAndRoundRate(
				givenProject.getUndefinedVerifiedRequirementNumber(), givenProject.getUndefinedRequirementNumber()));
	}

	/***
	 * This method returns the rate calculated from the given values
	 *
	 * @param verifiedNumber
	 *            the number of verified requirements
	 * @param totalNumber
	 *            the total number of requirement
	 * @return the rate (byte)
	 */
	private byte calculateAndRoundRate(Long verifiedNumber, Long totalNumber) {
		Double result = DEFAULT_RATE_VALUE;
		if (totalNumber > 0) {
			result = (double) verifiedNumber * 100 / (double) totalNumber;
		}
		// round
		result = Math.floor(result + 0.5);
		return result.byteValue();
	}

}

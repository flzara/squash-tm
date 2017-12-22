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
package org.squashtest.tm.web.internal.controller.campaign;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignStatus;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.CampaignModificationService;
import org.squashtest.tm.service.campaign.CampaignTestPlanManagerService;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneFeatureConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestonePanelConfiguration;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneTableModelHelper;
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseImportanceJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseModeJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.controller.testcase.executions.ExecutionStatusJeditableComboDataBuilder;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;
import org.squashtest.tm.web.internal.model.json.JsonIteration;

@Controller
@RequestMapping("/campaigns/{campaignId}")
public class CampaignModificationController {

	private static final String CAMPAIGN_ID = "campaignId";

	private static final String LOG_MSG_SET_CPG_SART_DATE = "Setting scheduled start date for campaign ";

	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignModificationController.class);

	private static final String PLANNING_URL = "/planning";
	private static final String NEW_DATE_ = ", new date : ";

	@Inject
	private CampaignModificationService campaignModService;

	@Inject
	private IterationModificationService iterationModService;

	@Inject
	private CustomFieldValueFinderService cufValueService;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentHelper;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseModeJeditableComboDataBuilder> modeComboBuilderProvider;

	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatterProvider;

	@Inject
	private Provider<CampaignStatusJeditableComboDataBuilder> statusComboBuilderProvider;

	@Inject
	private CampaignTestPlanManagerService testPlanManager;

	@Inject
	private MilestoneUIConfigurationService milestoneConfService;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private CustomReportDashboardService customReportDashboardService;

	@Inject
	private Provider<ExecutionStatusJeditableComboDataBuilder> executionStatusComboBuilderProvider;

	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public ModelAndView refreshStats(@PathVariable long campaignId) {

		TestPlanStatistics campaignStatistics = campaignModService.findCampaignStatistics(campaignId);
		Campaign campaign = campaignModService.findById(campaignId);
		ModelAndView mav = new ModelAndView("fragment/generics/statistics-fragment");
		mav.addObject("allowsSettled",
			campaign.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		mav.addObject("allowsUntestable",
			campaign.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
		mav.addObject("statisticsEntity", campaignStatistics);

		return mav;
	}

	// will return the Campaign in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String showCampaignInfo(@PathVariable long campaignId,
								   Model model) {
		populateCampaignModel(campaignId, model);
		return "page/campaign-workspace/show-campaign";
	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public String showCampaign(@PathVariable long campaignId, Model model) {
		populateCampaignModel(campaignId, model);
		return "fragment/campaigns/campaign";
	}

	private Model populateCampaignModel(long campaignId, Model model) {

		Campaign campaign = campaignModService.findById(campaignId);
		TestPlanStatistics statistics = campaignModService.findCampaignStatistics(campaignId);
		boolean hasCUF = cufValueService.hasCustomFields(campaign);
		DataTableModel attachments = attachmentHelper.findPagedAttachments(campaign);

		model.addAttribute("campaign", campaign);
		model.addAttribute("statistics", statistics);
		model.addAttribute("hasCUF", hasCUF);
		model.addAttribute("attachmentsModel", attachments);
		model.addAttribute("assignableUsers", getAssignableUsers(campaignId));
		model.addAttribute("weights", getWeights());
		model.addAttribute("modes", getModes());
		model.addAttribute("campaignStatusComboJson", buildStatusComboData());
		model.addAttribute("campaignStatusLabel", formatStatus(campaign.getStatus()));
		model.addAttribute("statuses", getStatuses(campaign.getProject().getId()));

		MilestoneFeatureConfiguration milestoneConf = milestoneConfService.configure(campaign);
		model.addAttribute("milestoneConf", milestoneConf);

		populateOptionalExecutionStatuses(campaign, model);

		boolean shouldShowDashboard = customReportDashboardService.shouldShowFavoriteDashboardInWorkspace(Workspace.CAMPAIGN);
		boolean canShowDashboard = customReportDashboardService.canShowDashboardInWorkspace(Workspace.CAMPAIGN);

		model.addAttribute("shouldShowDashboard", shouldShowDashboard);
		model.addAttribute("canShowDashboard", canShowDashboard);

		return model;
	}

	private void populateOptionalExecutionStatuses(Campaign campaign, Model model) {
		model.addAttribute("allowsSettled",
			campaign.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
		model.addAttribute("allowsUntestable",
			campaign.getProject().getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
	}

	private Map<String, String> getAssignableUsers(long campaignId) {

		Locale locale = LocaleContextHolder.getLocale();

		String unassignedLabel = messageSource.internationalize("label.Unassigned", locale);

		// Looking for users depending on the campaign id
		List<User> usersList = testPlanManager.findAssignableUserForTestPlan(campaignId);
		Collections.sort(usersList, new UserLoginComparator());

		Map<String, String> jsonUsers = new LinkedHashMap<>(usersList.size());
		jsonUsers.put(User.NO_USER_ID.toString(), unassignedLabel);
		for (User user : usersList) {
			jsonUsers.put(user.getId().toString(), user.getLogin());
		}

		return jsonUsers;
	}

	private Map<String, String> getStatuses(long projectId) {
		Locale locale = LocaleContextHolder.getLocale();
		return executionStatusComboBuilderProvider.get().useContext(projectId).useLocale(locale).buildMap();
	}

	private Map<String, String> getWeights() {
		Locale locale = LocaleContextHolder.getLocale();
		return importanceComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	private Map<String, String> getModes() {
		Locale locale = LocaleContextHolder.getLocale();
		return modeComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	private String formatStatus(CampaignStatus status) {
		Locale locale = LocaleContextHolder.getLocale();
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(status);
	}

	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	private String buildStatusComboData() {
		Locale locale = LocaleContextHolder.getLocale();
		return statusComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"id=campaign-description", VALUE})
	public String updateDescription(@RequestParam(VALUE) String newDescription, @PathVariable long campaignId) {

		campaignModService.changeDescription(campaignId, newDescription);
		LOGGER.trace("Campaign " + campaignId + ": updated description to " + newDescription);
		return newDescription;
	}


	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"id=campaign-reference", VALUE})
	public String updateReference(@RequestParam(VALUE) String newReference, @PathVariable long campaignId) {

		campaignModService.changeReference(campaignId, newReference);
		LOGGER.trace("Campaign " + campaignId + ": updated reference to " + newReference);
		return HtmlUtils.htmlEscape(newReference);
	}


	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"id=campaign-status", VALUE})
	public String changeStatus(@PathVariable long campaignId, @RequestParam(VALUE) CampaignStatus status) {
		campaignModService.changeStatus(campaignId, status);
		return formatStatus(status);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"newName"})
	public Object rename(@RequestParam("newName") String newName, @PathVariable long campaignId) {
		LOGGER.info("Renaming Campaign " + campaignId + " as " + newName);

		campaignModService.rename(campaignId, newName);
		return new RenameModel(newName);

	}

	@RequestMapping(value = "/general", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable long campaignId) {
		Campaign campaign = campaignModService.findById(campaignId);
		return new JsonGeneralInfo((AuditableMixin) campaign);

	}

	/*
	 * *************************************** planning *********************************
	 */

	// returns null if the string is empty, or a date otherwise. No check regarding the actual content of strDate.
	private Date strToDate(String strDate) {
		return DateUtils.millisecondsToDate(strDate);
	}

	private String dateToStr(Date date) {
		return DateUtils.dateToMillisecondsAsString(date);
	}

	@ResponseBody
	@RequestMapping(value = PLANNING_URL, params = {"scheduledStart"})
	public String setScheduledStart(@PathVariable long campaignId,
									@RequestParam(value = "scheduledStart") String strDate) {

		Date newScheduledStart = strToDate(strDate);
		String toReturn = dateToStr(newScheduledStart);

		LOGGER.info(LOG_MSG_SET_CPG_SART_DATE + campaignId + NEW_DATE_ + newScheduledStart);

		campaignModService.changeScheduledStartDate(campaignId, newScheduledStart);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = {"scheduledEnd"})
	@ResponseBody
	String setScheduledEnd(@PathVariable long campaignId,
						   @RequestParam(value = "scheduledEnd") String strDate) {

		Date newScheduledEnd = strToDate(strDate);
		String toReturn = dateToStr(newScheduledEnd);

		LOGGER.info(LOG_MSG_SET_CPG_SART_DATE + campaignId + NEW_DATE_ + newScheduledEnd);

		campaignModService.changeScheduledEndDate(campaignId, newScheduledEnd);

		return toReturn;

	}

	/**
	 * the next functions may receive null arguments : empty string
	 **/

	@RequestMapping(value = PLANNING_URL, params = {"actualStart"})
	@ResponseBody
	String setActualStart(@PathVariable long campaignId,
						  @RequestParam(value = "actualStart") String strDate) {

		Date newActualStart = strToDate(strDate);
		String toReturn = dateToStr(newActualStart);

		LOGGER.info(LOG_MSG_SET_CPG_SART_DATE + campaignId + NEW_DATE_ + newActualStart);

		campaignModService.changeActualStartDate(campaignId, newActualStart);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = {"actualEnd"})
	@ResponseBody
	String setActualEnd(@PathVariable long campaignId,
						@RequestParam(value = "actualEnd") String strDate) {

		Date newActualEnd = strToDate(strDate);
		String toReturn = dateToStr(newActualEnd);

		LOGGER.info(LOG_MSG_SET_CPG_SART_DATE + campaignId + NEW_DATE_ + newActualEnd);

		campaignModService.changeActualEndDate(campaignId, newActualEnd);

		return toReturn;

	}

	@RequestMapping(value = PLANNING_URL, params = {"setActualStartAuto"})
	@ResponseBody
	String setActualStartAuto(@PathVariable long campaignId,
							  @RequestParam(value = "setActualStartAuto") Boolean auto) {

		LOGGER.info("Autosetting actual start date for campaign " + campaignId + ", new value " + auto.toString());

		campaignModService.changeActualStartAuto(campaignId, auto);
		Campaign campaign = campaignModService.findById(campaignId);

		return dateToStr(campaign.getActualStartDate());
	}

	@RequestMapping(value = PLANNING_URL, params = {"setActualEndAuto"})
	@ResponseBody
	String setActualEndAuto(@PathVariable long campaignId,
							@RequestParam(value = "setActualEndAuto") Boolean auto) {
		LOGGER.info("CampaignModificationController : autosetting actual end date for campaign " + campaignId
			+ ", new value " + auto.toString());

		campaignModService.changeActualEndAuto(campaignId, auto);
		Campaign campaign = campaignModService.findById(campaignId);

		return dateToStr(campaign.getActualEndDate());

	}


	@RequestMapping(value = "/iterations", produces = ContentTypes.APPLICATION_JSON, method = RequestMethod.GET)
	@ResponseBody
	public List<JsonIteration> getIterations(@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId) {
		List<Iteration> iterations = campaignModService.findIterationsByCampaignId(campaignId);
		return createJsonIterations(iterations);
	}


	@RequestMapping(value = "/iterations/count", produces = ContentTypes.APPLICATION_JSON, method = RequestMethod.GET)
	@ResponseBody
	public Integer getNbIterations(@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId) {
		return campaignModService.countIterations(campaignId);
	}


	// for now, handles the scheduled dates only
	@RequestMapping(value = "/iterations/planning", consumes = ContentTypes.APPLICATION_JSON, method = RequestMethod.POST)
	@ResponseBody
	public void setIterationsPlanning(@RequestBody JsonIteration[] iterations) throws ParseException {
		Date date;
		for (JsonIteration iter : iterations) {
			date = iter.getScheduledStartDate() != null ? DateUtils.parseIso8601DateTime(iter
				.getScheduledStartDate()) : null;
			iterationModService.changeScheduledStartDate(iter.getId(), date);
			date = iter.getScheduledEndDate() != null ? DateUtils.parseIso8601DateTime(iter.getScheduledEndDate())
				: null;
			iterationModService.changeScheduledEndDate(iter.getId(), date);
		}
	}

	// *************************** statistics ********************************

	// URL should have been /statistics, but that was already used by another method in this controller
	@ResponseBody
	@RequestMapping(value = "/dashboard-statistics", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public CampaignStatisticsBundle getStatisticsAsJson(@PathVariable(RequestParams.CAMPAIGN_ID) long campaignId) {
		return campaignModService.gatherCampaignStatisticsBundle(campaignId);
	}

	@RequestMapping(value = "/dashboard", method = RequestMethod.GET, produces = ContentTypes.TEXT_HTML, params = "printmode")
	public ModelAndView getDashboard(Model model,
									 @PathVariable(RequestParams.CAMPAIGN_ID) long campaignId,
									 @RequestParam(value = "printmode", defaultValue = "false") Boolean printmode) {

		Campaign campaign = campaignModService.findById(campaignId);
		CampaignStatisticsBundle bundle = campaignModService.gatherCampaignStatisticsBundle(campaignId);

		ModelAndView mav = new ModelAndView("page/campaign-workspace/show-campaign-dashboard");
		mav.addObject("campaign", campaign);
		mav.addObject("dashboardModel", bundle);
		mav.addObject("printmode", printmode);

		populateOptionalExecutionStatuses(campaign, model);

		return mav;

	}



	/* **********************************************************************
	 *
	 * Milestones section
	 *
	 ********************************************************************** */

	@RequestMapping(value = "/milestones", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getBoundMilestones(@PathVariable(CAMPAIGN_ID) long campaignId, DataTableDrawParameters params) {

		Collection<Milestone> allMilestones = campaignModService.findAllMilestones(campaignId);

		return buildMilestoneModel(new ArrayList<>(allMilestones), params.getsEcho());
	}

	@RequestMapping(value = "/milestones/{milestoneId}", method = RequestMethod.POST)
	@ResponseBody
	public void bindMilestone(@PathVariable(CAMPAIGN_ID) long campaignId, @PathVariable("milestoneId") Long milestoneId) {

		campaignModService.bindMilestone(campaignId, milestoneId);
	}

	@RequestMapping(value = "/milestones/{milestoneIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindMilestones(@PathVariable(CAMPAIGN_ID) long campaignId, @PathVariable("milestoneIds") List<Long> milestoneIds) {

		campaignModService.unbindMilestones(campaignId, milestoneIds);
	}

	@RequestMapping(value = "/milestones/associables", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getNotYetBoundMilestones(@PathVariable(CAMPAIGN_ID) Long campaignId, DataTableDrawParameters params) {
		Collection<Milestone> notBoundMilestones = campaignModService.findAssociableMilestones(campaignId);
		return buildMilestoneModel(new ArrayList<>(notBoundMilestones), params.getsEcho());
	}


	@RequestMapping(value = "/milestones/panel", method = RequestMethod.GET)
	public String getMilestonesPanel(@PathVariable(CAMPAIGN_ID) Long campaignId, Model model) {

		MilestonePanelConfiguration conf = new MilestonePanelConfiguration();

		Campaign camp = campaignModService.findById(campaignId);
		// build the needed data
		Collection<Milestone> allMilestones = campaignModService.findAllMilestones(campaignId);
		List<?> currentModel = buildMilestoneModel(new ArrayList<>(allMilestones), "0").getAaData();

		Map<String, String> identity = new HashMap<>();
		identity.put("restype", "campaigns");
		identity.put("resid", campaignId.toString());

		String rootPath = "/campaigns/" + campaignId.toString();

		Boolean editable = permissionService.hasRole("ROLE_ADMIN") || permissionService.hasRole("ROLE_TM_PROJECT_MANAGER");

		List<Milestone> mil = camp.getProject().getMilestones();
		CollectionUtils.filter(mil, new Predicate() {
			@Override
			public boolean evaluate(Object milestone) {
				return ((Milestone) milestone).getStatus().isBindableToObject();
			}
		});
		Boolean isMilestoneInProject = !mil.isEmpty();


		// add them to the model
		conf.setNodeType("campaign");
		conf.setRootPath(rootPath);
		conf.setIdentity(identity);
		conf.setCurrentModel(currentModel);
		conf.setEditable(editable);
		conf.setMultilines(false);
		conf.setIsMilestoneInProject(isMilestoneInProject);

		model.addAttribute("conf", conf);

		return "milestones/milestones-tab.html";

	}

	private DataTableModel buildMilestoneModel(List<Milestone> milestones, String sEcho) {


		PagedCollectionHolder<List<Milestone>> collectionHolder =
			new SinglePageCollectionHolder<>(milestones);

		Locale locale = LocaleContextHolder.getLocale();
		return new MilestoneTableModelHelper(messageSource, locale).buildDataModel(collectionHolder, sEcho);

	}
	// **************************** private stuffs ***************************

	private List<JsonIteration> createJsonIterations(List<Iteration> iterations) {
		List<JsonIteration> jsonIters = new ArrayList<>(iterations.size());
		for (Iteration iter : iterations) {

			JsonIteration jsonIter = new JsonIteration(iter.getId(), iter.getName(), iter.getScheduledStartDate(),
				iter.getScheduledEndDate());

			jsonIters.add(jsonIter);
		}
		return jsonIters;
	}

	private static final class UserLoginComparator implements Comparator<User>, Serializable {
		@Override
		public int compare(User u1, User u2) {
			return u1.getLogin().compareTo(u2.getLogin());
		}

	}
}

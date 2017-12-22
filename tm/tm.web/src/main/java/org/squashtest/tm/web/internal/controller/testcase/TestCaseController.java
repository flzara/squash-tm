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
package org.squashtest.tm.web.internal.controller.testcase;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.requirement.VerifiedRequirementsFinderService;
import org.squashtest.tm.service.testcase.TestCaseFinder;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.json.JsonTestCase;
import org.squashtest.tm.web.internal.model.json.JsonTestCaseBuilder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Gregory Fouquet, mpagnon
 *
 */
@RequestMapping("/test-cases")
@Controller
public class TestCaseController {
	/**
	 * ids post param
	 */
	private static final String IDS = RequestParams.IDS;

	/**
	 * folder ids post param
	 */
	private static final String FOLDER_IDS = RequestParams.FOLDER_IDS;

	@Inject
	private Provider<JsonTestCaseBuilder> builder;

	@Inject
	private TestCaseFinder finder;

	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseStatusJeditableComboDataBuilder> statusComboBuilderProvider;


	@Inject
	private VerifiedRequirementsFinderService verifiedRequirementsFinderService;

	/**
	 * Fetches and returns a list of json test cases from their ids
	 *
	 * @param testCaseIds
	 *            non null list of test cases ids.
	 * @return
	 *
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, params = IDS, headers = AcceptHeaders.CONTENT_JSON)
	public
	List<JsonTestCase> getJsonTestCases(@RequestParam(IDS) List<Long> testCaseIds, Locale locale) {
		List<TestCase> testCases = finder.findAllByIds(testCaseIds);
		return builder.get().locale(locale).entities(testCases).toJson();
	}

	/**
	 * Fetches and returns a list of json test cases from their containers
	 *
	 * @param foldersIds
	 *            non null list of folders ids.
	 * @return
	 *
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, params = FOLDER_IDS, headers = AcceptHeaders.CONTENT_JSON)
	public
	List<JsonTestCase> getJsonTestCasesFromFolders(@RequestParam(FOLDER_IDS) List<Long> folderIds, Locale locale) {
		return buildJsonTestCasesFromAncestorIds(folderIds, locale);
	}

	private List<JsonTestCase> buildJsonTestCasesFromAncestorIds(List<Long> folderIds, Locale locale) {
		List<TestCase> testCases = finder.findAllByAncestorIds(folderIds);
		return builder.get().locale(locale).entities(testCases).toJson();
	}

	/**
	 * Fetches and returns a list of json test cases from their ids and containers
	 *
	 * @param testCaseIds
	 * @param folderIds
	 * @param locale
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, params = {IDS, FOLDER_IDS}, headers = AcceptHeaders.CONTENT_JSON)
	public
	List<JsonTestCase> getJsonTestCases(@RequestParam(IDS) List<Long> testCaseIds,
										@RequestParam(FOLDER_IDS) List<Long> folderIds, Locale locale) {
		List<Long> consolidatedIds = new ArrayList<>(testCaseIds.size() + folderIds.size());
		consolidatedIds.addAll(testCaseIds);
		consolidatedIds.addAll(folderIds);

		return buildJsonTestCasesFromAncestorIds(consolidatedIds, locale);
	}

	/**
	 * @see ...\scripts\workspace\workspace.tree-event-handler.js Request when a tree node has it's requirement property
	 *      updated, the importance and requirement property of the calling test cases must be updated to.
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "/tree-infos", method = RequestMethod.POST)
	@SuppressWarnings("unchecked")
	public
	List<TestCaseTreeIconsUpdate> getTestCaseTreeInfosToUpdate(@RequestBody Map<String, Object> form) {
		// get form content
		Map<String, String> updatedIdsAndOldReqString = (Map<String, String>) form.get("updatedIdsAndOldReq");
		Map<Long, Boolean> updatedIdsAndOldReq = transformToLongBooleanMap(updatedIdsAndOldReqString);
		Set<Long> updatedIds = updatedIdsAndOldReq.keySet();

		List<String> openedNodesIdsString = (ArrayList<String>) form.get("openedNodesIds");
		Set<Long> openedNodesIds = transformToLongSet(openedNodesIdsString);

		// remove updated nodes from opened Nodes ids. This list is used to identify the calling test cases that need to
		// be updated. The updated ids will be checked anway so we don't want to do the job twice
		openedNodesIds.removeAll(updatedIds);

		// distinguish nodes with 'isReqCovered' to update and pre-fill newIsReqCoveredById map
		Map<Long, Boolean> newIsReqCoveredById = findNodesWithReqCoverageThatChanged(updatedIdsAndOldReq);

		// find their calling test case and their new 'isReqCoveredProperty'
		Set<Long> newIsReqCoveredIdsAndCalling = new HashSet<>();

		for (Long idChange : new HashSet<>(newIsReqCoveredById.keySet())) {
			newIsReqCoveredIdsAndCalling.add(idChange);

			// in the meantime the calling nodes 'isReqCoveredProperty'
			Set<Long> callingOpenedNodesIds = finder.findCallingTCids(idChange, openedNodesIds);
			callingOpenedNodesIds.removeAll(newIsReqCoveredIdsAndCalling);
			newIsReqCoveredById.putAll(verifiedRequirementsFinderService
				.findisReqCoveredOfCallingTCWhenisReqCoveredChanged(idChange, callingOpenedNodesIds));


			newIsReqCoveredIdsAndCalling.addAll(callingOpenedNodesIds);
		}

		// deduce nodes with same 'isReqCovered'
		Set<Long> sameIsReqCoveredIds = new HashSet<>();
		sameIsReqCoveredIds.addAll(updatedIds);
		sameIsReqCoveredIds.removeAll(newIsReqCoveredById.keySet());

		// add their calling test cases ids
		Set<Long> sameIsReqCoveredIdsWCalling = addCallingNodesIds(openedNodesIds, sameIsReqCoveredIds);

		// get importances to update infos
		Set<Long> toUpdateImportanceId = new HashSet<>();
		toUpdateImportanceId.addAll(sameIsReqCoveredIdsWCalling);
		toUpdateImportanceId.addAll(newIsReqCoveredIdsAndCalling);
		Map<Long, TestCaseImportance> importancesToUpdate = finder.findImpTCWithImpAuto(toUpdateImportanceId);

		// merge
		return mergeImportanceAndReqCoverage(newIsReqCoveredById, importancesToUpdate);

	}

	private Set<Long> transformToLongSet(Collection<String> openedNodesIdsString) {
		Set<Long> openedNodesIds = new HashSet<>();
		for (String nodeId : openedNodesIdsString) {
			openedNodesIds.add(Long.parseLong(nodeId));
		}
		return openedNodesIds;
	}

	private Map<Long, Boolean> transformToLongBooleanMap(Map<String, String> map) {
		Map<Long, Boolean> result = new HashMap<>(map.size());
		for (Entry<String, String> entry : map.entrySet()) {
			result.put(Long.parseLong(entry.getKey()), Boolean.parseBoolean(entry.getValue()));
		}
		return result;
	}

	private Map<Long, Boolean> findNodesWithReqCoverageThatChanged(Map<Long, Boolean> updatedIdsAndOldReq) {
		Map<Long, Boolean> result = new HashMap<>();
		for (Entry<Long, Boolean> entry : updatedIdsAndOldReq.entrySet()) {
			long id = entry.getKey();
			boolean oldReqbool = updatedIdsAndOldReq.get(id);
			boolean newReq = verifiedRequirementsFinderService.testCaseHasDirectCoverage(id)
				|| verifiedRequirementsFinderService.testCaseHasUndirectRequirementCoverage(id);
			if (newReq != oldReqbool) {// then 'isReqCovered' changed
				result.put(id, newReq);
			}
		}
		return result;
	}

	private Set<Long> addCallingNodesIds(Collection<Long> openedNodesIds, Collection<Long> nodesIds) {
		Set<Long> idsToUpdate = new HashSet<>();
		for (Long id : nodesIds) {
			idsToUpdate.add(id);
			Set<Long> callingOpenedNodesIds = finder.findCallingTCids(id, openedNodesIds);
			idsToUpdate.addAll(callingOpenedNodesIds);
		}
		return idsToUpdate;
	}

	private List<TestCaseTreeIconsUpdate> mergeImportanceAndReqCoverage(Map<Long, Boolean> areReqCoveredToUpdate,
																		Map<Long, TestCaseImportance> importancesToUpdate) {
		List<TestCaseTreeIconsUpdate> result = new ArrayList<>();
		// go through importances to update and merge with matching reqCover to update
		for (Entry<Long, TestCaseImportance> importanceToUpdate : importancesToUpdate.entrySet()) {
			Long testCaseId = importanceToUpdate.getKey();
			TestCaseImportance imp = importanceToUpdate.getValue();
			Boolean isReqCovered = areReqCoveredToUpdate.get(testCaseId);
			if (isReqCovered != null) {
				result.add(new TestCaseTreeIconsUpdate(testCaseId, isReqCovered, imp));
				areReqCoveredToUpdate.remove(testCaseId);
			} else {
				result.add(new TestCaseTreeIconsUpdate(testCaseId, imp));
			}
		}
		// add remaining req to update
		for (Entry<Long, Boolean> isReqCoveredToUpdate : areReqCoveredToUpdate.entrySet()) {
			Long testCaseId = isReqCoveredToUpdate.getKey();
			Boolean isReqCovered = isReqCoveredToUpdate.getValue();
			result.add(new TestCaseTreeIconsUpdate(testCaseId, isReqCovered));
		}
		return result;
	}

	// TODO bind to /test-case-importances
	@RequestMapping(value = "/importance-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public Object buildImportanceComboData(Locale locale) {
		return importanceComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	// TODO bind to /test-case-statuses
	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public Object buildStatusComboData(Locale locale) {
		return statusComboBuilderProvider.get().useLocale(locale).buildMap();
	}

}

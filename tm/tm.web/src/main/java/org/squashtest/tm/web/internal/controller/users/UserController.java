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
package org.squashtest.tm.web.internal.controller.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.service.user.TeamFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * @author mpagnon
 * 
 */
@Controller
@RequestMapping("/users")
public class UserController {

	/**
	 * 
	 */
	private static final String USER_ID = "userId";

	@Inject
	private AdministrationService service;

	@Inject
	private TeamFinderService teamFinder;


	private static final String USER_ID_URL = "/{userId}";

	private DatatableMapper<String> teamsMapper = new NameBasedMapper(1).map("team-name", "name");

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@RequestMapping(value = USER_ID_URL + "/general", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable(USER_ID) long userId) {
		User user = service.findUserById(userId);
		return new JsonGeneralInfo((AuditableMixin) user);

	}

	// ************************************ team section ************************

	@RequestMapping(value = USER_ID_URL + "/teams", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTeamsTableModel(DataTableDrawParameters params, @PathVariable(USER_ID) long userId) {
		LOGGER.info("Find associated teams table model for user #{}", userId);
		PagingAndSorting paging = new DataTableSorting(params, teamsMapper);
		Filtering filtering = new DataTableFiltering(params);
		return getTeamsTableModel(userId, paging, filtering, params.getsEcho());
	}

	@RequestMapping(value = USER_ID_URL + "/teams/{teamIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deassociateTeams(@PathVariable(USER_ID) long userId, @PathVariable("teamIds") List<Long> teamIds) {
		LOGGER.info("Remove the user #{} from the given teams members.", userId);
		service.deassociateTeams(userId, teamIds);
	}

	@RequestMapping(value = USER_ID_URL + "/non-associated-teams", headers = "Accept=application/json")
	@ResponseBody
	public Map<String, Object> getNonAssociatedTeams(@PathVariable(USER_ID) long userId) {
		LOGGER.info("Find teams where user #{} is not a member.", userId);
		Map<String, Object> result = new HashMap<>();
		long teamTot = teamFinder.countAll();
		if (teamTot <= 0) {
			result.put("status", "no-team");
		} else {
			List<Team> nonAssociatedTeams = service.findAllNonAssociatedTeams(userId);
			if (nonAssociatedTeams.isEmpty()) {
				result.put("status", "no-more-teams");
			} else {
				result.put("status", "ok");
				result.put("teams", CollectionUtils.collect(nonAssociatedTeams, new TeamModelCreator()));
			}
		}
		return result;
	}

	@RequestMapping(value = USER_ID_URL + "/teams/{ids}", method = RequestMethod.PUT)
	@ResponseBody
	public void associateToTeams(@PathVariable(USER_ID) long userId, @PathVariable("ids") List<Long> teamIds) {
		LOGGER.info("Add user #{} to given teams members.", userId);
		service.associateToTeams(userId, teamIds);
	}

	private DataTableModel getTeamsTableModel(long userId, PagingAndSorting paging, Filtering filtering, String secho) {
		PagedCollectionHolder<List<Team>> holder = service.findSortedAssociatedTeams(userId, paging, filtering);
		return new TeamsTableModelHelper().buildDataModel(holder, secho);
	}

	private static final class TeamModelCreator implements Transformer {
		@Override
		public Object transform(Object team) {
			return new TeamModel((Team) team);
		}
	}

	private static final class TeamsTableModelHelper extends DataTableModelBuilder<Team> {
		private TeamsTableModelHelper() {
		}

		@Override
		protected Map<?, ?> buildItemData(Team item) {
			Map<String, Object> res = new HashMap<>();
			res.put("team-id", item.getId());
			res.put("team-index", getCurrentIndex());
			res.put("team-name", item.getName());
			res.put("empty-delete-holder", null);
			return res;
		}
	}
}

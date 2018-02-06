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
package org.squashtest.tm.web.internal.controller.administration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.squashtest.tm.service.user.ConnectionLogFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.*;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author aguilhem
 */
@Controller
@RequestMapping("/administration/connections")
public class ConnectionController {

	@Inject
	protected InternationalizationHelper messageSource;

	@Inject
	private ConnectionLogFinderService service;

	private DatatableMapper<String> connectionsMapper = new NameBasedMapper(9)
		.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, "id", ConnectionLog.class)
		.mapAttribute("login", "login", ConnectionLog.class)
		.mapAttribute("connection-date", "connectionDate", ConnectionLog.class)
		.mapAttribute("successful", "success", ConnectionLog.class);

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionController.class);

	/**
	 * Return the DataTableModel to display the table of all connections attempt.
	 *
	 * @param params
	 *            the {@link DataTableDrawParameters} for the connection logs table
	 * @return the {@link DataTableModel} with organized {@link ConnectionLog} infos.
	 */
	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTableModel(final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting paging = new DataTableSorting(params, connectionsMapper);
		Filtering filtering = new DataTableFiltering(params);

		PagedCollectionHolder<List<ConnectionLog>> holder = service.findAllFiltered(paging, filtering);

		return new ConnectionLogsDataTableModelHelper(locale, messageSource).buildDataModel(holder, params.getsEcho());
	}


	private static final class ConnectionLogsDataTableModelHelper extends DataTableModelBuilder<ConnectionLog> {
		private InternationalizationHelper messageSource;
		private Locale locale;

		private ConnectionLogsDataTableModelHelper(Locale locale, InternationalizationHelper messageSource) {
			this.locale = locale;
			this.messageSource = messageSource;
		}

		@Override
		protected Map<String, Object> buildItemData(ConnectionLog item) {
			Map<String, Object> res = new HashMap<>();
			res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("login", item.getLogin());
			res.put("connection-date", messageSource.localizeDate(item.getConnectionDate(), locale));
			res.put("successful", item.getSuccess());
			return res;
		}
	}
}

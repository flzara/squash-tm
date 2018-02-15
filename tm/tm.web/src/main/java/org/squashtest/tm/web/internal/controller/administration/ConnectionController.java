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
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.squashtest.tm.service.connectionhistory.ConnectionLogExportService;
import org.squashtest.tm.service.connectionhistory.ConnectionLogFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.*;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
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

	private static final String FILENAME = "filename";
	private static final String LOGIN = "login";
	private static final String ID = "id";
	private static final String CONNECTION_DATE_KEY = "connection-date";
	private static final String CONNECTION_DATE_ATTRIBUTE = "connectionDate";
	private static final String SUCCESS_KEY = "successful";
	private static final String SUCCESS_ATTRIBUTE = "success";


	@Inject
	protected InternationalizationHelper messageSource;

	@Inject
	private ConnectionLogFinderService connectionLogFinderServiceservice;
	@Inject
	private ConnectionLogExportService connectionLogExportService;

	private DatatableMapper<String> connectionsMapper = new NameBasedMapper(9)
		.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, ID, ConnectionLog.class)
		.mapAttribute(LOGIN, LOGIN, ConnectionLog.class)
		.mapAttribute(CONNECTION_DATE_KEY, CONNECTION_DATE_ATTRIBUTE, ConnectionLog.class)
		.mapAttribute(SUCCESS_KEY, SUCCESS_ATTRIBUTE, ConnectionLog.class);

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
		ColumnFiltering columnFiltering = new DataTableColumnFiltering(params);

		PagedCollectionHolder<List<ConnectionLog>> holder = connectionLogFinderServiceservice.findAllFiltered(paging, columnFiltering);

		return new ConnectionLogsDataTableModelHelper(locale, messageSource).buildDataModel(holder, params.getsEcho());
	}

	/**
	 * Return a csv file with chosen connection logs.
	 * @param filename the desired name for csv file
	 * @param params the {@link DataTableDrawParameters} for the connection logs table
	 * @param response the html response to be send
	 * @return the csv file with filtered {@link ConnectionLog} infos.
	 */
	@ResponseBody
	@RequestMapping(value = "/exports", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	public FileSystemResource exportConnectionHistoryExcel(@RequestParam(FILENAME) String filename,
													 final DataTableDrawParameters params, HttpServletResponse response) {

		ColumnFiltering columnFiltering = new DataTableColumnFiltering(params);

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

		File export = connectionLogExportService.exportConnectionLogsToCsv(columnFiltering);

		return new FileSystemResource(export);
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
			res.put(LOGIN, item.getLogin());
			res.put(CONNECTION_DATE_KEY, messageSource.localizeDate(item.getConnectionDate(), locale));
			res.put(SUCCESS_KEY, item.getSuccess());
			return res;
		}
	}
}

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
package org.squashtest.tm.service.internal.campaign.export;

import java.util.List;

import org.squashtest.tm.domain.campaign.export.CampaignExportCSVModel.Cell;
import org.squashtest.tm.domain.campaign.export.CampaignExportCSVModel.Row;

/**
 * Factored out of WhateverCampaignCSVModelImplImpl
 *
 * @author Gregory Fouquet (refactoring)
 * @since 1.14  16/03/16
 */
class RowImpl implements Row {
	private final char separator;
	private final List<? extends Cell> cells;

	@Override
	@SuppressWarnings("unchecked")
	public List<Cell> getCells() {
		return (List<Cell>) cells;
	}

	public RowImpl(List<? extends Cell> cells, char separator) {
		this.cells = cells;
		this.separator = separator;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String strSeparator = String.valueOf(separator);

		for (Cell cell : cells) {
			String value = cell.getValue();
			if (value == null) {
				String escaped = "";
			} else {
				String escaped = value.replaceAll(strSeparator, ";");
				/* Issue #6509:
				 * Inserting quotation mark around each cell value
				 * so one can put semicolons in cells values. */
				builder.append("\"")
					.append(escaped)
					.append("\"")
					.append(separator);
			}
			}


		return builder.toString().replaceAll(separator + "$", "");
	}
}



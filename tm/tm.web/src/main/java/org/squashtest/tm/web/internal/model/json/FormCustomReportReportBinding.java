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
package org.squashtest.tm.web.internal.model.json;

import org.squashtest.tm.domain.customreport.CustomReportReportBinding;

public class FormCustomReportReportBinding {

	private Long id;

	private Long reportNodeId;

	private Long dashboardNodeId;

	private int row;

	private int col;

	private int sizeX;

	private int sizeY;

	public FormCustomReportReportBinding() {
		//NOOP
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDashboardNodeId() {
		return dashboardNodeId;
	}

	public void setDashboardNodeId(Long dashboardId) {
		this.dashboardNodeId = dashboardId;
	}

	public Long getReportNodeId() {
		return reportNodeId;
	}

	public void setReportNodeId(Long reportNodeId) {
		this.reportNodeId = reportNodeId;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getSizeX() {
		return sizeX;
	}

	public void setSizeX(int sizeX) {
		this.sizeX = sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public void setSizeY(int sizeY) {
		this.sizeY = sizeY;
	}

	public CustomReportReportBinding convertToEntity (){
		CustomReportReportBinding crrb = new CustomReportReportBinding();

		crrb.setCol(col);
		crrb.setRow(row);
		crrb.setSizeX(sizeX);
		crrb.setSizeY(sizeY);

		return crrb;
	}
}

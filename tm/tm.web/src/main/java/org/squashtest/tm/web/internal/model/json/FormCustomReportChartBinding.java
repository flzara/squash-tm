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

import org.squashtest.tm.domain.customreport.CustomReportChartBinding;

/**
 * Pojo for creating a new {@link CustomReportChartBinding}.
 * Different from {@link JsonCustomReportChartBinding} because here, we have the TREE NODE id and not the id of
 * chart definition
 * @author jthebault
 *
 */
public class FormCustomReportChartBinding {
	
	private Long id;
	
	private Long chartNodeId;
	
	private Long dashboardNodeId;
	
	private int row;
	
	private int col;
	
	private int sizeX;
	
	private int sizeY;
	
	public FormCustomReportChartBinding() {
		// TODO Auto-generated constructor stub
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

	public Long getChartNodeId() {
		return chartNodeId;
	}

	public void setChartNodeId(Long chartNodeId) {
		this.chartNodeId = chartNodeId;
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
	
	public CustomReportChartBinding convertToEntity (){
		CustomReportChartBinding crcb = new CustomReportChartBinding();
		
		crcb.setCol(col);
		crcb.setRow(row);
		crcb.setSizeX(sizeX);
		crcb.setSizeY(sizeY);
		
		return crcb;
	}
}

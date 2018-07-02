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
package org.squashtest.tm.domain.customreport;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.DocumentId;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Entity
public class CustomReportChartBinding implements Identified {

	@Id
	@Column(name = "CRCB_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_report_chart_binding_crcb_id_seq")
	@SequenceGenerator(name = "custom_report_chart_binding_crcb_id_seq", sequenceName = "custom_report_chart_binding_crcb_id_seq", allocationSize = 1)
	@DocumentId
	private Long id;

	@NotNull
	@ManyToOne(cascade=CascadeType.DETACH)
	@JoinColumn(name = "CRD_ID", referencedColumnName = "CRD_ID")
	private CustomReportDashboard dashboard;

	@ManyToOne
	@JoinColumn(name = "CHART_ID", referencedColumnName = "CHART_ID")
	private ChartDefinition chart;

	private int row;

	private int col;

	private int sizeX;

	private int sizeY;

	public CustomReportDashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(CustomReportDashboard dashboard) {
		this.dashboard = dashboard;
	}

	public ChartDefinition getChart() {
		return chart;
	}

	public void setChart(ChartDefinition chart) {
		this.chart = chart;
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

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean hasMoved(CustomReportChartBinding transientBinding) {
		return hasMoveRow(transientBinding) || hasMoveCol(transientBinding) || hasMoveSizeX(transientBinding) || hasMoveSizeY(transientBinding);
	}

	public void move(CustomReportChartBinding transientBinding) {
		setRow(transientBinding.getRow());
		setCol(transientBinding.getCol());
		setSizeX(transientBinding.getSizeX());
		setSizeY(transientBinding.getSizeY());
	}

	@AclConstrainedObject
	public CustomReportLibrary getCustomReportLibrary(){
		return getDashboard().getProject().getCustomReportLibrary();
	}

	private boolean hasMoveSizeX(CustomReportChartBinding transientBinding) {
		return getSizeX()!=transientBinding.getSizeX();
	}

	private boolean hasMoveSizeY(CustomReportChartBinding transientBinding) {
		return getSizeY()!=transientBinding.getSizeY();
	}

	private boolean hasMoveCol(CustomReportChartBinding transientBinding) {
		return getRow()!=transientBinding.getRow();
	}

	private boolean hasMoveRow(CustomReportChartBinding transientBinding) {
		return getCol()!=transientBinding.getCol();
	}

	public CustomReportChartBinding createCopy() {
		CustomReportChartBinding copy = new CustomReportChartBinding();
		copy.setChart(this.getChart());
		copy.setDashboard(this.getDashboard());
		copy.move(this);
		return copy;
	}
}

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

import java.util.HashSet;
import java.util.Set;

/**
 * JSON used to forward to client side all the data needed to render a complete dashboard.
 * Avoids n requests where n = number of charts in dashboard, as all charts datas are retrieved in on request
 * @author jthebault
 *
 */
public class JsonCustomReportDashboard {
	
	private Long id;
	
	private String name;
	
	private String createdBy;
	
	private String lastModifiedBy;
	
	//Date formating is done server side
	private String createdOn;
	
	//Date formating is done server side
	private String lastModifiedOn;
	
	private Set<JsonCustomReportChartBinding> chartBindings = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the lastModifiedBy
	 */
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	/**
	 * @param lastModifiedBy the lastModifiedBy to set
	 */
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(String lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public Set<JsonCustomReportChartBinding> getChartBindings() {
		return chartBindings;
	}

	public void setChartBindings(Set<JsonCustomReportChartBinding> chartBindings) {
		this.chartBindings = chartBindings;
	}

}

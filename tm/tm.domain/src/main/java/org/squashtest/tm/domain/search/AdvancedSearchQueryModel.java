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
package org.squashtest.tm.domain.search;

import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;

public class AdvancedSearchQueryModel {

	private Pageable pageable;

	private Map<Integer, Object> mDataProp = new HashMap<>();

	private AdvancedSearchModel model;

	public AdvancedSearchQueryModel() {
	}

	public AdvancedSearchQueryModel(Pageable pageable, Map<Integer, Object> mDataProp, AdvancedSearchModel model) {
		this.pageable = pageable;
		this.mDataProp = mDataProp;
		this.model = model;
	}

	public Pageable getPageable() {
		return pageable;
	}

	public void setPageable(Pageable pageable) {
		this.pageable = pageable;
	}

	public Map<Integer, Object> getmDataProp() {
		return mDataProp;
	}

	public void setmDataProp(Map<Integer, Object> mDataProp) {
		this.mDataProp = mDataProp;
	}

	public AdvancedSearchModel getModel() {
		return model;
	}

	public void setModel(AdvancedSearchModel model) {
		this.model = model;
	}
}

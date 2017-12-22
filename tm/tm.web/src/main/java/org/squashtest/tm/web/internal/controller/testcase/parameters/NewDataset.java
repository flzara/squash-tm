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
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.testcase.ParameterFinder;

/**
 * @author mpagnon
 * 
 */
public class NewDataset extends Dataset {
	private Object[][] paramValues;

	public NewDataset() {
		
	}

	public Dataset createTransientEntity(TestCase testCase, ParameterFinder parameterFinder) {
		Dataset dataset =  new Dataset();
		dataset.setName(getName());
		for(Object[] paramValue : paramValues) {
			Number intParam = (Number) paramValue[0];
			Parameter parameter = parameterFinder.findById(intParam.longValue());
			new DatasetParamValue(parameter, dataset, (String) paramValue[1]);
		}
		return dataset;
	}

	public Object[][] getParamValues() {
		return paramValues;
	}

	public void setParamValues(Object[][] paramValues) {
		this.paramValues = paramValues;
	}
	

}

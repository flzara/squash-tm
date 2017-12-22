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
package org.squashtest.tm.domain.testcase;

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
import javax.validation.constraints.Size;

import org.squashtest.tm.domain.Identified;

@Entity
public class DatasetParamValue implements Identified {
	@Id
	@Column(name = "DATASET_PARAM_VALUE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "dataset_param_value_dataset_param_value_id_seq")
	@SequenceGenerator(name = "dataset_param_value_dataset_param_value_id_seq", sequenceName = "dataset_param_value_dataset_param_value_id_seq", allocationSize = 1)
	private Long id;

	@NotNull
	@ManyToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "PARAM_ID", referencedColumnName = "PARAM_ID")
	private Parameter parameter;

	@ManyToOne
	@JoinColumn(name = "DATASET_ID", referencedColumnName = "DATASET_ID")
	private Dataset dataset;

	@NotNull
	@Size(max = 255)
	private String paramValue = "";

	public DatasetParamValue() {
		super();
	}

	public DatasetParamValue(Parameter parameter, Dataset dataset) {
		this();
		this.parameter = parameter;
		this.dataset = dataset;
		this.dataset.addParameterValue(this);
	}

	public DatasetParamValue(Parameter parameter, Dataset dataset, String paramValue) {
		this(parameter, dataset);
		this.paramValue = paramValue;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	@Override
	public Long getId() {
		return id;
	}

}

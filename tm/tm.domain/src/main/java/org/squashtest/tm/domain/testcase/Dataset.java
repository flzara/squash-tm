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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"NAME","TEST_CASE_ID"})})
public class Dataset implements Identified {
	public static final int MAX_NAME_SIZE = Sizes.NAME_MAX;
	@Id
	@Column(name = "DATASET_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "dataset_dataset_id_seq")
	@SequenceGenerator(name = "dataset_dataset_id_seq", sequenceName = "dataset_dataset_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Size(min = 0, max = MAX_NAME_SIZE)
	private String name;


	@ManyToOne
	@JoinColumn(name = "TEST_CASE_ID", referencedColumnName = "TCLN_ID")
	private TestCase testCase;

	@NotNull
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy="dataset")
	private Set<DatasetParamValue> parameterValues = new HashSet<>(0);

	public Dataset() {
	}

	public Dataset(String name, @NotNull TestCase testCase) {
		super();
		this.name = name;
		this.testCase = testCase;
		this.testCase.addDataset(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(@NotNull TestCase testCase) {
		this.testCase = testCase;
	}

	@Override
	public Long getId() {
		return id;
	}

	public Set<DatasetParamValue> getParameterValues() {
		return Collections.unmodifiableSet(this.parameterValues);
	}

	public void addParameterValue(@NotNull DatasetParamValue datasetParamValue) {
		this.parameterValues.add(datasetParamValue);
	}

	public void removeParameterValue(@NotNull DatasetParamValue datasetParamValue) {
		this.parameterValues.remove(datasetParamValue);
	}

}

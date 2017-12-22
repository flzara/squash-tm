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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.exception.DuplicateNameException;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "TEST_CASE_ID" }) })
public class Parameter implements Identified {

	private static final String PARAM_REGEXP = "[A-Za-z0-9_-]{1,255}";
	public static final String NAME_REGEXP = "^" + PARAM_REGEXP + "$";
	public static final int MIN_NAME_SIZE = 1;
	public static final int MAX_NAME_SIZE = Sizes.NAME_MAX;

	public static final String USAGE_PREFIX = "${";
	public static final String USAGE_SUFFIX = "}";
	public static final String USAGE_PATTERN = "\\Q" + USAGE_PREFIX + "\\E(" + PARAM_REGEXP + ")\\Q" + USAGE_SUFFIX
			+ "\\E";

	@Id
	@Column(name = "PARAM_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "parameter_param_id_seq")
	@SequenceGenerator(name = "parameter_param_id_seq", sequenceName = "parameter_param_id_seq", allocationSize = 1)
	private Long id;

	@NotBlank
	@Pattern(regexp = NAME_REGEXP)
	@Size(min = MIN_NAME_SIZE, max = MAX_NAME_SIZE)
	private String name;

	@Lob
	@Type(type="org.hibernate.type.TextType")
	private String description = "";

	@ManyToOne
	@JoinColumn(name = "TEST_CASE_ID", referencedColumnName = "TCLN_ID")
	private TestCase testCase;

	@OneToMany(mappedBy = "parameter", cascade = { CascadeType.REMOVE })
	private List<DatasetParamValue> datasetParamValues = new ArrayList<>();

	public Parameter() {
		super();
	}

	public Parameter(String name) {
		this();
		this.name = name;
	}

	public Parameter(String name, @NotNull TestCase testCase) {
		this(name);
		this.testCase = testCase;
		this.testCase.addParameter(this);
	}

	/**
	 * A detached copy means it belong to no test case yet
	 * @return
	 */
	public Parameter detachedCopy(){
		Parameter p = new Parameter(name);
		p.setDescription(description);
		return p;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		if (this.name != null) {
			if (!this.name.equals(newName)) {
				checkForHomonymesAndUpdateSteps(newName);
				this.name = newName;
			}
		} else {
			this.name = newName;
		}

	}

	private void checkForHomonymesAndUpdateSteps(String newName) {
		if (this.testCase != null) {
			Parameter homonyme = this.testCase.findParameterByName(newName);
			if (homonyme != null) {
				throw new DuplicateNameException(this.name, newName);
			}
			updateParamNameInSteps(newName);

		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(@NotNull String description) {
		this.description = description;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	/**
	 * This method set the test case of this parameter with the given test case and add this to the given test case's
	 * parameters list.
	 *
	 * @see TestCase#addParameter(Parameter)
	 *
	 * @param testCase
	 */
	public void setTestCase(@NotNull TestCase testCase) {
		this.testCase = testCase;
		this.testCase.addParameter(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	/**
	 * Returns {@link Parameter#USAGE_PREFIX} + {@link Parameter#name} + {@link Parameter#USAGE_SUFFIX}
	 *
	 * @return
	 */
	public String getParamStringAsUsedInStep() {
		return getParamStringAsUsedInStep(this.name);
	}

	private void updateParamNameInSteps(String newName) {
		if (this.getTestCase() != null) {
			for (TestStep step : this.getTestCase().getSteps()) {
				step.accept(new ParameterNameInStepUpdater(this.name, newName));
			}
		}
	}

	/**
	 * Returns {@link Parameter#USAGE_PREFIX} + p + {@link Parameter#USAGE_SUFFIX}
	 *
	 * @param parameterName
	 * @return
	 */
	protected static String getParamStringAsUsedInStep(String parameterName) {
		return Parameter.USAGE_PREFIX + parameterName + Parameter.USAGE_SUFFIX;
	}

	protected static Set<String> findUsedParameterNamesInString(String content) {
		Set<String> paramNames = new HashSet<>();
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(Parameter.USAGE_PATTERN);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			paramNames.add(matcher.group(1));
		}
		return paramNames;
	}

	public static Parameter createBlankParameter() {
		Parameter res = new Parameter();

		res.name = null;
		res.description = null;

		return res;
	}
}

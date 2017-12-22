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
package org.squashtest.tm.plugin.testautomation.jenkins.beans;

public class Parameter {

	public static final String SYMBOLIC_FILENAME = "testsuite.json";

	protected String name;
	protected String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Parameter() {
		super();
	}

	public Parameter(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public static Parameter operationTestListParameter() {
		return new Parameter("operation", "list");
	}

	public static Parameter operationRunSuiteParameter() {
		return new Parameter("operation", "run");
	}

	public static Parameter newExtIdParameter(String externalId) {
		return new Parameter("externalJobId", externalId);
	}

	public static Parameter newCallbackURlParameter(String callback) {
		return new Parameter("notificationURL", callback);
	}

	public static Parameter testListParameter() {
		// not invented here : json marshaller
		return new Parameter("testList", "{file:" + SYMBOLIC_FILENAME + "}");
	}

	public static Parameter executorParameter(String executor) {
		return new Parameter("executor", executor);
	}

	// GENERATED:START
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Parameter other = (Parameter) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
	// GENERATED:END
}

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
package org.squashtest.tm.api.report.form;

import java.util.Collections;
import java.util.List;

import org.squashtest.tm.core.foundation.i18n.Labelled;

/**
 * This input holds other inputs but has no value.
 * 
 * @author Gregory Fouquet
 * 
 */
public class InputsGroup extends Labelled implements Input {
	private List<Input> inputs = Collections.emptyList();

	/**
	 * Please refer to {@link BasicInput#disabledBy}
	 */
	private String disabledBy;

	/**
	 * 
	 */
	public InputsGroup() {
		super();
	}

	/**
	 * @see org.squashtest.tm.api.report.form.Input#getName()
	 */
	@Override
	public String getName() {
		return "";
	}

	/**
	 * @see org.squashtest.tm.api.report.form.Input#getType()
	 */
	@Override
	public InputType getType() {
		return InputType.INPUTS_GROUP;
	}

	/**
	 * The inputs hold by this container.
	 * 
	 * @param inputs
	 *            the options to set
	 */
	public void setInputs(List<Input> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @return the options
	 */
	public List<Input> getInputs() {
		return inputs;
	}

	public String getDisabledBy() {
		return disabledBy;
	}

	public void setDisabledBy(String disabledBy) {
		this.disabledBy = disabledBy;
	}



}

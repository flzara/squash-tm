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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;

/**
 * Option which contains another input.
 *
 * This class extends OptionInput for api compatibility reason. In a perfect world it would only implement a common
 * interface.
 *
 * @author Gregory Fouquet
 *
 */
public class ContainerOption<I extends Input> extends OptionInput {
	private I content;

	/**
	 *
	 */
	public ContainerOption() {
		super();
	}

	/**
	 * @return the content
	 */
	public I getContent() {
		return content;
	}

	@PostConstruct
	public void initialize() {
		if (getContent() == null) {
			throw new IllegalStateException(
					"Container option has no content. Maybe you forgot to set its <property name=\"content\" />");
		}

		if (StringUtils.isNotBlank(content.getName())) {
			super.setGivesAccessTo(content.getName());
		}
	}

	@Override
	public void setGivesAccessTo(String s) {
		// this kinda breaks the LSP but we have no choice because of unwanted inheritance.
		throw new IllegalArgumentException(
				"This property is automatically set according to the option's content. You should not try to manually set it");
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(I content) {
		this.content = content;
	}
}
